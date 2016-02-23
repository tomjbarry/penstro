package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class ObjectLockedException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9170823579839083684L;

	public ObjectLockedException() {
		super(ExceptionMessages.OBJECT_LOCKED);
	}
}
