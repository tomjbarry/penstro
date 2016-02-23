package com.py.py.service.exception;

import com.py.py.service.exception.ServiceException;
import com.py.py.service.exception.constants.ExceptionMessages;

public class NotFoundException extends ServiceException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -838943619904294473L;

	public NotFoundException(String id) {
		super(String.format(ExceptionMessages.NOTFOUND,id));
	}
	
}
