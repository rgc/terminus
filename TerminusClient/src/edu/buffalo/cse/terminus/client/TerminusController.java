package edu.buffalo.cse.terminus.client;

import android.hardware.SensorEventListener;
import edu.buffalo.cse.terminus.client.sensors.ICameraCallbacks;
import edu.buffalo.cse.terminus.client.network.INetworkCallbacks;
import edu.buffalo.cse.terminus.client.network.TerminusConnection;
import edu.buffalo.cse.terminus.client.sensors.*;

public class TerminusController implements ICameraCallbacks
{
	static class NetworkSettings
	{
		String ip;
		int port;
		INetworkCallbacks networkCallbacks;
		
		//This allows us to turn off networking for testing
		boolean enabled = true;
	}
	
	static class SensorSettings
	{
		int[] sensorList;
		SensorEventListener listener;
	}
	
	private NetworkSettings networkSettings;
	private TerminusConnection connection;
	
	private TerminusSensorManager sensorManager;
	
	private TerminusClientMainActivity activity;
	
	public TerminusController(NetworkSettings ns, SensorSettings ss, 
			TerminusClientMainActivity a)
	{
		activity = a;
		
		networkSettings = ns;
		connection = new TerminusConnection(ns.networkCallbacks);
		
		sensorManager = new TerminusSensorManager(ss.sensorList, ss.listener, this, activity);
	}
	
	public void start()
	{
		sensorManager.start();
		
		if (networkSettings.enabled)
			connection.connect(networkSettings.ip, networkSettings.port);
	}
	
	public void stop()
	{
		sensorManager.stop();
		
		if (networkSettings.enabled)
			connection.disconnect();
	}
	
	public void sensorEventSensed(int type)
	{
		connection.sendMessage(connection.getEventMessage());
	}
	
	public void onCameraMotionDetected()
	{
		connection.sendMessage(connection.getEventMessage());
	}
	
	public void soundEventSensed()
	{
		
	}
}
