package com.py.py.service.mail;

import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.py.py.constants.ParamNames;
import com.py.py.domain.EmailTask;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.service.aws.CredentialsManager;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;

public class ProductionEmailClientManager implements EmailClientManager {

	@Autowired
	protected CredentialsManager credentialsManager;
	
	protected AmazonSimpleEmailServiceClient client;
	
	@PostConstruct
	public void initialize() {
		client = new AmazonSimpleEmailServiceClient(credentialsManager.getCredentials());
		client.setRegion(credentialsManager.getRegion());
	}
	
	public void sendEmail(EmailDetails details, EmailTask task, String username, String emailToken) throws ServiceException {
		ArgCheck.nullCheck(details, task);
		if(client == null) {
			throw new BadParameterException();
		}
		
		Destination destination = new Destination().withToAddresses(new String[]{task.getTarget()});
		Content subject = new Content().withData(details.getSubject());

		String delim = "?";
		String link = details.getLink();
		if(emailToken != null && !emailToken.isEmpty()) {
			link = link + delim + ParamNames.EMAIL_TOKEN + "=" + emailToken;
			delim = "&";
		}
		if(task.getType() == EMAIL_TYPE.RESET) {
			link = link + delim + ParamNames.USER + "=" + username;
			delim = "&";
		}
		
		String text = details.getText();
		text = EmailParser.replaceUsername(text, username);
		text = EmailParser.replaceLink(text, link);
		
		Content textBody = new Content().withData(text);
		Body body = new Body().withText(textBody);
		
		Message message = new Message().withSubject(subject).withBody(body);
		
		SendEmailRequest request = new SendEmailRequest().withSource(details.getFrom()).withDestination(destination).withMessage(message);
		
		try {
			client.sendEmail(request);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	public String generateEmailToken(EmailTask task) throws ServiceException {
		ArgCheck.nullCheck(task);
		EMAIL_TYPE type = task.getType();
		if(type == EMAIL_TYPE.OFFER) {
			return null;
		} else {
			String uuid = UUID.randomUUID().toString();
			return uuid.replace("-", "");
		}
	}
	
	public void setClient(AmazonSimpleEmailServiceClient client) {
		this.client = client;
		if(client != null) {
			client.setRegion(credentialsManager.getRegion());
		}
	}
}
