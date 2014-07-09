package general;

public class Posusr implements GlobCost{
	
	// Array for strings RMC, GGA e GPS
	private String[] arrayRMC;
	private String[] arrayGGA;
	private String[] arrayGPS;
	private String[] arrayMQTT;
	private double tempDouble;
	private double batt;
	InfoStato infoS;


	public Posusr(){
		
		//System.out.println("Th*CommGPStrasparent: CREATED");
		/*
		 * Array instances
		 */
		// $GPRMC has 14 fields (checksum included)
		arrayRMC = new String[14];
		// $GPGGA has 16 fields (checksum included)
		arrayGGA = new String[16];
		// Final GPS string has 19 fields (without checksum, added after)
		arrayGPS = new String[19];
		//arrayGPS = new String[17];	// removed fields rotta[10] and distanza[13]
		arrayMQTT = new String[21];

		infoS = new InfoStato();
		
	}
	
	public synchronized String set_posusr_old(String gps_rmc, String gps_gga){
		
		String ret = "";
		int virgIndex;
		/*
		 * Copy to arrayGPS the necessary fields that are present in arrayRMC
		 * 	
		 *  Important fields :
		 *  - index n.1	->  time hhmmss             [5, remove cents of s]
		 *  - index n.2	->	string validity         [2]
		 *  - index n.3 ->  latitude                [6]
		 *  - index n.4	->	N/S indication          [7]
		 *  - index n.5	->	longitude               [8]
		 *  - index n.6	->	E/W indication          [9]
		 *  - index n.7	->	speed                   [10]
		 *  - index n.9	->	date ddmmyy             [4, reformatting yymmdd]
		 */
				
		virgIndex = 0;
		for (int i=0; i<arrayRMC.length-2; i++) { /* from 0 to 11 */
			virgIndex = gps_rmc.indexOf(","); /* =6 */
			
			if (virgIndex>0) {
				arrayRMC[i] = gps_rmc.substring(0,virgIndex);
			} 
			else {
				arrayRMC[i] = vuota;	// if the field is empty I put an empty string
			}
			gps_rmc = gps_rmc.substring(virgIndex+1);
			switch (i){
				case 1:
					// Time
					try {
			    		if (!arrayRMC[1].equalsIgnoreCase(vuota)) {
			    			arrayGPS[5]  = arrayRMC[1].substring(0,6);
			    			infoS.setDataSMS(arrayRMC[1].substring(0,2)+":"+arrayRMC[1].substring(2,4)+":"+arrayRMC[1].substring(4,6), 2);
			    		} else {
			    			arrayGPS[5] = "000000";
			    			infoS.setDataSMS("00:00:00", 2);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on time");
					}
					break;
				case 2:
					// GPS Valid data
					if (!arrayRMC[2].equalsIgnoreCase(vuota)) {
						arrayGPS[2]  = arrayRMC[2];
					} else arrayGPS[2] = "V";
					break;
				case 3:
					// Latitude
					if (!arrayRMC[3].equalsIgnoreCase(vuota)) {
						arrayGPS[6]  = arrayRMC[3];
					} else arrayGPS[6] = "0000.0000";
					break;
				case 4:
					// Indicator N/S
					if (!arrayRMC[4].equalsIgnoreCase(vuota)) {
						arrayGPS[7]  = arrayRMC[4];
					} else arrayGPS[7] = "N";
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[6].substring(0,2)+" "+arrayGPS[6].substring(2,4)+"' "
							        	+arrayGPS[6].substring(5,7)+"\"."+arrayGPS[6].substring(7,9)+" "+arrayGPS[7], 3);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on latitude");
					}
					break;
				case 5:
					// Longitude
					if (!arrayRMC[5].equalsIgnoreCase(vuota)) {
						arrayGPS[8]  = arrayRMC[5];
					} else arrayGPS[8] = "00000.0000";
					break;
				case 6:
					// Indicatore E/W
					if (!arrayRMC[6].equalsIgnoreCase(vuota)) {
						arrayGPS[9]  = arrayRMC[6];
					} else arrayGPS[9] = "E";
					
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[8].substring(1,3)+" "+arrayGPS[8].substring(3,5)+"' "
								        +arrayGPS[8].substring(6,8)+"\"."+arrayGPS[8].substring(8,10)+" "+arrayGPS[9], 4);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on longitude");
					}
					break;
				case 7:
					// Speed
					if (!arrayRMC[7].equalsIgnoreCase(vuota)) {
						arrayGPS[10] = arrayRMC[7];
					} else arrayGPS[10] = "0.00";
					
