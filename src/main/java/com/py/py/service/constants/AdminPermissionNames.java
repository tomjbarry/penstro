package com.py.py.service.constants;

public class AdminPermissionNames {
	
	// this permission is also required for access to the endpoint as a second check
	// it should be included with every admin role
	public static final String ADMIN = "ADMIN";
	
	// admin
	public static final String ADMIN_CHECK = "ADMIN_CHECK";
	
	// authentication
	public static final String ADMIN_USER_LOGOUT = "ADMIN_USER_LOGOUT";
	
	// statistics
	public static final String ADMIN_STATISTICS_CACHE_VIEW = "ADMIN_STATISTICS_CACHE_VIEW";
	
	// user
	public static final String ADMIN_EMAIL_CHANGE = "ADMIN_EMAIL_CHANGE";
	public static final String ADMIN_EMAIL_CHANGE_REQUEST = "ADMIN_EMAIL_CHANGE_REQUEST";
	public static final String ADMIN_PAYMENT_CHANGE_REQUEST = "ADMIN_PAYMENT_CHANGE_REQUEST";
	public static final String ADMIN_PASSWORD_RESET = "ADMIN_PASSWORD_RESET";
	public static final String ADMIN_USER_CURRENT_VIEW = "ADMIN_USER_CURRENT_VIEW";
	public static final String ADMIN_USER_UNLOCK = "ADMIN_USER_UNLOCK";
	public static final String ADMIN_USER_LOCK = "ADMIN_USER_LOCK";
	public static final String ADMIN_USER_UNDELETE = "ADMIN_USER_UNDELETE";
	public static final String ADMIN_USER_PROFILE_VIEW = "ADMIN_USER_PROFILE_VIEW";
	public static final String ADMIN_USER_PROFILE_CHANGE = "ADMIN_USER_PROFILE_CHANGE";
	public static final String ADMIN_USER_APPRECIATION_RESPONSE_CHANGE = 
			"ADMIN_USER_APPRECIATION_RESPONSE_CHANGE";
	public static final String ADMIN_USER_APPRECIATION_RESPONSE_VIEW = 
			"ADMIN_USER_APPRECIATION_RESPONSE_VIEW";
	public static final String ADMIN_USER_ROLES_CHANGE = "ADMIN_USER_ROLES_CHANGE";
	public static final String ADMIN_USER_PENDING_ACTIONS_CHANGE = "ADMIN_USER_PENDING_ACTIONS_CHANGE";
	public static final String ADMIN_USER_ROLES_VIEW = "ADMIN_USER_ROLES_VIEW";
	public static final String ADMIN_USER_LOGIN_ATTEMPTS_REMOVE = "ADMIN_USER_LOGIN_ATTEMPTS_REMOVE";
	public static final String ADMIN_USER_RENAME = "ADMIN_USER_RENAME";
	public static final String ADMIN_USER_PASSWORD_CHANGE = "ADMIN_USER_PASSWORD_CHANGE";
	
	// finance
	public static final String ADMIN_FINANCES_VIEW = "ADMIN_FINANCES_VIEW";
	public static final String ADMIN_FINANCES_ADD = "ADMIN_FINANCES_ADD";
	public static final String ADMIN_FINANCES_REMOVE = "ADMIN_FINANCES_REMOVE";
	
	// payment
	public static final String ADMIN_PAYMENT_CHECK = "ADMIN_PAYMENT_CHECK";
	public static final String ADMIN_PAYMENT_MARK = "ADMIN_PAYMENT_MARK";
	
	// notifications
	public static final String ADMIN_NOTIFICATIONS_VIEW = "ADMIN_NOTIFICATIONS_VIEW";
	
	// feed
	public static final String ADMIN_FEED_VIEW = "ADMIN_FEED_VIEW";
	
	// postings
	public static final String ADMIN_POSTINGS_SELF_VIEW = "ADMIN_POSTINGS_SELF_VIEW";
	public static final String ADMIN_POSTING_VIEW = "ADMIN_POSTING_VIEW";
	public static final String ADMIN_POSTING_REMOVE = "ADMIN_POSTING_REMOVE";
	public static final String ADMIN_POSTING_UNREMOVE = "ADMIN_POSTING_UNREMOVE";
	public static final String ADMIN_POSTING_FLAG = "ADMIN_POSTING_FLAG";
	public static final String ADMIN_POSTING_UNFLAG = "ADMIN_POSTING_UNFLAG";
	public static final String ADMIN_POSTING_WARNING = "ADMIN_POSTING_WARNING";
	public static final String ADMIN_POSTING_UNWARNING = "ADMIN_POSTING_UNWARNING";
	public static final String ADMIN_POSTING_ENABLE = "ADMIN_POSTING_ENABLE";
	public static final String ADMIN_POSTING_DISABLE = "ADMIN_POSTING_DISABLE";
	public static final String ADMIN_POSTING_TALLY_CHANGE = "ADMIN_POSTING_TALLY_CHANGE";
	public static final String ADMIN_POSTING_APPRECIATE = "ADMIN_POSTING_APPRECIATE";
	
