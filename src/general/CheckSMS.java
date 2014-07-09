/*	
 * Class 	CheckSMS
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import choral.io.InfoMicro;

/**
 * Thread dedicated to SMS reception during application execution.
 * 
 * @version	1.04 <BR> <i>Last update</i>: 14-12-2007
 * @author 	alessioza
 * 
 */
public class CheckSMS extends ThreadCustom implements GlobCost{
	
	/* 
	 * local variables
	 */
	private String	text;
	int num = 0;
	InfoMicro infoGW;
	String release = "";
	int tempTimer1 = 10000;
	int countTimer1 = 0;
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the AT resource
	 * InfoStato 'infoS', status informations about application
	 * Mailbox   'mboxMAIN', to send msg to AppMain
	 * Mailbox   'mbox2', to send msg to ATsender
	 */

	
	/* 
	 * constructors
	 */
	public CheckSMS() {
		//System.out.println("Th*CheckSMS: CREATED");
	}
	
	/*
	 * methods
	 */
	/**
	 * Contains thread execution code.
	 * <BR>
	 * Notify to AppMain reception of a new SMS for tracking.
	 */
	public void run() {
		
		//System.out.println("Th*CheckSMS: STARTED");
		
		try {

			try{
				infoGW = new InfoMicro();
				release = infoGW.getRelease();
			} catch (Exception e){}
			/*
			 * Config module for SMS reception
			 */ 
			semAT.getCoin(5);
			
			infoS.setATexec(true);
			mbox2.write("AT+CMGF=1\r");
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
			
			infoS.setATexec(true);
			mbox2.write("AT+CPMS=\"MT\",\"MT\",\"MT\"\r");
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
			
			infoS.setATexec(true);
			mbox2.write("AT+CNMI=1,1\r");
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
			
			/*
			 * Analyze SMS presence on ME and delete
			 */
			while (true) {
				
				// CPMS
				infoS.setATexec(true);
				mbox2.write("AT+CPMS?\r");
				while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				
				/*
				 * Extract how many SMS are already read in the memory.
				 * Exit from loop if no SMS are found.
				 */ 				
				if (infoS.getNumSMS() > 0) {
					
					if(num > infoS.getMaxNumSMS())
						num = 0;
					num++;

					// Delete message
					infoS.setATexec(true);
					mbox2.write("AT+CMGD="+num+"\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					if(debug){
						System.out.println("Th*CheckSMS: deleted msg n. " + num + " from ME");
					}
				
				} else {
					num=0;
					break;
				}
				
				// Reset 'numSMS' and 'CodSMS'
				infoS.setNumSMS(0);
				infoS.setCodSMS(-1);
				
			} //while
			
			semAT.putCoin();
			
			if(debug){
				System.out.println("Th*CheckSMS: config OK");			
				System.out.println("Th*CheckSMS: wait for new SMS");
			}
			
			// Reset 'numSMS' and 'CodSMS'
			infoS.setNumSMS(0);
			infoS.setCodSMS(-1);			
			
		} catch(InterruptedException ie) {
			//System.out.println("CheckSMS: InterruptedException (1st part)");
			new LogError("CheckSMS InterruptedException (1st part)");
			
		} catch(StringIndexOutOfBoundsException sie) {
			//System.out.println("CheckSMS: StringIndexOutOfBoundsException (1st part)");
			new LogError("CheckSMS StringIndexOutOfBoundsException (1st part)");

		} catch (Exception e) {
			//System.out.println("CheckSMS: generic Exception (1st part)");
			new LogError("CheckSMS generic Exception (1st part)");
		}
			
		
		// MAIN LOOP, wait always for new SMS
		while(true) {	
			
			// Check Crash Alarm
			if(infoS.getAlarmCrash()){
				infoS.setAlarmCrash(false);
				String messaggio = infoS.getCoordinate();
				// send alarm to operator
				infoS.setATexec(true);
				mbox2.write("AT+CMGS=\"" + infoS.getInfoFileString(Operatore) + "\"\r");
				if(debug){
					System.out.println("AT+CMGS=\"" + infoS.getInfoFileString(Operatore) + "\"\r");
				}
				while(infoS.getATexec()) { try {
					Thread.sleep(whileSleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
				
				// PAY ATTENTION, answer to wait isn't 'OK' but '>' -> OK
				infoS.setATexec(true);
				mbox2.write(messaggio + "\032");
				
				while(infoS.getATexec()) { try {
					Thread.sleep(whileSleep);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} }
			}// end check crash alarm
			
			try{
				semAT.getCoin(5);
				
				// CPMS
				infoS.setATexec(true);
				mbox2.write("AT+CPMS?\r");
				while(infoS.getATexec()) { 
					try {
						Thread.sleep(whileSleep);
					} catch(InterruptedException ie) {
						//System.out.println("CheckSMS: InterruptedException (SMSsleep)");
						new LogError("CheckSMS InterruptedException (SMSsleep)");
					}
				}
				
				semAT.putCoin();
				
				/*
				 * If messages are present in memory, list all and read li e leggili one at time,
				 * consider and execute only the last
				 */
				if (infoS.getNumSMS() > 0) {
					
					num++;
					if(num > infoS.getMaxNumSMS())
						num = 1;
	
					if(debug){
						System.out.println("Th*CheckSMS: NumSMS = " + infoS.getNumSMS());
					}
	
					semAT.getCoin(5);
						
					try {
						
						// Read message
						infoS.setATexec(true);
						mbox2.write("AT+CMGR="+num+"\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						
						// extract telephone number to answer, with infoS.getNumTelSMS()
						try {
							Thread.sleep(5000);
						} catch(InterruptedException ie) {
							//System.out.println("CheckSMS: InterruptedException (SMSsleep)");
							new LogError("CheckSMS InterruptedException (SMSsleep)");
						}
						System.out.println("Th*CheckSMS: " + infoS.getNumTelSMS() + " VALID? " + infoS.getValidSMS() + " KEY: " + infoS.getSMSCommand());
						try {
							/*
		                     * Check if '+' is present, in the telephone number
		                     */ 
							if (infoS.getNumTelSMS().indexOf("+")>=0 && infoS.getValidSMS()==true) {
								
								if(infoS.getSMSCommand().equalsIgnoreCase(keySMS)){
									try {
										
										/*
										 * formatting sms text
										 */ 
										if (infoS.getValidFIX()==true) {
											text = "SW Java:" + revNumber + "\r\n" + "FW:" + release + "\r\n" + infoS.getInfoFileString(Header) + "-" + infoS.getInfoFileString(IDtraker) + ":\r\n"
										     + infoS.getDataSMS(1) + " " + infoS.getDataSMS(2) + " GMT\r\n"
											 + "LAT: " + infoS.getDataSMS(3) + "\r\n"
											 + "LON: " + infoS.getDataSMS(4) + "\r\n" 
											 //+ "ROT: " + infoS.getDataSMS(5)  + "\r\n"
											 + "ALT: " + infoS.getDataSMS(6) + " m" + "\r\n"
											 + "VEL: " + infoS.getDataSMS(7) + " kmh" + "\r\n"
											 + "BATT: " + infoS.getBatteryVoltage();
										
										}
										//System.out.println("SMS length: " + text.length());									} catch(StringIndexOutOfBoundsException sie) {
										//System.out.println("CheckSMS: StringIndexOutOfBoundsException (text)");
									} catch (Exception e) {
										//System.out.println("\r\nexception: " + e.getMessage());
										e.printStackTrace();
										//System.out.println("\r\n");
										//System.out.println("CheckSMS: generic Exception (text)");
										new LogError("CheckSMS generic Exception (text)");
										text = "Data not available";
									}
									
									/*
									 * Convert telephone number to integer, if not a number
									 * throws the exception NumberFormatException and exit from 'if'
									 */
									//System.out.print("Extracted number: " + infoS.getNumTelSMS() + ",coversion: ");
									//System.out.println(Integer.parseInt(infoS.getNumTelSMS().substring(6)));
									
									// Send msg to sender
									infoS.setATexec(true);
									mbox2.write("AT+CMGS=\"" + infoS.getNumTelSMS() + "\"\r");
									if(debug){
										System.out.println("AT+CMGS=\"" + infoS.getNumTelSMS() + "\"\r");
									}
									while(infoS.getATexec()) { Thread.sleep(whileSleep); }
									
									// PAY ATTENTION, answer to wait isn't 'OK' but '>' -> OK
									infoS.setATexec(true);
									mbox2.write(text + "\032");
									
									while(infoS.getATexec()) { Thread.sleep(whileSleep); }
									
									if(debug){
										System.out.println("Th*CheckSMS: Inviato SMS di risposta al mittente");
									}
								}
								else{
									if(infoS.getSMSCommand().equalsIgnoreCase(keySMS1)){

										infoS.setReboot();
										
									}
									else if(infoS.getSMSCommand().equalsIgnoreCase(keySMS2)){
									
										text = "+CSQ:" + infoS.getCSQ()+ ";BEARER:" + infoS.getGPRSBearer()
										+ ";CREG:" + infoS.getCREG() + ";CGREG:" + infoS.getCGREG() + ";ERR:"
										+ infoS.getERROR() + ";IN:" + infoS.getInfoFileInt(TrkIN) + ";OUT:"+infoS.getInfoFileInt(TrkOUT)
										+ ";t1:" + infoS.getTask1Timer() + ";t2:" + infoS.getTask2Timer() + ";t3:" + infoS.getTask3Timer()
										+ ";uFW:" + release + ";SW:" + revNumber;
										
										// Send msg to sender
										infoS.setATexec(true);
										mbox2.write("AT+CMGS=\"" + infoS.getNumTelSMS() + "\"\r");
										if(debug){
											System.out.println("AT+CMGS=\"" + infoS.getNumTelSMS() + "\"\r");
										}
										while(infoS.getATexec()) { Thread.sleep(whileSleep); }
										
										// PAY ATTENTION, answer to wait isn't 'OK' but '>' -> OK
										infoS.setATexec(true);
										mbox2.write(text + "\032");
										
										while(infoS.getATexec()) { Thread.sleep(whileSleep); }
										
									}
								}
									
							} //if
							
							/*
							 * If '+' not present, number is invalid -> not answers
							 */
							else {
								//System.out.println("Th*CheckSMS: No anser to SMS because telephone number or MSS text are invalid");
							} //else
						
						} catch (NumberFormatException nfe) {
							//System.out.println("Th*CheckSMS: NumberFormatException");
							new LogError("CheckSMS NumberFormatException");
						}
						
						// Delete message
						infoS.setATexec(true);
						mbox2.write("AT+CMGD="+num+"\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						
					} catch(InterruptedException ie) {
						//System.out.println("CheckSMS: InterruptedException (2nd part)");
						new LogError("CheckSMS InterruptedException (2nd part)");
						
					} catch(StringIndexOutOfBoundsException sie) {
						//System.out.println("CheckSMS: StringIndexOutOfBoundsException (2nd part)");
						new LogError("CheckSMS StringIndexOutOfBoundsException (2nd part)");
	
					} catch (Exception e) {
						//System.out.println("CheckSMS: generic Exception (2nd part)");
						new LogError("CheckSMS generic Exception (2nd part)");
					}
						
					semAT.putCoin();
					
					// Reset 'numSMS' and 'CodSMS'
					infoS.setNumSMS(0);
					infoS.setCodSMS(-1);
					
				} else {
					num=0;
					try {
						Thread.sleep(SMSsleep);
					} catch(InterruptedException ie) {
						//System.out.println("CheckSMS: InterruptedException (SMSsleep)");
						new LogError("CheckSMS InterruptedException (SMSsleep)");
					}
				} //else
				
				if(countTimer1 > 5){
					countTimer1 = 0;
					if(tempTimer1 == infoS.getTask1Timer())
						infoS.setReboot();
					tempTimer1 = infoS.getTask1Timer();
				}
				countTimer1++;
				Thread.sleep(2000);
			}catch(Exception e){
				new LogError("Exception SMS");
			}
		} //while(true)
		
	} //run
	
} //ChechSMS