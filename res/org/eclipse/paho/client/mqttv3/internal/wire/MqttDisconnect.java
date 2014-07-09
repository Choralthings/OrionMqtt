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
package org.eclipse.paho.client.mqttv3.internal.wire;

import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * An on-the-wire representation of an MQTT DISCONNECT message.
 */
public class MqttDisconnect extends MqttWireMessage {
	
	public MqttDisconnect() {
		super(MqttWireMessage.MESSAGE_TYPE_DISCONNECT);
	}
	
	protected byte getMessageInfo() {
		return (byte) 0;
	}

	protected byte[] getVariableHeader() throws MqttException {
		return new byte[0];
	}

	/**
	 * Returns whether or not this message needs to include a message ID.
	 */
	public boolean isMessageIdRequired() {
		return false;
	}
}
