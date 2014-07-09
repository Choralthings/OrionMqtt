/*	
 * Class 	Update CSD
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.util.Timer;

/**
 * Thread to control the update of configuration file settings through a CSD call. 
 * 
 * @version	1.02 <BR> <i>Last update</i>: 25-10-2007
 * @author 	alessioza
 * 
 */
public class UpdateCSD extends ThreadCustom implements GlobCost{
	
	/* 
	 * local variables
	 */
	private boolean closeSession = false;
	/** <BR> Timer for WatchDog on CSD call. */
	Timer 		CSDTimer;
	/** <BR> Task to execute WatchDog control on CSD call. */
	TimeoutTask CSDTimeoutTask;
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the AT resource
	 * InfoStato 'infoS', status informations about application
	 * Mailbox   'mbox2', to send msg to th2 (ATsender)
	 */

	
	/* 
	 * constructors
	 */
	public UpdateCSD() {
		if(debug){
			System.out.println("Th*UpdateCSD: CREATED");
		}
	}
	
	/*
	 * methods
	 */
	/**
	 * Contains thread execution code.
	 * <BR> ---------- <BR>
	 * Performed operations: <br>
	 * <ul type="disc">
	 *  <li> Disabling key;
	 * 	<li> Open CSD channel;
	 *  <li> Pass control to ATsender for parameters configuration;
	 *  <li> Enable key, when CSD connection is closed.	 
	 * </ul>
	 */
	public void run() {
		
		if(debug){
			System.out.println("Th*UpdateCSD: STARTED");
		}
		
		infoS.setCSDWatchDog(true);
		closeSession = false;
		
		// Create and start 'CSDTimeout' timer
		CSDTimer = new Timer();
		CSDTimeoutTask = new TimeoutTask(CSDtimeout);
		CSDTimeoutTask.addInfoStato(infoS);
		CSDTimer.schedule(CSDTimeoutTask, CSDTOvalue*1000);
		
	    try {

	    	// Disable KEY
			infoS.setInibizioneChiave(true);
			infoS.setCSDattivo(true);

	    	// Reserve AT resource
			semAT.getCoin(5);
						
			
			
			if (infoS.getCSDWatchDog() == false) { // If timeout expired -> exit
				closeSession = true;
			}
		    					
		    if (infoS.getCSDWatchDog() == true && closeSession == false) {
		    	
		    	// Timer and timeout reset and new start
		    	CSDTimer.cancel();
		    	CSDTimeoutTask.cancel();		
		    	CSDTimer  = new Timer();
		    	CSDTimeoutTask 	= new TimeoutTask(CSDtimeout);
		    	CSDTimeoutTask.addInfoStato(infoS);
		    	CSDTimer.schedule(CSDTimeoutTask, CSDTOvalue*1000);		    
		    
		    	/*
		    	 * Answer to CSD call
		    	 */ 
		    	if(debug){
		    		System.out.println("Th*UpdateCSD: I'm responding to the CSD call");
		    	}
		    	infoS.setATexec(true);
		    	mbox2.write("ATA\r");
			    
		    	// Wait until CSD connection is established
		    	while(infoS.getCSDconnect()== false) { 
		    		if (infoS.getCSDWatchDog() == false) { // if timeout expired -> exit
		    			closeSession = true;
		    			break;
		    		}
		    		Thread.sleep(whileSleep);
		    	} //while
		    } //if
		    
		    if (infoS.getCSDWatchDog() == true && closeSession == false) {
		    	
		    	// Timer and timeout reset and new start
		    	CSDTimer.cancel();
		    	CSDTimeoutTask.cancel();
					    
		    	// Until only streams are used, set ATexec = true
		    	infoS.setATexec(true);
    		
		    	// With CSD connection, start input and output streams
		    	if(debug){
		    		System.out.println("Th*UpdateCSD: Open CSD stream");
		    	}
		    	mbox2.write(csdOpen);
			
		    	// Write on output stream
		    	if(debug){
		    		System.out.println("Th*UpdateCSD: Write on output stream");
		    	}
		    	mbox2.write(csdWrite + "\n\rGreenwich Connected\n\r");
			
		    	// Read from input stream
		    	if(debug){
		    		System.out.println("Th*UpdateCSD: Read from input stream");
		    	}
		    	mbox2.write(csdRead);
			
		    	// ... authentication and commands ...
				
		    	// Wait until CSD connection is used
		    	while(infoS.getCSDconnect()== true) { Thread.sleep(whileSleep);	}
		    
		    } //if
		    
		    if (infoS.getCSDWatchDog() == true && closeSession == false) {
			
		    	// Restart timer
		    	CSDTimer  = new Timer();
				CSDTimeoutTask 	= new TimeoutTask(CSDtimeout);
				CSDTimeoutTask.addInfoStato(infoS);
				CSDTimer.schedule(CSDTimeoutTask, CSDTOvalue*1000);
			
				// Close CSD call
				if(debug){
					System.out.println("Th*UpdateCSD: I'm releasing CSD call");
				}
				infoS.setATexec(true);
				mbox2.write("ATH\r");
				while(infoS.getATexec()) { 
					if (infoS.getCSDWatchDog() == false) break;
					Thread.sleep(whileSleep);
				} //while	
				
		    } //if
		    
		    // Delete timer and task
	    	CSDTimer.cancel();
	    	CSDTimeoutTask.cancel();
	
		} catch (InterruptedException ie) {
			//System.out.println("UpdateCSD: InterruptedException");
		} //catch
		
    	// Release AT resource
		semAT.putCoin();
		
    	// Enable KEY
		infoS.setInibizioneChiave(false);
		infoS.setCSDattivo(false);		
		
		if(debug){
			System.out.println("Th*UpdateCSD: END");
		}
		
	} //run
	
} //UpdateCSD


