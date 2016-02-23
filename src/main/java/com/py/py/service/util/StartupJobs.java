package com.py.py.service.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import com.py.py.dao.UserDao;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.service.AuthenticationService;
import com.py.py.service.RestrictedService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.ServiceException;
import com.py.py.util.GenericDefaults;
import com.py.py.util.PyLogger;

public class StartupJobs {

	protected static final PyLogger logger = PyLogger.getLogger(StartupJobs.class);
	
	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	protected List<String> adminRoleList = new ArrayList<String>();
	protected List<String> removeOverrideRoleList = new ArrayList<String>();
	protected List<RegisterUserDTO> adminUsers = new ArrayList<RegisterUserDTO>();
	protected List<RegisterUserDTO> defaultUsers = new ArrayList<RegisterUserDTO>();
	
	private String restrictedUsernamesPropertyFilter;
	private String restrictedPasswordsPropertyFilter;
	private String restrictedEmailsPropertyFilter;
	private Properties propertiesHolder;
	private String lineRegexStrip = "[ \\n\\\\]";
	
	private String language = GenericDefaults.LANGUAGE;
	
	@Autowired
	private AuthenticationService authService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private UserDao userDao;
	
	@Autowired
	private RestrictedService restrictedService;

	private String stripLine(String r) {
		if(r == null) {
			return null;
		}
		return r.replaceAll(lineRegexStrip, "");
	}
	
	private List<String> getPropertyList(String property) {
		String propertyString = this.propertiesHolder.getProperty(property);
		List<String> list = new ArrayList<String>();
		if(propertyString != null) {
			String[] defaultArray = propertyString.split(",");
			for(String p : defaultArray) {
				if(p != null && !p.isEmpty()) {
					list.add(stripLine(p));
				}
			}
		}
		return list;
	}
	
	public void startup() throws ServiceException {
		createAdminUsers();
		createDefaultUsers();
		
		// restrictions after so that the default and admin users bypass this
		createRestrictedUsernames();
		createRestrictedPasswords();
		createRestrictedEmails();
		
		finishStartup();
	}
	
	public void finishStartup() throws ServiceException {
		loadFollowees();
	}
	
	public void createRestrictedUsernames() throws ServiceException {
		restrictedService.addStartupWords(getPropertyList(restrictedUsernamesPropertyFilter), RESTRICTED_TYPE.USERNAME);
	}
	
	public void createRestrictedPasswords() throws ServiceException {
		restrictedService.addStartupWords(getPropertyList(restrictedPasswordsPropertyFilter), RESTRICTED_TYPE.PASSWORD);
	}

	public void createRestrictedEmails() throws ServiceException {
		restrictedService.addStartupWords(getPropertyList(restrictedEmailsPropertyFilter), RESTRICTED_TYPE.EMAIL);
	}
	
	public void createAdminUsers() throws ServiceException {
		logger.info("Creating " + adminUsers.size() + " admin users");
		for(RegisterUserDTO dto : adminUsers) {
			try {
				authService.registerUser(dto, language, null, false);
				User user = userService.findUserByUsername(dto.getUsername());
				for(String adminRole : adminRoleList) {
					try {
						userDao.addRole(user.getId(), adminRole, null);
					} catch(Exception e) {
						logger.warn("Error adding role '" + adminRole
								+ "' to user '" + dto.getUsername() + "'.");
					}
				}
				for(String removeRole : removeOverrideRoleList) {
					try {
						userDao.removeRole(user.getId(), null, removeRole);
					} catch(Exception e) {
						logger.warn("Error removing role '" + removeRole
								+ "' to user '" + dto.getUsername() + "'.");
					}
				}
				try {
					ChangeSettingsDTO changeSettings = new ChangeSettingsDTO();
					Map<SETTING_OPTION, Boolean> options = 
							new HashMap<SETTING_OPTION, Boolean>();
					options.put(SETTING_OPTION.HIDE_USER_PROFILE, true);
					changeSettings.setOptions(options);
					userService.changeSettings(user.getId(), changeSettings);
				} catch(Exception e) {
					logger.warn("Error changing admin setting: 'HIDE_USER_PROFILE' to true");
				}
			} catch(ExistsException ee) {
				logger.debug("Username already exists: " + dto.getUsername());
			} catch(Exception e) {
				logger.warn("Error creating username: " + dto.getUsername());
				throw new ServiceException(e);
			}
		}
	}
	
	public void createDefaultUsers() throws ServiceException {
		logger.info("Creating " + defaultUsers.size() + " default users");
		for(RegisterUserDTO dto : defaultUsers) {
			try {
				authService.registerUser(dto, language, null, false);
				User user = userService.findUserByUsername(dto.getUsername());
				for(String removeRole : removeOverrideRoleList) {
					try {
						userDao.removeRole(user.getId(), null, removeRole);
					} catch(Exception e) {
						logger.warn("Error removing role '" + removeRole
								+ "' to user '" + dto.getUsername() + "'.");
					}
				}
				userDao.updatePayment(user.getId(), dto.getEmail());
			} catch(ExistsException ee) {
				logger.debug("Username already exists: " + dto.getUsername());
			} catch(Exception e) {
				logger.warn("Error creating username: " + dto.getUsername());
				throw new ServiceException(e);
			}
		}
	}
	
	public void loadFollowees() throws ServiceException {
		List<String> followeeStringList = defaultsFactory.getFolloweeList();
		logger.info("Loading " + followeeStringList.size() + " default followees.");
		try {
			List<User> users = userService.findUserListByUsernames(followeeStringList);
			if(users != null) {
				List<CachedUsername> cached = new ArrayList<CachedUsername>();
				for(User u : users) {
					cached.add(new CachedUsername(u.getId(), u.getUsername()));
				}
				defaultsFactory.setFolloweeCachedUsernameList(cached);
			}
			if(users.size() < followeeStringList.size()) {
				logger.error("Not all users were found when creating followee list!");
				throw new ServiceException();
			}
		} catch(ServiceException se) {
			logger.error("Error creating followee list.");
			throw se;
		}
	}

	public void setAdminRoleList(List<String> adminRoleList) {
		this.adminRoleList = adminRoleList;
	}

	public void setRemoveOverrideRoleList(List<String> removeOverrideRoleList) {
		this.removeOverrideRoleList = removeOverrideRoleList;
	}

	public void setAdminUsers(List<RegisterUserDTO> adminUsers) {
		this.adminUsers = adminUsers;
	}

	public void setDefaultUsers(List<RegisterUserDTO> defaultUsers) {
		this.defaultUsers = defaultUsers;
	}

	public void setRestrictedUsernamesPropertyFilter(
			String restrictedUsernamesPropertyFilter) {
		this.restrictedUsernamesPropertyFilter = restrictedUsernamesPropertyFilter;
	}

	public void setRestrictedPasswordsPropertyFilter(
			String restrictedPasswordsPropertyFilter) {
		this.restrictedPasswordsPropertyFilter = restrictedPasswordsPropertyFilter;
	}

	public void setRestrictedEmailsPropertyFilter(
			String restrictedEmailsPropertyFilter) {
		this.restrictedEmailsPropertyFilter = restrictedEmailsPropertyFilter;
	}

	public void setPropertiesHolder(Properties propertiesHolder) {
		this.propertiesHolder = propertiesHolder;
	}

	public void setLineRegexStrip(String lineRegexStrip) {
		this.lineRegexStrip = lineRegexStrip;
	}
	
	
}
