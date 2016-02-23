package com.py.py.service.exception;

import com.py.py.service.exception.ServiceException;
import com.py.py.service.exception.constants.ExceptionMessages;

public class ExistsException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7323424940782645875L;

	public ExistsException(String id) {
		super(String.format(ExceptionMessages.EXISTS, id));
	}
	
}
