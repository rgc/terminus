package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import edu.buffalo.cse.terminus.messages.UnregisterMessage;

public class LowLevelUnregisterMessage extends UnregisterMessage implements
		ILowLevelMessage {

	public LowLevelUnregisterMessage() 
	{
		
	}

	public LowLevelUnregisterMessage(String id) 
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
			
			/* NO PAYLOAD */
			
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
		/*
		 * No additional payload for this message type
		 */
	}

}
