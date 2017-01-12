/* Class 	AppMain
 * 
 * This software is developed for Choral devices with Java
 * Copyright Choral srl. All Rights reserved. 
 */

package general;

import javax.microedition.midlet.*;
import javax.microedition.rms.*;

import choral.io.CheckUpgrade;
import choral.io.PowerManager;

import java.io.IOException;
import java.util.*;

/**
 * Main application class.
 * <BR>
 * startApp() method contains the main body of the application.
 *
 * @version	1.00 <BR> <i>Last update</i>: 28-05-2008
 * @author 	matteobo
 */
public class AppMain extends MIDlet implements GlobCost {
	
	/*
	 * local variables
	 */
	private String text;
	private String 	msgRicevuto = "";
	private boolean GOTOstagePwDownSTANDBY = false;
	private int	countTimerResetGPS = 1;
//	private int	countTimerStartGPS = 1;
	private int	countTimerStopGPS  = 1;
	private int	countTimerResetGPRS = 1;
//	private int	countTimerStartGPRS = 1;
	private int	countTimerStopGPRS  = 1;
	private boolean elabBATT, elabPWD = false;
//	private int TOgprsMovelength;
//	private boolean trackFIRST = false;
//	private boolean trackSMSenable = false;
//	private boolean checkCHIAVE = false;
	private boolean watchdog = true;
	private boolean PowDown = false;
	private boolean restart = false;
	private boolean puntatori = false;
	PowerManager pwr;
	int timeToOff = 0;
	
	/** <BR> Structure with main informations about application. */
	InfoStato infoS;
	
	/*
	 * threads
	 */
	/** <BR> Thread for GPS receiver management (transparent mode) and position strings creation. */
	CommGPStrasparent 	th1;
	/** <BR> Thread for AT interface management. */
	ATsender 			th2;
	/** <BR> Thread for sending position strings through GPRS connection (TCP or UDP). */
	TrackingGPRS    	th3;
	/** <BR> Thread for set module in POWER DOWN mode. */
	GoToPowerDown	  	th4;
	/** <BR> Thread for key management. */
	TestChiave			th5;
	/** <BR> Thread for GPIO management (excluding key). */
	GPIOmanager			th6;
	/** <BR> Thread for update configuration parameters through CSD call. */
	UpdateCSD			th7;
	/** <BR> Thread for (received) SMS management. */
	CheckSMS			th8;
	/** <BR> Thread for serial (ASC0) management. */
	Seriale				th9;
	/** <BR> Thread for TCP socket management. */
	SocketGPRStask		th10;
	/** <BR> Thread for UDP socket management. */
	UDPSocketTask		th11;
	/** <BR> Thread for crash detect management. */
	//AccelerometerTask	th12;
	
	/*
	 * mailboxes
	 */
	/** <BR> Mailbox for AppMain */
	Mailbox 	mboxMAIN;
	/** <BR> Mailbox for CommGPStrasparent */
	Mailbox 	mbox1;
	/** <BR> Mailbox for ATsender */
	Mailbox 	mbox2;
	/** <BR> Mailbox for TrackingGPRS */
	Mailbox 	mbox3;
	/** <BR> Mailbox for GoToPowerDown */
	Mailbox 	mbox4;
	/** <BR> Mailbox for SocketGPRStask and UDPSocketTask */
	Mailbox 	mbox5;
	
	/*
	 * datastore
	 */
	/** <BR> Datastore for save position strings. */
	DataStore 	dsDataRMC;
	DataStore	dsDataGGA;
//	Coda choralQueue;
	/** <BR> Datastore for save tracking position strings. */
	DataStore 	dsTrkRMC;
	DataStore	dsTrkGGA;
//	Coda NMEAQueue;
	
	/*
	 * semaphores
	 */
	/** <BR> Semaphore to regulate use of AT interface. */
	PrioSem 	semAT;
	
	/*
	 * file and recordstore
	 */
	/** <BR> Manager for save and take data from configuration file. */
	FlashFile 	file;
	SaveData	save;
	
	/*
	 * timer and tasks about GPS, GPRS e battery control
	 */
	/** <BR> Timer for FIX GPS timeout management. */
	Timer 		FIXgpsTimer;
	private boolean gpsTimerAlive = false;
	/** <BR> Timer for GPRS timeout management. */
	Timer 		FIXgprsTimer;
//	private boolean gprsTimerAlive = false;
	/** <BR> Task for execute operations when GPS timeout expires. */
	TimeoutTask FIXgpsTimeoutTask;
	/** <BR> Task for execute operations when GPRS timeout expires. */
	TimeoutTask FIXgprsTimeoutTask;
	/** <BR> Timer for cyclic monitoring of battery level. */
	Timer 		batteryTimer;
	/** <BR> Task for execute operations about battery level monitoring. */
	TimeoutTask batteryTimeoutTask;
	/** <BR> Timer for cyclic monitoring of network registration status. */
	Timer 		regTimer;
	/** <BR> Task for execute operations about network registration status monitoring. */
	TimeoutTask regTimeoutTask;
	/** <BR> Timer for cyclic monitoring of WatchDog. */
	Timer 		WatchDogTimer;
	/** <BR> Task for execute operations about WatchDog. */
	TimeoutTask WatchDogTimeoutTask;
	/** <BR> Timer for delay tracking start when key is activated. */
	Timer 		trackTimer;
	private boolean trackTimerAlive = false;
	/** <BR> Task for delay tracking start when key is activated. */
	TimeoutTask trackTimeoutTask;
	
	
	/*
	 * constructors 
	 */
	
	public AppMain() {
		
		CheckUpgrade fw = new CheckUpgrade("");
		
		if(debug){
			System.out.println("AppMain: starting...");
		}
		// Status info strcture creation
		infoS = new InfoStato();
		// Threads creation
		th1	= new CommGPStrasparent();
		th2 = new ATsender();
		th3	= new TrackingGPRS();
		th4	= new GoToPowerDown();
		th5 = new TestChiave();
		th6 = new GPIOmanager();
		th8 = new CheckSMS();
		th9 = new Seriale();
		th10 = new SocketGPRStask();
		th11 = new UDPSocketTask();
		//th12 = new AccelerometerTask();
		// Mailboxes creation
		mboxMAIN = new Mailbox(20);
		mbox1 	 = new Mailbox(20);
		mbox2 	 = new Mailbox(20);
		mbox3 	 = new Mailbox(20);
		mbox4 	 = new Mailbox(20);
		mbox5 	 = new Mailbox(20);
        // Datastore creation
		dsDataGGA = new DataStore(dsDGGA);
		dsDataRMC = new DataStore(dsDRMC);
//		choralQueue = new Coda(dsCHORAL, 100);
		dsTrkGGA = new DataStore(dsTGGA);
		dsTrkRMC = new DataStore(dsTRMC);
//		NMEAQueue = new Coda(dsNMEA, 100);
		/* Semaphore to manage AT interface to the module,
		 * semaphore with priority queue for mutual exclusion */
		semAT 	= new PrioSem(0);		// initially not used
		// file and recordStre
		file 	= new FlashFile();
		save	= new SaveData();
	}
	
	
	/*
	 *  methods
	 */
	
