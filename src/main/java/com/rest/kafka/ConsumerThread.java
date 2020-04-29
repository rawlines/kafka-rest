package com.rest.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Queue;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

import com.rest.utils.KafkaUtil;

public class ConsumerThread implements Runnable {
	private Consumer<String, byte[]> cons;
	private Queue<String> queue;
	
	public ConsumerThread(String user, String pass, Queue<String> queue) throws Exception {
		cons = KafkaUtil.getConsumer(user, pass);
		this.queue = queue;
	}

	@Override
	public void run() {
		try {
			while (true) {
				synchronized (queue) {
					ConsumerRecords<String, byte[]> records = cons.poll(Duration.ofSeconds(3));
					
					for (TopicPartition partition : records.partitions()) {
						long lastOffset = 0L;
						for (ConsumerRecord<String, byte[]> record : records.records(partition)) {
							queue.add(new String(record.value()));
							queue.notify();
			 				queue.wait();
			 				boolean ack = new String(queue.poll()).equals("ok");
			 				if (ack) {
			 					lastOffset = record.offset();
			 					System.out.println("Succesfully consumed");
			 				}
						}
						cons.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));
					}
				}
			}
		} catch (Exception e) {
			System.out.println("Error in consumer thread: " + Thread.currentThread().getName());
			e.printStackTrace();
		}
	}
}
