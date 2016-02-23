package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.py.py.domain.subdomain.Permission;
import com.py.py.domain.subdomain.Role;
import com.py.py.service.RoleService;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.util.ArgCheck;

public class RoleServiceImpl implements RoleService {

	private String rolePropertyFilter;
	private String overrideRolePropertyFilter;
	private String rankRolePropertyFilter;
	private String rankOverrideRolePropertyFilter;
	private Properties propertiesHolder;
	private List<Role> roleList;
	private List<Role> overrideRoleList;
	private List<String> defaultRoles;
	private List<String> defaultOverrideRoles;
	private String lineRegexStrip = "[ \\n\\\\]";
	
	private String stripLine(String r) {
		if(r == null) {
			return null;
		}
		return r.replaceAll(lineRegexStrip, "");
	}
	
	// used in bean
	@SuppressWarnings("unused")
	private void populateRoles() {
		// retrieve default roles to be used upon user creation
		String rolesString = this.propertiesHolder.getProperty("py.defaults.user.roles");
		if(rolesString != null) {
			String[] defaultArray = rolesString.split(",");
			defaultRoles = new ArrayList<String>();
			for(String r : defaultArray) {
				if(r != null && !r.isEmpty()) {
					defaultRoles.add(stripLine(r));
				}
			}
		}
		
		String overrideRolesString = 
				this.propertiesHolder.getProperty("py.defaults.user.overrideRoles");
		if(overrideRolesString != null) {
			String[] defaultArray = overrideRolesString.split(",");
			defaultOverrideRoles = new ArrayList<String>();
			for(String r : defaultArray) {
				if(r != null && !r.isEmpty()) {
					defaultOverrideRoles.add(stripLine(r));
				}
			}
		}
		
		// populate all roles with corresponding permissions
		roleList = new ArrayList<Role>();
		for(Enumeration<Object> en = this.propertiesHolder.keys(); en.hasMoreElements(); ) {
			String key = (String) en.nextElement();
			if(key.startsWith(this.rolePropertyFilter + ".")) {
				roleList.add(createRole(key.replaceFirst("^"+this.rolePropertyFilter+"[.]", ""),
						this.propertiesHolder.getProperty(key)));
			}
		}
		
		overrideRoleList = new ArrayList<Role>();
		for(Enumeration<Object> en = this.propertiesHolder.keys(); en.hasMoreElements(); ) {
			String key = (String) en.nextElement();
			if(key.startsWith(this.overrideRolePropertyFilter + ".")) {
				overrideRoleList.add(createRole(key.replaceFirst("^"+this.overrideRolePropertyFilter+"[.]", ""),
						this.propertiesHolder.getProperty(key)));
			}
		}
		
		// ranks
		for(Enumeration<Object> en = this.propertiesHolder.keys(); en.hasMoreElements(); ) {
			String key = (String) en.nextElement();
			if(key.startsWith(this.rankRolePropertyFilter + ".")) {
				String roleName = key.replaceFirst("^"+this.rankRolePropertyFilter+"[.]", "");
				
				try {
					Role role = getRoleByName(roleName);
					int rank = 0;
					String v = this.propertiesHolder.getProperty(key);
					rank = Integer.parseInt(v);
					role.setRank(rank);
				} catch(Exception e) {
					// do nothing
				}
			}
		}
		
		for(Enumeration<Object> en = this.propertiesHolder.keys(); en.hasMoreElements(); ) {
			String key = (String) en.nextElement();
			if(key.startsWith(this.rankOverrideRolePropertyFilter + ".")) {
				String roleName = key.replaceFirst("^"+this.rankOverrideRolePropertyFilter+"[.]", "");
				
				try {
					Role role = getOverrideRoleByName(roleName);
					int rank = 0;
					String v = this.propertiesHolder.getProperty(key);
					rank = Integer.parseInt(v);
					role.setRank(rank);
				} catch(Exception e) {
					// do nothing
				}
			}
		}
	}
	
	private Role createRole(String key, String property) {
		Role role = new Role(key);
		String[] permissions = property.split(",");
		for(String p : permissions) {
			if(p != null && !p.isEmpty()) {
				Permission permission = new Permission(stripLine(p));
				role.addPermission(permission);
			}
		}
		return role;
	}
	
	public void setRolePropertyFilter(String propertyFilter) {
		this.rolePropertyFilter = propertyFilter;
	}
	
	public void setOverrideRolePropertyFilter(String propertyFilter) {
		this.overrideRolePropertyFilter = propertyFilter;
	}

	public void setPropertiesHolder(Properties propertiesHolder) {
		this.propertiesHolder = propertiesHolder;
	}

	@Override
	public Role getRoleByName(String name) throws BadParameterException {
		ArgCheck.nullCheck(name);
		if(name.isEmpty()) {
			throw new BadParameterException();
		}
		
		for(Role role : roleList) {
			if(role.getName().equals(name)) {
				return role;
			}
		}
		return null;
	}

	@Override
	public Role getOverrideRoleByName(String name) throws BadParameterException {
		ArgCheck.nullCheck(name);
		if(name.isEmpty()) {
			throw new BadParameterException();
		}
		
		for(Role role : overrideRoleList) {
			if(role.getName().equals(name)) {
				return role;
			}
		}
		return null;
	}
	
	@Override
	public List<Permission> getPermissionsFromRoleNames(List<String> roleNames,
			List<String> overrideRoles) throws BadParameterException {
		List<Permission> permissions = new ArrayList<Permission>();
		if(overrideRoles != null && !overrideRoles.isEmpty()) {
			int rank = -1;
			List<Permission> highest = new ArrayList<Permission>();
			for(String name : overrideRoles) {
				Role role = getOverrideRoleByName(name);
				if(role != null && role.getPermissions() != null) {
					if(role.getRank() > rank) {
						highest = role.getPermissions();
					}
				}
			}
			permissions.addAll(highest);
		} else {
			if(roleNames != null) {
				for(String name : roleNames) {
					Role role = getRoleByName(name);
					if(role != null && role.getPermissions() != null) {
						permissions.addAll(role.getPermissions());
					}
				}
			}
		}
		return permissions;
	}

	@Override
	public List<String> getDefaultRoles() {
		return defaultRoles;
	}
	
	@Override
	public List<String> getDefaultOverrideRoles() {
		return defaultOverrideRoles;
	}

	public void setRankRolePropertyFilter(String rankRolePropertyFilter) {
		this.rankRolePropertyFilter = rankRolePropertyFilter;
	}

	public void setRankOverrideRolePropertyFilter(
			String rankOverrideRolePropertyFilter) {
		this.rankOverrideRolePropertyFilter = rankOverrideRolePropertyFilter;
	}
	
}
