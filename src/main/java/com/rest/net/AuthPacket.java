package com.rest.net;

import java.nio.ByteBuffer;

public class AuthPacket implements Packet {
	private String user;
	private String password;
	
	public AuthPacket(String user, String password) {
		this.user = user;
		this.password = password;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getPassword() {
		return this.password;
	}

	@Override
	public byte[] toBytes() {
		byte[] com = Packet.AUTH_BYTES;
		byte[] args = (user + ARGUMENT_SEPARATOR + password).getBytes();
		int fullLength = Integer.BYTES + com.length + args.length;
		int packetLength = fullLength - Integer.BYTES;
		
		ByteBuffer buffer = ByteBuffer.allocate(fullLength);
		buffer.putInt(packetLength);
		buffer.put(com);
		buffer.put(args);
		
		return buffer.array();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.AUTH;
	}
}
