package com.yingjun.ssm.common.model;

/**
 * @author yingjun
 */
public class BizOperator {

    private String operator;
    private long goodsid;

    public BizOperator(String operator, long goodsid) {
        this.operator = operator;
        this.goodsid = goodsid;
    }

    public BizOperator() {
    }

    public long getGoodsid() {
        return goodsid;
    }

    public void setGoodsid(long goodsid) {
        this.goodsid = goodsid;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

}
