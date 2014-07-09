/*	
 * Class 	FlashFile
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.util.*;
import java.io.*;
import javax.microedition.io.Connector;
import com.cinterion.io.file.FileConnection;

/**
 * File management, to store on file system settings.
 * To add a setting to file, just add it inside the constructor.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 21-09-2007
 * @author 	alessioza
 * 
 */
public class FlashFile implements GlobCost {
	
	/*
	 * local variables 
	 */
	/** Number of settings in the file */
	private int			numValues;
	private Vector		settings;
	private Vector		values;
	private String 		letturaFile, scritturaFile;
	/** Can be read max 1024 bytes */
	private	byte 		b[]	= new byte [1024];
	/** Number of bytes read from file */
	private int 		numBytesRead;
	/** Index for read setting values */
	private int			init;
	InfoStato 			infoS;
	
	
	/*
	 * constructors 
	 */
	public FlashFile() {
		//System.out.println("FlashFile: CREATED");
		// vector instances
		settings 	= new Vector();
		values	 	= new Vector();
		// file settings list
		settings.addElement(IDtraker);
		settings.addElement(PasswordCSD);
		settings.addElement(AppName);
		settings.addElement(CloseMode);
		settings.addElement(LastGPSValid);
		settings.addElement(TrackingInterv);
		settings.addElement(Operatore);
		settings.addElement(TrackingType);
		settings.addElement(TrackingProt);
		settings.addElement(Header);
		settings.addElement(Ackn);
		settings.addElement(GprsOnTime);
		settings.addElement(TrkState);
		settings.addElement(PublishTopic);
		settings.addElement(SlpState);
		settings.addElement(StillTime);
		settings.addElement(MovState);
		settings.addElement(IgnState);
		settings.addElement(UartSpeed);
		settings.addElement(UartGateway);
		settings.addElement(UartHeaderRS);
		settings.addElement(UartEndOfMessage);
		settings.addElement(UartAnswerTimeOut);
		settings.addElement(UartNumTent);
		settings.addElement(UartEndOfMessageIP);
		settings.addElement(UartIDdisp);
		settings.addElement(UartTXtimeOut);
		settings.addElement(OrePowerDownOK);
		settings.addElement(MinPowerDownOK);
		settings.addElement(OrePowerDownTOexpired);
		settings.addElement(MinPowerDownTOexpired);
		settings.addElement(DestHost);
		settings.addElement(DestPort);
		settings.addElement(ConnProfileGPRS);
		settings.addElement(apn);
		settings.addElement(GPRSProtocol);
		settings.addElement(TrkIN);
		settings.addElement(TrkOUT);
		settings.addElement(InsensibilitaGPS);
		// ecc...
		
		// calculate number of settings
		numValues = settings.size();
		infoS = new InfoStato();
	}
	
	
	/*
	 *  methods
	 */

	/**
	 * RECOVER SYSTEM SETTINGS from file
	 * 
	 * @return 'OK' if recover is ok, 'FileNotFound' if file not found
	 */
	public synchronized String loadSettings() {
		try {
			// Open connection to file
			FileConnection fconn = (FileConnection)Connector.open("file:///a:/file/" + fileName);
			/*
			 * If configuration file not exists, load it to flash and
			 * application ends 
			 */
			if (!fconn.exists()) {
				//fconn.create();				// to create file
				//fconn.setReadable(true);		// to set file readable
				System.out.println("FlashFile: Missing configuration file\r\nApplication will be closed");
				return "FileNotFound";
			}
			
			// Read file
			InputStream is = fconn.openInputStream();
			numBytesRead = is.read(b, 0, 1024);		
			
			// Convert string
			letturaFile = new String(b, 0, numBytesRead); 
			//System.out.println("FlashFile: file content "+fileName+":\r\n"+ letturaFile);
			
			/*
			 * Extract settings in a string array, pay attention to
			 * '=' symbol management
			 */
			init = letturaFile.indexOf(settings.elementAt(0)+"=");
			letturaFile = letturaFile.substring(init);
			for (int i=0; i<numValues-1; i++) {
				init = letturaFile.indexOf(settings.elementAt(i)+"=") + (settings.elementAt(i)+"=").length();
				values.addElement(letturaFile.substring(init, letturaFile.indexOf("\r\n")));
				letturaFile = letturaFile.substring(init + ((String)values.elementAt(i)).length() + 2);
				//System.out.println("letturaFile: "+letturaFile);
			}
			// Last cycle
			init = letturaFile.indexOf(settings.elementAt(numValues-1)+"=") + (settings.elementAt(numValues-1)+"=").length();
			values.addElement(letturaFile.substring(init, letturaFile.indexOf("\r\n###")));
			
			// Print settings
			/*for (int i=0; i<numValues; i++) {
				System.out.println("FlashFile: Setting n. "+i+": " + values.elementAt(i));
			}
			*/
			// Close connection to file
			is.close();
			fconn.close();
			
		} catch (IOException ioe) {
			//System.out.println("exception: " + ioe.getMessage());
			//ioe.printStackTrace();
		} //catch
		
		return "OK";
	}
	
	/**
	 * SAVE SYSTEM SETTINGS to FILE
	 */
	public synchronized String writeSettings() {
		try {
			// Open connection to file
			FileConnection fconn = (FileConnection)Connector.open("file:///a:/file/" + fileName);
			
			/* 
			 * configuration file must exists, otherwise application
			 * not arrive at this point 
			 */

			/*
			 * Reconvert strings array into a unique string,
			 * pay attention to '=' symbol management
			 */
			scritturaFile = "Greenwich settings\r\n###\r\n";
			for (int i=0; i<numValues; i++) {
				scritturaFile = scritturaFile + settings.elementAt(i) + "=" + values.elementAt(i) + "\r\n";
			}
			// Last cycle
			scritturaFile = scritturaFile + "###";
			
			// Verify
			//System.out.println("FlashFile: I'm writing to file:\r\n" + scritturaFile);
			
			// Write to file
			DataOutputStream dos = fconn.openDataOutputStream();
			dos.write(scritturaFile.getBytes());	
			
			// Close connection to file
			dos.flush();
			dos.close();
			fconn.close();
			
		} catch (Exception ioe) {
			//System.out.println("exception: " + ioe.getMessage());
			new LogError("File: " + ioe.getMessage());
			//ioe.printStackTrace();
		} //catch
		
		return "OK";
	}
	
	/**
	 * To set a system setting to read from FILE
	 * 
	 * @param	impost	setting alias
	 * @param	val		setting value 
	 */
	public synchronized void setImpostazione(String impost, String val) {
		/*
		 * Identify setting and set value
		 */
		for (int i=0; i<numValues; i++) {
			if (impost.equalsIgnoreCase((String)settings.elementAt(i))) {
				//System.out.println("size=" + values.size() +", i=" +i+", val=" + val);
				values.setElementAt(val,i);
				//System.out.println("FlashFile: setting " + settings.elementAt(i) + " modified at value " + val);
			} //if
		} //for
	} //setImpostazione
	
	/**
	 * To get a system setting to write to FILE
	 * 
	 * @param	impost	setting alias
	 * @return	value   setting value
	 */
	public synchronized String getImpostazione(String impost) {
		/*
		 * identify setting and return current value
		 */
		for (int i=0; i<numValues; i++) {
			if (impost.equalsIgnoreCase((String)settings.elementAt(i))) {
				//System.out.println("FlashFile: get setting " + settings.elementAt(i) + ", with value " + values.elementAt(i));
				return (String)values.elementAt(i);
			} //if
		}  //for
		return null;
	} //getImpostazione
	
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
	
} //FlashFile

