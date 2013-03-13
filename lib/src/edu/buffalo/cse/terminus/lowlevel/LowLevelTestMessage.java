package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.buffalo.cse.terminus.messages.TestMessage;

public class LowLevelTestMessage extends TestMessage implements ILowLevelMessage
{
	public LowLevelTestMessage(String id)
	{
		super(id);
		this.type = TerminusMessage.MSG_TEST;
	}
	
	public LowLevelTestMessage(byte[] msg)
	{
		super();		// No id yet
		this.type = TerminusMessage.MSG_TEST;
		loadFromBytes(msg);
	}
	
	private void loadFromBytes(byte[] msg)
	{
		int len = msg.length;
		
		if (len < 10)	// Length + Type + ID
			return;
		
		/* Start with the ID */
		int pos = 8;
		
		/* Read in the ID */
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while (pos < len)
		{
			if (msg[pos] == '\0')
			{
				++pos;
				break;
			}
			os.write(msg[pos++]);
		}
		
		this.uid = os.toString();
		
		/* Read in the message */
		os = new ByteArrayOutputStream();
		while (pos < len)
		{
			if (msg[pos] == '\0')
			{
				++pos;
				break;
			}
			os.write(msg[pos++]);
		}
		
		this.message = os.toString();
	}
	
	@Override
	public byte[] getBytes()
	{
		ByteArrayOutputStream messageOS = new ByteArrayOutputStream();
		
		try
		{
			//Place older for length
			messageOS.write(0);
			messageOS.write(0);
			messageOS.write(0);
			messageOS.write(0);
			
			/* Message Type */
			ByteBuffer typeBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
			typeBuffer.putInt(this.type);
			messageOS.write(typeBuffer.array());
			
			/* ID */
			messageOS.write(this.uid.getBytes());
			messageOS.write(0);		//Null terminator
			
			/* Message Payload */
			messageOS.write(this.message.getBytes());
			messageOS.write(0);
			
			/* Prepend length */
			ByteBuffer lengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
			lengthBuffer.putInt(messageOS.size());
			
			byte[] lenArray = lengthBuffer.array();
			byte[] messageArray = messageOS.toByteArray();
			
			for (int i = 0; i < 4; i++)
				messageArray[i] = lenArray[i];
			
			return messageArray;
		}
		catch (IOException e) 
		{
			return null;
		}
	}
	
}
