/*	
 * Class 	ATsender
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;

import javax.microedition.io.Connector;

import com.cinterion.io.*;
import com.cinterion.io.file.FileConnection;

/**
 * AT commands sending to module with mutual exclusion.
 * <BR>
 * Semaphore 'semAT' regulate access to AT resource,
 * AT resource must be requested calling 'semAT.getCoin(prio)'
 * and released calling 'semAT.putCoin()' when finish to use.
 * <br>
 * When a thred can use AT resource, it must wait for complete execution
 * of ANY PREVIOUS AT COMMAND before send another to module.
 * To do this, set 'ATexec=true' before sending ANY AT COMMAND to 'mbox2'
 * and wait for command execution using this code:
 * <pre>
 * System.out.println("Wait for free AT resource...");
 * while(ATexec) {}	// until AT resource is busy
 * </pre>
 * 
 * @version	1.02 <BR> <i>Last update</i>: 25-10-2007
 * @author 	alessioza
 * 
 */
public class ATsender extends ThreadCustom implements GlobCost {
	
	/* 
	 * local variables
	 */
	private		ATCommand			ATCMD;
	protected 	ATListenerStd		ATListSTD;
	protected 	ATListenerEvents	ATListEV;
	private 	String				comandoAT;
	/**	 indication about release of AT resource */
	public boolean 	stopAT = false;		
	// CSD
	OutputStream 	dataOut;
	InputStream 	dataIn;
	private String 	comCSD;
	private int		rcv; 
	private boolean auth = false;
	private boolean confirmPWD = false;
	private String	newPWD,strCHpwd,info,transportType;
	private int		infoInt;
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the resource AT
	 * flag 	 'ATexec', indicates if AT resource is busy
	 * Mailbox   'mbox2', receive msg with this thread
	 * Mailbox   'mboxMAIN', send msg to AppMain
	 */

	
	/* 
	 * constructors
	 */
	public ATsender() {
		//System.out.println("Th*ATsender: CREATED");
	}
	
