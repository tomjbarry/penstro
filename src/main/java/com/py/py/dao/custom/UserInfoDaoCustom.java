package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;

public interface UserInfoDaoCustom {

	void incrementCommentCount(ObjectId id, long count) throws DaoException;

	void incrementCommentTallyCost(ObjectId id, Long cost) throws DaoException;

	UserInfo findUserInfo(ObjectId id) throws DaoException;

	void incrementContributionCost(ObjectId id, long cost, long postings,
			long comments) throws DaoException;

	void updateAggregation(ObjectId id, long value, TIME_OPTION segment)
			throws DaoException;

	void updateLastChecked(ObjectId id, Date notifications, Date feed)
			throws DaoException;

	void emptyAggregations(TIME_OPTION segment) throws DaoException;

	void incrementFollowerCount(ObjectId id, long increment) throws DaoException;

	void addVote(ObjectId id, ObjectId userId, long weight) throws DaoException;

	void incrementCommentTallyAppreciationPromotion(ObjectId id,
			Long appreciation, Long promotion) throws DaoException;

	void incrementContributionAppreciationPromotion(ObjectId id,
			CachedUsername target, Long appreciation, Long appreciationCount,
			Long promotion, Long promotionCount) throws DaoException;

	void incrementAppreciationPromotion(ObjectId id, Long appreciation,
			Long appreciationCount, Long promotion, Long promotionCount)
			throws DaoException;

	void updateAppreciationResponse(ObjectId id, String appreciationResponse,
			Boolean warning, boolean noResponse) throws DaoException;

	void rename(ObjectId id, String replacement) throws DaoException;

	void renameUserInAppreciationDates(ObjectId userId, String replacement)
			throws DaoException;

	void removeUserInAppreciationDates(ObjectId userId) throws DaoException;

	void updateProfile(ObjectId id, String description, 
			Boolean warning, boolean noDescription) throws DaoException;

	Page<UserInfo> findUserInfos(String language, Pageable pageable,
			TIME_OPTION time) throws DaoException;

	void updateSettings(ObjectId id, Map<String, Boolean> options,
			List<String> hiddenNotifications, List<Filter> filters,
			String language, String interfaceLanguage) throws DaoException;

	void incrementFolloweeCount(ObjectId id, long increment)
			throws DaoException;

	void setEnabled(ObjectId id, Date flagged, Boolean warning,
			boolean clearVotes) throws DaoException;

	UserInfo findCachedUserInfo(ObjectId id) throws DaoException;

	void updatePendingActions(ObjectId id, List<String> add, List<String> remove)
			throws DaoException;

	void updatePendingActions(ObjectId id, List<String> pendingActions)
			throws DaoException;

}
