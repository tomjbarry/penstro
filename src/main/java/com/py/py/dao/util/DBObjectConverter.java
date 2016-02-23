package com.py.py.dao.util;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.subdomain.AppreciationDate;
import com.py.py.domain.subdomain.AuthenticationInformation;
import com.py.py.domain.subdomain.BinaryUserId;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.ImageLink;
import com.py.py.domain.subdomain.TimeSumAggregate;

public class DBObjectConverter {

	public static DBObject convertCachedUsername(CachedUsername cu) throws DaoException {
		CheckUtil.nullCheck(cu);
		DBObject obj = new BasicDBObject();
		obj.put(CachedUsername.USERNAME, cu.getUsername());
		obj.put(CachedUsername.OID, cu.getId());
		obj.put(CachedUsername.EXISTS, cu.isExists());
		return obj;
	}

	public static DBObject convertAppreciationDate(AppreciationDate ad) 
			throws DaoException {
		CheckUtil.nullCheck(ad);
		DBObject obj = new BasicDBObject();
		obj.put(AppreciationDate.CACHED_USERNAME, 
				convertCachedUsername(ad.getCachedUsername()));
		obj.put(AppreciationDate.DATE, ad.getDate());
		return obj;
	}

	public static DBObject convertTimeSumAggregate(long amount) 
			throws DaoException {
		DBObject obj = new BasicDBObject();
		obj.put(TimeSumAggregate.HOUR, amount);
		obj.put(TimeSumAggregate.DAY, amount);
		obj.put(TimeSumAggregate.MONTH, amount);
		obj.put(TimeSumAggregate.YEAR, amount);
		return obj;
	}
	
	public static DBObject convertBinaryUserId(BinaryUserId buId) throws DaoException {
		CheckUtil.nullCheck(buId);
		DBObject obj = new BasicDBObject();
		obj.put(BinaryUserId.FIRST, buId.getFirst());
		obj.put(BinaryUserId.SECOND, buId.getSecond());
		return obj;
	}
	
	public static DBObject convertAuthenticationInformation(AuthenticationInformation aI) throws DaoException {
		CheckUtil.nullCheck(aI);
		DBObject obj = new BasicDBObject();
		obj.put(AuthenticationInformation.TOKEN, aI.getToken());
		obj.put(AuthenticationInformation.EXPIRY, aI.getExpiry());
		obj.put(AuthenticationInformation.INACTIVITY, aI.getInactivity());
		return obj;
	}
	
	public static DBObject convertImageLink(ImageLink iL) throws DaoException {
		CheckUtil.nullCheck(iL);
		DBObject obj = new BasicDBObject();
		obj.put(ImageLink.LINK, iL.getLink());
		obj.put(ImageLink.WIDTH, iL.getWidth());
		obj.put(ImageLink.HEIGHT, iL.getHeight());
		return obj;
	}
}
