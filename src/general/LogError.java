/*	
 * Class 	LogError
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;
import java.util.*;
import javax.microedition.io.Connector;
import com.cinterion.io.file.FileConnection;

/**
 * Save important events on a log file
 * 
 * @version	1.06 <BR> <i>Last update</i>: 07-05-2008
 * @author 	matteobo
 * 
 */
public class LogError implements GlobCost{

	
	/*
	 * local variables 
	 */
	String fileLog = "log.txt";
	String fileOLD = "logOLD.txt";
	
		
	public LogError(String error) {
		
		try{
			while(!InfoStato.getLogSemaphore()){Thread.sleep(1);}
		}catch(InterruptedException e){}
		writeError(error);
		InfoStato.freeLogSemaphore();
	}

	
	
	/* 
	 * Method run
	 */
	
	
	/**
	 * Method to SAVE SYSTEM SETTINGS on FILE
	 *
	 * @return	
	 */
	public synchronized String writeError(String error) {
		
		//System.out.println("FreeMem: " + Runtime.getRuntime().freeMemory());
        //System.out.println("TotalMem: " + Runtime.getRuntime().totalMemory());
       
        try {
        	if(Runtime.getRuntime().freeMemory() > 200){
	            FileConnection fconn = (FileConnection) Connector.open("file:///a:/log/" + fileLog);
	            // If no exception is thrown, then the URI is valid, but the file
	            // may or may not exist.
	            if (!fconn.exists()) {
	                fconn.create();   // create the file if it doesn't exist
	            }
	            
	            DataOutputStream dos = fconn.openDataOutputStream();
	            dos.write(("Versione software: " + revNumber + "\r\n").getBytes());
	            dos.flush();
	            dos.close();
	            //append writing
	            fconn = (FileConnection) Connector.open("file:///a:/log/" + fileLog);
	            fconn.setReadable(true);
	            OutputStream os;
	            //System.out.println(fconn.fileSize());
	            os = fconn.openOutputStream(fconn.fileSize());
	            Calendar cal = Calendar.getInstance();
	            os.write((cal.getTime() + " - " + error + "\r\n").getBytes());
	            //os.write((error + "\r\n").getBytes());
	            os.flush();
	            os.close();

	            if(fconn.fileSize() > 20000){	
	            	FileConnection fconn1 = (FileConnection) Connector.open("file:///a:/log/" + fileOLD);
	            	if (fconn1.exists()) {
	            		fconn1.delete();
	            		fconn1.close();
	            	}
	            	fconn.rename(fileOLD);
	            }
	            //System.out.println(fconn.getName());
	            fconn.close();
        	}
			
		} catch (IOException ioe) {
			//System.out.println("exception: " + ioe.getMessage());
			//ioe.printStackTrace();
		} catch (SecurityException e){}
		
		
		return "OK";
	}


}
