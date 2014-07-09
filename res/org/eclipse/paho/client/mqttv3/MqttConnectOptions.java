/* 
 * Copyright (c) 2009, 2012 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */
package org.eclipse.paho.client.mqttv3;

import com.m2mgo.net.SocketFactory;
import com.m2mgo.util.Properties;

//import java.util.Properties; //m2mgo

//import javax.net.SocketFactory; //m2mgo


/**
 * Stores options used when connecting to a server.
 */
public class MqttConnectOptions {
	private int keepAliveInterval = 60;
	private MqttTopic willDestination = null;
	private MqttMessage willMessage = null;
	private String userName;
	private char[] password;
	private SocketFactory socketFactory;
	private Properties sslClientProps = null;
	private boolean cleanSession = true;
	private int connectionTimeout = 30;
	
	/**
	 * Constructs a new <code>MqttConnectOptions</code> object using the 
	 * default values.
	 * 
	 * The defaults are:
	 * <ul>
	 * <li>The keepalive interval is 60 seconds</li>
	 * <li>Clean Session is true</li>
	 * <li>The message delivery retry interval is 15 seconds</li>
	 * <li>The connection timeout period is 30 seconds</li> 
	 * <li>No Will message is set</li>
	 * <li>A standard SocketFactory is used</li>
	 * </ul>
	 * More information about these values can be found in the setter methods. 
	 */
	public MqttConnectOptions() {
	}
	
	/**
	 * Returns the password to use for the connection.
	 * @return the password to use for the connection.
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * Sets the password to use for the connection.
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}

	/**
	 * Returns the user name to use for the connection.
	 * @return the user name to use for the connection.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name to use for the connection.
	 * @throws IllegalArgumentException if the user name is blank or only
	 * contains whitespace characters.
	 */
	public void setUserName(String userName) {
		if ((userName != null) && (userName.trim().equals(""))) {
			throw new IllegalArgumentException();
		}
		this.userName = userName;
	}
	
