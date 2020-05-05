package com.rest.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 
 * Class for representating an Authentication packet
 * 
 * bytes representation: 0000AUTHuser@@pass
 * 
 * @author gonza
 *
 */
public class CreaPacket implements Packet {
	private String user;
	private String password;
	
	public CreaPacket(String user, String password) {
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
		byte[] com = Packet.CREA_BYTES;
		byte[] args = (user + ARGUMENT_SEPARATOR + password).getBytes(StandardCharsets.ISO_8859_1);
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
		return PacketType.CREA;
	}
}
