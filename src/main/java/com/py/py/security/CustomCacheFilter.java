package com.py.py.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

public class CustomCacheFilter extends GenericFilterBean {
	
	protected Map<String, String> cacheHeaders = new HashMap<String, String>();
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		for(Map.Entry<String, String> header : cacheHeaders.entrySet()) {
			response.addHeader(header.getKey(), header.getValue());
		}
		chain.doFilter(req, res);
	}

	public Map<String, String> getCacheHeaders() {
		return cacheHeaders;
	}

	public void setCacheHeaders(Map<String, String> cacheHeaders) {
		this.cacheHeaders = cacheHeaders;
	}
}
