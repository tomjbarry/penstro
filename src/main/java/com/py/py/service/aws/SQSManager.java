package com.py.py.service.aws;

import java.util.List;

import com.py.py.service.exception.ServiceException;


public interface SQSManager extends AWSManager {

	List<String> getBouncedEmails() throws ServiceException;

	List<String> getComplaintEmails() throws ServiceException;
}
