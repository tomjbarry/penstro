package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;


public class TagCountException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8185389372424320461L;

	public TagCountException(String tag) {
		super(String.format(ExceptionMessages.TAG_COUNT, tag));
	}
	
}
