package edu.buffalo.cse.terminus.client.sensors;

public interface ICameraCallbacks
{
	/*
	 * Called when motion is detected
	 */
	public void onCameraMotionDetected();
	public void onCameraMotionDetected(byte[] image);
	
}
