/*	
 * Class 	SemaforoEV
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.util.*;

/**
 * Advanced semaphore, with timeout
 * 
 * @version	1.00 <BR> <i>Last update</i>: 06-07-2007
 * @author 	alessioza
 * 
 */
public class SemaforoEV {
	
	/* 
	 * variables
	 */
	/** Semaphore value (with available tokens) */
	protected int value;
	/** Number of waiting threads */
	protected int waitNum = 0;
	/** Max number of available tokens (default=1, binary semaphore = MUTUAL EXCLUSION) */
	protected int maxvalue = 1;
	/** FIFO list for waiting queue 
	 *  Set queue capacity, default = 10 */
	protected Vector codaAttesa = new Vector();
	/** true = FIFO management, false = LIFO management */
	protected boolean isFifo = true;		
	/** Owner of the semaphore, if private */
	protected Thread tit = null;		

	/* Special values
	 * 
	 * Please note: to assign a numeric value of type Long, you need to add
	 * an 'L' character at the end
	 */
	/** Immediate synchronization, immediately executes or skips the execution */
	public static final long IMMEDIATE = 1L;
	/** Wait without timeout */
	public static final long NOTIMEOUT = 0L;
	/** Timeout expired */
	public static final long EXPIRED = 0L;
	/** Timeout not expired */
	public static final long INTIME = 1L;
	
	
	/* 
	 * constructors
	 */
	public SemaforoEV(int v, int max, boolean fifo) {
		value = v;
		maxvalue = max;
		isFifo = fifo;
	}
	public SemaforoEV(int v, int max) {
		this(v,max,true);
	}
	public SemaforoEV(int v) {
		this(v,v);
	}
	public SemaforoEV(boolean b) {	// binary semaphore
		this(b?1:0,1);
	}
	public SemaforoEV() {				// private semaphore
		this(false);
		tit = Thread.currentThread();
		/* the owner is the current thread */
	}
	

	/* 
	 * methods
	 */
	public synchronized void getCoin() {	// REQUEST "TOKEN"
		Thread curTh = Thread.currentThread();
		if (tit != null && tit != curTh) {		// control on the owner
			throw new InvalidThreadException();
		}
		if (value == 0) {
			/* If NO tokens available, stop thread execution and add to queue */
			waitNum++;
			if(isFifo) { 	// add to FIFO queue
				codaAttesa.addElement(new WaitingThread(curTh, false));
			} else {		// add to LIFO head (position 0)
				codaAttesa.insertElementAt(new WaitingThread(curTh, false), 0);
			} //isFifo
			//System.out.println("GET: Put "+curTh.getName()+" in the queue");
			//System.out.println("GET: Number of thread in the queue: "+waitNum);
			while(true) {
				try {
					wait();
					/* wait() call freeze method (and thread) execution in
					 * this point until notifyAll() */
				} catch (InterruptedException ie) {			
				} //catch
				/* if you have to wake up this thread, it is usually the first,
				 * but in the case of LIFO queue, another thread can be placed
				 * in head and be suspended, so you have to look linearly
				 */  
				for (int i=0; i<codaAttesa.size(); i++) {
					// check all threads of the queue
					WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
					if (wt.th == curTh) {
						// if found current thread...
						if (wt.wakenUp) {
							// and it WAS AWAKENED...
							//System.out.println("GET: "+curTh.getName()+" was awakened, remove it from queue");		
							codaAttesa.removeElementAt(i);	
							waitNum--;
							// then semaphore value is already OK
							return;
						} else break;
							// otherwise, if it is NOT already awakened, is re-suspend */
					} //if wt.th
					else;
				} //for
			} //while
		} else {
			value--;
			//System.out.println("GET: I can execute thread "+curTh.getName()+" immediately");
		} //if value
			/* if there are tokens available, then the thread can continue
			 * execution and simply decrements the semaphore
			 */
		//System.out.println("GET: semaphore values is now: "+value);
		//System.out.println("GET: number of thread in the queue: "+waitNum);
	} //getCoin
	
