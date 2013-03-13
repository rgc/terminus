package eventserver;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentHashMap;

import lowlevelserver.LowLevelServer;
import shared.ATerminusConnection;
import shared.ITerminusServer;
import shared.ServerCloseException;
import edu.buffalo.cse.terminus.messages.TerminusMessage;

/*
 * This class handles handles the message processing portion of event handling.
 * Also, this class acts as a bridge to the implmentation defined communications portion.
 */
public class EventServer implements IEventCallbacks, ITerminusServer
{
	public static final int EVENT_PORT = 34410;

	private ITerminusServer eventCom;

	private final ArrayList<IEventCallbacks> eventClients = new ArrayList<IEventCallbacks>();
	private final ConcurrentLinkedQueue<TerminusMessage> eventQueue = new ConcurrentLinkedQueue<TerminusMessage>();
	private final ConcurrentHashMap<String, ATerminusConnection> sessions = new ConcurrentHashMap<String, ATerminusConnection>();

	public EventServer()
	{
		eventCom = new LowLevelServer(this);
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
		eventCom.start();
		processMessages();
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

	private void processMessages()
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
						/*
						 * TODO: Do something meaningful here.
						 *  	 This is just to test the architecture.
						 */
						TerminusMessage m = eventQueue.remove();
						ATerminusConnection c = sessions.get("1");
						if (c != null)
							c.sendMessage(m);

						for (IEventCallbacks t : EventServer.this.eventClients)
						{
							t.messageReceived(m);
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

	// /////////////////////////// NETWORK EVENTS /////////////////////////////

	/*
	 * We'll do anything we need to with the events and then broadcast them to
	 * any other registered clients.
	 */
	@Override
	public synchronized void connectionAdded(ATerminusConnection connection)
	{
		if (sessions.containsKey("1"))
			sessions.remove("1");

		// sessions.put(connection.getID(), connection);
		sessions.put("1", connection);

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
	public synchronized void messageReceived(TerminusMessage msg)
	{
		/*
		 * Just queue the message, it'll get processed by the processing thread.
		 */
		eventQueue.add(msg);
	}
}
