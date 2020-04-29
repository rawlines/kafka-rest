package com.rest.exceptions;

public class CommandParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public CommandParseException(String s) {
		super("Error parsing command: " + s);
	}
}