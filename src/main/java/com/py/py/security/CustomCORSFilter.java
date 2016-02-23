package com.py.py.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.GenericFilterBean;

import com.py.py.constants.HeaderNames;

public class CustomCORSFilter extends GenericFilterBean {

	protected String accessControlAllowOrigin = "*";
	protected String accessControlAllowMethods = "GET, PUT, POST, OPTIONS, DELETE";
	protected String accessControlMaxAge = "3600";
	protected String accessControlAllowHeaders = HeaderNames.AUTHENTICATION_TOKEN + ", " + HeaderNames.ANTI_CACHE_TIMESTAMP + ", Content-Type";
	protected List<String> accessControlAllowHeadersList = new ArrayList<String>();
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		response.setHeader("Access-Control-Allow-Origin", accessControlAllowOrigin);
		response.setHeader("Access-Control-Allow-Methods", accessControlAllowMethods);
		response.setHeader("Access-Control-Max-Age", accessControlMaxAge);
		response.setHeader("Access-Control-Allow-Headers", accessControlAllowHeaders);
		chain.doFilter(req, res);
	}

	public String getAccessControlAllowOrigin() {
		return accessControlAllowOrigin;
	}

	public void setAccessControlAllowOrigin(String accessControlAllowOrigin) {
		this.accessControlAllowOrigin = accessControlAllowOrigin;
	}

	public String getAccessControlAllowMethods() {
		return accessControlAllowMethods;
	}

	public void setAccessControlAllowMethods(String accessControlAllowMethods) {
		this.accessControlAllowMethods = accessControlAllowMethods;
	}

	public String getAccessControlMaxAge() {
		return accessControlMaxAge;
	}

	public void setAccessControlMaxAge(String accessControlMaxAge) {
		this.accessControlMaxAge = accessControlMaxAge;
	}
	
	/*

	public String getAccessControlAllowHeaders() {
		return accessControlAllowHeaders;
	}

	public void setAccessControlAllowHeaders(String accessControlAllowHeaders) {
		this.accessControlAllowHeaders = accessControlAllowHeaders;
	}
	
	*/
	
	public List<String> getAccessControlAllowHeadersList() {
		return accessControlAllowHeadersList;
	}
	
	public void setAccessControlAllowHeadersList(List<String> accessControlAllowHeadersList) {
		this.accessControlAllowHeadersList = accessControlAllowHeadersList;
		this.accessControlAllowHeaders = "";
		String delimiter = "";
		for(String header : accessControlAllowHeadersList) {
			this.accessControlAllowHeaders = this.accessControlAllowHeaders + delimiter + header;
			delimiter = ", ";
		}
	}
	
	
}
