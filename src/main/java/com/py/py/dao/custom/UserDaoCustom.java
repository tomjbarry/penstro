package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.LoginAttempt;
import com.py.py.enumeration.LOCK_REASON;

public interface UserDaoCustom {

	void addLoginAttempt(ObjectId id, LoginAttempt attempt)
			throws DaoException;
	
	void clearLoginAttempts(ObjectId id, Date date, Boolean success)
			throws DaoException;

	User findByEmail(String email) throws DaoException;

	void addEmailToken(ObjectId id, String token, EMAIL_TYPE type)
			throws DaoException;

	boolean hasEmailToken(ObjectId id, String token, EMAIL_TYPE type)
			throws DaoException;

	void removeEmailToken(ObjectId id, String token, EMAIL_TYPE type)
			throws DaoException;

	void addRole(ObjectId id, String role, String overrideRole)
			throws DaoException;

	void removeRole(ObjectId id, String role, String overrideRole)
			throws DaoException;

	void updateRoles(ObjectId id, List<String> roles,
			List<String> overrideRoles) throws DaoException;

	User findByUniqueName(String username) throws DaoException;

	void updateStatus(ObjectId id, Date lockedUntil, Long suspensions,
			LOCK_REASON reason, boolean noLocked) throws DaoException;

	void updateUser(ObjectId id, String email, String password)
			throws DaoException;

	void updatePayment(ObjectId id, String paymentId) throws DaoException;

	void setDeleted(ObjectId id, Date deletedDate) throws DaoException;

	Page<User> getDeleted(Date olderThanDeleted, Pageable pageable)
			throws DaoException;

	void rename(ObjectId id, String rename, String replacement)
			throws DaoException;

	Page<User> getRename(Pageable pageable) throws DaoException;

	User create(User user) throws DaoException;

	void doPendingSchemaUpdate(ObjectId id, String field, String value, String addPending, String removePending)
		throws DaoException;

	void addLocation(ObjectId id, String location) throws DaoException;

	void setPasswordAttempts(ObjectId id, int fails, Date when, boolean setNotIncrement) throws DaoException;
	
}
