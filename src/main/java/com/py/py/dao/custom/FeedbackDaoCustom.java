package com.py.py.dao.custom;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Feedback;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;

public interface FeedbackDaoCustom {

	Page<Feedback> getFeedbacks(FEEDBACK_TYPE type, FEEDBACK_STATE state,
			FEEDBACK_CONTEXT context, ObjectId author, Pageable pageable,
			int direction) throws DaoException;

	void rename(ObjectId userId, String replacement) throws DaoException;

	void removeUser(ObjectId userId) throws DaoException;

	void updateFeedback(List<ObjectId> ids, FEEDBACK_TYPE type,
			FEEDBACK_STATE state, FEEDBACK_CONTEXT context, String summary)
			throws DaoException;

}
