/*	
 * Class 	TestChiave
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Thread that controls during application execution if occurs an event of
 * key activation or deactivation.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 04-11-2008
 * @author 	matteobo
 * 
 */
public class TestChiave extends ThreadCustom implements GlobCost {
	
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore  'semAT', for the exclusive use of the AT resource
	 * InfoStato  'infoS', status informations about application
	 * Mailbox    'mboxMAIN', to send msg to AppMain
	 * Mailbox    'mbox2', to send msg to ATsender
	 */

	
	/* 
	 * constructors
	 */
	public TestChiave() {
		//System.out.println("Th*TestChiave: CREATED");
	}
	
	/*
	 * methods
	 */
	/**
	 * Contains thread execution code.
	 * <BR>
	 * Notify to AppMain an event of activation or deactivation of the key.
	 */
	public void run() {
		
		//System.out.println("Th*TestChiave: STARTED");
		
		try {

			//System.out.println("Th*TestChiave: monitoring key GPIO");

			
			/*
             * Enables polling on key GPIO and check the initial value
             */ 

			// reserve AT resource
			semAT.getCoin(5);
						
			/*
			 * GPIO init
			 */ 
			
			
			infoS.setATexec(true);
			mbox2.write("at^scpin=1,6,0\r");
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }

			/*
			 * Activate GPIO n.1 and n.3
			 */ 
			infoS.setATexec(true);
			mbox2.write("at^scpin=1,0,0\r");	// GPIO1
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
			
			infoS.setATexec(true);
			mbox2.write("at^scpin=1,2,0\r");	// GPIO3
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }

			/*
			 * Activate polling
			 */ 

			infoS.setATexec(true);
			mbox2.write("at^scpol=1,6\r");
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }

			/*
			 * Activate polling for GPIO n.1 and n.3
			 */ 
			infoS.setATexec(true);
			mbox2.write("at^scpol=1,0\r");	// GPIO1
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
		
			infoS.setATexec(true);
			mbox2.write("at^scpol=1,2\r");	// GPIO3
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }
 			
			/*
			 * Check GPIO initial values
			 */ 									
			infoS.setATexec(true);
			infoS.setGPIOnumberTEST(7);
			mbox2.write("at^sgio=6\r");
	 		while(infoS.getATexec()) { Thread.sleep(whileSleep); }

			/*
			 * Check GPIO n.1 and n.3 initial values
			 */
			infoS.setATexec(true);
			infoS.setGPIOnumberTEST(1);
			mbox2.write("at^sgio=0\r");		//GPIO1
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }

			infoS.setATexec(true);
			infoS.setGPIOnumberTEST(3);
			mbox2.write("at^sgio=2\r");		//GPIO3
			while(infoS.getATexec()) { Thread.sleep(whileSleep); }	 			
			
			// Release At resource
			semAT.putCoin();
			
			// Notify to AppMain that polling is enabled
			infoS.setPollingAttivo(true);
			mboxMAIN.write(msgALIVE);
			
			// MAIN LOOP
			while(true) {
				
				// Pause
				Thread.sleep(whileSleepGPIO);
				
				semAT.getCoin(5);
				infoS.setATexec(true);
				infoS.setGPIOnumberTEST(7);
				mbox2.write("at^sgio=6\r");
				while(infoS.getATexec()) { Thread.sleep(whileSleep); }
 				semAT.putCoin();
 				
				/*
				 * KEY ACTIVATED (GPIO = "0")
				 */
				if (infoS.getGPIOchiave()==0) {
							
					// Send msg to AppMain
					if(debug_chiave){
						System.out.println("Th*TestChiave: KEY ACTIVATED!!!");
					}
					mboxMAIN.write(msgChiaveAttivata);
					
					// stop send of other messages
					infoS.setGPIOchiave(-1);
						
				} //KEY ACTIVATED
									
				/*
				 * KEY DEACTIVATED (GPIO = "1")
				 */
				else if (infoS.getGPIOchiave()==1) {
						
					// Send msg to AppMain
					mboxMAIN.write(msgChiaveDisattivata);
					/*semAT.getCoin(5);
						infoS.setATexec(true);
						mbox2.write("at^spio=0\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					semAT.putCoin();
					*/
					if(debug_chiave){
						System.out.println("Th*TestChiave: KEY DEACTIVATED!!!");
					}
					
					// blocco invio di ulteriori messaggi
					infoS.setGPIOchiave(-1);
					//break;
									
				} //KEY DEACTIVATED
								
			} //while(true)
			
		} catch(InterruptedException ie) {
			//System.out.println("exception: " + ie.getMessage());
			//ie.printStackTrace();
		} //catch
		
	} //run
	
} //TestChiave

