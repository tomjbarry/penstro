package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.SubscriptionDao;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.exception.NoDocumentFoundException;
import com.py.py.domain.Subscription;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.SubscriptionDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.EventService;
import com.py.py.service.FollowService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.LimitException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class FollowServiceImpl implements FollowService {

	protected static final PyLogger logger = PyLogger.getLogger(FollowServiceImpl.class);
	
	@Autowired
	protected SubscriptionDao subscriptionDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	@Override
	public void create(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		ObjectId id = user.getId();
		List<CachedUsername> comparison = defaultsFactory.getFolloweeCachedUsernameList();
		List<CachedUsername> list = new ArrayList<CachedUsername>();
		if(comparison != null) {
			// filter out matches to this user
			for(CachedUsername cu : comparison) {
				if(!PyUtils.objectIdCompare(cu.getId(), id)) {
					list.add(cu);
				}
			}
		}
		
		try {
			subscriptionDao.createSubscription(id, list);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		for(CachedUsername followee : list) {
			userService.incrementFollowerCount(followee.getId());
			userService.incrementFolloweeCount(user.getId());
			eventService.eventFollowAdd(new CachedUsername(id, user.getUsername()), followee);
		}
		logger.info("Created user subscription for id {" + id.toHexString() + "}.");
	}
	
	@Override
	public void addFollowee(User user, User followee) throws ServiceException {
		ArgCheck.userCheck(user, followee);

		if(PyUtils.objectIdCompare(user.getId(), followee.getId())) {
			throw new ActionNotAllowedException();
		}

		CachedUsername userCached = new CachedUsername(user.getId(), user.getUsername());
		CachedUsername followeeCached = new CachedUsername(followee.getId(), 
				followee.getUsername());
		
		if(getFolloweeCount(user.getId()) >= ServiceValues.MAX_FOLLOWEES) {
			throw new LimitException();
		}
		
		try {
			subscriptionDao.addSubscription(user.getId(), followeeCached);
			userService.incrementFollowerCount(followee.getId());
			eventService.eventFollowAdd(userCached, followeeCached);
			userService.incrementFolloweeCount(user.getId());
		} catch(CollisionException ce) {
			throw new ExistsException(followee.getUsername());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeFollowee(User user, User followee, String followName) 
			throws ServiceException {
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(followName);
		
		if(followee != null) {
			followName = followee.getUsername();
		}

		CachedUsername userCached = new CachedUsername(user.getId(), user.getUsername());
		
		try {
			subscriptionDao.removeSubscription(user.getId(), followName);
		} catch(NoDocumentFoundException ndfe) {
			throw new NotFoundException(followName);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// separate catch in case user does not exist, but still needs removal from
		// subscription
		try {
			if(followee != null) {
				CachedUsername followeeCached = new CachedUsername(followee.getId(), 
						followee.getUsername());
				userService.decrementFollowerCount(followeeCached.getId());
				eventService.eventFollowRemove(userCached, followeeCached);
			}
			userService.decrementFolloweeCount(user.getId());
		} catch(NotFoundException nfe) {
			// this is ok
		}
	}
	
	@Override
	public void addBlocked(User user, User blocked) throws ServiceException {
		ArgCheck.userCheck(user, blocked);

		if(PyUtils.objectIdCompare(user.getId(), blocked.getId())) {
			throw new ActionNotAllowedException();
		}
		
		CachedUsername blockedCached = new CachedUsername(blocked.getId(), 
				blocked.getUsername());

		if(getBlockedCount(user.getId()) >= ServiceValues.MAX_BLOCKED) {
			throw new LimitException();
		}
		
		try {
			subscriptionDao.addBlocked(user.getId(), blockedCached);
		} catch(CollisionException ce) {
			throw new ExistsException(blocked.getUsername());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeBlocked(ObjectId id, String blockedName) throws ServiceException {
		ArgCheck.nullCheck(id, blockedName);
		
		try {
			subscriptionDao.removeBlocked(id, blockedName);
		} catch(NoDocumentFoundException ndfe) {
			throw new NotFoundException(blockedName);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Page<FollowDTO> getFolloweeDTOs(ObjectId id, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(id, pageable);
		List<FollowDTO> dtolist = ModelFactory.<FollowDTO>constructList();
		Page<FollowInfo> page = new PageImpl<FollowInfo>(new ArrayList<FollowInfo>(), 
				pageable, 0);
		
		try {
			page = subscriptionDao.getSortedSubscribed(id, pageable);
			
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(FollowInfo fi : page.getContent()) {
			try {
				dtolist.add(Mapper.mapFollowDTO(fi));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for followInfo!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for followInfo!", e);
			}
		}
		
		return new PageImpl<FollowDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public boolean isBlocked(ObjectId id, ObjectId blockedId) throws ServiceException {
		ArgCheck.nullCheck(id, blockedId);
		try {
			FollowInfo fi = getBlocked(id, blockedId);
			if(fi == null) {
				return false;
			}
			return true;
		} catch(NotFoundException nfe) {
			return false;
		}
	}
	
	@Override
	public FollowDTO getBlockedDTO(ObjectId id, ObjectId blockedId) throws ServiceException {
		ArgCheck.nullCheck(id, blockedId);
		return Mapper.mapFollowDTO(getBlocked(id, blockedId));
	}
	
	@Override
	public FollowInfo getBlocked(ObjectId id, ObjectId blockedId) throws ServiceException {
		ArgCheck.nullCheck(id, blockedId);
		
		try {
			FollowInfo followInfo = subscriptionDao.blocked(id, blockedId);
			if(followInfo == null) {
				throw new NotFoundException(blockedId.toHexString());
			}
			return followInfo;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public List<FollowInfo> getBlockedList(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			Subscription sub = subscriptionDao.getSubscription(id);
			if(sub == null) {
				return new ArrayList<FollowInfo>();
			}
			if(sub.getBlocked() == null) {
				return new ArrayList<FollowInfo>();
			}
			return sub.getBlocked();
		} catch(DaoException de) {
			throw new ServiceException();
		}
	}
	
	@Override
	public List<FollowInfo> getFolloweeList(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			Subscription sub = subscriptionDao.getSubscription(id);
			if(sub == null) {
				return new ArrayList<FollowInfo>();
			}
			if(sub.getFollows() == null) {
				return new ArrayList<FollowInfo>();
			}
			return sub.getFollows();
		} catch(DaoException de) {
			throw new ServiceException();
		}
	}
	
	@Override
	public long getBlockedCount(ObjectId id) throws ServiceException {
		return getBlockedList(id).size();
	}
	
	@Override
	public long getFolloweeCount(ObjectId id) throws ServiceException {
		return getFolloweeList(id).size();
	}
	
	@Override
	public Page<FollowDTO> getBlockedDTOs(ObjectId id, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(id, pageable);
		
		List<FollowDTO> dtolist = ModelFactory.<FollowDTO>constructList();
		Page<FollowInfo> page = new PageImpl<FollowInfo>(new ArrayList<FollowInfo>(), pageable, 0);
		
		try {
			page = subscriptionDao.getSortedBlocked(id, pageable);
			
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(FollowInfo fi : page.getContent()) {
			try {
				dtolist.add(Mapper.mapFollowDTO(fi));
			} catch(BadParameterException bpe) {
				// continue
			} catch(Exception e) {
				// continue
			}
		}
		
		return new PageImpl<FollowDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public SubscriptionDTO getSubscription(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			Subscription sub = subscriptionDao.getSubscription(id);
			return Mapper.mapSubscriptionDTO(sub);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Page<FollowDTO> getFollowerDTOs(ObjectId id, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(id, pageable);
		
		return eventService.getFollowerEvents(id, pageable);
	}
	
	@Override
	public FollowInfo getFollowee(ObjectId id, ObjectId followeeId) throws ServiceException {
		ArgCheck.nullCheck(id, followeeId);
		
		try {
			FollowInfo followInfo = subscriptionDao.subscribed(id, followeeId);
			if(followInfo == null) {
				throw new NotFoundException(followeeId.toHexString());
			}
			return followInfo;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public FollowDTO getFolloweeDTO(ObjectId id, ObjectId followeeId) throws ServiceException {
		ArgCheck.nullCheck(id, followeeId);
		return Mapper.mapFollowDTO(getFollowee(id, followeeId));
	}
	
	@Override
	public FollowDTO getFollowerDTO(ObjectId id, ObjectId followerId) throws ServiceException {
		ArgCheck.nullCheck(id, followerId);
		
		try {
			FollowInfo followInfo = subscriptionDao.subscribed(followerId, id);
			if(followInfo == null) {
				throw new NotFoundException(followerId.toHexString());
			}
			return Mapper.mapFollowDTO(followInfo);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Page<FeedDTO> getFeedDTOs(ObjectId id, List<EVENT_TYPE> types, 
			Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(id, pageable);

		List<EVENT_TYPE> events = types;
		if(types == null) {
			events = getHiddenFeedEvents(id);
		}
		
		List<ObjectId> followees = getFolloweeIds(id);
		
		return eventService.checkFeedDTOs(id, followees, events, types == null, pageable);
	}
	
	@Override
	public Page<FeedDTO> getUserFeedDTOs(ObjectId targetId, List<EVENT_TYPE> types, 
			Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(targetId, pageable);
		
		List<EVENT_TYPE> events = types;
		if(types == null) {
			events = defaultsFactory.getFeedEventsList();
		}
		
		return eventService.getFeedDTOs(Arrays.asList(targetId), events, false, pageable);
	}
	
	@Override
	public List<ObjectId> getFolloweeIds(ObjectId id) throws ServiceException {
		List<ObjectId> followees = new ArrayList<ObjectId>();
		
		List<FollowInfo> list = getFolloweeList(id);
		for(FollowInfo fi : list) {
			if(fi != null && fi.getUsername() != null && fi.getUsername().getId() != null) {
				followees.add(fi.getUsername().getId());
			}
		}
		return followees;
	}
	
	@Override
	public List<EVENT_TYPE> getHiddenFeedEvents(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			Subscription subscription = subscriptionDao.getSubscription(id);
			
			List<EVENT_TYPE> shown = new ArrayList<EVENT_TYPE>();
			if(subscription == null) {
				return shown;
			}
			List<String> hidden = subscription.getHiddenFeedEvents();
			if(hidden != null) {
				for(String s : hidden) {
					try {
						EVENT_TYPE e = EVENT_TYPE.valueOf(s.toUpperCase());
						if(defaultsFactory.getFeedEventsList().contains(e)) {
							shown.add(e);
						}
					} catch(Exception e) {
						// do nothing
					}
				}
			}
			
			return shown;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void updateHiddenFeed(ObjectId id, List<EVENT_TYPE> hiddenFeed)
			throws ServiceException {
		ArgCheck.nullCheck(id, hiddenFeed);
		
		List<String> hidden = new ArrayList<String>();
		for(EVENT_TYPE e : defaultsFactory.getFeedEventsList()) {
			try {
				if(hiddenFeed.contains(e)) {
					hidden.add(e.toString());
				}
			} catch(Exception ex) {
				// do nothing
			}
		}
		
		try {
			subscriptionDao.setHiddenFeed(id, hidden);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
}
