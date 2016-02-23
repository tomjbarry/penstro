package com.py.py.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.RestrictedDao;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Restricted;
import com.py.py.domain.subdomain.RestrictedWord;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.RestrictedService;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;

public class RestrictedServiceImpl implements RestrictedService {

	protected static final PyLogger logger = PyLogger.getLogger(RestrictedServiceImpl.class);

	@Autowired
	protected RestrictedDao restrictedDao;

	protected boolean useDatabase = false;
	protected int maxSize = 500;
	protected Set<String> usernames = new HashSet<String>();
	protected Set<String> passwords = new HashSet<String>();
	protected Set<String> emails = new HashSet<String>();
	
	protected void populateSet(Set<String> set, int batchSize, RESTRICTED_TYPE type) throws ServiceException {
		int page = 0;
		int total = 0;
		if(batchSize <= 0) {
			return;
		}
		Pageable pageable = new PageRequest(page, batchSize);
		Page<Restricted> result = null;
		try {
			while(total < maxSize) {
				pageable = new PageRequest(page, batchSize);
				result = restrictedDao.findRestricteds(type, pageable);
				if(result == null) {
					throw new NotFoundException(type.toString());
				}
				for(Restricted r : result.getContent()) {
					RestrictedWord rw = r.getId();
					if(rw != null && rw.getWord() != null) {
						set.add(rw.getWord());
						total++;
					}
				}
				
				if(!result.hasNext()) {
					break;
				}
				page++;
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void populateRestricted() throws ServiceException {
		int size = ServiceValues.RESTRICTED_BATCH_SIZE;
		if(maxSize < size) {
			size = maxSize;
		}
		if(!useDatabase) {
			populateSet(usernames, size, RESTRICTED_TYPE.USERNAME);
			populateSet(passwords, size, RESTRICTED_TYPE.PASSWORD);
			populateSet(emails, size, RESTRICTED_TYPE.EMAIL);
		}
	}
	
	@Override
	public RestrictedDTO getRestrictedDTO(String word, RESTRICTED_TYPE type) 
			throws ServiceException {
		return Mapper.mapRestrictedDTO(getRestricted(word, type));
	}
	
	protected Restricted getRestrictedFromDatabase(String word, RESTRICTED_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(word, type);
		
		String correctWord = ServiceUtils.getWord(word);
		
		try {
			Restricted restricted = restrictedDao.getRestricted(correctWord, type);
			if(restricted == null) {
				throw new NotFoundException(correctWord);
			}
			return restricted;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected Restricted getRestrictedFromSet(String word, RESTRICTED_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(word, type);
		
		String correctWord = ServiceUtils.getWord(word);
		
		boolean contained = false;
		if(RESTRICTED_TYPE.USERNAME == type) {
			contained = usernames.contains(correctWord);
		} else if(RESTRICTED_TYPE.PASSWORD == type) {
			contained = passwords.contains(correctWord);
		} else if(RESTRICTED_TYPE.EMAIL == type) {
			contained = emails.contains(correctWord);
		}
		if(!contained) {
			throw new NotFoundException(correctWord);
		}
		RestrictedWord restrictedWord = new RestrictedWord();
		restrictedWord.setWord(correctWord);
		restrictedWord.setType(type);
		Restricted result = new Restricted();
		result.setId(restrictedWord);
		result.setCreated(null);
		
		return result;
	}
	
	@Override
	public Restricted getRestricted(String word, RESTRICTED_TYPE type) 
			throws ServiceException {
		if(useDatabase) {
			return getRestrictedFromDatabase(word, type);
		} else {
			return getRestrictedFromSet(word, type);
		}
	}
	
	@Override
	public boolean isRestricted(String word, RESTRICTED_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(word, type);
		
		try {
			Restricted restricted = getRestricted(word, type);
			if(restricted != null) {
				return true;
			}
			return false;
		} catch(NotFoundException nfe) {
			return false;
		}
	}
	
	@Override
	public void addRestricted(String word, RESTRICTED_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(word, type);
		
		String correctWord = ServiceUtils.getWord(word);
		
		try {
			restrictedDao.add(word, type);
		} catch(CollisionException ce) {
			throw new ExistsException(correctWord);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeRestricted(String word, RESTRICTED_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(word, type);
		
		String correctWord = ServiceUtils.getWord(word);
		
		try {
			restrictedDao.remove(correctWord, type);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void addStartupWords(List<String> wordList, RESTRICTED_TYPE type) throws ServiceException {
		Set<String> set = null;
		if(RESTRICTED_TYPE.USERNAME == type) {
			set = usernames;
		} else if(RESTRICTED_TYPE.PASSWORD == type) {
			set = passwords;
		} else if(RESTRICTED_TYPE.EMAIL == type) {
			set = emails;
		}
		
		for(String word : wordList) {
			try {
				if(useDatabase) {
					addRestricted(word, type);
				} else {
					if(set != null) {
						set.add(word);
					}
				}
			} catch(ExistsException ee) {
				logger.debug("Restricted word already exists: {" + word + "} for type: " + type.toString());
			} catch(ServiceException se) {
				logger.warn("Error creating word: {" + word + "} for type: " + type.toString());
			}
		}
	}
	
	public boolean isUseDatabase() {
		return useDatabase;
	}

	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}
	
}
