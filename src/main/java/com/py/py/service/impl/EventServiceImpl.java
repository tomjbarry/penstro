package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.EventMessages;
import com.py.py.dao.EventDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Event;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.NotificationDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.EventService;
import com.py.py.service.UserService;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.util.PyLogger;

public class EventServiceImpl implements EventService {
	
	protected static final PyLogger logger = PyLogger.getLogger(EventServiceImpl.class);
	
	@Autowired
	private EventDao eventDao;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	protected List<String> createNotificationList(ObjectId id, List<EVENT_TYPE> types)
			throws ServiceException {
		List<String> notificationList = new ArrayList<String>();
		if(types != null && !types.isEmpty()) {
			for(EVENT_TYPE t : types) {
				if(defaultsFactory.getNotificationsEventsList().contains(t)
						&& !notificationList.contains(t)) {
					// ensure single of its type
					notificationList.add(t.toString());
				}
			}
		} else {
			UserInfo userInfo = userService.findUserInfo(id);
			if(userInfo.getSettings() == null) {
				return defaultsFactory.getNotificationsEventsStringList();
			}
			List<String> hiddenNotifications = userInfo.getSettings()
					.getHiddenNotifications();
			if(hiddenNotifications != null && !hiddenNotifications.isEmpty()) {
				for(String s : defaultsFactory.getNotificationsEventsStringList()) {
					if(!hiddenNotifications.contains(s)) {
						notificationList.add(s);
					}
				}
			} else {
				notificationList = defaultsFactory.getNotificationsEventsStringList();
			}
		}
		return notificationList;
	}
	
	protected List<String> createFeedList(List<EVENT_TYPE> types, boolean hidden) 
			throws ServiceException {
		List<String> feedList = new ArrayList<String>();
		if(types != null && !types.isEmpty()) {
			if(hidden) {
				for(EVENT_TYPE t : defaultsFactory.getFeedEventsList()) {
					if(!types.contains(t)) {
						feedList.add(t.toString());
					}
				}
			} else {
				for(EVENT_TYPE t : types) {
					if(defaultsFactory.getFeedEventsList().contains(t)) {
						feedList.add(t.toString());
					}
				}
			}
		} else {
			// these should be preloaded, so this shouldnt happen
			feedList = defaultsFactory.getFeedEventsStringList();
		}
		return feedList;
	}
	
