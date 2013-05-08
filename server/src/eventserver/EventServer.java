package eventserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;

import lowlevelserver.LowLevelServer;
import akkaserver.AkkaServer;
import shared.ATerminusConnection;
import shared.ITerminusServer;
import shared.ServerCloseException;
import edu.buffalo.cse.terminus.lowlevel.LowLevelMessageFactory;
import edu.buffalo.cse.terminus.messages.AlertMessage;
import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
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
		
	/* Implementation specific communication */
	private ITerminusServer eventCom;
	
	/* Implementation specific message generator */
	private ITerminusMessageFactory messageFactory;
	
	/* List of parties interested in events */
	private final ITerminusMsgCallback messageCallback;
	
	/* 
	 * Active connections.
	 * 
	 * This may be useful for broadcasting messages.
	 */
	private final ConcurrentHashMap<String, ATerminusConnection> sessions;
	private final ConcurrentHashMap<String, ATerminusConnection> consumerSessions;
	
	private String eventServerIP;
	private ImageServer imageServer;
	
	public EventServer(ITerminusMsgCallback msgCallback)
	{
		this.messageCallback = msgCallback;
		sessions = new ConcurrentHashMap<String, ATerminusConnection>();
		consumerSessions = new ConcurrentHashMap<String, ATerminusConnection>();
		eventCom = new LowLevelServer(this, EVENT_PORT);
		imageServer = new ImageServer(this);
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
			imageServer.start();
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
		//runMessageProcThread();
	}

	@Override
	public void stop()
	{
		eventCom.stop();
		imageServer.stop();
		
		for (ATerminusConnection connection : sessions.values())
		{
			connection.shutdown();
		}

		sessions.clear();
	}

	private boolean isRegistered(String id)
	{
		if (id == null || id.isEmpty())
			return false;
		else
			return this.sessions.containsKey(id);
	}
	
	private void processMessage(TerminusMessage message, ATerminusConnection connection)
	{
		if (message == null)
		{
			return;
		}
		else if (connection == null)
		{
			if (message.getMessageType() == TerminusMessage.MSG_IMAGE)
			{
				processImage(message);
			}
			return;
		}
		else if (!isRegistered(message.getID()) && message.getMessageType() != TerminusMessage.MSG_REGISTER)
		{
			/*
			 * No session established for this id.
			 * We'll send an Unregister message and won't take this message any further 
			 */
			UnregisterMessage urm = messageFactory.getUnregisterMessage(message.getID());
			connection.sendMessage(urm);
			return;
		}
		
		
		switch (message.getMessageType())
		{
			case TerminusMessage.MSG_TEST:
				if (connection != null)
				{
					connection.sendMessage(message);
				}
				break;
				
			case TerminusMessage.MSG_REGISTER:
				registrationRequest((RegisterMessage) message, connection);
				break;
			
			case TerminusMessage.MSG_UNREGISTER:
				unregister(message, connection);
				break;
				
			case TerminusMessage.MSG_EVENT:
				processEvent(message, connection);
				break;
				
			default:
				break;
		}
		
		if (messageCallback != null)
		{
			messageCallback.messageReceived(connection, message);
		}
	}
	
	private void registrationRequest(RegisterMessage message, ATerminusConnection connection)
	{
		/*
		 * Right now we simply accept the registration and
		 * register the session.
		 * 
		 * In the future, this is where we could talk to an
		 * authentication server and run an authentication protocol.
		 */
		
		String id = message.getID();
		
		if (sessions.contains(id))
			sessions.remove(id);
		
		sessions.put(id, connection);
		
		connection.setID(id);
		connection.setLocation(message.getLocation());
		connection.setNickname(message.getNickname());
		
		if (message.getRegistrationType() == RegisterMessage.REG_TYPE_CONSUMER)
			consumerSessions.put(id, connection);
		
		RegistrationResponse response = messageFactory.getRegistrationResponse(id);
		response.setResult(RegistrationResponse.REGISTRATION_SUCCESS);
		connection.sendMessage(response);
	}
	
	private void processEvent(TerminusMessage message, ATerminusConnection connection)
	{
		EventMessage em = (EventMessage) message;
		if (em.getEventMsgType() == EventMessage.EVENT_START)
		{
			alertConsumers(connection, "http://www.cse.buffalo.edu");
		}
	}
	
	private void alertConsumers(ATerminusConnection connection, String url)
	{
		//For now, we'll send the connection id of the node that triggered the event.
		//this might come in handy later if we need to add messages and do some kind
		//of look up.
		AlertMessage message = messageFactory.getAlertMessage(connection.getID());
		message.setLocation(connection.getLocation());
		message.setNickname(connection.getNickname());
		message.setURL(url);
		
		for (ATerminusConnection c : consumerSessions.values())
		{
			c.sendMessage(message);
		}
	}
	
	/*
	 * Images are a little different.  There is no connection for these messages
	 * because the socket they were created on is closed right away.  So, we'll
	 * look up the connection before sending it on to the UI
	 */
	private void processImage(TerminusMessage tm)
	{
		if (isRegistered(tm.getID()))
		{
			ATerminusConnection connection = sessions.get(tm.getID());
			this.messageCallback.messageReceived(connection, tm);
		}
	}
	
	private void unregister(TerminusMessage message, ATerminusConnection connection)
	{
		String id = message.getID();
		if (isRegistered(id))
		{
			sessions.remove(id);
			connection.shutdown();
		}
		
		if (consumerSessions.containsKey(id))
			consumerSessions.remove(id);
	}
	
	// /////////////////////////// NETWORK EVENTS /////////////////////////////

	
	@Override
	public synchronized void messageReceived(ATerminusConnection connection, TerminusMessage msg)
	{
		if (msg != null)
		{
			processMessage(msg, connection);
		}
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
