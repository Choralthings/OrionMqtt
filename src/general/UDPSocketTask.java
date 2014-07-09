/*	
 * Class 	UDPSocketTask
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;
import java.util.*;

import javax.microedition.io.*;

import com.cinterion.io.BearerControl;

/**
 * Task that executes send of strings through GPRS connection using UDP sockets.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 24-06-2009
 * @author 	matteobo
 * 
 */

public class UDPSocketTask extends ThreadCustom implements GlobCost {	
	/* 
	 * local variables
	 */
	private double ctrlSpeed =0;
	private boolean close = false;
	private boolean exitTRKON = true;
	private int 	temp;
	private boolean ram = true;
	/** Full string to send through GPRS */
	private String 	outText;
	// socket TCP
	UDPDatagramConnection	udpConn;
	Datagram		dgram;
	byte[]			buff;
	private String	destAddressUDP;
	private int val_insensibgps;
	private int countDownException = 0;
	private boolean errorSent = false;
	BCListenerCustom list;
	String answer = "";
	
	/* 
	 * constructors
	 */
	public UDPSocketTask() {
		if(debug){
			System.out.println("TT*UDPSocketTask: CREATED");
		}
	}
	
	/*
	 * methods
	 */
	/**
	 * Task execution code:
	 * <BR> ---------- <BR>
	 * Performed operations: <br>
	 * <ul type="disc">
	 *  <li> Add alarm to position strings, if necessary;
	 * 	<li> Send string;
	 *  <li> Check the number of sent strings.	 
	 * </ul>
	 */
	public void run() {
		
		list = new BCListenerCustom();
		list.addInfoStato(infoS);
		BearerControl.addListener(list);
		
		while (!infoS.isCloseUDPSocketTask()){
			
			//if(false){
			if((infoS.getInfoFileString(TrkState).equals("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS")) && infoS.getInfoFileString(GPRSProtocol).equals("UDP") && ((infoS.getInfoFileInt(TrkIN) != infoS.getInfoFileInt(TrkOUT)) || !infoS.getDataRAM().equals(""))){
			
				exitTRKON = false;
				
				try {
			
					// Indicates if GPRS SOCKET is ACTIVE
					//System.out.println("TT*UDPSocketTask: START");
					infoS.setIfsocketAttivo(true);
					destAddressUDP = "datagram://" + infoS.getInfoFileString(DestHost) + ":" + infoS.getInfoFileString(DestPort);
				
			
					/*
					 * Once this task has been started, it is completely
					 * finished before proceeding to a re-use, even if the
					 * timeout expires (so there may be a FIX GPRS timeout
					 * expired!)
					 */
					try {
						try{
							while (!InfoStato.getCoda()) Thread.sleep(1L);
						}catch(InterruptedException e){}
						
						if(infoS.getInfoFileInt(TrkIN) == infoS.getInfoFileInt(TrkOUT)){
							outText = infoS.getDataRAM();
							ram = true;
						}
						else{
							ram = false;
							temp = infoS.getInfoFileInt("TrkOUT");
							System.out.println("TT*UDPSocketTask: pointer out - " + temp);
							if ((temp >= codaSize) || (temp < 0))
								temp = 0;
							outText = infoS.getRecord(temp);
							new LogError("TT*UDPSocketTask: pointer out - " + temp + " " + outText);
							System.out.println("TT*UDPSocketTask: data in queue: " + outText);
						}
						
						System.out.println("TT*UDPSocketTask: string to send through GPRS:\r\n" + this.outText);

						ctrlSpeed = infoS.getSpeedForTrk();
						if(debug_speed){
							ctrlSpeed = infoS.getSpeedGree();
							System.out.println("SPEED " + ctrlSpeed);
						}
						try{
							val_insensibgps = Integer.parseInt(infoS.getInfoFileString(InsensibilitaGPS));
						}catch(NumberFormatException e){
							val_insensibgps = 0;
						}
						//new LogError("Actual speed: " + ctrlSpeed + ". Val insens: " + val_insensibgps);
												
						if  (ram){
							
							//System.out.println("ACTUAL SPEED: " + this.ctrlSpeed);
							//System.out.println("outText.indexOf(ALARM) " + (this.outText.indexOf("ALARM") > 0));
							//System.out.println("outText.indexOf(ALIVE) " + (this.outText.indexOf("ALIVE") > 0));
							//System.out.println("SPEED LIMIT: " + this.val_insensibgps);
							//System.out.println("PREVIOUS MESSAGE IS ALIVE: " + this.infoS.getPreAlive());
							//System.out.println("SPEED LIMIT: " + this.val_insensibgps);
							//System.out.println("PREVIOUS SPEED: " + this.infoS.getPreSpeedDFS());
							
							if (this.ctrlSpeed > this.val_insensibgps) {
								System.out.println("Speed check ok.");
								infoS.settrasmetti(true);
								if (this.infoS.getInvioStop()) {
									infoS.setApriGPRS(true);
								}
								infoS.setInvioStop(false);
							}
							else 
							{
								if((outText.indexOf("ALARM")>0) || (outText.indexOf("ALIVE")>0)){
									System.out.println("Alarm");
									infoS.settrasmetti(true);
									infoS.setApriGPRS(true);
								}
								else{
									
									if((!infoS.getPreAlive())&&(ctrlSpeed <= val_insensibgps)&&(infoS.getPreSpeedDFS() > val_insensibgps)){
										
										System.out.println("Speed check less then insensitivity, previous speed is greater");
										infoS.settrasmetti(true);
										if (infoS.getInvioStop()== true){
											infoS.setApriGPRS(true);
										}
										infoS.setInvioStop(false);
										
									}
									else{
										
										System.out.println("Speed check failed.");
										if (infoS.getInvioStop()== false){
											System.out.println("Send stop coordinate.");
											infoS.settrasmetti(true);
											infoS.setInvioStop(true);
											infoS.setChiudiGPRS(true);
																						
											//new LogError("Send stop.");
										}
									}
								}
							}
							if (this.outText.indexOf("ALIVE") > 0) {
								System.out.println("ALIVE MESSAGE");
								infoS.setPreAlive(true);
							}
							else {
								infoS.setPreAlive(false);
								System.out.println("NO ALIVE MESSAGE");
							}
						}
						else{
							//new LogError("From store.");
							
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
								try{
									udpConn = (UDPDatagramConnection) Connector.open(destAddressUDP);
								}catch(Exception e){
									System.out.println("TT*UDPSocketTask: Connector.open");
								}
								infoS.setApriGPRS(false);
							}
							
							try{
								//mem2 = r.freeMemory();
							    //System.out.println("Free memory after allocation: " + mem2);
								if((outText == null) || (outText.indexOf("null")>=0)){
									outText = infoS.getInfoFileString(Header)+","+infoS.getInfoFileString(IDtraker)+defaultGPS+",<ERROR>*00";
									buff = outText.getBytes();
								}
								System.out.println("OPEN DATAGRAM");
								System.out.println(outText);
								dgram = udpConn.newDatagram(outText.length());
								buff = new byte[outText.length()];
								System.out.println("SEND DATAGRAM");
								buff = outText.getBytes();
								new LogError("outText = " + outText);
								dgram.setData(buff,0,buff.length);
								udpConn.send(dgram);
								int gprsCount = 0;
								answer = "";
								String ack = infoS.getInfoFileString(Ackn);
								if(!infoS.getInfoFileString(Ackn).equals("")){
									while(true){
										dgram.reset();
										dgram.setLength(infoS.getInfoFileString(Ackn).length()+1);
										udpConn.receive(dgram);
										byte[] data = dgram.getData();
										answer = new String(data);
										answer = answer.substring(0, ack.length());
										if(debug){
											System.out.println("ACK: " + answer);
										}
										if(answer.equals(ack)){
											new LogError("ACK");
											if(debug)
												System.out.println("ACK RECEIVED");
											break;
										}
										else{
											if(debug)
												System.out.println("WAITING ACK");
											try{
											Thread.sleep(1000);
											}catch(InterruptedException e){}
											gprsCount++;
										}
										if(gprsCount>15){
											new LogError("NACK");
											infoS.setReboot();
											errorSent = true;
											break;
										}
									}
								}
								
							}catch(Exception err){
								System.out.println("TT*UDPSocketTask: Exception err");
								new LogError("TT*UDPSocketTask: Exception during out text" + err.getMessage());
								infoS.setReboot();
								errorSent = true;
								break;
							}
							//new LogError(outText);
							if(debug)
								System.out.println(outText);
						
							if (infoS.getChiudiGPRS()==true){
							
								infoS.setTRKstate(false);
								try{
									System.out.println("TT*UDPSocketTask: close UDP");
									udpConn.close();
								}catch(NullPointerException e){
									infoS.setChiudiGPRS(false);
								}
								infoS.setChiudiGPRS(false);
							}
						}
						
						System.out.println("BEARER: " + infoS.getGprsState());
						if(!infoS.getGprsState()){
							errorSent = true;
							System.out.println("BEARER ERROR");
							new LogError("BEARER ERROR");
						}		
						
						if(ram){
							if(!errorSent){
								infoS.setDataRAM("");
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
						 //r.gc(); // request garbage collection

						 //mem2 = r.freeMemory();
						 //System.out.println("Free memory after collecting" + " discarded Integers: " + mem2);
						
					} catch (IOException e) {
						close = true;
						String msgExcept = e.getMessage();
						System.out.println("TT*UDPSocketTask: exception: "+msgExcept);
				
						//new LogError("SocketGPRStask IOException: " + e);
						infoS.setIfsocketAttivo(false);
						
						infoS.setApriGPRS(false);
						infoS.setChiudiGPRS(false);
					
					} catch (EmptyStackException e) {
						close = true;
						//System.out.println("exception: " + e.getMessage());
						e.printStackTrace();
																	  
						//new LogError("SocketGPRStask EmptyStackException");
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
					
				if(close){
					
					try{
						semAT.getCoin(5);
							infoS.setATexec(true);
							mbox2.write("at^smong\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						semAT.putCoin();
					} catch(Exception e){}
						
					
					try{
						//System.out.println("***************CLOSE******************");
						try{
							udpConn.close();
						}catch(NullPointerException e){
							
						}
						//System.out.println("***************CLOSED******************");
					
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
						
					} catch(IOException e){
						
					} catch(Exception e){
						
					}
					System.out.println("WAIT - DISCONNECT GPRS");
					for(countDownException = 0;countDownException < 100;countDownException++){
						if(infoS.isCloseUDPSocketTask())
							break;
						try{
							Thread.sleep(1000);
						}catch(InterruptedException e){}
					}
					infoS.setApriGPRS(true);
				}
			}
			else{
				
				try{
					if(infoS.getInfoFileString(TrkState).equals("OFF")){
						
						infoS.setTRKstate(false);
						infoS.setEnableCSD(true);
						semAT.putCoin();	// release AT interface
						try{
							semAT.getCoin(5);
								// Close GPRS channel
								//System.out.println("SocketGPRSTask: TRK OFF KILL GPRS");
								infoS.setATexec(true);
								mbox2.write("at+cgatt=0\r");
								while(infoS.getATexec()) { Thread.sleep(whileSleep); }
							semAT.putCoin();
						} catch(InterruptedException e){}
					}
					Thread.sleep(2000);
				}catch (InterruptedException e){}
			}
		}// while
	} //run
} //UDPSocketTask
