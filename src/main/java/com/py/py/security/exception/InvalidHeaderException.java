package com.py.py.security.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidHeaderException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1326082599425790306L;

	public InvalidHeaderException(String message) {
		super(message);
	}

}
