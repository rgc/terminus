package com.example.camtest;

import android.content.Context;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback{

    private SurfaceHolder mHolder;
    private Camera mCamera;

    public CameraPreview(Context context,Camera camera) {
        super(context);
        mCamera = camera;

        /*SurfaceView view = new SurfaceView(this);
        c.setPreviewDisplay(view.getHolder());
        c.startPreview();
        c.takePicture(shutterCallback, rawPictureCallback, jpegPictureCallback);
         * */

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();

        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);   


    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub

        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
          }

          // stop preview before making changes
          try {
              mCamera.stopPreview();
          } catch (Exception e){
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

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

}