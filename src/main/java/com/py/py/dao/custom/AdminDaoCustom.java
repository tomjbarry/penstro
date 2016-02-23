package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.AdminAction;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.DTO;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;

public interface AdminDaoCustom {

	void updateAction(ObjectId id, ADMIN_STATE state) throws DaoException;

	void remove(List<ADMIN_STATE> states, Date olderThanModified)
			throws DaoException;

	AdminAction createAction(CachedUsername admin, ADMIN_STATE state,
			ADMIN_TYPE type, String target, DTO dto, Object reference)
			throws DaoException;

	Page<AdminAction> findSortedActions(ObjectId adminId,
			List<ADMIN_STATE> states, ADMIN_TYPE type, String target,
			Date olderThanModified, Pageable pageable, int direction)
			throws DaoException;

}
