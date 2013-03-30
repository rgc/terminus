package edu.buffalo.cse.terminus.client;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
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
	
	public static final String IP_ADDRESS_PARAM = "ipAddress";
	public static final String PORT_PARAM = "portNumber";
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_connection_parameters);
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
		txtIPAddress.setText(settings.getString(IP_ADDRESS_PARAM, ""));
		txtPort.setText(settings.getString(PORT_PARAM, ""));
	}
	
	private void setUserPreferences()
	{
		SharedPreferences settings = getSharedPreferences(CONNECTION_PREF_FILE, 0);
		Editor e = settings.edit();
		e.putString(IP_ADDRESS_PARAM, txtIPAddress.getText().toString());
		e.putString(PORT_PARAM, txtPort.getText().toString());
		e.commit();
	}
	
	public void okClicked(View view)
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
		
		Intent intent = new Intent();
		intent.putExtra(IP_ADDRESS_PARAM, ip);
		intent.putExtra(PORT_PARAM, port);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void cancelClicked(View view)
	{
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}
}
