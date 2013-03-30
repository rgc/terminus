package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class AccelCDFAlgo extends SensorAlgo 
{
	public static final int VIBRATION_FACTOR = 20;
	
	//raw sensor data
	private float[] xacel;
	private float[] yacel;
	private float[] zacel;
	
	public AccelCDFAlgo(TerminusController c) 
	{
		super(c);
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		// start wasn't called
		if (xacel == null)
			startAlgo();
		
		//shift old values
		CDFFunctions.shifta(xacel);
		CDFFunctions.shifta(yacel);
		CDFFunctions.shifta(zacel);
		
		//assign values
		xacel[0] = event.values[0];
		yacel[0] = event.values[1];
		zacel[0] = event.values[2];
		
		//find derivative
		float dx = CDFFunctions.CDF1O4(xacel);
		float dy = CDFFunctions.CDF1O4(yacel);
		float dz = CDFFunctions.CDF1O4(zacel);

		//check for large change in some direction
		if((dx > VIBRATION_FACTOR)||(dy > VIBRATION_FACTOR)||(dz > VIBRATION_FACTOR))
		{
			this.controller.sensorEventSensed(Sensor.TYPE_ACCELEROMETER);
		}
	}

	@Override
	public void startAlgo() 
	{
		xacel = new float[5];
		yacel = new float[5];
		zacel = new float[5];
	}

	@Override
	public void stopAlgo() 
	{
		xacel = null;
		yacel = null;
		zacel = null;
	}

}
