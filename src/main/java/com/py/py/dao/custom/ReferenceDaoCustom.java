package com.py.py.dao.custom;

import org.bson.types.ObjectId;

import com.py.py.dao.exception.DaoException;

public interface ReferenceDaoCustom {

	void chargeTally(ObjectId id, ObjectId tid, long amount,
			String collectionName) throws DaoException;

	void completeTally(ObjectId id, ObjectId tid, String collectionName)
			throws DaoException;

	boolean verifyTallyAdded(ObjectId id, ObjectId tid, String collectionName)
			throws DaoException;

	boolean verifyTallyCompleted(ObjectId id, ObjectId tid,
			String collectionName) throws DaoException;

	void revertPendingCost(ObjectId id, ObjectId tid, String collectionName)
			throws DaoException;

	boolean verifyTallyCostCompleted(ObjectId id, ObjectId tid, long amount,
			String collectionName) throws DaoException;

	void addTally(ObjectId id, ObjectId tid, Long appreciation, Long promotion,
			String collectionName) throws DaoException;

	void adminIncrement(ObjectId id, Long cost, Long appreciation,
			Long promotion, String collectionName) throws DaoException;

	void revertPendingTally(ObjectId id, ObjectId tid, Long appreciation,
			Long promotion, String collectionName) throws DaoException;


}
