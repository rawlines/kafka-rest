package com.rest.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import com.rest.net.AuthPacket;
import com.rest.net.PacketReader;
import com.rest.net.PacketReader.ArgumentParseException;
import com.rest.net.PacketReader.CommandParseEsception;

public class SocketCommunicationRunnable implements Runnable {
	private Socket socket;
	private PacketReader pReader;
	private DataOutputStream dos;
	
	public SocketCommunicationRunnable(Socket socket) {
		this.socket = socket;
	}

	public void prepareEnvironment() throws IOException {
		pReader = new PacketReader(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}
	
	public void waitForAuth() throws ArgumentParseException, IOException, CommandParseEsception {
		AuthPacket auth = (AuthPacket) pReader.readPacket();
		
	}
	
	public void talkWithClient() throws IOException, InterruptedException {
		System.out.println("...Connection receibed, communication with:" + socket.getInetAddress());
		for (int i = 0; i < 10; i++) {
			dos.write(i);
			System.out.println("Sent:" + i);
			Thread.sleep(1000);
		}
	}
	
	@Override
	public void run() {
		try {
			prepareEnvironment();
			waitForAuth();
			talkWithClient();
		} catch (Exception e) {
			System.out.println("Error on connection thread: " + Thread.currentThread().getName());
			e.printStackTrace();
			return;
		}
	}
}
