package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class PaymentTargetException extends PaymentException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8117533808204493417L;

	public PaymentTargetException() {
		super(ExceptionMessages.PAYMENT_TARGET);
	}
}
