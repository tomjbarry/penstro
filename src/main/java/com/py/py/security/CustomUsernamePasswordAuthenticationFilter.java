package com.py.py.security;

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.py.py.dto.in.LoginRequestDTO;
import com.py.py.security.exception.AuthenticationParameterException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.util.ServiceUtils;

public class CustomUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
	
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
		if (!request.getMethod().equals("POST")) {
	        throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
	    }
	
	    LoginRequestDTO dto = this.constructLoginRequestDTO(request);
	    
	    String username = null;
	    String password = null;
	    boolean rememberMe = false;
	    
	    if(dto != null) {
	    	username = dto.getUsername();
	    	password = dto.getPassword();
	    }
	    
	    if (username == null) {
	        username = "";
	    }
	
	    if (password == null) {
	        password = "";
	    }
	    
	    if(dto.getRememberMe() != null && dto.getRememberMe()) {
	    	rememberMe = true;
	    }
	
	    username = username.trim();
	    
	    String clientAddress = null;
	    try {
	    	clientAddress = ServiceUtils.getClientAddress(request);
	    } catch(BadParameterException bpe) {
	    	// continue
	    }
	
	    UsernamePasswordRememberMeAuthenticationToken authRequest = new UsernamePasswordRememberMeAuthenticationToken(username, password, clientAddress, rememberMe);
	
	    // Allow subclasses to set the "details" property
	    setDetails(request, authRequest);
	
	    return this.getAuthenticationManager().authenticate(authRequest);
	}
	
	private LoginRequestDTO constructLoginRequestDTO(HttpServletRequest request) throws AuthenticationException {
		LoginRequestDTO dto = new LoginRequestDTO();
		dto.setUsername(null);
		dto.setPassword(null);
		dto.setRememberMe(null);
		try {
			StringBuffer sb = new StringBuffer();
			String line = null;
			
			BufferedReader reader = request.getReader();
			while((line = reader.readLine()) != null) {
				sb.append(line);
			}
			
			ObjectMapper mapper = new ObjectMapper();
			dto = mapper.readValue(sb.toString(), LoginRequestDTO.class);
			
			return dto;
		} catch(Exception e) {
			throw new AuthenticationParameterException("Invalid parameters");
		}
	}
	
}
