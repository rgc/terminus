package edu.buffalo.cse.terminus.client.network;

import android.app.Activity;
import android.content.Context;
import android.telephony.TelephonyManager;
import edu.buffalo.cse.terminus.client.network.ATerminusClient;
import edu.buffalo.cse.terminus.client.network.ConnectionResult.ConnectionStatus;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
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
	
	public void connect(final String host, final int port)
	{
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
	
	public void disconnect()
	{
		this.curRegistrationState = RegistrationState.Unregistered;
		this.curConnectionState = ConnectionState.Disconnected;
		terminusClient.disconnect();
	}
	
	public String getConnectionID()
	{
		return this.uid;
	}

	@Override
	public void connectionFinished(ConnectionResult result) 
	{
		if (result.status == ConnectionStatus.Success)
		{
			// Registration Protocol 
			curConnectionState = ConnectionState.Connected;
			curRegistrationState = RegistrationState.Pending;
			RegisterMessage rm = this.messageFactory.getRegisterMessage(this.uid);
			this.terminusClient.sendMessage(rm);
		}
		
		if (callbacks != null)
			callbacks.connectionFinished(result);
	}

	@Override
	public void messageFinished(ConnectionResult result) 
	{
		if (callbacks != null)
			callbacks.messageFinished(result);
	}

	@Override
	public void disconnectFinished(ConnectionResult result) 
	{
		if (callbacks != null)
			callbacks.disconnectFinished(result);
	}

	@Override
	public void messageReceived(TerminusMessage msg) 
	{
		if (msg.getMessageType() == TerminusMessage.MSG_REG_RESPONSE && 
				curRegistrationState == RegistrationState.Pending)
		{
			RegistrationResponse res = (RegistrationResponse) msg;
			if (res.getResult() == RegistrationResponse.REGISTRATION_SUCCESS)
			{
				curRegistrationState = RegistrationState.Registered;
			}
			else
			{
				curRegistrationState = RegistrationState.Unregistered;
				//TODO: Registration failed, what now?
			}
		}
		
		if (callbacks != null)
			callbacks.messageReceived(msg);
	}
}
