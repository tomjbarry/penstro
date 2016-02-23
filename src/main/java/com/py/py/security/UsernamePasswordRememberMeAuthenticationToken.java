package com.py.py.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class UsernamePasswordRememberMeAuthenticationToken extends UsernamePasswordAuthenticationToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4281885354115743608L;

	private final boolean rememberMe;
	private final String clientAddress;
	
	public UsernamePasswordRememberMeAuthenticationToken(Object principal, Object credentials, String clientAddress, boolean rememberMe) {
		super(principal, credentials);
		this.rememberMe = rememberMe;
		this.clientAddress = clientAddress;
	}
	
	public String getClientAddress() {
		return clientAddress;
	}

	public boolean isRememberMe() {
		return rememberMe;
	}

}
