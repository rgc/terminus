package server;

import java.net.*;
import java.util.ArrayList;
import java.io.*;

public class TerminusServer implements ITerminusCallbacks
{
	public static final int INTERNET_TIMEOUT = 10000;
	public static final int DEFAULT_PORT = 34410;
	
	private int acceptPort;
	private ServerSocket server = null;
	private ArrayList<ITerminusCallbacks> eventClients;
	private ITerminusCallbacks context = this;
	
	public TerminusServer()
	{
		this(DEFAULT_PORT);
	}
	
	public TerminusServer(int port)
	{
		this.acceptPort = port;
		eventClients = new ArrayList<ITerminusCallbacks>();
	}
	
	public void registerForCallbacks(ITerminusCallbacks t)
	{
		if (!eventClients.contains(t))
			eventClients.add(t);
	}
	
	public void unregisterForCallbacks(ITerminusCallbacks t)
	{
		if (eventClients.contains(t))
			eventClients.remove(t);
	}
	
	public void startServer() throws IOException
	{
		if (this.server != null && !this.server.isClosed())
		{
			stopServer();
		}
		
		server = new ServerSocket(this.acceptPort);
		
		/* Connection Accept Thread */
		new Thread(new Runnable()
		{
			public void run()
			{
				while (true)
				{
					Socket s = null;
					
					try 
					{
						s = server.accept();
						context.connectionAdded();
						new Thread(new TerminusConnection(s, context)).start();
					} 
					catch (IOException e) 
					{
						//TODO: This should propegate back to UI, or just be ignored 
						return;
					}
				}
			}
		}).start();
	}
	
	public void stopServer() throws IOException
	{
		server.close();
	}
	
	public InetAddress getLocalHost() throws IOException, UnknownHostException, SocketTimeoutException
	{
		Socket s = new Socket();
		SocketAddress address = new InetSocketAddress("www.google.com", 80);
		s.connect(address, INTERNET_TIMEOUT);
		
		InetAddress a = s.getLocalAddress();
		s.close();
		return a;
	}
	
	public int getAcceptPort()
	{
		return this.acceptPort;
	}
	
	//////////////////////////////////////   NETWORK EVENTS   //////////////////////////////////////
	
	/* 
	 * We'll do anything we need to with the events and then broadcast them
	 * to any other registered clients 
	 * 
	 * Note: I made these synchronized to act more like the event queue model.
	 * Of course, each messages will queue up in the OS buffer if this takes
	 * a long time.
	 * 
	 * TODO: Reconsider this; if the events take a long time (UI/logging) this
	 * 		 might be bad.
	 */
	@Override
	public synchronized void connectionAdded()
	{
		for (ITerminusCallbacks t : this.eventClients)
		{
			t.connectionAdded();
		}
	}

	@Override
	public synchronized void connectionDropped()
	{
		for (ITerminusCallbacks t : this.eventClients)
		{
			t.connectionDropped();
		}
	}

	@Override
	public synchronized void messageReceived(TerminusMessage msg)
	{
		for (ITerminusCallbacks t : this.eventClients)
		{
			t.messageReceived(msg);
		}
	}
}
