package com.rest.net;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.PacketParseException;

public interface Packet {
	enum PacketType {
		AUTH, PROD, CONS, ACKN, KEEP
	}
	
	String ARGUMENT_SEPARATOR = new String(new byte[] {'@', '@'}, StandardCharsets.ISO_8859_1); 
	
	byte[] AUTH_BYTES = new byte[] {'A', 'U', 'T', 'H'};
	byte[] PROD_BYTES = new byte[] {'P', 'R', 'O', 'D'};
	byte[] CONS_BYTES = new byte[] {'C', 'O', 'N', 'S'};
	byte[] ACKN_BYTES = new byte[] {'A', 'C', 'K', 'N'};
	byte[] KEEP_BYTES = new byte[] {'K', 'E', 'E', 'P'};
	
	byte[] toBytes();
	PacketType getPacketType();
	
	static byte[] getBytesFromType(PacketType c) {
		byte[] b = null;
		
		switch (c) {
			case KEEP:
				b = KEEP_BYTES;
				break;
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
		if (Arrays.equals(b, KEEP_BYTES)) {
			
			return PacketType.KEEP;
			
		} else if (Arrays.equals(b, AUTH_BYTES)) {
			
			return PacketType.AUTH;
			
		} else if (Arrays.equals(b, PROD_BYTES)) {
			
			return PacketType.PROD;
			
		} else if (Arrays.equals(b, CONS_BYTES)) {
			
			return PacketType.CONS;
			
		} else if (Arrays.equals(b, ACKN_BYTES)) {
			
			return PacketType.ACKN;
			
		}
		
		throw new CommandParseException("Invalid type");
	}
	
	static Packet fromBytes(byte[] bytes) throws ArgumentParseException, PacketParseException {
		byte[] com = Arrays.copyOfRange(bytes, 0, 4);
		byte[] bargs = Arrays.copyOfRange(bytes, 4, bytes.length);
			
		PacketType type = getTypeFromBytes(com);
		Packet p = null;
		switch (type) {
			case KEEP:
				p = new KeepAlivePacket();
				break;
	
			case AUTH:
				String aargs = new String(bargs, StandardCharsets.ISO_8859_1);
				String[] atokens = aargs.split(ARGUMENT_SEPARATOR);
				
				if (atokens.length != 2)
					throw new ArgumentParseException("Invalid arguments");
				
				p = new AuthPacket(atokens[0], atokens[1]);
				break;
				
			case CONS:
				p = new ConsumePacket(bargs);
				break;
				
			case PROD:
				String pargs = new String(bargs, StandardCharsets.ISO_8859_1);
				String[] ptokens = pargs.split(ARGUMENT_SEPARATOR);
				
				if (ptokens.length != 2)
					throw new ArgumentParseException("Invalid arguments");
				
				p = new ProducePacket(ptokens[0], ptokens[1].getBytes(StandardCharsets.ISO_8859_1));
				break;
				
			case ACKN:
				p = new AcknPacket(getTypeFromBytes(bargs));
				break;
				
			default:
				throw new PacketParseException("Cannot determine command type");
		}
		
		return p;
	}
}
