package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class FinanceException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1743619983137523447L;

	public FinanceException() {
		super(ExceptionMessages.FINANCE);
	}
	
	public FinanceException(Throwable t) {
		super(ExceptionMessages.FINANCE, t);
	}
	
}
