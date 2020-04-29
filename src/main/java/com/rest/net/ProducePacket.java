package com.rest.net;

import com.rest.net.Packet.Command;

public class ProducePacket extends Packet {
	protected ProducePacket() {
		super(Command.PRODUCE);
	}

}
