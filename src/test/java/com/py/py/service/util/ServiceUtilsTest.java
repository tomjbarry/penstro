package com.py.py.service.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;

import com.py.py.BaseTest;
import com.py.py.constants.CurrencyNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.service.exception.BadParameterException;
import com.py.py.util.PyUtils;

public class ServiceUtilsTest extends BaseTest {
	
	// no setup, all methods are static
	
	private String invalidUsername = "~@9cinvalid39v9#89dc9)  d93jc";
	private String validUsername = "TeSt";
	private String invalidEmail = "9c3nc0id93";
	private String validEmail = "TestS@test.com";
	
	@Test(expected = BadParameterException.class)
	public void getNameStringNull() throws Exception {
		ServiceUtils.getName((String)null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getNameStringInvalid() throws Exception {
		ServiceUtils.getName(invalidUsername);
	}
	
	@Test
	public void getNameString() throws Exception {
		Assert.assertEquals(ServiceUtils.getName(validUsername), validUsername);
	}
	
	@Test
	public void isUsernameDeleted() throws Exception {
		Assert.assertEquals(ServiceUtils.isUsernameDeleted(null), true);
		Assert.assertEquals(ServiceUtils.isUsernameDeleted(
				new CachedUsername(null, validUsername)), true);
		Assert.assertEquals(ServiceUtils.isUsernameDeleted(
				new CachedUsername(new ObjectId(), validUsername)), false);
	}
	
	@Test(expected = BadParameterException.class)
	public void getIdNameStringNull() throws Exception {
		ServiceUtils.getIdName((String)null);
	}

	@Test(expected = BadParameterException.class)
	public void getIdNameStringInvalid() throws Exception {
		ServiceUtils.getIdName(invalidUsername);
	}
	
	@Test
	public void getIdNameString() throws Exception {
		Assert.assertEquals(ServiceUtils.getIdName(validUsername), 
				validUsername.toLowerCase());
	}
	
	@Test(expected = BadParameterException.class)
	public void getEmailNull() throws Exception {
		ServiceUtils.getEmail(null);
	}

	@Test(expected = BadParameterException.class)
	public void getEmailInvalid() throws Exception {
		ServiceUtils.getEmail(invalidEmail);
	}

	@Test
	public void getEmail() throws Exception {
		Assert.assertEquals(ServiceUtils.getEmail(validEmail), validEmail);
	}

	@Test(expected = BadParameterException.class)
	public void getCurrencyNull() throws Exception {
		ServiceUtils.getCurrency(null);
	}

	@Test(expected = BadParameterException.class)
	public void getCurrencyInvalid() throws Exception {
		ServiceUtils.getCurrency("dvjeotgin");
	}

	@Test
	public void getCurrency() throws Exception {
		Map<String, String> currencyMap = PyUtils.constructConstantMap(CurrencyNames.class);
		for(String currency : currencyMap.values()) {
			Assert.assertEquals(ServiceUtils.getCurrency(currency), currency);
		}
	}

	@Test(expected = BadParameterException.class)
	public void getContentNull() throws Exception {
		ServiceUtils.getContent(null);
	}

	@Test(expected = BadParameterException.class)
	public void getContentInvalid() throws Exception {
		ServiceUtils.getContent("");
	}

	@Test
	public void getContent() throws Exception {
		String validContent = "TedeEDsting29cx)12CX823<>!:29c0#+";
		Assert.assertEquals(ServiceUtils.getContent(validContent), validContent);
	}

	@Test(expected = BadParameterException.class)
	public void getTitleNull() throws Exception {
		ServiceUtils.getTitle(null);
	}

	@Test(expected = BadParameterException.class)
	public void getTitleInvalid() throws Exception {
		ServiceUtils.getTitle("");
	}

	@Test
	public void getTitle() throws Exception {
		String validTitle = "TedeEDsting29cx)12CX823<>!:29c0#+";
		Assert.assertEquals(ServiceUtils.getTitle(validTitle), validTitle);
	}

	@Test(expected = BadParameterException.class)
	public void getMessageNull() throws Exception {
		ServiceUtils.getMessage(null);
	}

	@Test(expected = BadParameterException.class)
	public void getMessageInvalid() throws Exception {
		ServiceUtils.getMessage("");
	}

	@Test
	public void getMessage() throws Exception {
		String validMessage = "TedeEDsting29cx)12CX823<>!:29c0#+";
		Assert.assertEquals(ServiceUtils.getMessage(validMessage), validMessage);
	}

	@Test(expected = BadParameterException.class)
	public void getTagNull() throws Exception {
		ServiceUtils.getTag(null);
	}

	@Test(expected = BadParameterException.class)
	public void getTagInvalid() throws Exception {
		ServiceUtils.getTag("3c93jnd02-1f9dch1-3d9@#_39");
	}

	@Test
	public void getTag() throws Exception {
		String validTag = "TeSting_123";
		Assert.assertEquals(ServiceUtils.getTag(validTag), validTag.toLowerCase());
	}
	
	@Test
	public void getTagMap() throws Exception {
		List<String> tags = Arrays.asList("test","test2","tag3");
		long testValue = (new Random()).nextInt(10000);
		
		Map<String, Long> tagMap = ServiceUtils.getTagMap(null, testValue);
		Assert.assertNotNull(tagMap);
		Assert.assertEquals(tagMap.size(), 0);
		
		tagMap = ServiceUtils.getTagMap(tags, testValue);
		for(Map.Entry<String, Long> entry : tagMap.entrySet()) {
			if(!tags.contains(entry.getKey())) {
				Assert.fail();
			}
			Assert.assertEquals(entry.getValue().longValue(), testValue);
		}
	}
	
	@Test
	public void getLimitedTags() throws Exception {
		Random random = new Random();
		int length = 25;
		int limit = 10;
		int more = 2 * length;
		
		Map<String, Long> tags = new HashMap<String, Long>();
		for(int i = 0; i < length; i++) {
			tags.put("tag" + i, random.nextLong());
		}
		
		Map<String, Long> result = ServiceUtils.getLimitedTags(null, length);
		Assert.assertNotNull(result);
		Assert.assertEquals(result.size(), 0);
		
		result = ServiceUtils.getLimitedTags(tags, length);
		Assert.assertEquals(result.size(), length);

		result = ServiceUtils.getLimitedTags(tags, more);
		Assert.assertEquals(result.size(), length);
		
		result = ServiceUtils.getLimitedTags(tags, limit);
		Assert.assertEquals(result.size(), limit);
	}
}