	@Override
	public void rename(ObjectId userId, String username, String replacement) throws ServiceException {
		ArgCheck.nullCheck(userId);
		
		ServiceException se = null;
		// author
		try {
			eventDao.rename(userId, replacement, null, true, false, false, null, 
					EventMessages.SOURCE);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		
		// target
		try {
			eventDao.rename(userId, replacement, null, false, true, false, null, 
					EventMessages.TARGET);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		
		// beneficiary
		try {
			eventDao.rename(userId, replacement, null, false, false, true, null, 
					EventMessages.BENEFICIARY);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		
		// special case of comment on user where username is title
		try {
			eventDao.rename(userId, replacement, EVENT_TYPE.COMMENT, false, false, false, 
					username, EventMessages.TITLE);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		try {
			eventDao.rename(userId, replacement, EVENT_TYPE.COMMENT_SUB, false, false, false, 
					username, EventMessages.TITLE);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		if(se != null) {
			throw se;
		}
	}
	
	@Override
	public void removeUser(ObjectId userId) throws ServiceException {
		ArgCheck.nullCheck(userId);
		
		ServiceException se = null;
		// author
		try {
			eventDao.removeUser(userId, true, false, false);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		
		// target
		try {
			eventDao.removeUser(userId, false, true, false);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		
		// beneficiary
		try {
			eventDao.removeUser(userId, false, false, true);
		} catch(DaoException de) {
			se = new ServiceException(de);
		}
		if(se != null) {
			throw se;
		}
	}
	
	@Override
	public void createEvent(CachedUsername author, CachedUsername target, 
			CachedUsername beneficiary, ObjectId baseId, String baseString, 
			COMMENT_TYPE baseType, ObjectId primaryId, ObjectId parentId, EVENT_TYPE type, 
			Map<String, String> targets) throws ServiceException {
		ArgCheck.nullCheck(author, targets, type);
		
		Event event = new Event();
		event.setAuthor(author);
		event.setTargets(targets);
		event.setType(type);
		event.setCreated(new Date());
		if(target != null) {
			event.setTarget(target);
		}
		if(beneficiary != null) {
			event.setBeneficiary(beneficiary);
		}
		if(baseId != null) {
			event.setBaseId(baseId);
		}
		if(baseString != null) {
			event.setBaseString(baseString);
		}
		if(baseType != null) {
			event.setBaseType(baseType);
		}
		if(primaryId != null) {
			event.setPrimaryId(primaryId);
		}
		if(parentId != null) {
			event.setParentId(parentId);
		}
		
		try {
			eventDao.insertEvent(event);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.debug("Created by author {" + author + "} of type {" + type + "}!");
	}
	
	@Override
	public Page<FeedDTO> getFeedDTOs(List<ObjectId> ids, List<EVENT_TYPE> types, 
			boolean hidden, Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(ids, pageable);
		
		if(ids.size() <= 0) {
			return new PageImpl<FeedDTO>(new ArrayList<FeedDTO>(), pageable, 0);
		}
		
		List<String> feedList = createFeedList(types, hidden);
		
		Page<Event> events = null;
		try {
			events = eventDao.findEventsMultipleAuthors(ids, null, feedList, null, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(events == null) {
			events = new PageImpl<Event>(ModelFactory.<Event>constructList(), pageable, 0);
		}
		
		List<FeedDTO> feed = ModelFactory.<FeedDTO>constructList();
		for(Event e : events.getContent()) {
			try {
				FeedDTO n = Mapper.mapFeedDTO(e);
				feed.add(n);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of feed event!", bpe);
			} catch(Exception ex) {
				logger.info("Invalid mapping of feed event!", ex);
			}
		}
		
		return new PageImpl<FeedDTO>(feed, pageable, events.getTotalElements());
	}
	
	@Override
	public Page<FeedDTO> checkFeedDTOs(ObjectId userId, List<ObjectId> ids, 
			List<EVENT_TYPE> types, boolean hidden, Pageable pageable) 
					throws ServiceException {
		ArgCheck.nullCheck(userId);
		userService.checkedFeed(userId);
		return getFeedDTOs(ids, types, hidden, pageable);
	}
	
	@Override
	public Page<NotificationDTO> getNotificationDTOs(ObjectId id, List<EVENT_TYPE> types, 
			Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(id, pageable);
		
		List<String> notificationList = createNotificationList(id, types);
		
		Page<Event> events = null;
		try {
			events = eventDao.findEvents(null, id, notificationList, null, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(events == null) {
			events = new PageImpl<Event>(ModelFactory.<Event>constructList(), pageable, 0);
		}
		
		List<NotificationDTO> notifications = ModelFactory.<NotificationDTO>constructList();
		for(Event e : events.getContent()) {
			try {
				NotificationDTO n = Mapper.mapNotificationDTO(e);
				notifications.add(n);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of notification event!", bpe);
			} catch(Exception ex) {
				logger.info("Invalid mapping of notification event!", ex);
			}
		}
		
		userService.checkedNotifications(id);
		
		return new PageImpl<NotificationDTO>(notifications, pageable, 
				events.getTotalElements());
	}
	
	@Override
	public long getFeedCount(ObjectId userId, List<ObjectId> ids, List<EVENT_TYPE> types, 
			Date lastChecked) throws ServiceException {
		ArgCheck.nullCheck(userId, ids, lastChecked);
		
		if(ids.size() <= 0) {
			return 0;
		}
		
		List<String> feedList = createFeedList(types, true);
		
		Page<Event> events = null;
		try {
			events = eventDao.findEventsMultipleAuthors(ids, null, feedList, lastChecked, 
					ModelFactory.constructEmptyPageable());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(events == null) {
			return 0L;
		}
		return events.getTotalElements();
	}
	
	@Override
	public long getNotificationCount(ObjectId id, List<EVENT_TYPE> types, Date lastChecked)
			throws ServiceException {
		ArgCheck.nullCheck(id, lastChecked);
		
		List<String> notificationList = createNotificationList(id, types);
		
		Page<Event> events = null;
		try {
			events = eventDao.findEvents(null, id, notificationList, lastChecked, 
					ModelFactory.constructEmptyPageable());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(events == null) {
			return 0L;
		}
		return events.getTotalElements();
	}
	
	@Override
	public Page<FollowDTO> getFollowerEvents(ObjectId id, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(id, pageable);
		
		List<String> followerEventList = new ArrayList<String>();
		followerEventList.add(EVENT_TYPE.FOLLOW_ADD.toString());
		
		Page<Event> events = null;
		try {
			events = eventDao.findEvents(null, id, followerEventList, null, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(events == null) {
			return new PageImpl<FollowDTO>(ModelFactory.<FollowDTO>constructList(), 
					pageable, 0);
		}
		
		List<FollowDTO> follows = ModelFactory.<FollowDTO>constructList();
		for(Event e : events.getContent()) {
			try {
				FollowDTO dto = Mapper.mapFollowDTO(e);
				follows.add(dto);
			} catch(BadParameterException bpe) {
				// continue
			} catch(Exception ex) {
				// continue
			}
		}
		return new PageImpl<FollowDTO>(follows, pageable, events.getTotalElements());
	}
	
	// specific event type shortcut methods
	@Override
	public void eventPosting(CachedUsername source, CachedUsername beneficiary, 
			String title, ObjectId postingId, long cost) throws ServiceException {
		ArgCheck.nullCheck(source, title, postingId);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.COST, Long.toString(cost));
		targets.put(EventMessages.TITLE, title);

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, null, beneficiary, null, null, null, postingId, null, 
				EVENT_TYPE.POSTING, targets);
	}
	
	@Override
	public void eventComment(CachedUsername source, CachedUsername beneficiary, 
			ObjectId commentId, ObjectId baseId, String baseString, COMMENT_TYPE type, 
			String title, CachedUsername baseTarget, long cost) throws ServiceException {
		ArgCheck.nullCheck(source, commentId, type, title);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.COST, Long.toString(cost));
		targets.put(EventMessages.TITLE, title);
		targets.put(EventMessages.TYPE, type.toString());
		if(baseTarget != null) {
			targets.put(EventMessages.TARGET, baseTarget.getUsername());
		}
		
		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, baseTarget, beneficiary, baseId, baseString, type, commentId, 
				null, EVENT_TYPE.COMMENT, targets);
	}
	
	@Override
	public void eventSubComment(CachedUsername source, CachedUsername beneficiary, 
			ObjectId commentId, ObjectId baseId, String baseString, COMMENT_TYPE type, 
			CachedUsername parentAuthor, ObjectId parentCommentId, String title, 
			long cost) throws ServiceException {
		ArgCheck.nullCheck(source, commentId, type, parentAuthor, 
				parentCommentId, title);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.COST, Long.toString(cost));
		targets.put(EventMessages.TARGET, parentAuthor.getUsername());
		targets.put(EventMessages.TITLE, title);
		targets.put(EventMessages.TYPE, type.toString());

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, parentAuthor, beneficiary, baseId, baseString, type,
				commentId, parentCommentId, EVENT_TYPE.COMMENT_SUB, targets);
	}
	
	@Override
	public void eventAppreciationPosting(CachedUsername source, CachedUsername beneficiary, 
			ObjectId postingId, CachedUsername postingAuthor, String postingTitle, 
			double appreciation) 
					throws ServiceException {
		ArgCheck.nullCheck(source, postingId, postingAuthor, postingTitle);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.APPRECIATION, Double.toString(appreciation));
		targets.put(EventMessages.TARGET, postingAuthor.getUsername());
		targets.put(EventMessages.TITLE, postingTitle);

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, postingAuthor, beneficiary, null, null, null, postingId, null, 
				EVENT_TYPE.APPRECIATION_POSTING, targets);
	}
	
	@Override
	public void eventPromotionPosting(CachedUsername source, CachedUsername beneficiary, 
			ObjectId postingId, CachedUsername postingAuthor, String postingTitle, long promotion) 
					throws ServiceException {
		ArgCheck.nullCheck(source, postingId, postingAuthor, postingTitle);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.PROMOTION, Long.toString(promotion));
		targets.put(EventMessages.TARGET, postingAuthor.getUsername());
		targets.put(EventMessages.TITLE, postingTitle);

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, postingAuthor, beneficiary, null, null, null, postingId, null, 
				EVENT_TYPE.PROMOTION_POSTING, targets);
	}
	
	@Override
	public void eventAppreciationComment(CachedUsername source, CachedUsername beneficiary, 
			ObjectId commentId, ObjectId baseId, String baseString, COMMENT_TYPE type, 
			CachedUsername commentAuthor, String title, double appreciation) 
					throws ServiceException {
		ArgCheck.nullCheck(source, commentId, type, commentAuthor, title);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.APPRECIATION, Double.toString(appreciation));
		targets.put(EventMessages.TARGET, commentAuthor.getUsername());
		targets.put(EventMessages.TITLE, title);
		targets.put(EventMessages.TYPE, type.toString());

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, commentAuthor, beneficiary, baseId, baseString, type, commentId, 
				null, EVENT_TYPE.APPRECIATION_COMMENT, targets);
	}
	
	@Override
	public void eventAppreciationAttempt(CachedUsername source, CachedUsername target, double appreciation) throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.APPRECIATION, Double.toString(appreciation));
		targets.put(EventMessages.TITLE, target.getUsername());
		
		createEvent(source, target, null, null, null, null, null, null, EVENT_TYPE.APPRECIATION_ATTEMPT, targets);
	}
	
	@Override
	public void eventPromotionComment(CachedUsername source, CachedUsername beneficiary, 
			ObjectId commentId, ObjectId baseId, String baseString, COMMENT_TYPE type, 
			CachedUsername commentAuthor, String title, long promotion) throws ServiceException {
		ArgCheck.nullCheck(source, commentId, type, commentAuthor, title);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.PROMOTION, Long.toString(promotion));
		targets.put(EventMessages.TARGET, commentAuthor.getUsername());
		targets.put(EventMessages.TITLE, title);
		targets.put(EventMessages.TYPE, type.toString());

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(source, commentAuthor, beneficiary, baseId, baseString, type, commentId, 
				null, EVENT_TYPE.PROMOTION_COMMENT, targets);
	}

	@Override
	public void eventMessage(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		
		createEvent(source, target, null, null, null, null, null, null, EVENT_TYPE.MESSAGE,
				targets);
	}
	
	@Override
	public void eventOffer(CachedUsername source, CachedUsername target, long amount)
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		targets.put(EventMessages.COST, Long.toString(amount));
		
		createEvent(source, target, null, null, null, null, null, null, EVENT_TYPE.OFFER, 
				targets);
	}
	
