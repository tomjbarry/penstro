package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class TagLockedException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -532630559709314778L;

	public TagLockedException(String tag) {
		super(String.format(ExceptionMessages.TAG_LOCKED, tag));
	}
}
