/*	
 * Class 	ATListenerCustom
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Extension of GlobCost interface, with methods to interface
 * a class with status info and configuration file 
 * 
 * @version	1.00 <BR> <i>Last update</i>: 27-08-2007
 * @author 	alessioza
 * 
 */
public class ATListenerCustom implements GlobCost {
	
	/* 
	 * local variables
	 */
	InfoStato	infoS = new InfoStato();
	FlashFile file 	= new FlashFile();
	
	/**
	 *  Add reference to InfoStato data structure
	 *  
	 *  @param	is	InfoStato object
	 *  @return "OK, infoS"
	 */
	public synchronized String addInfoStato(InfoStato is) {	
		infoS = is;
		return "OK,infoS";
	} //addInfoStato
	
	/**
	 *  Add reference to FlashFile data structure
	 *  
	 *  @param	is	FlashFile object
	 *  @return "OK, file"
	 */
	public synchronized String addFile(FlashFile is) {	
		file = is;
		return "OK,file";
	} //addFile
		
} //ATListenerCustom

