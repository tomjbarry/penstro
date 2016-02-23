package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class BadParameterException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1787204649467268636L;

	public BadParameterException() {
		super(ExceptionMessages.BADPARAMETER);
	}
	
	public BadParameterException(Throwable cause) {
		super(ExceptionMessages.BADPARAMETER, cause);
	}
	
}
