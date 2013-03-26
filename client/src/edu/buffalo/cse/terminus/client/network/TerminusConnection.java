package edu.buffalo.cse.terminus.client.network;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import edu.buffalo.cse.terminus.client.network.ATerminusClient;
import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.buffalo.cse.terminus.messages.UnregisterMessage;
import edu.buffalo.cse.terminus.client.network.lowlevel.LowLevelClient;
import edu.buffalo.cse.terminus.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.TestMessage;

public class TerminusConnection implements INetworkCallbacks
{
	private ATerminusClient terminusClient;
	private ITerminusMessageFactory messageFactory = new LowLevelMessageFactory();
	
	private enum ConnectionState
	{
		Disconnected,
		Connecting,
		Connected
	}
	
	private enum RegistrationState
	{
		Unregistered,
		Pending,
		Registered
	}
	
	private String uid;
	private INetworkCallbacks callbacks;
	private ConnectionState curConnectionState;
	private RegistrationState curRegistrationState;
	private String eventIPAddress;
	private int eventPort;
	
	public TerminusConnection(INetworkCallbacks c, Activity a) 
	{
		terminusClient = new LowLevelClient();
		terminusClient.setCallback(this);
		this.callbacks = c;
		curConnectionState = ConnectionState.Disconnected;
		curRegistrationState = RegistrationState.Unregistered;
		
		setUserID(a);
	}
	
	private void setUserID(Activity a)
	{
		TelephonyManager mgr = (TelephonyManager)a.getSystemService(Context.TELEPHONY_SERVICE);
		this.uid = mgr.getDeviceId();
	}
	
	public void connect(String host, int port)
	{
		this.eventIPAddress = host;
		this.eventPort = port;
		
		if (curConnectionState != ConnectionState.Disconnected)
			return;
		
		curConnectionState = ConnectionState.Connecting;
		terminusClient.connect(host, port);
	}
	
	public void sendTestMessage(String message)
	{
		if (curConnectionState == ConnectionState.Connected && curRegistrationState == RegistrationState.Registered)
		{
			TestMessage tm = messageFactory.getTestMessage(this.uid);
			tm.message = message;
			terminusClient.sendMessage(tm);	
		}
		else
		{
			//TODO: Queue items, connect
		}
	}
	
	public void sendEventMessage()
	{
		for (int i = 0; i < 100; i ++)
		{
			EventMessage em = this.messageFactory.getEventMessage(this.uid);
			terminusClient.sendMessage(em);
			try 
			{
				Thread.sleep(1);
			} 
			catch (InterruptedException e) 
			{
				
			}
		}
	}
	
	/*
	 * Disconnect from the Event Server
	 */
	public void disconnect()
	{
		if (this.curRegistrationState == RegistrationState.Registered && 
				this.curConnectionState == ConnectionState.Connected)
		{
			//TODO: Will the send thread run before the disconnect thread?
			//		We may want to queue the messages and disconnect after flushing the queue.
			UnregisterMessage urm = this.messageFactory.getUnregisterMessage(this.uid);
			terminusClient.sendMessage(urm);
			terminusClient.disconnect();
		}
		
		this.curRegistrationState = RegistrationState.Unregistered;
		this.curConnectionState = ConnectionState.Disconnected;
	}
	
	public String getConnectionID()
	{
		return this.uid;
	}
	
	@Override
	public void onConnectionComplete() 
	{
		startRegistrationProtocol();
		
		if (callbacks != null)
			callbacks.onConnectionComplete();
	}

	@Override
	public void onConnectionError(IOException e) 
	{
		if (callbacks != null)
			callbacks.onConnectionError(e);
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) 
	{
		if (callbacks != null)
			callbacks.onConnectionError(e);	
	}

	@Override
	public void onConnectionError(UnknownHostException e) 
	{
		if (callbacks != null)
			callbacks.onConnectionError(e);
	}

	@Override
	public void onDisconnectComplete() 
	{
		if (callbacks != null)
			callbacks.onDisconnectComplete();	
	}

	@Override
	public void onConnectionDropped() 
	{
		//TODO Reconnect!
		if (callbacks != null)
			callbacks.onConnectionDropped();
	}

	@Override
	public void onMessageReceived(TerminusMessage msg) 
	{
		if (msg == null)
			return;
		
		switch (msg.getMessageType())
		{
			case TerminusMessage.MSG_REG_RESPONSE:
				regResponseReceived((RegistrationResponse) msg);
				break;
				
			case TerminusMessage.MSG_UNREGISTER:
				unregisterReceived((UnregisterMessage) msg);
				break;
				
			default:
				break;
		}
		
		if (callbacks != null)
			callbacks.onMessageReceived(msg);
	}

	@Override
	public void onSendComplete() 
	{
		if (callbacks != null)
			callbacks.onSendComplete();
	}

	@Override
	public void onMessageFailed(TerminusMessage msg) 
	{
		//TODO Queue and try again!
		
		if (callbacks != null)
			callbacks.onMessageFailed(msg);
	}
	
	////////////////////////   REGISTRATION PROTOCOL   ////////////////////////
	
	private void startRegistrationProtocol()
	{
		curConnectionState = ConnectionState.Connected;
		curRegistrationState = RegistrationState.Pending;
		RegisterMessage rm = this.messageFactory.getRegisterMessage(this.uid);
		this.terminusClient.sendMessage(rm);
	}
	
	private void regResponseReceived(RegistrationResponse msg)
	{
		if (curRegistrationState == RegistrationState.Pending)
		{
			if (msg.getResult() == RegistrationResponse.REGISTRATION_SUCCESS)
			{
				curRegistrationState = RegistrationState.Registered;
			}
			else
			{
				curRegistrationState = RegistrationState.Unregistered;
				reconnectAndRegister();
			}
		}
	}
	
	private void unregisterReceived(UnregisterMessage msg)
	{
		//The indirection is in case we need to actually do
		//anything with the message in the future.
		reconnectAndRegister();
	}
	
	private void reconnectAndRegister()
	{
		/*
		 * The event server doesn't know us and so we need to 
		 * reconnect and start the registration protocol again
		 */
		
		this.terminusClient.disconnect();
		this.curConnectionState = ConnectionState.Disconnected;
		this.curRegistrationState = RegistrationState.Unregistered;
		this.connect(this.eventIPAddress, this.eventPort);
	}
}
