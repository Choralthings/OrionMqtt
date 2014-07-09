/*	
 * Class 	BCListener
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Listener for the control of events on TCP and UDP sockets
 * 
 * @version	1.00 <BR> <i>Last update</i>: 13-09-2007
 * @author 	alessioza
 * 
 */
public class BCListener extends BCListenerCustom implements GlobCost {
		
	/* 
	 * local variables
	 */
	
	
	/* 
	 * constructors
	 */
	public BCListener() {
		//System.out.println("BCListener: CREATED");
	}
	
	
	/* 
	 * methods
	 */
	/**
	 * Callback method for bearer state changes. 
	 * Any integer value returned by this callback different then the ones
	 * specified above should to be treated as unknown state.
	 * 
	 * @param state
	 */
	public void stateChanged (int state) {
		
		if (state==BEARER_STATE_CLOSING) {
			//System.out.println("***\nBCListener: " + BSclosing + "\n***");
		}
		
		else if (state==BEARER_STATE_CONNECTING) {
			//System.out.println("***\nBCListener: " + BSconnecting + "\n***");
		}
		
		else if (state==BEARER_STATE_DOWN) {
			//System.out.println("***\nBCListener: " + BSdown + "\n***");
		}
		
		else if (state==BEARER_STATE_LIMITED_UP) {
			//System.out.println("***\nBCListener: " + BSlimitedUp + "\n***");
		}
		
		else if (state==BEARER_STATE_UP) {
			//System.out.println("***\nBCListener: " + BSup + "\n***");
		}
		
	} //stateChanged 
	
} //BCListener

