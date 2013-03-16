package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import edu.buffalo.cse.terminus.messages.RegisterMessage;

public class LowLevelRegisterMessage extends RegisterMessage implements ILowLevelMessage
{	
	public LowLevelRegisterMessage()
	{
		super();
	}
	
	public LowLevelRegisterMessage(String id)
	{
		super(id);
	}
	
	@Override
	public void loadFromBytes(byte[] msg)
	{
		this.regType = LowLevelHelper.readInt(msg, LowLevelHelper.getPayloadStart(this));
	}
	
	@Override
	public byte[] getBytes() 
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try
		{	
			LowLevelHelper.putMessageHeader(this, os);
			
			//Payload is the reg type
			os.write(LowLevelHelper.getIntArray((this.regType)));
			
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
