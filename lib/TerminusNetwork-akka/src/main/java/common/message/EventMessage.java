package edu.buffalo.cse.terminus.common.message;

import java.sql.Timestamp;

public class EventMessage extends TerminusMessage {

	// even if this message is queued, this will give us
	// the time the event was recorded.
	private final Timestamp eventTimestamp;

	public EventMessage(String id) {
		super(id);
		this.eventTimestamp = new java.sql.Timestamp(System.currentTimeMillis());
	}

	public EventMessage(long time, String id) {
		super(id);
		this.eventTimestamp = new java.sql.Timestamp(time);
	}

	public Timestamp getTimestamp() {
		return this.eventTimestamp;
	}

  
}
