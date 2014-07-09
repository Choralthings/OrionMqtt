/*	
 * Class 	PrioSem
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Semaphore with priority queue, adding to the queue occurs according to
 * th priority declared, LOW VALUES of the parameter correspond to HIGHER
 * PRIORITIES.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 06-07-2007
 * @author 	alessioza
 * 
 */
public class PrioSem extends SemaforoEV {
	
	/* 
	 * constructors
	 */
	public PrioSem(int v, boolean fifo) {
		super(v, v==0 ? 1 : v, fifo);		// max is irrelevant
	}
	public PrioSem(int v) {
		this(v, true);
	}
		
	
	/* 
	 * methods
	 */
	public synchronized void getCoin(int prio) {		// REQUEST "TOKENS"
		// add to queue with prio priority
		Thread curTh = Thread.currentThread();
		if (value == 0) {
			/* if there are no tokens available, must wait in the queue with
			 * priority even if it has been notified (but not yet awakened) a
			 * lower priority thread compared to this */
			waitNum++;
			int pos = 0;
			// add to priority queue, with linear search
			while (pos < codaAttesa.size() && ((WaitingThread)codaAttesa.elementAt(pos)).prio <= prio) {
				pos++;
			}
			codaAttesa.insertElementAt(new WaitingThread(curTh, false, 1, prio), pos);
			//System.out.println("GET: Put "+curTh.getName()+"to queue");
			//System.out.println("GET: Number of thread in the queue: "+waitNum);
			while(true) {
				try {
					wait();
				} catch (InterruptedException ie) {			
				} //catch
				/* if you have to wake up this thread, it is not generally the
				   first of the queue */
				for (int i=0; i<codaAttesa.size(); i++) {
					WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
					if (wt.th == curTh) { 	
						// if I find current thread...
						if (wt.wakenUp) {
							// and was awakened...
							//System.out.println("GET: "+curTh.getName()+" has been awakened, I remove it from the queue");
							codaAttesa.removeElementAt(i);	
							waitNum--;
							/* semaphore value is already OK */
							return;
						} else break;		/* not to awaken, re-suspend */
					} //if wt.th
					else;
				} //for
			} //while
		} else {
			value --;			/* decrements the semaphore */
			//System.out.println("GET: I can run the thread "+curTh.getName()+" immediately");
		} //if value
		//System.out.println("GET: Semaphore value is now: "+value);
		//System.out.println("GET: Number of thread in the queue: "+waitNum);
	} //getCoin(int prio)
	
} //PrioSem

