package com.yingjun.ssm.web.user.controller;

import com.yingjun.ssm.api.user.entity.User;
import com.yingjun.ssm.api.user.service.UserService;
import com.yingjun.ssm.common.dto.BootStrapTableResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;


@Controller
@RequestMapping("/user")
public class UserController {

	private final Logger LOG = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private UserService userService;

	/*@RequestMapping(value = "/list", method = RequestMethod.GET)
	public String list(Model entity, Integer offset, Integer limit) {
		LOG.info("invoke----------/user/list");
		offset = offset == null ? 0 : offset;//默认便宜0
		limit = limit == null ? 50 : limit;//默认展示50条
		List<User> list = userService.getUserList(offset, limit);
		entity.addAttribute("userlist", list);
		return "userlist";
	}*/

	
	/**
	 * 摒弃jsp页面通过ajax接口做到真正意义上的前后分离 
	 * @param offset
	 * @param limit
	 * @return
	 */
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public BootStrapTableResult<User> list(Integer offset, Integer limit) {
		LOG.info("invoke----------/user/list");
		offset = offset == null ? 0 : offset;//默认便宜0
		limit = limit == null ? 50 : limit;//默认展示50条
		List<User> list = userService.getUserList(offset, limit);
		return new BootStrapTableResult<User>(list);
	}


}
