package com.rest;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.CommandParseException;
import com.rest.kafka.ConsumerThread;
import com.rest.kafka.ProducerThread;
import com.rest.net.AcknPacket;
import com.rest.net.AuthPacket;
import com.rest.net.Packet;
import com.rest.net.Packet.PacketType;
import com.rest.net.PacketReader;
import com.rest.net.PacketWriter;
import com.rest.net.ProducePacket;

public class SocketCommunicationRunnable implements Runnable {
	private Socket socket;
	
	private PacketReader pReader;
	private PacketWriter pWriter;
	
	private Queue<Packet> consumerQueue = new LinkedList<>();
	private Queue<Packet> producerQueue = new LinkedList<>();
	
	private Thread consumer;
	private Thread producer;
	
	private AuthPacket auth;
	
	private class KeepAliveThread implements Runnable {
		@Override
		public void run() {
		}
	}
	
	public SocketCommunicationRunnable(Socket socket) {
		this.socket = socket;
	}

	public void prepareEnvironment() throws IOException {
		pReader = new PacketReader(socket.getInputStream());
		pWriter = new PacketWriter(socket.getOutputStream());
	}
	
	public void waitForAuth() throws ArgumentParseException, IOException, CommandParseException {
		System.out.println("Waiting for auth...");
		auth = (AuthPacket) pReader.readPacket();
		System.out.println("...Auth packet receibed: user: " + auth.getUser());
	}
	
	public void talkWithClient() throws Exception {
		consumer = new Thread(new ConsumerThread(auth.getUser(), auth.getPassword(), consumerQueue, pWriter), "consumer-" + auth.getUser());
		producer = new Thread(new ProducerThread(auth.getUser(), auth.getPassword(), producerQueue, pWriter), "producer-" + auth.getUser());
		
		consumer.start();
		producer.start();
		new Thread(new KeepAliveThread(), "keepAlive-" + auth.getUser()).start();
		
		while (consumer.isAlive() && producer.isAlive()) {
			Packet packet = pReader.readPacket();
			
			switch (packet.getPacketType()) {
				case ACKN:
					acknPacket((AcknPacket) packet);
					break;
				case PROD:
					prodPacket((ProducePacket) packet);
					break;
				default:
					//default
					break;
			}
		}
	}
	
	private void prodPacket(ProducePacket p) throws IllegalMonitorStateException {
		synchronized (producerQueue) {
			producerQueue.add(p);
			producerQueue.notify();
		}
	}
	
	private void acknPacket(AcknPacket p) throws IllegalMonitorStateException {
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
		}
		
		//KILL THE SESSION
		try {
			consumer.interrupt();
		} catch (Exception e) {}
			
		try {
			producer.interrupt();
		} catch (Exception e) {}
		
		try {
			pReader.close();
		} catch (Exception e) {}
		
		try {
			pWriter.close();
		} catch (Exception e) {}
		
		System.out.println("Well thats all folks");
		System.gc();
	}
}
