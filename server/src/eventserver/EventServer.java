package eventserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

import lowlevelserver.LowLevelServer;
import shared.ATerminusConnection;
import shared.ITerminusServer;
import shared.ServerCloseException;
import edu.buffalo.cse.terminus.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;

/*
 * This class handles handles the message processing portion of event handling.
 * Also, this class acts as a bridge to the implementation defined communications portion.
 */
public class EventServer implements IEventCallbacks, ITerminusServer
{
	public static final int EVENT_PORT = 34411;
	public static final int INTERNET_TIMEOUT = 10000;
	
	class QueuedMessage
	{
		ATerminusConnection connection;
		TerminusMessage message;
		
		public QueuedMessage(ATerminusConnection c, TerminusMessage m)
		{
			this.connection = c;
			this.message = m;
		}
	}
	
	/* Implementation specific communication */
	private ITerminusServer eventCom;
	
	/* Implementation specific message generator */
	private ITerminusMessageFactory messageFactory;
	
	/* List of parties interested in events */
	private final ArrayList<IEventCallbacks> eventClients;
	
	/* 
	 *  Queued events
	 * 
	 *  We have a thread whose job is to process the queued events.
	 *  Right now everything goes into this one queue.  We could change this
	 *  to separate out the registration requests and other higher priority
	 *  messages.
	 */
	private final ConcurrentLinkedQueue<QueuedMessage> eventQueue;
	
	/* 
	 * Active connections.
	 * 
	 * This may be useful for brocasting messages.
	 */
	private final ConcurrentHashMap<String, ATerminusConnection> sessions;
	
	private String eventServerIP;
	
	public EventServer()
	{
		eventClients = new ArrayList<IEventCallbacks>();
		eventQueue = new ConcurrentLinkedQueue<QueuedMessage>();
		sessions = new ConcurrentHashMap<String, ATerminusConnection>();
		eventCom = new LowLevelServer(this, EVENT_PORT);
		messageFactory = new LowLevelMessageFactory();
	}

	public void registerForCallbacks(IEventCallbacks t)
	{
		if (!eventClients.contains(t))
		{
			eventClients.add(t);
		}
	}

	public void unregisterForCallbacks(IEventCallbacks t)
	{
		if (eventClients.contains(t))
		{
			eventClients.remove(t);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see shared.ITerminusServer#start()
	 * 
	 * Just pass these on to whichever communications server we're using.
	 */

	@Override
	public void start() throws ServerCloseException
	{
		try
		{
			eventServerIP = EventServer.getLocalHost().getHostAddress();
		}
		catch (SocketTimeoutException e)
		{
			throw new ServerCloseException(e.getMessage());
		}
		catch (UnknownHostException e)
		{
			throw new ServerCloseException(e.getMessage());
		}
		catch (IOException e)
		{
			throw new ServerCloseException(e.getMessage());
		}
		
		eventCom.start();
		runMessageProcThread();
	}

	@Override
	public void stop()
	{
		eventCom.stop();

		for (ATerminusConnection connection : sessions.values())
		{
			connection.shutdown();
		}

		sessions.clear();
		eventQueue.clear();
	}

	private void runMessageProcThread()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (!eventQueue.isEmpty())
					{
						QueuedMessage qm = eventQueue.remove();
						EventServer.this.processMessage(qm);
							
						for (IEventCallbacks t : EventServer.this.eventClients)
						{
							t.messageReceived(qm.connection, qm.message);
						}
					}

					try
					{
						Thread.sleep(5);
					}
					catch (InterruptedException e)
					{
						// Ignore
					}
				}
			}
		}).start();
	}

	private void processMessage(QueuedMessage msg)
	{
		if (msg == null)
			return;
		
		switch (msg.message.getMessageType())
		{
			case TerminusMessage.MSG_TEST:
				if (msg.connection != null)
				{
					msg.connection.sendMessage(msg.message);
				}
				break;
				
			case TerminusMessage.MSG_REGISTER:
				registrationRequest(msg);
				break;
			
			case TerminusMessage.MSG_EVENT:
				processEvent(msg);
				break;
				
			default:
				break;
		}
	}
	
	private void registrationRequest(QueuedMessage msg)
	{
		/*
		 * Right now we simply accept the registration and
		 * register the session.
		 * 
		 * In the future, this is where we could talk to an
		 * authentication server and run an authentication protocol.
		 */
		
		String id = msg.message.getID();
		
		if (sessions.contains(id))
		{
			sessions.remove(id);
		}
		
		sessions.put(id, msg.connection);
		RegistrationResponse response = messageFactory.getRegistrationResponse(id);
		response.setResult(RegistrationResponse.REGISTRATION_SUCCESS);
		msg.connection.sendMessage(response);
	}
	
	private void processEvent(QueuedMessage qm)
	{
		//TODO do something with the event!
		//		Alternatively, just register all handlers 
		//		for to receive callbacks
	}
	
	// /////////////////////////// NETWORK EVENTS /////////////////////////////

	/*
	 * We'll do anything we need to with the events and then broadcast them to
	 * any other registered clients.
	 */
	@Override
	public synchronized void connectionAdded(ATerminusConnection connection)
	{
		/*
		 * Pass the event on to whoever else needs to know
		 */
		for (IEventCallbacks t : this.eventClients)
		{
			t.connectionAdded(connection);
		}
	}

	@Override
	public synchronized void connectionDropped(ATerminusConnection connection)
	{
		for (IEventCallbacks t : this.eventClients)
		{
			t.connectionDropped(connection);
		}
	}

	@Override
	public synchronized void messageReceived(ATerminusConnection connection, TerminusMessage msg)
	{
		/*
		 * Just queue the message, it'll get processed by the processing thread.
		 */
		if (msg != null)
			eventQueue.add(new QueuedMessage(connection, msg));
	}
	
	public String getEventServerIP()
	{
		return this.eventServerIP;
	}
	
	private static InetAddress getLocalHost() throws IOException, UnknownHostException, SocketTimeoutException
	{
		Socket s = new Socket();
		SocketAddress address = new InetSocketAddress("www.google.com", 80);
		s.connect(address, INTERNET_TIMEOUT);
		
		InetAddress a = s.getLocalAddress();
		s.close();
		return a;
	}
	
}
