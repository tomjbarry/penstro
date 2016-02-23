package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class BlockedException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6293870127230914119L;

	public BlockedException() {
		super(ExceptionMessages.BLOCKED);
	}
}
