package com.rest.net;

import com.rest.net.Packet.Command;

public class AuthPacket extends Packet {
	private String user;
	private String password;
	
	protected AuthPacket(String user, String password) {
		super(Command.AUTH);
		
		this.user = user;
		this.password = password;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.password;
	}
}
