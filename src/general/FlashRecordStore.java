/*	
 * Class 	FlashRecordStore
 * 
 * Version	prototipo
 * 
 * Date		29/08/2007
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreFullException;
import javax.microedition.rms.RecordStoreNotOpenException;

/**
 * Manager of data to store in flash memory between an
 * application execution and another.
 * 
 * @version	29-08-2007
 * @author 	alessioza
 * 
 */
public class FlashRecordStore implements GlobCost {
	
	/*
	 * local variables 
	 */
	
	
	/*
	 * constructors 
	 */
	
	public FlashRecordStore() {
		//System.out.println("FlashRecordStore: CREATED");
	}
	
	
	/*
	 *  methods
	 */

	/**
	 * Creates a RecordStore on FLASH, after first execution
	 * of the application.
	 *
	 * @return	OK
	 * 
	 */
	public synchronized String createSettings() {
		return "OK";
	}
	
	/**
	 * Recover data from RecordStore (FLASH)
	 *
	 * @return	OK
	 * 
	 */
	public synchronized String loadSettings() {
		return "OK";
	}
	
	/**
	 * Store data to RecordStore (FLASH)
	 *
	 * @return	OK
	 * 
	 */
	public synchronized String writeSettings() {
		return "OK";
	}
	
	/**
	 * Read a record from RecordStore
	 * @param	recordID	ID of record to read
	 * @return	s			string read from RecordStore
	 * 
	 */
	public synchronized String readRecord(int recordID) {
		// read a string
		String s = "";
		byte[] b = new byte[100];	// 100 byte
		try {
			RecordStore rs = RecordStore.openRecordStore(recordStoreName, true);
			if (rs != null) {
				rs.getRecord(recordID, b, 0);
				s = new String(b);
				rs.closeRecordStore();
			}
		} catch (RecordStoreNotOpenException rsnoe) {
		} catch (RecordStoreFullException rsfe) {
		} catch (RecordStoreException rse) {}
		return s;
	} //readRecord
	
	/**
	 * Write a record to RecordStore
	 * @param	s			string read from RecordStore
	 * @param   recordID	ID to be used to write record	
	 * @return	recordID	ID used to write record
	 * 
	 */
	public synchronized int writeRecord(String s, int recordID) {
		// write a string
		byte[] b = s.getBytes();
		try {
			RecordStore rs = RecordStore.openRecordStore(recordStoreName, true);
			if (rs != null) {
				// public int addRecord(byte[] data, int offset, int numBytes)
				rs.deleteRecord(recordID);
				recordID = rs.addRecord(b, 0, b.length);
				rs.closeRecordStore();
			}
		} catch (RecordStoreNotOpenException rsnoe) {
		} catch (RecordStoreFullException rsfe) {
		} catch (RecordStoreException rse) {}
		return recordID;
	} //writeRecord
	
} //FlashRecordStore
