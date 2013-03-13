package edu.buffalo.cse.terminus.client;

import android.app.Activity;


import edu.buffalo.cse.terminus.client.network.ConnectionResult;
import edu.buffalo.cse.terminus.client.network.INetworkCallbacks;
import edu.buffalo.cse.terminus.messages.TerminusMessage;

public class UIEventBridge implements INetworkCallbacks
{
	/*
	 *  The whole purpose of this class is to cross over threads onto the UI thread.
	 *  
	 *  You could just wrap each of the callbacks in the Activity in a runOnUOThread call, but
	 *  this looks a little cleaner and more obvious.
	 */
	
	final INetworkCallbacks caller;
	final Activity activity;
	
	public UIEventBridge(INetworkCallbacks caller, Activity activity)
	{
		this.caller = caller;
		this.activity = activity;
	}
	
	@Override
	public void connectionFinished(final ConnectionResult result)
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.connectionFinished(result); 
			}
		});
	}

	@Override
	public void messageFinished(final ConnectionResult result)
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.messageFinished(result); 
			}
		});
	}

	@Override
	public void disconnectFinished(final ConnectionResult result)
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.disconnectFinished(result); 
			}
		});
	}

	@Override
	public void messageReceived(final TerminusMessage msg)
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.messageReceived(msg); 
			}
		});
	}

}
