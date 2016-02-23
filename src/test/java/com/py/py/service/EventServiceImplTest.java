package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.EventDao;
import com.py.py.domain.Event;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.NotificationDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.util.DefaultsFactory;

public class EventServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("eventService")
	private EventService eventService;
	
	@Autowired
	private EventDao eventDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	private Map<String, String> targets = new HashMap<String, String>();
	private List<EVENT_TYPE> types = new ArrayList<EVENT_TYPE>();
	private List<Event> validEventList = new ArrayList<Event>();
	private List<ObjectId> validIdList = Arrays.asList(new ObjectId(), new ObjectId());
	
	@Before
	public void setUp() {
		reset(eventDao, userService, defaultsFactory);
		types.add(EVENT_TYPE.POSTING);
		
		Event event1 = new Event();
		event1.setAuthor(validSourceCU);
		event1.setCreated(new Date());
		event1.setTargets(targets);
		event1.setType(EVENT_TYPE.COMMENT);
		Event event2 = new Event();
		event2.setAuthor(validSourceCU);
		event2.setCreated(new Date());
		event2.setTargets(targets);
		event2.setType(EVENT_TYPE.POSTING);
		Event event3 = new Event();
		event3.setAuthor(validSourceCU);
		event3.setCreated(new Date());
		event3.setTargets(targets);
		event3.setType(EVENT_TYPE.OFFER);
		
		validEventList.add(event1);
		validEventList.add(event2);
		validEventList.add(event3);
	}
	
	@Test(expected = BadParameterException.class)
	public void createEventNull1() throws Exception {
		eventService.createEvent(null, validTargetCU, validSourceCU, validObjectId, 
				validName, COMMENT_TYPE.POSTING, validObjectId, validObjectId, 
				EVENT_TYPE.COMMENT_SUB, targets);
	}
	
	@Test(expected = BadParameterException.class)
	public void createEventNull9() throws Exception {
		eventService.createEvent(validSourceCU, validTargetCU, validSourceCU, validObjectId, 
				validName, COMMENT_TYPE.POSTING, validObjectId, validObjectId, 
				null, targets);
	}
	
	@Test(expected = BadParameterException.class)
	public void createEventNull10() throws Exception {
		eventService.createEvent(validSourceCU, validTargetCU, validSourceCU, validObjectId, 
				validName, COMMENT_TYPE.POSTING, validObjectId, validObjectId, 
				EVENT_TYPE.COMMENT_SUB, null);
	}
	
	@Test
	public void createEvent() throws Exception {
		eventService.createEvent(validSourceCU, null, null, null, null, null, null, null, 
				EVENT_TYPE.COMMENT_SUB, targets);
		eventService.createEvent(validSourceCU, validTargetCU, validSourceCU, validObjectId, 
				validName,COMMENT_TYPE.POSTING, validObjectId, validObjectId, 
				EVENT_TYPE.COMMENT_SUB, targets);
		targets.put("test", "test");
		targets.put("test2", "test2");
		eventService.createEvent(validSourceCU, validTargetCU, validSourceCU, validObjectId, 
				validName, COMMENT_TYPE.POSTING, validObjectId, validObjectId, 
				EVENT_TYPE.COMMENT_SUB, targets);
	}
	
	@Test(expected = BadParameterException.class)
	public void checkFeedDTOsNull1() throws Exception {
		eventService.checkFeedDTOs(null, validIdList, types, randomBoolean(), 
				constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void checkFeedDTOsNull2() throws Exception {
		eventService.checkFeedDTOs(validUserId, null, types, randomBoolean(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void checkFeedDTOsNull5() throws Exception {
		eventService.checkFeedDTOs(validUserId, validIdList, types, randomBoolean(), null);
	}
	
	@Test
	public void checkFeedDTOsInvalidList() throws Exception {
		List<Event> invalidEventList = addNullToList(validEventList);
		when(eventDao.findEventsMultipleAuthors(anyListOf(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(invalidEventList));
		
		Page<FeedDTO> result = eventService.checkFeedDTOs(validUserId, validIdList, types, 
				randomBoolean(), constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedDTOsNull1() throws Exception {
		eventService.getFeedDTOs(null, types, randomBoolean(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedDTOsNull4() throws Exception {
		eventService.getFeedDTOs(validIdList, types, randomBoolean(), null);
	}
	
	@Test
	public void getFeedDTOsInvalidList() throws Exception {
		List<Event> invalidEventList = addNullToList(validEventList);
		when(eventDao.findEventsMultipleAuthors(anyListOf(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(invalidEventList));
		
		Page<FeedDTO> result = eventService.getFeedDTOs(validIdList, types, 
				randomBoolean(), constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test
	public void getFeedDTOs() throws Exception {
		when(eventDao.findEventsMultipleAuthors(anyListOf(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(validEventList));
		
		Page<FeedDTO> result = eventService.getFeedDTOs(validIdList, types, 
				randomBoolean(), constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getNotificationDTOsNull1() throws Exception {
		eventService.getNotificationDTOs(null, types, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getNotificationDTOsNull3() throws Exception {
		eventService.getNotificationDTOs(validUserId, types, null);
	}
	
	@Test
	public void getNotificationDTOsInvalidList() throws Exception {
		List<Event> invalidEventList = addNullToList(validEventList);
		when(eventDao.findEvents(any(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(invalidEventList));
		
		Page<NotificationDTO> result = eventService.getNotificationDTOs(validUserId, 
				types,  constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test
	public void getNotificationDTOs() throws Exception {
		when(eventDao.findEvents(any(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(validEventList));
		
		Page<NotificationDTO> result = eventService.getNotificationDTOs(validUserId, 
				types, constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedCountNull1() throws Exception {
		eventService.getFeedCount(null, validIdList, types, new Date());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedCountNull2() throws Exception {
		eventService.getFeedCount(validUserId, null, types, new Date());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedCountNull5() throws Exception {
		eventService.getFeedCount(validUserId, validIdList, types, null);
	}
	
	@Test
	public void getFeedCount() throws Exception {
		when(eventDao.findEventsMultipleAuthors(anyListOf(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(validEventList));
		
		long result = eventService.getFeedCount(validUserId, validIdList, types, new Date());
		Assert.assertEquals(result, validEventList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getNotificationCountNull1() throws Exception {
		eventService.getNotificationCount(null, types, new Date());
	}
	
	@Test(expected = BadParameterException.class)
	public void getNotificationCountNull3() throws Exception {
		eventService.getNotificationCount(validUserId, types, null);
	}
	
	@Test
	public void getNotificationCount() throws Exception {
		when(eventDao.findEvents(any(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(validEventList));
		
		long result = eventService.getNotificationCount(validUserId, types, new Date());
		Assert.assertEquals(result, validEventList.size());
	}

	@Test(expected = BadParameterException.class)
	public void getFollowerEventsNull1() throws Exception {
		eventService.getFollowerEvents(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFollowerEventsNull2() throws Exception {
		eventService.getFollowerEvents(validUserId, null);
	}
	
	@Test
	public void getFollowerEventsInvalidList() throws Exception {
		List<Event> invalidEventList = addNullToList(validEventList);
		when(eventDao.findEvents(any(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(invalidEventList));
		
		Page<FollowDTO> result = eventService.getFollowerEvents(validUserId, 
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test
	public void getFollowerEvents() throws Exception {
		when(eventDao.findEvents(any(ObjectId.class), any(ObjectId.class), 
				anyListOf(String.class), any(Date.class), any(Pageable.class)))
				.thenReturn(new PageImpl<Event>(validEventList));
		
		Page<FollowDTO> result = eventService.getFollowerEvents(validUserId,
				constructPageable());
		Assert.assertEquals(result.getContent().size(), validEventList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void eventPostingNull1() throws Exception {
		eventService.eventPosting(null, validTargetCU, validTitle, validObjectId, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventPostingNull3() throws Exception {
		eventService.eventPosting(validSourceCU, validTargetCU, null, validObjectId, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventPostingNull4() throws Exception {
		eventService.eventPosting(validSourceCU, validTargetCU, validTitle, null, 
				validCost);
	}
	
	@Test
	public void eventPosting() throws Exception {
		eventService.eventPosting(validSourceCU, validTargetCU, validTitle, 
				validObjectId, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventCommentNull1() throws Exception {
		eventService.eventComment(null, validTargetCU, validObjectId, validObjectId, 
				validTag, COMMENT_TYPE.TAG, validTitle, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventCommentNull3() throws Exception {
		eventService.eventComment(validSourceCU, validTargetCU, null, null, validTag, 
				COMMENT_TYPE.TAG, validTitle, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventCommentNull6() throws Exception {
		eventService.eventComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, null, validTitle, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventCommentNull7() throws Exception {
		eventService.eventComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, null, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventCommentInvalid() throws Exception {
		eventService.eventComment(validSourceCU, validTargetCU, validObjectId, null, null, 
				COMMENT_TYPE.TAG, validTitle, validTargetCU, validCost);
	}
	
	@Test
	public void eventComment() throws Exception {
		eventService.eventComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, validTitle, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull1() throws Exception {
		eventService.eventSubComment(null, validTargetCU, validObjectId, null, validTag, 
				COMMENT_TYPE.TAG, validSourceCU, new ObjectId(), validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull3() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, null, null, validTag, 
				COMMENT_TYPE.TAG, validSourceCU, new ObjectId(), validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull6() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, null, validSourceCU, new ObjectId(), validTitle, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull7() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, null, new ObjectId(), validTitle, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull8() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, validSourceCU, null, validTitle, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentNull9() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, validSourceCU, new ObjectId(), null, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventSubCommentInvalid() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				null, COMMENT_TYPE.TAG, validSourceCU, new ObjectId(), validTitle, validCost);
	}
	
	@Test
	public void eventSubComment() throws Exception {
		eventService.eventSubComment(validSourceCU, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, validSourceCU, new ObjectId(), validTitle, 
				validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationPostingNull1() throws Exception {
		eventService.eventAppreciationPosting(null, validTargetCU, validObjectId, 
				validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationPostingNull3() throws Exception {
		eventService.eventAppreciationPosting(validSourceCU, validTargetCU, null, 
				validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationPostingNull4() throws Exception {
		eventService.eventAppreciationPosting(validSourceCU, validTargetCU, validObjectId, 
				null, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationPostingNull5() throws Exception {
		eventService.eventAppreciationPosting(validSourceCU, validTargetCU, validObjectId, 
				validSourceCU, null, validCost);
	}
	
	@Test
	public void eventAppreciationPosting() throws Exception {
		eventService.eventAppreciationPosting(validSourceCU, validTargetCU, validObjectId, 
				validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentNull1() throws Exception {
		eventService.eventAppreciationComment(null, validTargetCU, validObjectId, null, 
				validTag, COMMENT_TYPE.TAG, validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentNull3() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, null, null, 
				validTag, COMMENT_TYPE.TAG, validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentNull6() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, validObjectId, 
				null, validTag, null, validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentNull7() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, validObjectId, 
				null, validTag, COMMENT_TYPE.TAG, null, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentNull8() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, validObjectId, 
				null, validTag, COMMENT_TYPE.TAG, validSourceCU, null, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventAppreciationCommentInvalid() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, validObjectId, 
				null, null, COMMENT_TYPE.TAG, validSourceCU, validTitle, validCost);
	}
	
	@Test
	public void eventAppreciationComment() throws Exception {
		eventService.eventAppreciationComment(validSourceCU, validTargetCU, validObjectId, 
				null, validTag, COMMENT_TYPE.TAG, validSourceCU, validTitle, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventMessageNull1() throws Exception {
		eventService.eventMessage(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventMessageNull2() throws Exception {
		eventService.eventMessage(validSourceCU, null);
	}
	
	@Test
	public void eventMessage() throws Exception {
		eventService.eventMessage(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferNull1() throws Exception {
		eventService.eventOffer(null, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferNull2() throws Exception {
		eventService.eventOffer(validSourceCU, null, validCost);
	}
	
	@Test
	public void eventOffer() throws Exception {
		eventService.eventOffer(validSourceCU, validTargetCU, validCost);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferAcceptNull1() throws Exception {
		eventService.eventOfferAccept(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferAcceptNull2() throws Exception {
		eventService.eventOfferAccept(validSourceCU, null);
	}
	
	@Test
	public void eventOfferAccept() throws Exception {
		eventService.eventOfferAccept(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferDenyNull1() throws Exception {
		eventService.eventOfferDeny(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferDenyNull2() throws Exception {
		eventService.eventOfferDeny(validSourceCU, null);
	}
	
	@Test
	public void eventOfferDeny() throws Exception {
		eventService.eventOfferDeny(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferWithdrawNull1() throws Exception {
		eventService.eventOfferWithdraw(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventOfferWithdrawNull2() throws Exception {
		eventService.eventOfferWithdraw(validSourceCU, null);
	}
	
	@Test
	public void eventOfferWithdraw() throws Exception {
		eventService.eventOfferWithdraw(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventBackingCancelNull1() throws Exception {
		eventService.eventBackingCancel(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventBackingCancelNull2() throws Exception {
		eventService.eventBackingCancel(validSourceCU, null);
	}
	
	@Test
	public void eventBackingCancel() throws Exception {
		eventService.eventBackingCancel(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventBackingWithdrawNull1() throws Exception {
		eventService.eventBackingWithdraw(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventBackingWithdrawNull2() throws Exception {
		eventService.eventBackingWithdraw(validSourceCU, null);
	}
	
	@Test
	public void eventBackingWithdraw() throws Exception {
		eventService.eventBackingWithdraw(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventFollowAddNull1() throws Exception {
		eventService.eventFollowAdd(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventFollowAddNull2() throws Exception {
		eventService.eventFollowAdd(validSourceCU, null);
	}
	
	@Test
	public void eventFollowAdd() throws Exception {
		eventService.eventFollowAdd(validSourceCU, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventFollowRemoveNull1() throws Exception {
		eventService.eventFollowRemove(null, validTargetCU);
	}
	
	@Test(expected = BadParameterException.class)
	public void eventFollowRemoveNull2() throws Exception {
		eventService.eventFollowRemove(validSourceCU, null);
	}
	
	@Test
	public void eventFollowRemove() throws Exception {
		eventService.eventFollowRemove(validSourceCU, validTargetCU);
	}
}
