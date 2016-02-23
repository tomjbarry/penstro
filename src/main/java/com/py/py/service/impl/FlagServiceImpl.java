package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.FlagDataDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.FlagData;
import com.py.py.dto.out.FlagDataDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.service.FlagService;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.util.PyLogger;

public class FlagServiceImpl implements FlagService {

	protected static final PyLogger logger = PyLogger.getLogger(FlagServiceImpl.class);
	
	@Autowired
	protected FlagDataDao flagDao;
	
	@Override
	public Page<FlagDataDTO> getFlagDataDTOs(FLAG_TYPE type, Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		List<FlagDataDTO> dtolist = ModelFactory.<FlagDataDTO>constructList();
		Page<FlagData> page = new PageImpl<FlagData>(new ArrayList<FlagData>(), pageable, 0);
		
		try {
			page = flagDao.findSorted(type, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(FlagData fd : page.getContent()) {
			try {
				dtolist.add(Mapper.mapFlagDataDTO(fd));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of flag data!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of flag data!", e);
			}
		}
		
		return new PageImpl<FlagDataDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public void addData(ObjectId id, FLAG_TYPE type, String target, long weight, FLAG_REASON reason) throws ServiceException {
		ArgCheck.nullCheck(id, type, target, reason);
		
		if(weight <= 0l) {
			throw new BadParameterException();
		}
		
		try {
			flagDao.addData(id, type, target, weight, reason);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeOne(ObjectId id, FLAG_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(id, type);
		
		try {
			flagDao.remove(id, type);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void decrementAll() throws ServiceException {
		try {
			flagDao.remove(ServiceValues.FLAG_DATA_DECREMENT);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeOld() throws ServiceException {
		try {
			flagDao.remove(ServiceValues.FLAG_DATA_MINIMUM);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
}
