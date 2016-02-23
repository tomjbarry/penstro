package com.py.py.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.Assert;

import com.py.py.domain.User;
import com.py.py.domain.constants.SchemaUpdates;
import com.py.py.service.AuthenticationService;
import com.py.py.service.UserService;
import com.py.py.service.exception.LoginLockedException;
import com.py.py.service.exception.ServiceException;

public class LimitingDaoAuthenticationProvider extends DaoAuthenticationProvider {

	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private UserService userService;
	
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		User user = null;
		Authentication retAuth;
		String name = authentication.getName();
		UsernamePasswordRememberMeAuthenticationToken uprmat = (UsernamePasswordRememberMeAuthenticationToken)authentication;
		
		String address = uprmat.getClientAddress();
		if(address == null) {
			address = "";
		}
		
		try {
			// do not run by usual service locking requirements, those are checked later
			user = userService.findUserByUsername(name);
			if(authService.isLoginLocked(user, address)) {
				// login is locked, dont record more attempts!
				//authService.recordLoginAttempt(user, address, false);
				throw new LoginLockedException();
			}
			retAuth = continueAuthentication(authentication, user);
			// checks again for the location lock
			
			authService.recordLoginAttempt(user, address, true);
			
		} catch(BadCredentialsException e) {
			
			try {
				authService.recordLoginAttempt(user, address, false);
			} catch (ServiceException se) {
				throw e;
			}
			throw e;
			
		} catch(LoginLockedException lle) {
			throw lle;
		} catch(ServiceException se) {
			throw new BadCredentialsException(se.getMessage());
		}
		
		return retAuth;
	}

  public Authentication continueAuthentication(Authentication authentication, User user) 
  		throws AuthenticationException {

    Assert.notNull(user, "User was null - a violation of the interface contract");
    
    UserDetails userDetails = authService.loadUser(user, true);
    getPreAuthenticationChecks().check(userDetails);
    
    // pre-upgrade check here
    boolean needsBcryptUpdate = user.hasPendingSchemaUpdate(SchemaUpdates.USER_PASSWORD_BCRYPT);
    UsernamePasswordAuthenticationToken upa = (UsernamePasswordAuthenticationToken)authentication;
    if(!needsBcryptUpdate) {
    	try {
    	upa = new UsernamePasswordAuthenticationToken(upa.getPrincipal(), authService.prehashPassword((String)upa.getCredentials()));
    	} catch(Exception e) {
    		logger.info("Cannot compare passwords for user {" + user.getUsername() + "} for USER_PASSWORD_BCRYPT schema update! This user probably cannot log in!", e);
    	}
    }
    
    // password check here
    additionalAuthenticationChecks(userDetails, upa);

    getPostAuthenticationChecks().check(userDetails);

    Object principalToReturn = userDetails;
    
    if(needsBcryptUpdate) {
    	try {
    		userService.doPendingSchemaUpdatePassword(user.getId(), authService.encodePassword((String)upa.getCredentials()), SchemaUpdates.USER_PASSWORD_BCRYPT);
    	} catch(Exception e) {
    		logger.info("Cannot convert user {" + user.getUsername() + "} for USER_PASSWORD_BCRYPT schema update!", e);
    	}
    }

    boolean rememberMe = ((UsernamePasswordRememberMeAuthenticationToken)authentication).isRememberMe();
    
    return createSuccessAuthentication(principalToReturn, authentication, 
    		user, userDetails, rememberMe);
  }

  protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
          User user, UserDetails userDetails, boolean rememberMe) {
    UserAuthenticationToken result = new UserAuthenticationToken(user, principal,
            authentication.getCredentials(), authService.generateTokenData(), authService.generateInactivity(rememberMe), rememberMe, userDetails.getAuthorities());
    result.setDetails(authentication.getDetails());

    return result;
  }
    
}
