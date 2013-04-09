package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;

import edu.buffalo.cse.terminus.client.TerminusController;

public class MagCDFAlgo extends SensorAlgo 
{
	public static int MAGNET_FACTOR = 20;
	public static boolean FirstMagPri = false;
	
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
		float dm = Math.abs(CDFFunctions.CDF1O4(mlevel));
		
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
	}

	@Override
	public void stopAlgo() 
	{
		mlevel = null;
	}

}
