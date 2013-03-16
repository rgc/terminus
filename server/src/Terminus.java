import java.text.SimpleDateFormat;

import eventserver.EventServer;
import eventserver.IEventCallbacks;
import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;
import shared.ServerCloseException;

public class Terminus implements IEventCallbacks
{
	private EventServer tserver;
	
	public Terminus(String[] args)
	{
		this.tserver = new EventServer();
		
		/* Start the server(s) */
		try 
		{
			tserver.registerForCallbacks(this);
			tserver.start();
			System.out.println("Welcome to the Terminus Server\n");
			System.out.println("Local IP Address: " + tserver.getEventServerIP());
			System.out.println("Listening for events on port " + String.valueOf(EventServer.EVENT_PORT) + "\n");
		}
		catch (ServerCloseException e) {
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}
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
	public void messageReceived(ATerminusConnection connection, TerminusMessage msg)
	{
		if (msg == null)
			return;
		
		switch (msg.getMessageType())
		{
		case TerminusMessage.MSG_REGISTER:
			System.out.println("Registration Message Received.  ID == " + msg.getID());
			break;
			
		case TerminusMessage.MSG_TEST:
			System.out.println("Data in: " + ((TestMessage)msg).message);
			break;
			
		case TerminusMessage.MSG_EVENT:
			EventMessage em = (EventMessage)msg;
			String timestamp = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss").format(em.getTimestamp());
			System.out.println("Event Received");
			System.out.println("    From: " + msg.getID());
			System.out.println("    Time: " + timestamp);
			System.out.println();
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