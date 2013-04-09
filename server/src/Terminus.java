import java.util.Date;

import eventserver.EventServer;
import eventserver.ITerminusMsgCallback;
import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;
import shared.ServerCloseException;

public class Terminus implements ITerminusMsgCallback
{
	private EventServer tserver;
	TerminusDashboard dashboard;
	
	public Terminus(String[] args)
	{
		this.tserver = new EventServer(this);
        dashboard = new TerminusDashboard();
			
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

		Date timestamp   = new Date();
		String type		 = "Unknown";
		
		switch (msg.getMessageType()) {
		
			case TerminusMessage.MSG_REGISTER:
				type = "Reg";
				break;

			case TerminusMessage.MSG_UNREGISTER:
				type = "Unreg";
				break;

			case TerminusMessage.MSG_TEST:
				type = "Test";

			break;

			case TerminusMessage.MSG_EVENT:
				EventMessage em  = (EventMessage)msg;

				switch (em.getEventType()) {
					case EventMessage.EVENT_ACCELEROMETER:
						type = "Accel";
						break;
					case EventMessage.EVENT_MAGNETOMETER:
						type = "Magno";
						break;
					case EventMessage.EVENT_LIGHT:
						type = "Light";
						break;
					case EventMessage.EVENT_CAMERA_MOTION:
						type = "Camera";
						if(em.getData() != null) {
							System.out.println(em.getData().length);
							dashboard.addUpdateImage(em.getID(), em.getData());
						} else {
							System.out.println("null image");
						}
						break;
					case EventMessage.EVENT_SOUND:
						type = "Sound";
						break;
						
				} // end switch
		} // end switch
		
		dashboard.addMessage(msg.getID(), type, timestamp);
		System.out.println(type + " Message Received from" + msg.getID());
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new Terminus(args);
	}
}