package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.py.py.security.exception.AuthenticationExpiredException;
import com.py.py.security.exception.AuthenticationTheftException;

public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	protected String defaultFailureUrl = "/denied";
	protected String theftFailureUrl = "/theft";
	protected String expiredFailureUrl = "/expired";
	protected String lockedFailureUrl = "/locked";
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, 
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		if(exception instanceof AuthenticationTheftException) {
			request.getRequestDispatcher(theftFailureUrl).forward(request, response);
		} else if(exception instanceof AuthenticationExpiredException) {
			request.getRequestDispatcher(expiredFailureUrl).forward(request, response);
		} else if(exception instanceof LockedException) {
			request.getRequestDispatcher(lockedFailureUrl).forward(request, response);
		} else {
			request.getRequestDispatcher(defaultFailureUrl).forward(request, response);
		}
	}

	public void setDefaultFailureUrl(String defaultFailureUrl) {
		this.defaultFailureUrl = defaultFailureUrl;
	}

	public void setTheftFailureUrl(String theftFailureUrl) {
		this.theftFailureUrl = theftFailureUrl;
	}

	public void setExpiredFailureUrl(String expiredFailureUrl) {
		this.expiredFailureUrl = expiredFailureUrl;
	}
	
	public void setLockedFailureUrl(String lockedFailureUrl) {
		this.lockedFailureUrl = lockedFailureUrl;
	}
}
