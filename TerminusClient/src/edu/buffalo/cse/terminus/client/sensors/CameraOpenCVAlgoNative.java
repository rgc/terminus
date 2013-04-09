package edu.buffalo.cse.terminus.client.sensors;

import edu.buffalo.cse.terminus.client.R;

public class CameraOpenCVAlgoNative extends CameraOpenCVAlgo 
{
	@Override
	public int getBaseView() 
	{	
		return R.id.cameraViewNative;
	}
	
	@Override
	public int getLayoutId()
	{
		return R.layout.fragment_opencv_native;
	}
}
