package edu.buffalo.cse.terminus.client.sensors;

import edu.buffalo.cse.terminus.client.R;

public class CameraOpenCVAlgoJava extends CameraOpenCVAlgo
{
	@Override
	public int getBaseView() 
	{	
		return R.id.cameraViewJava;
	}
	
	@Override
	public int getLayoutId()
	{
		return R.layout.fragment_opencv_java;
	}
}
