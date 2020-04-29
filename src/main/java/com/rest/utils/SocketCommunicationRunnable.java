package com.rest.utils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

import com.rest.kafka.ConsumerThread;
import com.rest.kafka.ProducerThread;
import com.rest.net.AuthPacket;
import com.rest.net.Packet;
import com.rest.net.PacketReader;
import com.rest.net.PacketReader.ArgumentParseException;
import com.rest.net.PacketReader.CommandParseEsception;

public class SocketCommunicationRunnable implements Runnable {
	private Socket socket;
	private PacketReader pReader;
	private DataOutputStream dos;
	private Queue<String> consumerQueue = new LinkedList<>();
	private Queue<String> producerQueue = new LinkedList<>();
	
	private AuthPacket auth;
	
	public SocketCommunicationRunnable(Socket socket) {
		this.socket = socket;
	}

	public void prepareEnvironment() throws IOException {
		pReader = new PacketReader(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());
	}
	
	public void waitForAuth() throws ArgumentParseException, IOException, CommandParseEsception {
		System.out.println("Auth packet receibed");
		auth = (AuthPacket) pReader.readPacket();
	}
	
	public synchronized void talkWithClient() throws Exception {
		//Runnable consumer = new ConsumerThread(auth.getUser(), auth.getPassword(), queue);
		Thread consumer = new Thread(new ConsumerThread("client1", "1234567890qw", consumerQueue), "consumer-" + auth.getUser());
		Thread producer = new Thread(new ProducerThread("client1", "1234567890qw", producerQueue), "producer-" + auth.getUser());
		
		while (true) {
			Packet packet = pReader.readPacket();
			
			//notify consumer or producer
		}
	}
	
	@Override
	public void run() {
		System.out.println("...New connection receibed from:" + socket.getInetAddress());
		try {
			prepareEnvironment();
			//waitForAuth();
			talkWithClient();
		} catch (Exception e) {
			System.out.println("Error on connection thread: " + Thread.currentThread().getName());
			e.printStackTrace();
			return;
		}
	}
}
