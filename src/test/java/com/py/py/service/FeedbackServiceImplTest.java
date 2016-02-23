package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.FeedbackDao;
import com.py.py.dao.constants.DaoValues;
import com.py.py.domain.Feedback;
import com.py.py.dto.in.SubmitFeedbackDTO;
import com.py.py.dto.in.admin.ChangeFeedbackDTO;
import com.py.py.dto.out.admin.FeedbackDTO;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;

public class FeedbackServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("feedbackService")
	private FeedbackService feedbackService;

	@Autowired
	protected FeedbackDao feedbackDao;
	
	@Autowired
	protected UserService userService;

	private List<Feedback> validFeedbacks = new ArrayList<Feedback>();
	private SubmitFeedbackDTO submitFeedbackDTO = new SubmitFeedbackDTO();
	private ChangeFeedbackDTO changeFeedbackDTO = new ChangeFeedbackDTO();
	
	@Before
	public void setUp() {
		Feedback feedback1 = new Feedback();
		feedback1.setAuthor(validSourceCU);
		feedback1.setId(validObjectId);
		Feedback feedback2 = new Feedback();
		feedback2.setAuthor(validSourceCU);
		feedback2.setId(validObjectId);
		Feedback feedback3 = new Feedback();
		feedback3.setAuthor(validSourceCU);
		feedback3.setId(validObjectId);
		validFeedbacks.add(feedback1);
		validFeedbacks.add(feedback2);
		validFeedbacks.add(feedback3);
		List<String> list = new ArrayList<String>();
		list.add((new ObjectId()).toHexString());
		list.add((new ObjectId()).toHexString());
		list.add((new ObjectId()).toHexString());
		list.add((new ObjectId()).toHexString());
		changeFeedbackDTO.setIds(list);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedbackDTOsNull() throws Exception {
		feedbackService.getFeedbackDTOs(FEEDBACK_TYPE.SUGGESTION, FEEDBACK_STATE.COMPLETE, 
				FEEDBACK_CONTEXT.ACCOUNT, validUserId, null, DaoValues.SORT_ASCENDING);
	}

	@Test
	public void getFeedbackDTOsInvalidList() throws Exception {
		List<Feedback> invalidFeedbacks = addNullToList(validFeedbacks);
		when(feedbackDao.getFeedbacks(any(FEEDBACK_TYPE.class), any(FEEDBACK_STATE.class),
				any(FEEDBACK_CONTEXT.class), any(ObjectId.class), any(Pageable.class),
				anyInt())).thenReturn(new PageImpl<Feedback>(invalidFeedbacks));
		Page<FeedbackDTO> result = feedbackService.getFeedbackDTOs(FEEDBACK_TYPE.SUGGESTION, 
				FEEDBACK_STATE.COMPLETE, FEEDBACK_CONTEXT.ACCOUNT, validUserId, 
				constructPageable(), DaoValues.SORT_ASCENDING);
		Assert.assertEquals(result.getContent().size(), validFeedbacks.size());
	}

	@Test
	public void getFeedbackDTOs() throws Exception {
		when(feedbackDao.getFeedbacks(any(FEEDBACK_TYPE.class), any(FEEDBACK_STATE.class),
				any(FEEDBACK_CONTEXT.class), any(ObjectId.class), any(Pageable.class),
				anyInt())).thenReturn(new PageImpl<Feedback>(validFeedbacks));
		Page<FeedbackDTO> result = feedbackService.getFeedbackDTOs(FEEDBACK_TYPE.SUGGESTION, 
				FEEDBACK_STATE.COMPLETE, FEEDBACK_CONTEXT.ACCOUNT, validUserId, 
				constructPageable(), DaoValues.SORT_ASCENDING);
		Assert.assertEquals(result.getContent().size(), validFeedbacks.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedbackNull() throws Exception {
		feedbackService.getFeedback(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getFeedbackNotFound() throws Exception {
		when(feedbackDao.findOne(any(ObjectId.class))).thenReturn(null);
		feedbackService.getFeedback(validObjectId);
	}
	
	@Test
	public void getFeedback() throws Exception {
		when(feedbackDao.findOne(any(ObjectId.class))).thenReturn(new Feedback());
		feedbackService.getFeedback(validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void createFeedbackNull1() throws Exception {
		feedbackService.createFeedback(null, submitFeedbackDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void createFeedbackNull2() throws Exception {
		feedbackService.createFeedback(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void createFeedbackInvalid() throws Exception {
		feedbackService.createFeedback(createInvalidUser(), submitFeedbackDTO);
	}
	
	@Test
	public void createFeedback() throws Exception {
		feedbackService.createFeedback(createValidUser(), submitFeedbackDTO);
	}
	@Test(expected = BadParameterException.class)
	public void updateFeedbackNull() throws Exception {
		feedbackService.updateFeedback(null);
	}
	
	@Test
	public void updateFeedback() throws Exception {
		feedbackService.updateFeedback(changeFeedbackDTO);
	}
}
