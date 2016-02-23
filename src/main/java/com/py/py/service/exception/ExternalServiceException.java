package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class ExternalServiceException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8374095447874196632L;

	public ExternalServiceException() {
		super(ExceptionMessages.EXTERNAL_SERVICE);
	}
	
}
