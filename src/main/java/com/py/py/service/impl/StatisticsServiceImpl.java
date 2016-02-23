package com.py.py.service.impl;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.statistics.StatisticsGateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;

import com.py.py.dto.out.admin.CacheStatisticsDTO;
import com.py.py.service.StatisticsService;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.Mapper;

public class StatisticsServiceImpl implements StatisticsService {

	private static final List<String> availableStatistics = Arrays.asList(
			"getSize",
			"getRemoteSize",
			"getLocalDiskSize",
			"getLocalDiskSizeInBytes",
			"getLocalHeapSize",
			"getLocalHeapSizeInBytes",
			"getLocalOffHeapSize",
			"cacheHitRatio",
			"cacheHitCount",
			"cacheExpiredCount",
			"cacheMissCount",
			"cacheMissExpiredCount",
			"cachePutCount",
			"cacheEvictedCount",
			"cachePutUpdatedCount",
			"cachePutAddedCount",
			"cacheRemoveCount",
			"cacheMissNotFoundCount"
			);
	
	@Autowired
	protected CacheManager cacheManager;
	
	protected Map<String, Map<String, Object> > getCacheStatistics() 
			throws ServiceException {
		Map<String, Map<String, Object> > stats = 
				new HashMap<String, Map<String, Object> >();
		if(cacheManager != null && cacheManager.getCacheNames() != null) {
			for(String cacheName : cacheManager.getCacheNames()) {
				Map<String, Object> cacheStatistics = new HashMap<String, Object>();
				Cache cache = (Cache)cacheManager.getCache(cacheName).getNativeCache();
				StatisticsGateway statsGateway = cache.getStatistics();
				for(String methodName : availableStatistics) {
					try {
						Method m = statsGateway.getClass().getDeclaredMethod(methodName);
						m.setAccessible(true);
						cacheStatistics.put(formatMethodName(methodName), m.invoke(statsGateway));
					} catch(Exception e) {
						// do nothing
					}
				}
				stats.put(cacheName, cacheStatistics);
			}
		}
		return stats;
	}
	
	private String formatMethodName(String name) {
		if(name == null) {
			return null;
		}
		if(name.startsWith("get") || name.startsWith("Get")) {
			return name.substring(3, 4).toLowerCase() + name.substring(4);
		} else {
			return name;
		}
	}
	
	@Override
	public CacheStatisticsDTO getCacheStatisticsDTO() throws ServiceException {
		return Mapper.mapCacheStatisticsDTO(getCacheStatistics());
	}
	
}
