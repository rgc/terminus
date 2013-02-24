import java.io.*;
import java.net.*;

import server.ITerminusCallbacks;
import server.TerminusMessage;
import server.TerminusServer;

public class Terminus implements ITerminusCallbacks
{	
	public static final int INTERNET_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 34410;
	
	private TerminusServer tserver;
	
	public Terminus(String[] args)
	{
		/* 
		 * This kills the program if the port isn't valid
		 * 
		 * -1 => No port entered.
		 */
		int port = getPortFromArgs(args);
		
		if (port == -1)
			this.tserver = new TerminusServer();
		else
			this.tserver = new TerminusServer(port);
		
		System.out.println("Welcome to the Terminus Server\n");
		
		/* Get and display our listening IP address from the tserver */
		try
		{
			InetAddress localHost = tserver.getLocalHost();
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
		
		/* Get the port from the tserver */
		try 
		{
			tserver.registerForCallbacks(this);
			tserver.startServer();
			System.out.println("Listening for connections on port " + String.valueOf(tserver.getAcceptPort()) + "\n");
		} 
		catch (IOException e) {
			System.out.println(e.getMessage());
			return;
		}
	}
	
	private int getPortFromArgs(String[] args)
	{
		int port = 0;

		if (args.length > 1)
		{
			System.out.println("Invalid arguments\n   Usage: Terminus [port]");
			System.exit(1);
		}
		else if (args.length == 0)
		{
			port = -1;
		}
		else
		{
			try
			{
				port = Integer.parseInt(args[0]);
				if (port < 1 || port > 60000)
				{
					System.out.println("Port must be between 1 and 60000");
					System.exit(1);
				}
			}
			catch (NumberFormatException e)
			{
				System.out.println("Invalid Port Number");
				System.exit(1);
			}
		}
		
		return port;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Terminus(args);
	}

	@Override
	public void connectionAdded()
	{
		System.out.println("Connection Added");
	}

	@Override
	public void connectionDropped()
	{
		System.out.println("Connection Dropped");
	}

	@Override
	public void messageReceived(TerminusMessage msg)
	{
		System.out.println("Data in: " + msg.message);
	}

}