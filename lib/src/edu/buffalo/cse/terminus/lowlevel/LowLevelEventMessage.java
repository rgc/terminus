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
			os.write(datetime.getBytes());
			os.write(0);
			
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
		
		String datetime = LowLevelHelper.readString(msg, LowLevelHelper.getPayloadStart(this));
		if (datetime.length() != 14)
			return;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		
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
