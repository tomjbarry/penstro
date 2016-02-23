package com.py.py.service.aws;

import java.util.ArrayList;
import java.util.List;

import com.py.py.service.exception.ServiceException;


public class SQSManagerTesting implements SQSManager {

	@Override
	public List<String> getBouncedEmails() throws ServiceException {
		return new ArrayList<String>();
	}
	
	@Override
	public List<String> getComplaintEmails() throws ServiceException {
		return new ArrayList<String>();
	}
}
