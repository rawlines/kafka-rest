package com.rest.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.CommandParseException;

public class PacketReader extends DataInputStream {	
	public PacketReader(InputStream in) {
		super(in);
	}
	
	
	/**
	 * Reads a packet, this method block the thread until the whole aviable packet is readed.
	 * 
	 * @return
	 * @throws IOException - if IO error occurs
	 * @throws CommandParseEsception - if packet command could not be determined
	 * @throws ArgumentParseException - if arguments are not well formatted
	 */
	public Packet readPacket() throws ArgumentParseException, IOException, CommandParseException {
		int packetSize = readInt();
		
		byte[] buff = readNBytes(packetSize);
		
		return Packet.fromBytes(buff);
	}
	
	private byte[] readNBytes(int num) throws IOException {
		ByteBuffer buff = ByteBuffer.allocate(num);
		while (buff.hasRemaining()) {
			buff.put(readByte());
		}
		return buff.array();
	}
}
