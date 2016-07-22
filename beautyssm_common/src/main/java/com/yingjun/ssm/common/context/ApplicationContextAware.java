package com.yingjun.ssm.common.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * 描述:获取spring上下文
 * 
 * @author yingjun
 * 
 */
@Lazy(value = false)
@Component
public class ApplicationContextAware implements org.springframework.context.ApplicationContextAware {
	
	private static ApplicationContext ctx;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		ApplicationContextAware.ctx = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return ctx;
	}
}