	/**
	 * Contains the main body of the application.
	 * <BR> ---------- <BR>
	 * Executed operations: <br>
	 * <ul type="disc">
	 * 	<li> Threads init and start;
	 * 	<li> Recover system settings from the configuration file;
	 * 	<li> Determination of the STATE with which start the application;
	 * 	<li> Retrieving data stored in the internal flash by RecordStore;
	 *  <li> Set AUTOSTART (only if applicazion is in state <i>execFIRST</i> or <i>execPOSTRESET</i>);
	 *  <li> Execution of AT^SJNET to set UDP connections through GPRS;
	 *  <li> GPIO driver activation;
	 *  <li> Check key status (GPIO n.7). If key is active, the device is powered up
	 *  at voltage 24 VDC and GPIO n.7 has logic value "0". The device must remain powered up
	 *  in STAND-BY mode (accessible but with tracking disabled), then the application waits for
	 *  GPIO n.7 goes to logic value "1". If at application startup the key is active yet,
	 *  then it means that the device has been awakened from trailer coupling.
	 *  If trailer is attached when application is executed with status different from 
	 *  <i>execCHIAVEattivata</i>, then application goes to STAND-BY mode.
	 *  <li> Disabling motion sensor;
	 *  <li> Preparing timeout about FIX GPS and position strings sending through GPRS connection;
	 *  <li> Possible radio parts activation (AIRPLANE MODE) and start of GPS receiver in transparent mode;
	 *  <li> Wait for messages from threads and messages management, main class waiting for 
	 *  messages from threads and coordinates operations to do, based on received messages and priorities.
	 *  <li> Power of module and stop application.			 
	 * </ul>
	 * ---------- <BR>
	 * Messages sent from threads or events managed in AppMain are the following:
	 * <ul type="circle">
	 * 	<li> Key activation/deactivation;
	 *  <li> Valid FIX GPS or 'FIXtimeout' expired;
	 *  <li> Request to send GPS position strings through GPRS;
	 *  <li> Sending successfully completed of position strings through GPRS or 'FIXgprsTimeout' expired;
	 *  <li> Request to close application or transition to STAND-BY mode;
	 *  <li> RING event (CSD call) to configure application parameters.
	 * </ul>
	 */
	protected void startApp() throws MIDletStateChangeException {
		
		try{
		
		/*
		 * standard application execution
		 */
			if (!TESTenv) {
				
				/* 
				 * [1] INITIALIZATION AND START OF THREADS
				 * 
				 */
				
				// Set threads priority (default value=5, min=1, max=10)
				th1.setPriority(5);
				th2.setPriority(5);
				th3.setPriority(5);
				th4.setPriority(5);
				th5.setPriority(5);
				th6.setPriority(5);
				th8.setPriority(5);
				th9.setPriority(5);
				th10.setPriority(5);
				th11.setPriority(5);
				//th12.setPriority(5);
							
				// Add mailboxes
	
				/* addMailbox(Mailbox mb, int nMbox, int nth, boolean isRcv) 
				 * 	-	nMbox = number that identifies thread owner of the mailbox
				 * 	-	nth   = number that identifies thread that will use mailbox instance */
				th1.addMailbox(mboxMAIN,0,1,false);
				th1.addMailbox(mbox1,1,1,true);
				th1.addMailbox(mbox2,2,1,false);
				
				th2.addMailbox(mbox2,2,2,true);
				th2.addMailbox(mboxMAIN,0,2,true);
				th2.addMailbox(mbox3,3,2,false);
				
				th3.addMailbox(mbox3,3,3,true);
				th3.addMailbox(mboxMAIN,0,3,false);
				th3.addMailbox(mbox2,2,3,false);
				th3.addMailbox(mbox5,5,3,false);
				
				th4.addMailbox(mbox4,4,4,true);
				th4.addMailbox(mboxMAIN,0,4,false);
				th4.addMailbox(mbox2,2,4,false);
				
				th5.addMailbox(mboxMAIN,0,5,false);
				th5.addMailbox(mbox2,2,5,false);
				
				th6.addMailbox(mboxMAIN,0,6,false);
				th6.addMailbox(mbox2,2,6,false);
				
				th8.addMailbox(mboxMAIN,0,8,false);
				th8.addMailbox(mbox2,2,8,false);
				
				th9.addMailbox(mboxMAIN,0,9,false);
				th9.addMailbox(mbox2,2,9,false);
				th9.addMailbox(mbox3,3,9,false);
				
				th10.addMailbox(mbox2,2,10,false);
				th10.addMailbox(mbox3,3,10,false);
				
				
				th11.addMailbox(mboxMAIN,0,11,false);
				th11.addMailbox(mbox2,2,11,false);
				th11.addMailbox(mbox3,3,11,false);
				
				//th12.addMailbox(mboxMAIN,0,12,false);
				//th12.addMailbox(mbox2,2,12,false);
				
                // Add datastores
				th1.addDataStore(dsDataRMC,dsDRMC);
				th2.addDataStore(dsDataRMC,dsDRMC);
				th3.addDataStore(dsDataRMC,dsDRMC);
				th4.addDataStore(dsDataRMC,dsDRMC);
				th9.addDataStore(dsDataRMC,dsDRMC);
				
				th1.addDataStore(dsDataGGA,dsDGGA);
				th2.addDataStore(dsDataGGA,dsDGGA);
				th3.addDataStore(dsDataGGA,dsDGGA);
				th4.addDataStore(dsDataGGA,dsDGGA);
				th9.addDataStore(dsDataGGA,dsDGGA);
	/*			th1.addQueue(choralQueue,dsCHORAL);
				th2.addQueue(choralQueue,dsCHORAL);
				th3.addQueue(choralQueue,dsCHORAL);
				th4.addQueue(choralQueue,dsCHORAL);
	*/			
				th1.addDataStore(dsTrkGGA,dsTGGA);
				th2.addDataStore(dsTrkGGA,dsTGGA);
				th3.addDataStore(dsTrkGGA,dsTGGA);
				th4.addDataStore(dsTrkGGA,dsTGGA);
				th9.addDataStore(dsTrkGGA,dsTGGA);
				
				th1.addDataStore(dsTrkRMC,dsTRMC);
				th2.addDataStore(dsTrkRMC,dsTRMC);
				th3.addDataStore(dsTrkRMC,dsTRMC);
				th4.addDataStore(dsTrkRMC,dsTRMC);
				th9.addDataStore(dsTrkRMC,dsTRMC);
				
	/*			th1.addQueue(NMEAQueue,dsNMEA);
				th2.addQueue(NMEAQueue,dsNMEA);
				th3.addQueue(NMEAQueue,dsNMEA);
				th4.addQueue(NMEAQueue,dsNMEA);
	*/			
				
				// Add semaphores (to all threads using AT interface)
				th1.addPrioSem(semAT, 1, 1);
				th2.addPrioSem(semAT, 1, 2);
				th3.addPrioSem(semAT, 1, 3);
				th4.addPrioSem(semAT, 1, 4);
				th5.addPrioSem(semAT, 1, 5);
				th6.addPrioSem(semAT, 1, 6);
				th8.addPrioSem(semAT, 1, 8);
				th9.addPrioSem(semAT, 1, 9);
				th10.addPrioSem(semAT, 1, 10);
				th11.addPrioSem(semAT, 1, 11);
				//th12.addPrioSem(semAT, 1, 12);
				
				// Add info structure to all threads
				th1.addInfoStato(infoS);
				th2.addInfoStato(infoS);
				th3.addInfoStato(infoS);
				th4.addInfoStato(infoS);
				th5.addInfoStato(infoS);
				th6.addInfoStato(infoS);
				th8.addInfoStato(infoS);
				th9.addInfoStato(infoS);
				th10.addInfoStato(infoS);
				th11.addInfoStato(infoS);
				//th12.addInfoStato(infoS);
				save.addInfoStato(infoS);
				puntatori = save.loadLog();
				if(!puntatori){
					infoS.setInfoFileInt(TrkIN, "0");
					infoS.setInfoFileInt(TrkOUT, "0");
				}
			
				dsDataRMC.addInfoStato(infoS);
				dsDataGGA.addInfoStato(infoS);
				dsTrkRMC.addInfoStato(infoS);
				dsTrkGGA.addInfoStato(infoS);
	//			choralQueue.addInfoStato(infoS);
	//			NMEAQueue.addInfoStato(infoS);
				
				// Start ATsender thread 
				th2.start();
				
				
				/*
				 * [2] RECOVER SSYSTEM SETTINGS FROM CONFIGURATION FILE
				 * 
				 */
				th2.addFlashFile(file);
				th3.addFlashFile(file);
				th4.addFlashFile(file);
				th9.addFlashFile(file);
				th10.addFlashFile(file);
				th11.addFlashFile(file);
				
				if(debug){
					System.out.println("AppMain: Recover system settings in progress...");
				}
				String rsp = file.loadSettings();
				/* 
				 * If configuration file not found, close application
				 *  
				 */
				if (rsp.equalsIgnoreCase("AppMain: FileNotFound")) {
					destroyApp(true);
				}
				
				/*
				 * Recover settings
				 */
				//file.setImpostazione(TrkIN, "0");
				//file.setImpostazione(TrkOUT, "0");
				//temp
				infoS.setInfoFileString(IDtraker,       file.getImpostazione(IDtraker));
				infoS.setInfoFileString(PasswordCSD,    file.getImpostazione(PasswordCSD));
				infoS.setInfoFileString(AppName,        file.getImpostazione(AppName));
				infoS.setInfoFileString(CloseMode,      file.getImpostazione(CloseMode));
				if(debug){
					System.out.println("AppMain: Last closing of application: " + infoS.getInfoFileString(CloseMode));
				}
				infoS.setInfoFileString(LastGPSValid, 	file.getImpostazione(LastGPSValid));
				if(debug){
					System.out.println("AppMain: Last valid string: " + infoS.getInfoFileString(LastGPSValid));
				}
				infoS.setInfoFileString(TrackingInterv, 	file.getImpostazione(TrackingInterv));
				infoS.setInfoFileString(Operatore, 	file.getImpostazione(Operatore));
				infoS.setInfoFileString(TrackingType, 	file.getImpostazione(TrackingType));
				infoS.setInfoFileString(TrackingProt, 	file.getImpostazione(TrackingProt));
				infoS.setInfoFileString(Header, 	file.getImpostazione(Header));
				infoS.setInfoFileString(Ackn, 	file.getImpostazione(Ackn));
				infoS.setInfoFileString(GprsOnTime, 	file.getImpostazione(GprsOnTime));
				infoS.setInfoFileString(TrkState, 	file.getImpostazione(TrkState));
				infoS.setInfoFileString(PublishTopic, 	file.getImpostazione(PublishTopic));
				infoS.setInfoFileString(SlpState, 	file.getImpostazione(SlpState));
				infoS.setInfoFileInt(StillTime, 	file.getImpostazione(StillTime));
				infoS.setInfoFileString(MovState, 	file.getImpostazione(MovState));
				infoS.setInfoFileString(IgnState, 	file.getImpostazione(IgnState));
				infoS.setInfoFileInt(UartSpeed, 	file.getImpostazione(UartSpeed));
				infoS.setInfoFileString(UartGateway, 	file.getImpostazione(UartGateway));
				infoS.setInfoFileString(UartHeaderRS, 	file.getImpostazione(UartHeaderRS));
				infoS.setInfoFileString(UartEndOfMessage, 	file.getImpostazione(UartEndOfMessage));
				infoS.setInfoFileInt(UartAnswerTimeOut, 	file.getImpostazione(UartAnswerTimeOut));
				infoS.setInfoFileInt(UartNumTent, 	file.getImpostazione(UartNumTent));
				infoS.setInfoFileString(UartEndOfMessageIP, 	file.getImpostazione(UartEndOfMessageIP));
				infoS.setInfoFileString(UartIDdisp, 	file.getImpostazione(UartIDdisp));
				infoS.setInfoFileInt(UartTXtimeOut, 	file.getImpostazione(UartTXtimeOut));
				infoS.setInfoFileInt(OrePowerDownOK, 	file.getImpostazione(OrePowerDownOK));
				infoS.setInfoFileInt(MinPowerDownOK, 	file.getImpostazione(MinPowerDownOK));
				infoS.setInfoFileInt(OrePowerDownTOexpired, file.getImpostazione(OrePowerDownTOexpired));
				infoS.setInfoFileInt(MinPowerDownTOexpired, file.getImpostazione(MinPowerDownTOexpired));
				infoS.setInfoFileString(DestHost, 		file.getImpostazione(DestHost));
				if(debug){
					System.out.println("AppMain: destination host: " + infoS.getInfoFileString(DestHost));
				}
				infoS.setInfoFileString(DestPort, 		file.getImpostazione(DestPort));
				infoS.setInfoFileString(ConnProfileGPRS, 	file.getImpostazione(ConnProfileGPRS));
				infoS.setInfoFileString(apn, 	file.getImpostazione(apn));
				infoS.setInfoFileString(GPRSProtocol, 	file.getImpostazione(GPRSProtocol));
				if(puntatori){
					infoS.setInfoFileInt(TrkIN, file.getImpostazione(TrkIN));
					infoS.setInfoFileInt(TrkOUT, file.getImpostazione(TrkOUT));
				}
				//infoS.setInfoFileInt(TrkIN, "0");
				//infoS.setInfoFileInt(TrkOUT, "0");
				infoS.setInfoFileString(InsensibilitaGPS, 	file.getImpostazione(InsensibilitaGPS));
				/*
				 * Prepare settings file for any possible hardware reset or close of application
				 * due to shutdown or unexpected
				 */
				file.setImpostazione(CloseMode, closeAppResetHW);
				file.writeSettings();
	
				
				/*
				 * [3] DETERMINATION OF THE STATE WITH WHICH START THE APPLICATION
				 * 
				 */
				if(debug){
					System.out.println("GPS insensibility: " + infoS.getInfoFileString(InsensibilitaGPS));
					System.out.println("AppMain: Last CloseMode: " + infoS.getInfoFileString(CloseMode));
					System.out.print("AppMain: Determination of application state...");
				}
				
				/*
				 * By default it is supposed that the awakening with '^SYSTART' is due
				 * to the motion sensor, then if I verify that the key is activated
				 * then it means that it was due to the key
				 */
				infoS.setOpMode("NORMAL");
				infoS.setTipoRisveglio(risveglioMovimento);
				
				// First execution after leaving the factory
				if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppFactory)) {
					infoS.setSTATOexecApp(execFIRST);
				}
				// Reboot after AIRPLANE MODE
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAIR)) {
					infoS.setSTATOexecApp(execNORMALE);
					// Set AIRPLANE MODE to deactivate radio parts of the module 
					infoS.setOpMode("AIRPLANE");
					infoS.setTipoRisveglio(risveglioCala);
				}
				
				// Normal OK
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppNormaleOK)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Normal Timeout
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppNormaleTimeout)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Key deactivation FIRST
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppDisattivChiaveFIRST)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Key deactivation OK
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppDisattivChiaveOK)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Key deactivation Timeout
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppDisattivChiaveTimeout)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Motion sensor OK
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppMovimentoOK)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Post Reset
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppPostReset)) {
					infoS.setSTATOexecApp(execNORMALE);
				}
				
				// Low battery
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppBatteriaScarica)) {
					infoS.setSTATOexecApp(execNORMALE);						
				}
				
				// Hardware Reset
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppResetHW)) {
					infoS.setSTATOexecApp(execPOSTRESET);
				}
				
				// Closure due to low battery
				else if (infoS.getInfoFileString(CloseMode).equalsIgnoreCase(closeAppBatteriaScarica)) {
					infoS.setSTATOexecApp(execPOSTRESET);					
				}
				
				else {
					if(debug){
						System.out.println("AppMain: ERROR, I can not determine the status of execution of the application!");
					}
					new LogError("AppMain: ERROR, I can not determine the status of execution of the application!");
				}
	
				if(debug){
					System.out.println(infoS.getSTATOexecApp());
				}
				
				/*
				 *	SET SIM PIN
				 */
				
				semAT.getCoin(5);	
					infoS.setATexec(true);
					mbox2.write("at+cpin=5555\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();	
				
	
				/*
				 * [4] START OF BATTERY LEVEL CONTROL
				 * 
				 */
				batteryTimer = new Timer();
				batteryTimeoutTask = new TimeoutTask(BatteryTimeout);
				batteryTimeoutTask.addInfoStato(infoS);
				batteryTimeoutTask.addMailbox(mbox2,2,11,false);
				batteryTimeoutTask.addPrioSem(semAT, 1, 11);
				batteryTimer.scheduleAtFixedRate(batteryTimeoutTask, 0, batteryTOvalue*1000);
	
				regTimer = new Timer();
				regTimeoutTask = new TimeoutTask(RegTimeout);
				regTimeoutTask.addInfoStato(infoS);
				regTimeoutTask.addMailbox(mbox2,2,11,false);
				regTimeoutTask.addPrioSem(semAT, 1, 11);
				regTimer.scheduleAtFixedRate(regTimeoutTask, 0, regTOvalue*1000);
				
				
				/*
				 * [5] RECOVER DATA FROM FLASH
				 * 
				 */
				try {
					// Open RecordStore
					RecordStore rs = RecordStore.openRecordStore(recordStoreName, true);
					
					/*
					 * Record recovey:
					 * 1) Last GPRMC valid string
					 * 2) <empty> ...
					 */ 
				    byte b[] = rs.getRecord(1);
				    String str = new String(b,0,b.length);
				    infoS.setInfoFileString(LastGPRMCValid, str);
				    if(debug){
				    	System.out.println("RecordStore, record n.1: "+str);
				    }
					
					// Chiusura RecordStore
					rs.closeRecordStore();
				
				} catch (RecordStoreNotOpenException rsnoe) {
					if(debug){
						System.out.println("FlashRecordStore: RecordStoreNotOpenException");
					}
				} catch (RecordStoreFullException rsfe) {
					if(debug){
						System.out.println("FlashRecordStore: RecordStoreFullException");
					}
				} catch (RecordStoreException rse) {
					if(debug){
						System.out.println("FlashRecordStore: RecordStoreException");
					}
				} catch (IllegalArgumentException e){
					if(debug){
						System.out.println("FlashRecordStore: IllegalArgumentException");
					}
				} 
				
						
				/*
				 * [6a] SET AUTOSTART ONLY IF APPLICATION IS IN 
                 * 	    STATE 'execFIRST' OR 'execPOSTRESET'
				 * 
				 */
				if (infoS.getSTATOexecApp().equalsIgnoreCase(execFIRST) ||
					infoS.getSTATOexecApp().equalsIgnoreCase(execPOSTRESET)) {
					
					semAT.getCoin(5);	
						if(debug){
							System.out.println("AppMain: Set AUTOSTART...");
						}
						infoS.setATexec(true);
						mbox2.write("at^scfg=\"Userware/Autostart/AppName\",\"\",\"a:/app/"+ infoS.getInfoFileString(AppName) +"\"\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						infoS.setATexec(true);
						mbox2.write("at^scfg=\"Userware/Autostart/Delay\",\"\",10\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						infoS.setATexec(true);
						mbox2.write("at^scfg=\"Userware/Autostart\",\"\",\"1\"\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						if(usbDebug){
							infoS.setATexec(true);
							mbox2.write("at^scfg=\"Userware/StdOut\",USB\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						}
						else{
							infoS.setATexec(true);
							mbox2.write("at^scfg=\"Userware/StdOut\",ASC0\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						}
					semAT.putCoin();	
					
					infoS.setSTATOexecApp(execNORMALE);
					
				} //AUTOSTART
				
				// Set wake up for ALIVE
				semAT.getCoin(5);			
					infoS.setATexec(true);
					mbox2.write("ati\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					if(debug){
						System.out.println("AppMain: Set AT+CCLK");
					}
					infoS.setATexec(true);
					mbox2.write("at+cclk=\"02/01/01,00:00:00\"\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					infoS.setATexec(true);
					mbox2.write("at+cala=\"02/01/01,06:00:00\"\r");
					//mbox2.write("at+cala=\"02/01/01,00:05:00\"\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				
				semAT.putCoin();
				
				
				/*
				 * [6b] SET AT^SBC TO ADJUST APPLICATION CONSUMPTION
				 * 
				 */
				semAT.getCoin(5);			
					if(debug){
						System.out.println("AppMain: Set AT^SBC...");
					}
					infoS.setATexec(true);
					mbox2.write("AT^SBC=5000\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				

				/*
				 * [6c] SET AT^SJNET FOR UDP CONNECTION (ALWAYS)
				 * 
				 */
				semAT.getCoin(5);
				if(debug){
					System.out.println("AppMain: Set AT^SJNET...");
				}
					infoS.setATexec(true);
					mbox2.write("at^sjnet=\"GPRS\",\"" + infoS.getInfoFileString(apn) + "\",\"\",\"\",\"\",0\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				

				/*
				 * [6d] SET AT+CGSN TO GET MODULE IMEI
				 *
				 */
				semAT.getCoin(5);			
					if(debug){
						System.out.println("AppMain: Read IMEI...");
					}
					infoS.setATexec(true);
					mbox2.write("AT+CGSN\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					infoS.setATexec(true);
					mbox2.write("AT^SCKS=1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				

				/*
				 * [7] GPIO DRIVER ACTIVATION (FOR BOTH KEY AND MOTION SENSOR)
				 * 
				 */
				semAT.getCoin(5);
					// Open GPIO driver, send 'at^spio=1'
					if(debug){
						System.out.println("Th*GPIOmanager: Open GPIO driver...");
					}
					infoS.setATexec(true);
					mbox2.write("at^spio=1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
			    semAT.putCoin();
				
				
				/*
				 * [8] CONTROL KEY ACTIVATED (GPIO n.7)
				 * key control must be done before the motion sensor!
				 */
				
				// Start thread TestChiave
				th5.start();
	
				// Wait until polling is enabled on GPIO key
				while(infoS.getPollingAttivo()==false) { Thread.sleep(whileSleep); }
				if(debug){
					System.out.println("AppMain: Polling is enabled on GPIO key!");
				}
					
				/*
                 * At this point I know if device was started
                 * due to the motion sensor, the key or + CALA 
				 */
				if (infoS.getTipoRisveglio().equalsIgnoreCase(risveglioMovimento)) {
					/*
					 * If awakening due to motion sensor
					 * go to state 'execMOVIMENTO'
					 */
					if(debug){
						System.out.println("execMOVIMENTO");
					}
					infoS.setSTATOexecApp(execMOVIMENTO);
				} //risveglioMovimento
				
				
				/*
				 * [9] DISABLING MOTION SENSOR
				 *
				 */
				infoS.setDisattivaSensore(true);
				
				// Start GPIO Manager, motion sensor and WatchDog
				th6.start();
				//th12.start();
		
				// Start WatchDog timer			
				if (watchdog == true){
					WatchDogTimer = new Timer();
					WatchDogTimeoutTask = new TimeoutTask(WatchDogTimeout);
					WatchDogTimeoutTask.addInfoStato(infoS);
					WatchDogTimeoutTask.addMailbox(mbox2,2,12,false);
					WatchDogTimeoutTask.addPrioSem(semAT, 1, 12);
					WatchDogTimer.scheduleAtFixedRate(WatchDogTimeoutTask, 1000*60, WatchDogTOvalue*1000);
				}
				
				// Wait until motion sensor is disabled
				if(debug){
					System.out.println("AppMain: waiting for disabling motion sensor...");
				}
				while(infoS.getDisattivaSensore()==true) { Thread.sleep(whileSleep); }
				if(debug){
					System.out.println("AppMain: Motion sensor disabled!");
				}
				
				
				/* 
				 * [10] RADIO PARTS OR GPS RECEIVER ACTIVATIONS
				 *  
				 */
				
				/* 
				 * If application is in state 'execNORMALE' or 'execMOVIMENTO'
				 * I must go to from AIRPLANE mode to NORMAL mode,
				 * activating radio part
				 * (case 'execMOVIMENTO' should be covered because it is in fact
				 * identical to the normal case, up to this point in the application)
				 */
				
				/* Send 'AT' for safety, per sicurezza, in order to succeed
				 * in any case to view the ^SYSSTART URC
				 */
				semAT.getCoin(1);
					infoS.setATexec(true);
					mbox2.write("AT\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }	
					// Set URC
					infoS.setATexec(true);
					mbox2.write("at+crc=1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
				// Wait for info about ^SYSSTART type
				while (infoS.getOpMode()==null) { Thread.sleep(whileSleep); }
				
				/* Power on radio parts of the module for safety */
				if(debug){
					System.out.println("AppMain: SWITCH ON RADIO PART of the module...");
				}
				semAT.getCoin(1);
					infoS.setATexec(true);
					mbox2.write("AT^SCFG=\"MEopMode/Airplane\",\"off\"\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
				semAT.getCoin(1);
					infoS.setATexec(true);
					mbox2.write("AT+CREG=1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
				semAT.getCoin(1);
					infoS.setATexec(true);
					mbox2.write("AT+CGREG=1\r");
					while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				semAT.putCoin();
				
				
				// Radio activation
				
				// Starting threads CommGPStrasparent, TrackingGPRS, CheckSMS e Seriale
				th1.start();
				th3.start();
				th8.start();
				th9.start();
				th10.start();
				th11.start();
				

				/* 
				 * [11] PREPARING TIMEOUTS ABOUT FIX GPS AND POSITIONING STRINGS SENDING
				 * 
				 */
				// GPS
				FIXgpsTimer	= new Timer();
				if(debug){
					System.out.println("AppMain: GPS timer RESET n." + countTimerResetGPS);
				}
				countTimerResetGPS++;
				// GPS task and resources
				FIXgpsTimeoutTask 	= new TimeoutTask(FIXgpsTimeout);
				FIXgpsTimeoutTask.addInfoStato(infoS);
				
				// GPRS
				FIXgprsTimer 		= new Timer();
				if(debug){
					System.out.println("AppMain: GPRS timer RESET n." + countTimerResetGPRS);
				}
				countTimerResetGPRS++;
				// GPRS task and resources
				FIXgprsTimeoutTask 	= new TimeoutTask(FIXgprsTimeout);
				FIXgprsTimeoutTask.addInfoStato(infoS);
				FIXgprsTimeoutTask.addMailbox(mbox3,3,9,false);
							
				
                /* Enable CSD */
                infoS.setEnableCSD(true);
				
				
				/* 
				 * [12] WAIT FOR MESSAGES FROM THREADS AND MANAGEMENT
				 * 
				 *	From here AppMain waits for msg from threds and coordinates
				 * the operations to be carried out in accordance with the msg
				 * received, according to the priorities
                 */
				if(debug){
					System.out.println("AppMain, actual state of the application: " + infoS.getSTATOexecApp());
				}	
				
				if(infoS.getCALA()){
					semAT.getCoin(1);
						System.out.println("AppMain: Module reboot in progress...");
						infoS.setATexec(true);
						mbox2.write("AT+CFUN=1,1\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
				    semAT.putCoin();
				}
					
				while(true) {
						
					try{	
						
						semAT.getCoin(1);
							infoS.setATexec(true);
							mbox2.write("AT+CSQ\r");
							while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						semAT.putCoin();
						
						/*
						 * Read a possible msg present on the Mailbox
						 * 
						 * read() method is BLOCKING, until a message is received
						 * (of some sort) the while loop does not continue
						 */ 
						try{	
							
							msgRicevuto = (String)mboxMAIN.read();
								
								//System.out.println(infoS.getEnableCSD());
								//System.out.println(infoS.getCSDattivo());
							
							}catch(Exception e){
								System.out.println("ERROR READ mboxMAIN");
							}
							
							if (msgRicevuto.equalsIgnoreCase(msgCloseGPRS)) {
								//while(!th3.isAlive()){ Thread.sleep(whileSleep); }
								th3.start();
							}
							

							//*** [12a] KEY MANAGEMENT
							
							/*
							 * ACTIVATION KEY event
							 * 
							 * (both in the case where the key is the cause of the
							 *  application startup and when the key is activated
							 * while the application is running in another state)
							 */
							if(debug_main){
								System.out.println("msg? " + msgRicevuto);
							}
							if (msgRicevuto.equalsIgnoreCase(msgChiaveAttivata) && infoS.getInibizioneChiave()==false) {
								
								if(debug_chiave){
									System.out.println("AppMain: KEY activation detected...");
								}
								
								/*
								 * Origin state: 'execMOVIMENTO'
								 * 
								 * Suspend motion tracking, execute tracking with active KEY
								 * and go to STAND-BY
								 */ 
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execMOVIMENTO)) {
									
									if(debug){
										System.out.println("AppMain,MOTION: key was activated!");
									}
									
									//infoS.setSTATOexecApp(execCHIAVEattivata);
									infoS.setSTATOexecApp(execNORMALE);
									mbox3.write(trackAttivChiave);
			
								} //execMOVIMENTO
			
								else if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata)) {
									
									if(debug){
										System.out.println("AppMain, KEY ACTIVE: already in key active state!");
									}
									/*
									 * Start tracking with active key, in there is a FIX
									 */
									//if (infoS.getValidFIX() == true || gpsTimerAlive == false) {
										infoS.setSTATOexecApp(execNORMALE);
										if (infoS.getEnableGPRS()==true) {
											mbox3.write(trackAttivChiave);
											if(debug){
												System.out.println("AppMain: sent messageo: " + trackAttivChiave + " to tasksocket");
											}
										} else if(debug){
											System.out.println("AppMain: GPRS disabled");
										}
									//}
													
								}
								
							} //msgChiaveAttivata
							
/*
 *  TIMEOUT GPRS
 *
 *	if 'gprsTimeout' and 'gprsTimeoutAck'
 * 
 */				
													
							if(msgRicevuto.equalsIgnoreCase(gprsTimeoutStart)){
												
								//System.out.println("START GPRS TIMEOUT");
								infoS.setIfIsFIXgprsTimeoutExpired(false);
								FIXgprsTimer = new Timer();
								FIXgprsTimeoutTask 	= new TimeoutTask(FIXgprsTimeout);
								FIXgprsTimeoutTask.addMailbox(mboxMAIN,0,11,false);
								FIXgprsTimeoutTask.addMailbox(mbox3,3,11,false);
								FIXgprsTimeoutTask.addInfoStato(infoS);
								FIXgprsTimer.schedule(FIXgprsTimeoutTask, 30*1000);
														
							}
												
							if(msgRicevuto.equalsIgnoreCase(gprsTimeoutStop)){
													
								//System.out.println("STOP GPRS TIMEOUT");
								FIXgprsTimer.cancel();
								FIXgprsTimeoutTask.cancel();
														
							}
									
							
							/*
							 * KEY DEACTIVATION event
							 * 
							 * (you can get here from the state 'execCHIAVEattivata'
							 *  but also from states 'execFIRST' and 'execPOSTRESET',
							 *  becOrigin stateause in these states I check the FIX only if the KEY
							 *  is activated and I would like to know if it has been disabled in the meantime)		  	
							 */				
							if (msgRicevuto.equalsIgnoreCase(msgChiaveDisattivata) && infoS.getInibizioneChiave()==false) {
							//if (msgRicevuto.equalsIgnoreCase(msgChiaveDisattivata)){	
								if(debug_chiave){
									System.out.println("AppMain: KEY deactivation detected");
								}
																								
								/*
								 * Origin state: 'execCHIAVEattivata'
								 * 
								 * App sends last valid GPS position (you don't need GPS timer,
								 * but GPRS timer yes!) and goes to power down
								 */ 
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata) || infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
			
									// Block everything before closing
									mbox3.write(exitTrack);
									
									// Wait for closure
									while (infoS.getTrackingAttivo()==true) { Thread.sleep(whileSleep); }
									
									GOTOstagePwDownSTANDBY = true;
									
									// Set new application state
									infoS.setSTATOexecApp(execCHIAVEdisattivata);
								}
								
								/*
								 * Case 'execMOVIMENTO' -> do nothing
								 */
								else{ 
									if(debug){
										System.out.println("AppMain,CHIAVEdisattivata: nothing to do, key already deactivated!");
									}
									if(timeToOff == 50 || timeToOff == 70)
										mbox3.write(trackMovimento);
									else{
										if(timeToOff >= 100){
											// Block everything before closing
											mbox3.write(exitTrack);
											
											// Wait for closure
											while (infoS.getTrackingAttivo()==true) { Thread.sleep(whileSleep); }
											
											GOTOstagePwDownSTANDBY = true;
											
											// Set new application state
											infoS.setSTATOexecApp(execCHIAVEdisattivata);
										}
									}
									timeToOff++;
									//System.out.println("timeToOff" + timeToOff);
								}
				
							} //msgChiaveDisattivata		
							
							
							//*** [12c] WAIT FOR VALID GPS FIX / 'FIXtimeout' EXPIRED
							
							if (infoS.getValidFIX()==false && infoS.isFIXtimeoutExpired()==false) {
								// No valid FIX and 'FIXtimeout' not expired
								if(debug){
									System.out.println("AppMain: waiting for 'validFIX'...");
								}
							}

							//*** [12e]	WAIT FOR SENDING A VALID GPS FIX WITH SUCCESS THROUGH GPRS
							//***  		OR 'FIXgprsTimeout' EXPIRED
							if(debug_main){
								System.out.println(msgRicevuto);
							}
							/*
							 * 'msgFIXgprs' received (within 'FIXgprsTimeout')
							 * I have at least a valid GPS FIX,
							 * go to application closure procedure
							 */
							if (msgRicevuto.equalsIgnoreCase(msgFIXgprs) && infoS.getCSDattivo()==false && ((infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS"))) {
								
								if(debug){
									System.out.println("AppMain: valid GPS FIX sent through GPRS connection with success");
								}
								infoS.setValidFIXgprs(true);
								
								// If KEY is activated, repeat tracking
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata) || infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
									
									infoS.setSTATOexecApp(execNORMALE);
									mbox3.write(trackNormale);
									trackTimerAlive = true;
								}
								
								else if (infoS.getSTATOexecApp().equalsIgnoreCase(execMOVIMENTO)) {
									
									/*
									 * Stop timer and reset for a new execution
									 */
									FIXgprsTimer.cancel();
									//gprsTimerAlive = false;
									FIXgprsTimeoutTask.cancel();
									if(debug){
										System.out.println("AppMain: GPRS timer STOP n." + countTimerStopGPRS);
									}
									countTimerStopGPRS++;
									
									FIXgprsTimer = new Timer();
									if(debug){
										System.out.println("AppMain: GPRS timer RESET n." + countTimerResetGPRS);
									}
									countTimerResetGPRS++;
									FIXgprsTimeoutTask 	= new TimeoutTask(FIXgprsTimeout);
									FIXgprsTimeoutTask.addInfoStato(infoS);
									FIXgprsTimeoutTask.addMailbox(mbox3,3,9,false);
									
									// invokes GoToPowerDown
									//GOTOstagePwDownSTANDBY = true;
								}
								
								// other cases (including low battery)
								//else GOTOstagePwDownSTANDBY = true;
								
							} //msgFIXgprs
							
							if (msgRicevuto.equalsIgnoreCase(msgALIVE) && infoS.getCSDattivo()==false && ((infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS"))) {
								
								if(debug){
									System.out.println("AppMain: Alive message");
								}
								infoS.setValidFIXgprs(true);
								
								semAT.getCoin(5);			
									if(debug){
										System.out.println("AppMain: Set AT+CCLK");
									}
									infoS.setATexec(true);
									mbox2.write("at+cclk=\"02/01/01,00:00:00\"\r");
									while(infoS.getATexec()) { Thread.sleep(whileSleep); }
									infoS.setATexec(true);
									mbox2.write("at+cala=\"02/01/01,06:00:00\"\r");
									//mbox2.write("at+cala=\"02/01/01,00:05:00\"\r");
									while(infoS.getATexec()) { Thread.sleep(whileSleep); }
									
								semAT.putCoin();
								
								// If KEY is activated, repeat tracking
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata) || infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
									
									infoS.setSTATOexecApp(execNORMALE);
									mbox3.write(trackAlive);
									trackTimerAlive = true;
									if(infoS.getInfoFileInt(UartNumTent)>0){
										infoS.setInfoFileInt(UartNumTent, "0");
										file.setImpostazione(UartNumTent, "0");
										InfoStato.getFile();
										file.writeSettings();
										InfoStato.freeFile();
										mbox3.write(trackUrcSim);
									}
								}
								
							} //msgAlive

							if (msgRicevuto.equalsIgnoreCase(msgALR1) && infoS.getCSDattivo()==false && ((infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS"))) {
								
								if(debug){
									System.out.println("AppMain: rilevato ALLARME INPUT 1");
								}
								infoS.setValidFIXgprs(true);
								
								// If KEY is activated, repeat tracking
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata) || infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
									
									infoS.setSTATOexecApp(execNORMALE);
									mbox3.write(trackAlarmIn1);
									trackTimerAlive = true;
								}
								
							} //msgALR1
							
							if (msgRicevuto.equalsIgnoreCase(msgALR2) && infoS.getCSDattivo()==false && ((infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON") || (infoS.getInfoFileString(TrkState)).equalsIgnoreCase("ON,FMS"))) {
								
								if(debug){
									System.out.println("AppMain: rilevato ALLARME INPUT 2");
								}
								infoS.setValidFIXgprs(true);
								
								// If KEY is activated, repeat tracking
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execCHIAVEattivata) || infoS.getSTATOexecApp().equalsIgnoreCase(execNORMALE)) {
									
									infoS.setSTATOexecApp(execNORMALE);
									mbox3.write(trackAlarmIn2);
									trackTimerAlive = true;
								}
								
							} //msgALR1

							
							//*** [12f] LOW BATTERY CONTROL
							
							/*
							 * 'msgBattScarica' received
							 * Put module in power down, without enabling motion sensor
							 */
							if (msgRicevuto.equalsIgnoreCase(msgBattScarica)&& elabBATT == false && PowDown == false) {
								
													
								elabBATT = true;
								
								if(debug){
									System.out.println("AppMain: low battery signal received");
								}
																
								/*
								 * Se è attivo il timer GPS -> STOP e RESET
								 */
								if (gpsTimerAlive == true) {
									
									FIXgpsTimer.cancel();
									gpsTimerAlive = false;
									FIXgpsTimeoutTask.cancel();
									if(debug){
										System.out.println("AppMain: GPS timer STOP n." + countTimerStopGPS);
									}
									countTimerStopGPS++;
									
									FIXgpsTimer  = new Timer();
									if(debug){
										System.out.println("AppMain: GPS timer RESET n." + countTimerResetGPS);
									}
									countTimerResetGPS++;
									FIXgpsTimeoutTask 	= new TimeoutTask(FIXgpsTimeout);
									FIXgpsTimeoutTask.addInfoStato(infoS);
								}
								
								/*
								 * Block GPRS timer in state 'execMOVIMENTO'
								 */ 
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execMOVIMENTO)) {
									
									/*
									 * Only GPRS timer is running, reset it
									 */
									FIXgprsTimer.cancel();
									//gprsTimerAlive = false;
									FIXgprsTimeoutTask.cancel();
									if(debug){
										System.out.println("AppMain: GPRS timer STOP n." + countTimerStopGPRS);
									}
									countTimerStopGPRS++;
									
									FIXgprsTimer = new Timer();
									if(debug){
										System.out.println("AppMain: GPRS timer RESET n." + countTimerResetGPRS);
									}
									countTimerResetGPRS++;
									FIXgprsTimeoutTask 	= new TimeoutTask(FIXgprsTimeout);
									FIXgprsTimeoutTask.addInfoStato(infoS);
									FIXgprsTimeoutTask.addMailbox(mbox3,3,9,false);
								}
								
								// Set new application state
								infoS.setSTATOexecApp(execBATTSCARICA);
								
								/*
								 * Start tracking low battery
								 */
								if (infoS.getEnableGPRS()==true) {
//									infoS.setNumTrak(infoS.getInfoFileInt(NumTrakNormal));
									mbox3.write(trackBatteria);
									if(debug){
										System.out.println("AppMain: sent message: " + trackBatteria);
									}
								} else
								
								/*
								 * Close as low battery
								 */
								file.setImpostazione(CloseMode, closeAppBatteriaScarica);
								InfoStato.getFile();
								file.writeSettings();
								InfoStato.freeFile();
								/*
								 * Don't start GPRS timeout, try to send strings until battery is fully discharged
								 */
									
							} //msgBattScarica				
							
							
							//*** [12g] APPLICATION CLOSURE / STAND-BY
							
							if (GOTOstagePwDownSTANDBY == true) {
								/* 
								 * When I finish to send positions through GPRS,
								 * I prevent from happening more than once this step 
								 */
								GOTOstagePwDownSTANDBY = false;
								PowDown = true;
								
								/* 
								 * Inhibit key usage,
								 * device must go to power down mode
								 */
								infoS.setInibizioneChiave(true);
								infoS.setEnableCSD(false);
								
								/*
								 *  If battery low, call GoToPowerDown without
								 *  activate motion sensor and without set awakening,
								 *  writing on file the closure reason (battery low)
								 */
								if (infoS.getSTATOexecApp().equalsIgnoreCase(execBATTSCARICA)) {
									if(debug){
										System.out.println("AppMain, low battery: motion sensor DISABLED!");
									}
								}
								
								// If key deactivated, enable motion sensor and call 'GoToPowerDown'
								else {
									if(movsens){
										/*
										 * ENABLING MOTION SENSOR
										 */
										// Enable motion sensor
										infoS.setAttivaSensore(true);
									
										// Wait until motion sensor is running
										while(infoS.getAttivaSensore()==true) { Thread.sleep(whileSleep); }
										if(debug){
											System.out.println("AppMain: motion sensor ENABLED!");
										}
									}
								}
								
								/*
								 * Start thread 'GoToPowerDown' to get ready module for Power Down
								 */ 
								if (elabPWD==false) {
									elabPWD = true;
									th4.start();	
								}
								
							} //GOTOstagePwDownSTANDBY
			
							
							//*** [12h] EXIT LOOP TO CLOSE APPLICATION
							
							/*
							 * Receive 'msgClose'
							 * I can put module in Power Down mode
							 */
							if (msgRicevuto.equalsIgnoreCase(msgClose)) {
								if(debug){
									System.out.println("AppMain: received instruction to close application");
								}
								break;	//break while loop
							} //msgClose
							
			
							//*** [12i] RING EVENT, FOR CSD CALL (IF POSSIBLE)
							
							/*
							 * 'msgRING' received and CSD procedure enabled
							 * I can activate CSD procedure and disable every other operation
							 * until CSD procedure will be finished
							 */
							if (msgRicevuto.equalsIgnoreCase(msgRING) && infoS.getEnableCSD()==true && infoS.getCSDattivo()==false) {
								
								if(debug){
									System.out.println("AppMain: received instruction to start CSD procedure");
								}
														
								// Init and start UpdateCSD
								th7 = new UpdateCSD();
								th7.setPriority(5);
								th7.addPrioSem(semAT, 1, 7);
								th7.addInfoStato(infoS);
								th7.addMailbox(mbox2,2,7,false);
								th7.start();
							
								// Wait for UpdateCSD answers to incoming call
								Thread.sleep(2000);
								
							} //msgRING
							
							if(infoS.getReboot()){
								System.out.println("Reboot for GPIO");
								if(infoS.getInfoFileInt(UartNumTent)>0){
									file.setImpostazione(UartNumTent, "1");
									InfoStato.getFile();
									file.writeSettings();
									InfoStato.freeFile();
								}
								restart = true;
								//break;
							}
							
							Thread.sleep(10);
							
						} catch (InterruptedException ie){
							new LogError("Interrupted Exception AppMain");
							restart = true;
						} catch (Exception ioe){
							new LogError("Generic Exception AppMain");
							restart = true;
						}
						if(restart == true){
							InfoStato.getFile();
							save.writeLog();
							InfoStato.freeFile();
							restart = false;
							semAT.getCoin(1);
								if(debug){
									System.out.println("AppMain: Reboot module in progress...");
								}
								System.out.println("Reboot for GPIO");
								new LogError("Reboot for GPIO");
								pwr = new PowerManager();
								pwr.setReboot();
								//mbox2.write("AT+CFUN=1,1\r");
								while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						    semAT.putCoin();
						    break;
					    }
						
					} //while(true)
	
									
					/* 
					 * [13] POWER OFF MODULE AND CLOSE APPLICATION
					 *
					 * Please note:
					 *      AT^SMSO cause call of destroyApp(true),
					 * 		therefore AT^SMSO should not be called inside detroyApp()
					 */
					semAT.getCoin(1);
						if(debug){
							System.out.println("AppMain: Power off module in progress...");
						}
						infoS.closeTrackingGPRS();
						infoS.closeUDPSocketTask();
						infoS.closeTCPSocketTask();
						th3.join();
						th10.join();
						th11.join();
						InfoStato.getFile();
						save.writeLog();
						InfoStato.freeFile();
						infoS.setATexec(true);
						mbox2.write("AT^SPIO=0\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
						pwr = new PowerManager();
						pwr.setLowPwrMode();
						Thread.sleep(5000);
						infoS.setATexec(true);
						mbox2.write("AT^SMSO\r");
						while(infoS.getATexec()) { Thread.sleep(whileSleep); }
					semAT.putCoin();
					
					/*try {
						if(debug){
							System.out.println("XE DRIO STUARSE");
						}
						destroyApp(true);
					} catch (MIDletStateChangeException me) { 
						if(debug){
							System.out.println("AppMain: MIDletStateChangeException"); 
						}
					}*/
			}
						
			/*
			 * APPLICATION TEST ENVIRONMENT
			 */
			else if (TESTenv) {
				if(debug){
					System.out.println("Application test environment");
				}
			} //TEST ENVIRONMENT

			} catch (InterruptedException ie){
				if(debug){
					System.out.println("Interrupted Exception AppMain");
				}
				new LogError("Interrupted Exception AppMain");
			} catch (Exception ioe){
				if(debug){
					System.out.println("Generic Exception AppMain");
				}
				new LogError("Generic Exception AppMain");
			}
		} //startApp
		
		/**
		 * Contains code to run the application
		 * when is in PAUSE.
		 */
		protected void pauseApp() {
			if(debug){
				System.out.println("Application in pause...");
			}
			try {
				Thread.sleep(2000);
				destroyApp(true);
			}
			catch (InterruptedException ie){}
			catch (MIDletStateChangeException me){}
		} //pauseApp
		
		/**
		 * Contains code to execute before destroy application.
		 * 
		 * @throws	MIDletStateChangeException
		 */
		protected void destroyApp(boolean cond) throws MIDletStateChangeException {
			if(debug){
				System.out.println("Close application in progress...");
			}
			
			// Destroy application
			notifyDestroyed();
		} //destroyApp
		
	} //AppMain




