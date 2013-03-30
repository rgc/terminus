package edu.buffalo.cse.terminus.client.sensors;

import android.hardware.SensorEvent;
import edu.buffalo.cse.terminus.client.TerminusController;

public abstract class SensorAlgo 
{
	TerminusController controller;
	
	public SensorAlgo(TerminusController c)
	{
		controller = c;
	}
	
	public abstract void onSensorChanged(SensorEvent event);
	public abstract void startAlgo();
	public abstract void stopAlgo();
}
