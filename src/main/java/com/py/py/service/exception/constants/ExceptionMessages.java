package com.py.py.service.exception.constants;

public class ExceptionMessages {
	public static final String GENERIC = "A service exception has occurred!";
	public static final String BADPARAMETER = "The given parameter was invalid!";
	public static final String NOTFOUND = "The object with an id of { %s } was not found!";
	public static final String DELETED = "The object with an id of { %s } was deleted!";
	public static final String EXISTS = "The object with an id of { %s } already exists!";
	public static final String AUTHENTICATION = "An exception occurred during authentication!";
	public static final String LOCKED = "Resource is locked!";
	public static final String PAYMENT = "Payment exception!";
	public static final String PAYMENT_TARGET = "Payment target exception!";
	public static final String EXTERNAL_SERVICE = "Exception while attempting to use external service!";
	public static final String FEATURE_DISABLED = "Feature is disabled!";
	public static final String FINANCE = "Transaction exception!";
	public static final String LIMIT = "The value was not within accepted limits!";
	public static final String ACTION_NOT_ALLOWED = "The user could not perform this action!";
	public static final String OBJECT_LOCKED = "The action upon this object has been permanently disabled!";
	public static final String BLOCKED = "The action was unable to be completed because this user is blocked!";
	public static final String TAG_COUNT = "The tag with an id of { %s } already could not be added!";
	public static final String TAG_LOCKED = "The tag with an id of { %s } is locked!";
	public static final String RESTRICTED = "Restricted exception!";
}
