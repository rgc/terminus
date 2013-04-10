package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import edu.buffalo.cse.terminus.messages.ImageMessage;

public class LowLevelImageMessage extends ImageMessage implements ILowLevelMessage 
{
	public LowLevelImageMessage() 
	{
		super();
	}

	public LowLevelImageMessage(String id) 
	{
		super(id);
	}

	@Override
	public byte[] getBytes() 
	{
		//Create the normal byte version of the message
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		try
		{
			LowLevelHelper.putMessageHeader(this, os);
			os.write(this.getImage());
			
			byte[] message = os.toByteArray();
			LowLevelHelper.putMessageLength(message);	
			return message;
		}
		catch (IOException e)
		{
			return null;
		}
	}

	@Override
	public void loadFromBytes(byte[] msg) 
	{
		int imageStart = LowLevelHelper.getPayloadStart(this);
		image = new byte[msg.length - imageStart];
		
		for (int i = 0; i < image.length; i++)
		{
			image[i] = msg[imageStart + i]; 
		}
	}

}
