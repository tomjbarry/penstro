package com.py.py.security;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

import com.py.py.domain.User;

public class UserAuthenticationToken extends
		UsernamePasswordAuthenticationToken {
	private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  //~ Instance fields ================================================================================================
  private final User user;
  private final long inactivity;
  private final String token;
  private final Boolean rememberMe;
  
  //~ Constructors ===================================================================================================

  public UserAuthenticationToken(User user, Object principal, Object credentials, String token, long inactivity, Boolean rememberMe) {
      super(principal, credentials);
      this.user = user;
      this.token = token;
      this.inactivity = inactivity;
      this.rememberMe = rememberMe;
  }
  
  public UserAuthenticationToken(User user, Object principal, Object credentials, String token, long inactivity, Boolean rememberMe, Collection<? extends GrantedAuthority> authorities) {
      super(principal, credentials, authorities);
      this.user = user;
      this.token = token;
      this.inactivity = inactivity;
      this.rememberMe = rememberMe;
  }

	public User getUser() {
		return user;
	}
	
	public String getUnencodedToken() {
		return token;
	}
	
	public long getInactivity() {
		return inactivity;
	}
	
	public Boolean getRememberMe() {
		return rememberMe;
	}
}
