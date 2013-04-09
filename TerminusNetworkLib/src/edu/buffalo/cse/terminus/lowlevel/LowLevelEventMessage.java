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
			
			//Data (image) = Length + Byte Array
			if (this.data == null || this.data.length == 0)
			{
				os.write(LowLevelHelper.getIntArray((0)));
			}
			else
			{
				os.write(LowLevelHelper.getIntArray(this.data.length));
				os.write(this.data);
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
		
		//Event Type
		if (pos + 4 <= msg.length)
			this.eventType = LowLevelHelper.readInt(msg, pos);
		pos += 4;	//int length
		
		//Priority
		if (pos + 4 <= msg.length)
			this.priority = LowLevelHelper.readInt(msg, pos);
		pos += 4;	//int length
		
		//Data Length
		int dlen = 0;
		if (pos + 4 <= msg.length)
		{
			dlen = LowLevelHelper.readInt(msg, pos);
			pos += 4;	//int length
		}
		this.data = new byte[dlen];
		
		//Misc Data (i.e. image file)
		//Make sure we actually received dlen bytes.
		if (dlen > 0 && (pos + dlen <= msg.length))
		{
			for (int i = 0; i < dlen; i++)
			{
				this.data[i] = msg[pos++];
			}
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
