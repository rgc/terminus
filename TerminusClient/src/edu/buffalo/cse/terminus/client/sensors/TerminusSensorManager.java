package edu.buffalo.cse.terminus.client.sensors;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.terminus.client.TerminusController;
import edu.buffalo.cse.terminus.client.TerminusSettings;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TerminusSensorManager implements SensorEventListener 
{	
	/*
	 * This is just for UI updates/debugging
	 */
	private SensorEventListener sensorCallback;
	
	private SensorManager sensorManager;
	private TerminusSettings settings;
	private TerminusController controller;
	
	private SensorAlgo accelerometerAlgo;
	private SensorAlgo magnetometerAlgo;
	private SensorAlgo lightAlgo;
	
	public TerminusSensorManager(TerminusSettings settings, SensorEventListener listener, 
			TerminusController controller, Activity activity)
	{
		this.settings = settings;
		this.controller = controller;
		this.sensorCallback = listener;
		
		sensorManager = (SensorManager) activity.getSystemService(android.content.Context.SENSOR_SERVICE);
		createSensorAlgos();
	}
	
	private void createSensorAlgos()
	{
		for (int i = 0; i < settings.sensorList.size(); i++)
		{
			switch (settings.sensorList.get(i))
			{
			case Sensor.TYPE_ACCELEROMETER:
				accelerometerAlgo = new AccelCDFAlgo(controller); 
				break;
				
			case Sensor.TYPE_LIGHT:
				lightAlgo = new LightCDFAlgo(controller);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				magnetometerAlgo = new MagCDFAlgo(controller);
				break;
				
			default:
				break;
			}
		}
	}
	
	private void startSensorAlgos()
	{
		if (accelerometerAlgo != null)
			accelerometerAlgo.startAlgo();
		
		if (magnetometerAlgo != null)
			magnetometerAlgo.startAlgo();
		
		if (lightAlgo != null)
			lightAlgo.startAlgo();
	}
	
	private void stopSensorAlgos()
	{
		if (accelerometerAlgo != null)
			accelerometerAlgo.stopAlgo();
		
		if (magnetometerAlgo != null)
			magnetometerAlgo.stopAlgo();
		
		if (lightAlgo != null)
			lightAlgo.stopAlgo();
	}
	
	public void start()
	{
		/*
		 * Register all sensors that were selected
		 */
		for (int i = 0; i < settings.sensorList.size(); i++)
		{
			int type = settings.sensorList.get(i);
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(type),SensorManager.SENSOR_DELAY_UI);	
	    }
		startSensorAlgos();
	}
	
	public void stop()
	{
		/*
		 * Unregister all sensors that were selected in start()
		 */
		sensorManager.unregisterListener(this);
		stopSensorAlgos();
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		//Nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{	
		switch (event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			if (accelerometerAlgo != null)
			{
				accelerometerAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		case Sensor.TYPE_MAGNETIC_FIELD:
			if (magnetometerAlgo != null)
			{
				magnetometerAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		case Sensor.TYPE_LIGHT:
			if (lightAlgo != null)
			{
				lightAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		default:
			
			return;
		}
	}
	
	public static List<Integer> getSupportedSensors(Activity context)
	{
		/*
		 * Both of the following need to be true in order to use
		 * a specific sensor:
		 * 	- We need to support it
		 *  - The device needs to support it 
		 */
		
		SensorManager sm = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
		ArrayList<Integer> supported = new ArrayList<Integer>();
		
		for (Sensor s : sensors)
		{
			switch (s.getType())
			{
			case Sensor.TYPE_ACCELEROMETER:
				supported.add(Sensor.TYPE_ACCELEROMETER);
				break;
				
			case Sensor.TYPE_LIGHT:
				supported.add(Sensor.TYPE_LIGHT);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				supported.add(Sensor.TYPE_MAGNETIC_FIELD);
				break;
				
			default:
				break;
			}
		}
		
		return supported;
	}
	
	public static String getSensorDescription(int type)
	{
		String out = "";
		
		switch (type)
		{
		case Sensor.TYPE_ACCELEROMETER:
			out = "Accelerometer";
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			out = "Magnetometer";
			break;
		case Sensor.TYPE_LIGHT:
			out = "Ambient Light";
			break;
		default:
			out = "";
			break;
		}
		
		return out;
	}
}
