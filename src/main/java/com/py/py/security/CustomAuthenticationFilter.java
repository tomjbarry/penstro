package com.py.py.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.py.py.service.AuthenticationService;
import com.py.py.util.PyLogger;

public class CustomAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

	protected static final PyLogger logger = 
			PyLogger.getLogger(CustomAuthenticationFilter.class);
	
	protected AuthenticationService authenticationService;
	protected String authenticationDenied = "/denied";
	protected String authenticationExpired = "/expired";

	protected CustomAuthenticationFilter(AuthenticationService authenticationService) {
		super("/");
		this.authenticationService = authenticationService;
	}
	
	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) 
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Authentication auth = null;
		try {
			auth = attemptAuthentication(request, response);
		} catch(AuthenticationException ae) {
			unsuccessfulAuthentication(request, response, ae);
			return;
		}
			
        if (auth != null) {
            try {
                // Store to SecurityContextHolder
                SecurityContextHolder.getContext().setAuthentication(auth);

                successfulAuthentication(request, response, chain, auth);

                logger.debug("SecurityContextHolder populated with remember-me token: '"
                    + SecurityContextHolder.getContext().getAuthentication() + "'");

                // Fire event
                if (this.eventPublisher != null) {
                    eventPublisher.publishEvent(new InteractiveAuthenticationSuccessEvent(
                            SecurityContextHolder.getContext().getAuthentication(), this.getClass()));
                }

            } catch (AuthenticationException authenticationException) {
                logger.debug("SecurityContextHolder not populated with token, as "
                        + "AuthenticationManager rejected Authentication returned: '"
                        + auth + "'; invalidating remember-me token", authenticationException);

                unsuccessfulAuthentication(request, response, authenticationException);
                return;
            }
        }

        chain.doFilter(request, response);
	}
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
			HttpServletResponse response) throws AuthenticationException,
			IOException, ServletException {
		Authentication userAuthenticationToken = authenticationService.authenticate(
				request, response);
		return userAuthenticationToken;
	}

	public void setAuthenticationService(AuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	public void setAuthenticationDenied(String authenticationDenied) {
		this.authenticationDenied = authenticationDenied;
	}

	public void setAuthenticationExpired(String authenticationExpired) {
		this.authenticationExpired = authenticationExpired;
	}

}
