package com.py.py.dao.constants;



public class CacheKeys {

	protected static final String DIV = "+':'+";
	protected static final String LANGUAGE = "#language";
	protected static final String TIME = "#time?.toString()";
	protected static final String SORT = "#sort?.toString()";
	protected static final String WARNING = "#warning?.toString()";
	protected static final String PAGEABLE = "#pageable.getPageNumber()" 
			+DIV+ "#pageable.getPageSize()";
	protected static final String FILTER = "#filter?.getSort()?.toString()"
			+DIV+ "#filter?.getTime()?.toString()"
			+DIV+ "#filter?.getWarning()"
			+DIV+ "#filter?.getTags()?.toString()" 
			+DIV+ "#filter?.getExcludeTags()?.toString()";
	/*
	// User
	public static final String USER_ID_KEY = "#id";
	public static final String USER_USERNAME_KEY = "#username";
	public static final String USER_EMAIL_KEY = "#email";
	public static final String USER_CURRENT_USER = "#user.getId()";
	public static final String USER_CURRENT_ID = "#id";
	
	// UserInfo
	public static final String USER_INFO_ID_KEY = "#id";
	public static final String USER_INFO_ID_STRING_KEY = "new org.bson.types.ObjectId(#id)";
	// paged
	public static final String USER_INFO_PAGED = LANGUAGE 
			+DIV+ TIME 
			+DIV+ PAGEABLE;
	
	// Escrow
	
	// Posting
	public static final String POSTING_ID = "#id";
	// paged
	public static final String POSTING_PAGED = "#author?.toStringMongod()"
			+DIV+ "#beneficiary?.toStringMongod()"
			+DIV+ LANGUAGE 
			+DIV+ PAGEABLE 
			+DIV+ FILTER;
	
	// Comment
	public static final String COMMENT_ID = "#id";
	// paged
	public static final String COMMENT_PAGED = "#author?.toStringMongod()"
			+DIV+ "#beneficiary?.toStringMongod()" 
			+DIV+ "#parentId?.toStringMongod()"
			+DIV+ "#baseId?.toStringMongod()" 
			+DIV+ "#baseString"
			+DIV+ "#types?.toString()" 
			+DIV+ "#noSubComments"
			+DIV+ LANGUAGE 
			+DIV+ PAGEABLE 
			+DIV+ FILTER;
	
	// Tag
	public static final String TAG_NAME_LANGUAGE = "#name"
			+DIV+ "#language";
	public static final String TAG_ID = "#id.getName()"
			+DIV+ "#id.getLanguage()";
	// paged
	public static final String TAG_PAGED = LANGUAGE 
			+DIV+ TIME 
			+DIV+ PAGEABLE;
	
	// Message
	
	// Subscription
	public static final String SUBSCRIPTION_ID = "#id";
	
	// Event
	// paged
	public static final String EVENT_PAGED = "#author?.toStringMongod()"
			+DIV+ "#target?.toStringMongod()" 
			+DIV+ "#types?.toString()" 
			+DIV+ PAGEABLE;
	
	// Restricted
	public static final String RESTRICTED_WORD_TYPE = "#word" 
			+DIV+ "#type?.toString()";
	
	// Admin
	
	// Deal
	
	// Payment
	
	// Task
	
	// Feedback
	
	// Value Aggregation
	*/
}
