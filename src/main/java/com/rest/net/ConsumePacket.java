package com.rest.net;

import com.rest.net.Packet.Command;

public class ConsumePacket extends Packet {
	public ConsumePacket() {
		super(Command.CONSUME);
	}
}
