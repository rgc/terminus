package edu.buffalo.cse.terminus.client.network.lowlevel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

import edu.buffalo.cse.terminus.client.network.ATerminusClient;
import edu.buffalo.cse.terminus.client.network.ConnectionResult;
import edu.buffalo.cse.terminus.client.network.ConnectionResult.ConnectionStatus;
import edu.buffalo.cse.terminus.lowlevel.ILowLevelMessage;
import edu.buffalo.cse.terminus.lowlevel.LowLevelReader;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class LowLevelClient extends ATerminusClient
{
	private Socket socket;
	private static final int TIMEOUT = 5000;
	private static boolean TEST_DUPLICATE_MESSAGES = false;
	
	public LowLevelClient()
	{
		socket = new Socket();
	}

	@Override
	public void connect(final String host, final int port)
	{
		/* Create a generic socket so we can connect with a timeout value */
		socket = new Socket();
		
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				ConnectionResult result = null;
				try
				{
					SocketAddress address = new InetSocketAddress(host, port);
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
							catch (IOException e) {
								// TODO: Handle this!
								return;
							}
							
							while (true)
							{
								try 
								{
									TerminusMessage message = reader.getNextMessage();
									if (LowLevelClient.this.callback != null)
									{
										LowLevelClient.this.callback.messageReceived(message);
									}
								} 
								catch (IOException e) 
								{
									//TODO: Handle this!
									return;
								}
							}	
						}
					}).start();
					
					result = new ConnectionResult(ConnectionStatus.Success, null);
				}
				catch (UnknownHostException e)
				{
					result = new ConnectionResult(ConnectionStatus.UnknownHost, e);
				}
				catch (SocketTimeoutException e)
				{
					result = new ConnectionResult(ConnectionStatus.TimeOut, e);
				}
				catch (IOException e)
				{
					result = new ConnectionResult(ConnectionStatus.IOError, e);
				}
				
				if (callback != null)
					callback.connectionFinished(result);
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
				ConnectionResult result = null;
				
				try
				{
					socket.close();
					result = new ConnectionResult(ConnectionStatus.Success, null);
				}
				catch (IOException e)
				{
					result = new ConnectionResult(ConnectionStatus.IOError, e);
				}
				
				if (callback != null)
					callback.disconnectFinished(result);
			}
		}).start();
	}

	@Override
	public void sendMessage(final TerminusMessage m)
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				DataOutputStream out;
				ConnectionResult result;
				
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
					result = new ConnectionResult(ConnectionStatus.Success, null);
				}
				catch (IOException e)
				{
					result = new ConnectionResult(ConnectionStatus.IOError, e);
				}
				
				if (callback != null)
					callback.messageFinished(result);
				
			}
		}).start();
	}
}
