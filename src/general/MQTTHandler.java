package general;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.internal.MemoryPersistence;

public class MQTTHandler  implements MqttCallback{
	private MqttClient client;
	private String clientId;
	private String brokerURL;
	private SocketGPRStask view;
	
	public MQTTHandler(String clientId, String brokerURL){
		this.clientId = clientId;
		this.brokerURL = brokerURL;
	}

	public synchronized void connectToBroker()
			throws MqttSecurityException, MqttException {
			//System.out.println("Client ID: " + clientId);
			//System.out.println("Connecting to: " + brokerURL);

		// Construct the MqttClient instance
		client = new MqttClient(brokerURL, clientId, new MemoryPersistence());
		// Set this wrapper as the callback handler
		client.setCallback(this);
		// Connect to the server
		try {
			client.connect();
			//System.out.println("Connected!");

		} catch (MqttException e) {
			//System.out.println("!Exception at connecting!");
			throw e;
		}
	}

	public void publish(String topicName, int qos, byte[] payload)
			throws MqttException {

		if (client == null) {
			//System.out.println("Client non connesso");
			connectToBroker();
		}
		//System.out.println("Client: publish");
		
		// Get an instance of the topic
		MqttTopic topic = client.getTopic(topicName);
		//System.out.println("Client: publish1");

		MqttMessage message = new MqttMessage(payload);
		//System.out.println("Client: publish2");
		message.setQos(qos);
		//System.out.println("Client: publish3");
		if (client.isConnected()) {
			//System.out.println("Client: publish4");
			// Publish the message
			MqttDeliveryToken token = topic.publish(message);
			//System.out.println("Client: publish5");
			// Wait until the message has been delivered to the server
			token.waitForCompletion(5000);
			//System.out.println("Client: publish6");
		} else {
			// client.connect();
			//System.out.println("Client: publish7");
			connectToBroker();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//System.out.println("Exception at sleeping ");
			}
			if (client.isConnected()) {
				//System.out.println("Client: publish8");
				// Publish the message
				MqttDeliveryToken token = topic.publish(message);
				//System.out.println("Client: publish9");
				// Wait until the message has been delivered to the server
				token.waitForCompletion(5000);
				//System.out.println("Client: publish10");

			} else {
				//System.out.println("Client: publish11");
				throw new MqttException(
						MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
			}
		}
	}

	public void subscribe(String topicName, int qos) throws MqttException {

		if (client.isConnected()) {
			// Subscribe to the topic
			client.subscribe(topicName, qos);
		} else {
			// client.connect();
			connectToBroker();

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//System.out.println("Exception at sleeping ");
			}
			if (client.isConnected()) {
				// Subscribe to the topic
				client.subscribe(topicName, qos);
			} else {
				throw new MqttException(
						MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
			}
		}
	}
	
	public void disconnect(long timeout){
		try {
			client.disconnect(timeout);
			//System.out.println("Disconnected MQTT");
		} catch (MqttException e) {
			//System.out.println("Exception at disconnecting");
		}
	}

	public void connectionLost(Throwable cause) {
		//System.out.println("!!!!! Connection to MQTT broker lost !!!!!");
	}

	public void messageArrived(MqttTopic topic, MqttMessage message)
			throws Exception {
		System.out.println("Msg arrived!");
		System.out.println("Topic: " + topic.getName() + " QoS: "
				+ message.getQos());
		System.out.println("Message: " + new String(message.getPayload()));

	}

	public void deliveryComplete(MqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}
	
	public void applyView(SocketGPRStask view){
		this.view = view;
	}

}
