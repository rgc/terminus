package edu.buffalo.cse.terminus.client.sensors;

import java.util.List;

import edu.buffalo.cse.terminus.client.TerminusController;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class TerminusSensorManager implements SensorEventListener 
{
	public static final int SENSOR_TYPE_CAMERA = -2;
	public static final int SENSOR_TYPE_MIC = -3;
	
	/*
	 * This is just for UI updates/debugging
	 */
	private SensorEventListener sensorCallback;
	
	private SensorManager sensorManager;
	private int[] sensors;
	private TerminusController controller;
	
	private SensorAlgo accelerometerAlgo;
	private SensorAlgo magnetometerAlgo;
	private SensorAlgo lightAlgo;
	
	public TerminusSensorManager(int[] sensors, SensorEventListener l, TerminusController c, Activity a)
	{
		this.sensors = sensors;
		this.controller = c;
		this.sensorCallback = l;
		
		sensorManager = (SensorManager) a.getSystemService(android.content.Context.SENSOR_SERVICE);
		createSensorAlgos();
	}
	
	private void createSensorAlgos()
	{
		for (int i = 0; i < this.sensors.length; i++)
		{
			switch (i)
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
		for (int i = 0; i < sensors.length; i++)
		{
			int type = sensors[i];
			
			if (type != SENSOR_TYPE_CAMERA && type != SENSOR_TYPE_MIC)
			{
				sensorManager.registerListener(this, sensorManager.getDefaultSensor(type),SensorManager.SENSOR_DELAY_UI);	
			}
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
			
		case Sensor.TYPE_GRAVITY:
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
	
	public static int[] getSupportedSensors(Activity context)
	{
		/*
		 * Both of the following need to be true in order to use
		 * a specific sensor:
		 * 	- We need to support it
		 *  - The device needs to support it 
		 */
		
		/*
		 * We hard code camera and microphone
		 * These are the other sensors we currently support
		 */
		boolean accel = false;
		boolean mag = false;
		boolean light = false;
		
		SensorManager sm = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
		
		for (Sensor s : sensors)
		{
			switch (s.getType())
			{
			case Sensor.TYPE_ACCELEROMETER:
				accel = true;
				break;
				
			case Sensor.TYPE_LIGHT:
				light = true;
				break;
				
			case Sensor.TYPE_GRAVITY:
				mag = true;
				break;
				
			default:
				break;
			}
		}
		
		int count = 2;
		
		if (accel)
			count++;
		
		if (mag)
			count++;
		
		if (light)
			count++;
		
		int[] supported = new int[count];
		
		supported[0] = SENSOR_TYPE_CAMERA;
		supported[1] = SENSOR_TYPE_MIC;
		
		count = 2;
		if (accel)
			supported[count++] = Sensor.TYPE_ACCELEROMETER;
		
		if (mag)
			supported[count++] = Sensor.TYPE_MAGNETIC_FIELD;
		
		if (light)
			supported[count++] = Sensor.TYPE_LIGHT;
		
		return supported;
	}
	
	public static String getSensorDescription(int type)
	{
		String out = "";
		
		switch (type)
		{
		case SENSOR_TYPE_CAMERA:
			out = "Camera";
			break;
		case SENSOR_TYPE_MIC:
			out = "Microphone";
			break;
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