	/*
	 * methods
	 */
	public void run() {
		
		//System.out.println("Th*ATsender: STARTED");
		
		while(true){	
			try {
				/* 
				 * Init listeners and ATCommand object
				 */
				ATListSTD = new ATListenerStd();
				ATListEV  = new ATListenerEvents();
				
				// pass infoS to listeners
				ATListSTD.addInfoStato(infoS);
				ATListSTD.addFlashFile(file);
				ATListSTD.addMailbox(mboxMAIN,0,9,false);	// verify '9'
				ATListEV.addInfoStato(infoS);
				ATListEV.addMailbox(mboxMAIN,0,10,false);	// verify '10'
				
				// init object ATCommand and pass listener
				ATCMD = new ATCommand(true);
				ATCMD.addListener(ATListEV);
				
				
				/* 
				 * AT resource not available,
				 * because semAT has been initialized to 0.
				 * I free resource for use by other threds using putCoin()
				 */
				//System.out.println("semAT: initial value = " + semAT.getValue());		// =0
				semAT.putCoin();	// now AT resource is available  				
				/* 
				 * Main loop for AT commands execution,
				 * all AT commands read from mailbox 'mbox2' are executed.
				 * DURING AT command execution, AT resource remains BUSY.
				 * Two cases:
				 * 		a) Read commands
				 * 		b) Write & Execution commands
				 */
				while(true) {
					/* 
					 * Wait for new msg (with AT commands to execute) from mailbox
					 */
				
					if (mbox2.numMsg()>0){
						/*
						 * If new msg present in the Mailbox, I check flag 'ATexec':
						 * 	if 'ATexec=true'  -->  command execution
						 * 	if 'ATexec=false' -->  wait
						 */
						//System.out.println("Th*ATsender: ATexec = " + infoS.getATexec());
						if (infoS.getATexec()==false) {
							// if 'ATexec=false' wait for resource
							//System.out.print("Th*ATsender: wait for 'ATexec=true'...");
							while(infoS.getATexec()==false) { Thread.sleep(whileSleep); }
							//System.out.println("OK");
						} //if flagS.getATexec()
						//System.out.println("Th*ATsender: ATexec = " + infoS.getATexec());
						
						// Read msg
						comandoAT = (String)mbox2.read();
						
						
						/*
						 * Operations on ATCommand for CSD protocol
						 */
						
						// Open IN/OUT stream for CSD
						if (comandoAT.indexOf(csdOpen)>=0) {
							dataOut = ATCMD.getDataOutputStream();
			    			dataIn = ATCMD.getDataInputStream();
			    			if(debugGSM){
			    				System.out.println("Th*ATsender: Stream CSD aperto");
			    			}
						} //csdOpen
						
						// Write on CSD output channel
						else
							if (comandoAT.indexOf(csdWrite)>=0) {
								try {
									dataOut.write((comandoAT.substring(comandoAT.indexOf(csdWrite) + csdWrite.length())).getBytes());
								} catch (IOException ioe) {
									System.out.println("Th*ATsender: IOException");
								}
						} //csdWrite
						
						// Read from CSD input channel
						else if (comandoAT.indexOf(csdRead)>=0) {
							
							/*
							 * Start CSD read cycle
							 * (to do: stop application until CSD call is closed)
							 */					
							
							try {
	
								// If CSD PWD is null, no authentication required
								if (infoS.getInfoFileString(PasswordCSD).equalsIgnoreCase("")) {
									auth=true;
									if(debug){
										System.out.println("Th*ATsender: no authentication required because PWD is null");
									}
								}
								
								while(true) {
									
									try {
									
										/*
										 * Read command
										 */ 
						    			rcv = 0;
						    			comCSD = "";
						    			do {
						    				rcv = dataIn.read();
						    				if(rcv != '\n' ){
							    				if(debug){
							    					if (rcv >= 0) System.out.print((char)rcv);
							    				}
							    				// update string read from CSD
							    				if ((byte)rcv != '\r') {
							    					dataOut.write((byte)rcv);
							    					comCSD = comCSD + (char)rcv;
							    				} else dataOut.write("\r\n".getBytes());
						    				}
						    			} while((char)rcv != '\r');
						    			// If '\r' received, process command	
						    			if(debug){
						    				System.out.println("Th*ATsender, CSD command received: " + comCSD + "***");
						    			}
						    			
						    			
						    			/*
						    			 * Command processing
						    			 */
						    			
						    			
						    			//** Messages accepted with or without authentication **//		
						    			
						    			// #CLOSE --> close connection						
						    			if (comCSD.indexOf(csdCLOSE)>=0) {
						    				if(debug){
						    					System.out.println("Th*ATsender: closing CSD connection");
						    				}
						    				//dataOut.write("\n\rClosing connection...\n\r".getBytes());
						    				try{
						    					ATCMD.breakConnection();
						    				}catch(IOException e){}
					    					break;
						    			}
						    			// @CODA --> Read queue						
						    			else if (comCSD.indexOf(logQUEUE)>=0) {
						    				for(int indice=0;indice<99;indice++){
						    					dataOut.write(infoS.getRecord(indice).getBytes());
						    				}
						    			}
						    			// @LOG --> Read log file						
						    			else if (comCSD.indexOf(logREAD)>=0) {
						    				try{
					    						while(!InfoStato.getLogSemaphore()){Thread.sleep(1);}
					    					}catch(InterruptedException e){}


						    				try {
							    				FileConnection fconn = (FileConnection) Connector.open("file:///a:/log/log.txt");
							    				if (fconn.exists()) {
							    					DataInputStream dos = fconn.openDataInputStream();
							    					dataOut.write(("\r\n").getBytes());
							    					while(dos.available() > 0){
							    						dataOut.write((char)dos.read());
							    					}
							    					dataOut.write(logEND.getBytes());
							    					dos.close();
							    				}
							    				else{
							    					dataOut.write(("\r\nNO LOG" + logEND).getBytes());
							    				}						    					
							    				fconn.close();
						    				} catch (IOException ioe) {
						    					
						    				} catch (SecurityException e){}
					    					InfoStato.freeLogSemaphore();
						    			}
						    			
						    			//@OLDLOG
						    			else if (comCSD.indexOf(OLDlogREAD)>=0) {
						    				try{
					    						while(!InfoStato.getLogSemaphore()){Thread.sleep(1);}
					    					}catch(InterruptedException e){}


						    				try {
							    				FileConnection fconn = (FileConnection) Connector.open("file:///a:/log/logOLD.txt");
							    				if (fconn.exists()) {
							    					DataInputStream dos = fconn.openDataInputStream();
							    					dataOut.write(("\r\n").getBytes());
							    					while(dos.available() > 0){
							    						dataOut.write((char)dos.read());
							    					}
							    					dataOut.write(logEND.getBytes());
							    					dos.close();
							    				}
							    				else{
							    					dataOut.write(("\r\nNO LOG" + logEND).getBytes());
							    				}						    					
							    				fconn.close();
						    				} catch (IOException ioe) {
						    					
						    				} catch (SecurityException e){}
					    					InfoStato.freeLogSemaphore();
						    			}
				
						    			
						    			// @DELLOG --> delete log file				
						    			else if (comCSD.indexOf(logDELETE)>=0) {
						    				try{
					    						while(!InfoStato.getLogSemaphore()){Thread.sleep(1);}
					    					}catch(InterruptedException e){}


						    				try{
						    					FileConnection fconn1 = (FileConnection) Connector.open("file:///a:/log/log.txt");
						    					if (fconn1.exists()) {
						    						fconn1.delete();
						    					}
						    					fconn1.close();
						    				}catch (IOException  e){
						    					
						    				}catch (SecurityException   e){}
						    				dataOut.write(logEND.getBytes());
					    					InfoStato.freeLogSemaphore();
						            	}
						    			
						    			//** Messages accepted only without authentication **//			    			
						    			
						    			// #PWD --> verifico l'authentication						
						    			else if (comCSD.indexOf(PWD+" ")>=0) {
						    				if(debug){
						    					System.out.print("Th*ATsender: check for authentication...");
						    				}
						    				if (comCSD.indexOf(PWD + " " + infoS.getInfoFileString(PasswordCSD))>=0) {
						    					if(debug){
						    						System.out.println("OK");
						    					}
						    					dataOut.write(PWDok.getBytes());
						    					// Authentication OK
						    					auth = true;
						    				}
						    				else {
						    					if(debug){
						    						System.out.println("ERROR");
						    					}
						    					auth = false;
						    					dataOut.write(PWDerr.getBytes());
						    				}
						    			}
						    			// REBOOT
						    			else if (comCSD.indexOf(REBOOT)>=0) {
											if(debug){
												System.out.print("Th*Seriale: System reboot");
											}
							
											// process return string
											if(debug){
												System.out.println("OK");
											}
											dataOut.write(ACK.getBytes());
											
											try{
												ATCMD.breakConnection();
											}catch(IOException e){}
											
											if(debug){
												System.out.println("Seriale: module reboot in progress...");
											}
											try { ATCMD.send("AT+CFUN=1,1\r", ATListSTD); }
											catch (ATCommandFailedException ATex) { 
												if(debug){
													System.out.println("Th*ATsender: ATCommandFailedException"); }
												}
											break;
							
						    			}
						    			
						    			// #PWD without authentication
						    			else if (auth==false) {
						    				if(debug){
						    					System.out.println("Th*ATsender: authentication failed");
						    				}
						    				dataOut.write((NACK).getBytes());
						    			}
	
						    			
						    			//** Messages accepted only with authentication **//
						    			
						    			// #CFG					
						    			else if (comCSD.indexOf(CFG)>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: configuration options list...");
						    				}
						    				// process return string
						    				if(debug){
						    					System.out.println("OK");
						    				}
						    				dataOut.write(("\r\n" + "Greenwich rev. " + revNumber + ", " + dataRev + "\r\n").getBytes());
										    dataOut.write((moduleCodeRev + infoS.getREV() + "\n\r").getBytes());
										    dataOut.write(("IMEI: " + infoS.getIMEI() + "\r\n").getBytes());
										    dataOut.write((SETID + ": " + infoS.getInfoFileString(IDtraker) + "\r\n").getBytes());
										    dataOut.write((SNOP + ": " + infoS.getInfoFileString(Operatore) +  "\r\n").getBytes());
										    //dataOut.write((ACTOP + "\r\n").getBytes());
										    dataOut.write((GPRSCFG + ": " + infoS.getInfoFileString(apn) + "," 
										    			+ infoS.getInfoFileString(GPRSProtocol)+ "," + infoS.getInfoFileString(DestHost) 
										    			+ "," + infoS.getInfoFileString(DestPort) + "\r\n").getBytes());
										    dataOut.write((TRKCFG + ": " + infoS.getInfoFileString(TrackingType) + "," 
									    			+ infoS.getInfoFileString(TrackingProt)+ "," + infoS.getInfoFileString(Header) 
									    			+ "," + infoS.getInfoFileString(Ackn) + "," + infoS.getInfoFileString(GprsOnTime) + "\r\n").getBytes());
										    dataOut.write((TRKTM + ": " + infoS.getInfoFileString(TrackingInterv) + "\r\n").getBytes());
										    dataOut.write((TRK + ": " + infoS.getInfoFileString(TrkState) + "\r\n").getBytes());
										    dataOut.write((PUBTOPIC + ": " + infoS.getInfoFileString(PublishTopic) + "\r\n").getBytes());
										    dataOut.write((SLPTM + ": " + infoS.getInfoFileInt(OrePowerDownOK) + "\r\n").getBytes());
										    dataOut.write((SLP + ": " + infoS.getInfoFileString(SlpState) + "\r\n").getBytes());
										    dataOut.write((STILLTM  + ": " + infoS.getInfoFileInt(StillTime) + "\r\n").getBytes());
										    dataOut.write((MOVSENS + ": " + infoS.getInfoFileString(MovState) + "\r\n").getBytes());
										    dataOut.write((IGNCFG + ": " + infoS.getInfoFileString(IgnState) + "\r\n").getBytes());
										    dataOut.write((UARTCFG + ": " + infoS.getInfoFileInt(UartSpeed) + "," 
									    			+ infoS.getInfoFileString(UartGateway)+ "," + infoS.getInfoFileString(UartHeaderRS) 
									    			+ "," + infoS.getInfoFileString(UartEndOfMessage) + "," + infoS.getInfoFileInt(UartAnswerTimeOut)
									    			+ "," + infoS.getInfoFileInt(UartNumTent) + "," + infoS.getInfoFileString(UartEndOfMessageIP)
									    			+ "," + infoS.getInfoFileString(UartIDdisp) + "," + infoS.getInfoFileInt(UartTXtimeOut) + "\r\n").getBytes());
										    dataOut.write((SIG + ": " + infoS.getCSQ() + "," + infoS.getNumSat() + "\r\n").getBytes());
										    dataOut.write((VBAT + ": " + infoS.getBatteryVoltage() + "\r\n\r\n").getBytes());
										    dataOut.write(ACK.getBytes());
										    dataOut.write((csdSETINSENSIBILITAGPS + ": " + infoS.getInfoFileString(InsensibilitaGPS) + "\n\r").getBytes());
					    						
						    			}
						    			
						    			// #CHPWD --> I can change password						
						    			else if (comCSD.indexOf(CHPWD+" ")>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: change password...");
						    				}
						    				// cancel new password
						    				newPWD = "";
						    				strCHpwd = CHPWD + " " + infoS.getInfoFileString(PasswordCSD);
						    				// check
						    				if (comCSD.indexOf(strCHpwd)>=0) {
						    					// extract new password
						    					if (comCSD.length()==comCSD.indexOf(strCHpwd)+strCHpwd.length()+1) newPWD = "";
						    					else newPWD = comCSD.substring(comCSD.indexOf(strCHpwd)+strCHpwd.length()+1);
						    					if(debug){
						    						System.out.print("new password: " + newPWD + " ...");
						    					}
						    					// check PWD length
						    					if (newPWD.length()<=15) {
							    					// request confirm of the new password
							    					dataOut.write(CHPWDconfirm.getBytes());
							    					if(debug){
							    						System.out.println("OK, waiting for confirm");
							    					}
							    					confirmPWD = true;				    					
						    					} else {
						    						// Password too long
						    						if(debug){
						    							System.out.println("ERROR, password too long");
						    						}
						    						dataOut.write(CHPWDlong.getBytes());
						    					} //else
						    				} else {
						    					// password change not valid
						    					if(debug){
						    						System.out.println("ERROR, password change not valid");
						    					}
						    					dataOut.write(CHPWDerr.getBytes());
						    				} //strCHpwd
						    			} //CHPWD
						    			
						    			// Confirm new password
						    			else if (confirmPWD==true && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: confirm new password...");
						    				}
							    			if (comCSD.indexOf(newPWD)>=0) {
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
						    					dataOut.write(CHPWDok.getBytes());
					    					} else {
					    						// Confirmed password is different
					    						if(debug){
					    							System.out.println("ERROR, password not confirmed");
					    						}
					    						dataOut.write(CHPWDerr.getBytes());
					    					} //else
							    			confirmPWD = false;
						    			}
						    			
						    			// #SETID --> modify DeviceID					
						    			else if (comCSD.indexOf(SETID+" ")>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: set DeviceID...");
						    				}
						    				/*
						    				 * Check lenght: min 1 and max 15 chars
						    				 */
							    			if (comCSD.length() >= SETID.length()) {
							    				// extract info
							    				info = comCSD.substring(SETID.length()+1);
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
							    					dataOut.write(ACK.getBytes());		    						
							    				} else {
							    					if(debug){
							    						System.out.println("ERROR, DeviceID too long.");
							    					}
							    					dataOut.write(NACK.getBytes());
							    				}
							    				
							    			} else {
							    				if(debug){
							    					System.out.println("ERROR");
							    				}
						    					dataOut.write(NACK.getBytes());
							    			}
						    			}
						    			
						    			// #GPRSCFG					
						    			else if (comCSD.indexOf(GPRSCFG+" ")>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: settings ConnProfileGPRS and apn...");
						    				}
						    				/*
						    				 * At least 1 char
						    				 */
							    			if (comCSD.length() > (GPRSCFG+" ").length() && checkComma(3,comCSD)){
							    				
							    				// extract APN info (user and pwd are ignored now)
							    				info = comCSD.substring(GPRSCFG.length()+1, comCSD.indexOf(","));
							    				comCSD = comCSD.substring(comCSD.indexOf(",")+1);
							    				
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
						    						
//						    						extract protocol type
								    				if((comCSD.substring(0, comCSD.indexOf(",")).equals("TCP")) || (comCSD.substring(0, comCSD.indexOf(",")).equals("UDP"))){
								    					
								    					// extract <transportType>
									    				transportType = (comCSD.substring(comCSD.indexOf(" ")+1, comCSD.indexOf(",")));
									    				comCSD = comCSD.substring(comCSD.indexOf(",")+1);
									    				if(debug){
									    					System.out.println("Th*ATsender, transportType: " + transportType);
									    				}
									    				
									    				// extract <ip>
									    				info = comCSD.substring(0, comCSD.indexOf(","));
									    				comCSD = comCSD.substring(comCSD.indexOf(",")+1);
									    				if(debug){
									    					System.out.println("Th*ATsender, IP: " + info);
									    					System.out.println("Th*ATsender, PORTA: " + comCSD);
									    				}
									    				
									    				// set and check (max 39 char)
									    				if (info.length()<=39) {
									    					infoS.setInfoFileString(DestHost, info);
									    					file.setImpostazione(DestHost, info);
									    					
										    				// set <port>
										    				infoS.setInfoFileString(DestPort, comCSD);
										    				file.setImpostazione(DestPort, comCSD);
										    				
										    				infoS.setInfoFileString(GPRSProtocol, transportType);
									    					file.setImpostazione(GPRSProtocol, transportType);
										    				
									    					dataOut.write(ACK.getBytes());	
									    					
										    				// write on file
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
								    					dataOut.write(NACK.getBytes());
									    			}
						    							    						
							    				}
							    				else {
							    					if(debug){
							    						System.out.println("ERROR, APN too long.");
							    					}
							    					dataOut.write(NACK.getBytes());
							    				}
							    				
							    			} else {
							    				if(debug){
							    					System.out.println("ERROR");
							    				}
						    					dataOut.write(NACK.getBytes());
							    			}
						    			}
						    			
						    			// #POSREP						
										else if (comCSD.indexOf(POSREP)>=0 && auth==true) {
											if(comCSD.indexOf(POSREP + " ENA")>=0){
												dataOut.write(ACK.getBytes());
												infoS.setCSDTraspGPS(true);
											}
											else{
												if(comCSD.indexOf(POSREP + " DIS")>=0){
													infoS.setCSDTraspGPS(false);
													dataOut.write(ACK.getBytes());
												}
												else dataOut.write(NACK.getBytes());
											}
											
										}
						    			
						    			// #POSUSR						
						    			else if (comCSD.indexOf(POSUSR)>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: single position string...");
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
					    						dataOut.write((temp + "\n\r").getBytes());
					//    						dataOut.write((choralQueue.LastValidElement() + "\n\r").getBytes());
					    					} else if (CSDposFormat.equalsIgnoreCase(NMEA)) {
					    						dataOut.write((tempRMC + "\n\r").getBytes());
					//    						dataOut.write((NMEAQueue.LastValidElement() + "\n\r").getBytes());
					    					}
						    			}
						    									    			
						    			// #SIG						
						    			else if (comCSD.indexOf(SIG)>=0 && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: network coverage and satellites number...");
						    				}
	
						    				// process return string
						    				if(debug){
						    					System.out.println("OK");
						    				}
					    					dataOut.write((SIG + ": " + infoS.getCSQ() + "," + infoS.getNumSat() + "\r\n\r\n").getBytes());		    						
	
						    			}
						    								    			// #REPNUM				
						    			/*else if (comCSD.indexOf(csdREPNUM+" ")>=0 && auth==true) {
						    				
						    				System.out.print("Th*ATsender: set NumTrakNormal, NumTrakKeyON and NumTrakKeyOFF...");
						    				
						    				// Check for presence of at least 1 char
						    				if (comCSD.length() >= csdREPNUM.length()) {
						    				
						    					// extract information
							    				info = comCSD.substring(csdREPNUM.length()+1);
	
							    				// Convert to integer to check
							    				try{
							    					infoInt = Integer.parseInt(info);
							    				} catch (NumberFormatException nfe) {
							    					System.out.println("exception: " + nfe.getMessage());
							    					nfe.printStackTrace();
							    					System.out.println("ERROR, no numeric char.");
							    					dataOut.write(csdREPNUMerr.getBytes());
							    				}
							    				
							    				// Check on value (min 1 max 99)
							    				if (infoInt >= 1 && infoInt <= 99) {
							    					System.out.println("OK");
							    					// modification OK, change value on file and write
						    						infoS.setInfoFileInt(NumTrakNormal, Integer.toString(infoInt));
						    						infoS.setInfoFileInt(NumTrakDisattivChiave, Integer.toString(infoInt));
						    						infoS.setInfoFileInt(NumTrakAttivChiave, Integer.toString(infoInt));
						    						// write immediately on file the change
						    						file.setImpostazione(NumTrakNormal, Integer.toString(infoInt));
						    						file.setImpostazione(NumTrakDisattivChiave, Integer.toString(infoInt));
						    						file.setImpostazione(NumTrakAttivChiave, Integer.toString(infoInt));
						    						file.writeSettings();
							    					dataOut.write(csdREPNUMok.getBytes());		    						
							    				} else {
							    					System.out.println("ERROR, not valid number.");
							    					dataOut.write(csdREPNUMerr.getBytes());
							    					dataOut.write("Insert number between 1 and 99\r\n".getBytes());
							    				}
							    				
							    			} else {
							    				System.out.println("ERROR");
						    					dataOut.write(csdREPNUMerr.getBytes());
							    			}
						    			}*/
						    			
						    			// #SLPTM				
						    			else if (comCSD.indexOf(SLPTM+" ")>=0 && auth==true) {
						    				
						    				if(debug){
						    					System.out.print("Th*ATsender: set OrePowerDownOK...");
						    				}
						    				
						    				// Check for presence of at least 1 char
						    				if (comCSD.length() >= SLPTM.length()) {
						    				
						    					// Extract information
							    				info = comCSD.substring(SLPTM.length()+1);
	
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
							    					dataOut.write(NACK.getBytes());
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
							    					dataOut.write(ACK.getBytes());		    						
							    				} else {
							    					if(debug){
							    						System.out.println("ERROR, not valid number.");
							    					}
							    					dataOut.write(NACK.getBytes());
							    					dataOut.write("Insert number between 1 and 48\r\n".getBytes());
							    				}
							    				
							    			} else {
							    				if(debug){
							    					System.out.println("ERROR");
							    				}
						    					dataOut.write(NACK.getBytes());
							    			}
						    			}
						    			
