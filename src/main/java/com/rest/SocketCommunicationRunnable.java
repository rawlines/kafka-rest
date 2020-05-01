package com.rest;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.rest.exceptions.ArgumentParseException;
import com.rest.exceptions.PacketParseException;
import com.rest.kafka.ConsumerThread;
import com.rest.kafka.ProducerThread;
import com.rest.net.AcknPacket;
import com.rest.net.AuthPacket;
import com.rest.net.KeepAlivePacket;
import com.rest.net.Packet;
import com.rest.net.Packet.PacketType;
import com.rest.net.PacketReader;
import com.rest.net.PacketWriter;
import com.rest.net.ProducePacket;

public class SocketCommunicationRunnable implements Runnable {
	private static short KEEP_ALIVE_MILLIS = 10000;
	
	private Socket socket;
	
	private PacketReader pReader;
	private PacketWriter pWriter;
	
	private final Queue<Packet> consumerQueue = new LinkedList<>();
	private final Queue<Packet> producerQueue = new LinkedList<>();
	private final ConcurrentLinkedQueue<KeepAlivePacket> keepaliveQueue = new ConcurrentLinkedQueue<>();
	
	private Thread consumer;
	private Thread producer;
	private Thread keepaliveThread;
	
	private AuthPacket auth;
	private final KeepAlivePacket keepAlive = new KeepAlivePacket();
	
	private class KeepAliveThread implements Runnable {
		@Override
		public void run() {
			try {
				while (!Thread.interrupted()) {
					Thread.sleep(KEEP_ALIVE_MILLIS);
					if (keepaliveQueue.poll() == null)
						break;
					System.out.println("KEEP");
				}
				System.out.println("Quitting because no signal was receibed from client");
			} catch (Exception e) {}
			
			try {
				pReader.close();
			} catch (Exception e) {}
			
			System.gc();
		}
	}
	
	public SocketCommunicationRunnable(Socket socket) {
		this.socket = socket;
	}

	public void prepareEnvironment() throws IOException {
		pReader = new PacketReader(socket.getInputStream());
		pWriter = new PacketWriter(socket.getOutputStream());
	}
	
	public void waitForAuth() throws ArgumentParseException, IOException, PacketParseException {
		System.out.println("Waiting for auth...");
		auth = (AuthPacket) pReader.readPacket();
		System.out.println("...Auth packet receibed: user: " + auth.getUser());
	}
	
	public void talkWithClient() throws Exception {
		consumer = new Thread(new ConsumerThread(auth.getUser(), auth.getPassword(), consumerQueue, pWriter), "consumer-" + auth.getUser());
		producer = new Thread(new ProducerThread(auth.getUser(), auth.getPassword(), producerQueue, pWriter), "producer-" + auth.getUser());
		keepaliveThread = new Thread(new KeepAliveThread(), "keepAlive-" + auth.getUser()); 
		
		consumer.start();
		producer.start();
		keepaliveThread.start();
		
		while (consumer.isAlive() && producer.isAlive() && !Thread.interrupted()) {
			Packet packet = pReader.readPacket();
			
			switch (packet.getPacketType()) {
				case KEEP:
					keepAlive();
					break;
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
	
	private void keepAlive() {
		if (keepaliveQueue.isEmpty())
			keepaliveQueue.add(keepAlive);
	}
	
	private void prodPacket(ProducePacket p) throws IllegalMonitorStateException {
		synchronized (producerQueue) {
			producerQueue.add(p);
			producerQueue.notify();
		}
		keepAlive();
	}
	
	private void acknPacket(AcknPacket p) throws IllegalMonitorStateException {
		synchronized (consumerQueue) {
			PacketType c = p.getCommand();
			if (c == PacketType.CONS) {
				consumerQueue.add(p);
				consumerQueue.notify();
			}
		}
		keepAlive();
	}
	
	@Override
	public void run() {
		System.out.println("...New connection receibed from:" + socket.getInetAddress());
		try {
			prepareEnvironment();
			waitForAuth();
			talkWithClient();
		} catch (Exception e) {
			System.out.println("Error on connection thread: " + Thread.currentThread().getName() + ": " + e.getMessage());
		}
		
		//KILL THE SESSION
		try {
			System.out.println("Killing keepAlive thread");
			keepaliveThread.interrupt();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
		
		try {
			System.out.println("Killing consumer thread");
			consumer.interrupt();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
			
		try {
			System.out.println("Killing producer thread");
			producer.interrupt();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
		
		try {
			System.out.println("Closing Input Stream");
			pReader.close();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
		
		try {
			System.out.println("Closing Output Stream");
			pWriter.close();
		} catch (Exception e) {
			System.err.println("\t" + e.getMessage());
		}
		
		try {
			keepaliveThread.join();
			consumer.join();
			producer.join();
		} catch (Exception e) {}
		
		System.out.println("Well thats all folks");
		System.gc();
	}
}
