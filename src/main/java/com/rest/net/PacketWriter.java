package com.rest.net;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PacketWriter extends DataOutputStream {
	public PacketWriter(OutputStream os) {
		super(os);
	}
	
	public synchronized void sendPacket(Packet packet) throws IOException {
		write(packet.toBytes());
		flush();
	}
}
