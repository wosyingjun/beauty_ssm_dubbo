package com.yingjun.ssm.api.user.service;

import com.yingjun.ssm.api.user.entity.User;

import java.util.List;



public interface UserService {

	List<User> getUserList(int offset, int limit);

	User queryByPhone(long userPhone);

	/**
	 * 用于消费端同步调用（需要在dubbo-consumer.xml中配置async参数）
	 * @param score
	 * @return
     */
	int addScoreBySyn(int score);

	/**
	 * 用于消费端异步调用（需要在dubbo-consumer.xml中配置async参数）
	 * @param score
	 * @return
     */
	int addScoreByAsy(int score);

}