	@Override
	public void eventOfferAccept(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.OFFER_ACCEPT, targets);
	}
	
	@Override
	public void eventOfferDeny(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		/*
		createEvent(source, target, null, null, null, null, null, null,
				EVENT_TYPE.OFFER_DENY, targets);
		*/
	}
	
	@Override
	public void eventOfferWithdraw(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());

		/*
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.OFFER_WITHDRAW, targets);
		*/
	}
	
	@Override
	public void eventBackingCancel(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		/*
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.BACKING_CANCEL, targets);
		*/
	}
	
	@Override
	public void eventBackingWithdraw(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		/*
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.BACKING_WITHDRAW, targets);
		*/
	}
	
	@Override
	public void eventFollowAdd(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.FOLLOW_ADD, targets);
	}
	
	@Override
	public void eventFollowRemove(CachedUsername source, CachedUsername target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.SOURCE, source.getUsername());
		targets.put(EventMessages.TARGET, target.getUsername());
		
		createEvent(source, target, null, null, null, null, null, null, 
				EVENT_TYPE.FOLLOW_REMOVE, targets);
	}
	
	@Override
	public void eventPostingInfringement(CachedUsername target, CachedUsername beneficiary, 
			String title, ObjectId postingId) throws ServiceException {
		ArgCheck.nullCheck(target, title, postingId);
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.TITLE, title);
		targets.put(EventMessages.TARGET, target.getUsername());

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(target, target, beneficiary, null, null, null, postingId, null, 
				EVENT_TYPE.POSTING_INFRINGEMENT, targets);
	}
	
	@Override
	public void eventCommentInfringement(CachedUsername target, CachedUsername beneficiary, 
			ObjectId commentId, ObjectId baseId, String baseString, COMMENT_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(target, commentId, type);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		
		Map<String, String> targets = new HashMap<String, String>();
		targets.put(EventMessages.TYPE, type.toString());
		targets.put(EventMessages.TARGET, target.getUsername());

		if(beneficiary != null) {
			targets.put(EventMessages.BENEFICIARY, beneficiary.getUsername());
		}
		
		createEvent(target, target, beneficiary, baseId, baseString, type, commentId, 
				null, EVENT_TYPE.COMMENT_INFRINGEMENT, targets);
	}
	
}
