package com.m2mgo.util;


public class GPRSConnectOptions {
	
	private static GPRSConnectOptions connOptions;
	private String bearer_type = "gprs";
	private String access_point = "internet";
	private String username = "";
	private String passwd = "";
	//private int timeout = 40;
	private int timeout = 0; //[MB] 20130924 Modificato per eludere Timeout di connessione
	
	public static GPRSConnectOptions getConnectOptions() {
		if (connOptions == null) {
			connOptions = new GPRSConnectOptions();
		}
		return connOptions;
	}
	
	public void setBearerType(String bt) {
		bearer_type = bt;
	}

	public void setAPN(String apn) {
		access_point = apn;
	}

	public void setTimeout(int time) {
		timeout = time;
	}
	
	public void setUser(String usr) {
		this.username = usr;
	}

	public void setPasswd(String pwd) {
		this.passwd = pwd;
	}
	
	public String getBearerType() {
		return bearer_type;
	}

	public String getAPN() {
		return access_point;
	}

	public int getTimeout(){
		return timeout;
	}
	
	public String getUser() {
		return username;
	}

	public String getPasswd() {
		return passwd;
	}
}
