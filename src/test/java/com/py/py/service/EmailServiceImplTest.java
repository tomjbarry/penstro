package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.EmailDao;
import com.py.py.domain.EmailTask;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.enumeration.TASK_STATE;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.impl.EmailServiceImpl;
import com.py.py.service.mail.EmailClientManager;
import com.py.py.service.mail.EmailDetails;

public class EmailServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("emailService")
	private EmailServiceImpl emailService;

	@Autowired
	protected EmailDao emailDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected EmailClientManager clientManager;
	
	private List<EmailTask> validTasks = new ArrayList<EmailTask>();
	
	@Before
	public void setUp() {
		reset(emailDao, userService);
		EmailDetails details = mock(EmailDetails.class);
		when(details.getLink()).thenReturn("127.0.0.1");
		when(details.getFrom()).thenReturn("Testing");
		when(details.getText()).thenReturn("test");
		emailService.setChangeEmailDetails(details);
		emailService.setConfirmationDetails(details);
		emailService.setPasswordResetDetails(details);
		emailService.setOfferDetails(details);
		
		EmailTask et1 = new EmailTask();
		et1.setId(new ObjectId());
		et1.setState(TASK_STATE.INITIAL);
		et1.setTarget(validEmail);
		et1.setType(EMAIL_TYPE.RESET);
		EmailTask et2 = new EmailTask();
		et2.setId(new ObjectId());
		et2.setState(TASK_STATE.INITIAL);
		et2.setTarget(validEmail);
		et2.setType(EMAIL_TYPE.RESET);
		EmailTask et3 = new EmailTask();
		et3.setId(new ObjectId());
		et3.setState(TASK_STATE.INITIAL);
		et3.setTarget(validEmail);
		et3.setType(EMAIL_TYPE.RESET);
		validTasks.add(et1);
		validTasks.add(et2);
		validTasks.add(et3);
	}
	
	@Test(expected = BadParameterException.class)
	public void resetPasswordNull() throws Exception {
		emailService.resetPassword(null, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void resetPasswordInvalid() throws Exception {
		emailService.resetPassword(invalidEmail, validName);
	}
	
	@Test
	public void resetPassword() throws Exception {
		emailService.resetPassword(validEmail, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationNull() throws Exception {
		emailService.confirmation(null, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationInvalid() throws Exception {
		emailService.confirmation(invalidEmail, validName);
	}
	
	@Test
	public void confirmation() throws Exception {
		emailService.confirmation(validEmail, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull() throws Exception {
		emailService.changeEmail(null, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailInvalid() throws Exception {
		emailService.changeEmail(invalidEmail, validName);
	}
	
	@Test
	public void changeEmail() throws Exception {
		emailService.changeEmail(validEmail, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void offerEmailNull() throws Exception {
		emailService.offerEmail(null, validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void offerEmailInvalid() throws Exception {
		emailService.offerEmail(invalidEmail, validName);
	}
	
	@Test
	public void offerEmail() throws Exception {
		emailService.offerEmail(validEmail, validName);
	}
	
	@Test
	public void cleanupCompleted() throws Exception {
		emailService.cleanupCompleted();
	}
	
	@Test
	public void cleanupErrors() throws Exception {
		emailService.cleanupErrors();
	}
	
	@Test
	public void sendEmails() throws Exception {
		when(emailDao.findNonCompleteTasks(any(Pageable.class), any(TASK_STATE.class), 
				any(EMAIL_TYPE.class))).thenReturn(new PageImpl<EmailTask>(validTasks));
		when(userService.findUserByEmail(anyString())).thenReturn(createValidUser())
			.thenThrow(new NotFoundException(validEmail)).thenReturn(createValidUser())
			.thenReturn(createValidUser());
		emailService.sendEmails();
		verify(clientManager, times(validTasks.size())).sendEmail(any(EmailDetails.class), any(EmailTask.class), anyString(), anyString());
	}
}
