package edu.buffalo.cse.terminus.client.network;

import edu.buffalo.cse.terminus.client.network.ATerminusClient;
import edu.buffalo.cse.terminus.client.network.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.client.network.lowlevel.LowLevelClient;
import edu.buffalo.cse.terminus.client.network.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.TestMessage;

public class TerminusConnection
{
	private ATerminusClient terminusClient;
	private ITerminusMessageFactory messageFactory = new LowLevelMessageFactory();
	
	//TODO: Set user id
	private String uid = "1";
	
	public TerminusConnection(INetworkCallbacks c)
	{
		terminusClient = new LowLevelClient();
		terminusClient.setCallback(c);
	}
	
	public void connect(final String host, final int port)
	{
		terminusClient.connect(host, port);
	}
	
	public void sendTestMessage(String message)
	{
		TestMessage tm = messageFactory.getTestMessage(this.uid);
		tm.message = message;
		terminusClient.sendMessage(tm);
	}
	
	public void disconnect()
	{
		terminusClient.disconnect();
	}

}
