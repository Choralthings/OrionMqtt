/*	
 * Class 	InfoStato
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Contains all informations about thread and task status,
 * to be used and exchanged between application classes.
 * 
 * @version	1.04 <BR> <i>Last update</i>: 04-12-2007
 * @author 	alessioza
 * 
 */
public class InfoStato implements GlobCost {
	
	/* 
	 * local variables
	 */
	private boolean ATexec = false;		// resource is free at startup
	private int		numSMS, maxSMS;
	private int		codSMS = -1;
	private boolean	validSMS = false;
	private String	numTelSMS = null;
	private String	opMode = null;
	private String  validOP = "no rete";
	private String  responseAT;
	private boolean validFIX = false;
	private boolean validFIXgprs = false;
	private boolean FIXtimeoutExpired = false;
	private boolean FIXgprsTimeoutExpired = false;
	private int		GPIOvalue;
	private int 	digInput0 = 0;
	private int 	digInput1 = 0;
	private int		digInput2 = 0;
	private int		digInput3 = 0;
	private boolean cala = false;
	private boolean	CSDWatchDog = false;
	private boolean attivazioneSensore = false;
	private boolean disattivazioneSensore = false;
	private boolean attivazionePolling = false;
	private String  STATOexecApp;
	private boolean inibizioneChiave = false;
	private boolean socketAttivo = false;
	private boolean trackingAttivo = false;
	private String  tipoRisveglio;
	private boolean	appSTANDBY = false;
	private boolean enableCSD = false;
	private boolean enableGPRS = true;
	private boolean CSDconnect = false;
	private boolean CSDattivo = false;
	private boolean trackingInCorso = false;
	private boolean SMSsent = false;
	private boolean restartGPRS = false;
	private boolean uartTraspGPS = false;
	private boolean csdTraspGPS = false;
	private String	numSat, rssiCSQ;
	private String 	imei;
	private String	batteryVoltage = "0.0V";
	private String  value1,value2,value3,value4,value5,value6,value7;
	private String	temp;
	private int		tempInt;
	private String  dataGPRMC = null;
	private String  oraGPRMC = null;
	private int 	GPIOnumberTEST;
	private double 	distance;
	private double 	DFSSpeed = 0.0;
	private double  TrkSpeed = 0.0;
	private double	DFSPreSpeed = 0.0;
	private int		GreeSpeed=0;
	private boolean InvioCoordinataStop = true;
	private boolean trasmetti = false;
	
	// Settings for configuration file
	private String  header;
	private String  trakerID;
	private String  pwCSD;
	private String	appname;
	private String  lastCloseMode;
	private String  lastGPSvalid;
	private String  lastGPRMCvalid;
	private String	destHostFile;
	private String 	destPortFile;
	private String 	connProfileGPRS;
	private String	entryPointUDPfile;
	private String  gprsProtocolfile;
	private String 	typeTRK;
	private String  protTRK;
	private String  acknowledge;
	private String 	opNum;
	private String  gprsOnTm;
	private String  trkState;
	private String	publishTopic;
	private String  slp;
	private String  movsens;
	private String  ign;
	private String  uGW;
	private String  uHead;
	private String  uEOMrs;
	private String  uEOMip;
	private String  uID;
	private String	trackTM;
	private String 	dataRAM = "";
	private String	moduleRev = "";
	private String	rmc = "";
	private String 	gga = "";
	private int  stilltm;
	private int uSpeed;
	private int uATO;
	private int uNumT;
	private int uTXto;
	private int	trackInterval;
	private int	orePowerDownOK;
	private int	minPdOK;
	private int	orePdTOexpired;
	private int	minPdTOexpired;
	private int contIN;
	private int contOUT;
	private int gprsOT;
	private String  insensibilitaGPS;
	private boolean apriGPRS= true;
	private boolean chiudiGPRS = false;
	private boolean preAlive = false;
	private static boolean free_coda = true;
	private static boolean free_file = true;
	private static boolean free_log = true;
	private static boolean free_micro = true;
	private String[] record = new String[codaSize];
	private String[][] recordMqtt = new String[codaSize][21];
	private String commandSMS= "";
	private boolean gprsBearer = false;
	private boolean riavvia = false;
	private String codiceTastiera = "";
	private int dataX = 0;
	private int dataY = 0;
	private int dataZ = 0;
	private boolean crashDetect = false;
	private int asseX = 0;
	private int asseY = 0;
	private int asseZ = 0;
	private boolean closeTrack = false;
	private boolean closeUDP = false;
	private boolean closeTCP = false;
	private boolean canbusState = false;
	private boolean gpsLive = true;
	private int bearer = 0;
	private String creg = "null";
	private String cgreg = "null";
	private int errorGPS = 0;
	private boolean alarmNack = false;
	private int t1=0,t2=0,t3=0;
	private boolean t1WD = false, t2WD = false, t3WD = false;
	private boolean gpsLed = false;
	private String[] dataMqttRAM = null;
	private int counterIn1 = 0;
	private int counterIn2 = 0;
	private boolean waitAlarmVinSMS = false;
	private boolean gps_state = true;
	private boolean powerSupply = false;
	private boolean powerSupplyOff = false;
	private String powerVoltage = "0.0";
	
	/* 
	 * constructors
	 */
	public InfoStato() {
		//System.out.println("InfoStato: CREATED");
	}
	
	
	/* 
	 * methods
	 */
	
	synchronized public void setTickTask1WD(){
		//System.out.println("InfoStato: setTickTask1WD()++++++TRUE");
		t1WD = true;
	}
	synchronized public void setTickTask3WD(){
		//System.out.println("InfoStato: setTickTask3WD()++++++TRUE");
		t3WD = true;
	}
	synchronized public boolean getTickTask1WD(){
		return t1WD;
	}
	synchronized public boolean getTickTask3WD(){
		return t3WD;
	}
	synchronized public void resetTickTaskWD(){
		//System.out.println("InfoStato: resetTickTaskWD()++++++FALSE");
		t1WD = false;
		t3WD = false;
	}
	
	synchronized static public boolean getFile()
	{				
		if(!free_file)
		{
			return false;
		}
		else
		{
			free_file = false;
			return true;
		}
	}
	
	synchronized static public void freeFile() 
	{		
		free_file = true;
	}
	
	synchronized static public boolean getCoda()
	{				
		if(!free_coda)
		{
			return false;
		}
		else
		{
			free_coda = false;
			return true;
		}
	}
	
	synchronized static public void freeCoda() 
	{		
		free_coda = true;
	}
	
	synchronized static public boolean getLogSemaphore()
	{				
		if(!free_log)
		{
			return false;
		}
		else
		{
			free_log = false;
			return true;
		}
	}
	
