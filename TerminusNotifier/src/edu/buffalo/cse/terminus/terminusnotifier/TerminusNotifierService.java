package edu.buffalo.cse.terminus.terminusnotifier;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Date;

import edu.buffalo.cse.terminus.messages.TerminusMessage;
import edu.buffalo.cse.terminus.messages.AlertMessage;
import edu.cse.buffalo.terminus.clientlib.INetworkCallbacks;
import edu.cse.buffalo.terminus.clientlib.TerminusConnection;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/*
 * Note, we implement both the start and bind functionality.
 */
public class TerminusNotifierService extends Service implements INetworkCallbacks
{
	/*
	 * This is probably the easiest way to communicate the
	 * service status
	 */
	public static boolean isRunning = false;
	
	/*
	 * This is used to update the notification bar as opposed to
	 * creating a new notification for each event.  This can be
	 * anything.
	 */
	private static final int NOTIFICATION_ID = 4815;
	
	/*
	 * The binder binds this to the application that it's part of
	 */
	private final IBinder binder = new LocalBinder();
	
	/*
	 * Vibration pattern for when the user's phone is on vibrate.
	 * This corresponds to 0 wait time + 3 300ms pulses. 
	 */
	private static final long[] vibPattern = {0, 300, 300, 300, 300, 300};
	
	/*
	 * IP Address and port for now
	 */
	private Settings settings;
	
	private TerminusConnection connection;
	private Date lastEvent = null;
	
	public class LocalBinder extends Binder
	{
		TerminusNotifierService getService()
		{
			return TerminusNotifierService.this;
		}
	}
	
	////////////////////////////////  Interface to Binding  ////////////////////////////////
	
	public boolean isNetworkConnected()
	{
		return connection.isConnected();
	}
	
	public Date getLastEventTime()
	{
		return lastEvent;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) 
	{
		settings = new Settings();
        settings.retrieve(this);
        
        isRunning = true;
        connection = new TerminusConnection(this, this);
        connection.setConsumer(true);
        connection.connect(settings.ipAddress, settings.port);
        
        // Tell the user we started.
        Toast.makeText(this, "Terminus Notification Service has started", Toast.LENGTH_SHORT).show();
        
        //This means the service will keep on running, which is very important here
        return START_STICKY;
    }
	
	@Override
    public void onDestroy() 
	{
		connection.disconnect();
		
        // Tell the user we stopped.
        Toast.makeText(this, "Terminus Notification Service has stopped", Toast.LENGTH_SHORT).show();
        
        isRunning = false;
    }
	
	@Override
	public IBinder onBind(Intent arg0) 
	{
		return binder;
	}
	
	private void showNotification(String title, String message, String url)
	{	
		Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		NotificationCompat.Builder mBuilder =
	        new NotificationCompat.Builder(this)
	        .setSmallIcon(R.drawable.ic_launcher)
	        .setContentTitle(title)
	        .setContentText(message)
	        .setAutoCancel(true)
	        .setVibrate(vibPattern)
	        .setSound(notificationSound);
		
		Intent notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		mBuilder.setContentIntent(contentIntent);
		
		NotificationManager mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
	
	private void alertReceived(AlertMessage alert)
	{
		lastEvent = new Date();
		
		String title = "Event Detected!";
		
		String location;
		if (!alert.getLocation().isEmpty())
			location = alert.getLocation();
		else
			location = "Unknown";
		
		String device;
		if (!alert.getLocation().isEmpty())
			device = alert.getNickname();
		else
			device = "Device";
		
		String alertMessage = "Detected in " + location + " by " + device;
		this.showNotification(title, alertMessage, alert.getURL());
	}
	
	@Override
	public void onConnectionComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionError(IOException e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionError(UnknownHostException e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDisconnectComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnectionDropped() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageReceived(TerminusMessage msg) {
		
		if (msg.getMessageType() == TerminusMessage.MSG_ALERT)
		{
			alertReceived((AlertMessage) msg);
		}
		
	}

	@Override
	public void onSendComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onMessageFailed(TerminusMessage msg) {
		// TODO Auto-generated method stub
		
	}
}
