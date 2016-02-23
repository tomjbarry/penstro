package com.py.py.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationTheftException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1501659614230502557L;

	public AuthenticationTheftException(String message) {
		super(message);
	}

}
