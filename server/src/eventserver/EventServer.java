package eventserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

import lowlevelserver.LowLevelServer;
import akkaserver.AkkaServer;
import shared.ATerminusConnection;
import shared.ITerminusServer;
import shared.ServerCloseException;
import edu.buffalo.cse.terminus.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.buffalo.cse.terminus.messages.UnregisterMessage;

/*
 * This class handles handles the message processing portion of event handling.
 * Also, this class acts as a bridge to the implementation defined communications portion.
 */
public class EventServer implements ITerminusMsgCallback, ITerminusServer
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
	private final ITerminusMsgCallback messageCallback;
	
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
	
	public EventServer(ITerminusMsgCallback msgCallback)
	{
		this.messageCallback = msgCallback;
		eventQueue = new ConcurrentLinkedQueue<QueuedMessage>();
		sessions = new ConcurrentHashMap<String, ATerminusConnection>();
		eventCom = new LowLevelServer(this, EVENT_PORT);
		//eventCom = new AkkaServer(this, EVENT_PORT);
		messageFactory = new LowLevelMessageFactory();
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
						
						if (!isRegistered(qm.message.getID()) && qm.message.getMessageType() != TerminusMessage.MSG_REGISTER)
						{
							/*
							 * No session established for this id.
							 * We'll send a Unregister message and won't take this message any further 
							 */
							UnregisterMessage urm = messageFactory.getUnregisterMessage(qm.message.getID());
							qm.connection.sendMessage(urm);
						}
						
						EventServer.this.processMessage(qm);
						
						if (EventServer.this.messageCallback != null)
						{
							EventServer.this.messageCallback.messageReceived(qm.connection, qm.message);
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

	private boolean isRegistered(String id)
	{
		if (id == null || id.isEmpty())
			return false;
		else
			return this.sessions.containsKey(id);
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
			
			case TerminusMessage.MSG_UNREGISTER:
				unregister(msg);
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
			sessions.remove(id);
		
		sessions.put(id, msg.connection);
		
		msg.connection.setID(id);
		
		RegistrationResponse response = messageFactory.getRegistrationResponse(id);
		response.setResult(RegistrationResponse.REGISTRATION_SUCCESS);
		msg.connection.sendMessage(response);
	}
	
	private void processEvent(QueuedMessage qm)
	{
		//TODO do something with the event!
		//		Alternatively, just register all handlers for 
		//		events to receive callbacks.
	}
	
	private void unregister(QueuedMessage qm)
	{
		String id = qm.message.getID();
		if (isRegistered(id))
		{
			sessions.remove(id);
			qm.connection.shutdown();
		}
	}
	
	// /////////////////////////// NETWORK EVENTS /////////////////////////////

	
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