	public synchronized long getCoin(long timeout) {	// REQUEST "TOKENS" with timeout
		if (timeout == NOTIMEOUT) {
			getCoin();			// bring back to normal getCoin
			return INTIME;
		}
		Thread curTh = Thread.currentThread();
		if (tit != null && tit != curTh) {
			throw new InvalidThreadException();
		}
		if (value == 0) {
			/* if NOT tokens available, stop thread execution and add to queue */
			if (timeout == IMMEDIATE) return EXPIRED;	// immediate synchronization NOT possible
			// otherwise wait in queue
			long exp = System.currentTimeMillis() + timeout;	// expiration moment
			waitNum++;
			if(isFifo) { 	// add to FIFO queue
				codaAttesa.addElement(new WaitingThread(curTh, false));
			} else {		// add to LIFO head (position 0)
				codaAttesa.insertElementAt(new WaitingThread(curTh, false), 0);
			}
			//System.out.println("GET: Put "+curTh.getName()+" in the queue");
			//System.out.println("GET: Number of threads in the queue: "+waitNum);
			long diffTime = timeout;	// actual time to wait
			while(true) {
				if (diffTime>0) {
					try {
						// wakes up the thread when the timeout expires
						wait(diffTime);
					} catch (InterruptedException ie) {			
					} //catch
				} //if
				diffTime = exp - System.currentTimeMillis();
				/* if you have to wake up this thread, it is usually the first,
				 * but in the case of LIFO queue, another thread can be placed
				 * in head and be suspended, so you have to look linearly
				 */
				for (int i=0; i<codaAttesa.size(); i++) {
					WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
					if (wt.th == curTh) {
						// if found current thread...
						if (wt.wakenUp) {
							// and it WAS AWAKENED...
							//System.out.println("GET: "+curTh.getName()+" was awakened, remove it from queue");		
							codaAttesa.removeElementAt(i);	
							waitNum--;
						    // then semaphore value is already OK
							return diffTime > 0L ? diffTime : INTIME;	// however >0
						} else if (diffTime <=0) {	// wake up for timeout
							codaAttesa.removeElementAt(i);
							waitNum--;
							/* semaphore vaue is already OK */
							return EXPIRED;			// timeout expired
						} else break;
						// otherwise, if it is NOT already awakened, is re-suspend */
					} //if wt.th
					else;
				} //for
			} //while
		} else {
			value--;			/* decrease semaphore */
			//System.out.println("GET: I can execute thread "+curTh.getName()+" immediately");
		} //if value
		//System.out.println("GET: Semaphore value is now: "+value);
		//System.out.println("GET: Number of threads in the queue is: "+waitNum);
		return timeout;			// not suspensive
	} //getCoin(long timeout)
	
	public synchronized void putCoin() {	// PUT "TOKEN"
		if (value == maxvalue) return;
		if (waitNum > 0) {
			// if there are thread in the queue, try to satisfy them immediately
			//System.out.println("PUT: there are threads in the queue");
			for (int i=0; i<codaAttesa.size(); i++) {
				// linear search in the list
				WaitingThread wt = (WaitingThread)codaAttesa.elementAt(i);
				if (!wt.wakenUp) {
					// if thread not already awakened, then I can awaken
					//System.out.println("PUT: Prepare thread for awaken "+wt.th.getName());
					wt.wakenUp = true;
					// waitNum--; NO, decrease of waitNumis already performed into GET
					break;		// exit for loop
				}
			} //for
			//System.out.println("Notify to all threads");
			notifyAll();
		} else {
			// No threads in the queue, increase only semaphore value
			value++;
			//System.out.println("PUT: no threads in the queue, increase only semaphore value");
		} //waitNum
		//System.out.println("PUT: semaphore value is now: "+value);
		//System.out.println("PUT: Number of threads in the queue: "+waitNum);
	} //putCoin
	
	public synchronized Thread waitingThread(int pos) {
		/* Waiting thread with 'pos' index in the queue */
		if (pos >= codaAttesa.size()) return null;
		return ((WaitingThread)codaAttesa.elementAt(pos)).th;
	}

	public synchronized int getValue() {		/* return number of available tokens */
		return value;
	}
	public synchronized int getQueued() {		/* return number of threads in the queue */
		return waitNum;
	}
	public synchronized int getCapacity() {		/* return queue capacity */
		return codaAttesa.capacity();
	}
	public synchronized String possDa() {		/* return semaphore owner */
		return tit == null ? "nessuno" : tit.getName();
	}

	
	/* 
	 * Internal class 
	 * Waiting thread descriptor
	 */
	protected class WaitingThread {
		
		/* 
		 * variables
		 */		
		Thread th;			// Thread ID
		boolean wakenUp;	// awakened?
		int qty;			// request quantity
		int prio;			/* waiting priority
							 * low values = high priority */
		static final int MIN_PRIO = 10;
		static final int MAX_PRIO = 1;
		
		
		/* 
		 * constructors
		 */
		WaitingThread(Thread t, boolean w, int q, int p) {
			th = t;
			wakenUp = w;
			qty = q;
			prio = p;
		}
		WaitingThread(Thread t, boolean w, int q) {
			this(t,w,q,MIN_PRIO);
		}
		WaitingThread(Thread t, boolean w) {
			this(t,w,1,MIN_PRIO);
		}
	} //WaitingThread
	
} //SemaforoEV

