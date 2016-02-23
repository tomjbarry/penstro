package com.py.py.service;

import com.py.py.dto.out.admin.CacheStatisticsDTO;
import com.py.py.service.exception.ServiceException;

public interface StatisticsService {

	CacheStatisticsDTO getCacheStatisticsDTO() throws ServiceException;

}
