/*	
 * Class 	Seriale
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/** 
 * @version	1.07 <BR> <i>Last update</i>: 04-08-2008
 * @author 	matteobo
 * 
 */

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;

import com.cinterion.io.file.FileConnection;

public class Seriale extends ThreadCustom implements GlobCost {

	// Seriale
	OutputStream 	serialOut;
	InputStream 	serialIn;
	private String 	comando;
	private int		rx; 
	private boolean auth = false;
	private boolean confirmPWD = false;
	private String	newPWD,strCHpwd,info,transportType;
	private int		infoInt;
	private boolean read_start = false;
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the AT resource
	 * flag 	 'ATexec', indicates if AT resource is busy
	 * Mailbox   'mbox2', to receive msg with this thread
	 * Mailbox   'mboxMAIN', to send msg to AppMain
	 */

	
	/* 
	 * constructors
	 */
	public Seriale() {
	
	}
	
	/*
	 * methods
	 */
	public void run() {
		
		try {
			CommConnection connASC0 = (CommConnection)Connector.open(ASC0);
		    serialIn =  connASC0.openInputStream();
		    serialOut = connASC0.openOutputStream();
		    
		    			    
		} catch(IOException e) {
			//System.out.println("Th*Seriale: IOException");
		} //catch	
		
		while(true) {
						
			try {
				
				Thread.sleep(200);
		
				// If PWD is null, no authentication required
				if (infoS.getInfoFileString(PasswordCSD).equalsIgnoreCase("")) {
					auth=true;
					if(debug){
						System.out.println("Th*Seriale: no authentication required because PWD is null");
					}
				}
									
				while(true) {
									
					try {
						
						if(false){
							/*
							 * READ COMMAND
							 */ 
							rx = 0;
							comando = "";
							
							do {
								serialOut.write(">00\r\n".getBytes());
								serialOut.write(">10\r\n".getBytes());
								rx = serialIn.read();
								if(rx != '\n' ){
									if((rx != '>') && (rx != '\r') && (rx != '\n') ){
										if(rx == '*'){
											comando = "";
											serialOut.write(">01\r\n".getBytes());
											serialOut.write(">10\r\n".getBytes());
										}
										else{
											serialOut.write(">00\r\n".getBytes());
											serialOut.write(">11\r\n".getBytes());
										}
										if (rx >= 0) 
											System.out.print((char)rx);
										comando = comando + (char)rx;
					    				
									}
								}
							} while((char)rx != '#');
							serialOut.write(">01\r\n".getBytes());
							serialOut.write(">11\r\n".getBytes());
							// Process command if received '#'
							if(comando.length()>2)
								comando = comando.substring(1,comando.length()-1);
							//System.out.println("Th*Seriale, received code: " + comando + " ***");
							infoS.setCode(comando);
							Thread.sleep(3000);
							mbox3.write(trackCodice);
								    			
						}		    			
							    			
						// #PWD --> verify authentication	
						if(true){
							/*
							 * READ COMMAND
							 */ 
							rx = 0;
							comando = "";
							do {
								rx = serialIn.read();
								if(rx != '\n' ){
									if (rx >= 0) System.out.print((char)rx);
									// update read string
									if ((byte)rx != '\r') {
										//serialOut.write((byte)rx);
									    comando = comando + (char)rx;
				    				} else serialOut.write("\r\n".getBytes());
									
								}
							} while((char)rx != '\r');
							// Process command in received '\r'	
							if(debug){
								System.out.println("Th*Seriale, received command: " + comando + "***");
							}
							
						if (comando.indexOf(PWD+" ")>=0) {
							if(debug){
								System.out.print("Th*Seriale: check authentication...");
							}
							if (comando.indexOf(PWD + " " + infoS.getInfoFileString(PasswordCSD))>=0) {
								if(debug){
									System.out.println("OK");
							    }
							    serialOut.write(PWDok.getBytes());
							    // authentication OK
							    auth = true;
							}
							else {
								if(debug){
									System.out.println("ERROR");
							    }
								auth = false;
							    serialOut.write(PWDerr.getBytes());
							}
						}
						// REBOOT
						else if (comando.indexOf(REBOOT)>=0) {
							if(debug){
								System.out.print("Th*Seriale: SYSTEM RESTART");
							}
			
							// process return string
							if(debug){
								System.out.println("OK");
							}
							serialOut.write(ACK.getBytes());
							
							semAT.getCoin(5);
								if(debug){
									System.out.println("Seriale: module restart in progress...");
								}
								infoS.setATexec(true);
								mbox2.write("AT+CFUN=1,1\r");
								while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						    semAT.putCoin();
			
		    			}
							    			
						// #PWD and no authentication
						else if (auth==false) {
							if(debug){
								System.out.println("Th*Seriale: authentication failed");
							}
							serialOut.write((NACK).getBytes());
						}
		
							    			
						//** ACCEPTED MESSAGES ONLY WITH AUTHENTICATION **//
							    			
						// #CFG					
						else if (comando.indexOf(CFG)>=0 && auth==true) {
							if(debug){
								System.out.print("Th*Seriale: Configuration options list...");
							}
							// process return string
							if(debug){
								System.out.println("OK");
							}
						    serialOut.write(("\r\n" + "Greenwich rev. " + revNumber + ", " + dataRev + "\r\n").getBytes());
						    serialOut.write((moduleCodeRev + infoS.getREV() + "\n\r").getBytes());
						    serialOut.write(("IMEI: " + infoS.getIMEI() + "\r\n").getBytes());
						    serialOut.write((SETID + ": " + infoS.getInfoFileString(IDtraker) + "\r\n").getBytes());
						    serialOut.write((SNOP + ": " + infoS.getInfoFileString(Operatore) +  "\r\n").getBytes());
						    //serialOut.write((ACTOP + "\r\n").getBytes());
						    serialOut.write((GPRSCFG + ": " + infoS.getInfoFileString(apn) + "," 
						    			+ infoS.getInfoFileString(GPRSProtocol)+ "," + infoS.getInfoFileString(DestHost) 
						    			+ "," + infoS.getInfoFileString(DestPort) + "\r\n").getBytes());
						    serialOut.write((TRKCFG + ": " + infoS.getInfoFileString(TrackingType) + "," 
					    			+ infoS.getInfoFileString(TrackingProt)+ "," + infoS.getInfoFileString(Header) 
					    			+ "," + infoS.getInfoFileString(Ackn) + "," + infoS.getInfoFileString(GprsOnTime) + "\r\n").getBytes());
						    serialOut.write((TRKTM + ": " + infoS.getInfoFileString(TrackingInterv) + "\r\n").getBytes());
						    serialOut.write((TRK + ": " + infoS.getInfoFileString(TrkState) + "\r\n").getBytes());
						    serialOut.write((PUBTOPIC + ": " + infoS.getInfoFileString(PublishTopic) + "\r\n").getBytes());
						    serialOut.write((SLPTM + ": " + infoS.getInfoFileInt(OrePowerDownOK) + "\r\n").getBytes());
						    serialOut.write((SLP + ": " + infoS.getInfoFileString(SlpState) + "\r\n").getBytes());
						    serialOut.write((STILLTM  + ": " + infoS.getInfoFileInt(StillTime) + "\r\n").getBytes());
						    serialOut.write((MOVSENS + ": " + infoS.getInfoFileString(MovState) + "\r\n").getBytes());
						    serialOut.write((IGNCFG + ": " + infoS.getInfoFileString(IgnState) + "\r\n").getBytes());
						    serialOut.write((UARTCFG + ": " + infoS.getInfoFileInt(UartSpeed) + "," 
					    			+ infoS.getInfoFileString(UartGateway)+ "," + infoS.getInfoFileString(UartHeaderRS) 
					    			+ "," + infoS.getInfoFileString(UartEndOfMessage) + "," + infoS.getInfoFileInt(UartAnswerTimeOut)
					    			+ "," + infoS.getInfoFileInt(UartNumTent) + "," + infoS.getInfoFileString(UartEndOfMessageIP)
					    			+ "," + infoS.getInfoFileString(UartIDdisp) + "," + infoS.getInfoFileInt(UartTXtimeOut) + "\r\n").getBytes());
						    serialOut.write((SIG + ": " + infoS.getCSQ() + "," + infoS.getNumSat() + "\r\n").getBytes());
						    serialOut.write((VBAT + ": " + infoS.getBatteryVoltage() + "\r\n\r\n").getBytes());
						    serialOut.write(ACK.getBytes());
						    						
						}
						   			
						// #CHPWD --> can change password						
						else if (comando.indexOf(CHPWD+" ")>=0 && auth==true) {
							if(debug){
								System.out.print("Th*Seriale: Cambio password...");
							}
							// reset new password
							newPWD = "";
							strCHpwd = CHPWD + " " + infoS.getInfoFileString(PasswordCSD);
							// check
							if (comando.indexOf(strCHpwd)>=0) {
								// extract new password
							    if (comando.length()==comando.indexOf(strCHpwd)+strCHpwd.length()+1)
							    	newPWD = "";
							    else 
							    	newPWD = comando.substring(comando.indexOf(strCHpwd)+strCHpwd.length()+1);
							    
							    if(debug){
							    	System.out.print("new password: " + newPWD + " ...");
							    }
							    // check length PWD
							    if (newPWD.length()<=15) {
							    	// request confirmation of new password
								    serialOut.write(CHPWDconfirm.getBytes());
								    if(debug){
								    	System.out.println("OK, wait confirm");
								    }
								    confirmPWD = true;				    					
							    } else {
							    	// Password too long
							    	if(debug){
							    		System.out.println("ERROR, password too long");
							    	}
							    	serialOut.write(CHPWDlong.getBytes());
							    } //else
							} else {
								// change password not valid
							    if(debug){
							    	System.out.println("ERROR, change password not valid");
							    }
							    serialOut.write(CHPWDerr.getBytes());
							} //strCHpwd
						} //CHPWD
							    			
						// Confirm modified password
						else if (confirmPWD==true && auth==true) {
							if(debug){
								System.out.print("Th*Seriale: password confirmation...");
							}
							if (comando.indexOf(newPWD)>=0) {
								// modification OK, change value on file and write
						    	infoS.setInfoFileString(PasswordCSD, newPWD);
						    	// write immediately on file the change
						    	file.setImpostazione(PasswordCSD, newPWD);
						    	InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
						    	// output
						    	if(debug){
						    		System.out.println("OK");
						    	}
							    serialOut.write(CHPWDok.getBytes());
						    } else {
						    	// Different password confirmed
						    	if(debug){
						    		System.out.println("ERROR, password not confirmed");
						    	}
						    	serialOut.write(CHPWDerr.getBytes());
						    } //else
							confirmPWD = false;
						}
							    			
						// #SETID --> modify DeviceID					
						else if (comando.indexOf(SETID+" ")>=0 && auth==true) {
							if(debug){
								System.out.print("Th*Seriale: set DeviceID...");
							}
						/*
						 * Check for presence of at least 1 char and max 15 chars
						 */
						if (comando.length() >= SETID.length()) {
							// extract information
							info = comando.substring(SETID.length()+1);
							if(debug){
								System.out.println(info);
							}
		
							// check length
							if (info.length()<=15) {
								if(debug){
									System.out.println("OK");
								}
								// modification OK, change value on file and write
							    infoS.setInfoFileString(IDtraker, info);
							    // write immediately on file the change
							    file.setImpostazione(IDtraker, info);
							    InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
								serialOut.write(ACK.getBytes());		    						
							} else {
								if(debug){
									System.out.println("ERROR, DeviceID too long.");
								}
								serialOut.write(NACK.getBytes());
							}
							    				
						} else {
							if(debug){
								System.out.println("ERROR");
							}
							serialOut.write(NACK.getBytes());
						}
					}
							    			
					// #GPRSCFG					
					else if (comando.indexOf(GPRSCFG+" ")>=0 && auth==true) {
						if(debug){
							System.out.print("Th*Seriale: set ConnProfileGPRS snd apn...");
						}
						/*
						 * Check for presence of at least 1 char
						 */
						if (comando.length() > (GPRSCFG+" ").length() && checkComma(3,comando)){
								    				
							// extract information APN (user and pwd ignored at the moment)
							info = comando.substring(GPRSCFG.length()+1, comando.indexOf(","));
							comando = comando.substring(comando.indexOf(",")+1);
								    				
							// check length (APN max 39 char)
							if (info.length()<=39) {
								if(debug){
									System.out.println("OK");
								}
								// modification OK, change value on file and write
								infoS.setInfoFileString(ConnProfileGPRS, "bearer_type=GPRS;access_point="+info);
								infoS.setInfoFileString(apn, info);			    						
								// write immediately on file the change
								file.setImpostazione(ConnProfileGPRS, "bearer_type=GPRS;access_point="+info);
								file.setImpostazione(apn, info);
							    						
								//extract protocol type
								if((comando.substring(0, comando.indexOf(",")).equals("TCP")) || (comando.substring(0, comando.indexOf(",")).equals("UDP"))){
								    					
									// extract <transportType>
									transportType = (comando.substring(comando.indexOf(" ")+1, comando.indexOf(",")));
									comando = comando.substring(comando.indexOf(",")+1);
									if(debug){
										System.out.println("Th*Seriale, transportType: " + transportType);
									}
										    				
									// extract <ip>
									info = comando.substring(0, comando.indexOf(","));
									comando = comando.substring(comando.indexOf(",")+1);
									if(debug){
										System.out.println("Th*Seriale, IP: " + info);
										System.out.println("Th*Seriale, PORT: " + comando);
									}
										    				
									// set and check (max 39 char)
									if (info.length()<=39) {
										infoS.setInfoFileString(DestHost, info);
										file.setImpostazione(DestHost, info);
										    					
										// set <port>
										infoS.setInfoFileString(DestPort, comando);
										file.setImpostazione(DestPort, comando);
										    				
										infoS.setInfoFileString(GPRSProtocol, transportType);
										file.setImpostazione(GPRSProtocol, transportType);
											    				
										serialOut.write(ACK.getBytes());	
																				    					
										// write to file
										InfoStato.getFile();
										file.writeSettings();
										InfoStato.freeFile();
										
										infoS.setCloseGPRS(true);
										mbox3.write(rebootTrack);
									}
								}
								else {
									if(debug){
										System.out.println("ERROR");
									}
									serialOut.write(NACK.getBytes());
								}
							    							    						
							}
							else {
								if(debug){
									System.out.println("ERROR, APN too long.");
								}
								serialOut.write(NACK.getBytes());
							}
								    				
						} else {
							if(debug){
								System.out.println("ERROR");
							}
							serialOut.write(NACK.getBytes());
						}
					}
						
					// #POSREP						
					else if (comando.indexOf(POSREP)>=0 && auth==true) {
						if(comando.indexOf(POSREP + " ENA")>=0){
							serialOut.write(ACK.getBytes());
							infoS.setUartTraspGPS(true);
						}
						else{
							if(comando.indexOf(POSREP + " DIS")>=0){
								infoS.setUartTraspGPS(false);
								serialOut.write(ACK.getBytes());
							}
							else serialOut.write(NACK.getBytes());
						}
						
					}
							    			
					// #POSUSR						
					else if (comando.indexOf(POSUSR)>=0 && auth==true) {
						if(debug){
							System.out.print("Th*Seriale: single position string...");
						}
		
						// process return string
						if(debug){
							System.out.println("OK");
						}
						Posusr msg = new Posusr();
    					msg.addInfoStato(infoS);
    					String tempRMC = (String)dsDataRMC.getLastValid();
    					String tempGGA = (String)dsDataGGA.getLastValid();
    					String temp;
    					if((tempRMC != null) && (!(tempRMC.equals(""))))
    						temp = msg.set_posusr(tempRMC, tempGGA);
    					else
    						temp = "";
    					if (CSDposFormat.equalsIgnoreCase(CHORAL)) {
    						serialOut.write((temp + "\n\r").getBytes());
//    						dataOut.write((choralQueue.LastValidElement() + "\n\r").getBytes());
    					} else if (CSDposFormat.equalsIgnoreCase(NMEA)) {
    						serialOut.write((tempRMC + "\n\r").getBytes());
//    						dataOut.write((NMEAQueue.LastValidElement() + "\n\r").getBytes());
    					}
						
					}
						
					else if(comando.indexOf(PUBTOPIC)>=0 && auth==true) {
		
						// extract information
						info = comando.substring(PUBTOPIC.length()+1);
						if(debug){
							System.out.println(info);
						}
						// check length
						if (info.length()<=20) {
							if(debug){
								System.out.println("OK");
							}
							// modification OK, change value on file and write
						    infoS.setInfoFileString(PublishTopic, info);
						    // write immediately on file the change
						    file.setImpostazione(PublishTopic, info);
						    InfoStato.getFile();
							file.writeSettings();
							InfoStato.freeFile();
							serialOut.write(ACK.getBytes());		    						
						} else {
							if(debug){
								System.out.println("ERROR, Topic ID is too long (max 20 chars)");
							}
							serialOut.write(NACK.getBytes());
						}
							    				
					} 
					
					
						// #SETINSENSIBILITAGPS --> modify sensitivity sending coordinates					
	    			else if (comando.indexOf(csdSETINSENSIBILITAGPS +" ")>=0 && auth==true) {
	    				//System.out.print("Th*ATsender: set INSENSIBILITAGPS...");
	    				/*
	    				 * Check for presence of at least 1 char and max 15 chars
	    				 */
		    			if (comando.length() >= csdSETINSENSIBILITAGPS.length()) {
		    				// extract information
		    				info = comando.substring(csdSETINSENSIBILITAGPS.length()+1);

		    				// check length
		    				if (info.length()<=3) {
		    					System.out.println("OK");
		    					// modification OK, change value on file and write
	    						infoS.setInfoFileString(InsensibilitaGPS, info);
	    						// write immediately on file the change
	    						file.setImpostazione(InsensibilitaGPS, info);
	    						InfoStato.getFile();
	    						file.writeSettings();
	    						InfoStato.freeFile();
	    						serialOut.write(csdSETINSENSIBILITAGPSok.getBytes());		    						
		    				} else {
		    					System.out.println("ERROR, InsensibilitaGPS too long.");
		    					serialOut.write(csdSETINSENSIBILITAGPSerr.getBytes());
		    				}
		    				
		    			} else {
		    				System.out.println("ERROR");
		    				serialOut.write(csdSETINSENSIBILITAGPSerr.getBytes());
		    			}
	    			}
							    			
					// #SIG						
					else if (comando.indexOf(SIG)>=0 && auth==true) {
						if(debug){
							System.out.print("Th*Seriale: Field strength and number of satellites...");
						}
		
						// process return string
						if(debug){
							System.out.println("OK");
						}
						serialOut.write((SIG + ": " + infoS.getCSQ() + "," + infoS.getNumSat() + "\r\n\r\n").getBytes());
						if(debug){
							serialOut.write((infoS.getInfoFileString(TrkState) + "\r\n\r\n").getBytes());
							serialOut.write((infoS.getInfoFileString(GPRSProtocol) + "\r\n\r\n").getBytes());
							serialOut.write((infoS.getInfoFileInt(TrkIN) + "\r\n\r\n").getBytes());
							serialOut.write((infoS.getInfoFileInt(TrkOUT) + "\r\n\r\n").getBytes());
							serialOut.write((infoS.getDataRAM() + "\r\n\r\n").getBytes());
						}
	    			}
							    			
	    			// #SLPTM				
					else if (comando.indexOf(SLPTM+" ")>=0 && auth==true) {
							    				
						if(debug){
							System.out.print("Th*Seriale: set OrePowerDownOK...");
						}
					    				
						// Check for presence of at least 1 char
						if (comando.length() >= SLPTM.length()) {
							    				
							// extract information
							info = comando.substring(SLPTM.length()+1);
		
							// Convert to integer to check
							try{
								infoInt = Integer.parseInt(info);
							} catch (NumberFormatException nfe) {
								if(debug){
									System.out.println("exception: " + nfe.getMessage());
								}
								nfe.printStackTrace();
								if(debug){
									System.out.println("ERROR, not numeric char.");
								}
								serialOut.write(NACK.getBytes());
							}
							    				
							// Check on value (min 1 max 48)
							if (infoInt >= 1 && infoInt <= 48) {
								if(debug){
									System.out.println("OK");
								}
								
								// modification OK, change value on file and write
								infoS.setInfoFileInt(OrePowerDownOK, Integer.toString(infoInt));
								// write immediately on file the change
								file.setImpostazione(OrePowerDownOK, Integer.toString(infoInt));
								InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
								serialOut.write(ACK.getBytes());		    						
							} else {
								if(debug){
									System.out.println("ERROR, not valid number.");
								}
								serialOut.write(NACK.getBytes());
								serialOut.write("Insert number between 1 and 48\r\n".getBytes());
							}
								    				
						} else {
							if(debug){
								System.out.println("ERROR");
							}
							serialOut.write(NACK.getBytes());
						}
					}
						
					// #REBOOT						
					else if (comando.indexOf(REBOOT)>=0 && auth==true) {
						if(debug){
							System.out.print("Th*Seriale: SYSTEM RESTART");
						}
		
						// process return string
						if(debug){
							System.out.println("OK");
						}
						serialOut.write(ACK.getBytes());
						
						semAT.getCoin(5);
							if(debug){
								System.out.println("Seriale: module restart in progress...");
							}
							infoS.setATexec(true);
							mbox2.write("AT+CFUN=1,1\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					    semAT.putCoin();
		
	    			}
					// #TRK						
					else if (comando.indexOf(TRK+" ")>=0 && auth==true) {
						if(debug){
							System.out.print("Th*Seriale: Activate or deactivate tracking...");
						}
						if (comando.indexOf(" ON,FMS") >= 0){
							infoS.setInfoFileString(TrkState, "ON,FMS");
							file.setImpostazione(TrkState, "ON,FMS");
							InfoStato.getFile();
							file.writeSettings();
							InfoStato.freeFile();
							serialOut.write(ACK.getBytes());
						}
						else if (comando.indexOf(" ON") >= 0){
							infoS.setInfoFileString(TrkState, "ON");
							file.setImpostazione(TrkState, "ON");
							InfoStato.getFile();
							file.writeSettings();
							InfoStato.freeFile();
							serialOut.write(ACK.getBytes());
						}
						else if (comando.indexOf(" OFF") >= 0){
							infoS.setInfoFileString(TrkState, "OFF");
							file.setImpostazione(TrkState, "OFF");
							InfoStato.getFile();
							file.writeSettings();
							InfoStato.freeFile();
							serialOut.write(ACK.getBytes());
						}
						else serialOut.write(NACK.getBytes());		    						
		
	    			}
						
					// #TRKCFG				
	    			else if ((comando.indexOf(TRKCFG+" ") >= 0) && auth==true) {
	    				if(debug){
	    					System.out.print("Th*ATsender: set TrackingType...");
	    				}
	    				/*
	    				 * Check for presence of at least 1 char
	    				 */
	    				if ((comando.length() >= TRKCFG.length()) && checkComma(4,comando)) {
			    				
			    			try{
		    					// extract information
				    			info = comando.substring(TRKCFG.length()+1);
				    			String temp = info.substring(0,info.indexOf(","));
				    			info = info.substring(info.indexOf(",")+1);
				    						    			
				    			if (temp.equals("USR") || temp.equals("NMEA")) {
				    				String temp1 = info.substring(0,info.indexOf(","));
				    				info = info.substring(info.indexOf(",")+1);
				    				if (temp1.equals("SMS") || temp1.equals("IP")) {
				    					String temp2 = "";
				    					String temp3 = "";
				    					String temp4 = "0s";
				    					try{
				    						temp2 = info.substring(0,info.indexOf(","));
					    					info = info.substring(info.indexOf(",")+1);
				    					} catch(IndexOutOfBoundsException  e){
				    						info = info.substring(info.indexOf(",")+1);
				    					}
				    					try{
					    					temp3 = info.substring(0,info.indexOf(","));
						    			} catch (IndexOutOfBoundsException  e){}
						    			try{
						    				temp4 = info.substring(info.indexOf(",")+1);
						    			}catch  (IndexOutOfBoundsException  e){}
						    			// write immediately on file the change
						    			infoS.setInfoFileString(TrackingType, temp1);
						    			infoS.setInfoFileString(TrackingProt, temp);
						    			infoS.setInfoFileString(Header, temp2);
						    			infoS.setInfoFileString(Ackn, temp3);
						    			boolean ok = infoS.setInfoFileString(GprsOnTime, temp4);
						    			if(ok){
						    				file.setImpostazione(TrackingType, temp1);
						    				file.setImpostazione(TrackingProt, temp);	
						    				file.setImpostazione(Header, temp2);	
						    				file.setImpostazione(Ackn, temp3);
						    				file.setImpostazione(GprsOnTime, temp4);	
						    				InfoStato.getFile();
						    				file.writeSettings();
						    				InfoStato.freeFile();
							    			serialOut.write(ACK.getBytes());
						    			}
						    			else serialOut.write(NACK.getBytes());
				    				}
				    				else serialOut.write(NACK.getBytes());			    				
				    			}
				    			else serialOut.write(NACK.getBytes());
			    			} catch (IndexOutOfBoundsException  e){
			    				serialOut.write(NACK.getBytes());
			    			}			    						
			    		} else {
			    			serialOut.write(NACK.getBytes());
			    		}
			    	}
					
					// #SETSPEED						
					else if (comando.indexOf(SETSPEED+" ")>=0 && auth==true) {
						
						// Check for presence of at least 1 char
	    				if (comando.length() >= SETSPEED.length()) {
							    				
	    					// extract information
							info = comando.substring(SETSPEED.length()+1);
							
							boolean er = false;
							int i = -1;
							
							// Convert to integer to check
							try {
								infoInt = Integer.parseInt(info);
							} catch (NumberFormatException nfe) {
								if(debug){
									System.out.println("exception: " + nfe.getMessage());
									nfe.printStackTrace();
								}
								if(debug){
									System.out.println("ERROR, not numeric char.");
								}
								er = true;
							}
							if(!er){
								if (infoInt >= 0 && infoInt <= 1440) {
									
									infoS.setSpeedGree(infoInt);
									if(debug){
										System.out.println("OK");
										System.out.println("SET: " + infoInt + " - READ: " + infoS.getSpeedGree());
									}
									serialOut.write(ACK.getBytes());
									
								}else {
									if(debug){
										System.out.println("ERROR, not valid number.");
									}
									serialOut.write(NACK.getBytes());
									
								}
								   				
								} else {
									if(debug){
										System.out.println("ERROR");
									}
								    serialOut.write(NACK.getBytes());
								}
	    					}    						
		
	    			}
							    			
	    			// #TRKTM				
	    			else if (comando.indexOf(TRKTM+" ")>=0 && auth==true) {
	    				if(debug){
	    					System.out.print("Th*Seriale: set TrackingInterv...");
	    				}
							    				
	    				// Check for presence of at least 1 char
	    				if (comando.length() >= TRKTM.length()) {
							    				
	    					// extract information
							info = comando.substring(TRKTM.length()+1);
							
							boolean er = false;
							int i = -1;
							if((i = info.indexOf("s")) > 0)
								info = info.substring(0,i);
							
							// Convert to integer to check
							try {
								infoInt = Integer.parseInt(info);
							} catch (NumberFormatException nfe) {
								if(debug){
									System.out.println("exception: " + nfe.getMessage());
									nfe.printStackTrace();
								}
								if(debug){
									System.out.println("ERROR, not numeric char.");
								}
								er = true;
							}
							if(!er){
								// Check on value (min 5 max 999)
								if ((i > 0) && infoInt >= 5 && infoInt <= 86400) {
									if(debug){
										System.out.println("OK");
									}
									// modification OK, change value on file and write
								    infoS.setInfoFileString(TrackingInterv, info + "s");
								    file.setImpostazione(TrackingInterv, info + "s");
								    InfoStato.getFile();
									file.writeSettings();
									InfoStato.freeFile();
									serialOut.write(ACK.getBytes());
								
								} else if ((i < 0) && infoInt >= 1 && infoInt <= 1440) {
									if(debug){
										System.out.println("OK");
									}
									// modification OK, change value on file and write
								    infoS.setInfoFileString(TrackingInterv, info + "");
								    file.setImpostazione(TrackingInterv, info + "");
								    InfoStato.getFile();
									file.writeSettings();
									InfoStato.freeFile();
									serialOut.write(ACK.getBytes());
									
								}else {
									if(debug){
										System.out.println("ERROR, not valid number.");
									}
									serialOut.write(NACK.getBytes());
									
								}
								   				
								} else {
									if(debug){
										System.out.println("ERROR");
									}
								    serialOut.write(NACK.getBytes());
								}
	    					}
						}
						
						// @LOG --> Read log file						
	    				else if (comando.indexOf(logREAD)>=0) {
	    					try{
	    						while(!InfoStato.getLogSemaphore()){Thread.sleep(1);}
	    					}catch(InterruptedException e){}


	    					try {
	    						FileConnection fconn = (FileConnection) Connector.open("file:///a:/log/log.txt");
	    						if (fconn.exists()) {
	    							DataInputStream dos = fconn.openDataInputStream();
	    							serialOut.write(("\r\n").getBytes());
	    							while(dos.available() > 0){
	    								serialOut.write((char)dos.read());
	    							}
	    							serialOut.write(logEND.getBytes());
	    							dos.close();
	    						}
	    						else{
	    							serialOut.write(("\r\nNO LOG" + logEND).getBytes());
	    						}						    					
	    						fconn.close();
	    					} catch (IOException ioe) {
	    					
	    					} catch (SecurityException e){}
	    					InfoStato.freeLogSemaphore();
	    				}
						// #EE_GET_PTR				
	    				else if (comando.indexOf(EE_GET_PTR)>=0) {
	    					serialOut.write(("PTR IN:" + infoS.getInfoFileInt(TrkIN) + " ; OUT:"+infoS.getInfoFileInt(TrkOUT)+"\r\n\r\n").getBytes());
						    serialOut.write(ACK.getBytes());
	    				}
							    			
						//** NOT VALID MESSAGES (with authentication) **//
							    			
						else if (auth==true) {
							// Received EOF, somebody else broke the connection
							if(debug){
								System.out.println("Th*Seriale: Command not recognized");
							}
							serialOut.write(NACK.getBytes());
						}
							    			
						else if(debug){
							System.out.println("Th*Seriale: CSD management error");
						}
						}	    			
					} catch (StringIndexOutOfBoundsException siobe) {
						if(debug){
							System.out.println("Th*Seriale: CSD exception");
						}
						serialOut.write("Command ERROR\n\r".getBytes());
					}
						    			
					    			
				} //while(true)
				    			
			} catch (IOException ioe) {
				if(debug){
					System.out.println("Th*Seriale: IOException"); 
				}
			} catch (InterruptedException e){
				
			}
		} //while	
	} //run
	
	/**
	 * public boolean checkComma(int comma, String text)
	 * 
	 * Method that check how comma contain the text
	 * 
	 * @param	comma: commas number to check
	 * @param	text: text where search commas
	 * @return	true if number is OK, false otherwise
	 */
	
	public boolean checkComma(int comma, String text){
		
		int count = 0;
		int s = 0;
		for(int i = 0; i < comma; i++){
			try{
				if((s = text.indexOf(",")) != -1){
					count++;
					text = text.substring(s+1);
				}
				else
					return false;
			}catch(NullPointerException e){
				return false;
			}catch(IndexOutOfBoundsException e){
				return false;
			}
			
		}
		if(count == comma)
			return true;
		return false;
	}
	
} //Seriale

