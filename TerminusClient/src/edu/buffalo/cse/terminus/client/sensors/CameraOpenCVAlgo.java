package edu.buffalo.cse.terminus.client.sensors;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;
import org.opencv.core.Size;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import org.opencv.video.BackgroundSubtractorMOG;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import edu.buffalo.cse.terminus.client.TerminusController;

public abstract class CameraOpenCVAlgo extends CameraAlgo implements CvCameraViewListener2
{
	public static final int CONTOUR_AREA_THRESH = 1600;
	
	private CameraBridgeViewBase mOpenCvCameraView;
	
    double learningRate;   
    private BackgroundSubtractorMOG mogBgSub;
    
    private Mat mGray;
    private Mat mGrayBox;
    private Mat mRgba;
	private Mat mRgb;
	private Mat mFGMask;
	private Mat mHierarchy;
	private List<MatOfPoint> contours;
	private Scalar CONTOUR_COLOR;
	private Size ksize;
	
	private boolean hadMotionLastFrame;
    
    private BaseLoaderCallback mLoaderCallback;
    
    private ArrayList<Long> frameTimes = new ArrayList<Long>();
    
    /**
     * Get the integer view id for the underlying CameraBridgeViewBase
     * preview window
     * @return the integer value of the CameraBridgeViewBase
     */
    public abstract int getBaseView();
    
    /**
     * Get the integer layout id for this fragment
     * @return the integer value of the layout found in R.layout
     */
    public abstract int getLayoutId();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) 
	{
	    View view = inflater.inflate(getLayoutId(), container, false);
	    
	    return view;
	}
	
	private void initCameraSettings()
	{
		hadMotionLastFrame = false;
		
        mOpenCvCameraView = (CameraBridgeViewBase) getActivity().findViewById(getBaseView());
        
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        
        mOpenCvCameraView.setCvCameraViewListener(this);
        
        mOpenCvCameraView.setMaxFrameSize(400, 400);
        
		mLoaderCallback = new BaseLoaderCallback(getActivity()) {
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
	                    
	                    // tried 25... 35 is best for now
	                    ksize			= new Size(35,35);
	                    
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
	public void setController(TerminusController c) 
	{
		controller = c;
	}
	
	@Override
	public void onResume() 
	{
		super.onResume();
		
		initCameraSettings();
		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, getActivity(), mLoaderCallback);
	}
	
	@Override
	public void onPause() 
	{
		super.onPause();
		
		if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
	}

	public void onCameraViewStarted(int width, int height) {
	}

	public void onCameraViewStopped() {
	}

	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
	
		mRgba 		= inputFrame.rgba();
    	mGray 		= inputFrame.gray();
    	mGrayBox	= mGray.clone();
    	
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
		
		// re-init or the old contours will stay on screen 
		contours = new ArrayList<MatOfPoint>();
		
		if (!mFGMask.empty()) {
			Imgproc.findContours(mFGMask, contours, mHierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
			
			// will draw the outlines on the regions
			//Imgproc.drawContours(mGrayBox, contours, -1, CONTOUR_COLOR, 3);
			
			boolean motion = false;
			
			Rect r;
			for (int i = 0; i < contours.size(); i++) {
				r = Imgproc.boundingRect(contours.get(i));
				// if bounding rect larger than min area, draw rect on screen
				if(r.area() > CONTOUR_AREA_THRESH) {
					// draw a rectangle around any contour with area greater
					// than threshold
					//Core.rectangle(mRgba, r.tl(), r.br(), CONTOUR_COLOR, 2);
					Core.rectangle(mGrayBox, r.tl(), r.br(), CONTOUR_COLOR, 2);
					
					// we have motion!
					motion = true;
				}
			}
			
			updateFrameTimes();
			
			// one event per frame, at most
			if(motion) {
				if(controller != null ) {
					hadMotionLastFrame  = true;
					MatOfByte matOfByte = new MatOfByte();
			        Highgui.imencode(".png", mGrayBox, matOfByte);
			        byte[] imageBytes = matOfByte.toArray();
					controller.onCameraMotionDetected(imageBytes, getFPS());
					
				}
			} else if(hadMotionLastFrame) {
				hadMotionLastFrame = false;
				
				// send a clean "unboxed" image
				MatOfByte matOfByte = new MatOfByte();
		        Highgui.imencode(".png", mGray, matOfByte);
		        byte[] imageBytes = matOfByte.toArray();
				controller.onCameraMotionDetected(imageBytes, getFPS());
			}
		}
		return mRgba;
	
	}
	
	private void updateFrameTimes()
	{
		if (frameTimes.size() == 10)
			frameTimes.remove(0);
		
		frameTimes.add(System.nanoTime());
	}
	
	public float getFPS()
	{
		int count = frameTimes.size();
		float fps = 0;
		if (count > 0)
		{
			long start = frameTimes.get(0);
			long end = frameTimes.get(count - 1);
			float sec = (end - start) / 1000000000f;
			fps = count / sec;
		}
		
		return fps;
	}
}
