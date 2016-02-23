package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
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

import com.py.py.dao.SubscriptionDao;
import com.py.py.domain.Subscription;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.dto.out.FollowDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.util.GenericDefaults;
import com.py.py.util.PyUtils;

public class FollowServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("followService")
	private FollowService followService;
	
	@Autowired
	protected SubscriptionDao subscriptionDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	private String tempName = "other";

	private ObjectId otherId = new ObjectId();
	private List<FollowInfo> validList = new ArrayList<FollowInfo>();
	private List<EVENT_TYPE> eventTypes = GenericDefaults.FEED_EVENTS;
	private List<String> stringEventTypes = 
			PyUtils.stringifiedList(GenericDefaults.FEED_EVENTS);
	
	@Before
	public void setUp() {
		FollowInfo fi1 = new FollowInfo();
		fi1.setAdded(new Date());
		fi1.setUsername(validSourceCU);
		FollowInfo fi2 = new FollowInfo();
		fi2.setAdded(new Date());
		fi2.setUsername(validSourceCU);
		FollowInfo fi3 = new FollowInfo();
		fi3.setAdded(new Date());
		fi3.setUsername(validSourceCU);
		
		validList.add(fi1);
		validList.add(fi2);
		validList.add(fi3);
		
		reset(userService,subscriptionDao, eventService, defaultsFactory);
	}
	
	@Test(expected = BadParameterException.class)
	public void createNull() throws Exception {
		followService.create(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void createInvalid() throws Exception {
		followService.create(createInvalidUser());
	}
	
	@Test
	public void createAddSelf() throws Exception {
		List<CachedUsername> list = new ArrayList<CachedUsername>();
		User user = createValidUser();
		list.add(new CachedUsername(user.getId(), user.getUsername()));
		when(defaultsFactory.getFolloweeCachedUsernameList()).thenReturn(list);
		followService.create(user);
		verify(eventService, times(0)).eventFollowAdd(any(CachedUsername.class), 
				any(CachedUsername.class));
	}
	
	@Test
	public void createAddNull() throws Exception {
		User user = createValidUser();
		when(defaultsFactory.getFolloweeCachedUsernameList()).thenReturn(null);
		followService.create(user);
		verify(eventService, times(0)).eventFollowAdd(any(CachedUsername.class), 
				any(CachedUsername.class));
	}
	
	@Test
	public void create() throws Exception {
		List<CachedUsername> list = new ArrayList<CachedUsername>();
		User user = createValidUser();
		list.add(new CachedUsername(new ObjectId(), validOtherName));
		when(defaultsFactory.getFolloweeCachedUsernameList()).thenReturn(list);
		followService.create(user);
		verify(eventService, atLeast(1)).eventFollowAdd(any(CachedUsername.class), 
				any(CachedUsername.class));
	}
	
	@Test(expected = BadParameterException.class)
	public void addFolloweeNull1() throws Exception {
		followService.addFollowee(null, createValidUser());
	}
	
	@Test(expected = BadParameterException.class)
	public void addFolloweeNull2() throws Exception {
		followService.addFollowee(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addFolloweeInvalid1() throws Exception {
		followService.addFollowee(createInvalidUser(), createValidUser());
	}
	
	@Test(expected = BadParameterException.class)
	public void addFolloweeInvalid2() throws Exception {
		followService.addFollowee(createValidUser(), createInvalidUser());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void addFolloweeNotAllowed() throws Exception {
		// same user
		followService.addFollowee(createValidUser(), createValidUser());
	}
	
	@Test
	public void addFollowee() throws Exception {
		User otherUser = createValidUser();
		otherUser.setId(otherId);
		otherUser.setUsername(tempName);
		followService.addFollowee(createValidUser(), otherUser);

		verify(userService, times(1)).incrementFollowerCount(any(ObjectId.class));
		verify(eventService, times(1)).eventFollowAdd(any(CachedUsername.class), 
				any(CachedUsername.class));
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFolloweeNull1() throws Exception {
		followService.removeFollowee(null, createValidUser(), tempName);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFolloweeNull3() throws Exception {
		followService.removeFollowee(createValidUser(), createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFolloweeInvalid() throws Exception {
		followService.removeFollowee(createInvalidUser(), createValidUser(), tempName);
	}
	
	@Test
	public void removeFollowee() throws Exception {
		User otherUser = createValidUser();
		otherUser.setId(otherId);
		otherUser.setUsername(tempName);
		
		followService.removeFollowee(createValidUser(), otherUser, tempName);
		
		followService.removeFollowee(createValidUser(), otherUser, tempName);
		verify(userService, atLeast(1)).decrementFollowerCount(otherId);
		verify(eventService, atLeast(1)).eventFollowRemove(any(CachedUsername.class), 
				any(CachedUsername.class));
	}
	
	@Test(expected = BadParameterException.class)
	public void addBlockedNull1() throws Exception {
		followService.addBlocked(null, createValidUser());
	}
	
	@Test(expected = BadParameterException.class)
	public void addBlockedNull2() throws Exception {
		followService.addBlocked(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addBlockedInvalid1() throws Exception {
		followService.addBlocked(createInvalidUser(), createValidUser());
	}
	
	@Test(expected = BadParameterException.class)
	public void addBlockedInvalid2() throws Exception {
		followService.addBlocked(createValidUser(), createInvalidUser());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void addBlockedNotAllowed() throws Exception {
		// same user
		followService.addBlocked(createValidUser(), createValidUser());
	}
	
	@Test
	public void addBlocked() throws Exception {
		User otherUser = createValidUser();
		ObjectId otherId = new ObjectId();
		otherUser.setId(otherId);
		otherUser.setUsername(tempName);
		followService.addBlocked(createValidUser(), otherUser);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeBlockedNull1() throws Exception {
		followService.removeBlocked(null, tempName);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeBlockedNull2() throws Exception {
		followService.removeBlocked(validUserId, null);
	}
	
	@Test
	public void removeBlocked() throws Exception {
		User otherUser = createValidUser();
		otherUser.setId(otherId);
		otherUser.setUsername(tempName);
		when(userService.findUserByUsername(tempName)).thenReturn(otherUser);
		followService.removeBlocked(validUserId, tempName);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeDTOsNull1() throws Exception {
		followService.getFolloweeDTOs(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeDTOsNull2() throws Exception {
		followService.getFolloweeDTOs(validUserId, null);
	}
	
	@Test
	public void getFolloweeDTOsInvalidList() throws Exception {
		List<FollowInfo> invalidList = addNullToList(validList);
		when(subscriptionDao.getSortedSubscribed(any(ObjectId.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FollowInfo>(invalidList));
		
		Page<FollowDTO> result = followService.getFolloweeDTOs(validUserId, 
				constructPageable());
		
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test
	public void getFolloweeDTOs() throws Exception {
		when(subscriptionDao.getSortedSubscribed(any(ObjectId.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FollowInfo>(validList));
		
		Page<FollowDTO> result = followService.getFolloweeDTOs(validUserId, 
				constructPageable());
		
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void isBlockedNull1() throws Exception {
		followService.isBlocked(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void isBlockedNull2() throws Exception {
		followService.isBlocked(validUserId, null);
	}
	
	@Test
	public void isBlocked() throws Exception {
		followService.isBlocked(validUserId, otherId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBlockedNull1() throws Exception {
		followService.getBlocked(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBlockedNull2() throws Exception {
		followService.getBlocked(validUserId, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getBlockedNotFound() throws Exception {
		when(subscriptionDao.blocked(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(null);
		followService.getBlocked(validUserId, otherId);
	}
	
	@Test
	public void getBlocked() throws Exception {
		FollowInfo temp = new FollowInfo();
		temp.setUsername(new CachedUsername(otherId, tempName));
		
		when(subscriptionDao.blocked(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(temp);
		followService.getBlocked(validUserId, otherId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBlockedListNull() throws Exception {
		followService.getBlockedList(null);
	}
	
	@Test
	public void getBlockedList() throws Exception {
		Subscription subscription = new Subscription();
		
		when(subscriptionDao.getSubscription(any(ObjectId.class))).thenReturn(null)
			.thenReturn(subscription).thenReturn(subscription);
		
		List<FollowInfo> result = followService.getBlockedList(validUserId);
		Assert.assertEquals(result.size(), 0);
		
		result = followService.getBlockedList(validUserId);
		Assert.assertEquals(result.size(), 0);
		
		subscription.setBlocked(validList);
		result = followService.getBlockedList(validUserId);
		Assert.assertEquals(result.size(), validList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBlockedDTOsNull1() throws Exception {
		followService.getBlockedDTOs(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBlockedDTOsNull2() throws Exception {
		followService.getBlockedDTOs(validUserId, null);
	}
	
	@Test
	public void getBlockedDTOsInvalidList() throws Exception {
		List<FollowInfo> invalidList = addNullToList(validList);
		when(subscriptionDao.getSortedBlocked(any(ObjectId.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FollowInfo>(invalidList));
		
		Page<FollowDTO> result = followService.getBlockedDTOs(validUserId, 
				constructPageable());
		
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test
	public void getBlockedDTOs() throws Exception {
		when(subscriptionDao.getSortedBlocked(any(ObjectId.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FollowInfo>(validList));
		
		Page<FollowDTO> result = followService.getBlockedDTOs(validUserId, 
				constructPageable());
		
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFollowerDTOsNull1() throws Exception {
		// pass through method needs little testing
		followService.getFollowerDTOs(null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFollowerDTOsNull2() throws Exception {
		followService.getFollowerDTOs(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeNull1() throws Exception {
		followService.getFollowee(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeNull2() throws Exception {
		followService.getFollowee(validUserId, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getFolloweeNotFound() throws Exception {
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(null);
		followService.getFollowee(validUserId, otherId);
	}
	
	@Test
	public void getFollowee() throws Exception {
		FollowInfo temp = new FollowInfo();
		temp.setUsername(new CachedUsername(otherId, tempName));
		
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(temp);
		followService.getFollowee(validUserId, otherId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeDTONull1() throws Exception {
		followService.getFolloweeDTO(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFolloweeDTONull2() throws Exception {
		followService.getFolloweeDTO(validUserId, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getFolloweeDTONotFound() throws Exception {
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(null);
		followService.getFolloweeDTO(validUserId, otherId);
	}
	
	@Test
	public void getFolloweeDTO() throws Exception {
		FollowInfo temp = new FollowInfo();
		temp.setUsername(new CachedUsername(otherId, tempName));
		
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(temp);
		followService.getFolloweeDTO(validUserId, otherId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFollowerNull1() throws Exception {
		followService.getFollowerDTO(null, validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFollowerNull2() throws Exception {
		followService.getFollowerDTO(validUserId, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getFollowerNotFound() throws Exception {
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(null);
		followService.getFollowerDTO(validUserId, otherId);
	}
	
	@Test
	public void getFollower() throws Exception {
		FollowInfo temp = new FollowInfo();
		temp.setUsername(new CachedUsername(otherId, tempName));
		
		when(subscriptionDao.subscribed(any(ObjectId.class), any(ObjectId.class)))
			.thenReturn(temp);
		followService.getFollowerDTO(validUserId, otherId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedDTOsNull1() throws Exception {
		followService.getFeedDTOs(null, eventTypes, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getFeedDTOsNull3() throws Exception {
		followService.getFeedDTOs(validUserId, eventTypes, null);
	}
	
	@Test
	public void getFeedDTOs() throws Exception {
		Subscription subscription = new Subscription();
		when(subscriptionDao.getSubscription(any(ObjectId.class))).thenReturn(null)
			.thenReturn(subscription).thenReturn(subscription).thenReturn(subscription);
		followService.getFeedDTOs(validUserId, eventTypes, constructPageable());
		followService.getFeedDTOs(validUserId, null, constructPageable());
		subscription.setHiddenFeedEvents(new ArrayList<String>());
		followService.getFeedDTOs(validUserId, null, constructPageable());
		subscription.setHiddenFeedEvents(stringEventTypes);
		followService.getHiddenFeedEvents(validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getUserFeedDTOsNull1() throws Exception {
		followService.getUserFeedDTOs(null, eventTypes, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getUserFeedDTOsNull3() throws Exception {
		followService.getUserFeedDTOs(validUserId, eventTypes, null);
	}
	
	@Test
	public void getUserFeedDTOs() throws Exception {
		followService.getUserFeedDTOs(validUserId, eventTypes, constructPageable());
		followService.getUserFeedDTOs(validUserId, null, constructPageable());
	}
	
	@Test(expected = BadParameterException.class)
	public void getHiddenFeedEventsNull() throws Exception {
		followService.getHiddenFeedEvents(null);
	}
	
	@Test
	public void getHiddenFeedEvents() throws Exception {
		Subscription subscription = new Subscription();
		when(subscriptionDao.getSubscription(any(ObjectId.class))).thenReturn(null)
			.thenReturn(subscription).thenReturn(subscription).thenReturn(subscription);
		followService.getHiddenFeedEvents(validUserId);
		followService.getHiddenFeedEvents(validUserId);
		subscription.setHiddenFeedEvents(new ArrayList<String>());
		followService.getHiddenFeedEvents(validUserId);
		subscription.setHiddenFeedEvents(stringEventTypes);
		followService.getHiddenFeedEvents(validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateHiddenFeedNull1() throws Exception {
		followService.updateHiddenFeed(null, eventTypes);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateHiddenFeedNull3() throws Exception {
		followService.updateHiddenFeed(validUserId, null);
	}
	
	@Test
	public void updateHiddenFeed() throws Exception {
		followService.updateHiddenFeed(validUserId, new ArrayList<EVENT_TYPE>());
		followService.updateHiddenFeed(validUserId, eventTypes);
	}
}