						    			// #TOGPS				
						    			/*else if (comCSD.indexOf(csdTOGPS+" ")>=0 && auth==true) {
						    				System.out.print("Th*ATsender: se tup FIXgpsTOvalue...");
						    				
						    				// Check for presence of at least 1 char
						    				if (comCSD.length() >= csdTOGPS.length()) {
						    				
						    					// extract information
							    				info = comCSD.substring(csdTOGPS.length()+1);
	
							    				// Convert to integer to check
							    				try {
							    					infoInt = Integer.parseInt(info);
							    				} catch (NumberFormatException nfe) {
							    					System.out.println("exception: " + nfe.getMessage());
							    					nfe.printStackTrace();
							    					System.out.println("ERROR, not numeric char.");
							    					dataOut.write(csdTOGPSerr.getBytes());
							    				}
							    				
							    				// Check on value (min 60 max 999)
							    				if (infoInt >= 60 && infoInt <= 999) {
							    					System.out.println("OK");
							    					// modification OK, change value on file and write
						    						infoS.setInfoFileInt(FIXgpsTOvalue, info);
						    						// write immediately on file the change
						    						file.setImpostazione(FIXgpsTOvalue, info);
						    						file.writeSettings();
							    					dataOut.write(csdTOGPSok.getBytes());		    						
							    				} else {
							    					System.out.println("ERROR, not valid number.");
							    					dataOut.write(csdTOGPSerr.getBytes());
							    					dataOut.write("Insert number between 60 and 999\r\n".getBytes());
							    				}
							    				
							    			} else {
							    				System.out.println("ERROR");
						    					dataOut.write(csdTOGPSerr.getBytes());
							    			}
						    			}
						    			
						    			// #TOGPRS				
						    			else if (comCSD.indexOf(csdTOGPRS+" ")>=0 && auth==true) {
						    				System.out.print("Th*ATsender: set FIXgprsTOvalue...");
						    				
						    				// Check for presence of at least 1 char
						    				if (comCSD.length() >= csdTOGPRS.length()) {
						    				
						    					// extract information
							    				info = comCSD.substring(csdTOGPRS.length()+1);
	
							    				// Convert to integer to check
							    				try {
							    					infoInt = Integer.parseInt(info);
							    				} catch (NumberFormatException nfe) {
							    					System.out.println("exception: " + nfe.getMessage());
							    					nfe.printStackTrace();
							    					System.out.println("ERROR, not numeric char.");
							    					dataOut.write(csdTOGPRSerr.getBytes());
							    				}
							    				
							    				// Check on value (min 90 max 999)
							    				if (infoInt >= 90 && infoInt <= 999) {
							    					System.out.println("OK");
							    					// modification OK, change value on file and write
						    						infoS.setInfoFileInt(FIXgprsTOvalue, info);
						    						// write immediately on file the change
						    						file.setImpostazione(FIXgprsTOvalue, info);
						    						file.writeSettings();
							    					dataOut.write(csdTOGPRSok.getBytes());		    						
							    				} else {
							    					System.out.println("ERROR, not valid number.");
							    					dataOut.write(csdTOGPRSerr.getBytes());
							    					dataOut.write("Insert number between 90 and 999\r\n".getBytes());
							    				}
							    				
							    			} else {
							    				System.out.println("ERROR");
						    					dataOut.write(csdTOGPRSerr.getBytes());
							    			}
						    			}*/
						    			
