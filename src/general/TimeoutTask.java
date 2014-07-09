/*	
 * Class 	TimeoutTask
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Operations to execute when timeout expires, depending on the type of timeout.
 * 
 * @version	1.02 <BR> <i>Last update</i>: 25-10-2007
 * @author 	alessioza
 * 
 */
public class TimeoutTask extends TimerTaskCustom implements GlobCost {
	
	/* 
	 * local variables
	 */
	private String 	timeoutType;
	//Runtime r;
	//long mem1, mem2;
	
	/* 
	 * constructors
	 */
	public TimeoutTask(String type) {
		//System.out.println("TimeoutTask: CREATED");
		timeoutType = type;
		//r = Runtime.getRuntime();
	}
	
	
	/*
	 * methods
	 */
	/**
	 * Task execution code.
	 * <BR> ---------- <BR>
	 * Performed operations: <br>
	 * <ul type="disc">
	 *  <li> Check timeout type, based on value passed through constructor;
	 * 	<li> Send message or sets parameters needed to notify about timeout
	 *       expiration.	 
	 * </ul>
	 */
	public void run() {
		
		try{
			
			/*
			 * FIXtimeout 
			 */
			if (timeoutType.equalsIgnoreCase(FIXgpsTimeout)) {
				
				//System.out.println("TimeoutTask, FIXgpsTimeout: STARTED");
				/*
				 * If there is a valid FIX do nothing at timeout expiration
				 */
				if (infoS.getValidFIX()==true) {
					
					if(debug){
						System.out.println("TimeoutTask, FIXgpsTimeout: FIXtimeout EXPIRED but FIX found");
					}
					
				} else {
					
					if(debug){
						System.out.println("TimeoutTask, FIXgpsTimeout: FIXtimeout EXPIRED");
					}
			
					// Set that 'FIXtimeout' is expired
					infoS.setIfIsFIXtimeoutExpired(true);
					
				} //if
				
			} //FIXtimeout
			
			/*
			 * FIXgprsTimeout 
			 */
			if (timeoutType.equalsIgnoreCase(FIXgprsTimeout)) {
				
				if(debug){
					System.out.println("TimeoutTask, FIXgprsTimeout: FIXgprsTimeout EXPIRED");
				}
				
				// Set that 'FIXgprsTimeout' is expired
				infoS.setIfIsFIXgprsTimeoutExpired(true);
					
				// Send msg to TrackingGPRS mailbox
				mbox3.write(timeoutExpired);
								
			} //FIXgprsTimeout

			/*
			 * CHIAVEtimeout
			 */
			if (timeoutType.equalsIgnoreCase(CHIAVEtimeout)) {

				//System.out.println("TimeoutTask, CHIAVEtimeout: STARTED");
				/*
				 * At timeout expiration, ENABLE again the key usage
				 */
				infoS.setInibizioneChiave(false);
				
			} //CHIAVEtimeout
			
			/*
			 * BatteryTimeout
			 */
			if (timeoutType.equalsIgnoreCase(BatteryTimeout)) {

				//System.out.println("TimeoutTask, BatteryTimeout: STARTED");
				
				// Send command AT^SBV 
				semAT.getCoin(5);
				//System.out.println("***   TimeoutTask, BatteryTimeout: EXEC COMMAND AT^SBV");
				infoS.setATexec(true);
				mbox2.write("AT^SBV\r");
				while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
			} //BatteryTimeout
			
			/*
			 * Network registration
			 */
			if (timeoutType.equalsIgnoreCase(RegTimeout)) {

				// Send command AT+COPS? 
				semAT.getCoin(5);
				
				infoS.setATexec(true);
				mbox2.write("AT+COPS?\r");
				while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
				//mem1 = r.freeMemory();
				//System.out.println("***Free memory before garbage collection: " + mem1);
				//r.gc();
				//mem1 = r.freeMemory();
			    //System.out.println("***Free memory after garbage collection: " + mem1);
				
			} //RegTimeout
			
			/*
			 * WatchDogTimeout
			 */
			if (timeoutType.equalsIgnoreCase(WatchDogTimeout)) {

				//System.out.println("TimeoutTask, WatchDogTimeout: STARTED");
				if(infoS.getTickTask1WD() && infoS.getTickTask3WD()){
				
					infoS.resetTickTaskWD();
					semAT.getCoin(5);
					
					//System.out.println("***   TimeoutTask, WatchDog: generated a WATCHDOG pulse");
				
					infoS.setATexec(true);
					mbox2.write("at^ssio=5,1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					
					// wait
					Thread.sleep(sensMovHoldTime);
					
					infoS.setATexec(true);
					mbox2.write("at^ssio=5,0\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					
					infoS.setATexec(true);
					mbox2.write("at^ssio=5,1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					
					// wait
					Thread.sleep(sensMovHoldTime);
					
					infoS.setATexec(true);
					mbox2.write("at^ssio=5,0\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					
					semAT.putCoin();
				}
				
				
			} //WatchDogTimeout
			
			/*
			 * CSDtimeout
			 */
			if (timeoutType.equalsIgnoreCase(CSDtimeout)) {

				//System.out.println("TimeoutTask, CSDtimeout: STARTED");
				/*
				 * At timeout expiration, disable CSD call
				 */
				infoS.setCSDWatchDog(false);
				
			} //CSDtimeout
			
			/*
			 * FIXgprsTimeout 
			 */
			if (timeoutType.equalsIgnoreCase(trackTimeout)) {
				
				/*
				 * If you are in a CSD call, wait until it's in progress
				 */
				
				while (infoS.getCSDattivo()==true) { Thread.sleep(whileSleep); }
				
				//System.out.println("TimeoutTask, trackTimeout: STARTED");
				mboxMAIN.write(msgChiaveAttivata);
				
			} //trackTimeout
			
		} catch (InterruptedException ie) {
			//System.out.println("exception: " + ie.getMessage());
			//ie.printStackTrace();
		} //catch
		
	} //run
	
} //TimeoutTask

