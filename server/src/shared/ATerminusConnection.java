package shared;

import java.net.InetAddress;

import edu.buffalo.cse.terminus.messages.*;

/*
 * This class represents a connection to a terminus event sensing client (as opposed to a consumer).
 * 
 * A basic function of this class is to be able to send terminus messages to the associated client.
 * This is implementation specific (hence abstract).
 * 
 * We'll need to expand on this as we go...
 */
public abstract class ATerminusConnection
{
	protected String uid;
	protected InetAddress address;
	protected int port;
	
	/*
	 * Abstract
	 */
	public abstract void sendMessage(TerminusMessage m);
	public abstract void shutdown();
	
	/*
	 * Implemented
	 */
	public ATerminusConnection()
	{
		
	}
	
	public String getID()
	{
		return this.uid;
	}
	
	public void setID(String id)
	{
		this.uid = id;
	}
	
	public InetAddress getAddress()
	{
		return this.address;
	}
	
	public void setAddress(InetAddress a)
	{
		this.address = a;
	}
	
	public int getPort()
	{
		return this.port;
	}
	
	public void setPort(int p)
	{
		this.port = p;
	}
}
