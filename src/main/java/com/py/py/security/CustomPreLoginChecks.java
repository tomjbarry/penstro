package com.py.py.security;

import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

public class CustomPreLoginChecks implements UserDetailsChecker {
	
	protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	
    public void check(UserDetails user) {
        
    }
}
