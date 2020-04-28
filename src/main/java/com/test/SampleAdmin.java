package com.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;

public class SampleAdmin {
	private Admin client;
	
	public SampleAdmin(String bootStrapServer) throws InterruptedException, ExecutionException {
		String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
		String jaasCfg = String.format(jaasTemplate, "admin", "1234567890qw");
		
		Properties props = new Properties();
		props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServer);
		props.put(AdminClientConfig.CLIENT_ID_CONFIG, "admin");
		
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
		props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "keystore/kafkarootCA.jks");
		props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "123456");
		props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "keystore/admin.jks");
		props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "123456");
		props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
		props.put(SaslConfigs.SASL_JAAS_CONFIG, jaasCfg);
		
		client = KafkaAdminClient.create(props);
		
		//deleteTopic("client");
		//createTopic("client", (short) 3);
		//addACLs("client", "client");
		listTopics();
	}
	
	private void listTopics() throws InterruptedException, ExecutionException {
		ListTopicsResult res = client.listTopics();
		KafkaFutureImpl<Set<String>> future = (KafkaFutureImpl<Set<String>>) res.names();
		
		Set<String> topics = future.get();
		topics.forEach(new Consumer<String>() {
			@Override
			public void accept(String topic) {
				System.out.println(topic);
				try {
					getACLfromTopic(topic).forEach(new Consumer<AclBinding>() {
						@Override
						public void accept(AclBinding t) {
							System.out.println("\t" + t.entry().toString());
						}
					});
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			
		});
	}
	
	private Collection<AclBinding> getACLfromTopic(String topic) throws InterruptedException, ExecutionException {
		ResourcePatternFilter rf = new ResourcePatternFilter(ResourceType.TOPIC, topic, PatternType.LITERAL);
		AccessControlEntryFilter acef = new AccessControlEntryFilter(null, "*", AclOperation.ANY, AclPermissionType.ANY);
		
		AclBindingFilter filter = new AclBindingFilter(rf, acef);
		
		Collection<AclBinding> acls = client.describeAcls(filter).values().get();
		return acls;
	}
	
	private void createTopic(String topicName, short replicationFactor) throws InterruptedException, ExecutionException {
		ArrayList<NewTopic> newTopics = new ArrayList<NewTopic>();
		
		newTopics.add(new NewTopic(topicName, 1, replicationFactor));
		client.createTopics(newTopics).all().get();
		System.out.println("done");
	}
	
	private void deleteTopic(String topic) throws InterruptedException, ExecutionException {
		//adding the topic to remove
		ArrayList<String> topics = new ArrayList<String>();
		topics.add(topic);
		
		//adding the acls of this topic to remove them
		ArrayList<AclBindingFilter> acls = new ArrayList<AclBindingFilter>();
		getACLfromTopic(topic).forEach(new Consumer<AclBinding>() {
			@Override
			public void accept(AclBinding t) {
				acls.add(t.toFilter());
			}
		});
		
		//remove the topic
		client.deleteTopics(topics).all().get();
		//remove the ACLs
		deleteACLs(acls);
		
		System.out.println("done");
	}
	
	private void deleteACLs(Collection<AclBindingFilter> acls) throws InterruptedException, ExecutionException {
		client.deleteAcls(acls).all().get();
	}
	
	private void allowUserWriteTopic(String user, String topic) throws InterruptedException, ExecutionException {
		ArrayList<AclBinding> acls = new ArrayList<AclBinding>();
		ResourcePattern rpattern;
		AccessControlEntry ace;
		
		rpattern = new ResourcePattern(ResourceType.TOPIC, topic, PatternType.LITERAL);
		ace = new AccessControlEntry("User:" + user, "*", AclOperation.WRITE, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));
		
		client.createAcls(acls).all().get();
		System.out.println("done");
	} 
	
	private void addACLs(String owner, String topic) throws InterruptedException, ExecutionException {
		ArrayList<AclBinding> acls = new ArrayList<AclBinding>();
		ResourcePattern rpattern;
		AccessControlEntry ace;
		
		//Global group, only need to add once
		/*rpattern = new ResourcePattern(ResourceType.GROUP, "privateConsumer", PatternType.LITERAL);
		ace = new AccessControlEntry("User:*", "*", AclOperation.READ, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));*/
		
		//ALLOW PERMISSIONS FOR OWNER
		//READ permission
		rpattern = new ResourcePattern(ResourceType.TOPIC, topic, PatternType.LITERAL);
		ace = new AccessControlEntry("User:" + owner, "*", AclOperation.READ, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));
		
		//WRITE permission
		ace = new AccessControlEntry("User:" + owner, "*", AclOperation.WRITE, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));		
	
		client.createAcls(acls).all().get();
		System.out.println("done");
	}
	
	public static void main(String[] args) throws InterruptedException, ExecutionException {
		new SampleAdmin("kafkaproject.ddns.net:9091");
	}
}
