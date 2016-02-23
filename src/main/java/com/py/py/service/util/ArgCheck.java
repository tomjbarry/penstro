package com.py.py.service.util;

import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.service.exception.BadParameterException;

public class ArgCheck {
	
	public static void nullCheck(Object... args) throws BadParameterException {
		try {
			CheckUtil.nullCheck(args);
		} catch(Exception e) {
			throw new BadParameterException();
		}
	}
	
	public static void objectIdCheck(String... args) throws BadParameterException {
		try {
			CheckUtil.objectIdCheck(args);
		} catch(Exception e) {
			throw new BadParameterException();
		}
	}
	
	public static void userCheck(User... users) throws BadParameterException {
		nullCheck((Object[])users);
		for(User u : users) {
			nullCheck(u.getId(), u.getUsername());
		}
	}
	
	public static void tagCheck(Tag... tags) throws BadParameterException {
		nullCheck((Object[])tags);
		for(Tag tag : tags) {
			nullCheck(tag.getId());
			nullCheck(tag.getId().getName(), tag.getId().getLanguage());
		}
	}
}
