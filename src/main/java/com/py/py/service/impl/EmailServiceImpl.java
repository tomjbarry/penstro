package com.py.py.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.mail.SimpleMailMessage;

import com.py.py.constants.PendingActions;
import com.py.py.dao.EmailDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.EmailTask;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.enumeration.TASK_STATE;
import com.py.py.service.AuthenticationService;
import com.py.py.service.EmailService;
import com.py.py.service.UserService;
import com.py.py.service.aws.SQSManager;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.mail.EmailClientManager;
import com.py.py.service.mail.EmailDetails;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;

public class EmailServiceImpl implements EmailService {
	
	protected static final PyLogger logger = PyLogger.getLogger(EmailServiceImpl.class);

	@Autowired
	protected AuthenticationService authService;
	
	@Autowired
	protected EmailDao emailDao;
	
	@Autowired
	protected UserService userService;
	
	/*@Autowired
	protected MailSender mailSender;
	*/
	
	@Autowired
	protected SQSManager sqsManager;
	
	@Autowired
	protected EmailClientManager clientManager;
	
	protected EmailDetails confirmationDetails;
	protected EmailDetails passwordResetDetails;
	protected EmailDetails changeEmailDetails;
	protected EmailDetails offerDetails;
	protected EmailDetails deleteDetails;
	protected EmailDetails changePaymentDetails;
	protected Integer emailsPageSize = 5;
	
