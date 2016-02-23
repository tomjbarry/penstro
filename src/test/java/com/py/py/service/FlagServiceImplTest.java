package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.FlagDataDao;
import com.py.py.domain.FlagData;
import com.py.py.domain.subdomain.FlagInfo;
import com.py.py.dto.out.FlagDataDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.service.exception.BadParameterException;

public class FlagServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("flagService")
	private FlagService flagService;
	
	@Autowired
	private FlagDataDao flagDataDao;

	private List<FlagData> validFlagDatas = new ArrayList<FlagData>();
	private List<FlagData> invalidFlagDatas = new ArrayList<FlagData>();
	private long validWeight = 5l;
	private String validTarget = "validTarget";
	
	protected FlagData createFlagData() {
		FlagData data = new FlagData();
		data.setId(new FlagInfo(new ObjectId(), FLAG_TYPE.POSTING));
		data.setValue(randomLong());
		data.setTotal(randomLong());
		data.setReasons(new HashMap<String, Long>());
		return data;
	}
	
	@Before
	public void setUp() {
		validFlagDatas.add(createFlagData());
		validFlagDatas.add(createFlagData());
		validFlagDatas.add(createFlagData());
		invalidFlagDatas.addAll(validFlagDatas);
		invalidFlagDatas.add(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getFlagDataDTOsNull2() throws Exception {
		flagService.getFlagDataDTOs(FLAG_TYPE.POSTING, null);
	}
	
	@Test
	public void getFlagDataDTOsInvalidList() throws Exception {
		when(flagDataDao.findSorted(any(FLAG_TYPE.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FlagData>(invalidFlagDatas));
		Page<FlagDataDTO> result = flagService.getFlagDataDTOs(null, constructPageable());
		Assert.assertEquals(result.getContent().size(), validFlagDatas.size());
		result = flagService.getFlagDataDTOs(FLAG_TYPE.POSTING, constructPageable());
		Assert.assertEquals(result.getContent().size(), validFlagDatas.size());
	}
	
	@Test
	public void getFlagDataDTOs() throws Exception {
		when(flagDataDao.findSorted(any(FLAG_TYPE.class), any(Pageable.class)))
			.thenReturn(new PageImpl<FlagData>(validFlagDatas));
		Page<FlagDataDTO> result = flagService.getFlagDataDTOs(null, constructPageable());
		Assert.assertEquals(result.getContent().size(), validFlagDatas.size());
		result = flagService.getFlagDataDTOs(FLAG_TYPE.POSTING, constructPageable());
		Assert.assertEquals(result.getContent().size(), validFlagDatas.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void addDataNull1() throws Exception {
		flagService.addData(null, FLAG_TYPE.POSTING, validTarget, validWeight, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void addDataNull2() throws Exception {
		flagService.addData(new ObjectId(), null, validTarget, validWeight, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void addDataNull3() throws Exception {
		flagService.addData(new ObjectId(), FLAG_TYPE.POSTING, null, validWeight, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void addDataNull4() throws Exception {
		flagService.addData(new ObjectId(), FLAG_TYPE.POSTING, validTarget, validWeight, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void addDataInvalid3() throws Exception {
		flagService.addData(new ObjectId(), FLAG_TYPE.POSTING, validTarget, 0l, FLAG_REASON.ILLICIT);
	}
	
	@Test
	public void addData() throws Exception {
		flagService.addData(new ObjectId(), FLAG_TYPE.POSTING, validTarget, validWeight, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeOneNull1() throws Exception {
		flagService.removeOne(null, FLAG_TYPE.POSTING);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeOneNull2() throws Exception {
		flagService.removeOne(new ObjectId(), null);
	}
	
	@Test
	public void removeOne() throws Exception {
		flagService.removeOne(new ObjectId(), FLAG_TYPE.POSTING);
	}
	
	@Test
	public void decrementAll() throws Exception {
		flagService.decrementAll();
	}
	
	@Test
	public void removeOld() throws Exception {
		flagService.removeOld();
	}
}
