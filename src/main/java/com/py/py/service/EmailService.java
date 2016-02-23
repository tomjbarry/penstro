package com.py.py.service;

import org.bson.types.ObjectId;

import com.py.py.service.exception.ServiceException;

public interface EmailService {

	void resetPassword(String email, String username) throws ServiceException;

	void confirmation(String email, String username) throws ServiceException;

	void changeEmail(String email, String username) throws ServiceException;

	void offerEmail(String email, String username) throws ServiceException;

	void sendEmails() throws ServiceException;

	void cleanupCompleted() throws ServiceException;

	void cleanupErrors() throws ServiceException;

	void delete(String email, String username) throws ServiceException;

	void changePaymentId(String email, String username) throws ServiceException;

	void emailComplaint(ObjectId id) throws ServiceException;

	void emailBounce(ObjectId id) throws ServiceException;

	void checkEmailBounces() throws ServiceException;

	void checkEmailComplaints() throws ServiceException;
}