	synchronized static public void freeLogSemaphore() 
	{		
		free_log = true;
	}
	
	synchronized static public boolean getMicroSemaphore()
	{				
		if(!free_micro)
		{
			return false;
		}
		else
		{
			free_micro = false;
			return true;
		}
	}
	
	synchronized static public void freeMicroSemaphore() 
	{		
		free_micro = true;
	}
	/**
	 * Load Record
	 */
	synchronized public String getRecord(int i){
		
		return record[i];
	}
	
	/**
	 * Save record
	 */
	synchronized public void saveRecord(int i, String dato){

		record[i] = dato;
	}
	
	/**
	 * Load Record
	 */
	synchronized public String[] getMqttRecord(int i){
		
		return recordMqtt[i];
	}
	
	/**
	 * Save record
	 */
	synchronized public void saveMqttRecord(int i, String[] dato){

		recordMqtt[i] = dato;
	}
	
	/**
	 * Set distance
	 * 
	 * @param	newValue	distance
	 * 
	 */
	synchronized public void setDist(double newValue){
		
		distance = newValue;
		
	}
	
	/**
	 * Get distance
	 * 
	 * @return	distance
	 * 
	 */
	synchronized public double getDist(){
		
		return distance;
		
	}
	
	public synchronized void setPreAlive(boolean value){
		
		preAlive = value;
	}
	
	public synchronized boolean getPreAlive(){
		
		return preAlive;
	}
	
	public synchronized double setSpeedDFS(double newValue) {
		DFSSpeed = newValue;
		return DFSSpeed;
	}
	
	public synchronized double getSpeedDFS() {
		return DFSSpeed;
	}
	
	public synchronized double setSpeedForTrk(double newValue) {
		TrkSpeed = newValue;
		return TrkSpeed;
	}
	
	public synchronized double getSpeedForTrk() {
		return TrkSpeed;
	}
	
	public synchronized double setPreSpeedDFS(double newValue) {
		DFSPreSpeed = newValue;
		return DFSPreSpeed;
	}
	
	public synchronized double getPreSpeedDFS() {
		return DFSPreSpeed;
	}
	
	public synchronized int getSpeedGree() {
		return GreeSpeed;
	}
	
	public synchronized int setSpeedGree(int newValue) {
		GreeSpeed = newValue;
		return GreeSpeed;
	}
	
	public synchronized void setGprsState(boolean newValue) {
		gprsBearer = newValue;
	}
	
	public synchronized boolean getGprsState() {
		return gprsBearer;
	}
	
	public synchronized boolean setInvioStop(boolean newValue) {
		InvioCoordinataStop = newValue;
		return InvioCoordinataStop;
	}
	
	public synchronized boolean getInvioStop() {
		return InvioCoordinataStop;
	}
	
	
	public synchronized boolean setApriGPRS(boolean newValue) {
		apriGPRS = newValue;
		return apriGPRS;
	}
	
	public synchronized boolean getApriGPRS() {
		return apriGPRS;
	}
	
	public synchronized boolean setChiudiGPRS(boolean newValue) {
		chiudiGPRS = newValue;
		return chiudiGPRS;
	}
	
	public synchronized boolean getChiudiGPRS() {
		return chiudiGPRS;
	}
	
	
	
	public synchronized boolean settrasmetti(boolean newValue) {
		trasmetti = newValue;
		return trasmetti;
	}
	
	public synchronized boolean gettrasmetti() {
		return trasmetti;
	}
	
	/**
	 * Set keyboard code
	 * 
	 * @param	newValue	String
	 * 
	 */
	public void setCode(String newValue){
		
		codiceTastiera = newValue;
		
	}
	
	/**
	 * Get keyboard code
	 * 
	 * @return	code
	 * 
	 */
	public String getCode(){
		
		return codiceTastiera;
		
	}
	
	/**
	 * Set fw XT65 release
	 * 
	 * @param	newValue	String
	 * 
	 */
	public void setREV(String newValue){
		
		moduleRev = newValue;
		
	}
	
	/**
	 * Get fw XT65 release
	 * 
	 * @return	module Revision
	 * 
	 */
	public String getREV(){
		
		return moduleRev;
		
	}
	
	/**
	 * Set GPS transparence
	 * 
	 * @param	newValue	Boolean value
	 * 
	 */
	public void setUartTraspGPS(boolean newValue){
		
		uartTraspGPS = newValue;
		
	}
	
	/**
	 * Get GPS transparence
	 * 
	 * @return	true or false;
	 * 
	 */
	public boolean getUartTraspGPS(){
		
		return uartTraspGPS;
		
	}
	
	/**
	 * Set GPS transparence
	 * 
	 * @param	newValue	Boolean value
	 * 
	 */
	public void setCSDTraspGPS(boolean newValue){
		
		csdTraspGPS = newValue;
		if(!csdTraspGPS){
			rmc = "";
			gga = "";
		}
		
	}
	
	/**
	 * Get GPS transparence
	 * 
	 * @return	true or false;
	 * 
	 */
	public boolean getCSDTraspGPS(){
		
		return csdTraspGPS;
		
	}
	
	/**
	 * Set CSD GPS transparence
	 * 
	 * @param	GPS string
	 * 
	 */
	public void setRMCTrasp(String newValue){
		rmc = newValue;
	}
	
	public void setGGATrasp(String newValue){
		gga = newValue;
	}
	
	/**
	 * Get CSD GPS transparence
	 * 
	 * @return	GPS string
	 * 
	 */
	
	public String getRMCTrasp(){
		return rmc;
	}
	
	public String getGGATrasp(){
		return gga;
	}
	
	/**
	 * To set busy the AT resource
	 * 
	 * @param	newValue	'true' if busy, 'false' if free
	 * @return	'OK'
	 */
	public synchronized String setATexec(boolean newValue) {
		ATexec = newValue;
		return "OK";
	}
	
	/**
	 * To verify if AT resource is busy
	 * 
	 * @return	'true' if busy, 'false' otherwise
	 */
	public synchronized boolean getATexec() {
		return ATexec;
	}
	
	/**
	 * To set GPRS reset
	 * 
	 * @param	state  'true' if necessary GPRS reset
	 */
	public synchronized void setCloseGPRS(boolean state) {
		restartGPRS = state;
	}
	
	/**
	 * To verify if restart GPRS
	 * 
	 * @return	'true' if GPRS restart needed
	 */
	public synchronized boolean closeGPRS() {
		return restartGPRS;
	}
	
