package com.py.py.dao.util;

import org.bson.types.ObjectId;

import com.py.py.dao.exception.DaoException;

public class CheckUtil {
	
	public static void nullCheck(Object... args) throws DaoException {
		for(Object arg : args) {
			if(arg == null) {
				throw new DaoException();
			}
		}
	}
	
	public static void objectIdCheck(String... args) throws DaoException {
		nullCheck((Object[])args);
		for(String arg : args) {
			if(!ObjectId.isValid(arg)) {
				throw new DaoException();
			}
		}
	}
}
