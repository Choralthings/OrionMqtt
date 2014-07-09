/*	
 * Class 	CommGPStrasparent
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import java.io.*;
import java.util.*;

import javax.microedition.io.*;
import choral.io.*;

/**
 * Thread for acquisition of the GPS positions in 'TRANSPARENT MODE'.
 * Checks for a valid FIX, build the string to send through GPRS 
 * and save it in a DataStore structure.
 * 
 * @version	1.00 <BR> <i>Last update</i>: 14-08-2008
 * @author 	matteoBo
 * 
 */
public class CommGPStrasparent extends ThreadCustom implements GlobCost {
	
	private static final int[] PWRON = {181,98,6,17,2,0,0,0,25,129,181,98,6,9,13,0,0,0,0,0,255,255,0,0,0,0,0,0,7,33,175};
	private static final int[] PWRLOW = {181,98,6,17,2,0,0,1,26,130,181,98,6,9,13,0,0,0,0,0,255,255,0,0,0,0,0,0,7,33,175};
	private static final int[] PM0sec = {181,98,6,50,24,0,0,6,0,0,4,144,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,234,232};
	/* 
	 * local variables
	 */
	private double DFSSpeed = 0.0;
	private String[] arrayRMC;
	private String	letturaStringa, RMCstring,  SpliRMCstring;
	private int 	virgIndex;
	
	/** is the first time that I report a FIX to AppMain? */
	private boolean	isFirstFIX = true;
	/** is the first time that I report a 'FIXtimeout EXPIRED' to AppMain? */
	private boolean pre_gps_state = true;
//	private boolean	isFirstEXPIRED = true;
	InputStream 	GPSread;
	OutputStream    outStreamgps;
	boolean trk = false;
	boolean go = false;
	// old latitude and longitude
	double vlat = 0;
	double vlon = 0;
	double vx = 0;
	double vy = 0;
	double vz = 0;
	// current latitude and longitude
	double nlat = 0;
	double nlon = 0;
	double nx = 0;
	double ny = 0;
	double nz = 0;	
	// distance
	double tmpDistance = 0;
	int gpsReset = 0;
	int timer1 = 0;
	