	/**
	 * To get number of SMS in memory
	 * 
	 * @return	total number of SMS in memory
	 */
	public synchronized int getNumSMS() {
		return numSMS;
	}
	
	/**
	 * To set number of SMS in memory
	 * 
	 * @param	value	total number of SMS in memory
	 */
	public synchronized void setNumSMS(int value) {
		numSMS = value;
	}
	
	/**
	 * To set sender telephone number (receiving SMS)
	 * 
	 * @param	value	sender telephone number (receiving SMS)
	 */
	public synchronized void setNumTelSMS(String value) {
		numTelSMS = value;
	}
	
	/**
	 * To get sender telephone number (receiving SMS)
	 * 
	 * @return	sender telephone number (receiving SMS)
	 */
	public synchronized String getNumTelSMS() {
		return numTelSMS;
	}
	
	/**
	 * To get max number of SMS that can be stored
	 * 
	 * @return	max number of SMS that can be stored
	 */
	public synchronized int getMaxNumSMS() {
		return maxSMS;
	}
	
	/**
	 * To set max number of SMS that can be stored
	 * 
	 * @param	value	max number of SMS that can be stored
	 */
	public synchronized void setMaxNumSms(int value) {
		maxSMS = value;
	}
	
	/**
	 * To set code of SMS to read
	 * 
	 * @param	value	code of SMS to read
	 */
	public synchronized void setCodSMS(int value) {
		codSMS = value;
	}
	
	/**
	 * To get code of SMS to read
	 * 
	 * @return	code of SMS to read
	 */
	public synchronized int getCodSMS() {
		return codSMS;
	}
	
	/**
	 * To get current number of satellites viewed
	 * 
	 * @return	current number of satellites viewed
	 */
	public synchronized String getNumSat() {
		if(numSat == null)
			return "0";
		return numSat;
	}
	
	/**
	 * To set GSM coverage indication
	 * 
	 * @param	value	GSM coverage indication
	 */
	public synchronized void setCSQ(String value) {
		rssiCSQ = value;
	}
	
	/**
	 * To get GSM coverage indication
	 * 
	 * @return	GSM coverage indication
	 */
	public synchronized String getCSQ() {
		return rssiCSQ;
	}
	
	/**
	 * To set module IMEI
	 * 
	 * @param	value	IMEI
	 */
	public synchronized void setIMEI(String value) {
		imei = value;
	}
	
	/**
	 * To get module IMEI
	 * 
	 * @return	module IMEI
	 */
	public synchronized String getIMEI() {
		return imei;
	}
	
	/**
	 * To set current number of satellites viewed
	 * 
	 * @param	value	current number of satellites viewed
	 */
	public synchronized void setNumSat(String value) {
		numSat = value;
	}
	
	/**
	 * To set module operating mode, based on input parameter,
	 * to control ^SYSSTART
	 * 
	 * @param	mode	operating mode, may be 'NORMAL' or 'AIRPLANE'
	 * @return	'NORMAL' or 'AIRPLANE'
	 */
	public synchronized String setOpMode(String mode) {
		if (mode.equalsIgnoreCase("NORMAL")) {
			opMode = "NORMAL";
		} else if (mode.equalsIgnoreCase("AIRPLANE")) {
			opMode = "AIRPLANE";
		} //if
		return opMode;
	}
	
	/**
	 * To get module operating mode
	 * 
	 * @return	'NORMAL' or 'AIRPLANE'
	 */
	public synchronized String getOpMode() {
		return opMode;
	}

	/**
	 * To modify answer to requested read AT command
	 * 
	 * @param	resp	answer to requested read AT command
	 */
	public synchronized void setResponseAT(String resp) {
		responseAT = resp;		
	}
	
	/**
	 * To get answer to requested read AT command
	 * 
	 * @return	responseAT
	 */
	public synchronized String getResponseAT() {
		return responseAT;
	}
	
	/**
	 * To set valid FIX indication before timeout expired
	 * 
	 * @param	fix		'true' if valid FIX, 'false' otherwise
	 */
	public synchronized void setValidFIX(boolean fix) {
		validFIX = fix;		
	}
	
	/**
	 * To verify valid FIX indication before timeout expired
	 * 
	 * @return	'true' if valid FIX, 'false' otherwise
	 */
	public synchronized boolean getValidFIX() {
		return validFIX;
	}
	
	/**
	 * To set if valid FIX indication sent correctly through GPRS
	 * for requested number of times
	 * 
	 * @param	fix		'true' if valid FIX sent, 'false' otherwise
	 */
	public synchronized void setValidFIXgprs(boolean fix) {
		validFIXgprs = fix;		
	}
	
	/**
	 * To verify if valid FIX indication sent correctly through GPRS
	 * for requested number of times
	 * 
	 * @return	'true' if valid FIX sent, 'false' otherwise
	 */
	public synchronized boolean getValidFIXgprs() {
		return validFIXgprs;
	}
	
	/**
	 * To set SMS validity indicator
	 * 
	 * @param	value	'true' if valid SMS, 'false' otherwise
	 */
	public synchronized void setValidSMS(boolean value) {
		validSMS = value;		
	}
	
	public synchronized String getSMSCommand() {
		return commandSMS;
	}
	
	public synchronized void setSMSCommand(String command){
		commandSMS = command;		
	}
	
	/**
	 * To verify SMS validity indicator
	 * 
	 * @return	'true' if valid SMS, 'false' otherwise
	 */
	public synchronized boolean getValidSMS() {
		return validSMS;
	}
	
	/**
	 * To set network operator
	 * 
	 * @param	value	network operator
	 */
	public synchronized void setOpRete(String value) {
		validOP = value;		
	}
	
	/**
	 * To get network operator
	 * 
	 * @return	network operator
	 */
	public synchronized String getOpRete() {
		return validOP;
	}
	
	/**
	 * To set if 'FIXtimeout' expired
	 * 
	 * @param	expired		'true' if 'FIXtimeout' expired, 'false' otherwise
	 */
	public synchronized void setIfIsFIXtimeoutExpired(boolean expired) {
		FIXtimeoutExpired = expired;		
	}
	
	/**
	 * To verify if 'FIXtimeout' expired
	 * 
	 * @return	'true' if 'FIXtimeout' expired, 'false' otherwise
	 */
	public synchronized boolean isFIXtimeoutExpired() {
		return FIXtimeoutExpired;
	}
	
	/**
	 * To set if 'FIXgprsTimeout' expired
	 * 
	 * @param	expired		'true' if 'FIXgprsTimeout' expired, 'false' otherwise
	 */
	public synchronized void setIfIsFIXgprsTimeoutExpired(boolean expired) {
		FIXgprsTimeoutExpired = expired;		
	}
	
