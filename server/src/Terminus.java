import java.io.*;
import java.net.*;

import eventserver.EventServer;
import eventserver.IEventCallbacks;
import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;
import shared.ServerCloseException;

public class Terminus implements IEventCallbacks
{	
	public static final int INTERNET_TIMEOUT = 10000;
	
	private EventServer tserver;
	
	public Terminus(String[] args)
	{
		this.tserver = new EventServer();
		
		System.out.println("Welcome to the Terminus Server\n");
		
		/* Get and display our listening IP address */
		try
		{
			InetAddress localHost = getLocalHost();
			System.out.println("Local IP Address: " + localHost.getHostAddress());
		}
		catch (SocketTimeoutException e)
		{
			System.out.println("Connection Timed Out, please check network connection\nShutting down\n");
			System.exit(1);
		}
		catch (IOException e)
		{
			System.out.println("Unable to get local host\nShutting down\n");
			System.exit(1);
		}
		
		/* Start the server(s) */
		try 
		{
			tserver.registerForCallbacks(this);
			tserver.start();
			System.out.println("Listening for connections on port " + String.valueOf(EventServer.EVENT_PORT) + "\n");
		}
		catch (ServerCloseException e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	private InetAddress getLocalHost() throws IOException, UnknownHostException, SocketTimeoutException
	{
		Socket s = new Socket();
		SocketAddress address = new InetSocketAddress("www.google.com", 80);
		s.connect(address, INTERNET_TIMEOUT);
		
		InetAddress a = s.getLocalAddress();
		s.close();
		return a;
	}

	@Override
	public void connectionAdded(ATerminusConnection c)
	{
		String id = (c.getID() == null) ? "unknown id" : c.getID();
		System.out.println("Connection Added:  " + id);
	}
	
	@Override
	public void connectionDropped(ATerminusConnection c)
	{
		String id = (c.getID() == null) ? "unknown id" : c.getID();
		System.out.println("Connection Dropped: " + id);
	}
	
	@Override
	public void messageReceived(TerminusMessage msg)
	{
		switch (msg.getMessageType())
		{
		case TerminusMessage.MSG_TEST:
			System.out.println("Data in: " + ((TestMessage)msg).message);
			break;
		default:
			break;
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Terminus(args);
	}
}