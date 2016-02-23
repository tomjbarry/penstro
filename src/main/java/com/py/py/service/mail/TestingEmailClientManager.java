package com.py.py.service.mail;

import com.py.py.domain.EmailTask;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;

public class TestingEmailClientManager implements EmailClientManager {
	
	protected String testingToken = "testingtoken";
	
	public void sendEmail(EmailDetails details, EmailTask task, String username, String emailToken) throws ServiceException {
		// DO NOTHING!
	}
	
	public String generateEmailToken(EmailTask task) throws ServiceException {
		ArgCheck.nullCheck(task);
		EMAIL_TYPE type = task.getType();
		if(type == EMAIL_TYPE.OFFER) {
			return null;
		} else {
			return testingToken;
		}
	}
	
	public void setTestingToken(String testingToken) {
		this.testingToken = testingToken;
	}
}
