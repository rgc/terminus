import java.util.Date;

import eventserver.EventServer;
import eventserver.ITerminusMsgCallback;
import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;
import shared.ServerCloseException;

import java.text.SimpleDateFormat;



public class Terminus implements ITerminusMsgCallback
{
	private EventServer tserver;
	TerminusDashboard dashboard;
	TerminusWebServer webserver;
	
	public Terminus(String[] args)
	{
		this.tserver = new EventServer(this);
        dashboard = new TerminusDashboard();
        // use high port since root needed for lower range
        webserver = new TerminusWebServer(6900);
                
		/* Start the server(s) */
		try
		{
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
		
        try
		{
			webserver.start();
			System.out.println("Terminus Web Server started\n");
			
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}
		
	}
	
	@Override
	public void messageReceived(ATerminusConnection connection, TerminusMessage msg)
	{
		if (msg == null) {
			return;
		}
		
		switch (msg.getMessageType()) 
		{
			case TerminusMessage.MSG_IMAGE:
				ImageMessage im = (ImageMessage) msg;
				//System.out.println("Image Received, Size = " + String.valueOf(im.getImage().length));
				dashboard.addUpdateImage(im.getID(), im.getImage());
				return;
			
			default:
				dashboard.addMessage(msg);
				
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