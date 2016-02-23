package com.py.py.service;

import java.math.BigDecimal;
import java.util.List;

import org.bson.types.ObjectId;

import com.py.py.domain.Deal;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.service.exception.ServiceException;

public interface FinanceService {

	void cleanupDeal(Deal deal) throws ServiceException;

	void charge(ObjectId sourceId, ObjectId referenceId,
			boolean createReferenceCost, String referenceCollection, long amount)
			throws ServiceException;

	void charge(EscrowSourceTarget escrow, ObjectId referenceId,
			boolean createReferenceCost, String referenceCollection, long amount)
			throws ServiceException;

	void refundEscrow(EscrowSourceTarget escrow, ObjectId sourceId, long amount)
			throws ServiceException;

	void chargeForEscrow(ObjectId sourceId, EscrowSourceTarget escrow, long amount)
			throws ServiceException;

	void transferEscrow(EscrowSourceTarget source, EscrowSourceTarget target, long amount)
			throws ServiceException;

	void addCurrency(ObjectId id, long amount)
			throws ServiceException;

	void removeCurrency(ObjectId id, long amount)
			throws ServiceException;

	void promote(ObjectId sourceId, ObjectId referenceId,
			String referenceCollection, long promotionAmount)
			throws ServiceException;

	BigDecimal getCurrencyCost(long amount);

	boolean completedPaymentTransaction(ObjectId paymentId)
			throws ServiceException;

	void purchaseCurrency(ObjectId paymentId, ObjectId id,
			PurchaseCurrencyDTO dto) throws ServiceException;

	void checkBatchDeals(List<DEAL_STATE> states) throws ServiceException;

	void removeFinishedDeals() throws ServiceException;

	void appreciate(ObjectId sourceId, ObjectId referenceId,
			String referenceCollection, long amount, long promotionAmount,
			ObjectId paymentId) throws ServiceException;

	long getCurrencyFromCost(BigDecimal amount);

	BigDecimal getTaxFromCost(BigDecimal amount);

}
