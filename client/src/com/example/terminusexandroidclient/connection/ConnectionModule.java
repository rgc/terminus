package com.example.terminusexandroidclient.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import com.example.terminusexandroidclient.connection.ConnectionResult.ConnectionStatus;

/*
 * This class provides the methods to connect to a terminus server, send messages, and disconnect.
 * 
 * This is a pseudo-event based approach:
 *  - All of these functions are performed in their own threads
 *  - Callbacks are made when something interesting happens
 * 	- *It's up to the event handler to implement atomicity
 * 
 */
public class ConnectionModule
{
	private Socket socket;
	private final INetworkCallbacks callback;
	private static final int TIMEOUT = 5000;
	
	public ConnectionModule(INetworkCallbacks c)
	{
		callback = c;
		socket = new Socket();
	}
	
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
					new Thread(new ReceiveThread(socket, callback)).start();
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
	
	public void send(final String message)
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
					out.writeBytes(message + "\n");
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
	
	/*
	 * Thread dedicated to receiving messages
	 * 
	 * This just sits in a while(1) loop waiting for the next message 
	 */
	class ReceiveThread implements Runnable
	{
		Socket socket;
		INetworkCallbacks callback;
		
		public ReceiveThread(Socket s, INetworkCallbacks c)
		{
			this.socket = s;
			this.callback = c;
		}
		
		@Override
		public void run()
		{
			TerminusMessage msg = new TerminusMessage();
			
			while (true)
			{
				try
				{
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					String s = in.readLine();
					msg.message = s;
					if (this.callback != null)
						this.callback.messageReceived(msg);
				}
				catch (IOException e)
				{
					return;
				}
			}
		}
	}
}
