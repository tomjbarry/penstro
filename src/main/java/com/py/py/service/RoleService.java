package com.py.py.service;

import java.util.List;

import com.py.py.domain.subdomain.Permission;
import com.py.py.domain.subdomain.Role;
import com.py.py.service.exception.BadParameterException;

public interface RoleService {

	List<String> getDefaultRoles();

	List<String> getDefaultOverrideRoles();
	
	Role getRoleByName(String name) throws BadParameterException;
	
	List<Permission> getPermissionsFromRoleNames(List<String> roleNames,
			List<String> overrideRoles) throws BadParameterException;

	Role getOverrideRoleByName(String name) throws BadParameterException;

}
