package edu.buffalo.cse.terminus.terminusnotifier;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings 
{		
	private static final String NOTIFICATION_PREF_FILE = "TerminusNotificationSettings";
	private static final String PARAM_IP_ADDRESS = "ipAddress";
	private static final String PARAM_PORT = "portNumber";
	
	private static final int DEFAULT_PORT = 34411;
	
	public String ipAddress;
	public int port;
	
	public void retrieve(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(NOTIFICATION_PREF_FILE, 0);
		ipAddress = settings.getString(PARAM_IP_ADDRESS, "");
		port = settings.getInt(PARAM_PORT, DEFAULT_PORT);
	}
	
	public void commit(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(NOTIFICATION_PREF_FILE, 0);
		Editor e = settings.edit();
		
		e.putString(PARAM_IP_ADDRESS, ipAddress);
		e.putInt(PARAM_PORT, port);
		e.commit();
	}
}