	/*
	 * INHERITED RESOURCES from ThreadCustom and passed to AppMain 
	 *
	 * semaphore 'semAT', for the exclusive use of the AT resource
	 * InfoStato 'infoS', status informations about application
	 * Mailbox   'mboxMAIN', to send msg to AppMain
	 * Mailbox   'mbox1', to receive msg with this thread
	 * Mailbox   'mbox2', to send msg to th2 (ATsender)
	 * DataStore 'dsDataCHORAL', to store the strings in CHORAL format 
	 * DataStore 'dsDataNMEA', to store the strings in NMEA format
	 */

	
	/* 
	 * constructor
	 */
	public CommGPStrasparent() {
	}
	
	
	/* 
	 * methods
	 */
	public void run() {
		//System.out.println("Th*CommGPStrasparent: STARTED");
		/* 
         * First you need to open the GPS driver in Transparent Mode,
		 * as soon as AT resource is available.
		 * 
		 * Request use of AT interface (with max priority) 
		 * In this case you must also release the AT resource because
		 * the class 'ATsender' fails to do so, as the purpose
		 * of reserve the resource is to avoid any conflicts
		 */
		while (true){
			
				//System.out.println("Open GPS driver in Transparent Mode...");
				/* 
				 * Calling method Connection.open with port ID "gps0"
				 * Transparent Mode is activated without AT command "at^sgpss=1,1"
				 * so you still need to get exclusive use of the AT resource,
				 * because Connection.open method use it. 
				 */
				try {
					CommConnection connGPS = (CommConnection)Connector.open(COMgps);
				    /* Open input channels */
					GPSread = connGPS.openInputStream();
				    outStreamgps = connGPS.openOutputStream();
				    //System.out.println("Th*CommGPStrasparent: InputStream OPENED");
				    /* 
				     * Enter any safety time to occupy the AT resource or wait for
				     * operation is completed successfully using a special Listener
				     */			    
				} catch(IOException e) {
					//System.out.println("Th*CommGPStrasparent: IOException");
				} //catch			    
			
			try {
			     
				init_GPS(outStreamgps);
				init_GPS(outStreamgps);
			    
				/* 
				 * Endless LOOP,
				 * CONTINUOUS listening on the serial 'gps0'
				 */		
				//System.out.println("Th*CommGPStrasparent: begin read loop from 'gps0'...");
				
				char c = 0;
				int next;
				int count = 0;
				int leggo = 0;
				while(true) {
					
						infoS.setGPSLive(true);
						
						//System.out.println("infoS.getGpsState() " + infoS.getGpsState());
						//System.out.println("pre_gps_state " + pre_gps_state);
						if((infoS.getInfoFileString(MovState)).indexOf("GPSOFF")>=0){
							if(pre_gps_state){
								sleep_GPS(outStreamgps);
								pre_gps_state = false;
								System.out.println("GPS sleep_GPS");
							}
						}else if(!pre_gps_state){
							init_GPS(outStreamgps);
							init_GPS(outStreamgps);
							pre_gps_state = true;
							//System.out.println("GPS init_GPS");
						}
						// Read from serial, until '\r'
				    	letturaStringa = "";
				    	leggo = GPSread.available();

				    	if(leggo>0 && pre_gps_state){
							//next = GPSread.read();
				    		next = -1;
							try{
					    	while(next != '\n') {
					    		leggo = GPSread.available();

						    	if(leggo>0){
						    		next = GPSread.read();
						    		c = (char)next;
						    		letturaStringa = letturaStringa + c;
						    	}
					    	}
							}catch(Exception e){
								e.printStackTrace();
							}
				    	//System.out.println("****" + letturaStringa);
				    	//System.out.flush();				    	
				    	
				    	/*
				    	 * STRING $GPRMC
				    	 */
				    	if (letturaStringa.indexOf("$GPRMC") >= 0) {
				    		
				    		// RMC String: $GPRMC,hhmmss,status,latitude,N,longitude,E,spd,cog,ddmmyy,mv,mvE,mode*cs<CR><LF>
				    		RMCstring = letturaStringa;
				    		if(infoS.getUartTraspGPS()){
				    			System.out.println(RMCstring);
				    			go = true;
				    		}
				    		else go = false;
    		
				    		
				    	} //tipoStringa $GPRMC
				    	
				    	
				    	/*
				    	 * STRING $GPGGA
				    	 */
				    	else{
					    	
				    		if (letturaStringa.indexOf("$GPGGA") >= 0) {
				    	
					    		count++;
					    		
					    		if(go)
					    			System.out.println(letturaStringa + "\r\n");
					    		if(infoS.getCSDTraspGPS()){
					    			infoS.setRMCTrasp(RMCstring);
					    			infoS.setRMCTrasp(letturaStringa + "\r\n");
					    		}

					    		if(true){

					    			//DFS save detected speed in this moment, to know if transmit
					    			int IndiceVirgola;
					    			IndiceVirgola = 0;
					    			arrayRMC = new String[13];
					    			
					    			SpliRMCstring=RMCstring;	
					    			
					    			try{
					    				for (int iii=0; iii<arrayRMC.length-2; iii++) {
					    			
						    				IndiceVirgola = SpliRMCstring.indexOf(","); 
						    				
						    				if (IndiceVirgola>0) {
						    					arrayRMC[iii] = SpliRMCstring.substring(0,IndiceVirgola);
						    				} 
						    				else {
						    					arrayRMC[iii] = vuota;	// if the field is empty I put an empty string
						    				}
						    				SpliRMCstring = SpliRMCstring.substring(IndiceVirgola+1);
						    				
						    				switch (iii){
							    				case 1:
							    					if(SpliRMCstring.indexOf("A")>=0){
							    						infoS.setGpsLed(true);
							    					}
							    					else
							    						infoS.setGpsLed(false);
							    					break;
						    						    								    				
							    				case 6:
							    					try{					    					
								    					// Speed
							    						if(debug)
							    							System.out.println("Velocità precedente: " + DFSSpeed);
							    						infoS.setPreSpeedDFS(DFSSpeed);
								    					DFSSpeed = Double.parseDouble(SpliRMCstring.substring(0, SpliRMCstring.indexOf(",")));
								    					gpsReset = 0;
							    					}catch(NumberFormatException e){
							    						//System.out.println("Th*CommGPStrasparent-switch-iii: NumberFormatException");
							    						new LogError("Th*CommGPStrasparent-switch-iii: NumberFormatException, DFSSpeed: " + DFSSpeed + "\r\nRMC "+RMCstring);
							    						DFSSpeed = 0;
							    						gpsReset++;
							    					}
								    					//new LogError ("Set speed variable " + DFSSpeed);
								    					infoS.setSpeedDFS(DFSSpeed);
								    				break;
							    				default:break;
						    				}
						    				
						    				
						    			}
					    			
					    			}catch(Exception e){
					    				//new LogError("Th*CommGPStrasparent-switch-iii2: NumberFormatException2");
					    				gpsReset++;
			    						DFSSpeed = 0;
					    			}
					    			
					    			nlat = getLatitude(RMCstring);
						    		nlon = getLongitude(RMCstring);
						    		if(nlat != 0 && nlon != 0){
							    		//distance = getDistance(nlat, vlat, nlon, vlon);
							    		tmpDistance = getDistance(nlat, nlon, vx, vy, vz, DFSSpeed);
							    		if(!isFirstFIX){
								    		if(((int)(infoS.getDist() + tmpDistance)) >= 1e6){
								                infoS.setDist(0);
											}
								    		else
								    			infoS.setDist((infoS.getDist() + tmpDistance));
							    		}
							    		isFirstFIX = false;
							    		vx = nx;
							    		vy = ny;
							    		vz = nz;
						    		}
						    		
					    			// Restore GPRMC string into stack
						    		dsDataRMC.replaceObject(RMCstring, true);
						    		dsDataGGA.replaceObject(letturaStringa, true);
						    			
						    		// Send msg to AppMain (only first time)
						    		if (isFirstFIX==true) {
						    			mboxMAIN.write(msgFIX);
						    			infoS.setValidFIX(true);
						    			isFirstFIX = false;		// because it does not happen again
						    		}
						    		else{
						    			
						    			infoS.setValidFIX(true);
							    		// Sending communication to AppMain
										if((!infoS.getInfoFileString(TrackingType).equalsIgnoreCase("SMS")) && (count >= infoS.getInfoFileInt(TrackingInterv))){
											//System.out.println("SAVED STRING: " + stringaGPS + " " + count);
											dsTrkRMC.replaceObject(RMCstring, true);
								    		dsTrkGGA.replaceObject(letturaStringa, true);
											mboxMAIN.write(msgFIXgprs);
											count = 0;
										}
							    	}
						    	} 
						    		
						    	else {	
							
						    		dsDataRMC.replaceObject(RMCstring, false);
						    		dsDataGGA.replaceObject(letturaStringa, false);
									// Sending communication to AppMain
								    if((!infoS.getInfoFileString(TrackingType).equalsIgnoreCase("SMS")) && (count >= infoS.getInfoFileInt(TrackingInterv))){
								    	dsTrkRMC.replaceObject(RMCstring, false);
								    	dsTrkGGA.replaceObject(letturaStringa, false);
								    	mboxMAIN.write(msgFIXgprs);
								    	count = 0;
									}
					    		}
							}
				    	} //tipoStringa $GPGGA
				   	} // fine available if
				   	else{
				   		//System.out.println("NO GPS");
				   	}
				    	
				   	// variable for task verification
				   	timer1++;
					infoS.setTask1Timer(timer1);
					infoS.setTickTask1WD();
				    
				   	Thread.sleep(100);					
				} //while
			} catch(IOException ioe) {
				//System.out.println("Th*CommGPStrasparent-while: IOException");
				new LogError("Th*CommGPStrasparent-while: IOException");
			} catch(StringIndexOutOfBoundsException ioe) {
				//System.out.println("Th*CommGPStrasparent-while: StringIndexOutOfBoundsException");
				new LogError("Th*CommGPStrasparent-while: St0ringIndexOutOfBoundsException");
			} catch (EmptyStackException ese) {
				//System.out.println("Th*CommGPStrasparent-while: EmptyStackException");
				new LogError("Th*CommGPStrasparent-while: EmptyStackException");
			} catch (InterruptedException ese) {
				//System.out.println("Th*CommGPStrasparent-while: InterruptedException");
				new LogError("Th*CommGPStrasparent-while: InterruptedException");
			} catch(Exception e){
				//System.out.println("Th*CommGPStrasparent-while: Exception");
				new LogError("Th*CommGPStrasparent-while: Exception");
			}
			//catch	
			new LogError("GPS Reboot");	

		} // first while used for anomale GPS crash
	
	} //run
	
