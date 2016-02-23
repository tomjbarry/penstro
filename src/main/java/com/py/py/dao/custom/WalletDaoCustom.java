package com.py.py.dao.custom;

import org.bson.types.ObjectId;

import com.py.py.dao.exception.DaoException;

public interface WalletDaoCustom {

	void startTransaction(String id, ObjectId tid, String currency,
			long amount, String collectionName) throws DaoException;

	void completeTransaction(String id, ObjectId tid, String collectionName)
			throws DaoException;

	boolean verifyHasFunds(String id, String currency, long amount,
			String collectionName) throws DaoException;

	boolean verifyTransactionStarted(String id, ObjectId tid,
			String collectionName) throws DaoException;

	boolean verifyTransactionCompleted(String id, ObjectId tid,
			String collectionName) throws DaoException;

	void revertPendingTransaction(String id, ObjectId tid, String currency,
			long amount, String collectionName) throws DaoException;

}
