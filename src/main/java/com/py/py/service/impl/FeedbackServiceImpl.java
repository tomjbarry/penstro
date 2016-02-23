package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.FeedbackDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Feedback;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.SubmitFeedbackDTO;
import com.py.py.dto.in.admin.ChangeFeedbackDTO;
import com.py.py.dto.out.admin.FeedbackDTO;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;
import com.py.py.service.FeedbackService;
import com.py.py.service.UserService;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class FeedbackServiceImpl implements FeedbackService {
	
	protected static final PyLogger logger = PyLogger.getLogger(FeedbackServiceImpl.class);
	
	@Autowired
	protected FeedbackDao feedbackDao;
	
	@Autowired
	protected UserService userService;
	
	@Override
	public Page<FeedbackDTO> getFeedbackDTOs(FEEDBACK_TYPE type,
			FEEDBACK_STATE state, FEEDBACK_CONTEXT context, ObjectId targetId, 
			Pageable pageable, int direction) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		
		List<FeedbackDTO> dtolist = ModelFactory.<FeedbackDTO>constructList();
		Page<Feedback> page = new PageImpl<Feedback>(new ArrayList<Feedback>(), pageable, 0);
		
		try {
			page = feedbackDao.getFeedbacks(type, state, context, targetId, 
					pageable, direction);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(Feedback f : page.getContent()) {
			try {
				dtolist.add(Mapper.mapFeedbackDTO(f));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of feedback!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of feedback!", e);
			}
		}
		
		return new PageImpl<FeedbackDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public Feedback getFeedback(ObjectId feedbackId) throws ServiceException {
		ArgCheck.nullCheck(feedbackId);
		
		try {
			Feedback feedback = feedbackDao.findOne(feedbackId);
			if(feedback == null) {
				throw new NotFoundException(feedbackId.toHexString());
			}
			return feedback;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public FeedbackDTO getFeedbackDTO(Feedback feedback) throws ServiceException {
		ArgCheck.nullCheck(feedback);
		return Mapper.mapFeedbackDTO(feedback);
	}
	
	@Override
	public void createFeedback(User author, SubmitFeedbackDTO dto) throws ServiceException {
		ArgCheck.nullCheck(author, dto);
		ArgCheck.nullCheck(author.getId(), author.getUsername());
		
		Feedback feedback = Mapper.mapFeedback(dto, 
				new CachedUsername(author.getId(), author.getUsername()));
		
		try {
			feedbackDao.save(feedback);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		logger.info("Feedback created by user (" + author.getUsername() + ") with id {" 
				+ author.getId().toHexString() + "}.");
	}
	
	@Override
	public void updateFeedback(ChangeFeedbackDTO dto) throws ServiceException {
		ArgCheck.nullCheck(dto);
		
		List<ObjectId> ids = PyUtils.objectIdList(dto.getIds());
		
		try {
			feedbackDao.updateFeedback(ids, dto.getType(), dto.getState(), 
					dto.getContext(), dto.getSummary());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
}
