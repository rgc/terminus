	package edu.buffalo.cse.terminus.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import edu.buffalo.cse.terminus.client.TerminusController.NetworkSettings;
import edu.buffalo.cse.terminus.client.TerminusController.SensorSettings;
import edu.buffalo.cse.terminus.client.network.INetworkCallbacks;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.Window;

public class TerminusClientMainActivity extends FragmentActivity implements INetworkCallbacks, SensorEventListener
{
	StatusFragment statusFragment;
	private boolean getParameters = true;
	
	private static final int INTENT_CONNECTION_PARAMS = 1;
	private static final int INTENT_SENSOR_PARAMS = 2;
	
	String ipAddress = "";
	int port = 0;
	TerminusController controller = null;
	
	private void setUpFragments()
	{
		FragmentManager fm = this.getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();	
		statusFragment = new StatusFragment();
		
		ft.add(R.id.statusPlaceholder, statusFragment);
		ft.commit();
	}
	
	public void setCameraFragment(Fragment f)
	{
		//TODO: set camera in camera place holder layout
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_terminus_client_main);
		
		setUpFragments();
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		
		if (controller != null)
		{
			controller.stop();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//The fragments aren't created until after onCreate
		//statusFragment.setTextColor(Color.RED);
		//statusFragment.setText("Network Status: Disconnected");
		
		if (getParameters)
		{
			getParametersFromActivites();
		}
		else if (controller != null)
		{
			controller.start();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_terminus_client_main, menu);
		return true;
	}
	
	private void getParametersFromActivites()
	{
		getParameters = false;
		Intent intent = new Intent(this, ConnectionParameters.class);
		startActivityForResult(intent, INTENT_CONNECTION_PARAMS);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) 
	{
		if (resultCode != RESULT_OK)
		{
			finish();
		}
		else if (requestCode == INTENT_CONNECTION_PARAMS)
		{
			this.ipAddress = data.getStringExtra(ConnectionParameters.IP_ADDRESS_PARAM);
			this.port = data.getIntExtra(ConnectionParameters.PORT_PARAM, 0);
			
			Intent intent = new Intent(this, GetSensorsActivity.class);
			startActivityForResult(intent, INTENT_SENSOR_PARAMS);
		}
		else if (requestCode == INTENT_SENSOR_PARAMS)
		{
			int[] sensors = data.getIntArrayExtra(GetSensorsActivity.SENSORS_PARAM);
			
			statusFragment.setSensorText("Sensor Count: " + String.valueOf(sensors.length));
			statusFragment.setNetworkText("Network: Not Connected", Color.BLACK);
			
			NetworkSettings ns = new NetworkSettings();
			ns.ip = ipAddress;
			ns.port = port; 
			ns.networkCallbacks = new UIEventBridge(this, this);
			
			SensorSettings ss = new SensorSettings();
			ss.listener = this;
			ss.sensorList = sensors;
			
			controller = new TerminusController(ns, ss, this);
			controller.start();
		}
	}
	
	@Override
	public void onConnectionComplete() 
	{
		statusFragment.setNetworkText("Network: Connected", Color.GREEN);
	}

	@Override
	public void onConnectionError(IOException e) 
	{
		statusFragment.setNetworkText("Network: Connection Error", Color.RED);
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) 
	{
		statusFragment.setNetworkText("Network: Connection Timed Out", Color.RED);
	}

	@Override
	public void onConnectionError(UnknownHostException e) 
	{
		statusFragment.setNetworkText("Network: Unknown Host", Color.RED);
	}

	@Override
	public void onDisconnectComplete() 
	{
		statusFragment.setNetworkText("Network: Not Connected", Color.BLACK);
	}

	@Override
	public void onConnectionDropped() 
	{
		statusFragment.setNetworkText("Network: Not Connected", Color.BLACK);
	}

	@Override
	public void onMessageReceived(TerminusMessage msg) 
	{ 
		if (msg.getMessageType() == TerminusMessage.MSG_REG_RESPONSE)
		{
			RegistrationResponse res = (RegistrationResponse) msg;
			if (res.getResult() == RegistrationResponse.REGISTRATION_SUCCESS)
			{
				statusFragment.setNetworkText("Network: Connected", Color.GREEN);
			}
		}
	}

	@Override
	public void onSendComplete() 
	{
		
	}

	@Override
	public void onMessageFailed(TerminusMessage msg) 
	{ 
		
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		String t = String.valueOf(event.values[0]);
		
		switch (event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			statusFragment.setSensorText("Accelerometer: " + t);
			break;
			
		case Sensor.TYPE_MAGNETIC_FIELD:
			statusFragment.setSensorText("Magnetometer: " + t);
			break;
			
		case Sensor.TYPE_LIGHT:
			statusFragment.setSensorText("Light: " + t);
			break;
		}
	}
}