					// Turn speed from knots to kmh
					try {
						if(arrayGPS[10].length() > 1){
							if(arrayGPS[10].indexOf(".") > 0)
								tempDouble = Double.parseDouble(arrayGPS[10].substring(0, arrayGPS[10].indexOf("."))) * 1.852;
							else
								tempDouble = Double.parseDouble(arrayGPS[10]) * 1.852;
						}
						else tempDouble = 0.00;
					} catch(NumberFormatException  e){
						//new LogError("Th*CommGPStrasparent: error speed parsing");
						//System.out.println("Th*CommGPStrasparent: error speed parsing");
						tempDouble = 0.00;
					}
					
					// Data SMS
					try {
						infoS.setDataSMS(Double.toString(tempDouble),7);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on speed");
					}
					break;
				/*case 8:
					// Course
					if (!(arrayRMC[8].equalsIgnoreCase(vuota) || arrayRMC[8] == null)) {
						arrayGPS[10] = arrayRMC[8];
					} else arrayGPS[10] = "0.00";
					
					// Data SMS
					try {
						if(arrayGPS[10].indexOf(".") > 0)
							infoS.setDataSMS(arrayGPS[10].substring(0, arrayGPS[10].indexOf(".")),5);
						else
							infoS.setDataSMS(arrayGPS[10],5);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on route");
					}	
					break;*/
				case 9:
					// Date
					try{
			    		if (arrayRMC[9].length()>3){
			    			arrayGPS[4]  = arrayRMC[9].substring(4)+arrayRMC[9].substring(2,4)+arrayRMC[9].substring(0,2);
			    			infoS.setDataSMS(arrayRMC[9].substring(0,2)+"-"+arrayRMC[9].substring(2,4)+"-"+arrayRMC[9].substring(4), 1);
			    		} else {
			    			arrayGPS[4] = "000000";
			    			infoS.setDataSMS("00-00-00", 1);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on date");
					}
					break;
				default:
					break;
			}
		}
		/* 
		 * Convert GPGGA phrase to array of strings 
		 * $GPGGA,hhmmss.ss,ddmm.mmm,a,dddmm.mmm,b,q,xx,p.p,a.b,M,c.d,M,x.x,nnnn*hh
		 */
		virgIndex = 0;
		for (int i=0; i<arrayGGA.length-2; i++) { /* da 0 a 13 */
			virgIndex = gps_gga.indexOf(",");
			if (virgIndex>0) {
				arrayGGA[i] = gps_gga.substring(0,virgIndex);
			} else arrayGGA[i] = vuota;	// if the field is empty I put an empty string
			
			/*
			 * Copy to arrayGPS necessary fields present in arrayGGA
			 * 	
			 *  Important fields:
			 *  - index n.7	->  satellite number        [3]
			 *  - index n.9	->	altitude                [12]
			 */
			gps_gga = gps_gga.substring(virgIndex+1);
			switch (i){
			
				case 7:
					// NumSat
					if (!arrayGGA[7].equalsIgnoreCase(vuota)) {
						arrayGPS[3]  = arrayGGA[7];
						infoS.setNumSat(arrayGGA[7]);
					} else {
						arrayGPS[3] = "00";
						infoS.setNumSat("00");
					}
					break;
				case 9:
					// Altitude
					if (!arrayGGA[9].equalsIgnoreCase(vuota)) {
						arrayGPS[11] = arrayGGA[9];
					} else arrayGPS[11] = "00.0";
					break;
				default:
					break;
				
			}
		}
		
		// Data SMS
		try {
			if(arrayGPS[11].indexOf(".") > 0)
				infoS.setDataSMS(arrayGPS[11].substring(0, arrayGPS[11].indexOf(".")),6);
			else
				infoS.setDataSMS(arrayGPS[11],6);
		} catch (StringIndexOutOfBoundsException soobex) {
			//System.out.println("Th*CommGPStrasparent: exception on SMS-altitude");
		} //catch
		
		// Fixed parameters on GPS string
		arrayGPS[0]  = infoS.getInfoFileString(Header);
		arrayGPS[1]  = infoS.getInfoFileString(IDtraker);
		/*String tempD = Integer.toString((int)infoS.getDist());
		int lungh = tempD.length();
		if(lungh < 6)
			for(int x = 0; x < 6-lungh; x++)
				tempD = "0"+tempD;
		
		arrayGPS[13] = tempD;*/
		arrayGPS[12] = infoS.getBatteryVoltage();
		/*if ((batt = Double.parseDouble(infoS.getBatteryVoltage())) > 4.3) {
			arrayGPS[15] = "E";		    // key activated = external power supply
		} else arrayGPS[15] = "B";		// key not activated = battery power supply
		*/
		arrayGPS[13] = "E";
		arrayGPS[14] = infoS.getDigitalIN();	// Digital Input
		arrayGPS[15] = "00";	// Digital Output
		arrayGPS[16] = "00000000"; 
		
		/*
		 * Create full GPS string
		 */
		
		for (int i=0; i<arrayGPS.length-1; i++) {
			ret = ret + arrayGPS[i] + ",";
		}
		// last 2 fields
		ret = ret + arrayGPS[16];
		
		return ret;
		
	}

	public synchronized String set_posusr(String gps_rmc, String gps_gga){
		
		String ret = "";
		int virgIndex;
		/*
		 * Copy to arrayGPS the necessary fields that are present in arrayRMC
		 * 	
		 *  Important fields :
		 *  - index n.1	->  time hhmmss             [5, remove cents of s]
		 *  - index n.2	->	string validity         [2]
		 *  - index n.3 ->  latitude                [6]
		 *  - index n.4	->	N/S indication          [7]
		 *  - index n.5	->	longitude               [8]
		 *  - index n.6	->	E/W indication          [9]
		 *  - index n.7	->	speed                   [10]
		 *  - index n.9	->	date ddmmyy             [4, reformatting yymmdd]
		 */			
		virgIndex = 0;
		for (int i=0; i<arrayRMC.length-2; i++) { /* from 0 to 11 */
			virgIndex = gps_rmc.indexOf(","); /* =6 */
			
			if (virgIndex>0) {
				arrayRMC[i] = gps_rmc.substring(0,virgIndex);
			} 
			else {
				arrayRMC[i] = vuota;	// if the field is empty I put an empty string
			}
			gps_rmc = gps_rmc.substring(virgIndex+1);
			switch (i){
				case 1:
					// Time
					try {
			    		if (!arrayRMC[1].equalsIgnoreCase(vuota)) {
			    			arrayGPS[5]  = arrayRMC[1].substring(0,6);
			    			infoS.setDataSMS(arrayRMC[1].substring(0,2)+":"+arrayRMC[1].substring(2,4)+":"+arrayRMC[1].substring(4,6), 2);
			    		} else {
			    			arrayGPS[5] = "000000";
			    			infoS.setDataSMS("00:00:00", 2);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on time");
					}
					break;
				case 2:
					// GPS Valid data
					if (!arrayRMC[2].equalsIgnoreCase(vuota)) {
						arrayGPS[2]  = arrayRMC[2];
					} else arrayGPS[2] = "V";
					break;
				case 3:
					// Latitude
					if (!arrayRMC[3].equalsIgnoreCase(vuota)) {
						arrayGPS[6]  = arrayRMC[3];
					} else arrayGPS[6] = "0000.0000";
					break;
				case 4:
					// Indicator N/S
					if (!arrayRMC[4].equalsIgnoreCase(vuota)) {
						arrayGPS[7]  = arrayRMC[4];
					} else arrayGPS[7] = "N";
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[6].substring(0,2)+" "+arrayGPS[6].substring(2,4)+"' "
							        	+arrayGPS[6].substring(5,7)+"\"."+arrayGPS[6].substring(7,9)+" "+arrayGPS[7], 3);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on latitude");
					}
					break;
				case 5:
					// Longitude
					if (!arrayRMC[5].equalsIgnoreCase(vuota)) {
						arrayGPS[8]  = arrayRMC[5];
					} else arrayGPS[8] = "00000.0000";
					break;
				case 6:
					// Indicatore E/W
					if (!arrayRMC[6].equalsIgnoreCase(vuota)) {
						arrayGPS[9]  = arrayRMC[6];
					} else arrayGPS[9] = "E";
					
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[8].substring(1,3)+" "+arrayGPS[8].substring(3,5)+"' "
								        +arrayGPS[8].substring(6,8)+"\"."+arrayGPS[8].substring(8,10)+" "+arrayGPS[9], 4);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on longitude");
					}
					break;
				case 7:
					// Speed
					if (!arrayRMC[7].equalsIgnoreCase(vuota)) {
						arrayGPS[11] = arrayRMC[7];
					} else arrayGPS[11] = "0.00";
					
					// Turn speed from knots to kmh
					try {
						if(arrayGPS[11].length() > 1){
							if(arrayGPS[11].indexOf(".") > 0)
								tempDouble = Double.parseDouble(arrayGPS[11].substring(0, arrayGPS[11].indexOf("."))) * 1.852;
							else
								tempDouble = Double.parseDouble(arrayGPS[11]) * 1.852;
						}
						else tempDouble = 0.00;
					} catch(NumberFormatException  e){
						//new LogError("Th*CommGPStrasparent: error speed parsing");
						//System.out.println("Th*CommGPStrasparent: error speed parsing");
						tempDouble = 0.00;
					}
					
					// Data SMS
					try {
						infoS.setDataSMS(Double.toString(tempDouble),7);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on speed");
					}
					break;
				case 8:
					// Course
					if (!(arrayRMC[8].equalsIgnoreCase(vuota) || arrayRMC[8] == null)) {
						arrayGPS[10] = arrayRMC[8];
					} else arrayGPS[10] = "0.00";
					
					// Data SMS
					try {
						if(arrayGPS[10].indexOf(".") > 0)
							infoS.setDataSMS(arrayGPS[10].substring(0, arrayGPS[10].indexOf(".")),5);
						else
							infoS.setDataSMS(arrayGPS[10],5);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on route");
					}	
					break;
				case 9:
					// Date
					try{
			    		if (arrayRMC[9].length()>3){
			    			arrayGPS[4]  = arrayRMC[9].substring(4)+arrayRMC[9].substring(2,4)+arrayRMC[9].substring(0,2);
			    			infoS.setDataSMS(arrayRMC[9].substring(0,2)+"-"+arrayRMC[9].substring(2,4)+"-"+arrayRMC[9].substring(4), 1);
			    		} else {
			    			arrayGPS[4] = "000000";
			    			infoS.setDataSMS("00-00-00", 1);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on date");
					}
					break;
				default:
					break;
			}
		}
		/* 
		 * Convert GPGGA phrase to array of strings
		 * $GPGGA,hhmmss.ss,ddmm.mmm,a,dddmm.mmm,b,q,xx,p.p,a.b,M,c.d,M,x.x,nnnn*hh
		 */
		virgIndex = 0;
		for (int i=0; i<arrayGGA.length-2; i++) { /* from 0 to 13 */
			virgIndex = gps_gga.indexOf(",");
			if (virgIndex>0) {
				arrayGGA[i] = gps_gga.substring(0,virgIndex);
			} else arrayGGA[i] = vuota;	// if the field is empty I put an empty string
			
			/*
			 * Copy to arrayGPS necessary fields present in arrayGGA
			 * 	
			 *  Important fields:
			 *  - index n.7	->  satellite number        [3]
			 *  - index n.9	->	altitude                [12]
			 */
			gps_gga = gps_gga.substring(virgIndex+1);
			switch (i){
			
				case 7:
					// NumSat
					if (!arrayGGA[7].equalsIgnoreCase(vuota)) {
						arrayGPS[3]  = arrayGGA[7];
						infoS.setNumSat(arrayGGA[7]);
					} else {
						arrayGPS[3] = "00";
						infoS.setNumSat("00");
					}
					break;
				case 9:
					// Altitude
					if (!arrayGGA[9].equalsIgnoreCase(vuota)) {
						arrayGPS[12] = arrayGGA[9];
					} else arrayGPS[12] = "00.0";
					break;
				default:
					break;
				
			}
		}
		
		// Data SMS
		try {
			if(arrayGPS[12].indexOf(".") > 0)
				infoS.setDataSMS(arrayGPS[12].substring(0, arrayGPS[12].indexOf(".")),6);
			else
				infoS.setDataSMS(arrayGPS[12],6);
		} catch (StringIndexOutOfBoundsException soobex) {
			//System.out.println("Th*CommGPStrasparent: exception on SMS-altitude");
		} //catch
		
		// Fixed parameters on GPS string
		arrayGPS[0]  = infoS.getInfoFileString(Header);
		arrayGPS[1]  = infoS.getInfoFileString(IDtraker);
		String tempD = Integer.toString((int)infoS.getDist());
		int lungh = tempD.length();
		if(lungh < 6)
			for(int x = 0; x < 6-lungh; x++)
				tempD = "0"+tempD;
		
		arrayGPS[13] = tempD;
		arrayGPS[14] = infoS.getBatteryVoltage();
		/*if ((batt = Double.parseDouble(infoS.getBatteryVoltage())) > 4.3) {
			arrayGPS[15] = "E";		    // key activated = external power supply
		} else arrayGPS[15] = "B";		// key not activated = battery power supply
		*/
		arrayGPS[15] = "E";
		arrayGPS[16] = infoS.getDigitalIN();	// Digital Input
		arrayGPS[17] = "00";	// Digital Output
		arrayGPS[18] = "00000000"; 
		
		/*
		 * Create full GPS string
		 */
		
		for (int i=0; i<arrayGPS.length-1; i++) {
			ret = ret + arrayGPS[i] + ",";
		}
		// last 2 fields
		ret = ret + arrayGPS[18];
		
		return ret;
		
	}
	
	public synchronized String set_posusr(String gps_rmc, String gps_gga, String alarm){
		
		String ret = "";
		int virgIndex;
		/*
		 * Copy to arrayGPS the necessary fields that are present in arrayRMC
		 * 	
		 *  Important fields :
		 *  - index n.1	->  time hhmmss             [5, remove cents of s]
		 *  - index n.2	->	string validity         [2]
		 *  - index n.3 ->  latitude                [6]
		 *  - index n.4	->	N/S indication          [7]
		 *  - index n.5	->	longitude               [8]
		 *  - index n.6	->	E/W indication          [9]
		 *  - index n.7	->	speed                   [10]
		 *  - index n.9	->	date ddmmyy             [4, reformatting yymmdd]
		 */
		 				
		virgIndex = 0;
		for (int i=0; i<arrayRMC.length-2; i++) { /* from 0 to 11 */
			virgIndex = gps_rmc.indexOf(","); /* =6 */
			
			if (virgIndex>0) {
				arrayRMC[i] = gps_rmc.substring(0,virgIndex);
			} 
			else {
				arrayRMC[i] = vuota;	// if the field is empty I put an empty string
			}
			gps_rmc = gps_rmc.substring(virgIndex+1);
			switch (i){
				case 1:
					// Time
					try {
			    		if (!arrayRMC[1].equalsIgnoreCase(vuota)) {
			    			arrayGPS[5]  = arrayRMC[1].substring(0,6);
			    			infoS.setDataSMS(arrayRMC[1].substring(0,2)+":"+arrayRMC[1].substring(2,4)+":"+arrayRMC[1].substring(4,6), 2);
			    		} else {
			    			arrayGPS[5] = "000000";
			    			infoS.setDataSMS("00:00:00", 2);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on time");
					}
					break;
				case 2:
					// GPS Valid data
					if (!arrayRMC[2].equalsIgnoreCase(vuota)) {
						arrayGPS[2]  = arrayRMC[2];
					} else arrayGPS[2] = "V";
					break;
				case 3:
					// Latitude
					if (!arrayRMC[3].equalsIgnoreCase(vuota)) {
						arrayGPS[6]  = arrayRMC[3];
					} else arrayGPS[6] = "0000.0000";
					break;
				case 4:
					// Indicator N/S
					if (!arrayRMC[4].equalsIgnoreCase(vuota)) {
						arrayGPS[7]  = arrayRMC[4];
					} else arrayGPS[7] = "N";
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[6].substring(0,2)+" "+arrayGPS[6].substring(2,4)+"' "
							        	+arrayGPS[6].substring(5,7)+"\"."+arrayGPS[6].substring(7,9)+" "+arrayGPS[7], 3);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on latitude");
					}
					break;
				case 5:
					// Longitude
					if (!arrayRMC[5].equalsIgnoreCase(vuota)) {
						arrayGPS[8]  = arrayRMC[5];
					} else arrayGPS[8] = "00000.0000";
					break;
				case 6:
					// Indicatore E/W
					if (!arrayRMC[6].equalsIgnoreCase(vuota)) {
						arrayGPS[9]  = arrayRMC[6];
					} else arrayGPS[9] = "E";
					
					// Data SMS
					try {
						infoS.setDataSMS(arrayGPS[8].substring(1,3)+" "+arrayGPS[8].substring(3,5)+"' "
								        +arrayGPS[8].substring(6,8)+"\"."+arrayGPS[8].substring(8,10)+" "+arrayGPS[9], 4);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on longitude");
					}
					break;
				case 7:
					// Speed
					if (!arrayRMC[7].equalsIgnoreCase(vuota)) {
						arrayGPS[11] = arrayRMC[7];
					} else arrayGPS[11] = "0.00";
					
					// Turn speed from knots to kmh
					try {
						if(arrayGPS[11].length() > 1){
							if(arrayGPS[11].indexOf(".") > 0)
								tempDouble = Double.parseDouble(arrayGPS[11].substring(0, arrayGPS[11].indexOf("."))) * 1.852;
							else
								tempDouble = Double.parseDouble(arrayGPS[11]) * 1.852;
						}
						else tempDouble = 0.00;
					} catch(NumberFormatException  e){
                        //new LogError("Th*CommGPStrasparent: error speed parsing");
						//System.out.println("Th*CommGPStrasparent: error speed parsing");
						tempDouble = 0.00;
					}
					
					// Data SMS
					try {
						infoS.setDataSMS(Double.toString(tempDouble),7);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on speed");
					}
					break;
				case 8:
					// Course
					if (!(arrayRMC[8].equalsIgnoreCase(vuota) || arrayRMC[8] == null)) {
						arrayGPS[10] = arrayRMC[8];
					} else arrayGPS[10] = "0.00";
					
					// Data SMS
					try {
						if(arrayGPS[10].indexOf(".") > 0)
							infoS.setDataSMS(arrayGPS[10].substring(0, arrayGPS[10].indexOf(".")),5);
						else
							infoS.setDataSMS(arrayGPS[10],5);
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on route");
					}	
					break;
				case 9:
					// Date
					try{
			    		if (arrayRMC[9].length()>3){
			    			arrayGPS[4]  = arrayRMC[9].substring(4)+arrayRMC[9].substring(2,4)+arrayRMC[9].substring(0,2);
			    			infoS.setDataSMS(arrayRMC[9].substring(0,2)+"-"+arrayRMC[9].substring(2,4)+"-"+arrayRMC[9].substring(4), 1);
			    		} else {
			    			arrayGPS[4] = "000000";
			    			infoS.setDataSMS("00-00-00", 1);
			    		}
					} catch (StringIndexOutOfBoundsException soobex) {
						//System.out.println("Th*CommGPStrasparent: exception on date");
					}
					break;
				default:
					break;
			}
		}
		/* 
		 * Convert GPGGA phrase to array of strings
		 * $GPGGA,hhmmss.ss,ddmm.mmm,a,dddmm.mmm,b,q,xx,p.p,a.b,M,c.d,M,x.x,nnnn*hh
		 */
		virgIndex = 0;
		for (int i=0; i<arrayGGA.length-2; i++) { /* from 0 to 13 */
			virgIndex = gps_gga.indexOf(",");
			if (virgIndex>0) {
				arrayGGA[i] = gps_gga.substring(0,virgIndex);
			} else arrayGGA[i] = vuota;	// if the field is empty I put an empty string
			
			/*
			 * Copy to arrayGPS necessary fields present in arrayGGA
			 * 	
			 *  Important fields:
			 *  - index n.7	->  satellite number        [3]
			 *  - index n.9	->	altitude                [12]
			 */
			gps_gga = gps_gga.substring(virgIndex+1);
			switch (i){
			
				case 7:
					// NumSat
					if (!arrayGGA[7].equalsIgnoreCase(vuota)) {
						arrayGPS[3]  = arrayGGA[7];
						infoS.setNumSat(arrayGGA[7]);
					} else {
						arrayGPS[3] = "00";
						infoS.setNumSat("00");
					}
					break;
				case 9:
					// Altitude
					if (!arrayGGA[9].equalsIgnoreCase(vuota)) {
						arrayGPS[12] = arrayGGA[9];
					} else arrayGPS[12] = "00.0";
					break;
				default:
					break;
				
			}
		}
		
		// Data SMS
		try {
			if(arrayGPS[12].indexOf(".") > 0)
				infoS.setDataSMS(arrayGPS[12].substring(0, arrayGPS[12].indexOf(".")),6);
			else
				infoS.setDataSMS(arrayGPS[12],6);
		} catch (StringIndexOutOfBoundsException soobex) {
			//System.out.println("Th*CommGPStrasparent: exception on SMS-altitude");
		} //catch

		// Fixed parameters on GPS string
		arrayGPS[0]  = infoS.getInfoFileString(Header);
		arrayGPS[1]  = infoS.getInfoFileString(IDtraker);
		String tempD = Integer.toString((int)infoS.getDist());
		int lungh = tempD.length();
		if(lungh < 6)
			for(int x = 0; x < 6-lungh; x++)
				tempD = "0"+tempD;
		
		arrayGPS[13] = tempD;
		arrayGPS[14] = infoS.getBatteryVoltage();
		/*if ((batt = Double.parseDouble(infoS.getBatteryVoltage())) > 4.3) {
			arrayGPS[15] = "E";		    // key activated = external power supply
		} else arrayGPS[15] = "B";		// key not activated = battery power supply
		*/
		arrayGPS[15] = "E";
		arrayGPS[16] = infoS.getDigitalIN();	// Digital Input
		arrayGPS[17] = "00";	// Digital Output
		arrayGPS[18] = "00000000"; 
		
		/*
		 * Create full GPS string
		 */
		
		for (int i=0; i<arrayGPS.length-1; i++) {
			ret = ret + arrayGPS[i] + ",";
		}
		// last 2 fields
		ret = ret + arrayGPS[18];
		
		return ret;	
	}
	
	public synchronized String[] set_posusr_mqtt(String gps_usr){
		
		int virgIndex;
		/*
		 * Copy to arrayGPS necessary fields present in the USR string
		 * 	
		 *  $CHX,GLOBA07,A,09,130829,134348,4540.89791,N,01157.71805,E,0.00,0.017,52.0,000000,4.4V,E,08,00,00000000*54
		 *  
		 * HEADER = 0;
		 * DEVICE_ID = 1;
		 * GPS_VALID_DATA = 2;
		 * NUM_SAT = 3;
		 * DATE = 4;
		 * TIME = 5;
		 * LAT = 6;
		 * NS = 7;
		 * LON = 8;
		 * WE = 9;
		 * COURSE = 10;
		 * SPEED = 11;
		 * ALT = 12;
		 * DIST = 13;
		 * VBATT = 14;
		 * EB = 15;
		 * DIN = 16;
		 * DOUT = 17;
		 * AIN = 18;
		 * ALR_IND = 19;
		 * 
		 */
				
		virgIndex = 0;
		System.out.println("LUNGH: " + arrayMQTT.length);
		for (int i=0; i<arrayMQTT.length-2; i++) {
			virgIndex = gps_usr.indexOf(",");
			
			if (virgIndex>0) {
				arrayMQTT[i] = gps_usr.substring(0,virgIndex);
			} 
			else {
				arrayMQTT[i] = vuota;	// if the field is empty I put an empty string
			}
			System.out.println(arrayMQTT[i]);
			gps_usr = gps_usr.substring(virgIndex+1);
		}		
		return arrayMQTT;
		
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