	/**
	 * To verify if 'FIXgprsTimeout' expired
	 * 
	 * @return	'true' if 'FIXgprsTimeout' expired, 'false' if SMS MOVE sent
	 */
	public synchronized boolean isFIXgprsTimeoutExpired() {
		return FIXgprsTimeoutExpired;
	}
	
	/**
	 * To set GPIO key value
	 * 
	 * @param	value	GPIO key value
	 */
	public synchronized void setGPIOchiave(int value) {
		GPIOvalue = value;		
	}
	
	/**
	 * To get GPIO key value
	 * 
	 * @return	GPIO key value
	 */
	public synchronized int getGPIOchiave() {
		return GPIOvalue;
	}
	
	/**
	 * To set an INPUT value
	 * 
	 * @param	value	an INPUT value
	 */
	public synchronized void setDigitalIN(int value, int number) {
		// Digital Input 0 = GPIO7 = KEY
		if (number==0) {
			if (value==0) digInput0=1;
			if (value==1) digInput0=0;	
		}
		// Digital Input 1 = GPIO1	
		if (number==1) {
			if (value==0) digInput1=1;
			if (value==1) digInput1=0;	
		}
		// Digital Input 2 = GPIO3
		if (number==2) {
			if (value==0) digInput2=1;
			if (value==1) digInput2=0;	
		}
		// Digital Input 3 = 0
	}
	
	/**
	 * To get an INPUT value
	 * 
	 * @return	an INPUT value
	 */
	public synchronized String getDigitalIN() {
		// calculate minimal string
		tempInt = digInput1*1 + digInput2*2 + digInput3*4 + digInput0*8;
		//System.out.println("Number to convert: " + digInput0+":"+digInput1+":"+digInput2+":"+tempInt);
		return ("0" + Integer.toHexString(tempInt)).toUpperCase();
	}
	
	/**
	 * To get an INPUT value
	 * 
	 * @return	an INPUT value
	 */
	public synchronized String getDigitalIN(int num) {
		// calculate minimal string
		switch(num){
		case 1:tempInt = 1*1 + digInput2*2 + digInput3*4 + digInput0*8;break;
		case 2:tempInt = digInput1*1 + 1*2 + digInput3*4 + digInput0*8;break;
		case 3:tempInt = 0*1 + digInput2*2 + digInput3*4 + digInput0*8;break;
		case 4:tempInt = digInput1*1 + digInput2*2 + digInput3*4 + digInput0*8;break;
		default:tempInt = digInput1*1 + 0*2 + digInput3*4 + digInput0*8;break;
		}
		return ("0" + Integer.toHexString(tempInt)).toUpperCase();
	}
	
	/**
	 * To set GPIO number under TEST <BR>
	 * Please note: to be attached to every invocation of the command AT^SGIO
	 * 
	 * @param	value	GPIO number under TEST
	 */
	public synchronized void setGPIOnumberTEST(int value) {
		GPIOnumberTEST = value;		
	}
	
	/**
	 * To get GPIO number under TEST
	 * 
	 * @return	GPIO number under TEST
	 */
	public synchronized int getGPIOnumberTEST() {
		return GPIOnumberTEST;
	}
	
	/**
	 * To set motion sensor activation
	 * 
	 * @param	value	boolean value about motion sensor activation
	 */
	public synchronized void setAttivaSensore(boolean value) {
		attivazioneSensore = value;		
	}
	
	/**
	 * To get indication about motion sensor activation
	 * 
	 * @return	boolean value about motion sensor activation
	 */
	public synchronized boolean getAttivaSensore() {
		return attivazioneSensore;
	}
	
	/**
	 * To set motion sensor deactivation
	 * 
	 * @param	value	boolean value about motion sensor deactivation
	 */
	public synchronized void setDisattivaSensore(boolean value) {
		disattivazioneSensore = value;		
	}
	
	/**
	 * To get indication about motion sensor deactivation
	 * 
	 * @return	boolean value about motion sensor deactivation
	 */
	public synchronized boolean getDisattivaSensore() {
		return disattivazioneSensore;
	}
	
	/**
	 * To set GPIO key polling activation
	 * 
	 * @param	value	boolean value about GPIO key polling activation
	 */
	public synchronized void setPollingAttivo(boolean value) {
		attivazionePolling = value;		
	}
	
	/**
	 * To get indication about GPIO key polling activation
	 * 
	 * @return	boolean value about GPIO key polling activation
	 */
	public synchronized boolean getPollingAttivo() {
		return attivazionePolling;
	}
	
	/**
	 * To set application execution status
	 * 
	 * @param	value	application execution status
	 */
	public synchronized void setSTATOexecApp(String value) {
		STATOexecApp = value;		
	}
	
	/**
	 * To get application execution status
	 * 
	 * @return	application execution status
	 */
	public synchronized String getSTATOexecApp() {
		return STATOexecApp;
	}
	
	/**
	 * To set KEY inhibition indication
	 * 
	 * @param	value   KEY inhibition indication
	 */
	public synchronized void setInibizioneChiave(boolean value) {
		inibizioneChiave= value;		
	}
	
	/**
	 * To get KEY inhibition indication
	 * 
	 * @return	KEY inhibition indication
	 */
	public synchronized boolean getInibizioneChiave() {
		return inibizioneChiave;
	}
	
	/**
	 * To set if GPRS socket is active
	 * 
	 * @param	value	indication about GPRS socket active
	 */
	public synchronized void setIfsocketAttivo(boolean value) {
		socketAttivo = value;		
	}
	
	/**
	 * To get indication about GPRS socket is active
	 * 
	 * @return	indication about GPRS socket active
	 */
	public synchronized boolean getIfsocketAttivo() {
		return socketAttivo;
	}
	
	/**
	 * To set indication about tracking activation
	 * 
	 * @param	value	indication about tracking activation
	 */
	public synchronized void setTrackingAttivo(boolean value) {
		trackingAttivo = value;		
	}
	
	/**
	 * To get indication about tracking activation
	 * 
	 * @return	indication about tracking activation
	 */
	public synchronized boolean getTrackingAttivo() {
		return trackingAttivo;
	}
	
	/**
	 * To set indication about CSD activation
	 * 
	 * @param	value	indication about CSD activation
	 */
	public synchronized void setCSDattivo(boolean value) {
		CSDattivo = value;		
	}
	
	/**
	 * To get indication about CSD activation
	 * 
	 * @return	indication about CSD activation
	 */
	public synchronized boolean getCSDattivo() {
		return CSDattivo;
	}
	
