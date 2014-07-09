/*	
 * Class 	SaveData
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;

import javax.microedition.io.Connector;
import com.cinterion.io.file.FileConnection;

/**
 * Save tracking data
 * 
 * @version	1.11 <BR> <i>Last update</i>: 06-04-2012
 * @author 	matteobo
 * 
 */
public class SaveData implements GlobCost{

	
	/*
	 * local variables 
	 */
	String fileStore = "store.txt";
	InfoStato infoS;
	int i = 0;
	int z = 0;
	/** Can be read a maximum of 1024 bytes */
	private	byte 		b[];
	/** Number of bytes read from file */
	private int 		numBytesRead;
	private String 		letturaFile, datoAttuale;
	private int			init;
		
	public SaveData() {
		
		infoS = new InfoStato();
	}

	
	
	/* 
	 * Method run
	 */
	
	
	/**
	 * Method to store tracking queue to FILE
	 *
	 * @return	
	 */
	public synchronized String writeLog() {
		
		try {
        	if(Runtime.getRuntime().freeMemory() > 200){
	            FileConnection fconn = (FileConnection) Connector.open("file:///a:/store/" + fileStore);

	            if (fconn.exists())	fconn.delete();
	            fconn.create();
	            
	            OutputStream os;
	            os = fconn.openOutputStream(fconn.fileSize());
	            
	            while(!InfoStato.getCoda()){Thread.sleep(1);}
				for(i = 0; i<codaSize; i++)
		            os.write((infoS.getRecord(i) + "\r\n").getBytes());
				InfoStato.freeCoda();
	            os.flush();
	            os.close();
	            fconn.close();
        	}
			
		} catch (IOException ioe) {
		} catch (SecurityException e){
		} catch (InterruptedException e){}
		
		
		return "OK";
	}

	public synchronized boolean loadLog() {
		boolean ret = false;
		try {
			// Open connection to file
			FileConnection fconn = (FileConnection)Connector.open("file:///a:/store/" + fileStore);
			/*
			 * If the configuration file does not exist you have to upload it
			 * to flash, the application ends notifying the lack
			 */
			if(debug){
				System.out.println("EXECUTE");
			}
			if (!fconn.exists()) {
				ret = false;
			}
			ret = true;
			
			// Read file
			InputStream is = fconn.openInputStream();
			numBytesRead = is.available();
			b = new byte[numBytesRead];
			numBytesRead = is.read(b, 0, numBytesRead);		
			
			// Convert to string
			letturaFile = new String(b, 0, numBytesRead); 
			//System.out.println("FlashFile: file content "+fileStore+":\r\n"+ letturaFile);
			
			for (z=0; z<codaSize; z++) {
				init = letturaFile.indexOf("\r\n");
				if(init == -1){
					ret = false;
					break;
				}
				//System.out.println(init);
				datoAttuale=letturaFile.substring(0,init);
				//System.out.println(datoAttuale);
				infoS.saveRecord(z, datoAttuale);
				if(z<(codaSize-1))
					letturaFile = letturaFile.substring(init+2);
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//System.out.println(z + " " + infoS.getRecord(z));
			}
			
			// Close connection to file
			is.close();
			fconn.close();
			
		} catch (Exception ioe) {
		} //catch
		return ret;
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
