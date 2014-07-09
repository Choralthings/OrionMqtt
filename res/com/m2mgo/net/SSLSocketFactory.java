package com.m2mgo.net;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SecureConnection;
import javax.microedition.io.SocketConnection;

import com.m2mgo.util.GPRSConnectOptions;

public class SSLSocketFactory extends SocketFactory {

	private static SSLSocketFactory sslSF = null;
	private SecureConnection secConn = null;
	private int keepAlive = 0;

	private GPRSConnectOptions connOptions = GPRSConnectOptions
			.getConnectOptions();

	private static SocketFactory getSocketFactory() {
		if (sslSF == null) {
			sslSF = new SSLSocketFactory();
		}
		return sslSF;
	}

	public static SocketFactory getDefault() {
		return getSocketFactory();
	}
//
	public SecureConnection createSecureSocket(String host, int port)
			throws IOException {

		// ssl://m2m.eclipse.org:8883

		secConn = (SecureConnection) Connector
				.open("ssl://" + host + ":" + port 
						+ ";bearer_type="
						+ connOptions.getBearerType() 
						+ ";access_point="
						+ connOptions.getAPN() 
						+ ";username="
						+ connOptions.getUser() 
						+ ";password="
						+ connOptions.getPasswd() 
						+ ";timeout="
						+ connOptions.getTimeout());

		secConn.setSocketOption(SocketConnection.LINGER, 10);
		// secConn.setSocketOption(SocketConnection.KEEPALIVE, keepAlive);

		return secConn;
	}
}
