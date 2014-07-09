/*	
 * Class 	CountSem
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * SemaforoEV extension, provides the ability to request / provide
 * a number of "tokens" different from 1
 * 
 * @version	1.00 <BR> <i>Last update</i>: 06-07-2007
 * @author 	alessioza
 * 
 */
public class CountSem extends SemaforoEV {
	
	/* 
	 * constructors
	 */
	public CountSem(int v, boolean fifo) {
		super(v, v==0 ? 1 : v, fifo);		// max is irrelevant
	}
	public CountSem(int v) {
		this(v, true);
	}
		
	
	/* 
	 * methods
	 */
	public synchronized void getCoin(int qty) {		// REQUESTED 'qty' "TOKENS"
		Thread curTh = Thread.currentThread();
		if (value < qty) {
			/* If al least 'qty' tokens are available, stop thread
			 * execution and add to queue  */
			waitNum++;
			if(isFifo) { 	// add to FIFO queue
				codaAttesa.addElement(new WaitingThread(curTh, false, qty));	
			} else {		// add to LIFO head (position 0)
				codaAttesa.insertElementAt(new WaitingThread(curTh, false, qty), 0);
			}
			//System.out.println("GET: put "+curTh.getName()+"to queue");
			//System.out.println("GET: number of threads in queue is: "+waitNum);
			while(true) {
				try {
					wait();
				} catch (InterruptedException ie) {			
				} //catch
				/* if you have to awaken this thread,
				 * it isn't in general the first of the queue */
				for (int i=0; i<codaAttesa.size(); i++) {
					WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
					if (wt.th == curTh) {
						// if I find the current thread...
						if (wt.wakenUp) {
							// and was awakened...
							////System.out.println("GET: "+curTh.getName()+" has been awakened, I remove it from the queue");
							codaAttesa.removeElementAt(i);	
							waitNum--;
							/* the value of the semaphore is already OK */
							return;
						} else break;		/* not to awaken, re-suspend */
					} //if wt.th
					else;
				} //for
			} //while
		} else {
			value -= qty;			/* decreases semaphore of 'qty' quantity */
			//System.out.println("GET: I can execute thread "+curTh.getName()+" immediately");
		} //if value
		//System.out.println("GET: Semaphore value is now: "+value);
		//System.out.println("GET: Number of thread in the queue: "+waitNum);
	} //getCoin(int qty)
	
	public synchronized void putCoin(int qty) {		// SUPPLY of 'qty' "TOKENS"
		value += qty;		// increase semaphore
		if (waitNum > 0) {	
			// if there are waiting threads, tries to satisfy them immediately
			//System.out.println("PUT: There are threads in the queue");
			while(true) {
				/* Search for the first thread (among those suspended and not satisfied)
				 * having the maximum of the required quantity (<= value) */
				int maxReq = 0;
				int found = -1;		// index of Thread to da awaken
				for (int i=0; i<codaAttesa.size(); i++) {
					WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
					if (!wt.wakenUp && wt.qty <= value && wt.qty > maxReq) {
						/* If thread has NOT been awakened yet and satify 
						 * requirements, then I can awaken it */
						found = i;		// found a thread to awaken
						maxReq = wt.qty;
					} //if !wt.wakenUp...ecc
				} //for
				if (found == -1) {
					// not found another thread to awaken
					return;
				} //if found
				value = value - maxReq;
					// decreased semaphore of the amount required by the awakened thread
				//System.out.println("PUT: I prepare thread for awakening");
				((WaitingThread)codaAttesa.elementAt(found)).wakenUp = true;
				//System.out.println("PUT: notify to all threads");
				notifyAll();
			} //while
		} //if waitNum
		//System.out.println("PUT: Semaphore value is now: "+value);
		//System.out.println("PUT: Number of thread in the queue: "+waitNum);
	} //putCoin(int qty)
	
	public synchronized void putCoin() {	// delete quntity checking
		putCoin(1);
	} //putCoin()
	
} //CountSem

