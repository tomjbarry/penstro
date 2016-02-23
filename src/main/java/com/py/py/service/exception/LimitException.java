package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class LimitException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2762825707799872365L;

	public LimitException() {
		super(ExceptionMessages.LIMIT);
	}
	
}
