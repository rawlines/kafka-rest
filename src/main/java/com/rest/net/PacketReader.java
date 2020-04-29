package com.rest.net;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import com.rest.net.Packet.Command;

public class PacketReader extends DataInputStream {
	public static class ArgumentParseException extends Exception {
		private static final long serialVersionUID = 1L;

		public ArgumentParseException(String s) {
			super("Error parsing arguments: " + s);
		}
	}
	
	public static class CommandParseEsception extends Exception {
		private static final long serialVersionUID = 1L;

		public CommandParseEsception(String s) {
			super("Error parsing command: " + s);
		}
	}
	
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
	public Packet readPacket() throws ArgumentParseException, IOException, CommandParseEsception {
		int packetSize = read();
		byte[] buff = readNBytes(packetSize);
		byte[] com = Arrays.copyOfRange(buff, 0, 3);
		byte[] args = Arrays.copyOfRange(buff, 3, buff.length);
		
		return parsePacket(com, args);
	}
	
	private AuthPacket parseAuth(byte[] bargs) throws ArgumentParseException {
		String args = new String(bargs);
		String[] tokens = args.split("@@");
		
		if (tokens.length != 2)
			throw new ArgumentParseException("Argumentos no correctos");
		
		return new AuthPacket(tokens[0], tokens[1]);
			
	}
	
	private ConsumePacket parseCons(byte[] bargs) {
		return null;
	}
	
	private ProducePacket parseProd(byte[] bargs) {
		return null;
	}
	
	private Packet parsePacket(byte[] com, byte[] args) throws CommandParseEsception, ArgumentParseException {
		String s = new String(com, Charset.forName("UTF-8"));
		
		if (s.equals("AUTH")) {
			
			return parseAuth(args);
			
		} else if (s.equals("CONS")) {
			
			return parseCons(args);
			
		} else if (s.equals("PROD")) {
			
			return parseProd(args);
			
		}
		
		throw new CommandParseEsception("Cannot determine command type");
	}
}
