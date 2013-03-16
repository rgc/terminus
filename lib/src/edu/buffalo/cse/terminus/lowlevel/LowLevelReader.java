package edu.buffalo.cse.terminus.lowlevel;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class LowLevelReader 
{
	private static final int BUFFER_SIZE = 1024;
	
	private byte[] lengthBuffer;
	private byte[] buffer;
	private int position;
	private int numBytes;
	private ByteBuffer msgLenBuffer;
	InputStream in;
	
	public LowLevelReader(Socket s) throws IOException
	{
		buffer = new byte[BUFFER_SIZE];
		lengthBuffer = new byte[4];
		msgLenBuffer = ByteBuffer.allocate(4);
		msgLenBuffer.order(ByteOrder.BIG_ENDIAN);
		in = s.getInputStream();
		position = 0;
		numBytes = 0;
	}
	
	/*
	 * Get one TerminusMessage from the given socket
	 */
	public TerminusMessage getNextMessage() throws IOException
	{
		/*
		 * First step, get the message length.
		 * 
		 * At this point, we one of the following:
		 * 	a) No data - In this case, we read data to get into case b or c.
		 *  
		 *  b) Some data excluding the length - In this case, read more data
		 *     to get into case c.
		 *  
		 *  c) Some data including the length - In this case we can determine how
		 *  	more data we need to read until we have the whole message. 
		 *  
		 */
		if (numBytes == 0)
		{
			// Case a
			while (numBytes <= 0)
				numBytes = in.read(buffer);
		}
		else if (numBytes > 0 && numBytes < 4)
		{
			/*
			 * Case b
			 * The data can be anywhere in the array.  We'll move the bytes to the
			 * the front of the array.
			 */
			
			//Copy to temp array
			int j = 0;
			for (int i = position; i < numBytes; i++)
				lengthBuffer[j++] = buffer[i];
			
			//Copy the length bytes back into the array
			for (int i = 0; i < numBytes; i++)
				buffer[i] = lengthBuffer[i];
			
			/*
			 * Read some more data 
			 */
			int newBytes = 0;
			
			while (newBytes <= 0)
				newBytes = in.read(buffer, numBytes, BUFFER_SIZE - numBytes);
			
			numBytes += newBytes;
		}
		
		// Case b
		// If we didn't get 4 bytes on that go, just start over.
		if (numBytes < 4)
		{
			return getNextMessage();
		}
		
		//We can't do zero copy here since our buffer size might be less than the
		//message size.
		
		/* Get the message length */
		msgLenBuffer.position(0);
		msgLenBuffer.put(buffer, 0, 4).position(0);
		int remaining = msgLenBuffer.getInt();
		
		/* Buffers for the message */
		byte[] message = new byte[remaining];
		ByteBuffer msgBuffer = ByteBuffer.wrap(message);
		
		/*
		 * Get the whole message.
		 * 
		 * Note: There may be bytes left over if messages are stacked.
		 * 		 position could be any point in the array.
		 * 		 numBytes will be the number of unprocessed bytes.
		 */
		while (remaining > 0)
		{
			if (numBytes >= remaining)
			{
				msgBuffer.put(buffer, position, remaining);
				numBytes -= remaining;
				position = (numBytes == 0) ? 0 : remaining;
				remaining = 0;
			}
			else
			{
				msgBuffer.put(buffer, position, numBytes);
				remaining -= numBytes;
				numBytes = 0;
				position = 0;
				
				/* Get more data */
				while (numBytes <= 0)
					numBytes = in.read(buffer);
			}
		}
		return getTypeSpecificMessage(message);
	}
	
	private TerminusMessage getTypeSpecificMessage(byte[] message)
	{
		int msgType = LowLevelHelper.readInt(message, LowLevelHelper.TYPE_START);
		String id = LowLevelHelper.readString(message, LowLevelHelper.ID_START);
		TerminusMessage m = null;
		
		switch (msgType)
		{
			case TerminusMessage.MSG_REGISTER:
				m = new LowLevelRegisterMessage(id);
				break;
			case TerminusMessage.MSG_REG_RESPONSE:
				m = new LowLevelRegResponse(id);
				break;
			case TerminusMessage.MSG_TEST:
				m = new LowLevelTestMessage(id);
				break;
				
			default:
				return null;
		}
		((ILowLevelMessage)m).loadFromBytes(message);
		return m;
	}
}
