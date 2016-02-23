package com.py.py.security;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.py.py.util.PyLogger;

public class CustomLogoutFilter extends LogoutFilter {

	protected static final PyLogger logger = PyLogger.getLogger(CustomLogoutFilter.class);
    protected List<LogoutHandler> handlers;
    protected CustomLogoutHandler logoutSuccessHandler = new CustomLogoutHandler();
    protected CustomLogoutHandler logoutFailureHandler = new CustomLogoutHandler();
	
	public CustomLogoutFilter(String logoutSuccessUrl, 
			String logoutFailureUrl, LogoutHandler... handlers) {
		super(logoutSuccessUrl, handlers);
		Assert.notEmpty(handlers, "LogoutHandlers are required");
        this.handlers = Arrays.asList(handlers);
        Assert.isTrue(!StringUtils.hasLength(logoutSuccessUrl) ||
                UrlUtils.isValidRedirectUrl(logoutSuccessUrl), logoutSuccessUrl + " isn't a valid forward URL");
        Assert.isTrue(!StringUtils.hasLength(logoutFailureUrl) ||
                UrlUtils.isValidRedirectUrl(logoutFailureUrl), logoutFailureUrl + " isn't a valid forward URL");

        if (StringUtils.hasText(logoutSuccessUrl)) {
            logoutSuccessHandler.setTargetUrl(logoutSuccessUrl);
        }
        
        if (StringUtils.hasText(logoutFailureUrl)) {
        	logoutFailureHandler.setTargetUrl(logoutFailureUrl);
        }
    }
	
	@Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (requiresLogout(request, response)) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if(auth != null && auth.isAuthenticated()) {
	            
	            logger.debug("Logging out user '" + auth + "' and transferring to logout destination");
	
	            for (LogoutHandler handler : handlers) {
	                handler.logout(request, response, auth);
	            }
	
	            logoutSuccessHandler.handle(request, response, auth);
	
	            return;
            } else {
            	logoutFailureHandler.handle(request, response, auth);
            	return;
            }
        }

        chain.doFilter(request, response);
    }
	
}
