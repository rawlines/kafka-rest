package com.rest.net;

import java.nio.ByteBuffer;

/**
 * 
 * Class that represents a KeepAlive packet
 * 
 * bytes representation: 0000KEEP
 * 
 * @author gonza
 *
 */
public class KeepAlivePacket implements Packet {
	public KeepAlivePacket() {}
	
	@Override
	public byte[] toBytes() {
		int fullLength = Integer.BYTES + KEEP_BYTES.length;
		int packetLength = fullLength - Integer.BYTES;
		
		ByteBuffer buff = ByteBuffer.allocate(fullLength);
		buff.putInt(packetLength);
		buff.put(KEEP_BYTES);
		
		return buff.array();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.KEEP;
	}
}
