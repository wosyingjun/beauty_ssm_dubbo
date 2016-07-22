package com.yingjun.ssm.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 功能描述：ThreadUtil
 */

public class ThreadUtil {

	@SuppressWarnings("rawtypes")
	private static final ThreadLocal ctx = new ThreadLocal();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void put(Object key, Object value) {
		Map m = (Map) ctx.get();
		if (m == null) {
			m = new HashMap();
		}
		m.put(key, value);
		ctx.set(m);
	}

	public static Object get(Object key) {
		@SuppressWarnings("rawtypes")
		Map m = (Map) ctx.get();
		if (m != null) {
			return m.get(key);
		}
		return null;
	}

}
