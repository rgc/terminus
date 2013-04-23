package edu.buffalo.cse.terminus.client;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TerminusSettings 
{
	public static final int CAMERA_NONE = 0;
	public static final int CAMERA_JAVA = 1;
	
	/*
	 * In the future, it would be great to be able to select the camera view
	 * However, it's not quite straight forward to change the opencv view
	 * outside of the XML.
	 */
	public static final int CAMERA_NATIVE = 2;
	public static final int CAMERA_LOWLEVEL = 3;
	
	private static final String TERMINUS_PREF_FILE = "TerminusSettings";
	private static final String PARAM_IP_ADDRESS = "ipAddress";
	private static final String PARAM_PORT = "portNumber";
	private static final String PARAM_CAMERA = "cameraOption";
	private static final String PARAM_SOUND = "soundOption";
	private static final String PARAM_SENSORS = "sensorList";
	private static final String PARAM_PRIORITY = "minPriority";
	
	public String ipAddress = "";
	public int port = 34411;
	public int cameraOption = CAMERA_JAVA;
	public boolean useSound = false;
	public ArrayList<Integer> sensorList = new ArrayList<Integer>();
	public int PriorityLimit = 1;
	
	public void retrieve(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(TERMINUS_PREF_FILE, 0);
		ipAddress = settings.getString(PARAM_IP_ADDRESS, "");
		port = settings.getInt(PARAM_PORT, 0);
		cameraOption = settings.getInt(PARAM_CAMERA, CAMERA_NONE);
		useSound = settings.getBoolean(PARAM_SOUND, false);
		
		PriorityLimit = settings.getInt(PARAM_PRIORITY, 0);
		String allSensors = settings.getString(PARAM_SENSORS, "");
		
		if (allSensors.length() > 0)
		{
			String tokens[] = settings.getString(PARAM_SENSORS, "").split(",");
			sensorList = new ArrayList<Integer>();
			
			for (int i = 0; i < tokens.length; i++)
			{
				try
				{
					sensorList.add(Integer.valueOf(tokens[i]));
				}
				catch (NumberFormatException e)
				{
					//Somehow garbage go it inhere. Skip this one.
				}
			}
		}
	}
	
	public void commit(Context c)
	{
		SharedPreferences settings = c.getSharedPreferences(TERMINUS_PREF_FILE, 0);
		Editor e = settings.edit();
		
		e.putString(PARAM_IP_ADDRESS, ipAddress);
		e.putInt(PARAM_PORT, port);
		e.putInt(PARAM_CAMERA, cameraOption);
		e.putBoolean(PARAM_SOUND, useSound);
		
		e.putInt(PARAM_PRIORITY, PriorityLimit);
		//No support for int arrays, so we'll build a csv string to serialize it.
		String sensors = "";
		
		if (sensorList.size() > 0)
		{
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < sensorList.size(); i++)
			{
				sb.append(sensorList.get(i));
				if (i < sensorList.size()-1)
				{
					sb.append(',');
				}
			}
			sensors = sb.toString();
		}
		
		e.putString(PARAM_SENSORS, sensors);
		
		e.commit();
	}
	
	public boolean containsSenor(int t)
	{
		for (int i = 0; i < this.sensorList.size(); i++)
		{
			if (sensorList.get(i) == t)
			{
				return true;
			}
		}
		
		return false;
	}
	
}
