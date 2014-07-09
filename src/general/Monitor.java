/*	
 * Class 	Monitor
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Implementation of Java Monitor
 * 
 * @version	1.00 <BR> <i>Last update</i>: 06-07-2007
 * @author 	alessioza
 * 
 */
public class Monitor {

	/* 
	 * variables
	 */
	/** Binary semaphore for mutual exclusion, it is a LIFO semaphore */
	private SemaforoEV mutex = new SemaforoEV(true);
	/** Semaphore to block momentarily threads that have executed a signal
	 *  to free other threads 	*/
	private SemaforoEV urgent = new CountSem(0, false);
	/** Number of threads on urgent semaphore */
	private int urgentCount = 0;

		
	/* 
	 * methods
	 */
	public void mEnter() {		// input for an entry
		mutex.getCoin();
	}
	public void mExit() {		// output for an entry
		if (urgentCount >0) urgent.putCoin();	
			// gives mutex to urgent thread
		else mutex.putCoin();	// release mutex
	}
	
	
	/* 
	 * Internal class to define a Condition
	 */
	protected class Condition {
		
		/* 
		 * variables
		 */		
		/** Binary semaphore for waiting, always RED */
		private SemaforoEV cond = new SemaforoEV(0,1);
		/** Counter of waiting threads */
		private int condCount = 0;
		
		
		/* 
		 * methods
		 */
		public void cWait() {		// wait on condition
			condCount++;		// add a waiting thread
			if (urgentCount >0) urgent.putCoin();
				// rather than release mutex, it 'gives' to the urgent thread
			else mutex.putCoin();	// release mutex
			cond.getCoin();		// wait for semaphore
			condCount--;
		} //cWait
		
		public void cSignal() {		// signal on condition
			if (condCount >0) {
				// there are threads that waiting on condition
				urgentCount++;
				cond.putCoin();		// awakening
				urgent.getCoin();		// wait for awakening
				urgentCount--;
			} //if
			// no operation if there aren't threads waiting on the condition
		} //cSignal
		
	} //Condition
	
} //Monitor

