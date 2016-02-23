package com.py.py.service.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

/* this class should be used for blank models and model construction,
 * especially in the case where the model cannot be found.
 * This will keep implementations consistent within view models.
 */

public class ModelFactory {
	
	// used for counting
	public static Pageable constructEmptyPageable() {
		return new PageRequest(0,1);
	}
	
	public static <T,E> Map<T,E> constructMap() {
		return new HashMap<T,E>();
	}
	
	public static <T> Page<T> constructPage() {
		List<T> constructList = constructList();
		return new PageImpl<T>(constructList);
	}
	
	public static <T> List<T> constructList() {
		return new ArrayList<T>();
	}
	
	public static UserDetails constructUserDetails(
			String username,
			String password,
			boolean enabled,
			boolean accountNonExpired,
			boolean credentialsNonExpired,
			boolean accountNonLocked,
			Collection<? extends GrantedAuthority> authorities) {
		
		return new User(username, password, enabled, accountNonExpired,
				credentialsNonExpired, accountNonLocked, authorities);
	}
	
}
