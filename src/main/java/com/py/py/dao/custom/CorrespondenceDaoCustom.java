package com.py.py.dao.custom;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Correspondence;
import com.py.py.domain.subdomain.CachedUsername;

public interface CorrespondenceDaoCustom {

	Page<Correspondence> getCorrespondences(ObjectId a, Pageable pageable)
			throws DaoException;

	void update(CachedUsername a, CachedUsername b, String lastMessagePreview)
			throws DaoException;

	void updateStatus(ObjectId uId, ObjectId otherId, boolean hidden)
			throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asFirst)
			throws DaoException;

	void removeUser(ObjectId userId, boolean asFirst) throws DaoException;

	void removeExpired(Date olderThanModified) throws DaoException;

}