	public String returnchecksum(String word){
		
		String formatString = word;
		String hex = "";
		
		int[] ris = new int[formatString.length()];
		ris[0] = formatString.charAt(0);
		
		for (int i = 1; i < formatString.length() ; i++){
			ris[i] = ris[i-1] ^ formatString.charAt(i);
		}
		
		
		hex = Integer.toHexString(ris[formatString.length()-1]);
		if(hex.length() < 2)
			hex = "0" + hex;
		return hex.toUpperCase();
		
	}
	
	public boolean get_validFix(String gps_rmc){
	
		String tmp = "";
		/*
		 * 	
		 *  Fields of interest:
		 *  - index n.2	->	validity of the string	[2]
		 */
				
		virgIndex = 0;
		for (int i=0; i<3; i++) { /* da 0 a 3 */
			virgIndex = gps_rmc.indexOf(","); /* =6 */
			
			if (virgIndex>0) {
				tmp = gps_rmc.substring(0,virgIndex);
			} 
			else {
				tmp = vuota;	// if the field is empty I put an empty string
			}
			gps_rmc = gps_rmc.substring(virgIndex+1);
			if(i == 2){
				// GPS Valid data
				if (tmp.equalsIgnoreCase("A")) {
					return true; 
				}
				return false;
			}
		}
		return false;
		
	}
	
