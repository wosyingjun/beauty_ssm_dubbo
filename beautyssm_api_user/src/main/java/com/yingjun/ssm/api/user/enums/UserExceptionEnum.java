package com.yingjun.ssm.api.user.enums;


/**
 * * 业务异常基类，所有业务异常都必须继承于此异常 定义异常时，需要先确定异常所属模块。 例如：无效用户可以定义为 [10010001]
 * 前四位数为系统模块编号，后4位为错误代码 ,唯一
 *
 * 注意和common包中的BizExceeptionEnum公用
 * 用户模块相关异常 1001
 *
 * @author yingjun
 *
 */
public enum UserExceptionEnum {

	INVALID_USER(1001001, "无效用户");

	private int state;

	private String msg;

	UserExceptionEnum(int state, String msg) {
		this.state = state;
		this.msg = msg;
	}

	public int getState() {
		return state;
	}

	public String getMsg() {
		return msg;
	}

	public static UserExceptionEnum stateOf(int index) {
		for (UserExceptionEnum state : values()) {
			if (state.getState() == index) {
				return state;
			}
		}
		return null;
	}

}
