package edu.buffalo.cse.terminus.terminusnotifier;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import edu.buffalo.cse.terminus.terminusnotifier.TerminusNotifierService.LocalBinder;

public class TerminusNotifierActivity extends Activity 
{
	
	private TextView tvService;
	private TextView tvNetwork;
	private TextView tvLastEvent;
	
	TerminusNotifierService notifierService;
    boolean isBound = false;
	
    private ServiceConnection myConnection = new ServiceConnection() 
    {
	    public void onServiceConnected(ComponentName className,
	            IBinder service) 
	    {
	        LocalBinder binder = (LocalBinder) service;
	        notifierService = binder.getService();
	        isBound = true;
	        
	        updateLabels();
	    }
	    
	    public void onServiceDisconnected(ComponentName arg0) 
	    {
	        isBound = false;
	        updateLabels();
	    }
    };
    
    private void bindTerminusService()
    {
    	Intent intent = new Intent(this, TerminusNotifierService.class);
    	bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }
    
    private void unbindTerminusService()
    {
    	if (isBound)
    	{
    		isBound = false;
    		unbindService(myConnection);
     	}
    }
    
    private void updateLabels()
    {
    	if (TerminusNotifierService.isRunning && notifierService != null)
    	{
    		tvService.setText("Running");
    		tvService.setTextColor(Color.GREEN);
    		
        	if (notifierService.isNetworkConnected())
        	{
        		tvNetwork.setText("Connected");
        		tvNetwork.setTextColor(Color.GREEN);
        	}
        	else
        	{
        		tvNetwork.setText("Disconnected");
        		tvNetwork.setTextColor(Color.RED);
        	}
        	
        	tvLastEvent.setText(notifierService.getLastEventTime());
    	}
    	else
    	{
    		tvService.setText("Stopped");
    		tvService.setTextColor(Color.RED);
        	tvNetwork.setText("Disconnected");
        	tvNetwork.setTextColor(Color.RED);
    	}
    	
    	tvLastEvent.setText("not available");
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_terminus_notifier);
		
		tvService = (TextView) findViewById(R.id.tvServiceStatus);
		tvNetwork = (TextView) findViewById(R.id.tvNetworkStatus);
		tvLastEvent = (TextView) findViewById(R.id.tvLastEvent);
		
		updateLabels();
		
		if (TerminusNotifierService.isRunning)
		{
			bindTerminusService();
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.terminus_notifier, menu);
		return true;
	}
	
	public void onDestroy()
	{
		super.onDestroy();
		
		//Unbind only, don't stop the service
		unbindTerminusService();
	}
	
	public void btnSettings_Click(View view)
	{
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
	
	public void btnStartService_Click(View view)
	{
		if (TerminusNotifierService.isRunning)
    		return;
    	
    	Intent intent = new Intent(this, TerminusNotifierService.class);
    	startService(intent);
    	bindTerminusService();
	}
	
	public void btnStopService_Click(View view)
	{
		if (TerminusNotifierService.isRunning)
    	{
    		unbindTerminusService();
    		Intent intent = new Intent(this, TerminusNotifierService.class);
        	stopService(intent);
        	updateLabels();
    	}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
	        case R.id.menu_refresh:
	            updateLabels();
	            return true;
	        
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}
