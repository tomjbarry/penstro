package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Deal;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.domain.enumeration.DEAL_TYPE;
import com.py.py.domain.subdomain.FinanceDescription;

public interface DealDaoCustom {

	void updateDealState(ObjectId id, DEAL_STATE state, Boolean sourceAdded,
			Boolean targetAdded, Boolean referenceAdded) throws DaoException;

	boolean checkDealState(ObjectId id, DEAL_STATE state) throws DaoException;

	boolean verifyDeal(ObjectId id, DEAL_TYPE type, FinanceDescription source,
			List<FinanceDescription> targets, ObjectId reference,
			boolean createReferenceCost, String referenceCollection,
			Long primaryAmount, Long secondaryAmount, ObjectId paymentId)
					throws DaoException;

	Deal getDeal(ObjectId id, DEAL_STATE state, ObjectId paymentId)
			throws DaoException;

	Page<Deal> getDeals(List<DEAL_STATE> states, Date olderThanModified,
			Pageable pageable) throws DaoException;

	void remove(List<DEAL_STATE> states, Date olderThanModified)
			throws DaoException;

	Deal initializeDeal(DEAL_TYPE type, FinanceDescription source,
			List<FinanceDescription> targets, ObjectId reference,
			boolean createReferenceCost, String referenceCollection,
			Long primaryAmount, Long secondaryAmount, ObjectId paymentId)
			throws DaoException;


}
