package com.py.py.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;

import com.py.py.domain.subdomain.Permission;
import com.py.py.util.PyLogger;

public class CustomAnonymousAuthenticationFilter extends
		AnonymousAuthenticationFilter {

	protected static final PyLogger logger = 
			PyLogger.getLogger(CustomAnonymousAuthenticationFilter.class);
	protected String anonymousProperty = "py.role.role.anonymous";
	protected Properties propertiesHolder;
	private String lineRegexStrip = "[ \\n\\\\]";
	
	// these are forced to be completely overridden because the authors apparently 
	// have no idea what protected members are for, especially since the setters are now
	// deprecated, and all methods refer specifically to the members instead of to
	// getters, leaving no reasonable alternative than to just rewrite the class
	protected AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource
    	= new WebAuthenticationDetailsSource();
	protected String key;
	protected Object principal;
	protected List<GrantedAuthority> authorities;
	
	private String stripLine(String r) {
		if(r == null) {
			return null;
		}
		return r.replaceAll(lineRegexStrip, "");
	}

    public CustomAnonymousAuthenticationFilter(String key) {
        this(key, "anonymous");
    }
    
    public CustomAnonymousAuthenticationFilter(String key, Object principal) {
    	super(key);
        this.key = key;
        this.principal = principal;
        this.authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new Permission("REGISTER"));
    }
	
	protected List<GrantedAuthority> getAnonymousStringList() {
		if(propertiesHolder != null) {
			String permissionString = this.propertiesHolder.getProperty(anonymousProperty);
			if(permissionString != null) {
				String[] defaultArray = permissionString.split(",");
				List<GrantedAuthority> permissionSet = new ArrayList<GrantedAuthority>();
				for(String r : defaultArray) {
					if(r != null && !r.isEmpty()) {
						permissionSet.add(new Permission(stripLine(r)));
					}
				}
				return permissionSet;
			}
		}
		ArrayList<GrantedAuthority> list = new ArrayList<GrantedAuthority>();
		list.add(new Permission("REGISTER"));
		return list;
	}
	
    @Override
    public void afterPropertiesSet() {
        Assert.hasLength(key);
        Assert.notNull(principal, "Anonymous authentication principal must be set");
        Assert.notNull(authorities, "Anonymous authorities must be set");
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            SecurityContextHolder.getContext().setAuthentication(createAuthentication((HttpServletRequest) req));

            logger.debug("Populated SecurityContextHolder with anonymous token: '"
                + SecurityContextHolder.getContext().getAuthentication() + "'");
        } else {
            logger.debug("SecurityContextHolder not populated with anonymous token, as it already contained: '"
                + SecurityContextHolder.getContext().getAuthentication() + "'");
        }

        chain.doFilter(req, res);
    }

    protected Authentication createAuthentication(HttpServletRequest request) {
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(key, principal, authorities);
        auth.setDetails(authenticationDetailsSource.buildDetails(request));

        return auth;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        Assert.notNull(authenticationDetailsSource, "AuthenticationDetailsSource required");
        this.authenticationDetailsSource = authenticationDetailsSource;
    }

    public Object getPrincipal() {
        return principal;
    }

    public List<GrantedAuthority> getAuthorities() {
        return authorities;
    }

	public void setAnonymousProperty(String anonymousProperty) {
		this.anonymousProperty = anonymousProperty;
	}

	public void setPropertiesHolder(Properties propertiesHolder) {
		this.propertiesHolder = propertiesHolder;
		// cannot load authorities until now
		this.authorities = getAnonymousStringList();
	}
    
    
}
