package edu.buffalo.cse.terminus.client.network.lowlevel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import edu.buffalo.cse.terminus.client.network.ATerminusClient;
import edu.buffalo.cse.terminus.lowlevel.ILowLevelMessage;
import edu.buffalo.cse.terminus.lowlevel.LowLevelReader;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class LowLevelClient extends ATerminusClient
{
	private Socket socket;
	private static final int TIMEOUT = 5000;
	private static boolean TEST_DUPLICATE_MESSAGES = false;
	
	private String eventIPAddress;
	private int eventPort;
	
	public LowLevelClient()
	{
		socket = new Socket();
	}
	
	private void dropConnection()
	{
		try 
		{
			socket.close();
		}
		catch (IOException e)
		{
		}
		
		if (this.callback != null)
		{
			this.callback.onConnectionDropped();	
		}
	}
	
	@Override
	public void connect(String host, int port)
	{
		eventIPAddress = host;
		eventPort = port;
		
		/* Create a generic socket so we can connect with a timeout value */
		socket = new Socket();
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					SocketAddress address = new InetSocketAddress(LowLevelClient.this.eventIPAddress, 
							LowLevelClient.this.eventPort);
					socket.connect(address, TIMEOUT);
					
					/*
					 * Thread dedicated to receiving messages
					 * 
					 * This just sits in a while(1) loop waiting for the next message 
					 */
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							LowLevelReader reader;
							try {
								reader = new LowLevelReader(LowLevelClient.this.socket);
							} 
							catch (IOException e) 
							{
								LowLevelClient.this.dropConnection();
								return;
							}
							
							while (true)
							{
								try 
								{
									TerminusMessage message = reader.getNextMessage();
									
									if (LowLevelClient.this.callback != null)
										LowLevelClient.this.callback.onMessageReceived(message);
								} 
								catch (IOException e) 
								{
									LowLevelClient.this.dropConnection();
									return;
								}
							}	
						}
					}).start();
					
					if (callback != null)
						callback.onConnectionComplete();
				}
				catch (UnknownHostException e)
				{
					if (LowLevelClient.this.callback != null)
						LowLevelClient.this.callback.onConnectionError(e);
				}
				catch (SocketTimeoutException e)
				{
					if (LowLevelClient.this.callback != null)
						LowLevelClient.this.callback.onConnectionError(e);
				}
				catch (IOException e)
				{
					if (LowLevelClient.this.callback != null)
						LowLevelClient.this.callback.onConnectionError(e);
				}
			}
		}).start();
	}
	
	@Override
	public void disconnect()
	{
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
				
				if (callback != null)
					callback.onDisconnectComplete();
			}
		}).start();
	}

	@Override
	public void sendMessage(final TerminusMessage m)
	{
		if (this.socket == null)
			return;
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				DataOutputStream out;
				
				try
				{
					out = new DataOutputStream(socket.getOutputStream());
					byte[] b  = ((ILowLevelMessage) m).getBytes();
					if (b == null)
						return;
					
					if (LowLevelClient.TEST_DUPLICATE_MESSAGES)
					{
						byte[] b2 = new byte[b.length * 2];
						ByteBuffer bb = ByteBuffer.wrap(b2);
						bb.put(b);
						bb.put(b);
						out.write(b2);
					}
					else
					{
						out.write(b);
					}
					
					if (callback != null)
						callback.onSendComplete();
				}
				catch (IOException e)
				{
					if (callback != null)
						callback.onMessageFailed(m);
				}
			}
		}).start();
	}
}
