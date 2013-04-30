package edu.buffalo.cse.terminus.client;
	
import java.io.IOException;

import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import edu.buffalo.cse.terminus.client.network.INetworkCallbacks;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import edu.buffalo.cse.terminus.client.sensors.AccelCDFAlgo;
import edu.buffalo.cse.terminus.client.sensors.CameraAlgo;
import edu.buffalo.cse.terminus.client.sensors.CameraOpenCVAlgoJava;
import edu.buffalo.cse.terminus.client.sensors.CameraOpenCVAlgoNative;
import edu.buffalo.cse.terminus.client.sensors.SoundAlgo;

public class TerminusClientMainActivity extends Activity implements INetworkCallbacks, SensorEventListener
{	
	private TerminusController controller = null;
    private TextView tvNetwork;
	private TextView tvSensors;
	private CameraAlgo curCameraFrag;	//Fragment
    
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_terminus_client_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
        tvNetwork = (TextView) findViewById(R.id.tvNetwork);
	    tvSensors = (TextView) findViewById(R.id.tvSensors);
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		stopSensors();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		startSensors();
	}
	
	private void startSensors()
	{
		/*
		 * For now, we'll stop everything and re-start it.  In the future,
		 * we should try to avoid the multiple onCreate/onResume issues.
		 */
		stopSensors();
		
		TerminusSettings settings = new TerminusSettings();
	    settings.retrieve(this);
	    
		setNetworkText("Network: Not Connected", Color.BLACK);
		setSensorText("Sensor Count: " + String.valueOf(settings.sensorList.size()));
		
		controller = new TerminusController(settings, this, new UIEventBridge(this, this), this);
		controller.start();
		
		startCamera(settings);
		
	}
	
	private void startCamera(TerminusSettings settings)
	{
		/*
		 * In order to switch between different camera algorithms, we use fragments.
		 * Each type of camera algo is a fragment
		 */
		
		//First, remove the old fragment
		FragmentManager fm = getFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		
		if (curCameraFrag != null)
		{
			ft.remove(curCameraFrag);
			ft.commit();
			ft = fm.beginTransaction();
		}
		
		//Next, insert the new fragment
		switch (settings.cameraOption)
		{
		case TerminusSettings.CAMERA_NATIVE:
			curCameraFrag = new CameraOpenCVAlgoNative();
			curCameraFrag.setController(controller);
			ft.add(R.id.cameraPlaceholder, curCameraFrag);
			ft.commit();
	        break;
	        
		case TerminusSettings.CAMERA_JAVA:
			curCameraFrag = new CameraOpenCVAlgoJava();
			curCameraFrag.setController(controller);
			ft.add(R.id.cameraPlaceholder, curCameraFrag);
			ft.commit();
	        break;
	        
        default:
        	ft.commit();
        	break;
		}
	}
	
	private void stopSensors()
	{
		if (controller != null)
			controller.stop();
		
		if (curCameraFrag != null)
		{
			//This will cause the camera algo to stop
			FragmentManager fm = getFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(curCameraFrag);
			ft.commit();
			ft = fm.beginTransaction();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_terminus_client_main, menu);
		return true;
	}
	
	/*
	 * This is the method that gets called when the users clicks the
	 * settings menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
	    switch (item.getItemId()) 
	    {
	        case R.id.menu_settings:
	            showSettings();
	            return true;
	        
	        case R.id.menu_reconnect:
	        	controller.reconnectNetwork();
	        	return true;
	        	
	        case R.id.menu_disconnect:
	        	controller.disconnectNetwork();
	        	return true;
	        	
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showSettings()
	{
		/*
		 * We don't care about the result.  For now, onResume handles, 
		 * we just re-launching everything after reloading the parameters. 
		 */
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}
		
	@Override
	public void onConnectionComplete() 
	{
		setNetworkText("Network: Connected", Color.GREEN);
	}

	@Override
	public void onConnectionError(IOException e) 
	{
		setNetworkText("Network: Connection Error", Color.RED);
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) 
	{
		setNetworkText("Network: Connection Timed Out", Color.RED);
	}

	@Override
	public void onConnectionError(UnknownHostException e) 
	{
		setNetworkText("Network: Unknown Host", Color.RED);
	}

	@Override
	public void onDisconnectComplete() 
	{
		setNetworkText("Network: Not Connected", Color.BLACK);
	}

	@Override
	public void onConnectionDropped() 
	{
		setNetworkText("Network: Not Connected", Color.BLACK);
	}

	@Override
	public void onMessageReceived(TerminusMessage msg) 
	{ 
		if (msg.getMessageType() == TerminusMessage.MSG_REG_RESPONSE)
		{
			RegistrationResponse res = (RegistrationResponse) msg;
			if (res.getResult() == RegistrationResponse.REGISTRATION_SUCCESS)
			{
				setNetworkText("Network: Connected", Color.GREEN);
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
		String t = String.valueOf(controller.getTotalPriority());
		
		switch (event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			setSensorText("Accelerometer: " + t);
			break;
			
		case Sensor.TYPE_MAGNETIC_FIELD:
			setSensorText("Magnetometer: " + t);
			break;
			
		case Sensor.TYPE_LIGHT:
			setSensorText("Light: " + t);
			break;
		}
	}
	
	/*
	 * Try to set the textview's text/color
	 * 
	 * If this is called before the view has been created,
	 * nothing happens
	 */
	public void setNetworkText(String text)
	{
		if (tvNetwork != null)
		{
			tvNetwork.setText(text);
		}
	}
	
	public void setNetworkText(String text, int color)
	{
		if (tvNetwork != null)
		{
			tvNetwork.setText(text);
			tvNetwork.setTextColor(color);
		}
	}

	public void setSensorText(String text)
	{
		if (tvSensors != null)
		{
			tvSensors.setText(text);
		}
	}
	
	public void setSensorText(String text, int color)
	{
		if (tvSensors != null)
		{
			tvSensors.setText(text);
			tvSensors.setTextColor(color);
		}
	}
}
