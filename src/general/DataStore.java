/*	
 * Class 	DataStore
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.util.*;

/**
 * Data container of stack type for position strings, which can be accessed
 * by multiple threads without the risk of collisions.
 * Keeps track of the last GPS position (valid or not),	an indicator
 * of the validity of the last GPS position and last GPS VALID string.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 14-09-2007
 * @author 	alessioza
 * 
 */
public class DataStore extends Stack implements GlobCost {

	/*
	 * local variables 
	 */
	private Object 	lastValidPOS= null;
	private boolean isLastValid = false;
	InfoStato infoS;
	private String dsType;

	
	/*
	 * constructors 
	 */
	public DataStore(String type) {
		//System.out.println("DataStore: CREATED");
		dsType = type;
		infoS = new InfoStato();
	}
	
	
	/*
	 *  methods
	 */

	/**
	 * Synchronized method to replace object at the top of the stack,
	 * if stack is empty simply insert a new object at the top.
	 * If object is valid, update validity indicator and replace the
	 * last valid GPS position with current position.
	 * 
	 * @param	item		object to insert
	 * @param 	isValid		'true' indicates that inserted object is valid, 'false' means not valid
	 * @return	object inserted at the top of the stack
	 * 
	 */
	public synchronized Object replaceObject(Object item, boolean isValid) {
		
		// update validity indicator (true or false)
		isLastValid = isValid;
		
		// update lasta valid string, ONLY if isValid = true
		if (isValid == true) lastValidPOS = item;
		
		// if stack is empty --> add string at the top
		if (this.synEmpty()) {	
			this.synPush(item);
			//System.out.println("Stack is empty, add item: " + item);
		} 
		// If stack not empty --> delete old item and replace with new one
		else { 
			this.synPop();
			this.synPush(item);
			//System.out.println("I get previous item and add item: " + item);
		}
		return item;
	}
	
	/**
	 * Synchronized method to read lasta valid GPS position (valid or not)
	 * saved in DataStore, without get from stack.
	 * Return 'null' if you have not already done the first FIX.
	 * 
	 * @return	object at the top of the stack or 'null'
	 * 
	 */
	public synchronized Object readOnlyObject() {
		return this.synPeek();
	}
	
	/**
	 * Synchronized method to read last valid GPS position saved 
	 * in DataStore, ONLY if is valid and without get from stack.
	 * Return 'null' if last GPS position is not valid
	 * or if you have not already done the first FIX.
	 * 
	 * @return object at the top of the stack or 'null'
	 * 
	 */
	public synchronized Object readOnlyIfObjectIsValid() {
		// return last GPS position, if valid
		if (isLastValid==true) {
			return this.synPeek();
		}
		// if not valid return 'null'
		else return null;
	}
	
	/**
	 * Synchronized method to get last valid GPS position saved
	 * in DataStore.
	 * Return 'null' if you have not already done the first FIX.
	 * 
	 * @return	GPRMC string corresponding to last valid GPS position
	 * 			or 'null' if you have not already done the first FIX
	 * 
	 */
	public synchronized Object getLastValid() {
		/* 
		 * If in current session there was a FIX, then send last valid
		 * GPS position, OTHERWISE SEND POSITION READ FROM FILE
		 */
		if (lastValidPOS!=null) 
			return lastValidPOS;
		else
			return infoS.getInfoFileString(LastGPRMCValid);
				
	}
	
	/**
	 * Synchronized method to insert a new item at the top of the stack
	 * 
	 * @param	item	item to insert
	 * @return	item inserted at the top of the stack
	 * 
	 */
	public synchronized Object synPush (Object item) {
		return this.push(item);
	}
	
	/**
	 * Synchronized method to remove an item from the top of the stack
	 * 
	 * @return	item removed from stack
	 * 
	 */
	public synchronized Object synPop() {
		return this.pop();
	}
	
	/**
	 * Synchronized method to read an item from the top of the stack without remove it
	 * 
	 * @return	item at the top of the stack
	 * 
	 */
	public synchronized Object synPeek() {
		return this.peek();
	}
	
	/**
	 * Synchronized method to verify if stack in empty
	 * 
	 * @return	true only if stack is empty, otherwise false
	 * 
	 */
	public synchronized boolean synEmpty() {
		return this.empty();
	}
	
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
	
}
