/*	
 * Class 	Mailbox
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Producer/consumer model on multiple buffer, mailbox as monitor,
 * protection is related to the inclusion of entry methods
 * 
 * @version	1.00 <BR> <i>Last update</i>: 01-08-2007
 * @author 	alessioza
 * 
 */
public class Mailbox extends Monitor {

	/* 
	 * variables
	 */
	private Condition spaceAval = new Condition();		// available space
	private Condition dataAval = new Condition();		// available data
	private Object data[];								// data buffer
	private int numEl, numData=0, head=0, tail=0;
		// item number, data number, where to read, where to write
	
	
	/* 
	 * constructors
	 */
	public Mailbox(int n) {		// constructor with n items buffer
		numEl = n;
		data = new Object[numEl];
	}
	
	
	/* 
	 * methods
	 */
	public Object read() {
		mEnter();	// prologue
		if (numData==0) dataAval.cWait();	// if no data
		Object ret = data[head];
		data[head] = null;
		head = (++head) % numEl;
		numData--;
		spaceAval.cSignal();	// notify available space
		mExit();	// epilogue
		return ret;
	} //read
	
	public void write (Object d) {
		mEnter();	// prologue
		if (numData == numEl) spaceAval.cWait();	// non c'è spazio
		data[tail] = d;
		tail = (++tail) % numEl;
		numData++;
		dataAval.cSignal();		// notify available data
		mExit();	// epilogue
	} //write
	
	public int numMsg() {		// number of messages in the Mailbox
		return  numData;
	} //size
	
	public int dimens() {		// Mailbox size (number of messages)
		return  numEl;
	} //dimens
	
} //Mailbox

