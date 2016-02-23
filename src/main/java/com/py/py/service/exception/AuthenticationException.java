package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class AuthenticationException extends ServiceException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5524610736164942213L;

	public AuthenticationException() {
		super(ExceptionMessages.AUTHENTICATION);
	}
	
}
