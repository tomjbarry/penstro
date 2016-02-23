package com.py.py.service.mail;

public interface EmailDetails {
	
	public String getFrom();
	
	public String getReplyTo();
	
	public String getSubject();
	
	public String getText();
	
	public String getLink();
}
