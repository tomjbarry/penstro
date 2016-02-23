package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;
import com.py.py.constants.OverrideRoleNames;
import com.py.py.constants.RoleNames;
import com.py.py.dao.AggregationDao;
import com.py.py.dao.UserDao;
import com.py.py.dao.UserInfoDao;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.AppreciationDate;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.Settings;
import com.py.py.dto.in.ChangeAppreciationResponseDTO;
import com.py.py.dto.in.ChangeProfileDTO;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.LOCK_REASON;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.util.DefaultsFactory;

public class UserServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("userService")
	private UserService userService;
	
	@Autowired
	protected UserDao userDao;
	
	@Autowired
	protected UserInfoDao userInfoDao;

	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	@Autowired
	protected FollowService followService;
	
	@Autowired
	protected AggregationDao aggregationDao;
	
	private String validNameNotFound = "notfound";
	private String validEmailNotFound = "cda@cda.com";
	private ObjectId validUserIdNotFound = new ObjectId();
	private String validEmailToken = "validET";
	private String invalidRoleName = "cdt289djc0";
	
	protected UserInfo createValidUserInfo() {
		UserInfo userInfo = new UserInfo();
		User user = createValidUser();
		userInfo.setUsername(user.getUsername());
		userInfo.setId(user.getId());
		return userInfo;
	}
	
	@Before
	public void setUp() throws Exception {
		reset(userDao, userInfoDao, defaultsFactory, followService, aggregationDao);
		when(userDao.findByUniqueName(validName)).thenReturn(createValidUser());
		when(userDao.findByEmail(validEmail)).thenReturn(createValidUser());
		when(userDao.findOne(validUserId)).thenReturn(createValidUser());
		when(userInfoDao.findUserInfo(validUserId)).thenReturn(new UserInfo());
		when(userInfoDao.findOne(validUserId)).thenReturn(new UserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserByUsernameNull() throws Exception {
		userService.findUserByUsername((String)null);
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserByUsernameInvalid() throws Exception {
		userService.findUserByUsername(invalidName);
	}
	
	@Test(expected = NotFoundException.class)
	public void findUserByUsernameNotFound() throws Exception {
		userService.findUserByUsername(validNameNotFound);
	}
	
	@Test
	public void findUserByUsername() throws Exception {
		Assert.assertNotNull(userService.findUserByUsername(validName));
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserNull() throws Exception {
		userService.findUser((ObjectId)null);
	}
	
	@Test(expected = NotFoundException.class)
	public void findUserNotFound() throws Exception {
		userService.findUser(validUserIdNotFound);
	}
	
	@Test
	public void findUser() throws Exception {
		Assert.assertNotNull(userService.findUser(validUserId));
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserByEmailNull() throws Exception {
		userService.findUserByEmail(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserByEmailInvalid() throws Exception {
		userService.findUserByEmail(invalidEmail);
	}
	
	@Test(expected = NotFoundException.class)
	public void findUserByEmailNotFound() throws Exception {
		userService.findUserByEmail(validEmailNotFound);
	}
	
	@Test
	public void findUserByEmail() throws Exception {
		Assert.assertNotNull(userService.findUserByEmail(validEmail));
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserInfoUserNull() throws Exception {
		userService.findUserInfo((User)null);
	}
	
	@Test(expected = NotFoundException.class)
	public void findUserInfoUserNotFound() throws Exception {
		User user = createValidUser();
		user.setId(validUserIdNotFound);
		userService.findUserInfo(user);
	}
	
	@Test
	public void findUserInfoUser() throws Exception {
		Assert.assertNotNull(userService.findUserInfo(createValidUser()));
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserInfoNull() throws Exception {
		userService.findUserInfo((ObjectId)null);
	}
	
	@Test(expected = NotFoundException.class)
	public void findUserInfoNotFound() throws Exception {
		userService.findUserInfo(validUserIdNotFound);
	}
	
	@Test
	public void findUserInfo() throws Exception {
		Assert.assertNotNull(userService.findUserInfo(validUserId));
	}
	
	@Test(expected = BadParameterException.class)
	public void findUserPreviewDTOsNull() throws Exception {
		userService.getUserPreviewDTOs(createValidUserInfo(), validLanguage, null, randomBoolean(), TIME_OPTION.ALLTIME, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getUserPreviewDTOsInvalid1() throws Exception {
		UserInfo userInfo1 = new UserInfo();
		userInfo1.setId(validUserId);
		userInfo1.setUsername(validName);
		UserInfo userInfo2 = new UserInfo();
		userInfo2.setId(validUserId);
		userInfo2.setUsername(validName);
		UserInfo userInfo3 = new UserInfo();
		userInfo3.setId(validUserId);
		userInfo3.setUsername(validName);
		List<UserInfo> validUserInfos = Arrays.asList(userInfo1, userInfo2, userInfo3);
		Page<UserInfo> validUserInfoPage = new PageImpl<UserInfo>(validUserInfos);
		when(userInfoDao.findUserInfos(anyString(), any(Pageable.class), 
				any(TIME_OPTION.class)))
			.thenReturn(validUserInfoPage);
		
		Page<UserDTO> result = userService.getUserPreviewDTOs(null, invalidLanguage, 
				constructPageable(), randomBoolean(), TIME_OPTION.DAY, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validUserInfos.size());
	}
	
	@Test
	public void getUserPreviewDTOsInvalidList() throws Exception {
		UserInfo userInfo1 = new UserInfo();
		userInfo1.setId(validUserId);
		userInfo1.setUsername(validName);
		UserInfo userInfo2 = new UserInfo();
		userInfo2.setId(validUserId);
		userInfo2.setUsername(validName);
		UserInfo userInfo3 = new UserInfo();
		userInfo3.setId(validUserId);
		userInfo3.setUsername(validName);
		List<UserInfo> validUserInfos = Arrays.asList(userInfo1, userInfo2, userInfo3);
		Page<UserInfo> validUserInfoPage = new PageImpl<UserInfo>(validUserInfos);
		when(userInfoDao.findUserInfos(anyString(), any(Pageable.class), 
				any(TIME_OPTION.class)))
			.thenReturn(validUserInfoPage);
		
		Page<UserDTO> result = userService.getUserPreviewDTOs(null, validLanguage, 
				constructPageable(), randomBoolean(), TIME_OPTION.DAY, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validUserInfos.size());
	}
	
	@Test
	public void getUserPreviewDTOs() throws Exception {
		UserInfo userInfo1 = new UserInfo();
		userInfo1.setId(validUserId);
		userInfo1.setUsername(validName);
		UserInfo userInfo2 = new UserInfo();
		userInfo2.setId(validUserId);
		userInfo2.setUsername(validName);
		UserInfo userInfo3 = new UserInfo();
		userInfo3.setId(validUserId);
		userInfo3.setUsername(validName);
		List<UserInfo> validUserInfos = Arrays.asList(userInfo1, userInfo2, userInfo3);
		Page<UserInfo> validUserInfoPage = new PageImpl<UserInfo>(validUserInfos);
		when(userInfoDao.findUserInfos(anyString(), any(Pageable.class), 
				any(TIME_OPTION.class)))
			.thenReturn(validUserInfoPage);
		
		Page<UserDTO> result = userService.getUserPreviewDTOs(createValidUserInfo(), validLanguage, 
				constructPageable(), true, TIME_OPTION.DAY, false);
		Assert.assertEquals(result.getContent().size(), validUserInfos.size());
	}

	@Test(expected = BadParameterException.class)
	public void getSettingsDTONull1() throws Exception {
		userService.getSettingsDTO(null, new UserInfo());
	}

	@Test(expected = BadParameterException.class)
	public void getSettingsDTONull2() throws Exception {
		userService.getSettingsDTO(createValidUser(), null);
	}

	@Test(expected = BadParameterException.class)
	public void getSettingsDTOInvalid() throws Exception {
		when(followService.getHiddenFeedEvents(any(ObjectId.class)))
			.thenReturn(null);
		UserInfo userInfo = createValidUserInfo();
		userInfo.setSettings(new Settings());
		userService.getSettingsDTO(createValidUser(), userInfo);
	}

	@Test
	public void getSettingsDTO() throws Exception {
		when(followService.getHiddenFeedEvents(any(ObjectId.class)))
			.thenReturn(new ArrayList<EVENT_TYPE>());
		UserInfo userInfo = createValidUserInfo();
		userInfo.setSettings(new Settings());
		userService.getSettingsDTO(createValidUser(), userInfo);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void getAppreciationResponseDTONull1() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userService.getAppreciationResponseDTO(null, userInfo, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAppreciationResponseDTONull2() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userService.getAppreciationResponseDTO(userInfo, null, randomBoolean());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void getAppreciationResponseDTONotAllowed() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		UserInfo targetUserInfo = createValidUserInfo();
		userService.getAppreciationResponseDTO(userInfo, targetUserInfo, randomBoolean());
	}
	
	@Test
	public void getAppreciationResponseDTO() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		UserInfo targetUserInfo = createValidUserInfo();
		CachedUsername cu = new CachedUsername(targetUserInfo.getId(), targetUserInfo.getUsername());
		AppreciationDate ad = new AppreciationDate(cu, new Date());
		List<AppreciationDate> dates = new ArrayList<AppreciationDate>();
		dates.add(ad);
		userInfo.setAppreciationDates(dates);
		userService.getAppreciationResponseDTO(userInfo, targetUserInfo, null);
		userService.getAppreciationResponseDTO(userInfo, targetUserInfo, true);
		userService.getAppreciationResponseDTO(userInfo, targetUserInfo, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void getUserDTONull() throws Exception {
		userService.getUserDTO(createValidUserInfo(), null, randomBoolean());
	}
	
	@Test
	public void getUserDTO() throws Exception {
		UserInfo targetUserInfo = createValidUserInfo();
		userService.getUserDTO(createValidUserInfo(), targetUserInfo, null);
		userService.getUserDTO(createValidUserInfo(), targetUserInfo, true);
		userService.getUserDTO(createValidUserInfo(), targetUserInfo, false);
		userService.getUserDTO(createValidUserInfo(), targetUserInfo, true);
		userService.getUserDTO(createValidUserInfo(), targetUserInfo, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void getUserDTOSelfNull() throws Exception {
		userService.getUserDTOSelf(null);
	}
	
	@Test
	public void getUserDTOSelf() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userService.getUserDTOSelf(userInfo);
	}
	
	@Test(expected = BadParameterException.class)
	public void createUserInfoNull1() throws Exception {
		userService.createUserInfo(null, validName, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createUserInfoNull2() throws Exception {
		userService.createUserInfo(validUserId, null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createUserInfoNull3() throws Exception {
		userService.createUserInfo(validUserId, validName, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void createUserInfoInvalid() throws Exception {
		userService.createUserInfo(validUserId, validName, invalidLanguage);
	}
	
	@Test
	public void createUserInfo() throws Exception {
		userService.createUserInfo(validUserId, validName, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailTokenNull1() throws Exception {
		userService.addEmailToken(null, validEmailToken, EMAIL_TYPE.CHANGE);
	}
	
	@Test(expected = BadParameterException.class)
	public void addEmailTokenNull2() throws Exception {
		userService.addEmailToken(validUserId, null, EMAIL_TYPE.CHANGE);
	}
	
	@Test
	public void addEmailToken() throws Exception {
		userService.addEmailToken(validUserId, validEmailToken, EMAIL_TYPE.CHANGE);
	}
	
	@Test(expected = BadParameterException.class)
	public void optionNull1() throws Exception {
		userService.option(null, SETTING_OPTION.ALLOW_PROFILE_COMMENTS);
	}
	
	@Test(expected = BadParameterException.class)
	public void optionNull2() throws Exception {
		userService.option(new UserInfo(), null);
	}
	
	@Test
	public void option() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setSettings(null);
		Assert.assertNull(userService.option(userInfo, 
				SETTING_OPTION.ALLOW_PROFILE_COMMENTS));
	
		Settings settings = new Settings();
		settings.setOptions(null);
		userInfo.setSettings(settings);
		Assert.assertNull(userService.option(userInfo, 
				SETTING_OPTION.ALLOW_PROFILE_COMMENTS));
		
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		settings.setOptions(map);
		Assert.assertNull(userService.option(userInfo, 
				SETTING_OPTION.ALLOW_PROFILE_COMMENTS));
		
		map.put(SETTING_OPTION.ALLOW_PROFILE_COMMENTS.toString(), true);
		Assert.assertTrue(userService.option(userInfo, SETTING_OPTION.ALLOW_PROFILE_COMMENTS));
		map.put(SETTING_OPTION.ALLOW_PROFILE_COMMENTS.toString(), false);
		Assert.assertFalse(userService.option(userInfo, SETTING_OPTION.ALLOW_PROFILE_COMMENTS));
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentCountNull() throws Exception {
		userService.incrementCommentCount(null, randomBoolean());
	}
	
	@Test
	public void incrementCommentCount() throws Exception {
		userService.incrementCommentCount(validUserId, true);
		userService.incrementCommentCount(validUserId, false);
		verify(userInfoDao).incrementCommentCount(validUserId, 1);
		verify(userInfoDao).incrementCommentCount(validUserId, -1);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentTallyApproximationNull() throws Exception {
		userService.incrementCommentTallyApproximation(null, validAppreciation, validCost, 
				validCost);
	}
	
	@Test
	public void incrementCommentTallyApproximation() throws Exception {
		userService.incrementCommentTallyApproximation(validUserId, null, null, null);
		userService.incrementCommentTallyApproximation(validUserId, null, null, validCost);
		userService.incrementCommentTallyApproximation(validUserId, null, validCost, null);
		userService.incrementCommentTallyApproximation(validUserId, null, validCost, 
				validCost);
		userService.incrementCommentTallyApproximation(validUserId, validAppreciation, null, 
				null);
		userService.incrementCommentTallyApproximation(validUserId, validAppreciation, null, 
				validCost);
		userService.incrementCommentTallyApproximation(validUserId, validAppreciation, validCost, 
				null);
		userService.incrementCommentTallyApproximation(validUserId, validAppreciation, validCost, 
				validCost);
		verify(userInfoDao, times(4))
			.incrementCommentTallyCost(validUserId, validCost);
		verify(userInfoDao, times(6)).incrementCommentTallyAppreciationPromotion(
				any(ObjectId.class), anyLong	(), anyLong());
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementContributedCostNull() throws Exception {
		userService.incrementContributedCost(null, 5L, randomBoolean());
	}
	
	@Test
	public void incrementContributedCost() throws Exception {
		long cost = 5L;
		userService.incrementContributedCost(validUserId, cost, true);
		userService.incrementContributedCost(validUserId, cost, false);
		verify(userInfoDao).incrementContributionCost(
				validUserId, cost, 1L, 0L);
		verify(userInfoDao).incrementContributionCost(
				validUserId, cost, 0L, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementContributedAppreciationNull() throws Exception {
		CachedUsername cu = new CachedUsername(validObjectId, validName);
		userService.incrementContributedAppreciationPromotion(null, cu, validAppreciation, 
				validCost);
	}
	
	@Test
	public void incrementContributedAppreciationPromotion() throws Exception {
		CachedUsername cu = new CachedUsername(validObjectId, validName);
		userService.incrementContributedAppreciationPromotion(validUserId, cu, validAppreciation, 
				validCost);
		verify(userInfoDao).incrementContributionAppreciationPromotion(
				validUserId, cu, validAppreciation, 1L, validCost, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementAppreciationNull() throws Exception {
		userService.incrementAppreciationPromotion(null, validAppreciation, validCost);
	}
	
	@Test
	public void incrementAppreciation() throws Exception {
		userService.incrementAppreciationPromotion(validUserId, validAppreciation, validCost);
		verify(userInfoDao).incrementAppreciationPromotion(
				validUserId, validAppreciation, 1L, validCost, 1L);
		verify(aggregationDao, atLeast(1)).add(any(AGGREGATION_TYPE.class), 
				anyString(), anyLong(), any(Date.class), 
				anyLong(), any(TIME_OPTION.class));
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull1() throws Exception {
		userService.updateAggregate(null, 5L, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull2() throws Exception {
		userService.updateAggregate(validUserId, 5L, null);
	}
	
	@Test
	public void updateAggregate() throws Exception {
		userService.updateAggregate(validUserId, 5L, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void aggregateUsersNull() throws Exception {
		userService.aggregateUsers(null);
	}
	
	@Test
	public void aggregateUsers() throws Exception {
		AggregationOutput output = mock(AggregationOutput.class);
		when(output.results()).thenReturn(new ArrayList<DBObject>());
		when(aggregationDao.getAggregation(any(AGGREGATION_TYPE.class), 
				any(Date.class),any(TIME_OPTION.class), anyLong()))
				.thenReturn(output);
		userService.aggregateUsers(TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void checkedNotificationsNull() throws Exception {
		userService.checkedNotifications(null);
	}
	
	@Test
	public void checkedNotifications() throws Exception {
		userService.checkedNotifications(validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void checkedFeedNull() throws Exception {
		userService.checkedFeed(null);
	}
	
	@Test
	public void checkedFeed() throws Exception {
		userService.checkedFeed(validUserId);
	}
	
	@Test(expected = BadParameterException.class)
	public void resetSettingsNull1() throws Exception {
		userService.resetSettings(null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void resetSettingsNull2() throws Exception {
		userService.resetSettings(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void resetSettingsInvalid2() throws Exception {
		userService.resetSettings(validUserId, invalidLanguage);
	}
	
	@Test
	public void resetSettings() throws Exception {
		when(defaultsFactory.getSettings()).thenReturn(new Settings());
		userService.resetSettings(validUserId, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeSettingsNull1() throws Exception {
		userService.changeSettings(null, new ChangeSettingsDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void changeSettingsNull2() throws Exception {
		userService.changeSettings(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeSettingsInvalid() throws Exception {
		ChangeSettingsDTO dto = new ChangeSettingsDTO();
		dto.setLanguage(invalidLanguage);
		userService.changeSettings(validUserId, dto);
	}
	
	@Test
	public void changeSettings() throws Exception {
		ChangeSettingsDTO dto = new ChangeSettingsDTO();
		dto.setLanguage(validLanguage);
		userService.changeSettings(validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementFollowerCountNull() throws Exception {
		userService.incrementFollowerCount(null);
	}
	
	@Test
	public void incrementFollowerCount() throws Exception {
		userService.incrementFollowerCount(validUserId);
		verify(userInfoDao).incrementFollowerCount(validUserId, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void decrementFollowerCountNull() throws Exception {
		userService.decrementFollowerCount(null);
	}
	
	@Test
	public void decrementFollowerCount() throws Exception {
		userService.decrementFollowerCount(validUserId);
		verify(userInfoDao).incrementFollowerCount(validUserId, -1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void addRoleNull1() throws Exception {
		userService.addRole(null, RoleNames.CONTRIBUTE);
	}
	
	@Test(expected = BadParameterException.class)
	public void addRoleNull2() throws Exception {
		userService.addRole(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addRoleInvalid() throws Exception {
		userService.addRole(validUserId, invalidRoleName);
	}
	
	@Test
	public void addRole() throws Exception {
		userService.addRole(validUserId, RoleNames.CONTRIBUTE);
		verify(userDao).addRole(validUserId, RoleNames.CONTRIBUTE, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRoleNull1() throws Exception {
		userService.removeRole(null, RoleNames.CONTRIBUTE);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRoleNull2() throws Exception {
		userService.removeRole(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRoleInvalid() throws Exception {
		userService.removeRole(validUserId, invalidRoleName);
	}
	
	@Test
	public void removeRole() throws Exception {
		userService.removeRole(validUserId, RoleNames.CONTRIBUTE);
		verify(userDao).removeRole(validUserId, RoleNames.CONTRIBUTE, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOverrideRoleNull1() throws Exception {
		userService.addOverrideRole(null, OverrideRoleNames.UNACCEPTED);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOverrideRoleNull2() throws Exception {
		userService.addOverrideRole(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addOverrideRoleInvalid() throws Exception {
		userService.addOverrideRole(validUserId, invalidRoleName);
	}
	
	@Test
	public void addOverrideRole() throws Exception {
		userService.addOverrideRole(validUserId, OverrideRoleNames.UNACCEPTED);
		verify(userDao).addRole(validUserId, null, OverrideRoleNames.UNACCEPTED);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeOverrideRoleNull1() throws Exception {
		userService.removeOverrideRole(null, OverrideRoleNames.UNACCEPTED);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeOverrideRoleNull2() throws Exception {
		userService.removeOverrideRole(validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeOverrideRoleInvalid() throws Exception {
		userService.removeOverrideRole(validUserId, invalidRoleName);
	}
	
	@Test
	public void removeOverrideRole() throws Exception {
		userService.removeOverrideRole(validUserId, OverrideRoleNames.UNACCEPTED);
		verify(userDao).removeRole(validUserId, null, OverrideRoleNames.UNACCEPTED);
	}
	
	@Test(expected = BadParameterException.class)
	public void getWeightNull1() throws Exception {
		userService.getWeight(null, new UserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void getWeightNull2() throws Exception {
		userService.getWeight(createValidUser(), null);
	}
	
	@Test
	public void getWeight() throws Exception {
		UserInfo userInfo = new UserInfo();
		
		userService.getWeight(createValidUser(), userInfo);
	}
	/*
	@Test(expected = BadParameterException.class)
	public void addLocationNull1() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.addLocation(null, ipAddress, new SaveLocationDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void addLocationNull2() throws Exception {
		userService.addLocation(validUserId, null, new SaveLocationDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void addLocationNull3() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.addLocation(validUserId, ipAddress, null);
	}
	
	@Test
	public void addLocation() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.addLocation(validUserId, ipAddress, new SaveLocationDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeLocationNull1() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.removeLocation(null, ipAddress, new SaveLocationDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeLocationNull2() throws Exception {
		userService.removeLocation(validUserId, null, new SaveLocationDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeLocationNull3() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.removeLocation(validUserId, ipAddress, null);
	}
	
	@Test
	public void removeLocation() throws Exception {
		String ipAddress = "127.0.0.1";
		userService.removeLocation(validUserId, ipAddress, new SaveLocationDTO());
	}
	*/
	
	@Test(expected = BadParameterException.class)
	public void flagNull1() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(null, createValidUserInfo(), targetUser, targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull2() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createValidUser(), null, targetUser, targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull3() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createValidUser(), createValidUserInfo(), null, targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull4() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createValidUser(), createValidUserInfo(), targetUser, null, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagInvalid1() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createInvalidUser(), createValidUserInfo(), targetUser, 
				targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagInvalid3() throws Exception {
		ObjectId targetId = new ObjectId();
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createValidUser(), createValidUserInfo(), createInvalidUser(), 
				targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void flagNotAllowed() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		User user = createValidUser();
		user.setUsername(validOtherName);
		user.setId(targetId);
		userService.flag(user, createValidUserInfo(), targetUser, createValidUserInfo(), FLAG_REASON.ILLICIT);
	}
	
	@Test
	public void flag() throws Exception {
		ObjectId targetId = new ObjectId();
		User targetUser = createValidUser();
		targetUser.setUsername(validOtherName);
		targetUser.setId(targetId);
		UserInfo targetUserInfo = createValidUserInfo();
		targetUserInfo.setUsername(validOtherName);
		targetUserInfo.setId(targetId);
		userService.flag(createValidUser(), createValidUserInfo(), targetUser, 
				targetUserInfo, null);
		userService.flag(createValidUser(), createValidUserInfo(), targetUser, 
				targetUserInfo, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void suspendNull1() throws Exception {
		userService.suspend(null, LOCK_REASON.FLAGGED);
	}
	
	@Test(expected = BadParameterException.class)
	public void suspendNull2() throws Exception {
		userService.suspend(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void suspendInvalid() throws Exception {
		userService.suspend(createInvalidUser(), LOCK_REASON.FLAGGED);
	}
	
	@Test
	public void suspend() throws Exception {
		userService.suspend(createValidUser(), LOCK_REASON.FLAGGED);
	}
	
	@Test(expected = BadParameterException.class)
	public void canCommentNull() throws Exception {
		userService.canComment(null);
	}
	
	@Test
	public void canComment() throws Exception {
		UserInfo userInfo = new UserInfo();
		Settings settings = new Settings();
		settings.setOptions(null);
		userInfo.setSettings(null);
		Assert.assertFalse(userService.canComment(userInfo));
		userInfo.setSettings(settings);
		Assert.assertFalse(userService.canComment(userInfo));
		Map<String, Boolean> options = new HashMap<String, Boolean>();
		settings.setOptions(options);
		Assert.assertFalse(userService.canComment(userInfo));
		options.put(SETTING_OPTION.ALLOW_PROFILE_COMMENTS.toString(), false);
		Assert.assertFalse(userService.canComment(userInfo));
		options.put(SETTING_OPTION.ALLOW_PROFILE_COMMENTS.toString(), true);
		Assert.assertTrue(userService.canComment(userInfo));
	}
	
	@Test(expected = BadParameterException.class)
	public void getRoleSetDTONull() throws Exception {
		userService.getRoleSetDTO(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getRoleSetDTOInvalid() throws Exception {
		userService.getRoleSetDTO(createInvalidUser());
	}
	
	@Test
	public void getRoleSetDTO() throws Exception {
		userService.getRoleSetDTO(createValidUser());
	}
	
	@Test(expected = BadParameterException.class)
	public void changeProfileNull1() throws Exception {
		ChangeProfileDTO dto = new ChangeProfileDTO();
		dto.setDescription(validContent);
		dto.setWarning(randomBoolean());
		userService.changeProfile(null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeProfileNull2() throws Exception {
		ChangeProfileDTO dto = new ChangeProfileDTO();
		dto.setDescription(validContent);
		dto.setWarning(randomBoolean());
		userService.changeProfile(validUserId, null);
	}
	
	@Test
	public void changeProfile() throws Exception {
		ChangeProfileDTO dto = new ChangeProfileDTO();
		dto.setDescription(validContent);
		dto.setWarning(true);
		userService.changeProfile(validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeAppreciationResponseNull1() throws Exception {
		ChangeAppreciationResponseDTO dto = new ChangeAppreciationResponseDTO();
		dto.setAppreciationResponse(validContent);
		dto.setAppreciationResponseWarning(randomBoolean());
		userService.changeAppreciationResponse(null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeAppreciationResponseNull2() throws Exception {
		ChangeAppreciationResponseDTO dto = new ChangeAppreciationResponseDTO();
		dto.setAppreciationResponse(validContent);
		dto.setAppreciationResponseWarning(randomBoolean());
		userService.changeAppreciationResponse(validUserId, null);
	}
	
	@Test
	public void changeAppreciationResponse() throws Exception {
		ChangeAppreciationResponseDTO dto = new ChangeAppreciationResponseDTO();
		dto.setAppreciationResponse(validContent);
		dto.setAppreciationResponseWarning(true);
		userService.changeAppreciationResponse(validUserId, dto);
	}
}