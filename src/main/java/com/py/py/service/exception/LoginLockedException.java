package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;


public class LoginLockedException extends org.springframework.security.core.AuthenticationException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4433912574919892383L;

	public LoginLockedException() {
		super(ExceptionMessages.LOCKED);
	}
}
