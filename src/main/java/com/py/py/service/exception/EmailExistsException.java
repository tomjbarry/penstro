package com.py.py.service.exception;

public class EmailExistsException extends ExistsException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5179597053829449315L;

	public EmailExistsException(String id) {
		super(id);
	}
	
}
