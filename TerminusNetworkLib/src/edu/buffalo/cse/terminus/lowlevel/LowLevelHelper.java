package edu.buffalo.cse.terminus.lowlevel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

/*
 * It would have been nice to let our low level messages inherit from a 
 * Low Level Message hierarchy. However, Java does not support multiple inheritance
 * so we're left with hacking this a bit.
 * 
 * Anyways, these are common LowLevelMessage tasks for reading and writing messages
 */
public class LowLevelHelper 
{
	public static final int TYPE_START = 4;
	public static final int ID_START = 8;
	
	//Starting position - at least one character (+1 for null terminator)
	public static final int MIN_LENGTH = 10; 
	
	public static String readString(byte[] message, int start)
	{
		int pos = start;
		final int len = message.length;
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		while (pos < len)
		{
			if (message[pos] == '\0')
			{
				++pos;
				break;
			}
			os.write(message[pos++]);
		}
		
		return os.toString();
	}
	
	public static int readInt(byte[] message, int start)
	{
		/* Read in the type */
		ByteBuffer typeBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
		typeBuffer.put(message, start, 4).position(0);
		return typeBuffer.getInt();
	}
	
	public static int getPayloadStart(TerminusMessage m)
	{
		return ID_START + m.getID().length() + 1;
	}
	
	public static byte[] getIntArray(int val)
	{
		ByteBuffer typeBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
		typeBuffer.putInt(val);
		return typeBuffer.array();
	}
	
	public static void putMessageHeader(TerminusMessage message, ByteArrayOutputStream messageOS) throws IOException
	{
		//Place older for length
		messageOS.write(0);
		messageOS.write(0);
		messageOS.write(0);
		messageOS.write(0);
		
		/* Message Type */
		messageOS.write(getIntArray((message.getMessageType())));
		
		/* ID */
		LowLevelHelper.writeString(message.getID(), messageOS);
	}
	
	public static void putMessageLength(byte[] message)
	{
		ByteBuffer lengthBuffer = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
		lengthBuffer.putInt(message.length);
		
		byte[] lenArray = lengthBuffer.array();
		
		for (int i = 0; i < 4; i++)
			message[i] = lenArray[i];
	}
	
	public static void writeString(String s, ByteArrayOutputStream os) throws IOException
	{
		os.write(s.getBytes());
		os.write(0);	//Null terminator
	}
}
