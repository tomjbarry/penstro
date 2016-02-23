package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

public class CustomAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
	
	public CustomAuthenticationSuccessHandler() {
    }

    public CustomAuthenticationSuccessHandler(String defaultTargetUrl) {
        super(defaultTargetUrl);
    }
    
    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
    	return request.getServletPath();
    }
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
    	// currently do nothing, forwarding overwrites response and throws exceptions
    	//request.getRequestDispatcher(request.getServletPath()).forward(request, response);
    }
}
