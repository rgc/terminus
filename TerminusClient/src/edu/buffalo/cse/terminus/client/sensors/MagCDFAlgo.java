package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class MagCDFAlgo extends SensorAlgo 
{
	public static int MAGNET_FACTOR = 1;
	public static boolean FirstMagPri = false;
	
	//raw sensor data
	private float[] mlevel;
	private long[] time;
	private float t;
	
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
		CDFFunctions.shifta(time);
		
		mlevel[0]=event.values[0];
		time[0] = event.timestamp;
		
		t = CDFFunctions.avgt(time);
		float dm = Math.abs(CDFFunctions.CDF1O4(mlevel, t));
		
		if(dm > MAGNET_FACTOR)
		{
			int MagPri = 0;
			if(FirstMagPri == false){
				FirstMagPri = true;
				MagPri+=50;
			}
			MagPri+=(dm/10);
			this.controller.sensorEventSensed(Sensor.TYPE_MAGNETIC_FIELD, MagPri);
		}
	}

	@Override
	public void startAlgo() 
	{
		mlevel = new float[5];
		time = new long[6];
	}

	@Override
	public void stopAlgo() 
	{
		mlevel = null;
	}

}
