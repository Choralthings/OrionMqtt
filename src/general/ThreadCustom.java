/*	
 * Class 	ThreadCustom
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Extension of Thread class, that contains some additional methods frequently
 * used in the thread for the MIDlets.
 * 
 * @version	1.01 <BR> <i>Last update</i>: 08-10-2007
 * @author 	alessioza
 * 
 */
public class ThreadCustom extends Thread implements GlobCost{
	
	/* 
	 * local variables
	 */
	DataStore 	dsDataGGA, dsDataRMC, dsTrkRMC, dsTrkGGA;
	int 		numThread;
	boolean 	isRicevitore;
	Mailbox 	mboxMAIN;
	Mailbox 	mbox1,mbox2,mbox3,mbox4,mbox5,mbox6,mbox7,mbox8,mbox9,mbox10;
	PrioSem 	semAT;
	InfoStato	infoS = new InfoStato();
	FlashFile 	file = new FlashFile();
	protected String	checksum;
	
	
	/*
	 * methods
	 */
	
	/**
	 *  Pass to thread a DataStore object
	 *  
	 *  @param	st      DataStore object
	 *  @param	dsType  datastore type
	 *  @return "OK,dsDataCHORAL" or "OK,dsDataNMEA"
	 */
	public synchronized String addDataStore(DataStore st, String dsType) {

		if (dsType.equalsIgnoreCase(dsDRMC)) {
			dsDataRMC = new DataStore(dsDRMC);
			dsDataRMC = st;
			return "OK,dsDataRMC";
		} else if (dsType.equalsIgnoreCase(dsDGGA)) {
			dsDataGGA = new DataStore(dsDGGA);
			dsDataGGA = st;
			return "OK,dsDataGGA";
		} else if (dsType.equalsIgnoreCase(dsTRMC)) {
			dsTrkRMC = new DataStore(dsTRMC);
			dsTrkRMC = st;
			return "OK,dsTrkRMC";
		} else if (dsType.equalsIgnoreCase(dsTGGA)) {
			dsTrkGGA = new DataStore(dsTGGA);
			dsTrkGGA = st;
			return "OK,dsNORM";
		}
		else return "Error";
		
	} //addDataStore
	
	/**
	 *  Pass to thread a PrioSem object
	 *  
	 *  @param	ps	PrioSem object
	 *  @param	nPS	type of priority semaphore
	 *  @param	nth thread number
	 *  @return "OK,semAT"
	 */
	public synchronized String addPrioSem(PrioSem ps, int nPS, int nth) {	
		switch (nPS) {
			case 1: {
				/* 
				 * init semaphore to 0, so initially the common resource is
				 * NOT available 
				 */
				semAT = new PrioSem(0);
				semAT = ps;
				numThread = nth;
				return "OK,semAT";
			}
			default: return "Error";
		} //switch		
	} //addPrioSem
	
	/**
	 *  Add reference to InfoStato data structure
	 *  
	 *  @param	is	InfoStato object
	 *  @return "OK,infoS"
	 */
	public synchronized String addInfoStato(InfoStato is) {	
		infoS = is;
		return "OK,infoS";
	} //addInfoStato
	
	/**
	 *  Add reference to FlashFile data structure
     *  
	 *  @param	ff	FlashFile object
	 *  @return "OK,FlashFile"
	 */
	public synchronized String addFlashFile(FlashFile ff) {	
		file = ff;
		return "OK,FlashFile";
	} //addFlashFile
	
	/**
	 *  Pass to a thread a Mailbox object
	 *  
	 *  @param	mb		Mailbox object
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
			case 1: {
				mbox1 = new Mailbox(20);
				mbox1 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox1";
			}
			case 2: {
				mbox2 = new Mailbox(20);
				mbox2 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox2";
			}
			case 3: {
				mbox3 = new Mailbox(20);
				mbox3 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox3";
			}
			case 4: {
				mbox4 = new Mailbox(20);
				mbox4 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox4";
			}
			case 5: {
				mbox5 = new Mailbox(20);
				mbox5 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox5";
			}
			case 6: {
				mbox6 = new Mailbox(20);
				mbox6 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox6";
			}
			case 7: {
				mbox7 = new Mailbox(20);
				mbox7 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox7";
			}
			case 8: {
				mbox8 = new Mailbox(20);
				mbox8 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox8";
			}
			case 9: {
				mbox9 = new Mailbox(20);
				mbox9 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox9";
			}
			case 10: {
				mbox10 = new Mailbox(20);
				mbox10 = mb;
				numThread = nth;
				isRicevitore = isRcv;
				return "OK,mbox10";
			}
			default: return "Error";
		} //switch		
	} //addMailbox
	
	/**
	 * For ckecksum calculation on a string
	 * 
	 * @param	sentence	string on which calculate checksum
	 * @return	checksum
	 */
	public String getChecksum(String sentence) {

		try{
			int[] intSentence = new int[sentence.length()];
			intSentence[0] = sentence.charAt(0);
			
			for (int i = 1; i < sentence.length() ; i++){
				intSentence[i] = intSentence[i-1] ^ sentence.charAt(i);
			}
			
			checksum = Integer.toHexString(intSentence[sentence.length()-1]);
			if(checksum.length() < 2) return "0" + checksum;
			else return checksum;
		}catch(IndexOutOfBoundsException e){
			return "00";
		}
	}
	
} //ThreadCustom

