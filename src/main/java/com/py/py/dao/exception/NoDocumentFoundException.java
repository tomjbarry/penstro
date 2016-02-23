package com.py.py.dao.exception;

import com.py.py.dao.exception.constants.ExceptionMessages;

public class NoDocumentFoundException extends DaoException {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4741202560538170380L;

	public NoDocumentFoundException() {
		super(ExceptionMessages.NO_DOCUMENT);
	}
}
