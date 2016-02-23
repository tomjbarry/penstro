package com.py.py.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationExpiredException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2835008888853554785L;

	public AuthenticationExpiredException(String message, Throwable t) {
		super(message, t);
	}
}
