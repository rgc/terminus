package edu.buffalo.cse.terminus.client.network;

import edu.buffalo.cse.terminus.messages.TerminusMessage;

public interface INetworkCallbacks
{
	/*
	 * Called when the connection attempt comletes
	 */
	public void connectionFinished(ConnectionResult result);
	
	/*
	 * Called when a the a message send attempt completes
	 */
	public void messageFinished(ConnectionResult result);
	
	/*
	 * Called when the disconnect attempt completes
	 */
	public void disconnectFinished(ConnectionResult result);
	
	/*
	 * Called when a message has been received from the connection
	 */
	public void messageReceived(TerminusMessage msg);
	
}
