package com.py.py.service.exception;

import com.py.py.service.exception.constants.ExceptionMessages;

public class FeatureDisabledException extends ServiceException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4327910909064768679L;

	public FeatureDisabledException() {
		super(ExceptionMessages.FEATURE_DISABLED);
	}

}
