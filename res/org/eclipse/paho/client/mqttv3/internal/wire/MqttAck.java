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


/**
 * Abstract super-class of all acknowledgement messages.
 */
public abstract class MqttAck extends MqttWireMessage {
	public MqttAck(byte type) {
		super(type);
	}
	
	protected byte getMessageInfo() {
		return 0;
	}

}
