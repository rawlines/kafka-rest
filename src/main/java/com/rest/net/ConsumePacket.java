package com.rest.net;

import java.nio.ByteBuffer;

/**
 * 
 * Class representation of a Consume packet
 * 
 * bytes representation: 0000CONScontent
 * 
 * @author gonza
 *
 */
public class ConsumePacket implements Packet {
	private final byte[] command = new byte[] {'C', 'O', 'N', 'S'};
	private byte[] content;
	
	public ConsumePacket(byte[] content) {
		this.content = content;
	}
	
	@Override
	public byte[] toBytes() {
		int fullLength = Integer.BYTES + command.length + content.length;
		int packetLength = fullLength - Integer.BYTES;
		
		ByteBuffer byteBuffer = ByteBuffer.allocate(fullLength);
		byteBuffer.putInt(packetLength);
		byteBuffer.put(command);
		byteBuffer.put(content);
		
		return byteBuffer.array();
	}
	
	public byte[] getContent() {
		return this.content;
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.CONS;
	}
}
