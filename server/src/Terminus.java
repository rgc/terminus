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
	
	public Terminus(String[] args)
	{
		this.tserver = new EventServer(this);
        dashboard = new TerminusDashboard();
        
        /*  DEBUG
         * 
        for (int i = 0; i<600; i++) {
        	dashboard.addMessage("lala" + i, "camera",new Date());
        	try {
        	    Thread.sleep(100);
        	} catch(InterruptedException ex) {
        	    Thread.currentThread().interrupt();
        	}
        }
        */
        
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
	}
	
	@Override
	public void messageReceived(ATerminusConnection connection, TerminusMessage msg)
	{
		if (msg == null) {
			return;
		}

		Date timestamp = new Date();
		String type = "Unknown";
		
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
		
		//System.out.println(type + " Message Received from" + msg.getID());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Terminus(args);
	}
}