	/**
	 * To set indication about SMS MOVE sent
	 * 
	 * @param	value	'true' if SMS MOVE sent, 'false' otherwise
	 */
	public synchronized void setSMSsent(boolean value) {
		SMSsent = value;		
	}
	
	/**
	 * To get indication about SMS MOVE sent
	 * 
	 * @return	'true' if SMS MOVE sent, 'false' otherwise
	 */
	public synchronized boolean getSMSsent() {
		return SMSsent;
	}
	
	/**
	 * To set awakening	type
	 * 
	 * @param	value	awakening type
	 */
	public synchronized void setTipoRisveglio(String value) {
		tipoRisveglio = value;		
	}
	
	/**
	 * To get awakening type
	 * 
	 * @return	awakening type
	 */
	public synchronized String getTipoRisveglio() {
		return tipoRisveglio;
	}
	
	/**
	 * To set a value of a configuration file parameter	(string)
	 * 
	 * @param	type	file parameter type
	 * @param	value	'String' value read from file
	 */
	public synchronized boolean setInfoFileString(String type, String value) {
		
		if (type.equalsIgnoreCase(IDtraker)) {
			trakerID = value;
		} //IDtraker
		
		else if (type.equalsIgnoreCase(PasswordCSD)) {
			pwCSD = value;
		} //PasswordCSD
		
		else if (type.equalsIgnoreCase(AppName)) {
			appname = value;
		} //AppName
		
		else if (type.equalsIgnoreCase(CloseMode)) {
			lastCloseMode = value;
		} //CloseMode
		
		else if (type.equalsIgnoreCase(LastGPSValid)) {
			lastGPSvalid = value;
		} //LastGPSValid
		
		else if (type.equalsIgnoreCase(LastGPRMCValid)) {
			lastGPRMCvalid = value;
		} //LastGPRMCValid
		
		else if (type.equalsIgnoreCase(TrackingInterv)) {
			try{
				int i = -1;
				trackTM = value;
				if((i = value.indexOf("s")) > 0){
					value = value.substring(0,i);				
					trackInterval = Integer.parseInt(value);
				}
				else{
					// convert minutes to seconds
					trackInterval = (Integer.parseInt(value))*60;
				}
			}catch (NumberFormatException e){
				return false;
			}catch (IndexOutOfBoundsException e){
				return false;
			}
		} //TrackingInterv
		
		else if (type.equalsIgnoreCase(DestHost)) {
			destHostFile = value;
		} //DestHost
		
		else if (type.equalsIgnoreCase(DestPort)) {
			destPortFile = value;
			if (value.equalsIgnoreCase("0")) enableGPRS=false;
			else enableGPRS=true;
		} //DestPort
				
		else if (type.equalsIgnoreCase(ConnProfileGPRS)) {
			connProfileGPRS = value;
		} //ConnProfileGPRS 
		
		else if (type.equalsIgnoreCase(apn)) {
			entryPointUDPfile = value;
		} //apn
		
		else if (type.equalsIgnoreCase(GPRSProtocol)) {
			gprsProtocolfile = value;
		} //Protocol
		else if (type.equalsIgnoreCase(TrackingType)) {
			typeTRK = value;
		} //Trktype
		else if (type.equalsIgnoreCase(TrackingProt)) {
			protTRK = value;
		} //TrkProtocol
		else if (type.equalsIgnoreCase(Header)) {
			header = value;
		} //TrkHeader
		else if (type.equalsIgnoreCase(Ackn)) {
			acknowledge = value;
		} //TrkAck
		else if (type.equalsIgnoreCase(GprsOnTime)) {
			try{
				int i = -1;
				gprsOnTm = value;
				if((i = value.indexOf("s")) > 0){
					value = value.substring(0,i);				
					gprsOT = Integer.parseInt(value);
				}
				else{
					// convero minuti in secondi
					gprsOT = (Integer.parseInt(value))*60;
				}
			}catch (NumberFormatException e){
				return false;
			}catch (IndexOutOfBoundsException e){
				return false;
			}
		} //GprsOnTime
		else if (type.equalsIgnoreCase(Operatore)) {
			opNum = value;
		} //Operator
		else if (type.equalsIgnoreCase(TrkState)) {
			trkState = value;
		} //TrkState
		else if (type.equalsIgnoreCase(PublishTopic)) {
			publishTopic = value;
		} //Topic
		else if (type.equalsIgnoreCase(SlpState)) {
			slp = value;
		} //Sleep
		else if (type.equalsIgnoreCase(MovState)) {
			movsens = value;
		} //Move sensor
		else if (type.equalsIgnoreCase(IgnState)) {
			ign = value;
		} //IgnState
		else if (type.equalsIgnoreCase(UartGateway)) {
			uGW = value;
		} //GateWay
		else if (type.equalsIgnoreCase(UartHeaderRS)) {
			uHead = value;
		} //UartHeader
		else if (type.equalsIgnoreCase(UartEndOfMessage)) {
			uEOMrs = value;
		} //EndOfMessageRS
		else if (type.equalsIgnoreCase(UartEndOfMessageIP)) {
			uEOMip = value;
		} //EndOfMessageIP
		else if (type.equalsIgnoreCase(UartIDdisp)) {
			uID = value;
		} //UartID
		else if (type.equalsIgnoreCase(InsensibilitaGPS)) {
			insensibilitaGPS = value;
		} //InsensibilitaGPS
		return true;
		
	}
	
	/**
	 * To set a value of a configuration file parameter	(integer)
	 * 
	 * @param	type	file parameter type
	 * @param	value	'int' value read from file
     */
	public synchronized void setInfoFileInt(String type, String value) {
		
		try {
		
			if (type.equalsIgnoreCase(TrackingInterv)) {
				trackInterval = Integer.parseInt(value);
			} //TrackingInterv

			else if (type.equalsIgnoreCase(OrePowerDownOK)) {
				orePowerDownOK = Integer.parseInt(value);
			} //OrePowerDownOK 
			
			else if (type.equalsIgnoreCase(StillTime)) {
				stilltm = Integer.parseInt(value);
			} //Stilltm
			
			else if (type.equalsIgnoreCase(MinPowerDownOK)) {
				minPdOK = Integer.parseInt(value);
			} //MinPowerDownOK
			
			else if (type.equalsIgnoreCase(OrePowerDownTOexpired)) {
				orePdTOexpired = Integer.parseInt(value);
			} //OrePowerDownTOexpired
			
			else if (type.equalsIgnoreCase(MinPowerDownTOexpired)) {
				minPdTOexpired = Integer.parseInt(value);
			} //MinPowerDownTOexpired
			else if (type.equalsIgnoreCase(UartSpeed)) {
				uSpeed = Integer.parseInt(value);
			} //UartSpeed
			else if (type.equalsIgnoreCase(UartAnswerTimeOut)) {
				uATO = Integer.parseInt(value);
			} //UartAnswerTimeOut
			else if (type.equalsIgnoreCase(UartNumTent)) {
				uNumT = Integer.parseInt(value);
			} //UartNumTent
			else if (type.equalsIgnoreCase(UartTXtimeOut)) {
				uTXto = Integer.parseInt(value);
			} //UartTXtimeOut
			else if (type.equalsIgnoreCase(TrkIN)) {
				contIN = Integer.parseInt(value);
			} //IN pointer
			else if (type.equalsIgnoreCase(TrkOUT)) {
				contOUT = Integer.parseInt(value);
			} //OUT pointer
		
		} catch (NumberFormatException nfe) {
			//System.out.println("InfoStato, setInfoFileInt: NumberFormatException");
		} //catch
	}

