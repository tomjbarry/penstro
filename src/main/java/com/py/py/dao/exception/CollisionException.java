package com.py.py.dao.exception;

import com.py.py.dao.exception.constants.ExceptionMessages;

public class CollisionException extends DaoException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2128912211145840906L;

	public CollisionException() {
		super(ExceptionMessages.COLLISION);
	}
	
}