						    			// #TRK						
										else if (comCSD.indexOf(TRK+" ")>=0 && auth==true) {
											if (comCSD.indexOf(" ON,FMS") >= 0){
												infoS.setInfoFileString(TrkState, "ON,FMS");
												file.setImpostazione(TrkState, "ON,FMS");
												InfoStato.getFile();
												file.writeSettings();
												InfoStato.freeFile();
												dataOut.write(ACK.getBytes());
											}
											else if (comCSD.indexOf(" ON") >= 0){
												infoS.setInfoFileString(TrkState, "ON");
												file.setImpostazione(TrkState, "ON");
												InfoStato.getFile();
												file.writeSettings();
												InfoStato.freeFile();
												dataOut.write(ACK.getBytes());
											}
											else if (comCSD.indexOf(" OFF") >= 0){
												infoS.setInfoFileString(TrkState, "OFF");
												file.setImpostazione(TrkState, "OFF");
												InfoStato.getFile();
												file.writeSettings();
												InfoStato.freeFile();
												dataOut.write(ACK.getBytes());
											}
											else dataOut.write(NACK.getBytes());	    						
							
						    			}
						    			
						    			// #TRKCFG				
						    			else if ((comCSD.indexOf(TRKCFG+" ") >= 0) && auth==true) {
						    				if(debug){
						    					System.out.print("Th*ATsender: set TrackingType...");
						    				}
						    				/*
						    				 * Check for presence of at least 1 char
						    				 */
						    				if ((comCSD.length() >= TRKCFG.length()) && checkComma(4,comCSD)) {
								    				
								    			try{
							    					// extract information
									    			info = comCSD.substring(TRKCFG.length()+1);
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
												    			dataOut.write(ACK.getBytes());
											    			}
											    			else dataOut.write(NACK.getBytes());
									    					
									    				}
									    				else dataOut.write(NACK.getBytes());			    				
									    			}
									    			else dataOut.write(NACK.getBytes());
								    			} catch (IndexOutOfBoundsException  e){
								    				dataOut.write(NACK.getBytes());
								    			}			    						
								    		} else {
								    			dataOut.write(NACK.getBytes());
								    		}
								    	}
						    			
						    			// #TRKTM				
						    			else if (comCSD.indexOf(TRKTM+" ")>=0 && auth==true) {
						    				// Check for presence of at least 1 char
						    				if (comCSD.length() >= TRKTM.length()) {
												    				
						    					// extract information
												info = comCSD.substring(TRKTM.length()+1);
							
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
														dataOut.write(ACK.getBytes());
														
													} else if ((i < 0) && infoInt >= 1 && infoInt <= 1440) {
														if(debug){
															System.out.println("OK");
														}
														// modification OK, change value on file and write
													    infoS.setInfoFileString(TrackingInterv, info);
													    file.setImpostazione(TrackingInterv, info);
													    InfoStato.getFile();
														file.writeSettings();
														InfoStato.freeFile();
														dataOut.write(ACK.getBytes());
															
													}else {
									    				if(debug){
									    					System.out.println("ERROR, not valid number.");
									    				}
									    				dataOut.write(NACK.getBytes());
									    				dataOut.write("Insert number between 5 and 999\r\n".getBytes());
									    			}
								    				
								    			} else {
								    				if(debug){
								    					System.out.println("ERROR");
								    				}
							    					dataOut.write(NACK.getBytes());
								    			}
						    				}
						    			}
						    			
						    			// #SETINSENSIBILITAGPS --> modify sensibility on coordinates sending					
						    			else if (comCSD.indexOf(csdSETINSENSIBILITAGPS +" ")>=0 && auth==true) {
						    				System.out.print("Th*ATsender: set INSENSIBILITAGPS...");
						    				/*
						    				 * Check for presence of at least 1 char and max 15 chars
						    				 */
							    			if (comCSD.length() >= csdSETINSENSIBILITAGPS.length()) {
							    				// extract information
							    				info = comCSD.substring(csdSETINSENSIBILITAGPS.length()+1);
	
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
							    					dataOut.write(csdSETINSENSIBILITAGPSok.getBytes());		    						
							    				} else {
							    					System.out.println("ERROR, InsensibilitaGPS too long.");
							    					dataOut.write(csdSETINSENSIBILITAGPSerr.getBytes());
							    				}
							    				
							    			} else {
							    				System.out.println("ERROR");
						    					dataOut.write(csdSETINSENSIBILITAGPSerr.getBytes());
							    			}
						    			}
						    			
						    			// #SETSPEED						
										else if (comCSD.indexOf(SETSPEED+" ")>=0 && auth==true) {
											
											// Check for presence of at least 1 char
						    				if (comCSD.length() >= SETSPEED.length()) {
												    				
						    					// extract information
												info = comCSD.substring(SETSPEED.length()+1);
												
												boolean er = false;
												//int i = -1;
												
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
														dataOut.write(ACK.getBytes());
														
													}else {
														if(debug){
															System.out.println("ERROR, not valid number.");
														}
														dataOut.write(NACK.getBytes());
														
													}
													   				
													} else {
														if(debug){
															System.out.println("ERROR");
														}
														dataOut.write(NACK.getBytes());
													}
						    					}    						
							
						    			}
						    			
						    			// #EE_GET_PTR				
					    				else if (comCSD.indexOf(EE_GET_PTR)>=0) {
					    					dataOut.write(("PTR IN:" + infoS.getInfoFileInt(TrkIN) + " ; OUT:"+infoS.getInfoFileInt(TrkOUT)+"\r\n\r\n").getBytes());
					    					dataOut.write(ACK.getBytes());
					    				}
						    			
						    			//** NOT VALID MESSAGES (WITH AUTHENTICATION) **//
						    			
						    			else if (auth==true) {
						    				// Received EOF, somebody else broke the connection
						    				if(debug){
						    					System.out.println("Th*ATsender: Command not recognized");
						    				}
						    				dataOut.write(NACK.getBytes());
						    			}
						    			
						    			else if(debug){
						    				System.out.println("Th*ATsender: CSD management error");
						    			}
						    			else;
						    			if(infoS.getCSDTraspGPS()){
						    				dataOut.write((infoS.getRMCTrasp()).getBytes());
						    				dataOut.write((infoS.getGGATrasp()).getBytes());
						    			}
					    			
									} catch (StringIndexOutOfBoundsException siobe) {
										if(debug){
											System.out.println("Th*ATsender: CSD exception");
										}
					    				dataOut.write("Command ERROR\n\r".getBytes());
									}
					    			
					    			
								} //while(true)
			    			
							} catch (IOException ioe) {
								if(debug){
									System.out.println("Th*ATsender: IOException"); 
								}
							}
						
						
							// At the end of stream use, set ATexec = false
							infoS.setATexec(false);	
				    	
							// Indicates that CSD connection isn't in use yet, to close UpdateSCD
							infoS.setCSDconnect(false);
	
							// Authentication non più valida
							auth = false;
			    		
						} //csdRead			
					
					
						/*
						 * WRITE OR EXECUTING AT COMMANDS
						 */
						
						else {
							/* 
							 * Waiting the end of AT command execution is demanded
							 * to 'ATListenerStd', that must be used also to process
							 * the response to an AT command						
							 */
							try { ATCMD.send(comandoAT, ATListSTD); }
							catch (ATCommandFailedException ATex) { 
								if(debug){
									System.out.println("Th*ATsender: ATCommandFailedException"); 
								}
								new LogError("Th*ATsender: ATCommandFailedException");	
							}
							catch (IllegalStateException e){
								if(debug){
									System.out.println("Th*ATsender: IllegalStateException");
								}
								new LogError("Th*ATsender: IllegalStateException");				
							}
							catch (IllegalArgumentException e){
								if(debug){
									System.out.println("Th*ATsender: IllegalArgumentException");
								}
								new LogError("Th*ATsender: IllegalArgumentException");
							}
							if(comandoAT.indexOf("ATA")>=0)
								Thread.sleep(30000);
														
						}
						
						// Wait before repeat cycle
						Thread.sleep(whileSleep);
					
					} //if mbox2.numMsg
					
					// break condition
					if (stopAT) break;
					
				} //while	
				
				/* 
				 * Release object ATCommand 
				 * could be done only by setting stopAT=false
				 */
				ATCMD.release();
				
			} catch (ATCommandFailedException ATex) { 
				if(debug){
					System.out.println("Th*ATsender: ATCommandFailedException");
				}
				new LogError("Th*ATsender: ATCommandFailedException");
			} catch (InterruptedException ie) {
				if(debug){
					System.out.println("Th*ATsender: InterruptedException");
				}
				new LogError("Th*ATsender: InterruptedException");
			}
			catch (Exception e){
				System.out.println("Th*ATsender: Exception2");
				e.printStackTrace();
			}//catch
			new LogError("Reboot ATsender");
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
	
} //ATsender



