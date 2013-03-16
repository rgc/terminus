package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import edu.buffalo.cse.terminus.messages.RegistrationResponse;

public class LowLevelRegResponse extends RegistrationResponse implements ILowLevelMessage 
{

	public LowLevelRegResponse() 
	{
		super();
	}

	public LowLevelRegResponse(String id) 
	{
		super(id);
	}

	@Override
	public byte[] getBytes() 
	{
		/*
		 * Payload Format: 	
		 * 		Result		int
		 */
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try
		{
			LowLevelHelper.putMessageHeader(this, os);
			
			os.write(LowLevelHelper.getIntArray(this.result));
			
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
		 * The payload for this type of message only consists of
		 * the result.
		 */
		int pos = LowLevelHelper.getPayloadStart(this);
		this.result = LowLevelHelper.readInt(msg, pos);
	}
}
