package edu.buffalo.cse.terminus.client.network;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.os.Build;
import edu.buffalo.cse.terminus.client.network.ATerminusClient;
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
		Disconnecting,
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
	
	public TerminusConnection(INetworkCallbacks c) 
	{
		terminusClient = new LowLevelClient();
		terminusClient.setCallback(this);
		this.callbacks = c;
		curConnectionState = ConnectionState.Disconnected;
		curRegistrationState = RegistrationState.Unregistered;
		
		setUserID();
	}
	
	private void setUserID()
	{
		//This didn't work on tablets
		//TelephonyManager mgr = (TelephonyManager)a.getSystemService(Context.TELEPHONY_SERVICE);
		//this.uid = mgr.getDeviceId();
		
		/*
		 * Try hardware serial number 
		 * If that doesn't work, try ID
		 * If that still doesn't work, throw in a bogus id that 
		 * we'll catch as we're debugging. 
		 */
		if (Build.SERIAL != null && !Build.SERIAL.isEmpty())
		{
			this.uid = Build.SERIAL;
		}
		else if (Build.ID != null && !Build.ID.isEmpty())
		{
			this.uid = Build.ID;
		}
		else
		{
			this.uid = "NO ID!";
		}
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
		if (curConnectionState == ConnectionState.Connected && 
				curRegistrationState == RegistrationState.Registered)
		{
			TestMessage tm = messageFactory.getTestMessage(this.uid);
			tm.message = message;
			terminusClient.sendMessage(tm);
		}
		else if (curConnectionState == ConnectionState.Disconnected || 
				curRegistrationState == RegistrationState.Unregistered)
		{
			
			this.reconnect();
		}
	}
	
	public TerminusMessage getEventMessage()
	{
		return messageFactory.getEventMessage(this.uid);
	}
	
	public void sendMessage(TerminusMessage m)
	{
		terminusClient.sendMessage(m);
	}
	
	public void reconnect()
	{
		if (this.eventIPAddress == null || this.eventIPAddress.isEmpty() || 
				this.eventPort == 0)
		{
			// We never connected in the first place
			return;
		}
		
		this.terminusClient.disconnect();
		this.curConnectionState = ConnectionState.Disconnected;
		this.connect(this.eventIPAddress, this.eventPort);
	}
	
	/*
	 * Disconnect from the Event Server
	 */
	public void disconnect()
	{
		if (this.curConnectionState == ConnectionState.Connected)
		{
			if (this.curRegistrationState != RegistrationState.Unregistered)
			{
				UnregisterMessage urm = this.messageFactory.getUnregisterMessage(this.uid);
				this.curRegistrationState = RegistrationState.Unregistered;
				this.curConnectionState = ConnectionState.Disconnecting;
				terminusClient.sendMessage(urm);
			}
			else
			{
				this.curConnectionState = ConnectionState.Disconnected;
				terminusClient.disconnect();
			}
		}
	}
	
	public String getConnectionID()
	{
		return this.uid;
	}
	
	@Override
	public void onConnectionComplete() 
	{
		curConnectionState = ConnectionState.Connected;
		
		if (this.curRegistrationState != RegistrationState.Registered)
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
		if (callbacks != null)
			callbacks.onConnectionDropped();
		
		/*
		 * It's possible this happened because we are in the process of disconnecting.
		 * We only want to reconnect if that's what our current status is
		 */
		if (this.curConnectionState == ConnectionState.Connected)
		{
			this.curConnectionState = ConnectionState.Disconnected;
			reconnect();
		}
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
		
		if (this.curConnectionState == ConnectionState.Disconnecting)
		{
			this.curConnectionState = ConnectionState.Disconnected;
			terminusClient.disconnect();
		}
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
				reconnect();
			}
		}
	}
	
	private void unregisterReceived(UnregisterMessage msg)
	{
		this.curRegistrationState = RegistrationState.Unregistered;
		reconnect();
	}
}
