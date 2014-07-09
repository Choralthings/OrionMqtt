/*	
 * Class 	GlobCost
 * 
 * This software is developed for Choral devices with Java.
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

/**
 * Global constants common for all application classes,
 * to use these constants just implement this interface.
 * 
 * @version	1.05 <BR> <i>Last update</i>: 05-12-2007
 * @author 	alessioza
 * 
 */
public interface GlobCost {
	
	/** Test environment
	 * 	 true  = test
	 * 	 false = normal operation of the application
	 */
	public static final boolean	TESTenv = false;
	public static final boolean movsens = false;
	boolean debug = false;
	boolean debugGSM = false;
	boolean debug_chiave = false;
	boolean debug_main = false;
	boolean debug_speed = false;
	boolean usbDebug = false; 
	boolean mqttParser = false; 
	//remember to modify empty string "defaultGPS" and "posusr" for tests
	
	public static final String	NMEA 	= "NMEA";
	public static final String	CHORAL 	= "CHORAL";
	
	/** Choice of sent position strings format */
	public static final String	GPRSposFormat = CHORAL;
	public static final String	CSDposFormat  = CHORAL;
	// on file set always CHORAL format
	
	/** Repeat intervals for while loops in [ms] */
	public static final int 	whileSleep 		= 100;	// standard while loops
	public static final int		satViewSleep	= 600;	// number of ticks to multiply with 'whileSleep' to obtain 30 seconds
	public static final int		satViewBeetween	= 10;	// number of ticks to multiply with 'whileSleep' to obtain  1 second
	public static final int 	whileSleepGPIO 	= 1000;	// GPIO test while loops
	public static final int		SMSsleep 		= 5000;
	
	/** Crash detect threshold **/
	public static final int		minThreshold 		= -1500;
	public static final int		maxThreshold 		= 1500;
	
	/** Alarm enable */
	public static final boolean alarmEnable	= true;
	
	public static final String 	recordStoreName 	= "GWimpostazioniRS";
	public static final String 	recordPOS 			= "Pos_RS";
	//public static final String 	recordPOS 			= "Pos_Prova";
	public static final String 	fileName 			= "GWmqttSettings.txt";
	public static final String 	revNumber 			= "1.23";
	public static final String 	dataRev 			= "27/06/2014";
	public static final String 	moduleCodeRev		= "EGS5 rev. ";
	
	/** Default messages */
	public static final String 	msgClose 				= "closeApp";
	public static final String 	msgResponseATOK 		= "ResponseAT,OK";
	public static final String 	msgFIX 					= "validFIX";
	public static final String 	msgFIXgprs 				= "validFIXgprs";
	public static final String 	msgFIXtimeoutEXPIRED 	= "FIXtimeoutEXPIRED";
	public static final String 	msgFIXgprsTimeoutEXPIRED = "FIXgprsTimeoutEXPIRED";
	public static final String 	msgChiaveAttivata 		= "KeyActivated";
	public static final String 	msgChiaveDisattivata 	= "KeyDeactivated";
	public static final String 	msgRING					= "RING";
	public static final String 	msgBattScarica			= "LowBattery";
	public static final String 	msgSMStrack				= "TrackingFromSMS";
	public static final String  msgCloseGPRS 			= "closeGPRS";
	public static final String  rebootTrack				= "rebootTrack";
	public static final String  msgREBOOT				= "RebootAIR";
	public static final String	msgALR1					= "alarm1";
	public static final String	msgALR2					= "alarm2";
	public static final String	msgALIVE				= "alive";
	// for GPRS Tracking
	public static final String 	trackNormale			= "TrackingNormal";
	public static final String 	trackAttivChiave		= "TrackingAfterKeyActivation";
	public static final String 	trackDisattivChiave		= "TrackingAfterKeyDeactivation";
	public static final String 	trackMovimento			= "TrackingMotionSensor";
	public static final String 	trackBatteria			= "TrackingLowBattery";
	public static final String 	trackAlarmIn1			= "trackAlarmIn1";
	public static final String 	trackAlarmIn2			= "trackAlarmIn2";
	public static final String 	trackAlive				= "trackAlive";
	public static final String 	trackSMS				= "TrackingFromSMS";
	public static final String  timeoutExpired			= "TimeoutEXPIRED";
	public static final String  invioCompletato			= "SendGPRSstringsCompleted";
	public static final String  stopTrack				= "SospendiTracking";
	public static final String  exitTrack				= "EsciDalTracking";
	public static final String  gprsTimeoutStart		= "gprsTimeoutStart";
	public static final String  gprsTimeoutStop			= "gprsTimeoutStop";
	public static final String 	trackCodice				= "trackCode";
	public static final String 	trackUrcSim				= "trackUrcSim";
	/** Keyword to send a valid command by SMS */
	public static final String	keySMS	= "gps";
	public static final String	keySMS1	= "reboot";
	public static final String	keySMS2	= "state";
	
