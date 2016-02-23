package com.py.py.dao.custom;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Message;

public interface MessageDaoCustom {

	Page<Message> getMessagesAll(ObjectId authorId, ObjectId targetId, Pageable pageable)
			throws DaoException;

	void mark(ObjectId authorId, ObjectId targetId, boolean read) throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asAuthor)
			throws DaoException;

	void removeUser(ObjectId userId, boolean asAuthor) throws DaoException;

	Page<Message> getMessages(ObjectId authorId, ObjectId targetId,
			Pageable pageable, Boolean read) throws DaoException;

}
