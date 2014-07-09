package general;

import java.util.Vector;

public class Coda extends Vector implements GlobCost{ 
	
	/*
	 * local variables 
	 */
	private Object 	lastValidPOSQ= null;
	private boolean isLastValidQ = false;
	InfoStato infoS;
	private String dsTypeQ;
	private int ptr_in;
	private int ptr_out;
	private Object element;
	
	/*
	 * constructors 
	 */
	public Coda(String type, int capacity) {
		dsTypeQ = type;
		this.setSize(capacity);
		infoS = new InfoStato();
	}
	
	public void addElement(Object obj){
		ptr_in = infoS.getInfoFileInt(TrkIN);
		removeElementAt(ptr_in);
		insertElementAt(obj, ptr_in);
		ptr_in++;
		if(ptr_in > 99){
			ptr_in = 0;
		}
		infoS.setInfoFileInt(TrkIN, Integer.toString(ptr_in));
	}
	
	public Object returnElement(){
		ptr_out = infoS.getInfoFileInt(TrkOUT);
		element = elementAt(ptr_out);
		ptr_out++;
		if(ptr_out > 99){
			ptr_out = 0;
		}
		infoS.setInfoFileInt(TrkOUT, Integer.toString(ptr_out));
		return element;
	}
	
	/**
	 * Synchronized method to obtain last GPS valid position saved
	 * to DataStore, if not already done the first FIX return 'null'
	 * 
	 * @return	GPRMC string corresponding to the last valid GPS position
	 * 			or 'null' value if not already done the first FIX
	 * 
	 */
//	public synchronized Object LastValidElement() {
		/* 
		 * If in current session there was a FIX, then send last valid
		 * position of current session, OTHERWISE SEND THE POSITION RECOVERED
		 * FROM FILE
		 */
/*		if (lastValidPOSQ!=null) return lastValidPOSQ;
		else {
			if (dsTypeQ.equalsIgnoreCase(dsCHORAL)) {
				//System.out.println("Datastore *** return: "+infoS.getInfoFileString(LastGPSValid));
				return infoS.getInfoFileString(LastGPSValid);
			}
			else {
				//System.out.println("Datastore *** return: "+infoS.getInfoFileString(LastGPRMCValid));
				return infoS.getInfoFileString(LastGPRMCValid);
			}
		}
	}
*/	
	public synchronized Object readOnlyIfObjectIsValid() {
		// if last received GPS position is valid, return this
		if (isLastValidQ==true) {
			return this.lastElement();
		}
		// otherwise return 'null'
		else return null;
	}
	
	/**
	 *  Add reference to InfoStato data structure
	 *  
	 *  @param	is 	InfoStato object
	 *  @return "OK,infoS"
	 */
	public synchronized String addInfoStato(InfoStato is) {	
		infoS = is;
		return "OK,infoS";
	} //addInfoStato

}