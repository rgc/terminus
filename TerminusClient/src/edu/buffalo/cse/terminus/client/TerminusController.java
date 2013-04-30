package edu.buffalo.cse.terminus.client;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	
	private int totPriority = 0;
	
	private static final int CAMERA_TOTALS = 0;
	private static final int SOUND_TOTALS = 1;
	private static final int MAG_TOTALS = 2;
	private static final int ACCEL_TOTALS = 3;
	private static final int LIGHT_TOTALS = 4;
	
	private static final int NUM_TOTALS = 5;
	private int sensorTotals[] = new int[NUM_TOTALS];
    
	private static final int DUTY_CYCLE_INTERVAL = 1000;
	private static final int START_DELAY = 5000;
	private Lock cycleLock;
    
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
		
		clearTotals();
		cycleLock = new ReentrantLock();
		
		startDutyCycle();
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
	
	public int getTotalPriority()
	{
		return this.totPriority;
	}
	
	private void clearTotals()
	{
		totPriority = 0;
		
		for (int i = 0; i < NUM_TOTALS; i++)
			sensorTotals[i] = 0;
	}
	
	private void updateTotals(int totType, int priority)
	{
		cycleLock.lock();
		
		if (totType >= 0 && totType < NUM_TOTALS)
		{
			totPriority += priority;
			sensorTotals[totType] += priority;
		}
		
		cycleLock.unlock();
	}
	
	private void prepareReport()
	{
		EventMessage em = connection.getMessageFactory().getEventMessage(connection.getConnectionID());
		em.setPrority(totPriority);
		em.setEventType(EventMessage.EVENT_CAMERA_MOTION);
		connection.sendMessage(em);	
	}
	
	private void startDutyCycle()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() 
			{
				//First, get the algos up and running
				try 
				{
					Thread.sleep(START_DELAY);
				}
				catch (InterruptedException e) 
				{
				}
				
				cycleLock.lock();
				clearTotals();
				sensorManager.clearSensorsPriority();
				cycleLock.unlock();
				
				while (true)
				{		
					try
					{
						Thread.sleep(DUTY_CYCLE_INTERVAL);
					}
					catch (InterruptedException e) 
					{
					}
					
					cycleLock.lock();
					
					if (totPriority > 0 && totPriority >= settings.PriorityLimit)
					{
						prepareReport();
					}
					
					sensorManager.clearSensorsPriority();
					clearTotals();
					cycleLock.unlock();
				}
			}
		}).start();
	}
	
	public void sensorEventSensed(int type, int pri)
	{
		int totalType = 0;
		
		switch (type)
		{
		case Sensor.TYPE_ACCELEROMETER:
			totalType = ACCEL_TOTALS;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			totalType = MAG_TOTALS;
			break;
		case Sensor.TYPE_LIGHT:
			totalType = LIGHT_TOTALS;
			break;
		}
		
		updateTotals(totalType, pri);
        
        /*
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
			// Convert to Terminus Message event types
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
        */
	}
	
	public void onCameraMotionDetected()
	{
		updateTotals(CAMERA_TOTALS, 1000);
	}
	
	public void onCameraMotionDetected(byte[] imageBytes)
	{
		updateTotals(CAMERA_TOTALS, 1000);
		
		//LowLevelImageMessage im = new LowLevelImageMessage(connection.getConnectionID());
		//im.setImage(imageBytes);
		//connection.sendImage(im);
	}
	
	public void soundEventSensed(int pri)
	{
		updateTotals(SOUND_TOTALS, pri);
        
        /*
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
        */
	}
}
