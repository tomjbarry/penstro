package com.py.py.service.impl;

import java.math.BigInteger;
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

import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.py.py.constants.RoleNames;
import com.py.py.constants.ServiceValues;
import com.py.py.constants.TimeValues;
import com.py.py.dao.UserDao;
import com.py.py.dao.UserInfoDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.AppreciationDate;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.Settings;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.dto.in.ChangeAppreciationResponseDTO;
import com.py.py.dto.in.ChangeProfileDTO;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.SettingsDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.LOCK_REASON;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.FlagService;
import com.py.py.service.FollowService;
import com.py.py.service.UserService;
import com.py.py.service.base.BaseAggregator;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;
import com.py.py.validation.util.Validation;

public class UserServiceImpl extends BaseAggregator implements UserService {

	protected static final PyLogger logger = PyLogger.getLogger(UserServiceImpl.class);
	
	@Autowired
	protected UserDao userDao;
	
	@Autowired
	protected UserInfoDao userInfoDao;

	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	@Autowired
	protected FollowService followService;
	
	@Autowired
	private FlagService flagService;
	
	@Override
	public List<User> findUserListByUsernames(List<String> usernames) 
			throws ServiceException {
		ArgCheck.nullCheck(usernames);
		List<User> users = new ArrayList<User>();
		for(String name : usernames) {
			try {
				users.add(findUserByUsername(name));
			} catch(ServiceException se) {
				// do nothing
			}
		}
		return users;
	}
	
	@Override
	public User findUserByUsername(String name) throws ServiceException {
		ArgCheck.nullCheck(name);
		String username = ServiceUtils.getIdName(name);
		try {
			User user = userDao.findByUniqueName(username);
			if(user == null) {
				throw new NotFoundException(username);
			}
			
			return user;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	/*
	@Override
	@Cacheable(value = CacheNames.USER_ID_USERNAME, key = "#p0")
	*/
	@Override
	public ObjectId findUserIdByUsername(String username) throws ServiceException {
		return findUserByUsername(username).getId();
	}

	/*
	@Override
	@Cacheable(value = CacheNames.USER_ID_EMAIL, key = "#p0")
	*/
	@Override
	public ObjectId findUserIdByEmail(String email) throws ServiceException {
		return findUserByEmail(email).getId();
	}
	
	/*
	@Override
	@Cacheable(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public User findUser(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			User user = userDao.findOne(id);
			if(user == null) {
				throw new NotFoundException(id.toHexString());
			}
			
			return user;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public User findUserByEmail(String email) throws ServiceException {
		ArgCheck.nullCheck(email);
		
		String correctEmail = ServiceUtils.getUniqueEmail(email);
		
		try {
			User user = userDao.findByEmail(correctEmail);
			if(user == null) {
				throw new NotFoundException(correctEmail);
			}
			
			return user;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}

	@Override
	public UserInfo findCachedUserInfo(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		return findCachedUserInfo(user.getId());
	}
	
	@Override
	public UserInfo findCachedUserInfo(ObjectId id) throws ServiceException {
		return findUserInfo(id, true);
	}

	@Override
	public UserInfo findUserInfo(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		return findUserInfo(user.getId());
	}
	
	@Override
	public UserInfo findUserInfo(ObjectId id) throws ServiceException {
		return findUserInfo(id, false);
	}
	
	private UserInfo findUserInfo(ObjectId id, boolean cached) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
		
			UserInfo userInfo;
			if(cached) {
				userInfo = userInfoDao.findCachedUserInfo(id);
			} else {
				userInfo = userInfoDao.findUserInfo(id);
			}
			if(userInfo == null) {
				throw new NotFoundException(id.toHexString());
			}
			
			return userInfo;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public Page<UserDTO> getUserPreviewDTOs(UserInfo userInfo, String language, Pageable pageable, Boolean warning, 
			TIME_OPTION time, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		
		Page<UserInfo> userInfos = null;
		String correctLanguage = ServiceUtils.getLanguageOrNull(language);
		
		try {
			userInfos = userInfoDao.findUserInfos(correctLanguage, pageable, time);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(userInfos == null) {
			throw new ServiceException();
		}
		
		List<UserDTO> userdtos = ModelFactory.<UserDTO>constructList();
		
		for(UserInfo ui : userInfos.getContent()) {
			try {
				String replacement = null;
				/*if(warning != null && !warning 
						&& userInfo.getDescription() != null && userInfo.isWarning()) {
					replacement = defaultsFactory.getWarningDescriptionReplacement();
				}*/
				UserDTO dto = Mapper.mapUserDTO(ui, replacement, canComment(ui), preview, hasAppreciated(userInfo, ui));
				userdtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for user preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for user preview!", e);
			}
		}
		
