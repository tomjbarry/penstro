package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;

import com.py.py.util.PyLogger;

public class CustomLogoutHandler extends AbstractAuthenticationTargetUrlRequestHandler {

	protected static final PyLogger logger = PyLogger.getLogger(CustomLogoutHandler.class);
	
	protected String targetUrl = "/logout/success";
	
	@Override
	protected void handle(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to forward to " + targetUrl);
            return;
        }

        request.getRequestDispatcher(targetUrl).forward(request, response);
    }
	
	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	
	public String getTargetUrl() {
		return targetUrl;
	}
	
}
