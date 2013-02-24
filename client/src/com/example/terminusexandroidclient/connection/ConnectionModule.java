package com.example.terminusexandroidclient.connection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import com.example.terminusexandroidclient.connection.ConnectionResult.ConnectionStatus;
import android.os.AsyncTask;

/*
 * This class provides the methods to connect to a terminus server, send messages, and disconnect.
 * All of these functions are performed in Asynchronous tasks to guarantee they run in the UI thread.
 * This can get messy quickly...
 * 
 */
public class ConnectionModule
{
	public Socket socket;
	private INetworkCallbacks callback;
		
	public ConnectionModule(INetworkCallbacks c)
	{
		callback = c;
		socket = new Socket();
	}
	
	public void connect(String host, int port)
	{
		/* Create a generic socket so we can connect with a timeout value */
		socket = new Socket();
		new ConnectToServer(socket, callback).execute(host, port);
	}
	
	public void send(String message)
	{
		new SendMessageToServer(socket, callback).execute(message);
	}
	
	public void disconnect()
	{
		new DisconnectFromServer(socket, callback).execute();	
	}
}

class ConnectToServer extends AsyncTask<Object, Void, ConnectionResult> {
	
	private static final int TIMEOUT = 3000;
	private INetworkCallbacks callbacks;
	private Socket socket;
	
	public ConnectToServer(Socket s, INetworkCallbacks c)
	{
		socket = s;
		callbacks = c;
	}
	
	@Override
	protected ConnectionResult doInBackground(Object... params)
	{
		String ip = (String) params[0];
		int port = (Integer) params[1];
		
		try
		{
			SocketAddress address = new InetSocketAddress(ip, port);
			socket.connect(address, TIMEOUT);
			new Thread(new ReceiveThread(socket, callbacks)).start();
		}
		catch (UnknownHostException e)
		{
			return new ConnectionResult(ConnectionStatus.UnknownHost, e);
		}
		catch (SocketTimeoutException e)
		{
			return new ConnectionResult(ConnectionStatus.TimeOut, e);
		}
		catch (IOException e)
		{
			return new ConnectionResult(ConnectionStatus.IOError, e);
		}
		
		return new ConnectionResult(ConnectionStatus.Success, null);
	}
	
	@Override
    protected void onPostExecute(ConnectionResult result) {
		if (this.callbacks != null)
			this.callbacks.connectionFinished(result);
	}
}

class SendMessageToServer extends AsyncTask<String, Void, ConnectionResult>
{
	private INetworkCallbacks callbacks;
	private Socket socket;

	public SendMessageToServer(Socket s, INetworkCallbacks c)
	{
		socket = s;
		callbacks = c;
	}
	
	@Override
	protected ConnectionResult doInBackground(String... params)
	{
		String message = (String) params[0];
		DataOutputStream out;
		
		try
		{
			out = new DataOutputStream(this.socket.getOutputStream());
			out.writeBytes(message + "\n");
		} 
		catch (IOException e)
		{
			return new ConnectionResult(ConnectionStatus.IOError, e);
		}
		
		return new ConnectionResult(ConnectionStatus.Success, null);
	}
	
	@Override
    protected void onPostExecute(ConnectionResult result) {
		if (this.callbacks != null)
			this.callbacks.messageFinished(result);
	}
}

class DisconnectFromServer extends AsyncTask<Void, Void, ConnectionResult>
{
	private INetworkCallbacks callbacks;
	private Socket socket;
	
	public DisconnectFromServer(Socket s, INetworkCallbacks c)
	{
		socket = s;
		callbacks = c;
	}
	
	@Override
	protected ConnectionResult doInBackground(Void... params)
	{
		try
		{
			socket.close();
		}
		catch (IOException e)
		{
			return new ConnectionResult(ConnectionStatus.IOError, e);
		}
		
		return new ConnectionResult(ConnectionStatus.Success, null);
	}
	
	@Override
	    protected void onPostExecute(ConnectionResult result) {
			if (this.callbacks != null)
				this.callbacks.disconnectFinished(result);
		}
}

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
				new ReceiveCallback(this.callback).execute(msg);
			}
			catch (IOException e)
			{
				return;
			}
		}
	}
}

class ReceiveCallback extends AsyncTask<TerminusMessage, Void, TerminusMessage>
{
	INetworkCallbacks callback;
	
	public ReceiveCallback(INetworkCallbacks c)
	{
		this.callback = c;
	}
	
	@Override
	protected TerminusMessage doInBackground(TerminusMessage... params)
	{	
		return params[0];
	}
	
	@Override
    protected void onPostExecute(TerminusMessage result) 
	{
		if (this.callback != null)
			this.callback.messageReceived(result);
	}
}