	public double getLatitude(String stringa){
	// RMC String: $GPRMC,hhmmss,status,latitude,N,longitude,E,spd,cog,ddmmyy,mv,mvE,mode*cs<CR><LF> 4540.14472,N,01155.85207
		try{
			String lat1 = stringa.substring(19,29);
			String latD = lat1.substring(0,2);
			String latM = lat1.substring(2);
			int a = Integer.parseInt(latD);
			double b = Double.parseDouble(latM);
			b=b/60;
			return a+b;
		}catch(IndexOutOfBoundsException e){
			
		}catch(NumberFormatException e){
			
		}
		return 0;	
		
	}
	public double getLongitude(String stringa){
	// RMC String: $GPRMC,hhmmss,status,latitude,N,longitude,E,spd,cog,ddmmyy,mv,mvE,mode*cs<CR><LF>	
		try{
			String lon1 = stringa.substring(32,43);
			String lonD = lon1.substring(0,3);
			String lonM = lon1.substring(3);
			int a = Integer.parseInt(lonD);
			double b = Double.parseDouble(lonM);
			b=b/60;
			return a+b;
		}catch(IndexOutOfBoundsException e){
			
		}catch(NumberFormatException e){
			
		}
		return 0;
	}
	
	/*// WGS84 ellipsoid constants:
    #define PI  3.14159265358979l
    #define A   6378137l
    #define E   8.1819190842622e-2
    #define DEG2RAD(x)  (float)(x*PI/180)
	#define ALTIT	250

    float lat = DEG2RAD(pos->latitude);
    float lon = DEG2RAD(pos->longitude);

    // intermediate calculation
    // (prime vertical radius of curvature)
    float N = A / sqrt(1 - pow(E, 2) * pow(sin(lat), 2));

	pos->x = (N+ALTIT) * cos(lat) * cos(lon);
    pos->y = (N+ALTIT) * cos(lat) * sin(lon);
    pos->z = ((1-pow(E, 2)) * N + ALTIT) * sin(lat);
		
	*/
	public double getDistance(double newLat, double newLon, double oldX, double oldY, double oldZ, double Vel ){
		
		double lat = newLat*Math.PI/180;
		double lon = newLon*Math.PI/180;
		
		int A = 6378137;
		double E = 8.1819190842622e-2;
		int ALTIT = 250;
		
		double N = A/Math.sqrt(1-E*E*Math.sin(lat)*Math.sin(lat));
		
		nx = (N+ALTIT)*Math.cos(lat)*Math.cos(lon);
		ny = (N+ALTIT)*Math.cos(lat)*Math.sin(lon);
		nz = ((1-E*E) * N + ALTIT)*Math.sin(lat);
		
		//return (Math.sqrt((vx-nx)*(vx-nx)+(vy-ny)*(vy-ny)+(vz-nz)*(vz-nz)));
		
		 if ((oldX != 0) && (Vel > 3)) { 
			 return (Math.sqrt((vx-nx)*(vx-nx)+(vy-ny)*(vy-ny)+(vz-nz)*(vz-nz)));
		 }
		 else
		 {
			   return 0; 
		 }
				
		
	}
	