	/** Settings for serial interface gps0 */
	public static final String  COMgps = "comm:com1;baudrate=9600;bitsperchar=8;blocking=on";
	
	/** Settings for serial interface ASC0 */
	public static final String  ASC0 = "comm:com0;baudrate=115200;bitsperchar=8;blocking=on";
		
	/* Timeout settings */
	// Timeout types
	public static final String 	FIXgpsTimeout 	= "FIXgpsTimeout";
	public static final String 	FIXgprsTimeout 	= "FIXgprsTimeout";
	public static final String  BatteryTimeout	= "BatteryTimeout";
	public static final String  RegTimeout		= "RegTimeout";
	public static final String  WatchDogTimeout	= "WatchDogTimeout";
	public static final String  CSDtimeout		= "CSDTimeout";
	public static final String  CHIAVEtimeout	= "TimeoutDisabilitazioneChiave";
	public static final String  trackTimeout	= "TimeoutTrackingCHIAVE";
	
	/** Inhibition KEY duration after GPRS request (in seconds)*/
	public static final int		durataInibCHIAVE	= 10;
	
	/** Range for battery level control (in seconds)*/
	public static final int		batteryTOvalue	= 60;
	
	/** Range for network registration control (in seconds)*/
	public static final int		regTOvalue	= 30;
	
	/** Range for WatchDog control (in seconds)*/
	public static final int		WatchDogTOvalue	= 100;
	
	/** Range for CSD WatchDog control (in seconds)*/
	public static final int		CSDTOvalue		= 30;
	
	/** Battery low threshold */
	public static final int		VbattSoglia = 3599;
	
	/** Time for motion sensor activation (in ms)*/
	public static final int		sensMovSetupTime	= 50;
	public static final int		sensMovHoldTime		= 2000;
	
	//if test_choral
	//public static final String defaultGPS = ",V,00,000000,000000,0000.00000,N,00000.00000,E,0.0,00.0,4.3V,B,00,00,00000000";
	//otherwise
	public static final String defaultGPS = ",V,00,000000,000000,0000.00000,N,00000.00000,E,0.0,0.0,00.0,000000,4.3V,B,00,00,00000000";

	
	/** ALIAS for file settings */
	public static final String	IDtraker				= "DeviceID";
	public static final String	PasswordCSD				= "PasswordCSD";
	public static final String 	AppName					= "AppName";
	public static final String 	CloseMode				= "CloseMode";
	public static final String 	LastGPSValid			= "LastGPSValid";
	public static final String	TrackingInterv  		= "TrackingInterval";
	public static final String	Operatore		  		= "Operatore";
	public static final String  TrackingType			= "TrackingType";
	public static final String  TrackingProt			= "TrackingProt";
	public static final String	Header					= "Header";
	public static final String  Ackn					= "Ackn";
	public static final String  GprsOnTime				= "GprsOnTime";
	public static final String	TrkState  				= "TrkState";
	public static final String	PublishTopic			= "PublishTopic";
	public static final String	SlpState		  		= "SlpState";
	public static final String	StillTime		  		= "StillTime";
	public static final String	MovState		  		= "MovState";
	public static final String	IgnState		  		= "IgnState";
	public static final String  UartSpeed				= "UartSpeed";
	public static final String  UartGateway				= "UartGateWay";
	public static final String  UartHeaderRS			= "UartHeaderRS";
	public static final String  UartEndOfMessage		= "UartEndOfMessage";
	public static final String  UartAnswerTimeOut		= "UartAnswerTimeOut";
	public static final String  UartNumTent				= "UartNumTent";
	public static final String  UartEndOfMessageIP		= "UartEndOfMessageIP";
	public static final String  UartIDdisp				= "UartIDdisp";
	public static final String  UartTXtimeOut			= "UartTXtimeOut";
	public static final String	OrePowerDownOK			= "HoursPowerDownOK";
	public static final String 	MinPowerDownOK 			= "MinPowerDownOK";
	public static final String	OrePowerDownTOexpired	= "HoursPowerDownTOexpired";
	public static final String 	MinPowerDownTOexpired 	= "MinPowerDownTOexpired";
	public static final String 	DestHost				= "DestHost";
	public static final String 	DestPort 				= "DestPort";
	public static final String 	ConnProfileGPRS 		= "ConnProfileGPRS";
	public static final String 	apn	 					= "apn";
	public static final String  GPRSProtocol			= "GPRSProtocol";
	public static final String  TrkIN					= "TrkIN";
	public static final String  TrkOUT					= "TrkOUT";
	//DFS our Alias
	public static final String InsensibilitaGPS			= "InsensibilitaGPS";
	//Fine **
		