	@Override
	public void resetPassword(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email, username);
		String checkedEmail = ServiceUtils.getEmail(email);
		String checkedUsername = ServiceUtils.getName(username);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setUsername(checkedUsername);
		task.setType(EMAIL_TYPE.RESET);
		
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void confirmation(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email, username);
		String checkedEmail = ServiceUtils.getEmail(email);
		String checkedUsername = ServiceUtils.getName(username);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setUsername(checkedUsername);
		task.setType(EMAIL_TYPE.CONFIRMATION);
		
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void changeEmail(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email, username);
		String checkedEmail = ServiceUtils.getEmail(email);
		String checkedUsername = ServiceUtils.getName(username);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setUsername(checkedUsername);
		task.setType(EMAIL_TYPE.CHANGE);
		
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void offerEmail(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email);
		String checkedEmail = ServiceUtils.getEmail(email);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setType(EMAIL_TYPE.OFFER);
		if(username != null) {
			task.setUsername(username);
		}
		/* Disabled feature
		 * TODO: Determine if this is actually useful
		 */
		/*
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}*/
		
	}
	
	@Override
	public void delete(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email, username);
		String checkedEmail = ServiceUtils.getEmail(email);
		String checkedUsername = ServiceUtils.getName(username);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setUsername(checkedUsername);
		task.setType(EMAIL_TYPE.DELETE);
		
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void emailComplaint(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		userService.addPendingActions(id, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
	}
	
	@Override
	public void emailBounce(ObjectId id) throws ServiceException {
		ArgCheck.nullCheck(id);
		userService.addPendingActions(id, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
	}
	
	@Override
	public void changePaymentId(String email, String username) throws ServiceException {
		ArgCheck.nullCheck(email, username);
		String checkedEmail = ServiceUtils.getEmail(email);
		String checkedUsername = ServiceUtils.getName(username);
		
		EmailTask task = new EmailTask();
		task.setTarget(checkedEmail);
		task.setUsername(checkedUsername);
		task.setType(EMAIL_TYPE.PAYMENT_CHANGE);
		
		try {
			emailDao.save(task);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void cleanupCompleted() throws ServiceException {
		try {
			emailDao.cleanupTasks(null, null, TASK_STATE.COMPLETE, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void cleanupErrors() throws ServiceException {
		try {
			emailDao.cleanupTasks(null, null, TASK_STATE.ERROR, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void sendEmails() throws ServiceException {
		// retrieve unsent emails
		// do not loop multiple pages, instead simply call this more often if needed
		Pageable pageable = new PageRequest(0, emailsPageSize);
		try {
			Page<EmailTask> tasks = emailDao.findNonCompleteTasks(
				pageable, TASK_STATE.INITIAL, null);
		
			if(tasks == null) {
				throw new NotFoundException("");
			}
			// for each unsent email, generate nonce, update user, send email, then update email
			for(EmailTask task : tasks.getContent()) {
				handleTask(task);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void handleTask(EmailTask task) throws ServiceException {
		String emailToken = clientManager.generateEmailToken(task);
		String hashedEmailToken = authService.hashToken(emailToken);
		
		String email = task.getTarget();
		
		try {
			// in some cases, there is no email token, such as for an offer
			if(emailToken != null) {
				User user = userService.findUserByEmail(email);
				userService.addEmailToken(user.getId(), hashedEmailToken, task.getType());
				logger.debug("Email token added for user id {" + user.getId() + "} with email token: " + emailToken + " (" + hashedEmailToken + ").");
			}
		} catch(NotFoundException nfe) {
			// do nothing, the user may not be registered, for some tasks this is acceptable
		} catch(Exception e) {
			try {
				emailDao.updateTask(task.getId(), new Date(), TASK_STATE.ERROR);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
			throw new ServiceException(e);
		}
		
		try {
			clientManager.sendEmail(getDetails(task), task, task.getUsername(), emailToken);
		} catch(ServiceException se) {
			try {
				emailDao.updateTask(task.getId(), new Date(), TASK_STATE.ERROR);
			} catch(DaoException de) {
				throw se;
			}
			throw se;
		}
			
		try {
			emailDao.updateTask(task.getId(), new Date(), TASK_STATE.COMPLETE);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.debug("Email of type (" + task.getType() 
				+ ") successfully sent to email (" + email + ").");
	}
	
	@Override
	public void checkEmailBounces() throws ServiceException {
		checkInvalidEmails(sqsManager.getBouncedEmails());
	}
	
	@Override
	public void checkEmailComplaints() throws ServiceException {
		checkInvalidEmails(sqsManager.getComplaintEmails());
	}
	
	protected void checkInvalidEmails(List<String> emails) throws ServiceException {
		if(emails == null) {
			throw new BadParameterException();
		}
		ServiceException lastException = null;
		for(String email : emails) {
			try {
				ObjectId id = userService.findUserIdByEmail(email);
				userService.addPendingActions(id, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
				//userService.addOverrideRole(id, OverrideRoleNames.UNCONFIRMED);
			} catch(NotFoundException nfe) {
				// do nothing. this implies it was already changed
			} catch(ServiceException se) {
				lastException = se;
			}
		}
		if(lastException != null) {
			throw lastException;
		}
	}
	
	/*
	protected void sendEmail(EmailTask task, String username, String emailToken) 
			throws ServiceException {
		EmailDetails details = getDetails(task);
		if(details == null) {
			throw new BadParameterException();
		}
		
		SimpleMailMessage message = createMailMessage(task);
		message.setFrom(details.getFrom());
		message.setReplyTo(details.getReplyTo());
		message.setSubject(details.getSubject());
		
		String link = details.getLink();
		if(emailToken != null && !emailToken.isEmpty()) {
			link = link + "?" + ParamNames.EMAIL_TOKEN + "=" + emailToken;
		}
		
		String text = details.getText();
		text = EmailParser.replaceUsername(text, username);
		text = EmailParser.replaceLink(text, link);
		
		message.setText(text);
		
		message.setTo(task.getTarget());
		message.setSentDate(new Date());
		
		// TODO: actually send email
		mailSender.send(message);
	}
	*/
	
	protected SimpleMailMessage createMailMessage(EmailTask task) throws ServiceException {
		ArgCheck.nullCheck(task);
		SimpleMailMessage message = new SimpleMailMessage();
		return message;
	}
	
	protected EmailDetails getDetails(EmailTask task) throws ServiceException {
		ArgCheck.nullCheck(task);
		EMAIL_TYPE type = task.getType();
		if(type == EMAIL_TYPE.CONFIRMATION) {
			return confirmationDetails;
		} else if(type == EMAIL_TYPE.RESET) {
			return passwordResetDetails;
		} else if(type == EMAIL_TYPE.CHANGE) {
			return changeEmailDetails;
		} else if(type == EMAIL_TYPE.OFFER) {
			return offerDetails;
		} else if(type == EMAIL_TYPE.DELETE) {
			return deleteDetails;
		} else if(type == EMAIL_TYPE.PAYMENT_CHANGE) {
			return changePaymentDetails;
		} else {
			return null;
		}
	}

	public void setConfirmationDetails(EmailDetails confirmationDetails) {
		this.confirmationDetails = confirmationDetails;
	}

	public void setPasswordResetDetails(EmailDetails passwordResetDetails) {
		this.passwordResetDetails = passwordResetDetails;
	}

	public void setChangeEmailDetails(EmailDetails changeEmailDetails) {
		this.changeEmailDetails = changeEmailDetails;
	}

	public void setOfferDetails(EmailDetails offerDetails) {
		this.offerDetails = offerDetails;
	}
	
	public void setDeleteDetails(EmailDetails deleteDetails) {
		this.deleteDetails = deleteDetails;
	}
	
	public void setChangePaymentDetails(EmailDetails changePaymentDetails) {
		this.changePaymentDetails = changePaymentDetails;
	}
	
	public void setEmailsPageSize(Integer emailsPageSize) {
		this.emailsPageSize = emailsPageSize;
	}
}
