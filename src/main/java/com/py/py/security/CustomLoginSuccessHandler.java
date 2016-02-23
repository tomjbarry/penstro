package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import com.py.py.domain.User;
import com.py.py.service.AuthenticationService;
import com.py.py.service.exception.ServiceException;

public class CustomLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	@Autowired
	protected AuthenticationService authenticationService;
	
	public CustomLoginSuccessHandler() {
		super();
    }
	
    public CustomLoginSuccessHandler(String defaultTargetUrl) {
        super(defaultTargetUrl);
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
    	return request.getServletPath();
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
    	if(authentication != null && authentication.isAuthenticated()) {
    		UserAuthenticationToken uat = (UserAuthenticationToken)authentication;
    		User user = uat.getUser();
    		String token = uat.getUnencodedToken();
    		long inactivity = uat.getInactivity();
    		boolean rememberMe = false;
    		if(uat.getRememberMe() != null) {
    			rememberMe = uat.getRememberMe();
    		}
    		if(user == null) {
    			throw new ServletException();
    		}
    		try {
    			authenticationService.onLoginSuccess(request, response, user, token, inactivity, rememberMe);
    		} catch (ServiceException se) {
    			throw new ServletException();
    		}
    	} else {
    		request.getRequestDispatcher(getDefaultTargetUrl()).forward(request, response);
    	}
    }

}
