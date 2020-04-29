package com.rest.net;

public abstract class Packet {
	public enum Command {
		CONSUME, PRODUCE, AUTH
	}
	
	private Command command;
	
	protected Packet(Command command) {
		this.command = command;
	}
	
	public Command getCommand() {
		return command;
	}
}
