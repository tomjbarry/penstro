package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class ActionNotAllowedException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3849768944930606019L;

	public ActionNotAllowedException() {
		super(ExceptionMessages.ACTION_NOT_ALLOWED);
	}
}
