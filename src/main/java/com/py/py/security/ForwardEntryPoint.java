package com.py.py.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class ForwardEntryPoint implements AuthenticationEntryPoint {

	protected String defaultForwardUrl = "/denied";
	
	@Override
	public void commence(HttpServletRequest req,
			HttpServletResponse res, AuthenticationException authException)
			throws IOException, ServletException {

        req.getRequestDispatcher(defaultForwardUrl).forward(req, res);
	}

	public String getDefaultForwardUrl() {
		return defaultForwardUrl;
	}

	public void setDefaultForwardUrl(String defaultForwardUrl) {
		this.defaultForwardUrl = defaultForwardUrl;
	}
}
