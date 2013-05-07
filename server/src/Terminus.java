import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import eventserver.EventServer;
import eventserver.ITerminusMsgCallback;
import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;
import shared.ServerCloseException;

import java.text.SimpleDateFormat;


public class Terminus implements ITerminusMsgCallback
{
	private EventServer tserver;
	TerminusDashboard 	dashboard;
	TerminusWebServer 	webserver;
	TerminusDatabase  	database;

	private HashMap<String, Boolean> recordVideo;
	private HashMap<String, TerminusMediaWriter> media;
		
	public Terminus(String[] args)
	{
		// init DB first
		try {
			database 	= new TerminusDatabase();
			
		}
		catch (ClassNotFoundException e) {
			System.out.println(e.getMessage());
			System.exit(1);
			return;
		}	
		
		tserver 	= new EventServer(this);
        dashboard 	= new TerminusDashboard();
        // use high port since root needed for lower range
        webserver 	= new TerminusWebServer(6900);
                
        recordVideo = new HashMap<String, Boolean>();
        media		= new HashMap<String, TerminusMediaWriter>();
        
		/* Start the server(s) */
		
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

        
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {

            public void run() {
                // application is stopping
            	System.out.println("closing DB");
            	database.closeDB();
            	
            	System.out.println("closing open media files");
            	for (String id : media.keySet()) {
            		closeVideo (id);
            	}
            }
        }));
		
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
				updateVideo(im);
				return;
			
			case TerminusMessage.MSG_EVENT:
				eventDetected((EventMessage)msg);
				dashboard.addMessage(msg);
				
			default:
				dashboard.addMessage(msg);
				
		}

	}
	
	private void eventDetected(EventMessage event)
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        String timestamp = dateFormat.format(event.getTimestamp());
        long epoch = event.getTimestamp().getTime();
        String priority = String.valueOf(event.getTotalPriority());
        String id = event.getID();
        String type = "";
        
        switch (event.getEventMsgType())
        {
        case EventMessage.EVENT_START:
        	// set recording = true, on next image it will be created
        	type = "Event Start";
        	recordVideo.put(event.getID(), true);
        	break;
        
        case EventMessage.EVENT_UPDATE:
        	// update existing video
        	type = "Event Update";
        	// maybe we never saw the start event...
        	recordVideo.put(event.getID(), true);
        	break;
        	
        case EventMessage.EVENT_END:
        	// close existing video, push into DB
        	type = "Event End";
        	if(recordVideo.containsKey(event.getID())) {
        		recordVideo.remove(event.getID());
        		closeVideo(event.getID());
        	}
        	
        	break;
        }
        
        database.addEventRow(epoch, priority, id, type);
        
	}
	
	private void updateVideo (ImageMessage msg)
	{
		if(!recordVideo.containsKey(msg.getID())) {
			return;
		}

		if(!media.containsKey(msg.getID())) {
			String filename = (System.currentTimeMillis()/1000) + "-" + msg.getID();
			media.put(msg.getID(), new TerminusMediaWriter(filename, msg.getImage()));
		} else {
			if(media.get(msg.getID()) != null) {
				media.get(msg.getID()).updateMedia(msg.getImage());
			}
		}
		
	}
	
	private void closeVideo (String id) {
		if(media.containsKey(id)) {
			if(media.get(id) != null) {
				media.get(id).writeMedia();
		        database.addEventRow(media.get(id).mediaEpoch(), "0", id, "Video", "", media.get(id).mediaPath());
		        media.remove(id);

			}
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