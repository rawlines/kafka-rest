package com.rest.exceptions;

public class UserExistsException extends Exception {
	private static final long serialVersionUID = 1L;

	public UserExistsException(String s) {
		super("Error parsing arguments: " + s);
	}
}
