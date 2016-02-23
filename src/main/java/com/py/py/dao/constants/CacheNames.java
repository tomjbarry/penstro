package com.py.py.dao.constants;

import com.py.py.domain.constants.CollectionNames;

public class CacheNames {

	protected static final String PAGED = "_paged";
	
	//public static final String USER = CollectionNames.USER;
	private static final String USER = CollectionNames.USER;
	public static final String USER_ID_USERNAME = USER + "_username";
	//public static final String USER_ID_EMAIL = USER + "_email";
	//public static final String USER_CURRENT = USER + "_current";
	
	public static final String USER_INFO = CollectionNames.USER_INFO;
	public static final String USER_INFO_PAGED = USER_INFO + PAGED;
	
	public static final String POSTING = CollectionNames.POSTING;
	public static final String POSTING_PAGED = POSTING + PAGED;
	public static final String POSTING_USER_PAGED = POSTING + "_" + USER + PAGED;
	
	public static final String COMMENT = CollectionNames.COMMENT;
	public static final String COMMENT_PAGED = COMMENT + PAGED;
	public static final String COMMENT_REPLY_PAGED = COMMENT + "_reply" + PAGED;
	public static final String COMMENT_USER_PAGED = COMMENT + "_" + USER + PAGED;
	
	public static final String TAG = CollectionNames.TAG;
	public static final String TAG_PAGED = TAG + PAGED;
	
	public static final String SUBSCRIPTION = CollectionNames.SUBSCRIPTION;
	
	//public static final String EVENT_PAGED = CollectionNames.EVENT + PAGED;
	
	public static final String RESTRICTED = CollectionNames.RESTRICTED;
	
	public static final String STATISTIC = CollectionNames.STATISTIC;
	
}
