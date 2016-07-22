package com.yingjun.ssm.api.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.util.Date;



/**
 * 用户
 * @author yingjun
 *
 */
public class User implements Serializable{
	
	private long userId;
	
	private String userName;
	
	private long userPhone;
	
	//这里展示了jackson封装好的以及自定义的对时间格式的转换方式
	//后续对于一些复杂的转换可以自定义转换方式
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date createTime;
	
	private int score;

	public long getUserId() {
		return userId;
	}

	public void setUserId(long userId) {
		this.userId = userId;
	}


	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public long getUserPhone() {
		return userPhone;
	}

	public void setUserPhone(long userPhone) {
		this.userPhone = userPhone;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return "User [userId=" + userId + ", userName=" + userName + ", userPhone=" + userPhone + ", createTime=" + createTime + ", score=" + score
				+ "]";
	}

	

}
