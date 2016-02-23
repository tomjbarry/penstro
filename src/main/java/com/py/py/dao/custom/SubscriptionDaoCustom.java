package com.py.py.dao.custom;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Subscription;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FollowInfo;

public interface SubscriptionDaoCustom {

	void createSubscription(ObjectId id, List<CachedUsername> followIds)
			throws DaoException;

	void addSubscription(ObjectId id, CachedUsername followId)
			throws DaoException;

	void addBlocked(ObjectId id, CachedUsername blockedId) throws DaoException;

	void removeSubscription(ObjectId id, String followName) throws DaoException;

	void removeBlocked(ObjectId id, String blockedName) throws DaoException;

	Page<FollowInfo> getSortedSubscribed(ObjectId id, Pageable pageable)
			throws DaoException;

	Page<FollowInfo> getSortedBlocked(ObjectId id, Pageable pageable)
			throws DaoException;

	FollowInfo subscribed(ObjectId id, ObjectId followId) throws DaoException;

	FollowInfo blocked(ObjectId id, ObjectId blockedId) throws DaoException;

	Subscription getSubscription(ObjectId id) throws DaoException;

	void setHiddenFeed(ObjectId id, List<String> hiddenFeed)
			throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asFollows)
			throws DaoException;

	void removeUser(ObjectId userId) throws DaoException;

	Subscription getCachedSubscription(ObjectId id) throws DaoException;


}
