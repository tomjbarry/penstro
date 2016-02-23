package com.py.py.security.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthenticationParameterException extends AuthenticationException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1652418422223956814L;

	public AuthenticationParameterException(String msg) {
		super(msg);
	}

	
	
}
