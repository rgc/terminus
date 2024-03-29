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
		int pos = LowLevelHelper.getPayloadStart(this);
		this.regType = LowLevelHelper.readInt(msg, pos);
		pos += 4;
		
		if (msg.length > pos)	//At least 1 more byte
		{
			this.location = LowLevelHelper.readString(msg, pos);
			pos += location.length() + 1;	//Plus 1 for null terminator
		}
		
		if (msg.length > pos)
		{
			this.nickname = LowLevelHelper.readString(msg, pos);
			pos += nickname.length() + 1;
		}
	}
	
	@Override
	public byte[] getBytes() 
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		try
		{	
			LowLevelHelper.putMessageHeader(this, os);
			
			//Payload = Reg type, location, nickname
			os.write(LowLevelHelper.getIntArray((this.regType)));
			LowLevelHelper.writeString(this.location, os);
			LowLevelHelper.writeString(this.nickname, os);
			
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