		return new PageImpl<UserDTO>(userdtos, pageable, 
				userInfos.getTotalElements());
	}
	
	@Override
	public SettingsDTO getSettingsDTO(User user, UserInfo userInfo) 
			throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		ArgCheck.userCheck(user);
		
		List<EVENT_TYPE> hiddenFeed = followService.getHiddenFeedEvents(user.getId());
		
		return Mapper.mapSettingsDTO(user, userInfo, hiddenFeed);
	}
	
	@Override
	public UserDTO getUserDTO(UserInfo userInfo, UserInfo targetUserInfo, Boolean warning) 
			throws ServiceException {
		ArgCheck.nullCheck(targetUserInfo);
		
		Boolean hide = option(targetUserInfo, SETTING_OPTION.HIDE_USER_PROFILE);
		if(hide != null && hide) {
			throw new ActionNotAllowedException();
		}
		
		String replacement = null;
		/*if(warning != null && !warning 
				&& targetUserInfo.getDescription() != null && targetUserInfo.isWarning()) {
			replacement = defaultsFactory.getWarningDescriptionReplacement();
		}*/
		
		
		return Mapper.mapUserDTO(targetUserInfo, replacement, canComment(targetUserInfo), false, hasAppreciated(userInfo, targetUserInfo));
	}
	
	@Override
	public AppreciationResponseDTO getAppreciationResponseDTO(UserInfo userInfo, UserInfo targetUserInfo, Boolean warning) 
			throws ServiceException {
		ArgCheck.nullCheck(targetUserInfo);
		
		// check if user is authorized
		if(userInfo == null || !hasAppreciated(userInfo, targetUserInfo)) {
			throw new ActionNotAllowedException();
		}
		
		String appreciationResponseReplacement = null;
		/*
		if(warning != null && !warning && targetUserInfo.getAppreciationResponse() != null 
				&& targetUserInfo.isAppreciationResponseWarning()) {
			appreciationResponseReplacement = defaultsFactory.getWarningAppreciationResponseReplacement();
		}
		*/
		
		return Mapper.mapAppreciationResponseDTO(targetUserInfo, appreciationResponseReplacement);
	}
	
	@Override
	public UserDTO getUserDTOSelf(UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		return Mapper.mapUserDTOFull(userInfo, canComment(userInfo));
	}
	
	@Override
	public AppreciationResponseDTO getAppreciationResponseDTOSelf(UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		return Mapper.mapAppreciationResponseDTOFull(userInfo);
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public UserInfo createUserInfo(ObjectId id, String username, String language) 
			throws ServiceException {
		ArgCheck.nullCheck(id, username, language);
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		UserInfo userInfo = new UserInfo();
		userInfo.setUsername(username);
		userInfo.setId(id);
		userInfo.setFlagged(new Date(new Date().getTime() - TimeValues.TIME_YEAR));
		
		Settings settings = defaultsFactory.getSettings();
		if(settings != null) {
			settings.setLanguage(correctLanguage);
			settings.setInterfaceLanguage(correctLanguage);
		}
		
		userInfo.setSettings(settings);
		
		userInfo.setPendingActions(ServiceValues.DEFAULT_PENDING_ACTIONS);
		
		try {
			userInfo = userInfoDao.save(userInfo);
		} catch(DuplicateKeyException dk) {
			throw new ExistsException(username);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		logger.info("Created user info for user (" + username 
				+ ") with id {" + id.toHexString() + "}.");
		return userInfo;
	}
	
	@Override
	public void addEmailToken(ObjectId id, String emailToken, EMAIL_TYPE type) 
			throws ServiceException {
		ArgCheck.nullCheck(id, emailToken);
		
		try {
			userDao.addEmailToken(id, emailToken, type);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Boolean option(UserInfo userInfo, SETTING_OPTION setting) 
			throws ServiceException {
		ArgCheck.nullCheck(userInfo, setting);
		
		Settings settings = userInfo.getSettings();
		if(settings == null) {
			return null;
		}
		
		
		Map<String, Boolean> options = settings.getOptions();
		if(options == null) {
			return null;
		}
		
		return options.get(setting.toString());
	}

	@Override
	public void incrementCommentCount(ObjectId id, boolean increment) 
			throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			if(increment) {
				userInfoDao.incrementCommentCount(id, 1);
			} else {
				userInfoDao.incrementCommentCount(id, -1);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementCommentTallyApproximation(ObjectId id, 
			Long appreciationIncrement, Long promotionIncrement, Long cost)
					throws ServiceException {
		// arguments may be null, do not update nulls
		ArgCheck.nullCheck(id);
		
		try {
			if(cost != null) {
				userInfoDao.incrementCommentTallyCost(id, cost);
			}
			if(appreciationIncrement != null || promotionIncrement != null) {
				userInfoDao.incrementCommentTallyAppreciationPromotion(id, 
						appreciationIncrement, promotionIncrement);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementContributedCost(ObjectId id, long cost, boolean posting) 
			throws ServiceException {
		ArgCheck.nullCheck(id);
		
		long postingCount = 0L;
		long commentCount = 0L;
		if(posting) {
			postingCount = 1L;
		} else {
			commentCount = 1L;
		}
		
		try {
			userInfoDao.incrementContributionCost(id, cost, postingCount, commentCount);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementContributedAppreciationPromotion(ObjectId id, 
			CachedUsername target, Long appreciation, Long promotion)
					throws ServiceException {
		ArgCheck.nullCheck(id);
		if(target == null && appreciation == null && promotion == null) {
			throw new BadParameterException();
		}
		
		Long appreciationCount = null;
		Long promotionCount = null;
		if(appreciation != null) {
			appreciationCount = 1L;
		}
		if(promotion != null) {
			promotionCount = 1L;
		}
		
		try {
			userInfoDao.incrementContributionAppreciationPromotion(id, target, 
					appreciation, appreciationCount, promotion, promotionCount);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementAppreciationPromotion(ObjectId id, Long appreciation,
			Long promotion) throws ServiceException {
		ArgCheck.nullCheck(id);
		if(appreciation == null && promotion == null) {
			throw new BadParameterException();
		}
		
		Long appreciationCount = null;
		Long promotionCount = null;
		if(appreciation != null) {
			appreciationCount = 1L;
		}
		if(promotion != null) {
			promotionCount = 1L;
		}
		
		try {
			userInfoDao.incrementAppreciationPromotion(id, appreciation, appreciationCount, 
					promotion, promotionCount);
			updateAggregates(id.toHexString(), AGGREGATION_TYPE.USER, promotion);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void updateAggregate(ObjectId id, long value, TIME_OPTION segment) 
			throws ServiceException {
		ArgCheck.nullCheck(id, segment);
		
		try {
			userInfoDao.updateAggregation(id, value, segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateUsers(TIME_OPTION segment) throws ServiceException {
		ArgCheck.nullCheck(segment);
		Iterable<DBObject> results = aggregate(AGGREGATION_TYPE.USER, segment);

		for(DBObject obj : results) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			ObjectId id = new ObjectId((String)map.get("_id"));
			long total = (Long) map.get("total");
			updateAggregate(id, total, segment);
		}
		
		try {
			userInfoDao.emptyAggregations(segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateTotals() throws ServiceException {
		updateAggregateTotals(AGGREGATION_TYPE.USER);
	}
	
	@Override
	public void checkedNotifications(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.updateLastChecked(id, new Date(), null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void checkedFeed(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.updateLastChecked(id, null, new Date());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void addPendingActions(ObjectId id, List<String> add) throws ServiceException {
		ArgCheck.nullCheck(id, add);
		
		if(add.isEmpty()) {
			throw new BadParameterException();
		}
		
		try {
			userInfoDao.updatePendingActions(id, add, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removePendingActions(ObjectId id, List<String> remove) throws ServiceException {
		ArgCheck.nullCheck(id, remove);
		
		if(remove.isEmpty()) {
			throw new BadParameterException();
		}
		
		try {
			userInfoDao.updatePendingActions(id, null, remove);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void resetSettings(ObjectId id, String language) throws ServiceException {
		ArgCheck.nullCheck(id);
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		Settings settings = defaultsFactory.getSettings();
		List<Filter> filters = new ArrayList<Filter>();
		if(settings.getFilters() != null) {
			for(Filter f : settings.getFilters().values()) {
				filters.add(f);
			}
		}
		try {
			userInfoDao.updateSettings(id, settings.getOptions(), 
					settings.getHiddenNotifications(), filters, correctLanguage, 
					correctLanguage);
			followService.updateHiddenFeed(id, new ArrayList<EVENT_TYPE>());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void changeSettings(ObjectId id, ChangeSettingsDTO dto) throws ServiceException {
		ArgCheck.nullCheck(id, dto);
		
		Map<SETTING_OPTION, Boolean> options = Mapper.mapOptions(dto);
		List<EVENT_TYPE> hiddenNotifications = Mapper.mapHiddenNotificationEvents(dto);
		List<EVENT_TYPE> hiddenFeed = Mapper.mapHiddenFeedEvents(dto);
		List<Filter> filters = Mapper.mapFilters(dto);
		String correctLanguage = ServiceUtils.getLanguageOrNull(dto.getLanguage());
		String correctInterfaceLanguage = 
				ServiceUtils.getLanguageOrNull(dto.getInterfaceLanguage());
		
		Map<String, Boolean> updateOptions = new HashMap<String, Boolean>();
		List<String> updateNotifications = new ArrayList<String>();
		List<EVENT_TYPE> updateFeed = new ArrayList<EVENT_TYPE>();
		List<Filter> updateFilters = new ArrayList<Filter>();
		if(options != null) {
			for(Map.Entry<SETTING_OPTION, Boolean> entry : options.entrySet()) {
				SETTING_OPTION key = entry.getKey();
				Boolean value = entry.getValue();
				if(Validation.validOption(key, value)) {
					Settings s = defaultsFactory.getSettings();
					if(s != null && s.getOptions() != null) {
						Map<String, Boolean> map = s.getOptions();
						if(map.containsKey(key.toString())) {
							updateOptions.put(key.toString(), value);
						}
					}
				}
			}
		} else {
			updateOptions = null;
		}
		
		if(hiddenNotifications != null) {
			for(EVENT_TYPE type : hiddenNotifications) {
				if(Validation.validEventType(type)) {
					if(defaultsFactory.getNotificationsEventsList() != null && 
							defaultsFactory.getNotificationsEventsList().contains(type)
							&& !updateNotifications.contains(type.toString())) {
						updateNotifications.add(type.toString());
					}
				}
			}
		} else {
			updateNotifications = null;
		}
		
		if(hiddenFeed != null) {
			for(EVENT_TYPE type : hiddenFeed) {
				if(Validation.validEventType(type)) {
					if(defaultsFactory.getFeedEventsList() != null && 
							defaultsFactory.getFeedEventsList().contains(type)
							&& !updateFeed.contains(type)) {
						updateFeed.add(type);
					}
				}
			}
		} else {
			updateFeed = null;
		}
		
		if(filters != null && Validation.validFilters(filters)) {
			for(Filter f : filters) {
				if(Validation.validFilter(f)) {
					updateFilters.add(f);
				}
			}
		}
		
		try {
			if(updateOptions != null || updateNotifications != null 
					|| updateFilters != null || correctLanguage != null 
					|| correctInterfaceLanguage != null) {
				userInfoDao.updateSettings(id, updateOptions, updateNotifications, 
						updateFilters, correctLanguage, correctInterfaceLanguage);
			}
			if(updateFeed != null) {
				followService.updateHiddenFeed(id, updateFeed);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementFollowerCount(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.incrementFollowerCount(id, 1L);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void decrementFollowerCount(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.incrementFollowerCount(id, -1L);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementFolloweeCount(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.incrementFolloweeCount(id, 1L);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void decrementFolloweeCount(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			userInfoDao.incrementFolloweeCount(id, -1L);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void addRole(ObjectId id, String role) throws ServiceException {
		ArgCheck.nullCheck(id, role);
		String validRole = ServiceUtils.getRole(role);
		
		try {
			userDao.addRole(id, validRole, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void addOverrideRole(ObjectId id, String overrideRole) 
			throws ServiceException {
		ArgCheck.nullCheck(id, overrideRole);
		String validOverrideRole = ServiceUtils.getOverrideRole(overrideRole);
		
		try {
			userDao.addRole(id, null, validOverrideRole);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeRole(ObjectId id, String role) throws ServiceException {
		ArgCheck.nullCheck(id, role);
		String validRole = ServiceUtils.getRole(role);
		
		try {
			userDao.removeRole(id, validRole, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeOverrideRole(ObjectId id, String overrideRole) 
			throws ServiceException {
		ArgCheck.nullCheck(id, overrideRole);
		String validOverrideRole = ServiceUtils.getOverrideRole(overrideRole);
		
		try {
			userDao.removeRole(id, null, validOverrideRole);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public long getWeight(User user, UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		ArgCheck.userCheck(user);
		
		long weight = 0;
		Date now = new Date();
		Date joined = user.getCreated();
		
		if(joined == null) {
			return ServiceValues.USER_WEIGHT_MIN;
		}
		
		long ms = (now.getTime() - joined.getTime());
		if(ms <= 0) {
			return ServiceValues.USER_WEIGHT_MIN;
		}
		
		long value = 0;
		value = value + (long)(userInfo.getAppreciation() * ServiceValues.APPRECIATE_WEIGHT);
		TallyApproximation tally = userInfo.getContributionTally();
		if(tally != null) {
			long postings = userInfo.getContributedPostings();
			long comments = userInfo.getContributedComments();
			if(postings <= 0) {
				postings = 1;
			}
			if(comments <= 0) {
				comments = 1;
			}
			long perContent = tally.getCost() / (postings + comments);
			value = value + (perContent * 
					userInfo.getContributedPostings() * ServiceValues.POSTING_WEIGHT);
			value = value + (perContent * 
					userInfo.getContributedComments() * ServiceValues.COMMENT_WEIGHT);
			value = value + (long)(tally.getAppreciation() * ServiceValues.APPRECIATE_WEIGHT);
		}
		
		if(ServiceValues.USER_WEIGHT_NORMALIZATION > 0) {
			weight = value / ServiceValues.USER_WEIGHT_NORMALIZATION;
		}

		long months = ms / TimeValues.TIME_MONTH;
		if(months <= 0) {
			long days = ms / TimeValues.TIME_DAY;
			if(days <= 1) {
				return ServiceValues.USER_WEIGHT_MIN;
			}
			weight = weight / days;
		} else {
			weight = weight / months;
		}
		
		if(weight > ServiceValues.USER_WEIGHT_MAX) {
			weight = ServiceValues.USER_WEIGHT_MAX;
		} else if(weight < ServiceValues.USER_WEIGHT_MIN) {
			weight = ServiceValues.USER_WEIGHT_MIN;
		}
		return weight;
	}
	/*
	@Override
	public void addLocation(ObjectId id, String ipAddress,  
			SaveLocationDTO dto) throws ServiceException {
		ArgCheck.nullCheck(id, ipAddress, dto);
		
		Location location = new Location();
		location.setIp(ipAddress);
		location.setName(dto.getName());
		
		try {
			userDao.addLocation(id, location);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeLocation(ObjectId id, String ipAddress, 
			SaveLocationDTO dto) throws ServiceException {
		ArgCheck.nullCheck(id, ipAddress, dto);
		
		Location location = new Location();
		location.setIp(ipAddress);
		location.setName(dto.getName());
		
		try {
			userDao.removeLocation(id, location);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	*/
	@Override
	public void flag(User user, UserInfo userInfo, User targetUser, UserInfo targetUserInfo, FLAG_REASON reason)
			throws ServiceException {
		ArgCheck.nullCheck(userInfo, targetUserInfo);
		ArgCheck.userCheck(user, targetUser);
		
		long weight = getWeight(user, userInfo);
		
		if(PyUtils.objectIdCompare(user.getId(), targetUser.getId())) {
			throw new ActionNotAllowedException();
		}
		
		if((new Date()).before(targetUserInfo.getFlagged())) {
			// no need
			return;
		}
		if(targetUserInfo.getVotes() != null 
				&& targetUserInfo.getVotes().contains(user.getId())) {
			checkFlag(targetUser, targetUserInfo, 0L);
			return;
		} else {
			checkFlag(targetUser, targetUserInfo, weight);
			if(reason != null) {
				try {
					flagService.addData(targetUser.getId(), FLAG_TYPE.USER, targetUser.getUsername(), weight, reason);
				} catch(ServiceException se) {
					//continue
				}
			}
		}
		
		try {
			userInfoDao.addVote(targetUser.getId(), user.getId(), weight);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void checkFlag(User user, UserInfo userInfo, long addedWeight)
			throws ServiceException {
		Date now = new Date();
		Date created = user.getCreated();
		long monthsSince = (now.getTime() - created.getTime()) / TimeValues.TIME_MONTH;
		
		long timeReduction = 1;
		if(monthsSince > 0) {
			timeReduction = monthsSince;
		}
		if(timeReduction > ServiceValues.FLAG_USER_MAX_MONTHS) {
			timeReduction = ServiceValues.FLAG_USER_MAX_MONTHS;
		}
		
		double flagValue = (userInfo.getFlagValue() + addedWeight) / timeReduction;
		int flagCount = 0;
		if(userInfo.getVotes() != null) {
			flagCount = userInfo.getVotes().size();
		}
		
		long weight = getWeight(user, userInfo);
		
		if(PyUtils.overUserFlagThreshold(weight, flagValue, flagCount)) {
			// should undo flag if they change description
			suspend(user, LOCK_REASON.FLAGGED);
		}
	}
	
	@Override
	public void suspend(User user, LOCK_REASON reason) throws ServiceException {
		ArgCheck.nullCheck(reason);
		ArgCheck.userCheck(user);
		
		if(!canSuspend(user)) {
			return;
		}
		
		long suspensions = user.getSuspensions() + 1;
		Date lockedUntil = PyUtils.calculateSuspension(suspensions, new Date());
		boolean noLocked = (lockedUntil == null);
		
		try {
			userInfoDao.setEnabled(user.getId(), lockedUntil, null, true);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		try {
			userDao.updateStatus(user.getId(), lockedUntil, suspensions, reason, noLocked);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.info("User suspended for reason (" + reason + ") with username (" 
				+ user.getUsername() + ") with id {" + user.getId() + "}.");
		
	}
	
	protected boolean canSuspend(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(user.getRoles() != null && user.getRoles().contains(RoleNames.ADMIN)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public void changeProfile(ObjectId userId, ChangeProfileDTO dto) 
			throws ServiceException {
		ArgCheck.nullCheck(userId, dto);
		
		String description = ServiceUtils.getDescription(dto.getDescription());
		
		boolean noDescription = (description == null);
		boolean warning = dto.isWarning();
		
		try {
			userInfoDao.updateProfile(userId, description, warning, noDescription);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
	}
	
	@Override
	public boolean canComment(UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		Boolean option = option(userInfo, SETTING_OPTION.ALLOW_PROFILE_COMMENTS);
		if(option == null || option == false) {
			return false;
		}
		return true;
	}
	
	@Override
	public RoleSetDTO getRoleSetDTO(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		return Mapper.mapRoleSetDTO(user);
	}
	
	protected boolean hasAppreciated(UserInfo userInfo, UserInfo targetUserInfo) 
			throws ServiceException {
		ArgCheck.nullCheck(targetUserInfo);
		
		if(userInfo == null) {
			return false;
		}
		
		ObjectId targetId = targetUserInfo.getId();
		ArgCheck.nullCheck(targetId);
		
		Date lastAcceptable = new Date((new Date()).getTime() - 
				ServiceValues.APPRECIATION_TARGET_EXPIRY);
		
		if(userInfo.getAppreciationDates() != null) {
			for(AppreciationDate ad : userInfo.getAppreciationDates()) {
				if(ad != null && ad.getCachedUsername() != null
						&& ad.getCachedUsername().getId() != null) {
					if(PyUtils.objectIdCompare(targetId, ad.getCachedUsername().getId())) {
						if(ad.getDate().after(lastAcceptable)) {
							return true;
						}
					}
				}
			}
		}
			
		return false;
	}
	
	@Override
	public void changeAppreciationResponse(ObjectId userId, 
			ChangeAppreciationResponseDTO dto) throws ServiceException {
		ArgCheck.nullCheck(userId, dto);

		
		String appreciationResponse = ServiceUtils.getAppreciationResponse(
				dto.getAppreciationResponse());
		
		boolean noResponse = (appreciationResponse == null);
		boolean warning = dto.isAppreciationResponseWarning();
		
		try {
			userInfoDao.updateAppreciationResponse(userId, 
					appreciationResponse, warning, noResponse);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException {
		return getAggregateTotals(AGGREGATION_TYPE.USER);
	}
	
	@Override
	public void doPendingSchemaUpdatePassword(ObjectId userId, String password, String schemaUpdate) throws ServiceException {
		try {
			userDao.doPendingSchemaUpdate(userId, User.PASSWORD, password, null, schemaUpdate);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
}
