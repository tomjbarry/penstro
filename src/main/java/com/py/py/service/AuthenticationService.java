package com.py.py.service;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.dto.in.ChangeEmailDTO;
import com.py.py.dto.in.ChangePasswordDTO;
import com.py.py.dto.in.ChangePasswordUnauthedDTO;
import com.py.py.dto.in.ChangePaymentDTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.dto.out.BalanceDTO;
import com.py.py.dto.out.CurrentUserDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;

public interface AuthenticationService extends UserDetailsService, InitializingBean, LogoutHandler {

	CurrentUserDTO getCurrentUserDTO(User user, UserInfo userInfo) throws ServiceException;

	boolean isLoginLocked(User user, String address) throws ServiceException;

	void recordLoginAttempt(User user, String address, boolean success) throws ServiceException;
	
	public Authentication authenticate(HttpServletRequest request, HttpServletResponse response);

	BalanceDTO getBalanceDTO(UserInfo userInfo) throws ServiceException;

	Date getLastLogin(User user) throws ServiceException;
	
	void changePassword(User user, ChangePasswordDTO dto)
			throws ServiceException;

	void changeEmail(User user, UserInfo userInfo, ChangeEmailDTO dto, String emailToken)
			throws ServiceException;

	void changePasswordUnauthed(User user, ChangePasswordUnauthedDTO dto,
			String emailToken) throws ServiceException;

	void sendConfirmation(User user, UserInfo userInfo) throws ServiceException;

	void confirmation(User user, UserInfo userInfo, String emailToken) throws ServiceException;

	void adminCheck(User user) throws ServiceException;

	void changeEmailRequest(User user) throws ServiceException;

	long getLoginFailureCount(User user) throws ServiceException;

	void delete(User user, String emailToken) throws ServiceException;

	void undelete(User user) throws ServiceException;

	void sendDelete(User user) throws ServiceException;

	void sendPaymentIdChange(User user) throws ServiceException;

	void changePaymentId(User user, ChangePaymentDTO dto, String emailToken)
			throws ServiceException;

	void resetPassword(User user) throws ServiceException;

	void accept(User user) throws ServiceException;

	void changeEmailPendingAction(User user, UserInfo userInfo,
			ChangeEmailDTO dto) throws ServiceException;

	String prehashPassword(String password);

	String encodePassword(String raw) throws ServiceException;

	String hashToken(String token) throws BadParameterException;

	String generateTokenData();

	long generateInactivity(Boolean rememberMe);

	UserDetails loadUser(User user, boolean credentialsNonExpired) throws UsernameNotFoundException;

	ResultSuccessDTO registerUser(RegisterUserDTO userdto, String language, String address, boolean requireRecaptcha)
		throws ServiceException;

	void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, User user, String unencodedToken,
		long inactivity, boolean rememberMe) throws ServiceException;

	void changeUserPassword(User user, String newPassword, String confirmNewPassword) throws ServiceException;

}
