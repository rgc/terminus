package edu.buffalo.cse.terminus.messages;

import java.util.Date;

public class EventMessage extends TerminusMessage 
{
	public static final int EVENT_CAMERA_UNKNOWN = 0;
	public static final int EVENT_CAMERA_MOTION = 1; 
	public static final int EVENT_ACCELEROMETER = 2;
	public static final int EVENT_MAGNETOMETER = 3;
	public static final int EVENT_LIGHT = 4;
	public static final int EVENT_SOUND = 5;
	
	protected int eventType;
	protected Date timestamp;
	protected int priority;

	public EventMessage()
	{ 
		this("");
	}

	public EventMessage(String id)
	{
		super(id);
		this.type = TerminusMessage.MSG_EVENT;
		this.eventType = EVENT_CAMERA_UNKNOWN;
		this.timestamp = new Date();
	}
	
	public Date getTimestamp()
	{
		return this.timestamp;
	}
	
	public int getEventType()
	{
		return this.eventType;
	}
	
	public void setEventType(int t)
	{
		this.eventType = t;
	}
	
	public int getPriority()
	{
		return this.eventType;
	}
	
	public void setPrority(int p)
	{
		this.priority = p;
	}
	
}
