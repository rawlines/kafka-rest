package com.rest.kafka;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Queue;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SaslAuthenticationException;

import com.rest.exceptions.NoAcnowledgeException;
import com.rest.net.AcknPacket;
import com.rest.net.ConsumePacket;
import com.rest.net.Packet;
import com.rest.net.Packet.PacketType;
import com.rest.net.PacketWriter;
import com.rest.utils.KafkaUtil;

public class ConsumerThread implements Runnable {
	private PacketWriter pWriter;
	private Consumer<String, byte[]> cons;
	private Queue<Packet> queue;
	
	public ConsumerThread(String user, String pass, Queue<Packet> queue, OutputStream os) throws Exception {
		this.cons = KafkaUtil.getConsumer(user, pass);
		this.queue = queue;
		this.pWriter = new PacketWriter(os);
	}
	
	private void waitForAcknowledge() throws NoAcnowledgeException, InterruptedException {
		synchronized(queue) {
			queue.wait();
		}
		Packet p = queue.poll();
		
		if (p == null || p.getPacketType() != PacketType.ACKN || ((AcknPacket)p).getCommand() != PacketType.CONS)
			throw new NoAcnowledgeException("Nope");
		
		System.out.println("ACK");
	}
	
	private void sendRecordToClient(byte[] recordValue) throws IOException {
		ConsumePacket cp = new ConsumePacket(recordValue);
		pWriter.sendPacket(cp);
		System.out.println("SENT RECORD");
	}

	@Override
	public void run() {
		try {
			while (!Thread.interrupted()) {
				ConsumerRecords<String, byte[]> records = cons.poll(Duration.ofSeconds(3));
				
				for (TopicPartition partition : records.partitions()) {
					long lastOffset = 0L;
					for (ConsumerRecord<String, byte[]> record : records.records(partition)) {
						
						sendRecordToClient(record.value());
						
						waitForAcknowledge();
					
						lastOffset = record.offset();
		 				System.out.println("Succesfully consumed");
					}
					cons.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
				}
			}
		} catch (SaslAuthenticationException e) {
			System.out.println("No permission in thread: " + Thread.currentThread().getName());
			e.printStackTrace();
		} catch (Exception e) {
			System.out.println("Error in consumer thread: " + Thread.currentThread().getName());
			e.printStackTrace();
		} finally {
			System.out.println("Killing consumer sesion: " + Thread.currentThread().getName());
			try {
				cons.close();
				pWriter.close();
			} catch (Exception e) {
				System.out.println("Error closing streams");
			}
		}
		
		System.gc();
	}
}
