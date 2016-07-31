package com.yingjun.ssm.api.goods.service.impl;

import com.alibaba.dubbo.rpc.RpcException;
import com.yingjun.ssm.api.goods.entity.Goods;
import com.yingjun.ssm.api.goods.service.GoodsService;
import com.yingjun.ssm.api.user.entity.User;
import com.yingjun.ssm.api.user.enums.UserExceptionEnum;
import com.yingjun.ssm.api.user.service.UserService;
import com.yingjun.ssm.common.enums.BizExceptionEnum;
import com.yingjun.ssm.common.model.MailParam;
import com.yingjun.ssm.common.util.cache.RedisCache;
import com.yingjun.ssm.core.goods.dao.GoodsDao;
import com.yingjun.ssm.core.goods.dao.OrderDao;
import com.yingjun.ssm.core.goods.mq.MQProducer;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yingjun
 */
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
    @Autowired
    private MQProducer mqProducer;

    @Override
    public List<Goods> getGoodsList(int offset, int limit) {
        String cache_key = RedisCache.CAHCENAME + "|getGoodsList|" + offset + "|" + limit;
        List<Goods> result_cache = cache.getListCache(cache_key, Goods.class);
        if (result_cache != null)
            LOG.info("get cache with key:" + cache_key);
        else {
            //缓存中没有再去数据库取，并插入缓存（缓存时间为60秒）
            result_cache = goodsDao.queryAll(offset, limit);
            cache.putListCacheWithExpireTime(cache_key, result_cache, RedisCache.CAHCETIME);
            LOG.info("put cache with key:" + cache_key);
        }
        return result_cache;
    }

    @Transactional
    @Override
    public void buyGoods(long userPhone, long goodsId, boolean useProcedure) {
        LOG.info("buyGoods"+userPhone);
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
                    // 邮件发送
                    //通过消息中心，发送邮件告知用户
                    MailParam mail = new MailParam();
                    mail.setTo("yingjunv_c@126.com");
                    mail.setSubject("订单确认");
                    mail.setContent("你通过手机号：" + userPhone + "下单成功！");
                    try {
                        mqProducer.sendMailMessage(mail);
                    } catch (Exception e) {
                        throw new RpcException(BizExceptionEnum.INNER_ERROR.getState(),
                                BizExceptionEnum.INNER_ERROR.getMsg());
                    }
                }
            }
        }
    }

    /**
     * 通过一系列写操作
     * 没有什么实际的功能需求
     * 仅仅是为了测试下分布式事务
     * <p>
     * TODO
     * 问题1：分布式系统异常处理的问题（自定义的RuntimeException无法被catch到）
     * 解决：采用dubbo的RpcException可以被远程catch到，实现分布式系统的异常传输。
     * <p>
     * 问题2：分布式事务存在问题
     * 解决(1)：通过事务协调器（TC）来解决，缺点：效率低(不采用)
     * 解决(2)：通过消息队列来避免分布式事务（采用）
     */
    @Transactional
    @Override
    public void testDistributedTransaction(long goodsid) {
        //这部分的事务可以由本地Spring事务管理到 出错了可以回滚
        int count = goodsDao.reduceNumber(goodsid);
        if (count <= 0) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
        count = orderDao.insertOrder(1000, goodsid, "普通买卖");
        if (count != 1) {
            throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                    BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }
        //userService是远程调用接口，无法由本独Spring事务管理 出错了无法回滚
        /*如下采用远程同步调用的方式，存在分布式事务的问题。
         count = userService.addScoreBySyn(100);
         if (count <= 0) {
             throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                     BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
        }*/
        /*如下采用远程异步调用的方式，存在分布式事务的问题。
        userService.addScoreByAsy(100);
        Future<Integer> future = RpcContext.getContext().getFuture();
        try {
            Integer addCount = future.get();
            if (addCount <= 0) {
                throw new RpcException(BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getState(),
                     BizExceptionEnum.DB_UPDATE_RESULT_ERROR.getMsg());
            }
        } catch (Exception e) {
            throw new RpcException(BizExceptionEnum.INNER_ERROR.getState(),
                    BizExceptionEnum.INNER_ERROR.getMsg());
        }*/
        //这里将userService的addScore操作通过消息中心去完成，避开分布式事务。
        try {
            mqProducer.sendBizMessage(goodsid);

        } catch (Exception e) {
            throw new RpcException(BizExceptionEnum.INNER_ERROR.getState(),
                    BizExceptionEnum.INNER_ERROR.getMsg());
        }
    }

}
