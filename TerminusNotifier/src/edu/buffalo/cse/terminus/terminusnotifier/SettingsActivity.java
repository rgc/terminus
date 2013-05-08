package edu.buffalo.cse.terminus.terminusnotifier;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends Activity 
{	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		Settings settings = new Settings();
		settings.retrieve(this);
		
		EditText et = (EditText) findViewById(R.id.ip_address);
		et.setText(settings.ipAddress);
		
		et = (EditText) findViewById(R.id.port);
		et.setText(String.valueOf(settings.port));
	}
	
	public void cancelClicked(View view)
	{
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}
	
	public void okClicked(View view)
	{
		Settings settings = new Settings();
		
		EditText et = (EditText) findViewById(R.id.port);
		String portText = et.getText().toString();
		
		int port = 0;
		
		try 
		{
			port = Integer.valueOf(portText);
			if (port < 1 || port > 60000)
			{
				Dialogs.getOKOnlyAlert("Port must be between 1 and 60000", this).show();
				return;
			}
		} 
		catch (NumberFormatException e) 
		{
			Dialogs.getOKOnlyAlert("Invalid Number Format", this).show();
			return;
		}
		
		settings.port = port;
		
		et = (EditText) findViewById(R.id.ip_address);
		settings.ipAddress = et.getText().toString();
		
		settings.commit(this);
		
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}
}
