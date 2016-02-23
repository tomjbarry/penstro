package com.py.py.service;

import java.util.List;

import com.py.py.domain.Restricted;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.exception.ServiceException;

public interface RestrictedService {

	Restricted getRestricted(String word, RESTRICTED_TYPE type)
			throws ServiceException;

	boolean isRestricted(String word, RESTRICTED_TYPE type)
			throws ServiceException;

	void addRestricted(String word, RESTRICTED_TYPE type)
			throws ServiceException;

	void removeRestricted(String word, RESTRICTED_TYPE type)
			throws ServiceException;

	RestrictedDTO getRestrictedDTO(String word, RESTRICTED_TYPE type)
			throws ServiceException;

	void addStartupWords(List<String> wordList, RESTRICTED_TYPE type)
			throws ServiceException;

}
