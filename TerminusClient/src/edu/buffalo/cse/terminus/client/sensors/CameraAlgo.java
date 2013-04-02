package edu.buffalo.cse.terminus.client.sensors;

import android.app.Activity;
import edu.buffalo.cse.terminus.client.TerminusController;

public abstract class CameraAlgo 
{
	TerminusController controller;
	Activity activity;
	
	public CameraAlgo(TerminusController c, Activity t)
	{
		controller = c;
		activity   = t;
	}
	public abstract void setController(TerminusController c);
	public abstract void onMotionDetected();
	public abstract void startAlgo();
	public abstract void pauseAlgo();
	public abstract void resumeAlgo();
	public abstract void stopAlgo();
}
