/*	
 * Class 	SocketGPRStask
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;
import javax.microedition.io.*;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import com.cinterion.io.BearerControl;
import com.m2mgo.util.GPRSConnectOptions;

/**
 * Task that tales care of sending strings through a GPRS connection using a
 * TCP socket service
 * 
 * @version	1.01 <BR> <i>Last update</i>: 05-10-2007
 * @author 	alessioza
 * 
 */
public class SocketGPRStask extends ThreadCustom implements GlobCost {	
	/* 
	 * local variables
	 */
	private double ctrlSpeed =0;
	private int		lettura;
	private boolean close = false;
	private boolean exitTRKON = true;
	private int 	temp;
	private boolean ram = true;
	/** Full string to send through GPRS */
	private String 	outText;
	private String[] outTextMqtt;
	// socket TCP
	SocketConnection sockConn;
	OutputStream    out;
	InputStream 	in;
	//private String	destAddressTCP;
	private int val_insensibgps;
	private int timeOutSocket = 0;
	private boolean errorSent = false;
	BCListenerCustom list;
	String answer = "";
	int nackCount = 0;
	int errorCount = 0;
	int timer3 = 0;
	
	//private String clientId = "EGS5x-356612026709230";
	private String clientId = "EGS5x-";
		//private String clientId = "EGS5x-3";
	//private String brokerUrl = "tcp://188.125.106.86";//"tcp://things.bluewind.it"; //"m2m.eclipse.org";
	//private int brokerPort = 1883;
	//private String publishTopic = "10000038/1/";
	//private String subscribeTopic = "10000038/1";
		//private String publishTopic = "01600018/1/";
		//private String subscribeTopic = "01600018/1";
	private int qos = 0;
	//private String apn = "internetm2m.air.com"; //"internet";
	
	private boolean firstTime = true;
	private MQTTHandler mqttH;
	SocketGPRStask thisTask;
	

	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore  'semAT', for the exclusive use of the AT resource
	 * InfoStato  'infoS', status informations about application
	 * Mailbox    'mbox5', to receive msg with this thread
	 * Mailbox    'mbox2', to send msg to th2 (ATsender)
	 * Mailbox    'mbox3', to send msg to th3
	 * DataStore  'dsData', to store GPS strings
	 */

	
	/* 
	 * constructors
	 */
	public SocketGPRStask() {
		if(debug){
			System.out.println("TT*SocketGPRStask: CREATED");
		}
		thisTask = this;
		//GPRSConnectOptions.getConnectOptions().setAPN(apn);

		//GPRSConnectOptions.getConnectOptions().setBearerType("gprs");
		
		//mqttH = new MQTTHandler(clientId, brokerUrl + ":" + brokerPort);
		//mqttH = new MQTTHandler(infoS.getInfoFileString(IDtraker),"tcp://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort));
		//mqttH.applyView(this);

	}
	