	/**
	 * To get a value of a configuration file parameter	(string)
	 * 
	 * @return	value of a configuration file parameter	(string)
	 */
	public synchronized String getInfoFileString(String type) {
		
		if (type.equalsIgnoreCase(Header)) {
			return header;
		} //Header
		
		else if (type.equalsIgnoreCase(IDtraker)) {
			return trakerID;
		} //IDtraker
		
		else if (type.equalsIgnoreCase(PasswordCSD)) {
			return pwCSD;
		} //PasswordCSD
		
		else if (type.equalsIgnoreCase(AppName)) {
			return appname;
		} //AppName
		
		else if (type.equalsIgnoreCase(CloseMode)) {
			return lastCloseMode;
		} //CloseMode
		
		else if (type.equalsIgnoreCase(LastGPSValid)) {
			return lastGPSvalid;
		} //LastGPSValid
		
		else if (type.equalsIgnoreCase(LastGPRMCValid)) {
			return lastGPRMCvalid;
		} //LastGPRMCValid
		
		else if (type.equalsIgnoreCase(DestHost)) {
			return destHostFile;
		} //DestHost
		
		else if (type.equalsIgnoreCase(DestPort)) {
			return destPortFile;
		} //DestPort
		
		else if (type.equalsIgnoreCase(ConnProfileGPRS)) {
			return connProfileGPRS;
		} //ConnProfileGPRS 
		
		else if (type.equalsIgnoreCase(apn)) {
			return entryPointUDPfile;
		} //apn
		
		else if (type.equalsIgnoreCase(GPRSProtocol)) {
			return gprsProtocolfile;
		} //Protocol
		
		else if (type.equalsIgnoreCase(TrackingType)) {
			return typeTRK;
		} //Tracking Type
		else if (type.equalsIgnoreCase(TrackingProt)) {
			return protTRK;
		} //TrkProtocol
		else if (type.equalsIgnoreCase(Ackn)) {
			return acknowledge;
		} //TrkAck
		else if (type.equalsIgnoreCase(GprsOnTime)) {
			return gprsOnTm;
		} //GprsOnTime
		else if (type.equalsIgnoreCase(Operatore)) {
			return opNum;
		} //Operator
		else if (type.equalsIgnoreCase(TrkState)) {
			return trkState;
		} //TrkState
		else if (type.equalsIgnoreCase(PublishTopic)) {
			return publishTopic;
		} //Topic
		else if (type.equalsIgnoreCase(SlpState)) {
			return slp;
		} //Sleep
		else if (type.equalsIgnoreCase(MovState)) {
			return movsens;
		} //Move sensor
		else if (type.equalsIgnoreCase(IgnState)) {
			return ign;
		} //IgnState
		else if (type.equalsIgnoreCase(UartGateway)) {
			return uGW;
		} //GateWay
		else if (type.equalsIgnoreCase(UartHeaderRS)) {
			return uHead;
		} //UartHeader
		else if (type.equalsIgnoreCase(UartEndOfMessage)) {
			return uEOMrs;
		} //EndOfMessageRS
		else if (type.equalsIgnoreCase(UartEndOfMessageIP)) {
			return uEOMip;
		} //EndOfMessageIP
		else if (type.equalsIgnoreCase(UartIDdisp)) {
			return uID;
		} //UartID
		else if (type.equalsIgnoreCase(TrackingInterv)) {
			return trackTM;
		} //TrackingInterv
		else if (type.equalsIgnoreCase(InsensibilitaGPS)) {
			return insensibilitaGPS;
		} //InsensibilitaGPS
		
		return "ERROR";
	}

	/**
	 * To get value of a configuration file parameter (integer)
	 * 
	 * @return	value of a configuration file parameter (integer)
	 */
	public synchronized int getInfoFileInt(String type) {
	
		if (type.equalsIgnoreCase(TrackingInterv)) {
			return trackInterval;
		} //TrackingInterv

		else if (type.equalsIgnoreCase(OrePowerDownOK)) {
			return orePowerDownOK;	
		} //OrePowerDownOK 
		
		else if (type.equalsIgnoreCase(MinPowerDownOK)) {
			return minPdOK;
		} //MinPowerDownOK
		
		else if (type.equalsIgnoreCase(StillTime)) {
			return stilltm;
		} //Stilltm
		
		else if (type.equalsIgnoreCase(OrePowerDownTOexpired)) {
			return orePdTOexpired;
		} //OrePowerDownTOexpired
		
		else if (type.equalsIgnoreCase(MinPowerDownTOexpired)) {
			return minPdTOexpired;
		} //MinPowerDownTOexpired
		else if (type.equalsIgnoreCase(UartSpeed)) {
			return uSpeed;
		} //UartSpeed
		else if (type.equalsIgnoreCase(UartAnswerTimeOut)) {
			return uATO;
		} //UartAnswerTimeOut
		else if (type.equalsIgnoreCase(UartNumTent)) {
			return uNumT;
		} //UartNumTent
		else if (type.equalsIgnoreCase(UartTXtimeOut)) {
			return uTXto;
		} //UartTXtimeOut
		else if (type.equalsIgnoreCase(TrkIN)) {
			return contIN;
		} //IN pointer
		else if (type.equalsIgnoreCase(TrkOUT)) {
			return contOUT;
		} //OUT pointer
		else if (type.equalsIgnoreCase(GprsOnTime)) {
			return gprsOT;
		} //GprsOnTime
		
		return 0;
	}
	
	/**
	 * To set if application is in STAND-BY
	 * 
	 * @param	value	'true' if application is in STAND-BY, 'false' otherwise
	 */
	public synchronized void setSTANDBY(boolean value) {
		appSTANDBY = value;		
	}
	
