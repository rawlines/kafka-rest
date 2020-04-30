package com.rest.net;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * 
 * Class representation of a Produce packet
 * 
 * bytes representation: 0000PRODtopic@@content
 * 
 * @author gonza
 *
 */
public class ProducePacket implements Packet {
	private String topic;
	private byte[] content;
	
	public ProducePacket(String topic, byte[] content) {
		this.topic = topic;
		this.content = content;
	}
	
	public String getTopic() {
		return this.topic;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	@Override
	public byte[] toBytes() {
		byte[] comm = Packet.PROD_BYTES;
		byte[] separator = Packet.ARGUMENT_SEPARATOR.getBytes(StandardCharsets.ISO_8859_1);
		byte[] topic = this.topic.getBytes(StandardCharsets.UTF_8);
		
		int fullLength = Integer.BYTES + comm.length + topic.length + separator.length + content.length;
		int packetLength = fullLength - Integer.BYTES;
		
		ByteBuffer buff = ByteBuffer.allocate(fullLength);
		buff.putInt(packetLength);
		buff.put(comm);
		buff.put(topic);
		buff.put(separator);
		buff.put(content);
		
		return buff.array();
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.PROD;
	}
}
