package com.rest.net;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.CommandParseException;

public interface Packet {
	enum PacketType {
		AUTH, PROD, CONS, ACKN
	}
	
	String ARGUMENT_SEPARATOR = new String(new byte[] {'@', '@'}, StandardCharsets.ISO_8859_1); 
	
	byte[] AUTH_BYTES = new byte[] {'A', 'U', 'T', 'H'};
	byte[] PROD_BYTES = new byte[] {'P', 'R', 'O', 'D'};
	byte[] CONS_BYTES = new byte[] {'C', 'O', 'N', 'S'};
	byte[] ACKN_BYTES = new byte[] {'A', 'C', 'K', 'N'};
	
	byte[] toBytes();
	PacketType getPacketType();
	
	static byte[] getBytesFromType(PacketType c) {
		byte[] b = null;
		
		switch (c) {
			case AUTH:
				b = AUTH_BYTES;
				break;
			case PROD:
				b = PROD_BYTES;
				break;
			case CONS:
				b = CONS_BYTES;
				break;
			case ACKN:
				b = ACKN_BYTES;
				break;
		}
		
		return b;
	}
	
	static PacketType getTypeFromBytes(byte[] b) {
		if (Arrays.equals(b, AUTH_BYTES)) {
			
			return PacketType.AUTH;
			
		} else if (Arrays.equals(b, PROD_BYTES)) {
			
			return PacketType.PROD;
			
		} else if (Arrays.equals(b, CONS_BYTES)) {
			
			return PacketType.CONS;
			
		} else if (Arrays.equals(b, ACKN_BYTES)) {
			
			return PacketType.ACKN;
			
		}
		
		return null;
	}
	
	static Packet fromBytes(byte[] bytes) throws ArgumentParseException, CommandParseException {
		byte[] com = Arrays.copyOfRange(bytes, 0, 4);
		byte[] bargs = Arrays.copyOfRange(bytes, 4, bytes.length);
			
		String command = new String(com, StandardCharsets.ISO_8859_1);
		
		if (command.equals("AUTH")) {
			
			String args = new String(bargs, StandardCharsets.ISO_8859_1);
			String[] tokens = args.split(ARGUMENT_SEPARATOR);
			
			if (tokens.length != 2)
				throw new ArgumentParseException("Invalid arguments");
			
			return new AuthPacket(tokens[0], tokens[1]);
			
		} else if (command.equals("CONS")) {
			
			return new ConsumePacket(bargs);
			
		} else if (command.equals("PROD")) {
			
			String args = new String(bargs, StandardCharsets.ISO_8859_1);
			String[] tokens = args.split(ARGUMENT_SEPARATOR);
			
			if (tokens.length != 2)
				throw new ArgumentParseException("Invalid arguments");
			
			return new ProducePacket(tokens[0], tokens[1].getBytes(StandardCharsets.ISO_8859_1));
			
		} else if (command.equals("ACKN")) {
			
			return new AcknPacket(getTypeFromBytes(bargs));
			
		}
		
		throw new CommandParseException("Cannot determine command type");
	}
}
