package com.rest.exceptions;

public class ArgumentParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public ArgumentParseException(String s) {
		super("Error parsing arguments: " + s);
	}
}
