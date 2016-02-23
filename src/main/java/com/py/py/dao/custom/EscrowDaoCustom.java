package com.py.py.dao.custom;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Escrow;
import com.py.py.domain.enumeration.ESCROW_TYPE;

public interface EscrowDaoCustom {

	Page<Escrow> findSorted(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName, Pageable pageable) throws DaoException;

	Page<Escrow> findSortedMulti(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName, ESCROW_TYPE typeAlternative,
			String sourceAlternative, String sourceNameAlternative, 
			String targetAlternative, String targetNameAlternative, 
			Pageable pageable) throws DaoException;

	void initializeEscrow(ESCROW_TYPE type, String source, String sourceName,
			String target, String targetName)
			throws DaoException;

	Escrow findEscrow(ESCROW_TYPE type, String source, String sourceName,
			String target, String targetName) throws DaoException;

	void cleanupEmpties(ESCROW_TYPE type, String source, String sourceName,
			String target, String targetName, Date olderThanCreated) throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asSource)
			throws DaoException;

	void markExists(ObjectId userId, boolean exists, boolean asSource)
			throws DaoException;

	void cleanupInvalid(ESCROW_TYPE type) throws DaoException;

	Page<Escrow> findOffersBeforeCreated(Date olderThanCreated,
			Pageable pageable) throws DaoException;

}
