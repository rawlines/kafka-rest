package com.rest.exceptions;

public class PacketParseException extends Exception {
	private static final long serialVersionUID = 1L;

	public PacketParseException(String s) {
		super("Error parsing command: " + s);
	}
}