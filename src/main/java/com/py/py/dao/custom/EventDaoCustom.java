package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Event;
import com.py.py.enumeration.EVENT_TYPE;

public interface EventDaoCustom {

	Page<Event> findEvents(ObjectId author, ObjectId target,
			List<String> types, Date time, Pageable pageable)
			throws DaoException;

	void rename(ObjectId userId, String replacement, EVENT_TYPE type,
			boolean isAuthor, boolean isTarget, boolean isBeneficiary,
			String previous, String targetsKey) throws DaoException;

	void removeUser(ObjectId userId, boolean isAuthor, boolean isTarget,
			boolean isBeneficiary) throws DaoException;

	Page<Event> findEventsMultipleAuthors(List<ObjectId> authors,
			ObjectId target, List<String> types, Date time, Pageable pageable)
			throws DaoException;

	void insertEvent(Event event) throws DaoException;
}
