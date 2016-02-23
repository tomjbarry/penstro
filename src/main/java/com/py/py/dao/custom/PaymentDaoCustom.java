package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Payment;
import com.py.py.domain.enumeration.PAYMENT_MARK;
import com.py.py.domain.enumeration.PAYMENT_STATE;
import com.py.py.domain.enumeration.PAYMENT_TYPE;
import com.py.py.dto.DTO;

public interface PaymentDaoCustom {

	void updatePaymentState(ObjectId id, PAYMENT_STATE state)
			throws DaoException;

	boolean checkPaymentState(ObjectId id, PAYMENT_STATE state)
			throws DaoException;

	void setPayKey(ObjectId id, PAYMENT_STATE state, String payKey)
			throws DaoException;

	boolean checkPayKey(ObjectId id, PAYMENT_STATE state, String payKey)
			throws DaoException;

	void remove(List<PAYMENT_STATE> states, Date olderThanModified)
			throws DaoException;

	Payment findPayment(ObjectId id, String payKey) throws DaoException;

	boolean verifyPayment(ObjectId id, PAYMENT_STATE state, PAYMENT_TYPE type,
			ObjectId referenceId, ObjectId sourceId, ObjectId targetId,
			String targetPaymentId, Map<ObjectId, String> beneficiaries,
			long amount, DTO dto) throws DaoException;

	Payment initializePayment(PAYMENT_TYPE type, ObjectId referenceId,
			ObjectId sourceId, ObjectId targetId, String targetPaymentId,
			Map<ObjectId, String> beneficiaries, long amount, DTO dto)
			throws DaoException;

	void markPayment(ObjectId id, PAYMENT_MARK marked, PAYMENT_MARK newMark)
			throws DaoException;

	Page<Payment> getPayments(List<PAYMENT_TYPE> types,
			List<PAYMENT_STATE> states, Date olderThanModified,
			PAYMENT_MARK marked, Pageable pageable) throws DaoException;

	void markPayments(List<PAYMENT_TYPE> types, List<PAYMENT_STATE> states,
			Date olderThanModified, PAYMENT_MARK marked, PAYMENT_MARK newMark)
			throws DaoException;

	void updatePaymentState(ObjectId id, PAYMENT_STATE state, PAYMENT_MARK marked, PAYMENT_MARK newMark)
		throws DaoException;

}
