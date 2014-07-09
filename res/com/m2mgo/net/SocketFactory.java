package com.m2mgo.net;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

import com.m2mgo.util.GPRSConnectOptions;

public class SocketFactory extends Object {

	private static SocketFactory sf = null;
	protected SocketConnection sc = null;
	private GPRSConnectOptions connOptions = GPRSConnectOptions
			.getConnectOptions();

	private static SocketFactory getSocketFactory() {
		if (sf == null) {
			sf = new SocketFactory();
		}
		return sf;
	}

	public SocketConnection createSocket(String host, int port)
			throws IOException {

		// socket://m2m.eclipse.org:1883

		sc = (SocketConnection) Connector.open("socket://" + host + ":" + port
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
		/*System.out.println("[MB]" + "socket://" + host + ":" + port
				+ ";bearer_type="
				+ connOptions.getBearerType() 
				+ ";access_point="
				+ connOptions.getAPN() 
				+ ";username="
				+ connOptions.getUser() 
				+ ";password="
				+ connOptions.getPasswd() 
				+ ";timeout="
				+ connOptions.getTimeout());*/
		//[MB]sc.setSocketOption(SocketConnection.LINGER, 5); // TODO verify if
														// settings correct

		return sc;
	}

	public static SocketFactory getDefault() {
		return getSocketFactory();
	}
}
