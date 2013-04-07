package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import edu.buffalo.cse.terminus.messages.EventMessage;

public class LowLevelEventMessage extends EventMessage implements ILowLevelMessage 
{
	public LowLevelEventMessage() 
	{
		super();
	}

	public LowLevelEventMessage(String id) 
	{
		super(id);
	}

	@Override
	public byte[] getBytes() 
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try
		{
			LowLevelHelper.putMessageHeader(this, os);
			
			/* Event Payload */
			String datetime = new SimpleDateFormat("yyyyMMddHHmmss").format(this.timestamp);
			LowLevelHelper.writeString(datetime, os);
			
			os.write(LowLevelHelper.getIntArray((this.eventType)));
			os.write(LowLevelHelper.getIntArray((this.priority)));
			
			byte[] fullMessage = os.toByteArray();
			LowLevelHelper.putMessageLength(fullMessage);
			return fullMessage;
		}
		catch (IOException e) 
		{
			return null;
		}
	}

	@Override
	public void loadFromBytes(byte[] msg) 
	{
		if (msg.length < LowLevelHelper.MIN_LENGTH)
			return;
		
		int pos = LowLevelHelper.getPayloadStart(this);
		
		String datetime = LowLevelHelper.readString(msg, pos);
		if (datetime.length() != 14)
			return;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
		pos += 15;	// + null terminator
		if (pos + 4 < msg.length)
			this.eventType = LowLevelHelper.readInt(msg, pos);
		
		pos += 4;
		if (pos + 4 < msg.length)
			this.priority = LowLevelHelper.readInt(msg, pos);
		
		try 
		{
			this.timestamp = sdf.parse(datetime);
		} 
		catch (ParseException e) {
			this.timestamp = null;
			return;
		}
	}

}
