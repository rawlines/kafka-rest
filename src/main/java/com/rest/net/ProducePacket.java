package com.rest.net;

public class ProducePacket implements Packet {

	@Override
	public byte[] toBytes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PacketType getPacketType() {
		return PacketType.PROD;
	}
}
