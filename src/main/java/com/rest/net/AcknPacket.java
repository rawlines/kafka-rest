package com.rest.net;

import java.nio.ByteBuffer;

public class AcknPacket implements Packet {
	private PacketType command;
	
	public AcknPacket(PacketType command) {
		this.command = command;
	}
	
	public PacketType getCommand() {
		return this.command;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] com = Packet.ACKN_BYTES;
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
		return PacketType.ACKN;
	}
}
