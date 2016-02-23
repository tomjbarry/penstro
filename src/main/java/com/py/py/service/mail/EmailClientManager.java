package com.py.py.service.mail;

import com.py.py.domain.EmailTask;
import com.py.py.service.exception.ServiceException;

public interface EmailClientManager {

	void sendEmail(EmailDetails details, EmailTask task, String username, String emailToken) throws ServiceException;
	
	String generateEmailToken(EmailTask task) throws ServiceException;
}
