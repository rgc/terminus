package edu.buffalo.cse.terminus.client;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import edu.buffalo.cse.terminus.client.sensors.ICameraCallbacks;
import edu.buffalo.cse.terminus.client.network.INetworkCallbacks;
import edu.buffalo.cse.terminus.client.network.TerminusConnection;
import edu.buffalo.cse.terminus.client.sensors.*;
import edu.buffalo.cse.terminus.lowlevel.LowLevelImageMessage;
import edu.buffalo.cse.terminus.messages.EventMessage;

public class TerminusController implements ICameraCallbacks
{

	private TerminusSettings settings;
	private TerminusConnection connection;
	
	private TerminusSensorManager sensorManager;
	
	private TerminusClientMainActivity activity;
	
	public int TotPriority=0;
	//priority level 0 is total priority 0
	public boolean[] PriorityLevels = new boolean[10];
	
	public TerminusController(TerminusSettings settings, SensorEventListener listener, 
			INetworkCallbacks networkCallbacks, TerminusClientMainActivity a)
	{
		activity = a;
		
		this.settings = settings;
		connection = new TerminusConnection(networkCallbacks, a);
		
		sensorManager = new TerminusSensorManager(settings, listener, this, activity);
		//sensorManager.createSoundAlgos();
	}
	
	public void start()
	{
		sensorManager.start();
		//sensorManager.startSoundAlgos();
		/*
		 * Just do some minor checking to ignore obvious errors
		 */
		if (settings.ipAddress != null && settings.ipAddress.length() > 0 
				&& settings.port > 0)
		{
			connection.connect(settings.ipAddress, settings.port);
		}
		
		for(int i=0;i<PriorityLevels.length;i++)
			PriorityLevels[i]=false;
	}
	
	public void stop()
	{
		sensorManager.stop();
		//sensorManager.stopSoundAlgos();
		connection.disconnect();
	}
	
	public void reconnectNetwork()
	{
		connection.reconnect();
	}
	
	public void disconnectNetwork()
	{
		connection.disconnect();
	}
	
	public void sensorEventSensed(int type,int pri)
	{
		boolean send = false;
		TotPriority+=pri;
		for(int i=0;i<PriorityLevels.length;i++){
			if(TotPriority>(settings.PriorityLimit*i)){
				if(PriorityLevels[i]==false){
					PriorityLevels[i]=true;
					send=true;
				}
			}
		}
		if(send==true){
			/* Convert to Terminus Message event types */
			int eventType = 0;
			
			switch (type)
			{
			case Sensor.TYPE_ACCELEROMETER:
				eventType = EventMessage.EVENT_ACCELEROMETER;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				eventType = EventMessage.EVENT_MAGNETOMETER;
				break;
			case Sensor.TYPE_LIGHT:
				eventType = EventMessage.EVENT_LIGHT;
				break;
			}
			
			EventMessage em = connection.getMessageFactory().getEventMessage(connection.getConnectionID());
			em.setEventType(eventType);
			em.setPrority(TotPriority);
			connection.sendMessage(em);
		}
	}
	
	public void onCameraMotionDetected()
	{
		EventMessage em = connection.getMessageFactory().getEventMessage(connection.getConnectionID());
		em.setEventType(EventMessage.EVENT_CAMERA_MOTION);
		connection.sendMessage(em);
	}
	
	public void onCameraMotionDetected(byte[] imageBytes)
	{
		onCameraMotionDetected();
		
		LowLevelImageMessage im = new LowLevelImageMessage(connection.getConnectionID());
		im.setImage(imageBytes);
		connection.sendImage(im);
	}
	
	public void soundEventSensed(int pri){
		boolean send = false;
		TotPriority+=pri;
		for(int i=0;i<PriorityLevels.length;i++){
			if(TotPriority>(settings.PriorityLimit*i)){
				if(PriorityLevels[i]==false){
					PriorityLevels[i]=true;
					send=true;
				}
			}
		}
		if(send==true){
			EventMessage em = connection.getMessageFactory().getEventMessage(connection.getConnectionID());
			em.setEventType(EventMessage.EVENT_SOUND);
			em.setPrority(TotPriority);
			connection.sendMessage(em);
		}
	}
}
