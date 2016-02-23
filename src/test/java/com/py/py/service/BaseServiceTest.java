package com.py.py.service;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.py.py.BaseTest;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.CachedUsername;

public class BaseServiceTest extends BaseTest {

	protected String validName = "test";
	protected String validOtherName = "test2";
	protected String validEmail = "email@email.com";
	protected String validOtherEmail = "email2@email.com";
	protected ObjectId validUserId = new ObjectId();
	protected ObjectId validObjectId = new ObjectId();
	protected String invalidName = "2c`- 0f3=ec2-c=c\n";
	protected String invalidEmail = "2c`- 0f3=ec2-c=c\n";
	protected String validContent = "Test content!";
	protected String validTag = "tag1";
	protected String invalidTag = "2c0n4 4v0 2-c`\n";
	protected String validTitle = "Title!";
	protected String validLanguage = "en";
	protected String invalidLanguage = "TOTALLY INVALID DUDE!!@$#";
	protected long validCost = 100;
	protected long validAppreciation = 10000l;
	protected CachedUsername validSourceCU = new CachedUsername(validUserId, validName);
	protected CachedUsername validTargetCU = new CachedUsername(validUserId, validName);
	
	protected User createValidUser() {
		User user = new User();
		user.setUsername(validName);
		user.setPassword(validName);
		user.setEmail(validEmail);
		user.setId(validUserId);
		return user;
	}
	
	protected User createInvalidUser() {
		User user = new User();
		user.setUsername(null);
		user.setPassword(null);
		user.setEmail(null);
		user.setId(null);
		return user;
	}
	
	protected UserInfo createValidUserInfo() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		userInfo.setFlagged(null);
		return userInfo;
	}
	
	protected UserInfo createInvalidUserInfo() {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(null);
		userInfo.setUsername(null);
		return userInfo;
	}
	
	@Test
	public void test() {
		// nothing
	}
}
