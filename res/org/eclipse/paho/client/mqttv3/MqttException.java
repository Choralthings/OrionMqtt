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

import org.eclipse.paho.client.mqttv3.internal.MessageCatalog;

/**
 * Thrown if an error occurs communicating with the server.
 */
public class MqttException extends Exception {
	private static final long serialVersionUID = 300L;
	
	/** 
	 * Client encountered an exception.  Use the {@link #getCause()}
	 * method to get the underlying reason.
	 */
	public static final short REASON_CODE_CLIENT_EXCEPTION              = 0x00;

	// CONNACK return codes
	/** The protocol version requested is not supported by the server. */
	public static final short REASON_CODE_INVALID_PROTOCOL_VERSION		= 0x01;
	/** The server has rejected the supplied client ID */
	public static final short REASON_CODE_INVALID_CLIENT_ID      		= 0x02;
	/** The broker was not available to handle the request. */
	public static final short REASON_CODE_BROKER_UNAVAILABLE             = 0x03;
	/** Authentication with the server has failed, due to a bad user name or password. */
	public static final short REASON_CODE_FAILED_AUTHENTICATION			= 0x04; 
	/** Not authorized to perform the requested operation */
	public static final short REASON_CODE_NOT_AUTHORIZED					= 0x05;

	
	/** An unexpected error has occurred. */
	public static final short REASON_CODE_UNEXPECTED_ERROR				= 0x06;
	
	/** 
	 * Client timed out while waiting for a response from the server.
	 * The server is no longer responding to keep-alive messages.
	 */
	public static final short REASON_CODE_CLIENT_TIMEOUT                = 32000;
	/**
	 * Internal error, caused by no new message IDs being available.
	 */
	public static final short REASON_CODE_NO_MESSAGE_IDS_AVAILABLE      = 32001;
	
	/**
	 * The client is already connected.
	 */
	public static final short REASON_CODE_CLIENT_ALREADY_CONNECTED      = 32100;
	/**
	 * The client is already disconnected.
	 */
	public static final short REASON_CODE_CLIENT_ALREADY_DISCONNECTED   = 32101;
	/** 
	 * The client is currently disconnecting and cannot accept any new work.
	 * This can occur when waiting on a token, and then disconnecting the client.  
	 * If the message delivery does not complete within the quiesce timeout 
	 * period, then the waiting token will be notified with an exception.
	 */
	public static final short REASON_CODE_CLIENT_DISCONNECTING          = 32102;
	
	/** Unable to connect to server */
	public static final short REASON_CODE_SERVER_CONNECT_ERROR          = 32103;

	/** 
	 * The client is not connected to the server.  The {@link MqttClient#connect()}
	 * or {@link MqttClient#connect(MqttConnectOptions)} method must be called
	 * first.  It is also possible that the connection was lost - see 
	 * {@link MqttClient#setCallback(MqttCallback)} for a way to track lost
	 * connections.  
	 */
	public static final short REASON_CODE_CLIENT_NOT_CONNECTED          = 32104;

	/** 
	 * Server URI and supplied <code>SocketFactory</code> do not match.
	 * URIs beginning <code>tcp://</code> must use a <code>javax.net.SocketFactory</code>,
	 * and URIs beginning <code>ssl://</code> must use a <code>javax.net.ssl.SSLSocketFactory</code>.
	 */
	public static final short REASON_CODE_SOCKET_FACTORY_MISMATCH       = 32105;
	
	/**
	 * SSL configuration error.
	 */
	public static final short REASON_CODE_SSL_CONFIG_ERROR              = 32106;

	/** 
	 * Thrown when an attempt to call {@link MqttClient#disconnect()} has been 
	 * made from within a method on {@link MqttCallback}.  These methods are invoked
	 * by the client's thread, and must not be used to control disconnection.
	 * 
	 * @see MqttCallback#messageArrived(MqttTopic, MqttMessage)
	 */
	public static final short REASON_CODE_CLIENT_DISCONNECT_PROHIBITED  = 32107;

	/** 
	 * Protocol error: the message was not recognized as a valid MQTT packet.
	 * Possible reasons for this include connecting to a non-MQTT server, or
	 * connecting to an SSL server port when the client isn't using SSL.
	 */
	public static final short REASON_CODE_INVALID_MESSAGE				= 32108;

	/**
	 * The client has been unexpectedly disconnected from the server. The {@link #getCause() cause}
	 * will provide more details. 
	 */
	public static final short REASON_CODE_CONNECTION_LOST               = 32109;
	
	private int reasonCode;
	private Throwable cause;
	
	/**
	 * Constructs a new <code>MqttException</code> with the specified code
	 * as the underlying reason.
	 * @param reasonCode the reason code for the exception.
	 */
	public MqttException(int reasonCode) {
		super();
		this.reasonCode = reasonCode;
	}
	
	/**
	 * Constructs a new <code>MqttException</code> with the specified 
	 * <code>Throwable</code> as the underlying reason.
	 * @param cause the underlying cause of the exception.
	 */
	public MqttException(Throwable cause) {
		super();
		this.reasonCode = REASON_CODE_CLIENT_EXCEPTION;
		this.cause = cause;
	}

	/**
	 * Constructs a new <code>MqttException</code> with the specified 
	 * <code>Throwable</code> as the underlying reason.
	 * @param reason the reason code for the exception.
	 * @param cause the underlying cause of the exception.
	 */
	public MqttException(int reason, Throwable cause) {
		super();
		this.reasonCode = reason;
		this.cause = cause;
	}

	
	/**
	 * Returns the reason code for this exception.
	 * @return the code representing the reason for this exception.
	 */
	public int getReasonCode() {
		return reasonCode;
	}
	
	/**
	 * Returns the underlying cause of this exception, if available.
	 * @return the Throwable that was the root cause of this exception,
	 * which may be <code>null</code>.
	 */
	public Throwable getCause() {
		return cause;
	}
	
	/**
	 * Returns the detail message for this exception.
	 * @return the detail message, which may be <code>null</code>.
	 */
	public String getMessage() {
		return MessageCatalog.getMessage(reasonCode);
	}
	
	/**
	 * Returns a <code>String</code> representation of this exception.
	 * @return a <code>String</code> representation of this exception.
	 */
	public String toString() {
		String result = getMessage() + " (" + reasonCode + ")";
		if (cause != null) {
			result = result + " - " + cause.toString();
		}
		return result;
	}
}
