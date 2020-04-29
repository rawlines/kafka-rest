package com.rest.net;

import java.nio.charset.Charset;
import java.util.Arrays;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.CommandParseException;

public interface Packet {
	enum PacketType {
		AUTH, PROD, CONS, ACKN
	}
	
	String ARGUMENT_SEPARATOR = "@@"; 
	
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
	
	public static Packet fromBytes(byte[] bytes) throws ArgumentParseException, CommandParseException {
		byte[] com = Arrays.copyOfRange(bytes, 0, 4);
		byte[] bargs = Arrays.copyOfRange(bytes, 4, bytes.length);
			
		String command = new String(com, Charset.forName("UTF-8"));
		
		if (command.equals("AUTH")) {
			
			String args = new String(bargs, Charset.forName("UTF-8"));
			String[] tokens = args.split(ARGUMENT_SEPARATOR);
			
			if (tokens.length != 2)
				throw new ArgumentParseException("Invalid arguments");
			
			return new AuthPacket(tokens[0], tokens[1]);
			
		} else if (command.equals("CONS")) {
			
			return parseCons(bargs);
			
		} else if (command.equals("PROD")) {
			
			return parseProd(bargs);
			
		} else if (command.equals("ACKN")) {
			
			return new AcknPacket(getTypeFromBytes(bargs));
			
		}
		
		throw new CommandParseException("Cannot determine command type");
	}
	
	static ConsumePacket parseCons(byte[] bargs) {
		return new ConsumePacket(bargs);
	}
	
	static ProducePacket parseProd(byte[] bargs) {
		return null;
	}
}
