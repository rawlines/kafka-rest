package com.test;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public class SampleProducer {
	public static void main(String[] args) throws Exception {
		String topic = "client";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		
		Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "kafkaproject.ddns.net:9091");
		
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "keystore/kafkarootCA.jks");
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "123456");
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "keystore/client.jks");
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "123456");
		
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
		
		
		Producer<String, String> prod = new KafkaProducer<String, String>(props);
		
		System.out.println("Writing into topic: " + topic);
		
		String r = br.readLine();
		while (!r.equals("exit")) {
			prod.send(new ProducerRecord<String, String>(topic, r));
			System.out.println("Sent: " + r);
			r = br.readLine();
		}
		prod.close();
    }
}
