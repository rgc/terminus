package edu.buffalo.cse.terminus.messages;

import java.util.Date;

public class EventMessage extends TerminusMessage 
{
	protected Date timestamp;
	
	//TODO Add more event fields
	
	public EventMessage() 
	{
		super();
		this.type = TerminusMessage.MSG_EVENT;
		this.timestamp = new Date();
	}

	public EventMessage(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_EVENT;
		this.timestamp = new Date();
	}
	
	public Date getTimestamp()
	{
		return this.timestamp;
	}
}
