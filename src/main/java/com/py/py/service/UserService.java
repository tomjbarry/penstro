package com.py.py.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.ChangeAppreciationResponseDTO;
import com.py.py.dto.in.ChangeProfileDTO;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.SettingsDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.LOCK_REASON;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.exception.ServiceException;

public interface UserService {
	
	User findUserByUsername(String name) throws ServiceException;

	SettingsDTO getSettingsDTO(User user, UserInfo userInfo) throws ServiceException;

	User findUserByEmail(String email) throws ServiceException;

	void incrementCommentCount(ObjectId id, boolean increment)
			throws ServiceException;

	UserInfo findUserInfo(User user) throws ServiceException;

	UserInfo findUserInfo(ObjectId id) throws ServiceException;

	Boolean option(UserInfo userInfo, SETTING_OPTION setting)
			throws ServiceException;

	void incrementContributedCost(ObjectId id, long cost, boolean posting)
			throws ServiceException;

	void updateAggregate(ObjectId id, long value, TIME_OPTION segment)
			throws ServiceException;

	void aggregateUsers(TIME_OPTION segment) throws ServiceException;

	void checkedNotifications(ObjectId id) throws ServiceException;

	void checkedFeed(ObjectId id) throws ServiceException;

	void changeSettings(ObjectId id, ChangeSettingsDTO dto)
			throws ServiceException;

	void incrementFollowerCount(ObjectId id) throws ServiceException;

	void decrementFollowerCount(ObjectId id) throws ServiceException;

	void addEmailToken(ObjectId id, String emailToken, EMAIL_TYPE type)
			throws ServiceException;

	void addRole(ObjectId id, String role) throws ServiceException;

	void addOverrideRole(ObjectId id, String overrideRole)
			throws ServiceException;

	void removeRole(ObjectId id, String role) throws ServiceException;

	void removeOverrideRole(ObjectId id, String overrideRole)
			throws ServiceException;

	User findUser(ObjectId id) throws ServiceException;

	long getWeight(User user, UserInfo userInfo) throws ServiceException;

	boolean canComment(UserInfo userInfo) throws ServiceException;

	void suspend(User user, LOCK_REASON reason) throws ServiceException;

	void changeProfile(ObjectId userId, ChangeProfileDTO dto)
			throws ServiceException;

	void incrementCommentTallyApproximation(ObjectId id,
			Long appreciationIncrement, Long promotionIncrement, Long cost)
			throws ServiceException;

	void incrementContributedAppreciationPromotion(ObjectId id,
			CachedUsername target, Long appreciation, Long promotion)
			throws ServiceException;

	void incrementAppreciationPromotion(ObjectId id, Long appreciation, Long promotion)
			throws ServiceException;

	RoleSetDTO getRoleSetDTO(User user) throws ServiceException;

	void changeAppreciationResponse(ObjectId userId,
			ChangeAppreciationResponseDTO dto) throws ServiceException;

	Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException;

	void aggregateTotals() throws ServiceException;

	UserInfo createUserInfo(ObjectId id, String username, String language)
			throws ServiceException;

	void resetSettings(ObjectId id, String language) throws ServiceException;

	List<User> findUserListByUsernames(List<String> usernames)
			throws ServiceException;

	ObjectId findUserIdByUsername(String username) throws ServiceException;

	ObjectId findUserIdByEmail(String email) throws ServiceException;

	UserDTO getUserDTOSelf(UserInfo userInfo)
			throws ServiceException;

	void incrementFolloweeCount(ObjectId id) throws ServiceException;

	void decrementFolloweeCount(ObjectId id) throws ServiceException;

	UserInfo findCachedUserInfo(User user) throws ServiceException;

	UserInfo findCachedUserInfo(ObjectId id) throws ServiceException;

	AppreciationResponseDTO getAppreciationResponseDTO(UserInfo userInfo,
			UserInfo targetUserInfo, Boolean warning) throws ServiceException;

	AppreciationResponseDTO getAppreciationResponseDTOSelf(UserInfo userInfo)
			throws ServiceException;

	void flag(User user, UserInfo userInfo, User targetUser,
			UserInfo targetUserInfo, FLAG_REASON reason)
			throws ServiceException;

	void addPendingActions(ObjectId id, List<String> add)
			throws ServiceException;

	void removePendingActions(ObjectId id, List<String> remove)
			throws ServiceException;

	void doPendingSchemaUpdatePassword(ObjectId userId, String password, String schemaUpdate) throws ServiceException;

	Page<UserDTO> getUserPreviewDTOs(UserInfo userInfo, String language, Pageable pageable, Boolean warning,
			TIME_OPTION time, boolean preview) throws ServiceException;

	UserDTO getUserDTO(UserInfo userInfo, UserInfo targetUserInfo, Boolean warning) throws ServiceException;

}
