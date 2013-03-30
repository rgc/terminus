package edu.buffalo.cse.terminus.client.network;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public interface INetworkCallbacks
{
	/*
	 * Successful connection attempt
	 */
	public void onConnectionComplete();
	
	/*
	 * Connection errors
	 */
	public void onConnectionError(IOException e);
	public void onConnectionError(SocketTimeoutException e);
	public void onConnectionError(UnknownHostException e);
	
	/*
	 * Called when the disconnect attempt completes
	 */
	public void onDisconnectComplete();
	
	/*
	 * Called if the lower layer connection is terminated due to error 
	 */
	public void onConnectionDropped();
	
	/*
	 * Called when a message has been received from the connection
	 */
	public void onMessageReceived(TerminusMessage msg);
	
	/*
	 * Called when a the a message send attempt successfully completes
	 */
	public void onSendComplete();
	
	/*
	 * Called when an error occurs when transmitting a message or the
	 * message cannot otherwise be sent
	 */
	public void onMessageFailed(TerminusMessage msg);
}
