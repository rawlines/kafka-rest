package com.rest;

import java.net.Socket;

import javax.net.ssl.SSLServerSocket;

import com.rest.utils.SSLUtils;
import com.rest.utils.SocketCommunicationRunnable;

public class Rest {
	private boolean stop = false;
	private SSLServerSocket ssocket;
	
	public Rest(int port) {
		try {
			ssocket = SSLUtils.getServerSocket(8081);
			
			System.out.println("Initialized server on: " + ssocket.getInetAddress().toString() + ":" + port);
		} catch (Exception e) {
			System.out.println("Error initializing main socket");
			e.printStackTrace();
			return;
		}
		
		listen();
	}
	
	private void listen() {
		while (!stop) {
			try {
				System.out.println("Listening for new connection...");
				Socket socket = ssocket.accept();
				
				new Thread(new SocketCommunicationRunnable(socket)).start(); //Give the connection his own thread
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