	/** ALIAS for flash settings */
	public static final String 	LastGPRMCValid			= "LastGPRMCValid";
	
	/** Application execution status */
	public static final String 	execFIRST				= "FirstExecution";
	public static final String 	execNORMALE				= "NormalExecution";
	public static final String 	execCHIAVEattivata 		= "KeyOnExecution";
	public static final String 	execTrack 				= "Tracking";
	public static final String	execCHIAVEdisattivata 	= "KeyOffExecution";
	public static final String 	execMOVIMENTO 			= "MoveExecution";
	public static final String 	execPOSTRESET 			= "PostResetExecution";
	public static final String 	execBATTSCARICA			= "BatteriaScaricaExecution";
	public static final String 	execSMStrack			= "TrackingFromSMSExecution";
	
	/** Application closure modes */
	public static final String 	closeAppFactory					= "Factory";
	public static final String 	closeAppDisattivChiaveOK 		= "DisattivChiaveOK";
	public static final String 	closeAppDisattivChiaveTimeout 	= "DisattivChiaveTimeout";
	public static final String 	closeAppDisattivChiaveFIRST 	= "DisattivChiaveFIRST";
	public static final String 	closeAppNormaleOK				= "NormaleOK";
	public static final String 	closeAppNormaleTimeout 			= "NormaleTimeout";
	public static final String 	closeAppMovimentoOK 			= "SensMovOK";
	public static final String 	closeAppResetHW 				= "ResetHW";
	public static final String 	closeAppBatteriaScarica 		= "BatteriaScarica";
	public static final String 	closeAppPostReset				= "DisattivChiavePostReset";
	public static final String 	closeAppOTAP					= "OTAP";
	public static final String  closeAIR						= "closeAIR";
	
	/** Awakening mode of tracker */
	public static final String 	risveglioCala			= "RisveglioCala";
	public static final String 	risveglioMovimento 		= "RisveglioSensoreMovimento";
	public static final String 	risveglioChiave			= "RisveglioChiave";
	
	/** Alarm messages */
	public static final String 	alarmInitSetup			= "INIT_SETUP";
	public static final String  alarmChiaveDisattivata	= "KEY_OFF";
	public static final String  alarmChiaveAttivata		= "KEY_ON";
	public static final String  alarmMovimento			= "MOVE";
	public static final String  alarmReset				= "RESET_HW";
	public static final String  alarmCALA				= "CALA";
	public static final String  alarmBatteria			= "BATTERY_UNDERVOLTAGE";
	public static final String  alarmSMStrack			= "SMS_TRACK";
	public static final String  alarmIn1				= "ALARM IN1";
	public static final String  alarmIn2				= "ALARM IN2";
	public static final String  alive					= "ALIVE";
	
	/** dataStore types */
	public static final String  dsDRMC	= "dsDRMC";
	public static final String  dsDGGA	= "dsDGGA";
	public static final String  dsTRMC	= "dsTRMC";
	public static final String  dsTGGA	= "dsTGGA";
	
	/** Empty string */
	public static final String 	vuota	= "";
	public static final String	virgola = ",";
	public static final String	virgolette = "\"";
	
