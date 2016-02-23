package com.py.py.dao.custom;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.FlagData;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;

public interface FlagDataDaoCustom {

	Page<FlagData> findSorted(FLAG_TYPE type, Pageable pageable)
			throws DaoException;

	void decrement(long amount) throws DaoException;

	void remove(long threshold) throws DaoException;

	void remove(ObjectId id, FLAG_TYPE type) throws DaoException;

	void addData(ObjectId id, FLAG_TYPE type, String target, long weight,
			FLAG_REASON reason) throws DaoException;

	void rename(ObjectId userId, String replacement) throws DaoException;

}
