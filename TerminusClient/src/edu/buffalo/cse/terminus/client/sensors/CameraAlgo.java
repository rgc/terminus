package edu.buffalo.cse.terminus.client.sensors;

import android.app.Fragment;
import edu.buffalo.cse.terminus.client.TerminusController;

public abstract class CameraAlgo extends Fragment
{
	TerminusController controller;
	
	public void setController(TerminusController c)
	{
		this.controller = c;
	}
	
	//public abstract void startAlgo();
	//public abstract void pauseAlgo();
	//public abstract void resumeAlgo();
	//public abstract void stopAlgo();
}
