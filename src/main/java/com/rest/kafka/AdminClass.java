package com.rest.kafka;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.acl.AccessControlEntry;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourceType;

import com.rest.exceptions.UserExistsException;
import com.rest.net.AcknPacket;
import com.rest.net.CreaPacket;
import com.rest.net.Packet.PacketType;
import com.rest.utils.KafkaUtil;

public class AdminClass {
	private static final String ZOOKEEPER_ADDR = "kafkaproject.ddns.net:2181";
	
	/**
	 * Creates a new user, a topic with his username and adds the correspondin ACLs for this user into the topic
	 * 
	 * @param packet - {@link CreaPacket} with the corresponding credentials for creating the new user
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws IOException
	 * @throws UserExistsException - if user already exists
	 */
	public static AcknPacket createUser(CreaPacket packet) throws InterruptedException, ExecutionException, IOException, UserExistsException {
		Admin admin = KafkaUtil.getAdmin();
		
		boolean exists = admin.listTopics().names().get().stream().anyMatch((topicName) -> topicName.equals(packet.getUser()));
		
		if (exists)
			throw new UserExistsException("User " + packet.getUser() + "already exists");
		
		//------------------------number of nodes
		short aviableBrokers = (short) admin.describeCluster().nodes().get().size();
		
		//CREATE USER
		System.out.println("creating user: " + packet.getUser() + " " + packet.getPassword());
		ProcessBuilder pb = new ProcessBuilder("./create-user.sh", ZOOKEEPER_ADDR, packet.getUser(), packet.getPassword());
		pb.environment().put("PATH", "/home/gonza/Programas/java/jdk-13.0.2/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin");
		
		pb.redirectErrorStream(true)
				.directory(new File("/home/gonza/Escritorio/Kafka/kafka_2.12-2.4.1"))
				.start().waitFor();
		//----------------------------
		
		
		//CREATE THE TOPIC
		ArrayList<NewTopic> newTopics = new ArrayList<NewTopic>();
		newTopics.add(new NewTopic(packet.getUser(), 1, aviableBrokers));
		admin.createTopics(newTopics).all().get();
		
		//ALLOW PERMISSIONS FOR OWNER
		ArrayList<AclBinding> acls = new ArrayList<>();
		AccessControlEntry ace;
		//read
		ResourcePattern rpattern = new ResourcePattern(ResourceType.TOPIC, packet.getUser(), PatternType.LITERAL);
		ace = new AccessControlEntry("User:" + packet.getUser(), "*", AclOperation.READ, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));
		//write
		ace = new AccessControlEntry("User:*", "*", AclOperation.WRITE, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));		
		admin.createAcls(acls).all().get();
		
		System.gc();
		
		return new AcknPacket(PacketType.CREA);
	}
	
	/**
	 * Allows a user to write into a topic
	 * 
	 * @param topic - the topic
	 * @param user - the user
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void permitUser(String topic, String user) throws InterruptedException, ExecutionException {
		Admin admin = KafkaUtil.getAdmin();
		
		ArrayList<AclBinding> acls = new ArrayList<AclBinding>();
		ResourcePattern rpattern = new ResourcePattern(ResourceType.TOPIC, topic, PatternType.LITERAL);
		AccessControlEntry ace = new AccessControlEntry("User:" + user, "*", AclOperation.WRITE, AclPermissionType.ALLOW);
		acls.add(new AclBinding(rpattern, ace));
		
		admin.createAcls(acls).all().get();
		
		System.gc();
	}
}
