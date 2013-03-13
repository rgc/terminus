package edu.buffalo.cse.terminus.client;

import edu.buffalo.cse.terminus.client.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/*
 * Activity to get the IP Address/Host Name from the user and
 * pass the information to the message sending activity.
 * 
 * The parameters are stored in the preference file to avoid having
 * to enter them in each time.
 * 
 * The port number is validated before passing it.
 */
public class ConnectionParameters extends Activity
{
	private static final String CONNECTION_PREF_FILE = "ConnectionPrefs"; 
	private EditText txtIPAddress;
	private EditText txtPort;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.parameter_layout);
		setActivityControls();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		setUserPreferences();
	}
	
	private void setActivityControls()
	{
		txtIPAddress = (EditText) this.findViewById(R.id.ip_address);
		txtPort = (EditText) this.findViewById(R.id.port);
		getUserPreferences();
	}
	
	private void getUserPreferences()
	{
		SharedPreferences settings = getSharedPreferences(CONNECTION_PREF_FILE, 0);
		txtIPAddress.setText(settings.getString("ipAddress", ""));
		txtPort.setText(settings.getString("portNumber", ""));
	}
	
	private void setUserPreferences()
	{
		SharedPreferences settings = getSharedPreferences(CONNECTION_PREF_FILE, 0);
		Editor e = settings.edit();
		e.putString("ipAddress", txtIPAddress.getText().toString());
		e.putString("portNumber", txtPort.getText().toString());
		e.commit();
	}
	
	public void connectClicked(View view)
	{	
		String ip = txtIPAddress.getText().toString();
		String portString = txtPort.getText().toString();
		
		int port = 0;
		
		try {
			port = Integer.valueOf(portString);
			if (port < 1 || port > 60000)
			{
				Dialogs.getOKOnlyAlert("Port must be between 1 and 60000", this).show();
				txtPort.setText("");
				return;
			}
		} catch (NumberFormatException e) {
			txtPort.setText("");
			Dialogs.getOKOnlyAlert("Invalid Number Format", this).show();
			return;
		}
		
		Intent intent = new Intent(this, SendMessageActivity.class);
		intent.putExtra("ipAddress", ip);
		intent.putExtra("portNumber", port);
		startActivity(intent);
	}
}
