package com.rest.net;

import java.nio.ByteBuffer;

/**
 * 
 * Representation of an Acknowledge packet
 * 
 * bytes representation: 0000ACKNcomm (where comm is the command to ACK)
 * 
 * @author gonza
 *
 */
public class DenyPacket implements Packet {
	private PacketType command;
	
	public DenyPacket(PacketType command) {
		this.command = command;
	}
	
	public PacketType getCommand() {
		return this.command;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] com = Packet.DENY_BYTES;
		byte[] arg = Packet.getBytesFromType(command);
		int fullLength = Integer.BYTES + com.length + arg.length;
		int packetLength = fullLength - Integer.BYTES;
		
		ByteBuffer bf = ByteBuffer.allocate(fullLength);
		bf.putInt(packetLength);
		bf.put(com);
		bf.put(arg);
		
		return bf.array();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.DENY;
	}
}
