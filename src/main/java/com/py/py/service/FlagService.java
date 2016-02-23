package com.py.py.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dto.out.FlagDataDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.service.exception.ServiceException;

public interface FlagService {

	Page<FlagDataDTO> getFlagDataDTOs(FLAG_TYPE type, Pageable pageable)
			throws ServiceException;

	void removeOne(ObjectId id, FLAG_TYPE type) throws ServiceException;

	void decrementAll() throws ServiceException;

	void removeOld() throws ServiceException;

	void addData(ObjectId id, FLAG_TYPE type, String target, long weight,
			FLAG_REASON reason) throws ServiceException;

}