	private void init_GPS(OutputStream outData) throws IOException{
		
		/* Delete GPS trasparent messages that are not use for application */ 
	    // Byte array with code for output time 
		outData.write(0xFF);
		outData.write(0xFF);
		outData.write(0xFF);
		outData.write(write(PWRON));
		
	    Integer temp = new Integer(181);
	    byte time[] = new byte[12];
	    time[0] = temp.byteValue();
	    time[1] = 98;
	    time[2] = 6;
	    time[3] = 23;
	    time[4] = 4;
	    time[5] = 0;
	    time[6] = 12;
	    time[7] = 35;
	    time[8] = 0;
	    time[9] = 2;
	    time[10] = 82;
	    temp = new Integer(132);
	    time[11] = temp.byteValue();
	      
	    // GPS messages to delete
	    String gll = "PUBX,40,GLL,0,0,0";
	    String gsa = "PUBX,40,GSA,0,0,0";
	    String gsv = "PUBX,40,GSV,0,0,0";
	    String vtg = "PUBX,40,VTG,0,0,0";
	      		      
	    String ck_gll = returnchecksum(gll);
	    String ck_gsa = returnchecksum(gsa);
	    String ck_gsv = returnchecksum(gsv);
	    String ck_vtg = returnchecksum(vtg);
	      
	    gll = "$" + gll + "*" + ck_gll + "\r\n";
	    gsa = "$" + gsa + "*" + ck_gsa + "\r\n";
	    gsv = "$" + gsv + "*" + ck_gsv + "\r\n";
	    vtg = "$" + vtg + "*" + ck_vtg + "\r\n";
	      
	      
	    outData.write(gll.getBytes());
	    outData.write(gsa.getBytes());
	    outData.write(gsv.getBytes());
	    outData.write(vtg.getBytes());
	    outData.write(time);
		
	}
	
	private void sleep_GPS(OutputStream outData) throws IOException{

		String gpsSet = "$PUBX,40,00,0,0,0,0,0,0*1B\r\n";
		outData.write(gpsSet.getBytes());
        gpsSet = "$PUBX,40,04,0,0,0,0,0,0*1F\r\n";
        outData.write(gpsSet.getBytes());
        outData.write(write(PM0sec));
        outData.write(write(PWRLOW));

	}
	
	private byte[] write(int[] data){
		  
		  int x;
		  Integer num;
		  byte[] array = new byte[data.length];
		  for(x = 0; x < data.length; x++){
			  num = new Integer(data[x]);
			  array[x] = num.byteValue();
		  }
		  return array;
			  
	  }
}
			
