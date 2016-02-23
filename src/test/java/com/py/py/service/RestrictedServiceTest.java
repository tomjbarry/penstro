package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.py.py.dao.RestrictedDao;
import com.py.py.domain.Restricted;
import com.py.py.domain.subdomain.RestrictedWord;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;

public class RestrictedServiceTest extends BaseServiceTest {

	@Autowired
	@Qualifier("restrictedService")
	private RestrictedService restrictedService;
	
	@Autowired
	protected RestrictedDao restrictedDao;

	private String validWord = validName;
	private String invalidWord = "";
	
	@Before
	public void setUp() {
		reset(restrictedDao);
	}

	protected Restricted createValidRestricted() {
		Restricted r = new Restricted();
		RestrictedWord rw = new RestrictedWord();
		rw.setType(RESTRICTED_TYPE.USERNAME);
		rw.setWord(validName);
		r.setId(rw);
		return r;
	}
	
	@Test(expected = BadParameterException.class)
	public void getRestrictedNull1() throws Exception {
		restrictedService.getRestricted(null, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void getRestrictedNull2() throws Exception {
		restrictedService.getRestricted(validWord, null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getRestrictedNotFound() throws Exception {
		when(restrictedDao.getRestricted(anyString(), 
				any(RESTRICTED_TYPE.class))).thenReturn(null);
		
		restrictedService.getRestricted(validWord, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test
	public void getRestricted() throws Exception {
		when(restrictedDao.getRestricted(anyString(), 
				any(RESTRICTED_TYPE.class))).thenReturn(createValidRestricted());
		
		restrictedService.getRestricted(validWord, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void isRestrictedNull1() throws Exception {
		restrictedService.isRestricted(null, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void isRestrictedNull2() throws Exception {
		restrictedService.isRestricted(validWord, null);
	}
	
	@Test
	public void isRestricted() throws Exception {
		when(restrictedDao.getRestricted(anyString(), 
				any(RESTRICTED_TYPE.class)))
				.thenReturn(null).thenReturn(createValidRestricted());
		Assert.assertFalse(restrictedService.isRestricted(
				validWord, RESTRICTED_TYPE.USERNAME));
		Assert.assertTrue(restrictedService.isRestricted(
				validWord, RESTRICTED_TYPE.USERNAME));
	}

	@Test(expected = BadParameterException.class)
	public void addRestrictedNull1() throws Exception {
		restrictedService.addRestricted(null, RESTRICTED_TYPE.USERNAME);
	}

	@Test(expected = BadParameterException.class)
	public void addRestrictedNull2() throws Exception {
		restrictedService.addRestricted(validWord, null);
	}

	@Test(expected = BadParameterException.class)
	public void addRestrictedInvalid() throws Exception {
		restrictedService.addRestricted(invalidWord, RESTRICTED_TYPE.USERNAME);
	}

	@Test
	public void addRestricted() throws Exception {
		restrictedService.addRestricted(validWord, RESTRICTED_TYPE.USERNAME);
	}

	@Test(expected = BadParameterException.class)
	public void removeRestrictedNull1() throws Exception {
		restrictedService.removeRestricted(null, RESTRICTED_TYPE.USERNAME);
	}

	@Test(expected = BadParameterException.class)
	public void removeRestrictedNull2() throws Exception {
		restrictedService.removeRestricted(validWord, null);
	}

	@Test(expected = BadParameterException.class)
	public void removeRestrictedInvalid() throws Exception {
		restrictedService.removeRestricted(invalidWord, RESTRICTED_TYPE.USERNAME);
	}

	@Test
	public void removeRestricted() throws Exception {
		restrictedService.removeRestricted(validWord, RESTRICTED_TYPE.USERNAME);
	}
}
