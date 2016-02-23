package com.py.py.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Feedback;
import com.py.py.domain.User;
import com.py.py.dto.in.SubmitFeedbackDTO;
import com.py.py.dto.in.admin.ChangeFeedbackDTO;
import com.py.py.dto.out.admin.FeedbackDTO;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;
import com.py.py.service.exception.ServiceException;

public interface FeedbackService {

	Page<FeedbackDTO> getFeedbackDTOs(FEEDBACK_TYPE type, FEEDBACK_STATE state,
			FEEDBACK_CONTEXT context, ObjectId authorId, Pageable pageable,
			int direction) throws ServiceException;

	Feedback getFeedback(ObjectId feedbackId) throws ServiceException;

	FeedbackDTO getFeedbackDTO(Feedback feedback) throws ServiceException;

	void createFeedback(User author, SubmitFeedbackDTO dto)
			throws ServiceException;

	void updateFeedback(ChangeFeedbackDTO dto) throws ServiceException;

}
