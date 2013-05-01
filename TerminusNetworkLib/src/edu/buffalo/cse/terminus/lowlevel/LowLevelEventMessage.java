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
			
			os.write(LowLevelHelper.getIntArray(this.eventMsgType));
			os.write(LowLevelHelper.getIntArray(this.priority));
			
			for (int i = 0; i < sensorPriorities.length; i++)
			{
				if (sensorPriorities[i] > 0)
				{
					os.write(LowLevelHelper.getIntArray(i));
					os.write(LowLevelHelper.getIntArray(sensorPriorities[i]));
				}
			}
			
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
		pos += 15;	//14 for string + null terminator
		
		//Event Message Type
		if (pos + 4 <= msg.length)
			this.eventMsgType = LowLevelHelper.readInt(msg, pos);
		pos += 4;	//int length
		
		//Total Priority
		if (pos + 4 <= msg.length)
			this.priority = LowLevelHelper.readInt(msg, pos);
		pos += 4;	//int length
		
		//Individual priorities
		while (pos + 8 <= msg.length)
		{
			int type = LowLevelHelper.readInt(msg, pos);
			pos += 4;
			
			int val = LowLevelHelper.readInt(msg, pos);
			pos += 4;
			
			this.setSensorPriority(type, val);
		}
				
		try 
		{
			this.timestamp = sdf.parse(datetime);
		} 
		catch (ParseException e) 
		{
			this.timestamp = null;
			return;
		}
	}

}
