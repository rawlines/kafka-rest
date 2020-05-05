package com.rest;

import java.net.Socket;

import javax.net.ssl.SSLServerSocket;

import com.rest.utils.SSLUtils;

public class Rest {
	private boolean stop = false;
	private SSLServerSocket ssocket;
	
	public Rest(int port) {
		try {
			ssocket = SSLUtils.getServerSocket(8081);
			
			System.out.println("Initialized server on: " + ssocket.getInetAddress() + ":" + port);
		} catch (Exception e) {
			System.out.println("Error initializing main socket");
			e.printStackTrace();
			return;
		}
		
		listen();
	}
	
	private void listen() {
		System.out.println("Listening for new connections...");
		while (!stop) {
			try {
				Socket socket = ssocket.accept();
				
				//Give each connection its own thread
				new Thread(new SocketCommunicationRunnable(socket), "main-" + socket.getInetAddress()).start();
			} catch (Exception e) {
				System.out.println("Error while listening for connection");
				e.printStackTrace();
				continue;
			}
			
			System.gc();
		}
	}
	
	public static void main(String args[]) {
		new Rest(8081);
	}
}
