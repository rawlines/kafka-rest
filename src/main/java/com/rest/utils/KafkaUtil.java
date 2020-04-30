package com.rest.utils;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;

public abstract class KafkaUtil {
	private static String JAAS_TEMPLATE = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
	
	private static String BOOTSTRAP_SERVER = "kafkaproject.ddns.net:9091";
	
	private static String KEY_DESERIALIZER = "org.apache.kafka.common.serialization.StringDeserializer";
	private static String VALUE_DESERIALIZER = "org.apache.kafka.common.serialization.ByteArrayDeserializer";
	private static String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
	private static String VALUE_SERIALIZER = "org.apache.kafka.common.serialization.ByteArraySerializer";
	
	private static String SECURITY_PROTOCOL = "SASL_SSL";
	private static String SASL_MECHANISM = "SCRAM-SHA-512";
	
	public static Consumer<String, byte[]> getConsumer(String user, String pass) throws Exception {
		String jaasCfg = String.format(JAAS_TEMPLATE, user, pass);
		
	    Properties props = new Properties();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, "privateConsumer");
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
		
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ResourcesLoader.load(SSLUtils.TRUSTSTORE_RESOURE_LOCATION));
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, SSLUtils.TRUSTSTORE_PASSWORD);
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ResourcesLoader.load(SSLUtils.KEYSTORE_RESOURCE_LOCATION));
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SSLUtils.KEYSTORE_PASSWORD);
		
		props.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
		props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
		
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, KEY_DESERIALIZER);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, VALUE_DESERIALIZER);
		
		Consumer<String, byte[]> cons = new KafkaConsumer<String, byte[]>(props);
		cons.subscribe(Arrays.asList(user));
		
		return cons;
	}
	
	public static Producer<String, byte[]> getProducer(String user, String pass) throws Exception {
		String jaasCfg = String.format(JAAS_TEMPLATE, user, pass);
		
	    Properties props = new Properties();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL);
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, ResourcesLoader.load(SSLUtils.TRUSTSTORE_RESOURE_LOCATION));
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, SSLUtils.TRUSTSTORE_PASSWORD);
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, ResourcesLoader.load(SSLUtils.KEYSTORE_RESOURCE_LOCATION));
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, SSLUtils.KEYSTORE_PASSWORD);
		
		props.put(SaslConfigs.SASL_MECHANISM, SASL_MECHANISM);
		props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
		
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, KEY_SERIALIZER);
		props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, VALUE_SERIALIZER);
		
		Producer<String, byte[]> prod = new KafkaProducer<String, byte[]>(props);
		
		return prod;
	}
}
