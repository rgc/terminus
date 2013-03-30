package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class LightCDFAlgo extends SensorAlgo 
{
	public static final int LIGHT_FACTOR = 20;
	
	//raw sensor data
	public float[] llevel;
	
	public LightCDFAlgo(TerminusController c) 
	{
		super(c);
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		if (llevel == null)
			startAlgo();
		
		CDFFunctions.shifta(llevel);
		llevel[0] = event.values[0];
		float dl = Math.abs(llevel[0] - llevel[1]);
		
		//TODO: Not checking dl??
		if(dl > 1)
		{
			this.controller.sensorEventSensed(Sensor.TYPE_LIGHT);
		}
	}

	@Override
	public void startAlgo() 
	{
		llevel = new float[5];
	}

	@Override
	public void stopAlgo() 
	{
		llevel = null;
	}

}