	/**
	 * To get if application is in STAND-BY
	 * 
	 * @return	'true' if application is in STAND-BY, 'false' otherwise
	 */
	public synchronized boolean getSTANDBY() {
		return appSTANDBY;
	}
	
	/**
	 * To set CSD enabled
	 * 
	 * @param	value	'true' if enabled, 'false' otherwise
	 */
	public synchronized void setEnableCSD(boolean value) {
		enableCSD = value;		
	}
	
	/**
	 * To get CSD enabled indication
	 * 
	 * @return	'true' if enabled, 'false' otherwise
	 */
	public synchronized boolean getEnableCSD() {
		return enableCSD;
	}
	
	/**
	 * To get GPRS enable indication 
	 * 
	 * @return	'true' if enabled, 'false' otherwise
	 */
	public synchronized boolean getEnableGPRS() {
		return enableGPRS;
	}
	
	/**
	 * To set if made a CSD connection
	 * 
	 * @param	value	'true' if made a CSD connection, 'false' otherwise
	 */
	public synchronized void setCSDconnect(boolean value) {
		CSDconnect= value;		
	}
	
	/**
	 * To get if made a CSD connection
	 * 
	 * @return	'true' if made a CSD connection, 'false' otherwise
	 */
	public synchronized boolean getCSDconnect() {
		return CSDconnect;
	}
	
	/**
	 * To set battery voltage value
	 * 
	 * @param	value	battery voltage value
	 */
	public synchronized void setBatteryVoltage(String value) {
		batteryVoltage = value;		
	}
	
	/**
	 * To get battery voltage value
	 * 
	 * @return	battery voltage value
	 */
	public synchronized String getBatteryVoltage() {
		return batteryVoltage;
	}
	
	/**
	 * To set a parameter returned by SMS
	 * 
	 * @param	value	parameter returned by SMS
	 * @param   cod		parameter code
	 */
	public synchronized void setDataSMS(String value, int cod) {
		
		switch(cod) {
			case 1: value1 = value; break;
			case 2: value2 = value; break;
			case 3: value3 = value; break;
			case 4: value4 = value; break;
			case 5: value5 = value; break;
			case 6: value6 = value; break;
			case 7: value7 = value; break;
			default: break;	
		}
	}
	
	/**
	 * To get a parameter returned by SMS
	 * 
	 * @return	parameter returned by SMS
	 */
	public synchronized String getDataSMS(int cod) {
		
		switch(cod) {			
			case 1: return value1; // DATA dd-mm-aa			
			case 2: return value2; // ORA  hh:mm:ss			
			case 3: return value3; // LAT  dd pp' ss''.cc N/S			
			case 4: return value4; // LON  dd pp' ss''.cc E/W			
			case 5: return value5; // ROT			
			case 6: return value6; // ALT
			case 7: return value7; // VEL kmh
			default: return "ERROR";
		}
	}
	
	/**
	 * To set indication about WatchDog activation on CSD call
	 * 
	 * @param	value	indication about WatchDog activation on CSD call
	 */
	public synchronized void setCSDWatchDog(boolean value) {
		CSDWatchDog = value;		
	}
	
	/**
	 * To get indication about WatchDog activation on CSD call
	 * 
	 * @return	indication about WatchDog activation on CSD call
	 */
	public synchronized boolean getCSDWatchDog() {
		return CSDWatchDog;
	}
	
	/**
	 * To set 'dataGPRMC' and 'oraGPRMC' values
	 * 
	 * @param	data	value of 'dataGPRMC'
	 * @param	ora 	value of 'oraGPRMC'
	 */
	public synchronized void setDataOraGPRMC(String data, String ora) {
		dataGPRMC = data;
		oraGPRMC = ora;	
	}
	
	/**
	 * To get 'dataGPRMC' value
	 * 
	 * @return value of 'dataGPRMC'
	 */
	public synchronized String getDataGPRMC() {
		return dataGPRMC;		
	}
	
	/**
	 * To get 'oraGPRMC' value
	 * 
	 * @return value of 'oraGPRMC'
	 */
	public synchronized String getOraGPRMC() {
		return oraGPRMC;		
	}
	
	/**
	 * To get tracking status
	 * 
	 * @return value of 'trackingInCorso'
	 */
	public synchronized boolean getTRKstate() {
		return trackingInCorso;		
	}
	
	/**
	 * To set tracking status
	 * 
	 * @param value		value of tracking status
	 */
	public synchronized void setTRKstate(boolean value) {
		trackingInCorso = value;		
	}
	
	/**
	 * To get indication about normal startup after +CALA
	 * 
	 * @return	true if reboot needed
	 */
	public synchronized boolean getCALA() {
		return cala;
	}
	
	/**
	 * To set indication about normal startup after +CALA
	 * 
	 * @return	true if reboot needed
	 */
	public synchronized void setCALA(boolean x) {
		cala = x;
	}
	
	/**
	 * To get a field of a string with commas as delimiters.
	 * <BR>
	 * If 'numDelim=0' returns the string from the beginning up to the first
	 * comma has been found.
	 * <BR>
	 * If it doesn't find a comma pulls up to the end of the string.
	 * <BR>
	 * 'virg' field determines whether the extracted field must be present
	 * quotation marks or not.
	 * 
	 * @param	orig		original string
	 * @param	numDelim	number of initial comma about field to extract
	 * @param 	virg		indicates quotation marks presence in the extracted
	 *                      field, 'false' = there shouldn't be quotation marks
	 * 
	 * @return	requested field, in the desired format
	 */
	public synchronized String campo(String orig, int numDelim, boolean virg) {
		try {
			
			//System.out.println("InfoStato, campo, orig: " + orig);
			
			/*
			 * If initial comma is at 0, then I extract string to the character
			 * before the first comma found, if present
			 */
			if (numDelim == 0) {
				// If there is a comma in the string -> OK
				if (orig.indexOf(virgola) >=0) {
					temp = orig.substring(0, orig.indexOf(virgola));
				}
				// If there isn't a comma in the string -> does nothing,
				// returns the string as it is
				else temp = orig;
				
				//System.out.println("InfoStato, campo, temp n.1/A: " + temp);
			} //if numDelim
			
			/*
			 * If initial comma isn't at 0, then run the normal procedure
			 */
			else {
				// copy the original string
				temp = orig;
				
				// Repeat as many times as the comma 'numDelim'
				for (int i=1; i <= numDelim; i++) {
					// Extract chars from comma onwards
					temp = temp.substring(temp.indexOf(virgola) +1);
				}//for

				//System.out.println("InfoStato, campo, temp n.1/B: " + temp);
			} //else
			
			/*
			 * Identify the end of the field, depending on whether or not
			 * there is a comma
			 */ 
			if (temp.indexOf(virgola) >=0) {
				temp = temp.substring(0, temp.indexOf(virgola));
			}
			
			//System.out.println("InfoStato, campo, temp n.2: " + temp);
			
			/*
			 * Check the quotation marks and if I have to delete them
			 */
			if (virg == false && temp.indexOf(virgolette) >=0) {
				temp = temp.substring(1);	// remove the first
				temp = temp.substring(0, temp.indexOf(virgolette));	// remove the second
			} // if virg
			
			//System.out.println("InfoStato, campo, temp n.3: " + temp);
			
		} catch (StringIndexOutOfBoundsException ex) {
			//System.out.println("InfoStato, campo: StringIndexOutOfBoundsException");
			temp = "ERROR";
		} catch (NullPointerException npe) {
			//System.out.println("InfoStato, campo: NullPointerException");
			temp = "ERROR";
		} catch (Exception e) {
			//System.out.println("InfoStato, campo: Eccezione generica");
			temp = "ERROR";
		} //catch
		
		// return value
		return temp;
	}
	
