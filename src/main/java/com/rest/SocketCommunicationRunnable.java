package com.rest;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.CommandParseException;
import com.rest.kafka.ConsumerThread;
import com.rest.net.AcknPacket;
import com.rest.net.AuthPacket;
import com.rest.net.Packet;
import com.rest.net.Packet.PacketType;
import com.rest.net.PacketReader;

public class SocketCommunicationRunnable implements Runnable {
	private Socket socket;
	private PacketReader pReader;
	private Queue<Packet> consumerQueue = new LinkedList<>();
	private Queue<Packet> producerQueue = new LinkedList<>();
	
	Thread consumer;
	Thread producer;
	
	private AuthPacket auth;
	
	public SocketCommunicationRunnable(Socket socket) {
		this.socket = socket;
	}

	public void prepareEnvironment() throws IOException {
		pReader = new PacketReader(socket.getInputStream());
	}
	
	public void waitForAuth() throws ArgumentParseException, IOException, CommandParseException {
		System.out.println("Waiting for auth...");
		auth = (AuthPacket) pReader.readPacket();
		System.out.println("...Auth packet receibed: user: " + auth.getUser());
	}
	
	public void talkWithClient() throws Exception {
		consumer = new Thread(new ConsumerThread(auth.getUser(), auth.getPassword(), consumerQueue, socket.getOutputStream()), "consumer-" + auth.getUser());
		//Thread producer = new Thread(new ProducerThread("client1", "1234567890qw", producerQueue), "producer-" + auth.getUser());
		
		consumer.start();
		while (true) {
			Packet packet = pReader.readPacket();
			
			if (packet.getPacketType() == PacketType.ACKN) {
				acknPacket((AcknPacket) packet); 
			}
		}
	}
	
	private void acknPacket(AcknPacket p) {
		synchronized (consumerQueue) {
			PacketType c = p.getCommand();
			if (c == PacketType.CONS) {
				consumerQueue.add(p);
				consumerQueue.notify();
			}
		}
	}
	
	@Override
	public void run() {
		System.out.println("...New connection receibed from:" + socket.getInetAddress());
		try {
			prepareEnvironment();
			waitForAuth();
			talkWithClient();
		} catch (Exception e) {
			System.out.println("Error on connection thread: " + Thread.currentThread().getName());
			e.printStackTrace();
		} finally {
			consumer.interrupt();
		}
		System.out.println("Well thats all folks");
		
		System.gc();
	}
}
