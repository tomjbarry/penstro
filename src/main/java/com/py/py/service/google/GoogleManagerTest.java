package com.py.py.service.google;

import com.py.py.service.exception.ServiceException;

public class GoogleManagerTest implements GoogleManager {

	@Override
	public boolean verifyRecaptchaResponse(String recaptchaResponse,
			String ipAddress) throws ServiceException {
		return true;
	}

}
