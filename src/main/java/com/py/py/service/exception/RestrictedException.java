package com.py.py.service.exception;

import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.exception.constants.ExceptionMessages;

public class RestrictedException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7258196498752163854L;
	protected RESTRICTED_TYPE type;
	
	public RestrictedException(RESTRICTED_TYPE type) {
		super(ExceptionMessages.RESTRICTED);
		this.type = type;
	}
	
	public RestrictedException(RESTRICTED_TYPE type, Throwable cause) {
		super(ExceptionMessages.RESTRICTED, cause);
		this.type = type;
	}
	
	public RESTRICTED_TYPE getType() {
		return type;
	}
}
