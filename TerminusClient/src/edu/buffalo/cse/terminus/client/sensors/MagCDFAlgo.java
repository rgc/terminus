package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class MagCDFAlgo extends SensorAlgo 
{
	public static final int MAGNET_FACTOR = 20;
	
	//raw sensor data
	public float[] mlevel;
	
	public MagCDFAlgo(TerminusController c) 
	{
		super(c);
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		if (mlevel == null)
			startAlgo();
		
		CDFFunctions.shifta(mlevel);
		mlevel[0]=event.values[0];
		float dm = CDFFunctions.CDF1O4(mlevel);
		
		if(dm > MAGNET_FACTOR)
		{
			this.controller.sensorEventSensed(Sensor.TYPE_MAGNETIC_FIELD);
		}
	}

	@Override
	public void startAlgo() 
	{
		mlevel = new float[5];
	}

	@Override
	public void stopAlgo() 
	{
		mlevel = null;
	}

}
