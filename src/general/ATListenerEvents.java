/* Class 	ATListenerEvents
 *
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.IOException;

import choral.io.PowerManager;

import com.cinterion.io.*;

/**
 * Module URCs management (AT-Events).
 * To use this class, create before a class instance and pass it
 * as parameter of method 'ATCommand.addListener'.
 * <BR>
 * Please note:
 *      "^SYSSTART" and others 'system start URCs' are passed to 
 * 		Java application after first AT command sent to the module
 * 		after power up.
 * 
 * @version	1.03 <BR> <i>Last update</i>: 20-11-2007
 * @author 	alessioza
 * 
 */
public class ATListenerEvents extends ATListenerCustom implements ATCommandListener {
		
	/* 
	 * local variables
	 */
	private int SCPOLvalue;
	Mailbox 	mboxMAIN;
	int 		numThread;
	
	boolean 	isRicevitore;
	
	
	/* 
	 * constructors
	 */
	public ATListenerEvents() {
		//System.out.println("ATListenerEvents: CREATED");
		// Mailboxes creation
		mboxMAIN = new Mailbox(20);
	}
	
	
	/* 
	 * methods
	 */
	/**
	 * URC manager.
	 */
	public void ATEvent(String event) {
		
		if(debugGSM){
			System.out.println("ATListenerEvents: " + event);
		}
		
		
		/* 
		 * Operations on ^SYSSTART
		 */
		if (event.indexOf("^SYSSTART") >=0) {
						
		} //^SYSSTART
		
		if (event.indexOf("^SYSSTART AIRPLANE MODE") >=0) {
			// AIRPLANE MODE to activate radio parts of the moduleo
			infoS.setOpMode("AIRPLANE");
			infoS.setTipoRisveglio(risveglioCala);
			// AIRPLANE MODE leaves out normal software procedure and implies module reboot
			infoS.setCALA(true);
		} //^SYSSTART AIRPLANE MODE
		
		
		/*
		 * +CALA management
		 */
		if (event.indexOf("+CALA") >=0) {
			//System.out.println("ATListenerEvents: +CALA event ignored");
			mboxMAIN.write(msgALIVE);
		} //+CALA
		
		if (event.indexOf("+CGREG") >=0) {
			infoS.setCGREG(event.substring((event.indexOf(": "))+2,(event.indexOf(": "))+3));
		}
		
		if (event.indexOf("+CREG") >=0) {
			infoS.setCREG(event.substring((event.indexOf(": "))+2,(event.indexOf(": "))+3));
		}
		
		/*
		 * Analyze answer for polling on key GPIO
		 */		
		if (event.indexOf("^SCPOL: 6") >=0) {
			SCPOLvalue = Integer.parseInt(event.substring(event.indexOf(",")+1,event.indexOf(",")+2));
			infoS.setGPIOchiave(SCPOLvalue);
			infoS.setDigitalIN(SCPOLvalue,0);
		} //GPIO7
		
		/*
		 * Analyze answer for polling on GPIO digital inputs
		 */
		
		// digital input 1 (GPIO1)
		if (event.indexOf("^SCPOL: 0") >=0) {
			SCPOLvalue = Integer.parseInt(event.substring(event.indexOf(",")+1,event.indexOf(",")+2));
			infoS.setDigitalIN(SCPOLvalue,1);
			//mboxMAIN.write(msgALR1);
		} //GPIO1
		
		// digital input 2 (GPIO3)
		if (event.indexOf("^SCPOL: 2") >=0) {
			SCPOLvalue = Integer.parseInt(event.substring(event.indexOf(",")+1,event.indexOf(",")+2));
			infoS.setDigitalIN(SCPOLvalue,2);
			//mboxMAIN.write(msgALR2);
		} //GPIO3
		
		
		/* 
		 * RING operations
		 */
		if (event.indexOf("RING") >=0) {
			//System.out.println("ATListenerEvents: incoming call waiting for answer...");
			// send msg to mboxMAIN
			//infoS.setCSDattivo(true);
			if(event.indexOf("REL ASYNC") > 0)
				mboxMAIN.write(msgRING);
			
		} //RING
		
		
		/*
		 * Undervoltage // Pay attention: can be a source of problems if there is an initial Undervoltage ?
		 */
		if (event.indexOf("^SBC: Undervoltage") >=0) {
			// send msg to AppMain about low battery
			mboxMAIN.write(msgBattScarica);
		} //^SBC: Undervoltage
		
		
		/* 
		 * +CMTI operations (new SMS received)
		 */
		if (event.indexOf("+CMTI") >=0) {
			infoS.setResponseAT(event);
			infoS.setNumSMS(1);
		} //+CMTI
		
		if (event.indexOf("^SCKS") >=0) {
			//System.out.println(event);
			new LogError(event);
			if (event.indexOf("2") >=0){
				infoS.setReboot();
				infoS.setInfoFileInt(UartNumTent,"1");
			}
		}
		
	} //ATEvent
	
	public void CONNChanged(boolean SignalState) {}
	public void RINGChanged(boolean SignalState) {}
	public void DCDChanged(boolean SignalState) {}
	public void DSRChanged(boolean SignalState) {}
	
	/**
	 *  Pass to a thread a Mailbox object
	 *  
	 *  @param	mb		Mailbox	object
	 *  @param 	nMbox	mailbox number
	 *  @param 	nth		thread number
	 *  @param	isRcv	mailbox-owner indication about thread
	 *  @return "OK,<mailbox name>"
	 */
	public synchronized String addMailbox(Mailbox mb, int nMbox, int nth, boolean isRcv) {
		// you can pass up to 10 (maximum) mailboxes for each Thread
		switch (nMbox) {
			case 0: {
				mboxMAIN = new Mailbox(20);
				mboxMAIN = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mboxMAIN";
			}
			default: return "Error";
		} //switch		
	} //addMailbox

	
} //ATListenerEvents


