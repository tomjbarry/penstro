package com.py.py.dao.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Restricted;
import com.py.py.enumeration.RESTRICTED_TYPE;

public interface RestrictedDaoCustom {

	Restricted add(String word, RESTRICTED_TYPE type) throws DaoException;

	Restricted getRestricted(String word, RESTRICTED_TYPE type)
			throws DaoException;

	void remove(String word, RESTRICTED_TYPE type) throws DaoException;

	Page<Restricted> findRestricteds(RESTRICTED_TYPE type, Pageable pageable)
			throws DaoException;

}
