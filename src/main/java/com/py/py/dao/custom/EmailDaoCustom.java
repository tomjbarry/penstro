package com.py.py.dao.custom;

import java.util.Date;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.EmailTask;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.enumeration.TASK_STATE;

public interface EmailDaoCustom {

	void updateTask(ObjectId id, Date completed, TASK_STATE state)
			throws DaoException;

	Page<EmailTask> findNonCompleteTasks(Pageable pageable, TASK_STATE state,
			EMAIL_TYPE type) throws DaoException;

	void cleanupTasks(Date created, Date completed, TASK_STATE state,
			EMAIL_TYPE type) throws DaoException;

}
