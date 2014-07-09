/*	
 * Class 	GoToPowerDown
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.util.EmptyStackException;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Sets the application to put the module in Power Down,
 * setting date and time of awakening of the module and
 * writing to FILE all data and system settings to recover 
 * at next start of the application.
 * 
 * @version	1.02 <BR> <i>Last update</i>: 25-10-2007
 * @author 	alessioza
 * 
 */
public class GoToPowerDown extends ThreadCustom implements GlobCost {
	
	/*
	 * local variables 
	 */
	protected Object	objGPRMC;
	protected String	GPRMCorig, GPRMCmod;
	protected String	dataGPRMC = "00/00/00";
	protected String	oraGPRMC  = "00:00:00";
	protected String	timeSveglia;
	protected int 		virgIndex;
	protected boolean	fixFlag = false;
	// Array for GPS position strings
	private String[] 	arrayGPS;
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the AT resource
	 * flag 	 'ATexec', indicates if AT resource is busy
	 * flag		 'validFIXgprs', indicates if it has been completed sending
	 *                           of GPS strings through GPRS
	 * flag		 'FIXtimeoutExpired', indicates if FIXtimeout expires
	 * flag		 'isFirstRun', indication for first application execution
	 * Mailbox   'mboxMAIN', to send msg to AppMain
	 * Mailbox   'mbox2', to send msg to th2 (ATsender)
	 * Mailbox   'mbox4', to receive msg with this thread
	 * DataStore 'dsData', to deposit GPS strings
	 * FlashFile 'file', to save settings
	 * FlashRecordStore 'flash', to save settings
	 */


