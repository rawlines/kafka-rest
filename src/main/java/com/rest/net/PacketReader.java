package com.rest.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.PacketParseException;

public class PacketReader extends DataInputStream {	
	public PacketReader(InputStream in) {
		super(in);
	}
	
	
	/**
	 * Reads a packet, this method block the thread until the whole aviable packet is readed.
	 * 
	 * @return
	 * @throws IOException - if IO error occurs
	 * @throws PacketParseEsception - if packet type could not be determined
	 * @throws ArgumentParseException - if arguments are not well formatted
	 */
	public Packet readPacket() throws ArgumentParseException, IOException, PacketParseException {
		int packetSize = readInt();
		
		byte[] buff = readNBytes(packetSize);
		
		return Packet.fromBytes(buff);
	}
	
	private byte[] readNBytes(int num) throws IOException {
		byte[] buff = new byte[num];
		
		for (int i = 0; i < buff.length; i++) {
			buff[i] = readByte();
		}
		
		return buff;
	}
}
