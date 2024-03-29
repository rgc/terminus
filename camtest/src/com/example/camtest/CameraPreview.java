package com.example.camtest;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private boolean secpic;
    private static final String TAG = "MyCameraPreview";
    private PictureCallback mPicture;

    public CameraPreview(Context context,Camera camera, boolean pictwo, PictureCallback Picture) {
        super(context);
        mCamera = camera;
        secpic=pictwo;
        mPicture=Picture;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);   


    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        
    	Log.d(TAG, "surface");
        if (mHolder.getSurface() == null){
            // preview surface does not exist
        	return;
        }
        
        // stop preview before making changes
        try {
        	mCamera.stopPreview();
        } 
        catch (Exception e){
		    // ignore: tried to stop a non-existent preview
        }
		
        // set preview size and make any resize, rotate or
        // reformatting changes here
		
        // start preview with new settings
        try {
        	mCamera.setPreviewDisplay(mHolder);
        	mCamera.startPreview();
		
        } catch (Exception e){
		
        }
        if(secpic==true){//check if you need to take a second picture
			secpic=false;
			mCamera.takePicture(null, null, mPicture);
		}
        
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

}