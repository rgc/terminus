package edu.buffalo.cse.terminus.client.network.lowlevel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.buffalo.cse.terminus.lowlevel.LowLevelImageMessage;

public class LowLevelImageClient 
{
	public static final int CHUNK_SIZE = 1024;
	private Socket socket;
	private String imageIPAddress;
	private int imagePort;
	
	/*
	 * It is vital that the send thread finish before the next one
	 * begins.  The reason is because the images are sent over TCP/IP
	 * in chunks.  The server side doesn't do any fragment re-assembly;
	 * it assumes in-order delivery of chunks 
	 */
	private Lock sendLock;
	
	public LowLevelImageClient(String host, int port)
	{
		imageIPAddress = host;
		imagePort = port;
		sendLock = new ReentrantLock();
	}

	public void disconnect() 
	{
		if (socket == null)
			return;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					socket.close();
				}
				catch (IOException e)
				{
				}
			}
		}).start();
	}
	
	public void sendMessage(final LowLevelImageMessage m) 
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				//See above
				sendLock.lock();
				
				/*
				 * Try to connect. 
				 * 
				 * We do this as needed to minimize connection maintenance. This connection
				 * can be considered a sub-connection of the event server connection. 
				 */
				if (socket == null || socket.isClosed())
				{
					try
					{
						socket = new Socket();
						SocketAddress address = new InetSocketAddress(imageIPAddress, imagePort);
						socket.connect(address, LowLevelClient.TIMEOUT);
					}
					catch (Exception e)
					{
						sendLock.unlock();
						return;
					}
				}
				
				/*
				 * We're connected, send the message in chunks.
				 */
				try
				{
					byte[] b  = m.getBytes();
					if (b == null)
					{
						sendLock.unlock();
						return;
					}
					
					int remaining = b.length;
					int pos = 0;
					DataOutputStream out = new DataOutputStream(socket.getOutputStream());
					
					while (remaining > 0)
					{
						int sendAmt = (remaining > CHUNK_SIZE) ? CHUNK_SIZE : remaining;
						out.write(b, pos, sendAmt);
						pos += sendAmt;
						remaining -= sendAmt;
					}
					socket.close();
				}
				catch (Exception e)
				{
					
				}
				
				sendLock.unlock();
			}
		}).start();
	}
}
