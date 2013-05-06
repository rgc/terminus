package edu.cse.buffalo.terminus.clientlib;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.buffalo.cse.terminus.messages.UnregisterMessage;
import edu.buffalo.cse.terminus.lowlevel.LowLevelClient;
import edu.buffalo.cse.terminus.lowlevel.LowLevelImageClient;
import edu.buffalo.cse.terminus.lowlevel.LowLevelImageMessage;
import edu.buffalo.cse.terminus.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.TestMessage;
import edu.cse.buffalo.terminus.clientlib.ATerminusClient;

public class TerminusConnection implements INetworkCallbacks
{
	private ATerminusClient terminusClient;
	private LowLevelImageClient imageClient;
	private ITerminusMessageFactory messageFactory = new LowLevelMessageFactory();
	
	private enum ConnectionState
	{
		Disconnected,
		Disconnecting,
		Reconnecting,
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
	
	/*
	 * Fields for registration
	 */
	private boolean isConsumer = false;
	private String regLocation = "";
	private String regNickname = "";
	
	/*
	 * This indicates we clicked the disconnect button vs. losing
	 * the network connection 
	 */
	private boolean hardDisconnect = false;
	
	public static final int IMAGE_PORT = 34412;
	
	public TerminusConnection(INetworkCallbacks c, Context context) 
	{
		terminusClient = new LowLevelClient();
		
		terminusClient.setCallback(this);
		this.callbacks = c;
		curConnectionState = ConnectionState.Disconnected;
		curRegistrationState = RegistrationState.Unregistered;
		setUserID(context);
	}
	
	public void setConsumer(boolean isConsumerApp)
	{
		this.isConsumer = isConsumerApp;
	}
	
	public void setNickname(String nickname)
	{
		regNickname = nickname;
	}
	
	public void setLocation(String location)
	{
		regLocation = location;
	}
	
	private void setUserID(Context c)
	{
		//This didn't work on tablets
		//TelephonyManager mgr = (TelephonyManager)a.getSystemService(Context.TELEPHONY_SERVICE);
		//this.uid = mgr.getDeviceId();
		
		/*
		 * First, try android id
		 * Next, try hardware serial number 
		 * If that doesn't work, try ID
		 * If that still doesn't work, throw in a bogus id that 
		 * we'll catch as we're debugging. 
		 */
		
		String androidID = Secure.getString(c.getContentResolver(),Secure.ANDROID_ID);
		
		if (androidID != null)
			this.uid = androidID; 
		else if (Build.SERIAL != null && !Build.SERIAL.isEmpty() && Build.SERIAL == Build.UNKNOWN)
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
		hardDisconnect = false;
		
		if (host == null || host.isEmpty() || port == 0)
		{
			return;
		}
		
		this.eventIPAddress = host;
		this.eventPort = port;
		
		imageClient = new LowLevelImageClient(host, IMAGE_PORT);
		
		if (curConnectionState != ConnectionState.Disconnected)
			return;
		
		curConnectionState = ConnectionState.Connecting;
		terminusClient.connect(host, port);
	}
	
	public boolean isConnected()
	{
		return (this.curConnectionState == ConnectionState.Connected);
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
	
	public ITerminusMessageFactory getMessageFactory()
	{
		return messageFactory;
	}
	
	public void sendMessage(TerminusMessage m)
	{
		if (curConnectionState == ConnectionState.Connected && 
				curRegistrationState == RegistrationState.Registered)
		{
			terminusClient.sendMessage(m);
		}
		else if (curConnectionState == ConnectionState.Disconnected)
		{
			if (!hardDisconnect)
				this.reconnect();
		}
	}
	
	public void sendImage(LowLevelImageMessage m)
	{
		if (imageClient != null)
		{
			if (curConnectionState == ConnectionState.Connected && 
					curRegistrationState == RegistrationState.Registered)
			{
				imageClient.sendMessage(m);
			}
		}
	}
	
	public void reconnect()
	{
		hardDisconnect = false;
		
		if (this.eventIPAddress == null || this.eventIPAddress.isEmpty() || 
				this.eventPort == 0)
		{
			// We never connected in the first place
			return;
		}
		
		this.curConnectionState = ConnectionState.Reconnecting;
		this.terminusClient.disconnect();
	}
	
	/*
	 * Disconnect from the Event Server
	 */
	public void disconnect()
	{
		hardDisconnect = true;
		
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
			
			if (imageClient != null)
				imageClient.disconnect();
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
		this.curConnectionState = ConnectionState.Disconnected;
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) 
	{
		if (callbacks != null)
			callbacks.onConnectionError(e);	
		this.curConnectionState = ConnectionState.Disconnected;
	}

	@Override
	public void onConnectionError(UnknownHostException e) 
	{
		if (callbacks != null)
			callbacks.onConnectionError(e);
		this.curConnectionState = ConnectionState.Disconnected;
	}

	@Override
	public void onDisconnectComplete() 
	{
		if (this.curConnectionState == ConnectionState.Reconnecting)
		{
			this.curConnectionState = ConnectionState.Disconnected;
			this.connect(this.eventIPAddress, this.eventPort);
		}
		else
		{
			this.curConnectionState = ConnectionState.Disconnected;
			if (callbacks != null)
				callbacks.onDisconnectComplete();
		}
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
		if (this.curConnectionState != ConnectionState.Disconnected)
		{
			this.curConnectionState = ConnectionState.Disconnected;
			
			if (!hardDisconnect)
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
		
		if (this.isConsumer)
			rm.setRegistrationType(RegisterMessage.REG_TYPE_CONSUMER);
		else
			rm.setRegistrationType(RegisterMessage.REG_TYPE_EVENT);
		
		rm.setLocation(regLocation);
		rm.setNickname(regNickname);
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
