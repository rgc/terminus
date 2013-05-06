package edu.cse.buffalo.edu.terminus.clientlib;


import edu.buffalo.cse.terminus.messages.TerminusMessage;

public abstract class ATerminusClient
{
	protected INetworkCallbacks callback;
	
	public ATerminusClient()
	{
		callback = null;
	}
	
	public void setCallback(INetworkCallbacks c)
	{
		this.callback = c;
	}
	
	public abstract void connect(String host, int port);
	public abstract void disconnect();
	public abstract void sendMessage(TerminusMessage m);
	
}
