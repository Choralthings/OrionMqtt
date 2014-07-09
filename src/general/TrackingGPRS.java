/*	
 * Class 	TrackingGPRS
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import choral.io.Can;
import com.cinterion.io.*;

/**
 * Thread that controls the periodic sending of GPS positions (GPRMC strings)
 * through GPRS connection, using a timer with time set. 
 * 
 * @version	1.02 <BR> <i>Last update</i>: 25-10-2007
 * @author 	alessioza
 * 
 */
public class TrackingGPRS extends ThreadCustom implements GlobCost{
	
	/* 
	 * local variables
	 */
	/** Store received msg into local mailbox */
	private String 	msgRicevuto = "";
	/** Sleep indicator of current tracking */
	private boolean sospendiTrack = false;
	/** Tracking execution indicator */
	private boolean trackingAttivo = false;
	private boolean exit = false;
	protected BCListener BCL;
	private String	infoGps;
	private boolean invia_to_socket = false;
	private boolean setStop = true;
	private double ctrlSpeed = 0.0;
	private int val_insensibgps;
	Can can;
	int counterCanMsg = 0;
	int timer2=0;
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore  'semAT', for the exclusive use of the AT resource
	 * InfoStato  'infoS', status informations about application
	 * Mailbox    'mbox3', to receive msg with this thread
	 * Mailbox    'mboxMAIN', to send msg to AppMain
	 * Mailbox    'mbox2', to send msg to th2 (ATsender)
	 * Mailbox    'mbox5', to send msg to th5
	 * DataStore  'dsData', to store GPS strings
	 */

	
	/* 
	 * constructors
	 */
	public TrackingGPRS() {
		//System.out.println("Th*TrackingGPRS: CREATED");
		can = new Can();
	}
	
	
	/*
	 * methods
	 */
	/**
	 * Thread execution code:
	 * <BR> ---------- <BR>
	 * Performed operations: <br>
	 * <ul type="disc">
	 *  <li> Create task to send strings through GPRS connection and pass parameters;
	 * 	<li> Loop for receive message sent from tasks and AppMain (about trasking
	 *       start and suspen).
	 * </ul>
	 */
	public void run() {
		
		//System.out.println("Th*TrackingGPRS: STARTED");
		while(!infoS.isCloseTrackingGPRS()){
			//try {
		
				// Bearer Control
				BCL = new BCListener();	
				BearerControl.addListener(BCL);
			
				/* 
				 * WAIT FOR MESSAGES into MAILBOX for GPRS TRACKING START
				 * 
				 */
				//System.out.println("Th*TrackingGPRS: Ready to start GPRS TRACKING...");
					
				while(true) {
					
					if (msgRicevuto.equalsIgnoreCase(timeoutExpired)) {
						//System.out.println("GPRS TIMEOUT ON MBOX3");
						try{
							semAT.getCoin(5);
							infoS.setATexec(true);
							mbox2.write("AT+CGATT=0\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
							semAT.putCoin();
						}catch(InterruptedException e){}
					}
					
					if (msgRicevuto.equalsIgnoreCase(rebootTrack)) {
						break;
					}
			
					// *** FORCED CLOSURE OF THREAD			
					
					if (msgRicevuto.equalsIgnoreCase(exitTrack)) {
						
						// Case 1: active tracking  -> suspend current tracking and exit
						if (trackingAttivo == true) {
							sospendiTrack = true;
							exit = true;
						}
						// Case 2: not active tracking -> exit directly
						else break;
						
					} //stopTrack
					
					
					// *** FINISHED to SEND strings through GPRS	
					
					if (msgRicevuto.equalsIgnoreCase(invioCompletato)) {
							
						if(debug){
							System.out.println("Th*TrackingGPRS: Tracking finished!!");	
						}
						// reset timer and task, and enable CSD
						sospendiTrack = true;
											
					} //invioCompletato
				
					
					// *** Suspend current tracking, if active			
					
					if (sospendiTrack == true) {
						
						infoS.setEnableCSD(true);
						
						if(debug){
							System.out.println("Th*TrackingGPRS: Reset socket task e timer completed");	
						}
						
						if (exit == true) break;
						
					} //sospendiTrack
					
					if ( msgRicevuto.equalsIgnoreCase(trackNormale) || msgRicevuto.equalsIgnoreCase(trackMovimento) || 
						 msgRicevuto.equalsIgnoreCase(trackAttivChiave) || msgRicevuto.equalsIgnoreCase(trackDisattivChiave) ||
						 msgRicevuto.equalsIgnoreCase(trackAlarmIn1) || msgRicevuto.equalsIgnoreCase(trackAlarmIn2) ||
						 msgRicevuto.equalsIgnoreCase(trackBatteria) || msgRicevuto.equalsIgnoreCase(trackSMS) ||
						 msgRicevuto.equalsIgnoreCase(trackAlive) || msgRicevuto.equalsIgnoreCase(trackCodice) ||
						 msgRicevuto.equalsIgnoreCase(trackUrcSim)) {
						
						String datoToSend;
						//if (GPRSposFormat == CHORAL) {
						if(infoS.getInfoFileString(TrackingProt).equals("USR")){
							infoGps = null;
							Posusr msg = new Posusr();
							msg.addInfoStato(infoS);
							String tempRMC = (String)dsDataRMC.getLastValid();
							String tempGGA = (String)dsDataGGA.getLastValid();
							if(tempRMC == null)
								tempRMC = "";
							if(tempGGA == null)
								tempGGA = "";
							if(tempRMC.equals(tempGGA))
								tempRMC = "";
							if((tempRMC != null) && (!(tempRMC.equals(""))))
								infoGps = msg.set_posusr(tempRMC, tempGGA);
							else
								infoGps = infoS.getInfoFileString(Header)+","+infoS.getInfoFileString(IDtraker)+defaultGPS;
							datoToSend = infoGps;
						}
						else
							datoToSend = (String)dsDataRMC.getLastValid();
						
						if((datoToSend == null) || (datoToSend.indexOf("null")>=0)){
							if(infoS.getInfoFileString(TrackingProt).equals("USR")){
								datoToSend = infoS.getInfoFileString(Header)+","+infoS.getInfoFileString(IDtraker)+defaultGPS+",<ERROR>*00";
							}
							/*else{
								datoToSend = defaultGpsNMEA;
							}*/
								
						}
						if(infoS.getInfoFileString(TrackingProt).equals("USR")){
							if(msgRicevuto.equalsIgnoreCase(trackMovimento))
								datoToSend = datoToSend + ",ALR<" + alarmMovimento + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackAttivChiave))
								datoToSend = datoToSend + ",ALR<" + alarmChiaveAttivata + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackDisattivChiave))
								datoToSend = datoToSend + ",ALR<" + alarmChiaveDisattivata + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackBatteria))
								datoToSend = datoToSend + ",ALR<" + alarmBatteria + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackAlarmIn1))
								datoToSend = datoToSend + ",ALR<" + alarmIn1 + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackAlarmIn2))
								datoToSend = datoToSend + ",ALR<" + alarmIn2 + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackAlive))
								datoToSend = datoToSend + ",ALR<" + alive + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackCodice))
								datoToSend = datoToSend + ",COD<" + infoS.getCode() + ">";
							else if(msgRicevuto.equalsIgnoreCase(trackUrcSim))
								datoToSend = datoToSend + ",ALR<URC SIM>";
							else;
						}
						
						if(infoS.getInfoFileString(TrackingProt).equals("USR")){
							datoToSend = datoToSend + "*" + this.getChecksum(datoToSend).toUpperCase();
						}
						
						if(debug)
							System.out.println(datoToSend);
						new LogError("Trk " +datoToSend);
						
						//System.out.println("TIME DATA: " + datoToSend);
						//System.out.println("RAM DATA: " + infoS.getDataRAM());
						
						// Aggiunto il 29/12/2011 [MB]
						ctrlSpeed =infoS.getSpeedDFS(); 
						if(debug_speed){
							ctrlSpeed = infoS.getSpeedGree();
							//System.out.println("SPEED " + ctrlSpeed);
						}
						
						infoS.setSpeedForTrk(ctrlSpeed);
						try{
							val_insensibgps = Integer.parseInt(infoS.getInfoFileString(InsensibilitaGPS));
						}catch(NumberFormatException e){
							val_insensibgps = 0;
						}
												
						if(msgRicevuto.equalsIgnoreCase(trackNormale)){
						
							if ((ctrlSpeed >= val_insensibgps)){
								invia_to_socket = true;
								setStop = false;
							}
							else 
							{
								if((!infoS.getPreAlive())&&(ctrlSpeed <= val_insensibgps)&&(infoS.getPreSpeedDFS() > val_insensibgps)){
										
									invia_to_socket = true;
									setStop = false;
								}
								else{
									if (!setStop){
										invia_to_socket = true;
										setStop = true;
									}
									else{
										invia_to_socket = false;
									}
								}
							}
						}
						else{
							invia_to_socket = true;
						}
						if(invia_to_socket){
							try{
								// semophore for queue
								while(!InfoStato.getCoda()){Thread.sleep(1);}
							}catch (InterruptedException e){}
							
							if((infoS.getDataRAM()).equals("")){
								infoS.setDataRAM(datoToSend); 
								if(debug)
									System.out.println(datoToSend);
							}
							else{								
								
								String datoToMem = infoS.getDataRAM();
								infoS.setDataRAM(datoToSend);
								
								/*
								 * Save data to send
								 */
							
								int temp = infoS.getInfoFileInt(TrkIN);
								//System.out.println("Th*TrackingGPRS: pointer in - " + temp);
								if ((temp >= codaSize) || (temp < 0)) {
									temp = 0;
								}
								new LogError("Th*TrackingGPRS: pointer in - " + temp + " " + datoToMem);
								infoS.saveRecord(temp, datoToMem);
								temp++;
								if ((temp >= codaSize) || (temp < 0))
									temp = 0;
								infoS.setInfoFileInt(TrkIN, "" + temp);
								file.setImpostazione(TrkIN, "" + temp);
								InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
								
		    					invia_to_socket = false;
							}
							InfoStato.freeCoda();
						}
					}			
					
					
					//variable to verify task operation
					timer2++;
					infoS.setTask2Timer(timer2);
					/*
					 * Read message from Mailbox, if present
					 * 
					 * read() method is BLOCKING, until a message is received
					 * (of some sort) while loop is stopped
					 */ 
					msgRicevuto = (String)mbox3.read();
					if(debug){
						System.out.println("Th*TrackingGPRS: Received message: " + msgRicevuto);
					}
					
					
					// *** Check if tracking is active				
					if (trackingAttivo == true) {
						sospendiTrack = true;
					}				
					
				} //while(true)		
				
				if(debug){
					System.out.println("Th*TrackingGPRS: END");
				}
				infoS.setTrackingAttivo(false);
				
				try{
					Thread.sleep(1000);
				}catch(InterruptedException e){}
			
			if(infoS.closeGPRS()){
				infoS.setCloseGPRS(false);
				// Sending communication to AppMain
				mboxMAIN.write(msgCloseGPRS);
				break;
			}
			
		} //while
		
	} //run
	
	public String getHexaString(int num){
		
		String dato = Integer.toHexString(num);
		if(dato.length()>2)
			dato = dato.substring(dato.length()-2);
		if(dato.length()<2)
			dato = "0" + dato;
		return dato;
	
	}
} //TrackingGPRS

