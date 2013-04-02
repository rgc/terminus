package edu.buffalo.cse.terminus.client.sensors;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.imgproc.Imgproc;

import android.app.Activity;
import android.view.SurfaceView;


import edu.buffalo.cse.terminus.client.R;
import edu.buffalo.cse.terminus.client.TerminusController;

public class CameraOpenCVAlgo extends CameraAlgo implements CvCameraViewListener2
{
	public static final int CONTOUR_AREA_THRESH = 1200;
	
	private CameraBridgeViewBase mOpenCvCameraView;
	
    double learningRate;   
    private BackgroundSubtractorMOG mogBgSub;
    
    private Mat mGray;
    private Mat mRgba;
	private Mat mRgb;
	private Mat mFGMask;
	private Mat mHierarchy;
	private List<MatOfPoint> contours;
	private Scalar CONTOUR_COLOR;
	private Size ksize;
    
    private BaseLoaderCallback mLoaderCallback;
    
	public CameraOpenCVAlgo(TerminusController c, Activity t) 
	{
		super(c, t);
		
        mOpenCvCameraView = (CameraBridgeViewBase) activity.findViewById(R.id.cameraPlaceholder);
        
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);
        
        mOpenCvCameraView.setMaxFrameSize(400, 400);
		
		mLoaderCallback = new BaseLoaderCallback(activity) {
		        @Override
		        public void onManagerConnected(int status) {
		            switch (status) {
		                case LoaderCallbackInterface.SUCCESS:
		                {
		                    mOpenCvCameraView.enableView();
		                 
		                    // for background removal...
		                    // need to tweak these
		                  //learningRate 	= .05;
		                    learningRate	= .1;
		                  //mogBgSub 		= new BackgroundSubtractorMOG();
		                    mogBgSub 		= new BackgroundSubtractorMOG(3, 4, 0.8);
		                        
		                    // try backgroundsubtractormog2 ?
		                    
		                    mGray 			= new Mat();
		                    mRgba 			= new Mat();
		                    mRgb 			= new Mat();
		                    mFGMask			= new Mat();
		                    
		                    ksize			= new Size(25,25);
		                    
		                    // for contours
		                    mHierarchy  	= new Mat();
		                    contours 		= new ArrayList<MatOfPoint>();
		                    CONTOUR_COLOR 	= new Scalar(255,0,0,255);
		                    
		                } break;
		                default:
		                {
		                    super.onManagerConnected(status);
		                } break;
		            }
		        }
		    };
		    
	}
	
	@Override
	public void setController(TerminusController c) {
		controller = c;
	}
	
	@Override
	public void onMotionDetected() 
	{
		//this.controller.sensorEventSensed(Sensor.TYPE_LIGHT);
	}

	@Override
	public void startAlgo() 
	{
		
	}
	
	@Override
	public void pauseAlgo() 
	{
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();	
	}
	
	@Override
	public void resumeAlgo() 
	{
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, activity, mLoaderCallback);	
	}
	
	@Override
	public void stopAlgo() 
	{
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();	
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	
		mRgba = inputFrame.rgba();
    	mGray = inputFrame.gray();
    	
    	// 6.2 fps here
    	
    	// ********* background subtraction **********
    	
    	// the apply function will throw an error if you don't feed it an RGB image
    	// but it exports a gray image, so we need to convert the gray MAT
    	// into RGB before we apply it to the foreground mask
        Imgproc.cvtColor(mGray, mRgb, Imgproc.COLOR_GRAY2RGB);
        // 5.2 fps here
                
        //apply() exports a gray image by definition
		mogBgSub.apply(mRgb, mFGMask, learningRate);
		
		// 0.27 fps here, ugh.
		
		// blur the fgmask to reduce the number of contours
        Imgproc.GaussianBlur(mFGMask, mFGMask, ksize, 0);
		
		// debug
		//if(true) return mRgb;
		//if(true) return mFGMask;
		
		// re-init or the old contours will stay on screen 
		contours = new ArrayList<MatOfPoint>();
		
		Imgproc.findContours(mFGMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		// will draw the outlines on the regions - debug
		//Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR, 3);
		
		boolean motion = false;
		
		Rect r;
		for (int i = 0; i < contours.size(); i++) {
			r = Imgproc.boundingRect(contours.get(i));
			// if bounding rect larger than min area, draw rect on screen
			if(r.area() > CONTOUR_AREA_THRESH) {
				// draw a rectangle around any contour with area greater
				// than threshold
				Core.rectangle(mRgba, r.tl(), r.br(), CONTOUR_COLOR, 3);
				
				// we have motion!
				motion = true;
			}
		}
		
		// one event per frame, at most
		if(motion) {
			if(controller != null ) {
				controller.onCameraMotionDetected();
			}
		}
		
		return mRgba;
	
	}
	
}
