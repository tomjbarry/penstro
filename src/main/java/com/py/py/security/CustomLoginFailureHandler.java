package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.py.py.constants.APIUrls;
import com.py.py.security.exception.AuthenticationParameterException;
import com.py.py.service.exception.LoginLockedException;
import com.py.py.util.PyLogger;

public class CustomLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	protected static final PyLogger logger = PyLogger.getLogger(CustomLoginFailureHandler.class);
	
	protected String lockedFailureUrl = APIUrls.LOCKED;
	protected String loginLockedFailureUrl = APIUrls.LOGIN_LOCKED;
	protected String parameterFailureUrl = APIUrls.INVALID;
	protected String defaultFailureUrl = APIUrls.DENIED;
	
	public CustomLoginFailureHandler() {
	}
	
	@Override
	public void onAuthenticationFailure(HttpServletRequest request, 
			HttpServletResponse response,
			AuthenticationException exception) throws IOException, ServletException {
		if(!response.isCommitted()) {
			if(exception.getClass().isAssignableFrom(LoginLockedException.class)) {
	            logger.debug("Forwarding to " + loginLockedFailureUrl);
	            request.getRequestDispatcher(loginLockedFailureUrl).forward(request, response);
			} else if(exception.getClass().isAssignableFrom(LockedException.class)) {
	            logger.debug("Forwarding to " + lockedFailureUrl);
	            request.getRequestDispatcher(lockedFailureUrl).forward(request, response);
			} else if(exception.getClass().isAssignableFrom(AuthenticationParameterException.class)) {
	            logger.debug("Forwarding to " + parameterFailureUrl);
				request.getRequestDispatcher(parameterFailureUrl).forward(request, response);
			} else {
	            logger.debug("Forwarding to " + defaultFailureUrl);
				request.getRequestDispatcher(defaultFailureUrl).forward(request, response);
			}
		}
	}

	public void setLockedFailureUrl(String lockedFailureUrl) {
		this.lockedFailureUrl = lockedFailureUrl;
	}

	public void setLoginLockedFailureUrl(String loginLockedFailureUrl) {
		this.loginLockedFailureUrl = loginLockedFailureUrl;
	}

	public void setParameterFailureUrl(String parameterFailureUrl) {
		this.parameterFailureUrl = parameterFailureUrl;
	}

	public void setDefaultFailureUrl(String defaultFailureUrl) {
		this.defaultFailureUrl = defaultFailureUrl;
	}
}
