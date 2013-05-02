package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class AccelCDFAlgo extends SensorAlgo 
{
	public static int VIBRATION_FACTOR = 1;
	public static boolean FirstAclPri = false;
	
	//raw sensor data
	private float[] xacel;
	private float[] yacel;
	private float[] zacel;
	private long[] time;
	public static float t;
	
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
		CDFFunctions.shifta(time);
		
		//assign values
		xacel[0] = event.values[0];
		yacel[0] = event.values[1];
		zacel[0] = event.values[2];
		time[0] = event.timestamp;
		
		t = CDFFunctions.avgt(time);
		//find derivative
		float dx = CDFFunctions.CDF1O8(xacel, t);
		float dy = CDFFunctions.CDF1O8(yacel, t);
		float dz = CDFFunctions.CDF1O8(zacel, t);

		//check for large change in some direction
		if((dx > VIBRATION_FACTOR)||(dy > VIBRATION_FACTOR)||(dz > VIBRATION_FACTOR))
		{
			int AclPri = 0;
			if(FirstAclPri == false){
				FirstAclPri = true;
				AclPri+=30;
			}
			AclPri+=(dx/10);
			AclPri+=(dy/10);
			AclPri+=(dz/10);
			
			this.controller.sensorEventSensed(Sensor.TYPE_ACCELEROMETER, AclPri);
			
		}
	}

	@Override
	public void startAlgo() 
	{
		xacel = new float[9];
		yacel = new float[9];
		zacel = new float[9];
		time = new long[10];
	}

	@Override
	public void stopAlgo() 
	{
		xacel = null;
		yacel = null;
		zacel = null;
	}

}
