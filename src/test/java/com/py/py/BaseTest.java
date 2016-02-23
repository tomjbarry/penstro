package com.py.py;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.py.py.service.constants.ProfileNames;
import com.py.py.service.exception.BadParameterException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({
	"/springTest/dao-context.xml",
	"/springTest/service-context.xml",
	"/spring/service-context.xml"})
@ActiveProfiles({ProfileNames.ADMIN, ProfileNames.DEVELOPMENT})
public class BaseTest {
	
	
	@Test
	public void test() {
		// nothing
	}
	
	protected <T> List<T> addNullToList(List<T> list) {
		List<T> ret = new ArrayList<T>();
		ret.addAll(list);
		ret.add((T)null);
		return ret;
	}
	
	protected Pageable constructPageable() {
		return new PageRequest(0, 100);
	}
	
	protected boolean randomBoolean() {
		Random random = new Random();
		return random.nextBoolean();
	}
	
	protected long randomLong() {
		Random random = new Random();
		return random.nextLong();
	}
	
	protected void nullTest(Method method, Object instance, int[] nullCheckPositions, 
			Object... args) {
		List<Object> arguments = Arrays.asList(args);
		List<Integer> positions = new ArrayList<Integer>();
		for(int in : nullCheckPositions) {
			positions.add(in);
		}
		for(int i = 0; i < arguments.size(); i++) {
			boolean check = false;
			if(positions.contains(i)) {
				check = true;
			}
			Object argument = arguments.get(i);
			arguments.set(i, null);
			try {
				method.invoke(instance, arguments);
				if(check) {
					Assert.fail();
				}
			} catch (IllegalAccessException e) {
				Assert.fail();
			} catch (IllegalArgumentException e) {
				Assert.fail();
			} catch (InvocationTargetException e) {
				Throwable cause = e.getCause();
				if(cause instanceof BadParameterException) {
					if(!check) {
						Assert.fail();
					}
				} else {
					Assert.fail();
				}
			}
			arguments.set(i, argument);
		}
	}
}
