package edu.buffalo.cse.terminus.client;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import edu.buffalo.cse.terminus.client.sensors.ICameraCallbacks;
import edu.buffalo.cse.terminus.client.sensors.*;
import edu.buffalo.cse.terminus.lowlevel.LowLevelImageMessage;
import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.cse.buffalo.terminus.clientlib.INetworkCallbacks;
import edu.cse.buffalo.terminus.clientlib.TerminusConnection;

public class TerminusController implements ICameraCallbacks
{
	private TerminusSettings settings;
	private TerminusConnection connection;
	private TerminusSensorManager sensorManager;
	private TerminusClientMainActivity activity;
	
	private int totPriority = 0;
	private Lock cycleLock;
	private int sensorTotals[] = new int[EventMessage.NUM_EVENT_TYPES];
	
	private static final int DUTY_CYCLE_INTERVAL = 1000;
	private static final int DECREMENT_CYCLE_INTERVAL = 100;
	private static final int START_DELAY = 5000;
	private static final int EVENT_END_CYCLES = 3;
    
	public static final int PRIORITY_CAP = 5000;
	
	//Priority per second
	private static final int CAMERA_PRI_RATE = 100;
	
	//Decrement weight
	private static final int CAMERA_DEC_RATE = 50;
    private int cameraDec = CAMERA_DEC_RATE / (1000 / DECREMENT_CYCLE_INTERVAL);
	
	//priority level 0 is total priority 0
	public boolean[] PriorityLevels = new boolean[10];
	
	private enum EventState
	{
		delayStart,
		idle,
		inEvent
	}
	
	EventState curState;
	
	public TerminusController(TerminusSettings settings, SensorEventListener listener, 
			INetworkCallbacks networkCallbacks, TerminusClientMainActivity a)
	{
		activity = a;
		
		this.settings = settings;
		connection = new TerminusConnection(networkCallbacks, a);
		connection.setLocation(settings.location);
		connection.setNickname(settings.nickname);
		
		sensorManager = new TerminusSensorManager(settings, listener, this, activity);
		//sensorManager.createSoundAlgos();
		
		clearTotals();
		cycleLock = new ReentrantLock();
		
		curState = EventState.delayStart;
		delayStartThreads();
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
		
		for (int i = 0; i < sensorTotals.length; i++)
			sensorTotals[i] = 0;
	}
	
	private void updateTotals(int totType, int priority)
	{
		if (curState == EventState.delayStart)
			return;
		
		cycleLock.lock();
		
		if (totPriority + priority > PRIORITY_CAP)
		{
			cycleLock.unlock();
			return;
		}
		
		if (totType >= 0 && totType < sensorTotals.length)
		{
			totPriority += priority;
			sensorTotals[totType] += priority;
		}
		
		if (curState == EventState.idle && totPriority > settings.PriorityLimit)
		{
			prepareReport(EventMessage.EVENT_START);
			curState = EventState.inEvent;
			cycleLock.unlock();
			startDutyCycle();
		}
		else
		{
			cycleLock.unlock();
		}
		
	}
	
	private void prepareReport(int messageType)
	{
		EventMessage em = connection.getMessageFactory().getEventMessage(connection.getConnectionID());
		
		em.setEventMsgType(messageType);
		em.setTotalPrority(totPriority);
		
		for (int i = 0; i < sensorTotals.length; i++)
		{
			em.setSensorPriority(i, sensorTotals[i]);
		}
		
		connection.sendMessage(em);	
	}
	
	private void delayStartThreads()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run() 
			{
				try
				{
					Thread.sleep(START_DELAY);
				}
				catch (InterruptedException e) 
				{
				}
				
				cycleLock.lock();
				clearTotals();
				AccelCDFAlgo.FirstAclPri = false;
				MagCDFAlgo.FirstMagPri = false;
				LightCDFAlgo.FirstLitPri = false;
				SoundAlgo.FirstSndPri = false;
				cycleLock.unlock();
				
				curState = EventState.idle;
				
				startDecrementCycle();
			}
		}
		).start();
	}
	
	private void startDutyCycle()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() 
			{
				int idleCount = 0;
				
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
						idleCount = 0;
						prepareReport(EventMessage.EVENT_UPDATE);
					}
					else
					{
						idleCount++;
						if (idleCount >= EVENT_END_CYCLES)
						{
							prepareReport(EventMessage.EVENT_END);
							curState = EventState.idle;
							cycleLock.unlock();
							return;
						}
					}
					
					cycleLock.unlock();
				}
			}
		}).start();
	}
	
	private void startDecrementCycle()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() 
			{
				while (true)
				{
					try
					{
						Thread.sleep(DECREMENT_CYCLE_INTERVAL);
					}
					catch (InterruptedException e) 
					{
					}
					
					cycleLock.lock();
					
					if (totPriority > 0)
					{
						for (int i = 0; i < sensorTotals.length; i++)
						{
							if (sensorTotals[i] > 0)
							{
								int dec = 0;
								switch (i)
								{
								case EventMessage.EVENT_CAMERA_MOTION:
									dec = cameraDec;
									break;
									
								case EventMessage.EVENT_ACCELEROMETER:
									dec = 1;
									if (sensorTotals[i] <= dec)
										AccelCDFAlgo.FirstAclPri = false;
									break;
									
								case EventMessage.EVENT_MAGNETOMETER:
									dec = 1;
									if (sensorTotals[i] <= dec)
										MagCDFAlgo.FirstMagPri = false;
									break;
									
								case EventMessage.EVENT_LIGHT:
									dec = 1;
									if (sensorTotals[i] <= dec)
										LightCDFAlgo.FirstLitPri = false;
									break;
									
								case EventMessage.EVENT_SOUND:
									dec = 1;
									if (sensorTotals[i] <= dec)
										SoundAlgo.FirstSndPri = false;
									break;
								}
								
								if (sensorTotals[i] <= dec)
								{
									totPriority -= sensorTotals[i];
									sensorTotals[i] = 0;
								}
								else
								{
									totPriority -= dec;
									sensorTotals[i] -= dec;
								}
							}
						}
					}
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
			totalType = EventMessage.EVENT_ACCELEROMETER;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			totalType = EventMessage.EVENT_MAGNETOMETER;
			break;
		case Sensor.TYPE_LIGHT:
			totalType = EventMessage.EVENT_LIGHT;
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
		updateTotals(EventMessage.EVENT_CAMERA_MOTION, 20);
	}
	
	public void onCameraMotionDetected(byte[] imageBytes, float fps)
	{
		if (fps == 0)
			return;
		
		int cameraPri = (int)(CAMERA_PRI_RATE / fps);
		
		updateTotals(EventMessage.EVENT_CAMERA_MOTION, cameraPri);
		
		if (curState != EventState.inEvent)
			return;
		
		LowLevelImageMessage im = new LowLevelImageMessage(connection.getConnectionID());
		im.setImage(imageBytes);
		connection.sendImage(im);
	}
	
	public void soundEventSensed(int pri)
	{
		updateTotals(EventMessage.EVENT_SOUND, pri);
        
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
