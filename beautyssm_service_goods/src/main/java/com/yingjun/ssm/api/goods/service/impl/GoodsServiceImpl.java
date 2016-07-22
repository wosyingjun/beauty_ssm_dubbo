package com.yingjun.ssm.api.goods.service.impl;

import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.yingjun.ssm.api.goods.entity.Goods;
import com.yingjun.ssm.api.goods.service.GoodsService;
import com.yingjun.ssm.api.user.entity.User;
import com.yingjun.ssm.api.user.enums.UserExceptionEnum;
import com.yingjun.ssm.api.user.service.UserService;
import com.yingjun.ssm.common.enums.BizExceptionEnum;
import com.yingjun.ssm.common.util.cache.RedisCache;
import com.yingjun.ssm.core.goods.dao.GoodsDao;
import com.yingjun.ssm.core.goods.dao.OrderDao;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

@Service("goodsService")
public class GoodsServiceImpl implements GoodsService {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private GoodsDao goodsDao;
    @Autowired
    private OrderDao orderDao;
    @Autowired
    private UserService userService;

    @Autowired
    private RedisCache cache;

    @Override
    public List<Goods> getGoodsList(int offset, int limit) {
        String cache_key = RedisCache.CAHCENAME + "|getGoodsList|" + offset + "|" + limit;
        List<Goods> result_cache = cache.getListCache(cache_key, Goods.class);
        if (result_cache == null) {
            //缓存中没有再去数据库取，并插入缓存（缓存时间为60秒）
            result_cache = goodsDao.queryAll(offset, limit);
            cache.putListCacheWithExpireTime(cache_key, result_cache, RedisCache.CAHCETIME);
            LOG.info("put cache with key:" + cache_key);
            return result_cache;
        } else {
            LOG.info("get cache with key:" + cache_key);
        }
        return result_cache;
    }

    @Transactional
    @Override
    public void buyGoods(long userPhone, long goodsId, boolean useProcedure) {
        // 用户校验
        User user = userService.queryByPhone(userPhone);
        if (user == null) {
            throw new RpcException(UserExceptionEnum.INVALID_USER.getState(),
                    UserExceptionEnum.INVALID_USER.getMsg());
        }
        if (useProcedure) {
            //通过存储方式的方法进行操作
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("userId", user.getUserId());
            map.put("goodsId", goodsId);
            map.put("title", "抢购");
            map.put("result", null);
            goodsDao.bugWithProcedure(map);
            int result = MapUtils.getInteger(map, "result", -1);
            if (result <= 0) {
                // 买卖失败
                throw new RpcException(BizExceptionEnum.INNER_ERROR.getState(),
                        BizExceptionEnum.INNER_ERROR.getMsg());
            } else {
                // 买卖成功
                // 此时缓存中的数据不是最新的，需要对缓存进行清理（具体的缓存策略还是要根据具体需求制定）
                cache.deleteCacheWithPattern(RedisCache.CAHCENAME + "|getGoodsList|*");
                LOG.info("delete cache with key:" + RedisCache.CAHCENAME + "|getGoodsList|*");
                return;
            }
        } else {
            int inserCount = orderDao.insertOrder(user.getUserId(), goodsId, "普通买卖");
            if (inserCount <= 0) {
                // 买卖失败
                throw new RpcException(BizExceptionEnum.DB_INSERT_RESULT_ERROR.getState(),
                        BizExceptionEnum.DB_INSERT_RESULT_ERROR.getMsg());
            } else {
                // 减库存
                int updateCount = goodsDao.reduceNumber(goodsId);
                if (updateCount <= 0) {
                    // 减库存失败
                    throw new RpcException(BizExceptionEnum.DB_SELECTONE_IS_NULL.getState(),
                            BizExceptionEnum.DB_SELECTONE_IS_NULL.getMsg());
                } else {
                    // 买卖成功
                    // 此时缓存中的数据不再是最新的，需要对缓存进行清理（具体的缓存策略还是要根据具体需求制定）
                    cache.deleteCacheWithPattern(RedisCache.CAHCENAME + "|getGoodsList|*");
                    LOG.info("delete cache with key:" + RedisCache.CAHCENAME + "|getGoodsList|*");
                    return;
                }
            }
        }
    }

    /**
     * 通过一系列写操作
     * 没有什么实际的功能需求
     * 仅仅是为了测试下分布式事务
     * 同步调用模式
     *
     * @param goodsid
     */
    @Transactional
    @Override
    //todo 分布式事务存在问题
    //todo 1、通过事务协调器（TC）来解决，缺点：效率低
    //todo 2、通过消息队列来避免分布式事务
    public void testDistributedTransaction(long goodsid) {
        int count = goodsDao.reduceNumber(goodsid);
        if (count <= 0) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
        //远程同步调用
        LOG.info("addScoreBySyn:startTime:" + System.currentTimeMillis());
        count = userService.addScoreBySyn(100);
        LOG.info("addScoreBySyn:endTime:" + System.currentTimeMillis());
        if (count <= 0) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
        count = orderDao.insertOrder(1000, goodsid, "普通买卖");
        if (count != 1) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
    }


    /**
     * 通过一系列写操作
     * 没有什么实际的功能需求
     * 仅仅是为了测试下分布式事务
     * 异步调用模式
     *
     * @param goodsid
     */
    @Transactional
    @Override
    //todo 分布式事务存在问题
    public void testDistributedTransactionByAsy(long goodsid) {
        int count = goodsDao.reduceNumber(goodsid);
        if (count <= 0) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
        //远程异步调用
        LOG.info("addScoreByAsy:startTime:" + System.currentTimeMillis());
        userService.addScoreByAsy(100);
        LOG.info("addScoreByAsy:endTime:" + System.currentTimeMillis());

        count = orderDao.insertOrder(1000, goodsid, "普通买卖");
        if (count != 1) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }

        Future<Integer> future = RpcContext.getContext().getFuture();
        try {
            Integer addCount = future.get();
        } catch (Exception e) {
            throw new RpcException(BizExceptionEnum.INNER_ERROR.getState(),
                    BizExceptionEnum.INNER_ERROR.getMsg());
        }
        if (count <= 0) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
    }


}
