package com.rest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public abstract class SSLUtils {
	private static final String KEYSTORE_RESOURCE_LOCATION = "keystore/admin.jks";
	private static final String KEYSTORE_PASSWORD = "123456";
	
	private static class KeyStoreLoader {
		private KeyManagerFactory knf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		
		public KeyStoreLoader(String keystorePath, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException, FileNotFoundException, IOException {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream(new File(getClass().getClassLoader().getResource(keystorePath).getPath())), "123456".toCharArray());
			knf.init(keyStore, password.toCharArray());
		}
		
		public KeyManager[] getKeyManagers() {
			return knf.getKeyManagers();
		}
	}
	
	private static class AcceptAllTrustManager implements X509TrustManager {
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
		@Override
		public X509Certificate[] getAcceptedIssuers() { return null; }
		
		public static TrustManager[] getTrustManagers() {
			return new TrustManager[] {new AcceptAllTrustManager()};
		}
	}
	
	public static SSLServerSocket getServerSocket(int port) throws KeyManagementException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, CertificateException, FileNotFoundException, IOException {
		KeyStoreLoader ksl = new KeyStoreLoader(KEYSTORE_RESOURCE_LOCATION, KEYSTORE_PASSWORD);
		
		SSLContext sslctx = SSLContext.getInstance("TLSv1.2");
		sslctx.init(ksl.getKeyManagers(), AcceptAllTrustManager.getTrustManagers(), new SecureRandom());
		
		SSLServerSocketFactory factory = sslctx.getServerSocketFactory();
		return (SSLServerSocket) factory.createServerSocket(port);
	} 
}
;