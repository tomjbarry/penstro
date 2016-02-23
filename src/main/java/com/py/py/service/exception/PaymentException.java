package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class PaymentException extends ServiceException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 3002990467141962244L;

	public PaymentException() {
		super(ExceptionMessages.PAYMENT);
	}
	
	public PaymentException(String message) {
		super(message);
	}
}
