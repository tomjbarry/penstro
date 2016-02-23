package com.py.py.service.util;

import org.bson.types.ObjectId;
import org.junit.Test;

import com.py.py.BaseTest;
import com.py.py.service.exception.BadParameterException;

public class ArgCheckTest extends BaseTest {
	
	@Test(expected = BadParameterException.class)
	public void nullCheckNull() throws Exception {
		ArgCheck.nullCheck((Object[])null);
	}
	
	@Test
	public void nullCheck() throws Exception {
		ArgCheck.nullCheck("Not null!");
	}
	
	@Test(expected = BadParameterException.class)
	public void objectIdCheckNull() throws Exception {
		ArgCheck.objectIdCheck((String)null);
	}
	
	@Test(expected = BadParameterException.class)
	public void objectIdCheckInvalid() throws Exception {
		ArgCheck.objectIdCheck("Not a valid object id string");
	}
	
	@Test
	public void objectIdCheck() throws Exception {
		ArgCheck.objectIdCheck((new ObjectId()).toHexString());
	}
	
	
}
