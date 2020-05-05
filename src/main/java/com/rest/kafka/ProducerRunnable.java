package com.rest.kafka;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.InterruptException;
import org.apache.kafka.common.errors.SaslAuthenticationException;

import com.rest.exceptions.NoProduceCommandException;
import com.rest.net.AcknPacket;
import com.rest.net.Packet;
import com.rest.net.Packet.PacketType;
import com.rest.net.ProducePacket;
import com.rest.net.PacketWriter;
import com.rest.utils.KafkaUtil;

public class ProducerRunnable implements Runnable {
	private PacketWriter pWriter;
	private Producer<String, byte[]> prod;
	private ConcurrentLinkedQueue<Packet> queue;
	
	private ProducePacket waitForOrder() throws InterruptedException, NoProduceCommandException {
		Packet p = queue.poll();
		
		if (p == null) {
			synchronized(queue) {
				queue.wait();
				p = queue.poll();
			}
		}
		
		if (p == null || p.getPacketType() != PacketType.PROD)
			throw new NoProduceCommandException("Nope");
		
		return (ProducePacket) p;
	}
	
	public ProducerRunnable(String user, String pass, ConcurrentLinkedQueue<Packet> queue, PacketWriter pWriter) throws Exception {
		this.prod = KafkaUtil.getProducer(user, pass);
		this.queue = queue;
		this.pWriter = pWriter;
	}
	
	public void sendAckToClient() throws IOException {
		AcknPacket ack = new AcknPacket(PacketType.PROD);
		pWriter.sendPacket(ack);
	}
	
	@Override
	public void run() {
		try {
			while(!Thread.interrupted()) {
				ProducePacket p = waitForOrder();
				prod.send(new ProducerRecord<String, byte[]>(p.getTopic(), p.getContent()));
				sendAckToClient();
				System.out.println("PRODUCED");
			}
		} catch (SaslAuthenticationException e) {
			System.out.println("No permission in thread: " + Thread.currentThread().getName());
		} catch (InterruptedException | InterruptException e) {
			Thread.interrupted();
			System.out.println("Now i will proceed to kill myself: " + Thread.currentThread().getName());
		} catch (Exception e) {
			System.out.println("Error in thread: " + Thread.currentThread().getName() + ": " + e.getMessage());
		} finally {
			Thread.interrupted();
		}
		
		//KILL PRODUCER SESSION
		try {
			System.out.println("Killing producer session: " + Thread.currentThread().getName());
			prod.close();
		} catch (Exception e) {}
		
		System.gc();
	}
}
