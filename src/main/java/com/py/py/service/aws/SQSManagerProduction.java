package com.py.py.service.aws;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.py.py.service.exception.ServiceException;
import com.py.py.util.PyLogger;

public class SQSManagerProduction implements SQSManager {
	
	protected static final PyLogger logger = PyLogger.getLogger(SQSManagerProduction.class);
	
	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class SESRecipients {
		private String emailAddress;
		private String status;
		private String action;

		public String getEmailAddress() {
			return emailAddress;
		}

		public void setEmailAddress(String emailAddress) {
			this.emailAddress = emailAddress;
		}

		public String getAction() {
			return action;
		}

		public void setAction(String action) {
			this.action = action;
		}

		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class SESBounce {
		private List<SESRecipients> bouncedRecipients;
		private String timestamp;
		private String feedbackId;
		
		public String getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
		public String getFeedbackId() {
			return feedbackId;
		}
		public void setFeedbackId(String feedbackId) {
			this.feedbackId = feedbackId;
		}
		public List<SESRecipients> getBouncedRecipients() {
			return bouncedRecipients;
		}
		public void setBouncedRecipients(List<SESRecipients> bouncedRecipients) {
			this.bouncedRecipients = bouncedRecipients;
		}
		
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class SESComplaint {
		private String userAgent;
		private List<SESRecipients> complainedRecipients;
		private String complaintFeedbackType;
		private String timestamp;
		private String feedbackId;
		private String arrivalDate;
		public String getUserAgent() {
			return userAgent;
		}
		public void setUserAgent(String userAgent) {
			this.userAgent = userAgent;
		}
		public List<SESRecipients> getComplainedRecipients() {
			return complainedRecipients;
		}
		public void setComplainedRecipients(List<SESRecipients> complainedRecipients) {
			this.complainedRecipients = complainedRecipients;
		}
		public String getComplaintFeedbackType() {
			return complaintFeedbackType;
		}
		public void setComplaintFeedbackType(String complaintFeedbackType) {
			this.complaintFeedbackType = complaintFeedbackType;
		}
		public String getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
		public String getFeedbackId() {
			return feedbackId;
		}
		public void setFeedbackId(String feedbackId) {
			this.feedbackId = feedbackId;
		}
		public String getArrivalDate() {
			return arrivalDate;
		}
		public void setArrivalDate(String arrivalDate) {
			this.arrivalDate = arrivalDate;
		}
		
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class SESMail {
		private String timestamp;
		private String source;
		private String messageId;
		private List<String> destination;
		public String getSource() {
			return source;
		}
		public void setSource(String source) {
			this.source = source;
		}
		public String getMessageId() {
			return messageId;
		}
		public void setMessageId(String messageId) {
			this.messageId = messageId;
		}
		public List<String> getDestination() {
			return destination;
		}
		public void setDestination(List<String> destination) {
			this.destination = destination;
		}
		public String getTimestamp() {
			return timestamp;
		}
		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	protected static class SESNotification {
		private String notificationType;
		private SESMail mail;
		private SESBounce bounce;
		private SESComplaint complaint;
		public String getNotificationType() {
			return notificationType;
		}
		public void setNotificationType(String notificationType) {
			this.notificationType = notificationType;
		}
		public SESMail getMail() {
			return mail;
		}
		public void setMail(SESMail mail) {
			this.mail = mail;
		}
		public SESComplaint getComplaint() {
			return complaint;
		}
		public void setComplaint(SESComplaint complaint) {
			this.complaint = complaint;
		}
		public SESBounce getBounce() {
			return bounce;
		}
		public void setBounce(SESBounce bounce) {
			this.bounce = bounce;
		}
	}
	
	@Autowired
	protected CredentialsManager credentialsManager;
	
	protected AmazonSQS sqs;
	
	protected String bounceQueueName;
	protected String complaintQueueName;
	protected String messageEmailAttribute;
	
	protected GetQueueUrlResult bounceQueueUrl;
	protected GetQueueUrlResult complaintQueueUrl;
	
	protected ObjectMapper mapper = new ObjectMapper();
	
	@PostConstruct
	public void initialize() {
		sqs = new AmazonSQSClient(credentialsManager.getCredentials());
		sqs.setRegion(credentialsManager.getRegion());
		bounceQueueUrl = sqs.getQueueUrl(bounceQueueName);
		complaintQueueUrl = sqs.getQueueUrl(complaintQueueName);
	}
	
	@Override
	public List<String> getBouncedEmails() throws ServiceException {
		return getEmailsFromQueue(bounceQueueUrl.getQueueUrl());
	}
	
	@Override
	public List<String> getComplaintEmails() throws ServiceException {
		return getEmailsFromQueue(complaintQueueUrl.getQueueUrl());
	}
	
	protected List<String> getEmailFromMessage(String body) {
		if(body == null || body.isEmpty()) {
			return null;
		}
		try {
			Map<String, String> bodyMap = mapper.readValue(body, new TypeReference<HashMap<String, String> >(){});
			SESNotification notification = mapper.readValue(bodyMap.get("Message"), SESNotification.class);
			if(notification != null && notification.getMail() != null) {
				return notification.getMail().getDestination();
			}
		} catch(Exception e) {
			logger.info("Could not decode AWS SQS Message with body: {" + body + "}.", e);
		}
		return null;
	}
	
	protected List<String> getEmailsFromQueue(String queueUrl) throws ServiceException {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
		
		List<Message> messages;
		try {
			messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
		} catch(AmazonClientException ace) {
			throw new ServiceException(ace);
		}
		
		List<String> emails = new ArrayList<String>();
		List<DeleteMessageBatchRequestEntry> entries = new ArrayList<DeleteMessageBatchRequestEntry>();
		for(Message message : messages) {
			List<String> destinations = getEmailFromMessage(message.getBody());
			if(destinations != null) {
				for(String email : destinations) {
					emails.add(email);
				}
			}
			entries.add(new DeleteMessageBatchRequestEntry(message.getMessageId(), message.getReceiptHandle()));
		}
		if(!entries.isEmpty()) {
			try {
				sqs.deleteMessageBatch(queueUrl, entries);
			} catch(AmazonClientException ace) {
				throw new ServiceException(ace);
			}
		}
		return emails;
	}
	
	public void setBounceQueueName(String bounceQueueName) {
		this.bounceQueueName = bounceQueueName;
	}
	
	public void setComplaintQueueName(String complaintQueueName) {
		this.complaintQueueName = complaintQueueName;
	}

	public void setMessageEmailAttribute(String messageEmailAttribute) {
		this.messageEmailAttribute = messageEmailAttribute;
	}
	
}
