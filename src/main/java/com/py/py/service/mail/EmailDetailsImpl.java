package com.py.py.service.mail;

public class EmailDetailsImpl implements EmailDetails {

	protected String from;
	protected String replyTo;
	protected String subject;
	protected String text;
	protected String link;
	
	public EmailDetailsImpl() {
	}
	
	@Override
	public String getFrom() {
		return from;
	}

	@Override
	public String getReplyTo() {
		return replyTo;
	}

	@Override
	public String getSubject() {
		return subject;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public String getLink() {
		return link;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void setLink(String link) {
		this.link = link;
	}
}