	/*
	 * constructors 
	 */
	public GoToPowerDown() {
		if(debug){
			System.out.println("Th*GoToPowerDown: CREATED");
		}
		// Final GPS string has 19 chars (without checksum, added after)
		arrayGPS = new String[19];
	}
	
	
	/*
	 * methods
	 */
	public void run() {
		if(debug){
			System.out.println("Th*GoToPowerDown: STARTED");
		}
		/* 
		 * Two possibilities:
		 *  1) valid FIX
		 *  2) not valid FIX or 'FIXtimeout' expired, timeout within which
		 *     it must be done the first FIX
		 *  
		 * In any case, I have to put the module in Power Down, if the
		 * timeout has expired I keep good the current Real Time Clock to
		 * calculate current date and time of awakening. 
		 * (Please note: possible problem if at the first start of the
		 *               application is not made a FIX)
		 */
		try {
			
			/*
			 * [1] EXTRACT CURRENT DATE/TIME
			 */
				
			/*
			 * CASE n.1: valid FIX correctly sent through GPRS and timeout not expired:
			 * 			 a) if actual string is valid, I use it
			 * 			 b) if actual string is not valid, I use the last valid
			 */
			if (infoS.getValidFIX()==true){
				
				if(debug){
					System.out.println("Th*GoToPowerDown: CASE n.1");
				}
				
				Posusr msg = new Posusr();
				msg.addInfoStato(infoS);
				String tempRMC = (String)dsDataRMC.readOnlyIfObjectIsValid();
				String tempGGA = (String)dsDataGGA.readOnlyIfObjectIsValid();
				if(tempRMC!=null)
					objGPRMC = msg.set_posusr(tempRMC, tempGGA);
				else
					objGPRMC = null;
				
				// check validity
				if (objGPRMC != null) {
					// if current string is valid --> OK
					GPRMCorig = (String)objGPRMC;
				}
				else {
					/* if current string is not valid
					 * then get the last valid
					 */ 
					tempRMC = (String)dsDataRMC.getLastValid();
					tempGGA = (String)dsDataGGA.getLastValid();
					if((tempRMC != null) && (!(tempRMC.equals(""))))
						GPRMCorig = msg.set_posusr(tempRMC, tempGGA);
					else
						GPRMCorig = "";
//					GPRMCorig = (String)choralQueue.LastValidElement();
				}
				// I create a local copy for subsequent processing
				GPRMCmod = GPRMCorig;
				if(debug){
					System.out.println("Th*GoToPowerDown: Letta la stringa: " + GPRMCorig);
				}
				
				/* 
	    		 * Convert GPS string in a array of strings 
	    		 * 
	    		 */
				
				virgIndex = 0;
				for (int i=0; i<arrayGPS.length-2; i++) {
					virgIndex = GPRMCmod.indexOf(","); /* =6 */
					//System.out.println("virgIndex: "+virgIndex);
					if (virgIndex>0) {
						arrayGPS[i] = GPRMCmod.substring(0,virgIndex); /* from 0 to 5 */
					} else arrayGPS[i] = null;	// null if field is empty
					//System.out.println("Item inserted into array: "+arrayGPRMC[i]);
					GPRMCmod = GPRMCmod.substring(virgIndex+1); /* from 7 to FINE_STRINGA */
					//System.out.println("letturaStringa: "+letturaStringa);
				}
				// pay attention to checksum, is preceded by *
				virgIndex = GPRMCmod.indexOf("*"); 	// identify *
				if (virgIndex>0) {
					arrayGPS[arrayGPS.length-2] = GPRMCmod.substring(0,virgIndex);
				} else arrayGPS[arrayGPS.length-2] = null;
				arrayGPS[arrayGPS.length-1] = GPRMCmod.substring(virgIndex+1);
				
				/* 
				 * Data of interest are into arrayGPS at indexes:
				 * 		 5 -> time in format hhmmss
				 * 		 4 -> date in format yymmdd
				 */		    		
				// extract time in format hh:mm:ss and date in format yy/mm/dd
				oraGPRMC = arrayGPS[5].substring(0,2)+":"+arrayGPS[5].substring(2,4)+":"+arrayGPS[5].substring(4,6);
				dataGPRMC = arrayGPS[4].substring(0,2)+"/"+arrayGPS[4].substring(2,4)+"/"+arrayGPS[4].substring(4,6);
				/* Please note: use satellite time */
				
				// SET RTC
				semAT.getCoin(5);
					if(debug){
						System.out.println("Th*GoToPowerDown: Set RTC in progress...");
					}
				
					// send 'at+cclk'
					infoS.setATexec(true);
					if(debug){
						System.out.println("Th*GoToPowerDown: ATexec = " + infoS.getATexec());
					}
						mbox2.write("at+cclk=\"" + dataGPRMC + "," + oraGPRMC + "\"\r");
						if(debug){
							System.out.println("Th*GoToPowerDown: sent message: at+cclk..");
							System.out.println("Th*GoToPowerDown,at+cclk: wait for AT resource is free...");
						}
				    while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				
				semAT.putCoin();		
				
			} //case 1
			
			/*
			 * CASE n.2: timeout expired, put module in Power Down
			 * 			 recovering current time and date from RTC
			 */
			else if (infoS.getValidFIX()==false) {
				
				if(debug){
					System.out.println("Th*GoToPowerDown: CASE n.2");
				}
				
				// recover time and date from RTC through read command 'AT+CCLK?'
				semAT.getCoin(5);
				
				if(debug){
					System.out.println("Th*GoToPowerDown: read RTC in progress...");
				}
				
				// send 'at+cclk?'
				infoS.setATexec(true);
				mbox2.write("at+cclk?\r");
				while(infoS.getATexec()) { Thread.sleep(whileSleep); }
		
				semAT.putCoin();	
					
				// wait for answer
				while(infoS.getDataGPRMC() == null || infoS.getOraGPRMC() == null) { Thread.sleep(whileSleep); }

				dataGPRMC = infoS.getDataGPRMC();
				oraGPRMC = infoS.getOraGPRMC();
				infoS.setDataOraGPRMC(null, null);
				GPRMCorig = "";
				
				if(debug){
					System.out.println("date GPRMC: " + dataGPRMC);
					System.out.println("time GPRMC: " + oraGPRMC);
				}
				
			} //case 2
			
			/*
			 * CASE n.3: other
			 */
			else {
				if(debug){
					System.out.println("Case n.3");
					System.out.println("Valid FIX GPRS: "+infoS.getValidFIXgprs());
					System.out.println("FIX timeout EXPIRED: "+infoS.isFIXtimeoutExpired());
				}
				GPRMCorig = "";
			} //case 3
			
			
			/*
			 * [2] SET TIME/DATE OF AWAKENING 
			 * 
			 * not to do incase of closure for battery low
			 */
			if (infoS.getSTATOexecApp().equalsIgnoreCase(execBATTSCARICA)) {
				/*
				 * Don't set time of awakening
				 */
				if(debug){
					System.out.println("Th*GoToPowerDown: not set time of awakening!");
				}
				
			} else {
				// Calculate new time of awakening
				CalcolaSveglia cs = new CalcolaSveglia();
				if(debug){
					System.out.println("Th*GoToPowerDown: calculation of new time of awakening...");
				}
				cs.putTime(dataGPRMC,oraGPRMC);
				timeSveglia = cs.setSveglia();
				
				if(!timeSveglia.equals("error")){
					semAT.getCoin(5);
						if(debug){
							System.out.println("Th*GoToPowerDown: Set alarm time in progress...");
						}
	
						// send 'at+cala'
						infoS.setATexec(true);
						//System.out.println("Th*GoToPowerDown: ATexec = " + infoS.getATexec());
						mbox2.write("at+cala=\"" + timeSveglia + "\"\r");
						if(debug){
							System.out.println("Th*GoToPowerDown: sent message 'at+cala'...");
						}
						//System.out.println("Th*GoToPowerDown: wait for AT resource is free...");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					semAT.putCoin();
				}
			}			
			
			
			/* 
			 * [3] SAVE SYSTEM SETTINGS to file
			 */
			if(debug){
				System.out.println("Th*GoToPowerDown: saving system settings to FILE in progress...");
			}
			
			/* Save last valid GPS position string:
			 * 	- if FIX done in this session  --> replace it with the last valid
			 * 	- if FIX not done  --> maintain original one
			 */
			Posusr msg = new Posusr();
			msg.addInfoStato(infoS);
			String tempRMC = (String)dsDataRMC.getLastValid();
			String tempGGA = (String)dsDataGGA.getLastValid();
			if((tempRMC != null) && (!(tempRMC.equals(""))))
				file.setImpostazione(LastGPSValid, msg.set_posusr(tempRMC, tempGGA));
			else
				file.setImpostazione(LastGPSValid, "");

			/* 
			 * Saving application closure mode
			 */
			
			/*
			 * FIRST EXECUTION
			 * (only if I find KEY not active at the first time that I observe it)
			 */
			if (infoS.getSTATOexecApp().equalsIgnoreCase(execFIRST)) {
			
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execFIRST);
				}
				
				// Key deactivation FIRST
				file.setImpostazione(CloseMode, closeAppDisattivChiaveFIRST);
				// pay attention, MODIFY FOR DEBUG
				//file.setImpostazione(CloseMode, closeAppFactory);
				
			}
			// NORMAL EXECUTION
			else if (infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
				
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execNORMALE);
				}
				
				// Normal OK
				if (infoS.getValidFIXgprs()==true) {
					file.setImpostazione(CloseMode, closeAppNormaleOK);
				}
				// Normal Timeout EXPIRED
				else if (infoS.getValidFIXgprs()==false) {
					file.setImpostazione(CloseMode, closeAppNormaleTimeout);
				}

			}
			// KEY DEACTIVATED
			else if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEdisattivata)) {
			
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execCHIAVEdisattivata);
				}
				
				// Key deactivation OK
				if (infoS.getValidFIXgprs()==true) {
					file.setImpostazione(CloseMode, closeAppDisattivChiaveOK);
				}
				// Key deactivation Timeout EXPIRED
				else if (infoS.getValidFIXgprs()==false) {
					file.setImpostazione(CloseMode, closeAppDisattivChiaveTimeout);
				}
				
			}
			// MOVEMENT
			else if (infoS.getSTATOexecApp().equalsIgnoreCase(execMOVIMENTO)) {
			
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execMOVIMENTO);
				}
				
				// Movement OK
				file.setImpostazione(CloseMode, closeAppMovimentoOK);
				
			}
			// AFTER RESET
			else if (infoS.getSTATOexecApp().equalsIgnoreCase(execPOSTRESET)) {
					
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execPOSTRESET);
				}
				
				// Key deactivation after RESET
				file.setImpostazione(CloseMode, closeAppPostReset);
				
			}
			// BATTERY LOW
			else if (infoS.getSTATOexecApp().equalsIgnoreCase(execBATTSCARICA)) {
					
				if(debug){
					System.out.println("Th*GoToPowerDown, closure from status :" + execBATTSCARICA);
				}
								
				// Battery Low
				file.setImpostazione(CloseMode, closeAppBatteriaScarica );
				
			}			
		
			// Write on file  	
			InfoStato.getFile();
			file.writeSettings();
			InfoStato.freeFile();
			

			/*
			 * [4] SAVING DATA on RecordStore
			 */
			if(debug){
				System.out.println("Th*GoToPowerDown: Saving data on RecordStore in progress...");
			}
			try {
				// Open RecordStore
				RecordStore rs = RecordStore.openRecordStore(recordStoreName, true);
								
				// Modify record
				try {
					String appt = (String)dsDataRMC.getLastValid();
					//System.out.println("FlashRecordStore: get: "+appt);
					byte data[] = appt.getBytes();
					//System.out.println("FlashRecordStore: converted in bytes: "+appt);
					rs.setRecord(1, data, 0, data.length);
					//System.out.println("FlashRecordStore: saved: "+appt);
				} catch (RecordStoreException rse) {
					//System.out.println("FlashRecordStore: RecordStoreException");
					// add record because not exists
					String newappt = (String)dsDataRMC.getLastValid();
					byte newdata[] = newappt.getBytes();
					rs.addRecord(newdata,0,newdata.length);
					//System.out.println("FlashRecordStore: saved: "+newappt);
				}
			    
				// Close RecordStore
				rs.closeRecordStore();
			
			} catch (RecordStoreNotOpenException rsnoe) {
				//System.out.println("FlashRecordStore: RecordStoreNotOpenException");
				new LogError("FlashRecordStore: RecordStoreNotOpenException");
			} catch (RecordStoreFullException rsfe) {
				//System.out.println("FlashRecordStore: RecordStoreFullException");
				new LogError("FlashRecordStore: RecordStoreFullException");
			} catch (RecordStoreException rse) {
				//System.out.println("FlashRecordStore: RecordStoreException");
				new LogError("FlashRecordStore: RecordStoreException");
			} catch (NullPointerException npe) {
				//System.out.println("NullPointerException in RecordStore");
				new LogError("FlashRecordStore: NullPointerException in RecordStore");
			}
			
			
			/* 
			 * Notify to AppMain to close application
			 */
			mboxMAIN.write(msgClose);
			if(debug){
				System.out.println("Th*GoToPowerDown: sent message: "+msgClose);
			}
		
		} catch (EmptyStackException e) {
			  //System.out.println("exception: " + e.getMessage());
			  new LogError("ERROR");
			  //e.printStackTrace();
		} catch (InterruptedException ie) {
			new LogError("Th*GoToPowerDown: InterruptedException ie");
		} catch (NullPointerException npe) {
			//System.out.println("NullPointerException in GoToPowerDown");
			new LogError("NullPointerException in GoToPowerDown");
		}//catch
	
	} //run
	

	
	/**
	 * Private class to calculate date and time to use to set
	 * awakening of the module
	 * 
	 * @version		
	 * @author 		alessioza
	 * 
	 */
	private class CalcolaSveglia implements GlobCost {
		
		/* 
		 * local variables
		 */
		private int 	hh,mm,ss,YY,MM,DD;
		private String	hhS,mmS,ssS,YYs,MMs,DDs,ris;
		private int 	addH,changeMM,bisest;
		
		/* 
		 * constructors
		 */
		public CalcolaSveglia() {
		}
		
		/* 
		 * methods
		 */
		
		/**
		 * Insert current date and time, to use for calculation
		 * of awakening time and date
		 * 
		 * @param	data	date with format YY/MM/DD
		 * @param 	ora		time with format hh:mm:ss
		 * 
		 */
		public void putTime(String data, String ora) {
			try{
				/* time -> hh:mm:ss */
				hh = Integer.parseInt(ora.substring(0,2));
				mm = Integer.parseInt(ora.substring(3,5));
				ss = Integer.parseInt(ora.substring(6));
				/* date -> YY/MM/DD */
				YY = Integer.parseInt(data.substring(0,2));
				MM = Integer.parseInt(data.substring(3,5));
				DD = Integer.parseInt(data.substring(6));
				if(debug){
					System.out.println("putTime\r\nhh:"+hh+" mm:"+mm+" ss:"+ss+"\r\nYY:"+YY+" MM:"+MM+" DD: "+DD);
				}
			}catch (NumberFormatException e){
				new LogError("putTime waking up error");
			}
		} //putTime
		
		/**
		 * Calculate date and time of awakening of the module
		 * 
		 * @return	string with format 'YY/MM/DD,hh:mm:ss'
		 * 
		 */
		public String setSveglia() {
			
			try{
				/* UPDATE MINUTES AND HOURS */
				/* Update minutes
				 * Please note: mSleep must be between 0 and 59 minutes */
				if(debug){
					System.out.println("CalcolaSveglia: set time of awakening in progress...");
				}
				addH = 0;
				mm = mm + infoS.getInfoFileInt(MinPowerDownOK);
				if (mm>59) {
					addH = 1; 	/* go to next hour */
					mm = mm-60;	/* update minutes */
				} //if
				/* update total numer of hours, hSleep >= 0 */
				if (addH==0) {
					hh = hh + infoS.getInfoFileInt(OrePowerDownOK);
				} else { /* addH=1 */
					hh = hh + infoS.getInfoFileInt(OrePowerDownOK) + 1;
				} //if
				/* UPDATE TIME and ev. DATE */
				/* Check leap year:
				 * are leap all years divisible by an integer multiple of 4,
				 * (as long as it is not secular years) */
				if (YY%4==0) {bisest=1;} else bisest=0;
				changeMM = 0;
				while (hh>23) {
					hh = hh - 24;
					/* if I have to change date, depends on month */
					switch (MM) { 
						case 1: { /*january*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 2: { /*february*/
							if (bisest==0) {
								if (DD>0 && DD<28) DD++;
								if (DD==28) changeMM=1;
							} else if (bisest==1) {
								if (DD>0 && DD<29) DD++;
								if (DD==29) changeMM=1;
							}
						} break;
						case 3: { /*march*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 4: { /*april*/
							if (DD>0 && DD<30) DD++;
							if (DD==30) changeMM=1;
						} break;
						case 5: { /*may*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 6: { /*june*/
							if (DD>0 && DD<30) DD++;
							if (DD==30) changeMM=1;
						} break;
						case 7: { /*july*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 8: { /*august*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 9: { /*september*/
							if (DD>0 && DD<30) DD++;
							if (DD==30) changeMM=1;
						} break;
						case 10: { /*october*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
						case 11: { /*november*/
							if (DD>0 && DD<30) DD++;
							if (DD==30) changeMM=1;
						} break;
						case 12: { /*december*/
							if (DD>0 && DD<31) DD++;
							if (DD==31) changeMM=1;
						} break;
					} //switch(MM)
					/* change month */
					if (changeMM==1) {
						if (MM>0 & MM<12) { /* month between january and november */
							MM++;
						} else { /* if decxember, go to january and change year */
							MM=1;
							YY++;
						} //if
					} //changeMM
				} //while
				// verify correct alarm setting
				if(debug){
					System.out.println("set alarm:\r\nhh:"+hh+" mm:"+mm+" ss:"+ss+"\r\nYY:"+YY+" MM:"+MM+" DD:"+DD);
				}
				/* Convert from number to strings,
				 * if number has 1 digit rather than 2, add zero where there isn't */
				hhS = Integer.toString(hh);
					if (hhS.length()==1) hhS = "0"+hhS;
				mmS = Integer.toString(mm);
					if (mmS.length()==1) mmS = "0"+mmS;
				ssS = Integer.toString(ss);
					if (ssS.length()==1) ssS = "0"+ssS;
				YYs = Integer.toString(YY);
					if (YYs.length()==1) YYs = "0"+YYs;
				MMs = Integer.toString(MM);
					if (MMs.length()==1) MMs = "0"+MMs;
				DDs = Integer.toString(DD);
					if (DDs.length()==1) DDs = "0"+DDs;
				/* create string */
				ris = YYs+"/"+MMs+"/"+DDs+","+hhS+":"+mmS+":"+ssS;
				if(debug){
					System.out.println("Result in string: "+ris);
				}
				return ris;
			} catch (NumberFormatException e){
				new LogError("Th*GoToPowerDown: NumberFormatException");
				return "error";
			}
		} //setSveglia
	} //class CalcolaSveglia
	
} //GoToPowerDown