	/** Bearer states */
	public static final String BSclosing	= "BearerState_Closing";
	public static final String BSconnecting	= "BearerState_Connecting";
	public static final String BSdown		= "BearerState_Down";
	public static final String BSlimitedUp	= "BearerState_LimitedUp";
	public static final String BSup			= "BearerState_UP";
	
	/** Commands to ATSender different from AT commands */
	public static final String csdOpen		= "OPEN-CSD";
	public static final String csdWrite		= "WRITE-CSD";
	public static final String csdRead		= "READ-CSD";
	
	/** Commands to send through CSD and UART */
	public static final String ACTOP				= "#ACTOP";
	public static final String CFG					= "#CFG";
	
	public static final String CHPWD				= "#CHPWD";
	public static final String CHPWDconfirm			= "Confirm new password: ";
	public static final String CHPWDok				= "#CHPWD:OK\r\n\r\n";
	public static final String CHPWDerr				= "#CHPWD:ERR\r\n\r\n";
	public static final String CHPWDlong			= "#CHPWD:ERR, maximum length 15 characters\r\n\r\n";
	
	public static final String csdCLOSE				= "#CLOSE";
	
	public static final String GPRSCFG				= "#GPRSCFG";
	public static final String IGNCFG				= "#IGNCFG";
	public static final String MOVSENS				= "#MOVSENS";
	public static final String POSREP				= "#POSREP";
	public static final String POSUSR				= "#POSUSR";
	public static final String PROTOCOLCFG			= "#PROTOCOLCFG";
	
	public static final String PWD					= "#PWD";
	public static final String PWDok				= "#PWD:OK\r\n\r\n";
	public static final String PWDerr				= "#PWD:ERR,\r\n\r\n";
	
	public static final String REBOOT				= "#REBOOT";
	public static final String REQIO				= "#REQIO";
	public static final String SETID				= "#SETID";
	public static final String SIG					= "#SIG";
	public static final String SLP					= "#SLP";
	public static final String SLPTM				= "#SLPTM";
	public static final String SNOP					= "#SNOP";
	public static final String PUBTOPIC				= "#PUBTOPIC";
	public static final String STILLTM				= "#STILLTM";
	public static final String TRK					= "#TRK";
	public static final String TRKCFG				= "#TRKCFG";
	public static final String TRKTM				= "#TRKTM";
	public static final String UARTCFG				= "#UARTCFG";
	public static final String VBAT					= "#VBAT";
	public static final String EE_GET_PTR			= "#EE_GET_PTR";
	public static final String SETSPEED				= "#SETSPEED";
	
	public static final String ACK					= "#ACK_OK\r\n\r\n";
	public static final String NACK					= "#ACK_ERR\r\n\r\n";
	
	public static final String logREAD		= "@LOG";
	public static final String logDELETE	= "@DELLOG";
	public static final String OLDlogREAD	= "@OLDLOG";
	public static final String logEND		= "\r\n@END\r\n\r\n";
	public static final String logQUEUE		= "@CODA";
	
	
	//DFS 
	public static final String csdSETINSENSIBILITAGPS		= "#SETINSENSIBILITAGPS";
	public static final String csdSETINSENSIBILITAGPSok 	= "#SETINSENSIBILITAGPS:OK\n\r";
	public static final String csdSETINSENSIBILITAGPSerr	= "#SETINSENSIBILITAGPS:ERR\n\r";
	
	public static final int codaSize = 100;
	
	// MQTT
	
	public static final int HEADER = 0;
	public static final int DEVICE_ID = 1;
	public static final int GPS_VALID_DATA = 2;
	public static final int NUM_SAT = 3;
	public static final int DATE = 4;
	public static final int TIME = 5;
	public static final int LAT = 6;
	public static final int NS = 7;
	public static final int LON = 8;
	public static final int WE = 9;
	public static final int COURSE = 10;
	public static final int SPEED = 11;
	public static final int ALT = 12;
	public static final int DIST = 13;
	public static final int VBATT = 14;
	public static final int EB = 15;
	public static final int DIN = 16;
	public static final int DOUT = 17;
	public static final int AIN = 18;
	public static final int ALR_IND = 19;
	
} //GlobCost

