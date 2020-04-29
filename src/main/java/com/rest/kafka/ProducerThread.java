package com.rest.kafka;

import java.util.Queue;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.producer.Producer;

import com.rest.utils.KafkaUtil;

public class ProducerThread implements Runnable {
	private Producer<String, byte[]> prod;
	private Queue<String> queue;
	
	public ProducerThread(String user, String pass, Queue<String> queue) throws Exception {
		prod = KafkaUtil.getProducer(user, pass);
		this.queue = queue;
	}
	
	@Override
	public void run() {
		try {
			
		} catch (Exception e) {
			
		}
	}
}