	/**
	 * Sets the "Last Will and Testament" (LWT) for the connection.
	 * In the event that this client unexpectedly loses its connection to the 
	 * server, the server will publish a message to itself using the supplied
	 * details.
	 * 
	 * @param topic the topic to publish to.
	 * @param payload the byte payload for the message.
	 * @param qos the quality of service to publish the message at (0, 1 or 2).
	 * @param retained whether or not the message should be retained.
	 */
	public void setWill(MqttTopic topic, byte[] payload, int qos, boolean retained) {
		validateWill(topic, payload);
		this.setWill(topic, new MqttMessage(payload), qos, retained);
	}
	
	
	/**
	 * Validates the will fields.
	 */
	private void validateWill(MqttTopic dest, Object payload) {
		if ((dest == null) || (payload == null)) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * Sets up the will information, based on the supplied parameters.
	 */
	private void setWill(MqttTopic topic, MqttMessage msg, int qos, boolean retained) {
		willDestination = topic;
		willMessage = msg;
		willMessage.setQos(qos);
		willMessage.setRetained(retained);
		// Prevent any more changes to the will message
		willMessage.setMutable(false);
	}
	
	/**
	 * Returns the "keep alive" interval.
	 * @see #setKeepAliveInterval(int)
	 * @return the keep alive interval.
	 */
	public int getKeepAliveInterval() {
		return keepAliveInterval;
	}

	/**
	 * Sets the "keep alive" interval.
	 * This value, measured in seconds, defines the maximum time interval 
	 * between messages sent or received. It enables the client to 
	 * detect that if the server is no longer available, without 
	 * having to wait for the long TCP/IP timeout. The client will ensure
	 * that at least one message travels across the network within each
	 * keep alive period.  In the absence of a data-related message during 
	 * the time period, the client sends a very small "ping" message, which
	 * the server will acknowledge.
	 * <p>The default value is 60 seconds</p>
	 * 
	 * @param keepAliveInterval the interval, measured in seconds.
	 */
	public void setKeepAliveInterval(int keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}
	
	/**
	 * Returns the connection timeout value.
	 * @see #setConnectionTimeout(int)
	 * @return the connection timeout value.
	 */
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	
	/**
	 * Sets the connection timeout value.
	 * This value, measured in seconds, defines the maximum time interval
	 * the client will wait for calls to {@link MqttClient#connect(MqttConnectOptions) connect}, 
	 * {@link MqttClient#subscribe(String[], int[]) subscribe} and 
	 * {@link MqttClient#unsubscribe(String[]) unsubscribe} to complete.
	 * The default timeout is 30 seconds.
	 * @param connectionTimeout the timeout value, measured in seconds.
	 */
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	
	/**
	 * Returns the socket factory that will be used when connecting, or
	 * <code>null</code> if one has not been set.
	 */
	public SocketFactory getSocketFactory() {
		return socketFactory;
	}
	
	/**
	 * Sets the <code>SocketFactory</code> to use.  This allows an application
	 * to apply its own policies around the creation of network sockets.  If
	 * using an SSL connection, an <code>SSLSocketFactory</code> can be used
	 * to supply application-specific security settings.
	 * @param socketFactory the factory to use.
	 */
	public void setSocketFactory(SocketFactory socketFactory) {
		this.socketFactory = socketFactory;
	}

	/**
	 * Returns the topic to be used for last will and testament (LWT).
	 * @return the MqttTopic to use, or <code>null</code> if LWT is not set.
	 * @see #setWill(MqttTopic, byte[], int, boolean)
	 */
	public MqttTopic getWillDestination() {
		return willDestination;
	}

	/**
	 * Returns the message to be sent as last will and testament (LWT).
	 * The returned object is "read only".  Calling any "setter" methods on 
	 * the returned object will result in an 
	 * <code>IllegalStateException</code> being thrown.
	 * @return the message to use, or <code>null</code> if LWT is not set.
	 */
	public MqttMessage getWillMessage() {
		return willMessage;
	}
	
	/**
	 * Returns the SSL properties for the connection.
	 * @return the properties for the SSL connection
	 */
	public Properties getSSLProperties() {
		return sslClientProps;
	}

	/**
	 * Sets the SSL properties for the connection.  Note that these
	 * properties are only valid if an implementation of the Java
	 * Secure Socket Extensions (JSSE) is available.  These properties are
	 * <em>not</em> used if a SocketFactory has been set using
	 * {@link #setSocketFactory(SocketFactory)}.
	 * The following properties can be used:</p>
	 * <dl>
	 * <dt>com.ibm.ssl.protocol</dt>
   	 * <dd>One of: SSL, SSLv3, TLS, TLSv1, SSL_TLS.</dd>
	 * <dt>com.ibm.ssl.contextProvider
   	 * <dd>Underlying JSSE provider.  For example "IBMJSSE2" or "SunJSSE"</dd>
	 * 
	 * <dt>com.ibm.ssl.keyStore</dt>
   	 * <dd>The name of the file that contains the KeyStore object that you 
   	 * want the KeyManager to use. For example /mydir/etc/key.p12</dd>
	 * 
	 * <dt>com.ibm.ssl.keyStorePassword</dt>
   	 * <dd>The password for the KeyStore object that you want the KeyManager to use.  
   	 * The password can either be in plain-text, 
   	 * or may be obfuscated using the static method:
     * <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>.
   	 * This obfuscates the password using a simple and insecure XOR and Base64 
   	 * encoding mechanism. Note that this is only a simple scrambler to 
   	 * obfuscate clear-text passwords.</dd>
	 * 
	 * <dt>com.ibm.ssl.keyStoreType</dt>
   	 * <dd>Type of key store, for example "PKCS12", "JKS", or "JCEKS".</dd>
	 * 
	 * <dt>com.ibm.ssl.keyStoreProvider</dt>
   	 * <dd>Key store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
	 * 
	 * <dt>com.ibm.ssl.trustStore</dt>
   	 * <dd>The name of the file that contains the KeyStore object that you 
   	 * want the TrustManager to use.</dd> 
	 * 
	 * <dt>com.ibm.ssl.trustStorePassword</dt>
   	 * <dd>The password for the TrustStore object that you want the 
   	 * TrustManager to use.  The password can either be in plain-text, 
   	 * or may be obfuscated using the static method:
     * <code>com.ibm.micro.security.Password.obfuscate(char[] password)</code>.
   	 * This obfuscates the password using a simple and insecure XOR and Base64 
   	 * encoding mechanism. Note that this is only a simple scrambler to 
   	 * obfuscate clear-text passwords.</dd>
	 * 
	 * <dt>com.ibm.ssl.trustStoreType</dt>
   	 * <dd>The type of KeyStore object that you want the default TrustManager to use.  
   	 * Same possible values as "keyStoreType".</dd>
	 * 
	 * <dt>com.ibm.ssl.trustStoreProvider</dt>
   	 * <dd>Trust store provider, for example "IBMJCE" or "IBMJCEFIPS".</dd>
	 * 
	 * <dt>com.ibm.ssl.enabledCipherSuites</dt>
	 * <dd>A list of which ciphers are enabled.  Values are dependent on the provider, 
	 * for example: SSL_RSA_WITH_AES_128_CBC_SHA;SSL_RSA_WITH_3DES_EDE_CBC_SHA.</dd>
	 * 
	 * <dt>com.ibm.ssl.keyManager</dt>
	 * <dd>Sets the algorithm that will be used to instantiate a KeyManagerFactory object 
	 * instead of using the default algorithm available in the platform. Example values: 
	 * "IbmX509" or "IBMJ9X509".
	 * </dd>
	 * 
	 * <dt>com.ibm.ssl.trustManager</dt>
	 * <dd>Sets the algorithm that will be used to instantiate a TrustManagerFactory object 
	 * instead of using the default algorithm available in the platform. Example values: 
	 * "PKIX" or "IBMJ9X509".
	 * </dd>
	 * </dl>
	 */
	public void setSSLProperties(Properties props) {
		this.sslClientProps = props;
	}
	
	/**
	 * Returns whether the server should remember state for the client across reconnects.
	 * @return the clean session flag
	 */
	public boolean isCleanSession() {
		return this.cleanSession;
	}
	
	/**
	 * Sets whether the server should remember state for the client across reconnects.
	 * This includes subscriptions and the state of any in-flight messages.
 	 */
	public void setCleanSession(boolean cleanSession) {
		this.cleanSession = cleanSession;
	}
}