	// comments
	public static final String ADMIN_COMMENTS_SELF_VIEW = "ADMIN_COMMENTS_SELF_VIEW";
	public static final String ADMIN_COMMENT_VIEW = "ADMIN_COMMENT_VIEW";
	public static final String ADMIN_COMMENT_REMOVE = "ADMIN_COMMENT_REMOVE";
	public static final String ADMIN_COMMENT_UNREMOVE = "ADMIN_COMMENT_UNREMOVE";
	public static final String ADMIN_COMMENT_FLAG = "ADMIN_COMMENT_FLAG";
	public static final String ADMIN_COMMENT_UNFLAG = "ADMIN_COMMENT_UNFLAG";
	public static final String ADMIN_COMMENT_WARNING = "ADMIN_COMMENT_WARNING";
	public static final String ADMIN_COMMENT_UNWARNING = "ADMIN_COMMENT_UNWARNING";
	public static final String ADMIN_COMMENT_ENABLE = "ADMIN_COMMENT_ENABLE";
	public static final String ADMIN_COMMENT_DISABLE = "ADMIN_COMMENT_DISABLE";
	public static final String ADMIN_COMMENT_TALLY_CHANGE = "ADMIN_COMMENT_TALLY_CHANGE";
	public static final String ADMIN_COMMENT_APPRECIATE = "ADMIN_COMMENT_APPRECIATE";
	
	// tag
	public static final String ADMIN_TAG_LOCK = "ADMIN_TAG_LOCK";
	public static final String ADMIN_TAG_UNLOCK = "ADMIN_TAG_UNLOCK";
	
	// subscription
	public static final String ADMIN_SUBSCRIPTION_VIEW = "ADMIN_SUBSCRIPTION_VIEW";
	
	// following
	public static final String ADMIN_FOLLOWEES_VIEW = "ADMIN_FOLLOWEES_VIEW";
	public static final String ADMIN_FOLLOWERS_VIEW = "ADMIN_FOLLOWERS_VIEW";
	public static final String ADMIN_FOLLOWER_VIEW = "ADMIN_FOLLOWER_VIEW";
	public static final String ADMIN_FOLLOWEE_VIEW = "ADMIN_FOLLOWEE_VIEW";
	public static final String ADMIN_FOLLOWEE_SUBMIT = "ADMIN_FOLLOWEE_SUBMIT";
	public static final String ADMIN_FOLLOWEE_REMOVE = "ADMIN_FOLLOWEE_REMOVE";
	
	// blocking
	public static final String ADMIN_BLOCKEDS_VIEW = "ADMIN_BLOCKEDS_VIEW";
	public static final String ADMIN_BLOCKED_VIEW = "ADMIN_BLOCKED_VIEW";
	public static final String ADMIN_BLOCKED_SUBMIT = "ADMIN_BLOCKED_SUBMIT";
	public static final String ADMIN_BLOCKED_REMOVE = "ADMIN_BLOCKED_REMOVE";
	
	// backing
	public static final String ADMIN_BACKINGS_VIEW = "ADMIN_BACKINGS_VIEW";
	public static final String ADMIN_BACKING_VIEW = "ADMIN_BACKING_VIEW";
	public static final String ADMIN_BACKINGS_OUTSTANDING_VIEW = 
			"ADMIN_BACKINGS_OUTSTANDING_VIEW";
	public static final String ADMIN_BACKING_OUTSTANDING_VIEW = 
			"ADMIN_BACKING_OUTSTANDING_VIEW";
	public static final String ADMIN_BACKING_WITHDRAW = "ADMIN_BACKING_WITHDRAW";
	public static final String ADMIN_BACKING_END = "ADMIN_BACKING_END";
	
	// offer
	public static final String ADMIN_OFFER_VIEW = "ADMIN_OFFER_VIEW";
	public static final String ADMIN_OFFERS_VIEW = "ADMIN_OFFERS_VIEW";
	public static final String ADMIN_OFFER_OUTSTANDING_VIEW = "ADMIN_OFFER_OUTSTANDING_VIEW";
	public static final String ADMIN_OFFERS_OUTSTANDING_VIEW = "ADMIN_OFFERS_OUTSTANDING_VIEW";
	public static final String ADMIN_OFFER_OUTSTANDING_EMAIL_VIEW = "ADMIN_OFFER_OUTSTANDING_EMAIL_VIEW";
	public static final String ADMIN_OFFERS_OUTSTANDING_EMAIL_VIEW = "ADMIN_OFFERS_OUTSTANDING_EMAIL_VIEW";
	public static final String ADMIN_OFFER_EMAIL_WITHDRAW = "ADMIN_OFFER_EMAIL_WITHDRAW";
	public static final String ADMIN_OFFER_WITHDRAW = "ADMIN_OFFER_WITHDRAW";
	
	// settings
	public static final String ADMIN_SETTINGS_VIEW = "ADMIN_SETTINGS_VIEW";
	public static final String ADMIN_SETTINGS_RESET = "ADMIN_SETTINGS_RESET";
	
	// feedback
	public static final String ADMIN_FEEDBACK_VIEW = "ADMIN_FEEDBACK_VIEW";
	public static final String ADMIN_FEEDBACKS_VIEW = "ADMIN_FEEDBACKS_VIEW";
	public static final String ADMIN_FEEDBACK_CHANGE = "ADMIN_FEEDBACK_CHANGE";
	
	// restricted
	public static final String ADMIN_RESTRICTED_VIEW = "ADMIN_RESTRICTED_VIEW";
	public static final String ADMIN_RESTRICTEDS_VIEW = "ADMIN_RESTRICTEDS_VIEW";
	public static final String ADMIN_RESTRICTED_ADD = "ADMIN_RESTRICTED_ADD";
	public static final String ADMIN_RESTRICTED_REMOVE = "ADMIN_RESTRICTED_REMOVE";
	
	// flag data
	public static final String ADMIN_FLAG_DATAS_VIEW = "ADMIN_FLAG_DATAS_VIEW";
	public static final String ADMIN_FLAG_DATA_REMOVE = "ADMIN_FLAG_DATA_REMOVE";
	
	// actions
	public static final String ADMIN_ACTIONS_VIEW = "ADMIN_ACTIONS_VIEW";
}
