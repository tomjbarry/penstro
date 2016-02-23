package com.py.py.service.mail;

public class EmailParser {

	private static final String usernameRegex = "<username>";
	private static final String linkRegex = "<link>";
	
	public static String replaceUsername(String text, String username) {
		return text.replaceFirst(usernameRegex, username);
	}
	
	public static String replaceLink(String text, String link) {
		return text.replaceFirst(linkRegex, link);
	}
	
}
