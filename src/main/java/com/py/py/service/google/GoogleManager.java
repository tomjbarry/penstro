package com.py.py.service.google;

import com.py.py.service.exception.ServiceException;

public interface GoogleManager {

	boolean verifyRecaptchaResponse(String recaptchaResponse, String ipAddress)
			throws ServiceException;

}