	/*
	 * methods
	 */
	/**
	 * Taske execution code:
	 * <BR> ---------- <BR>
	 * Performed operations: <br>
	 * <ul type="disc">
	 *  <li> Add alarm to position strings if necessary;
	 * 	<li> Send string;
	 *  <li> Check on number of strings sent.	 
	 * </ul>
	 */
	public void run() {
		
		//GPRSConnectOptions.getConnectOptions().setAPN(apn);
		GPRSConnectOptions.getConnectOptions().setAPN(infoS.getInfoFileString(apn));

		GPRSConnectOptions.getConnectOptions().setBearerType("gprs");
		
		//mqttH = new MQTTHandler(clientId, brokerUrl + ":" + brokerPort);
		//mqttH = new MQTTHandler(infoS.getInfoFileString(IDtraker),"tcp://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort));
		//mqttH = new MQTTHandler((clientId + infoS.getInfoFileString(IDtraker)), "tcp://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort));
		//mqttH.applyView(this);

		list = new BCListenerCustom();
		list.addInfoStato(infoS);
		BearerControl.addListener(list);
		
		while (!infoS.isCloseTCPSocketTask()){
			
			//System.out.println("SOCKET TASK ACTIVE");
			if((infoS.getInfoFileString(TrkState).equals("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS")) && infoS.getInfoFileString(GPRSProtocol).equals("TCP") && ((infoS.getInfoFileInt(TrkIN) != infoS.getInfoFileInt(TrkOUT)) || !infoS.getDataRAM().equals(""))){ 
				
				if(debug)
					System.out.println("In TRK ON");
				exitTRKON = false;
				
				try {
			
					// Indication about SOCKET TASK ACTIVE
					//System.out.println("TT*SocketGPRStask: START");
					infoS.setIfsocketAttivo(true);
					//destAddressTCP = "socket://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort) + ";" + infoS.getInfoFileString(ConnProfileGPRS) + ";timeout=0";
					
					
				
					/*
					 * Once this task has been started, it is completely
					 * finished before proceeding to a re-use, even if the
					 * timeout expires (so there may be a FIX GPRS timeout
					 * expired!)
					 */
					//System.out.println("SOCKET TASK ACTIVE - VERIFY POINTERS");
					try {
						try{
							// queue semaphore
							while(!InfoStato.getCoda()){Thread.sleep(1);}
						}catch(InterruptedException e){}
						
						// Verify if data are in RAM or FLASH	
						if(infoS.getInfoFileInt(TrkIN) == infoS.getInfoFileInt(TrkOUT)){
							//System.out.println("TT*SocketGPRStask: data from RAM");
							outText = infoS.getDataRAM();
							//outTextMqtt = infoS.getDataMqttRAM();
							//System.out.println("data from RAM");
							/*for(int ind = 0; ind < outTextMqtt.length; ind++)
								System.out.println(outTextMqtt[ind]);*/
							//System.out.println("data: "+ outText);
							ram = true;
						}
						else{
							ram = false;
							temp = infoS.getInfoFileInt(TrkOUT);
							//System.out.println("TT*SocketGPRStask: pointer out - " + temp);
							if ((temp >= codaSize) || temp < 0)
								temp = 0;
							outText = infoS.getRecord(temp);
							//outTextMqtt = infoS.getMqttRecord(temp);
							//new LogError("TT*SocketGPRSTask: pointer out - " + temp + " " + outText);
							//System.out.println("TT*SocketGPRStask: data in queue: ");
							//System.out.println("data from flash");
							/*for(int ind = 0; ind < outTextMqtt.length; ind++)
								System.out.println(outTextMqtt[ind]);
							*/
							
							//System.out.println("TT*SocketGPRStask: Free Coda");
								
						}						
						
						// Print string to send
						//System.out.println("TT*SocketGPRStask: String to sent through GPRS:\r\n");
						/*for(int ind = 0; ind < outTextMqtt.length; ind++)
							System.out.println(outTextMqtt[ind]);
						*/	
						//new LogError("GPRS string: " + outText);
						
						ctrlSpeed =infoS.getSpeedForTrk(); 
						if(debug_speed){
							ctrlSpeed = infoS.getSpeedGree();
							//System.out.println("SPEED " + ctrlSpeed);
						}
						
						try{
							val_insensibgps = Integer.parseInt(infoS.getInfoFileString(InsensibilitaGPS));
						}catch(NumberFormatException e){
							//new LogError("NumberFormatException");
							val_insensibgps = 0;
						}
						//new LogError("Velocoita attuale: " + ctrlSpeed + ". Val insens: " + val_insensibgps);
												
						if  (ram){

							if ((ctrlSpeed >= val_insensibgps)){
								//System.out.println("Speed check ok.");
								infoS.settrasmetti(true);
								if (infoS.getInvioStop()== true){
									infoS.setApriGPRS(true);
								}
								infoS.setInvioStop(false);
								
							}
							else 
							{
								if((outText.indexOf("ALARM")>0) || (outText.indexOf("ALIVE")>0) || (outText.indexOf("COD<")>0) || (outText.indexOf("URC SIM")>0)){
								//if((outTextMqtt[ALR_IND].indexOf("ALARM")>0) || (outTextMqtt[ALR_IND].indexOf("ALIVE")>0) || (outTextMqtt[ALR_IND].indexOf("COD<")>0) || (outTextMqtt[ALR_IND].indexOf("URC SIM")>0)){
									//System.out.println("Alarm");
									infoS.settrasmetti(true);
									infoS.setApriGPRS(true);
								}
								else{
									
									if((!infoS.getPreAlive())&&(ctrlSpeed <= val_insensibgps)&&(infoS.getPreSpeedDFS() > val_insensibgps)){
										
										//System.out.println("Speed check less then insensitivity, previous speed is greater");
										infoS.settrasmetti(true);
										if (infoS.getInvioStop()== true){
											infoS.setApriGPRS(true);
										}
										infoS.setInvioStop(false);
										
									}
									else{
										
										//System.out.println("Speed check failed.");
										if (infoS.getInvioStop()== false){
											//System.out.println("Send stop coordinate.");
											infoS.settrasmetti(true);
											infoS.setInvioStop(true);
											infoS.setChiudiGPRS(true);
																						
											//new LogError("Send stop.");
										}
									}
								}
							}
							//if(outTextMqtt[ALR_IND].indexOf("ALIVE")>0){
							if(outText.indexOf("ALARM")>0){
								//System.out.println("ALIVE MESSAGE");
								infoS.setPreAlive(true);
							}
							else{
								infoS.setPreAlive(false);
								//System.out.println("NO ALIVE MESSAGE");
							}
						}
						else{
							//new LogError("from store.");
							
							infoS.settrasmetti(true);
							infoS.setChiudiGPRS(false);
						}
						
						
						//new LogError("Transmission status: " + infoS.gettrasmetti());
											
						if (infoS.gettrasmetti()== true){
							
							infoS.settrasmetti(false);
							
							if (infoS.getApriGPRS()==true){
								close = false;
								infoS.setTRKstate(true);
								try{
									semAT.getCoin(5);
										infoS.setATexec(true);
										mbox2.write("at^smong\r");
										while(infoS.getATexec()) { Thread.sleep(whileSleep); }
										infoS.setATexec(true);
										mbox2.write("at+cgatt=1\r");
										while(infoS.getATexec()) { Thread.sleep(whileSleep); }
									semAT.putCoin();
								} catch(Exception e){}
								
								// Open GPRS Channel
								
								// connect to MQTT Brocker
								semAT.getCoin(5);
								try {
									if(firstTime){
										mqttH = new MQTTHandler((clientId + infoS.getInfoFileString(IDtraker)), "tcp://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort));
										mqttH.applyView(this);
									}
									//System.out.println("TT*SocketGPRSTask: ***************************:");
									mqttH.connectToBrocker();
									firstTime = false;
								} catch (MqttSecurityException e1) {
									e1.printStackTrace();
								} catch (MqttException e1) {
									e1.printStackTrace();
								}
								semAT.putCoin();

								infoS.setApriGPRS(false);
							}
							//System.out.println("TT*SocketGPRSTask: INVIO DATO:");
							
							if(mqttParser){
								Posusr msg = new Posusr();
								outTextMqtt = msg.set_posusr_mqtt(outText);
								for(int ind = 0; ind < outTextMqtt.length; ind++)
									System.out.println(outTextMqtt[ind]);
										
								// Send string through GPRS
								// send only date,time,longitude,latitude
								// 4 - date
								// 5 - time
								// 6 - lat
								// 7 - N/S
								// 8 - lon
								// 9 - E/W
								//for(int ind = 2; ind < outTextMqtt.length; ind++){
								for(int ind = 4; ind < 10; ind++){	// send only date,time,longitude,latitude
									if(outTextMqtt[ind] == null)
										outTextMqtt[ind] = "-";
									try {
										semAT.getCoin(5);
											//mqttH.publish(publishTopic + "/CH1/" + (ind), qos,outTextMqtt[ind].getBytes());
											//mqttH.publish(publishTopic + "/" + infoS.getInfoFileString(IDtraker)+ "/" + (ind), qos,outTextMqtt[ind].getBytes());
											mqttH.publish(infoS.getInfoFileString(PublishTopic) + infoS.getInfoFileString(IDtraker)+ "/" +(ind), qos,outTextMqtt[ind].getBytes());
										semAT.putCoin();	
									} catch (MqttException e) {
										e.printStackTrace();
									}
								}
							}
							else{
								try {
									semAT.getCoin(5);
										mqttH.publish(infoS.getInfoFileString(PublishTopic) + infoS.getInfoFileString(IDtraker), qos,outText.getBytes());
									semAT.putCoin();	
								} catch (MqttException e) {
									e.printStackTrace();
								}
							}
							
							if (infoS.getChiudiGPRS()==true){
								
								infoS.setTRKstate(false);
								//System.out.println("CLOSE GPRS");
								// Close GPRS Channel
								semAT.getCoin(5);
								mqttH.disconnect(0);
								semAT.putCoin();
								infoS.setChiudiGPRS(false);
							}
							
						}
						// If BearerListener different from BEARER_STATE_UP, I do not have network coverage
						if(debug){
							System.out.println("BEARER: " + infoS.getGprsState());
						}
						if(!infoS.getGprsState()){
							errorSent = true;
							//System.out.println("BEARER ERROR");
							new LogError("BEARER ERROR");
						}
						
						if(ram){
							if(!errorSent){
								infoS.setDataRAM("");
								//System.out.println("DELETE DATA FROM RAM");
								//infoS.setDataMqttRAM(null);
							}
						}
						else{
							if(!errorSent){
								temp++;
								if(temp >= codaSize || temp < 0)
									temp = 0;
								infoS.setInfoFileInt(TrkOUT, "" + temp);
								file.setImpostazione(TrkOUT, "" + temp);
								InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
							}
							errorSent = false;
						}
						InfoStato.freeCoda();
						
						infoS.setIfsocketAttivo(false);
						Thread.sleep(100);
						if(errorSent){
							close = true;
							semAT.putCoin();	// release AT interface
							infoS.setIfsocketAttivo(false);
							infoS.setApriGPRS(false);
							infoS.setChiudiGPRS(false);
						}
					} catch (Exception e) {
						close = true;
						String msgExcept = e.getMessage();
						//System.out.println("TT*SocketGPRStask, exception: "+msgExcept);
			
						infoS.setIfsocketAttivo(false);
						
						infoS.setApriGPRS(false);
						infoS.setChiudiGPRS(false);
						
					} //catch
				
				} catch (Exception e) {
					close = true;
					//new LogError("SocketGPRSTask generic Exception");
					infoS.setIfsocketAttivo(false);
					
					infoS.setApriGPRS(false);
					infoS.setChiudiGPRS(false);
				}
				if(debug){
					System.out.println("TT*SocketGPRStask, close: " + close);
				}
				if(close){
					
					try{
						semAT.getCoin(5);
							infoS.setATexec(true);
							mbox2.write("at^smong\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						semAT.putCoin();
					} catch(Exception e){}
					
					try{

						semAT.getCoin(5);
						mqttH.disconnect(0);
						semAT.putCoin();
					
						infoS.setTRKstate(false);
						infoS.setEnableCSD(true);
					
						semAT.getCoin(5);
							// Close GPRS channel
							//System.out.println("SocketGPRSTask: KILL GPRS");
							infoS.setATexec(true);
							mbox2.write("at+cgatt=0\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				
						semAT.putCoin();
						
						Thread.sleep(5000);
					} catch(InterruptedException e){
					} catch (Exception e){}
					
					for(timeOutSocket = 0;timeOutSocket<100;timeOutSocket++){
						if(infoS.isCloseTCPSocketTask())
							break;
						try{
							Thread.sleep(1000);
						}catch(InterruptedException e){}
					}
					infoS.setApriGPRS(true);
				}
				
			
			}
			else{
				if(debug)
					System.out.println("In no TRK ON");
				try{
					if(infoS.getInfoFileString(TrkState).equals("OFF")){
						
						if(!firstTime){
							semAT.getCoin(5);
							mqttH.disconnect(0);
							semAT.putCoin();
						}
						if(debug)
							System.out.println("In TRK OFF");
						
						infoS.setTRKstate(false);
						infoS.setEnableCSD(true);
						semAT.putCoin();	// release AT interface
						try{
							semAT.getCoin(5);
								// Close GPRS channel
								//System.out.println("SocketGPRSTask: KILL GPRS");
								infoS.setATexec(true);
								mbox2.write("at+cgatt=0\r");
								while(infoS.getATexec()) { Thread.sleep(whileSleep); }
							semAT.putCoin();
						} catch(InterruptedException e){}
						infoS.settrasmetti(true); //[MB] 20140530
						infoS.setApriGPRS(true);	//[MB] 20140530
					}
					Thread.sleep(2000);
				}catch (InterruptedException e){}
			}
		
			// Variable to test if task working
			timer3++;
			infoS.setTask3Timer(timer3);
			infoS.setTickTask3WD();
		
		}// while
	} //run

} //SocketGPRStask

