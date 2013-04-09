package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import edu.buffalo.cse.terminus.messages.TestMessage;

public class LowLevelTestMessage extends TestMessage implements ILowLevelMessage
{	
	public LowLevelTestMessage(String id) 
	{
		super(id);
	}

	@Override
	public void loadFromBytes(byte[] msg)
	{
		if (msg.length < LowLevelHelper.MIN_LENGTH)
			return;
		
		this.message = LowLevelHelper.readString(msg, LowLevelHelper.getPayloadStart(this));
	}
	
	@Override
	public byte[] getBytes()
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try
		{
			LowLevelHelper.putMessageHeader(this, os);
			
			/* This is the payload for test messages */
			os.write(this.message.getBytes());
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
}
