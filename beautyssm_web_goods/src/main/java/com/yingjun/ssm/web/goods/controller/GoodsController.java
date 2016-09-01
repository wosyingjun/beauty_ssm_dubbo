package com.yingjun.ssm.web.goods.controller;

import com.alibaba.dubbo.rpc.RpcException;
import com.yingjun.ssm.api.goods.entity.Goods;
import com.yingjun.ssm.api.goods.service.GoodsService;
import com.yingjun.ssm.common.dto.BaseResult;
import com.yingjun.ssm.common.dto.BootStrapTableResult;
import com.yingjun.ssm.common.enums.BizExceptionEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping("/goods")
public class GoodsController {

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private GoodsService goodsService;

	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model, Integer offset, Integer limit) {
		LOG.info("invoke----------/goods/list");
		offset = offset == null ? 0 : offset;//默认便宜0
		limit = limit == null ? 50 : limit;//默认展示50条
		List<Goods> list = goodsService.getGoodsList(offset, limit);
		model.addAttribute("goodslist", list);
		return "goodslist";
	}*/

    /**
     * 摒弃jsp页面通过ajax接口做到真正意义上的前后分离
     *
     * @param offset
     * @param limit
     * @return
     */
    @RequestMapping(value = "/list", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public BootStrapTableResult<Goods> list(Integer offset, Integer limit) {
        LOG.info("invoke----------/goods/list");
        offset = offset == null ? 0 : offset;//默认便宜0
        limit = limit == null ? 50 : limit;//默认展示50条
        List<Goods> list = goodsService.getGoodsList(offset, limit);
        return new BootStrapTableResult<Goods>(list);
    }


    @RequestMapping(value = "/{goodsId}/buy", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public BaseResult<Object> buy(@CookieValue(value = "userPhone", required = false) Long userPhone,
                                  @Valid Goods goods, BindingResult result, HttpSession httpSession) {
        LOG.info("invoke----------/" + goods.getGoodsId() + "/buy userPhone:" + userPhone);
        if (userPhone == null) {
            //return new BaseResult<Object>(false, UserExceptionEnum.INVALID_USER.getMsg());
            userPhone=18768128888L;
        }
        //Valid 参数验证
        if (result.hasErrors()) {
            String errorInfo = "[" + result.getFieldError().getField() + "]" + result.getFieldError().getDefaultMessage();
            return new BaseResult<Object>(false, errorInfo);
        }
        //这里纯粹是为了验证集群模式西的session共享功能上

        LOG.info("lastSessionTime:" + httpSession.getAttribute("sessionTime"));
        httpSession.setAttribute("sessionTime", System.currentTimeMillis());
        try {
            goodsService.buyGoods(userPhone, goods.getGoodsId(), false);
        } catch (RpcException e) {
            LOG.error(e.getMessage());
            return new BaseResult<Object>(false, e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new BaseResult<Object>(false, BizExceptionEnum.INNER_ERROR.getMsg());
        }
        return new BaseResult<Object>(true, null);
    }

    @RequestMapping(value = "/{goodsId}/testDistributedTransaction", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public BaseResult<Object> testDistributedTransaction(@Valid Goods goods, BindingResult result) {
        LOG.info("invoke----------/goods/testDistributedTransaction");
        //Valid 参数验证
        if (result.hasErrors()) {
            String errorInfo = "[" + result.getFieldError().getField() + "]" + result.getFieldError().getDefaultMessage();
            return new BaseResult<Object>(false, errorInfo);
        }
        try {
            goodsService.testDistributedTransaction(goods.getGoodsId());
        } catch (RpcException e) {
            LOG.error(e.getMessage());
            return new BaseResult<Object>(false, e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new BaseResult<Object>(false, BizExceptionEnum.INNER_ERROR.getMsg());
        }
        return new BaseResult<Object>(true, null);
    }


}
