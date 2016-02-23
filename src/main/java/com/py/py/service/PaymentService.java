package com.py.py.service;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;

import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.PAYMENT_STATE;
import com.py.py.domain.enumeration.PAYMENT_TYPE;
import com.py.py.dto.in.AppreciateCommentDTO;
import com.py.py.dto.in.AppreciatePostingDTO;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.exception.ServiceException;

public interface PaymentService {

	void removeFinishedPayments() throws ServiceException;

	void checkPaymentBatch(List<PAYMENT_TYPE> types,
			List<PAYMENT_STATE> states, Date olderThanModified)
			throws ServiceException;

	ResultSuccessDTO purchaseCurrency(User user, PurchaseCurrencyDTO dto,
			String ipAddress)
			throws ServiceException;

	ResultSuccessDTO appreciatePosting(User user, Posting posting,
			AppreciatePostingDTO dto, String ipAddress) throws ServiceException;

	ResultSuccessDTO appreciateComment(User user, Comment comment,
			AppreciateCommentDTO dto, String ipAddress) throws ServiceException;

	void checkRequested() throws ServiceException;
	
	void checkPayment(ObjectId userId, ObjectId id, String payKey)
			throws ServiceException;

	void markPayment(ObjectId userId, ObjectId id, String payKey)
			throws ServiceException;
	
	void paymentNotification(HttpServletRequest request)
			throws ServiceException;

	void checkApproved() throws ServiceException;

	void markOldPayments() throws ServiceException;

	ResultSuccessDTO adminAppreciatePosting(User user, Posting posting, AppreciatePostingDTO dto, String ipAddress)
			throws ServiceException;

	ResultSuccessDTO adminAppreciateComment(User user, Comment comment, AppreciateCommentDTO dto, String ipAddress)
			throws ServiceException;

}