	/**
	 * public boolean checkComma(int comma, String text)
	 * 
	 * Method that check how comma contain the text
	 * 
	 * @param	comma: commas number to check
	 * @param	text: text where search commas
	 * @return	true if number is OK, false otherwise
	 */
	
	public boolean checkComma(int comma, String text){
		
		int count = 0;
		int s = 0;
		for(int i = 0; i < comma; i++){
			try{
				if((s = text.indexOf(",")) != -1){
					count++;
					text = text.substring(s+1);
				}
				else
					return false;
			}catch(NullPointerException e){
				return false;
			}catch(IndexOutOfBoundsException e){
				return false;
			}
			
		}
		if(count == comma)
			return true;
		return false;
	}
	
	public synchronized String getDataRAM(){
		return dataRAM;
	}
	
	public synchronized void setDataRAM(String data){
		dataRAM = data;
	}
	public synchronized String[] getDataMqttRAM(){
		return dataMqttRAM;
	}
	
	public synchronized void setDataMqttRAM(String[] data){
		dataMqttRAM = data;
	}
	public synchronized void setReboot(){
		riavvia = true;
	}
	
	public synchronized boolean getReboot(){
		return riavvia;
	}
	
	public synchronized void setCanbus(boolean state){
		canbusState = state;
	}

	public synchronized boolean getCanbus(){
		return canbusState;
	}
	
	public synchronized void setGPSLive(boolean state){
		
		gpsLive = state;
	}
	
	public synchronized boolean getGPSLive(){
		
		return gpsLive;
	}
	
	public synchronized int setX(int data){
		dataX = data;
		return data;
	}
	public synchronized int getX(){
		return dataX;
	}
	public synchronized int setY(int data){
		dataY = data;
		return data;
	}
	public synchronized int getY(){
		return dataY;
	}
	public synchronized int setZ(int data){
		dataZ = data;
		return data;
	}
	public synchronized int getZ(){
		return dataZ;
	}
	public synchronized void setAlarmCrash(boolean x){
	
		crashDetect = x;
	}
	public synchronized boolean getAlarmCrash(){
		
		return crashDetect;
	}
	public synchronized void setCoordinate(int x, int y, int z){
		
		asseX = x;
		asseY = y;
		asseZ = z;
		
	}
	public synchronized String getCoordinate(){
		
		return "Allarme: " + "X=" + asseX + ", Y=" + asseY + ", Z=" + asseZ;
	}
	
	public synchronized void closeTrackingGPRS(){
		
		closeTrack = true;
	}
	
	public synchronized boolean isCloseTrackingGPRS(){
		
		return closeTrack;
	}

	public synchronized void closeUDPSocketTask(){
	
		closeUDP = true;
	}
	
	public synchronized boolean isCloseUDPSocketTask(){
		
		return closeUDP;
	}
	
	public synchronized void closeTCPSocketTask(){
		
		closeTCP = true;
	}
	
	public synchronized boolean isCloseTCPSocketTask(){
		
		return closeTCP;
	}
	
	public synchronized void setGPRSBearer(int state){
		bearer = state;
	}
	
	public synchronized int getGPRSBearer(){
		return bearer;
	}
	
	public synchronized void setCREG(String state){
		creg = state;
	}
	
	public synchronized String getCREG(){
		return creg;
	}
	
	public synchronized void setCGREG(String state){
		cgreg = state;
	}
	
	public synchronized String getCGREG(){
		return cgreg;
	}
	
	public synchronized void setERROR(int counter){
		errorGPS = counter;
	}
	
	public synchronized int getERROR(){
		return errorGPS;
	}


	public synchronized boolean getAlarm() {
		return alarmNack;
	}

	public synchronized void setAlarm(boolean b) {
		alarmNack = b;
	}
	
	public synchronized void setTask1Timer(int x){
		t1 = x;
	}
	public synchronized int getTask1Timer(){
		return t1;
	}
	
	public synchronized void setTask2Timer(int x){
		t2 = x;
	}
	public synchronized int getTask2Timer(){
		return t2;
	}
	
	public synchronized void setTask3Timer(int x){
		t3 = x;
	}
	public synchronized int getTask3Timer(){
		return t3;
	}
	
	public synchronized void setGpsLed(boolean x){
		
		gpsLed = x;
		
	}
	
	public synchronized boolean getGpsLed(){
		
		return gpsLed;
		
	}
	public synchronized void setGpsState(boolean x){
		
		gps_state = x;
	
	}
	
	public synchronized boolean getGpsState(){
		
		return gps_state;
	
	}
	
	public synchronized void setPowerSupply(boolean c){
		
		powerSupply = c;
	}
	
	public synchronized boolean getPowerSupply(){
		
		return powerSupply;
	
	}
	
	public synchronized void setPowerSupplyOff(boolean c){
		
		powerSupplyOff = c;
	}
	
	public synchronized boolean getPowerSupplyOff(){
		
		return powerSupplyOff;
	
	}
	
	public synchronized void setVIN(double c){
		
		String tempPowerVoltage = Double.toString(c);
		try{
			if(tempPowerVoltage.length()>6)
				powerVoltage = tempPowerVoltage.substring(0,5);
			else
				powerVoltage = tempPowerVoltage;
		}catch(IndexOutOfBoundsException ie){
			powerVoltage = tempPowerVoltage;
		}
	}
	
	public synchronized String getVIN(){
		
		return powerVoltage;
	
	}
} //InfoStato
