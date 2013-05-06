package edu.buffalo.cse.terminus.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import android.app.Activity;

import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.cse.buffalo.edu.terminus.clientlib.INetworkCallbacks;

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
	public void onConnectionComplete() 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onConnectionComplete(); 
			}
		});
	}

	@Override
	public void onConnectionError(final IOException e) 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onConnectionError(e); 
			}
		});
	}

	@Override
	public void onConnectionError(final SocketTimeoutException e) 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onConnectionError(e); 
			}
		});	
	}

	@Override
	public void onConnectionError(final UnknownHostException e) 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onConnectionError(e); 
			}
		});	
	}

	@Override
	public void onDisconnectComplete() 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onDisconnectComplete(); 
			}
		});	
	}

	@Override
	public void onConnectionDropped() 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onConnectionDropped(); 
			}
		});	
	}

	@Override
	public void onMessageReceived(final TerminusMessage msg) 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onMessageReceived(msg); 
			}
		});	
	}

	@Override
	public void onSendComplete() 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onSendComplete(); 
			}
		});	
	}

	@Override
	public void onMessageFailed(final TerminusMessage msg) 
	{
		this.activity.runOnUiThread(new Runnable() {
			@Override
			public void run() { 
				caller.onMessageFailed(msg); 
			}
		});	
	}
}
