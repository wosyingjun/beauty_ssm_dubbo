package com.yingjun.ssm.api.goods.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.yingjun.ssm.api.goods.utils.CustomDateSerializer;
import com.yingjun.ssm.api.user.entity.User;

import java.io.Serializable;
import java.util.Date;

/**
 * 订单
 * @author yingjun
 *
 */
public class Order implements Serializable {
	
	private long orderId;
	
	private User user;
	
	private long goodsId;
	
	private String title;
	
	@JsonSerialize(using = CustomDateSerializer.class)  
	private Date createTime;


	public User getUser() {
		return user;
	}


	public void setUser(User user) {
		this.user = user;
	}


	public long getOrderId() {
		return orderId;
	}


	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public Date getCreateTime() {
		return createTime;
	}


	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}


	public long getGoodsId() {
		return goodsId;
	}


	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}


	@Override
	public String toString() {
		return "Order [user=" + user + ", orderId=" + orderId + ", goodsId=" + goodsId + ", title=" + title + ", createTime=" + createTime + "]";
	}



	
	
}
