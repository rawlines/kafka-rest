package com.test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public class SampleConsumer {
	public static void main(String[] args) {
		String topic = "client";
		
	    Properties props = new Properties();
	     
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafkaproject.ddns.net:9091");
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "privateConsumer");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, args[0]);
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "123456");
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, args[1]);
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "123456");
			
			
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
		
		KafkaConsumer<String, String> cons = new KafkaConsumer<String, String>(props);
		cons.subscribe(Arrays.asList(topic));
		
		System.out.println("Reading from topic: " + topic);
		
		while (true) {
			ConsumerRecords<String, String> records = cons.poll(Duration.ofSeconds(1));
			
	        for (ConsumerRecord<String, String> record : records)
	        	System.out.printf("offset = %d, key = %s, value = %s%n", record.offset(), record.key(), record.value());
	        
	        cons.commitSync();
		}
	}
}
