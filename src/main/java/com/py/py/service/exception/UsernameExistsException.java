package com.py.py.service.exception;

public class UsernameExistsException extends ExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2336397101243087774L;

	public UsernameExistsException(String id) {
		super(id);
	}
	
}
