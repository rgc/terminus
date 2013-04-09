package edu.buffalo.cse.terminus.client;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.terminus.client.sensors.TerminusSensorManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

public class SettingsActivity extends Activity 
{
	private class SensorCheckBox extends CheckBox
	{
		int sensorType;
		
		public SensorCheckBox(Context context) 
		{
			super(context);
		}
	}
	
	private ArrayList<SensorCheckBox> sensorCheckBoxes;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_settings);
		
		TerminusSettings settings = new TerminusSettings();
		settings.retrieve(this);
		
		loadNetworkSettings(settings);
		loadCameraSettings(settings);
		loadSoundSettings(settings);
		loadSensorSettings(settings);
	}
	
	private void loadNetworkSettings(TerminusSettings settings)
	{
		EditText et = (EditText) findViewById(R.id.ip_address);
		et.setText(settings.ipAddress);
		
		et = (EditText) findViewById(R.id.port);
		et.setText(String.valueOf(settings.port));
		
	}
	
	private void loadCameraSettings(TerminusSettings settings)
	{
		int selected;
		
		switch (settings.cameraOption)
		{
		
		case TerminusSettings.CAMERA_NATIVE:
			selected = R.id.cameraRadioNative;
			break;
		
		case TerminusSettings.CAMERA_LOWLEVEL:
			selected = R.id.cameraRadioLowlevel;
			break;
		
		case TerminusSettings.CAMERA_JAVA:
			selected = R.id.cameraRadioJava;
			break;
			
		default:
			selected = R.id.cameraRadioNone;
			break;
		}
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.cameraRadioGroup);
		rg.check(selected);
	}
	
	private void loadSoundSettings(TerminusSettings settings)
	{
		if (settings.useSound)
		{
			CheckBox cb = (CheckBox) findViewById(R.id.cbSound);
			cb.setChecked(true);
		}
	}
	
	private void loadSensorSettings(TerminusSettings settings)
	{
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.sensorLayout);
		
		sensorCheckBoxes = new ArrayList<SensorCheckBox>();
		List<Integer> sensors = TerminusSensorManager.getSupportedSensors(this);
		
		for (int i = 0; i < sensors.size(); i++)
		{
			SensorCheckBox cb = new SensorCheckBox(this);
			sensorCheckBoxes.add(cb);
			cb.sensorType = sensors.get(i);
			cb.setText(TerminusSensorManager.getSensorDescription(cb.sensorType));
			cb.setChecked(settings.containsSenor(cb.sensorType));
			layout.addView(cb);
		}
	}
	
	public void cancelClicked(View view)
	{
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}
	
	public void okClicked(View view)
	{
		TerminusSettings settings = new TerminusSettings();
		
		try
		{
			saveNetworkSettings(settings);
			saveCameraSettings(settings);
			saveSoundSettings(settings);
			saveSensorSettings(settings);
		}
		catch (Exception e)
		{
			return;
		}
		
		settings.commit(this);
		Intent intent = new Intent();
		setResult(RESULT_OK, intent);
		finish();
	}
	
	private void saveNetworkSettings(TerminusSettings settings) throws Exception
	{
		EditText et = (EditText) findViewById(R.id.port);
		String portText = et.getText().toString();
		
		int port = 0;
		
		try 
		{
			port = Integer.valueOf(portText);
			if (port < 1 || port > 60000)
			{
				Dialogs.getOKOnlyAlert("Port must be between 1 and 60000", this).show();
				throw new Exception("Invalid Port");
			}
		} 
		catch (NumberFormatException e) 
		{
			Dialogs.getOKOnlyAlert("Invalid Number Format", this).show();
			throw new Exception("Invalid Port");
		}
		
		settings.port = port;
		
		et = (EditText) findViewById(R.id.ip_address);
		settings.ipAddress = et.getText().toString();
	}
	
	private void saveSensorSettings(TerminusSettings settings)
	{
		settings.sensorList = new ArrayList<Integer>();
		
		for (int i = 0 ; i < sensorCheckBoxes.size(); i++)
		{
			if (sensorCheckBoxes.get(i).isChecked())
				settings.sensorList.add(sensorCheckBoxes.get(i).sensorType);
		}
	}
	
	private void saveSoundSettings(TerminusSettings settings)
	{
		CheckBox cb = (CheckBox) findViewById(R.id.cbSound);
		settings.useSound = cb.isChecked();
	}
	
	private void saveCameraSettings(TerminusSettings settings)
	{
		RadioGroup rg = (RadioGroup) findViewById(R.id.cameraRadioGroup);
		
		int selected = rg.getCheckedRadioButtonId();
		
		switch (selected)
		{
		
		case R.id.cameraRadioNative:
			settings.cameraOption = TerminusSettings.CAMERA_NATIVE;
			break;
		
		case R.id.cameraRadioLowlevel:
			settings.cameraOption = TerminusSettings.CAMERA_LOWLEVEL;
			break;
		
		case R.id.cameraRadioJava:
			settings.cameraOption = TerminusSettings.CAMERA_JAVA;
			break;
			
		default:
			settings.cameraOption = TerminusSettings.CAMERA_NONE;
			break;
			
		}
	}
}
