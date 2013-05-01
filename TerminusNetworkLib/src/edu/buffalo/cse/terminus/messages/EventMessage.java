package edu.buffalo.cse.terminus.messages;

import java.util.Date;

public class EventMessage extends TerminusMessage 
{
	public static final int EVENT_CAMERA_MOTION = 0;
	public static final int EVENT_ACCELEROMETER = 1;
	public static final int EVENT_MAGNETOMETER = 2;
	public static final int EVENT_LIGHT = 3;
	public static final int EVENT_SOUND = 4;
	
	public static final int NUM_EVENT_TYPES = 5;
	
	
	public static final int EVENT_START = 0;
	public static final int EVENT_UPDATE = 1;
	public static final int EVENT_END = 2;
	
	protected Date timestamp;
	protected int priority;
	protected int[] sensorPriorities;
	protected int eventMsgType;
	
	public EventMessage()
	{ 
		this("");
	}

	public EventMessage(String id)
	{
		super(id);
		type = TerminusMessage.MSG_EVENT;
		sensorPriorities = new int[NUM_EVENT_TYPES];
		timestamp = new Date();
	}
	
	public Date getTimestamp()
	{
		return this.timestamp;
	}
	
	/*
	 * Return the message type, as in start, update, or stop 
	 */
	public int getEventMsgType()
	{
		return this.eventMsgType;
	}
	
	public void setEventMsgType(int t)
	{
		eventMsgType = t;
	}
	
	public int getTotalPriority()
	{
		return this.priority;
	}
	
	public void setTotalPrority(int p)
	{
		this.priority = p;
	}
	
	public int[] getSensorPriorities()
	{
		return sensorPriorities;
	}
	
	public int getSensorPriority(int type)
	{
		if (type < sensorPriorities.length)
			return sensorPriorities[type];
		else
			return 0;
	}
	
	public void setSensorPriority(int type, int val)
	{
		if (type < sensorPriorities.length)
			sensorPriorities[type] = val;
	}
}
