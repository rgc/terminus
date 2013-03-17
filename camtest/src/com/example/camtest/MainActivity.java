package com.example.camtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

private Camera mCamera;
private CameraPreview mPreview;
private SurfaceHolder mHolder;
public byte[] basePicF, basePicB;
public static final int MEDIA_TYPE_IMAGE = 1;
private SensorManager mSensorManager;
private List<Sensor> msensorList;
private boolean senspause = false;
public boolean sensorsInitilized = false;
public boolean sensdet = false;
public float[] xacel = new float[5];
public float[] yacel = new float[5];
public float[] zacel = new float[5];
public float[] spresure = new float[5];
public float[] llevel = new float[5];
public float[] mlevel = new float[5];
//how sensitive do you want detection, smaller number is more sensitive
public static final int vibrationfactor=20,soundfactor=100000,lightfactor=20,magnetfactor=20;
TextView xCoor,yCoor,zCoor,soundLev,lightLev,magLev;
FrameLayout preview;
private static final String TAG = "MyCameraMain";
private MediaRecorder mRecorder = null;
//enable sensors
public boolean sounde=true, accele=true, lighte=true, magnete=true;
public boolean initpic=false, diffpic=true, multicam=false, secpic=false;
public int currcam=0;
private ConditionVariable picLock = new ConditionVariable();

@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    /*Button captureButton = (Button) findViewById(R.id.button_capture);
    
    if(Camera.getNumberOfCameras()==2){
    	multicam=true;
    	Log.d(TAG, "2cams");
    }
    // Create an instance of Camera
    mCamera = getCameraInstance(currcam);
    // Create our Preview view and set it as the content of our activity.
    //mPreview = new CameraPreview(this, mCamera);
    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    //preview.addView(mPreview);
    //setcamprev();
    
    mPreview = new CameraPreview(this, mCamera);
    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(mPreview);
    
    // Add a listener to the Capture button
	captureButton.setOnClickListener(

			new View.OnClickListener() {

				public void onClick(View v) {
					// get an image from the camera   
					Log.d(TAG, "buttonpress");
					initpic=true;
					takepic();
					mCamera.takePicture(null, null, mPicture);
					
					/*if(multicam==true){
						swapcam();
						mCamera.takePicture(null, null, mPicture);
					}
				}
			}
	);*/
	
	
    // Get the SensorManager 
    /*mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
    List<Sensor> msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    String SensListName = new String("");
    String SensListType = new String("");
    Sensor tmp;
    int i;
    for (i=0;i<msensorList.size();i++){
    	tmp = msensorList.get(i);
    	SensListName = " "+SensListName+tmp.getName(); // Add the sensor name to the string of sensors available
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(tmp.getType()),SensorManager.SENSOR_DELAY_UI);

    }
    
    xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
	yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
	zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
	
	lightLev=(TextView)findViewById(R.id.lightLev);
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
	
	magLev=(TextView)findViewById(R.id.magLev);
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_UI);
	Log.d(TAG, "0");
	
	soundLev=(TextView)findViewById(R.id.soundLev);
	startRecording();*/
    
    
    camerainit();
    //sensorinit();
    
}

final PictureCallback mPicture = new PictureCallback() {

    public void onPictureTaken(byte[] data, Camera camera) {
    	byte[] tempPic = new byte[data.length];
    	tempPic = data;
    	Log.d(TAG, "takingpic1");
    	//picLock.open();
    	Log.d(TAG, "takingpic2");
    	if(initpic==true){//new initial picture
    		Log.d(TAG, "basepictomem1");
    		//basePicB = new byte[data.length];
    		Log.d(TAG, "basepictomem2");
    		//basePicB = data;
    		Log.d(TAG, "basepictomem3");
    	}
    	else{//check if image is different from base picture
    		checkImage(basePicB, tempPic);
    	}
        if((initpic==true)||(diffpic==true)){//picture is interesting so save it
	    	File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	
	        if (pictureFile == null){
	            return;
	        }
	
	        try {
	            FileOutputStream fos = new FileOutputStream(pictureFile);
	            
	            
	            fos.write(tempPic);
	            fos.close();
	            MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
	            Log.d(TAG, "writepic");
	            //picLock.open();
	        } catch (FileNotFoundException e) {
	
	        } catch (IOException e) {
	
	        }
    	}
        //holdpic=false;
        if(secpic==true){
    		swapcam();
    		//mCamera.takePicture(null, null, mPicture);
    	
    	}else{
    		if(sensorsInitilized==false){
				sensorsInitilized=true;
				sensorinit();
			}
    		if((senspause==true)&&(sensorsInitilized==true)){
    			senspause = false;
    		}
    	}

      }
};


private void camerainit(){//must finish before starting sensorinit
    Button captureButton = (Button) findViewById(R.id.button_capture);
    
    if(Camera.getNumberOfCameras()==2){
    	multicam=true;
    	Log.d(TAG, "2cams");
    }
    // Create an instance of Camera
    mCamera = getCameraInstance(currcam);
    // Create our Preview view and set it as the content of our activity.
    //mPreview = new CameraPreview(this, mCamera);
    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    //preview.addView(mPreview);
    preview = (FrameLayout) findViewById(R.id.camera_preview);
    setcamprev();
    
    //mPreview = new CameraPreview(this, mCamera);
    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    //preview.addView(mPreview);
    
    // Add a listener to the Capture button
	captureButton.setOnClickListener(

			new View.OnClickListener() {

				public void onClick(View v) {
					// get an image from the camera   
					Log.d(TAG, "buttonpress");
					initpic=true;
					takepic();
					/*if(sensorsInitilized==false){
						sensorsInitilized=true;
						sensorinit();
					}*/
					//mCamera.takePicture(null, null, mPicture);
					
					/*if(multicam==true){
						swapcam();
						mCamera.takePicture(null, null, mPicture);
					}*/
				}
			}
	);
}

private void sensorinit(){
    // Get the SensorManager 
    mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
    msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
    String SensListName = new String("");
    String SensListType = new String("");
    Sensor tmp;
    int i;
    for (i=0;i<msensorList.size();i++){
    	tmp = msensorList.get(i);
    	SensListName = " "+SensListName+tmp.getName(); // Add the sensor name to the string of sensors available
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(tmp.getType()),SensorManager.SENSOR_DELAY_UI);
    }
    
    
    xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
	yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
	zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
	
	lightLev=(TextView)findViewById(R.id.lightLev);
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
	
	magLev=(TextView)findViewById(R.id.magLev);
	//mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_UI);
	Log.d(TAG, "0");
	
	soundLev=(TextView)findViewById(R.id.soundLev);
	startRecording();
}



/** A safe way to get an instance of the Camera object. */
public static Camera getCameraInstance(int cn){
    Camera c = null;
    try {
        c = Camera.open(cn); // attempt to get a Camera instance requires sdk 9
    }
    catch (Exception e){
        // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
}

private void setcamprev(){//call this when switching cameras
    mPreview = new CameraPreview(this, mCamera, secpic, mPicture);
    //FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(mPreview);
    //mHolder = mPreview.getHolder();
    //mHolder.addCallback(this);
    //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    
    /*try {
		mCamera.setPreviewDisplay(mHolder);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}*/
    
    try {
		mCamera.setPreviewDisplay(mPreview.getHolder());
		Log.d(TAG, "swapcam3.1");
	} catch (IOException e) {
		Log.d(TAG, "swapcam3.2");
		e.printStackTrace();
	}
	mCamera.startPreview();
    Log.d(TAG, "swapcam3.5");
    //SystemClock.sleep(1000);
    if(secpic==true){
        /*try {
			mCamera.wait();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}*/
    	secpic = false;
    	Log.d(TAG, "swapcam5");
    	//mCamera.takePicture(null, null, mPicture);
    	Log.d(TAG, "swapcam6");
    }
}

private void swapcam(){
	Log.d(TAG, "picloc1");
	//picLock.block(5000);
	Log.d(TAG, "picloc2");
	if(currcam==0){
		currcam=1;
	}else{
		currcam=0;
	}
	Log.d(TAG, "swapcam1");
	mCamera.release();
	preview.removeView(mPreview);
	Log.d(TAG, "swapcam2");
	mCamera = getCameraInstance(currcam);
	Log.d(TAG, "swapcam3");
	setcamprev();
	//Log.d(TAG, "swapcam4");

}

private void takepic(){//call this when you need to take a picture
	//holdpic=true;
	mCamera.takePicture(null, null, mPicture);
	//while(holdpic==true){
	//}
	

	if(multicam==true){
		//swapcam();
		//mCamera.takePicture(null, null, mPicture);
		secpic = true;
	}
}


@Override
protected void onPause() {
    super.onPause();
    releaseCamera();              // release the camera immediately on pause event
    stopRecording();
}



private void releaseCamera(){
    if (mCamera != null){
        mCamera.release();        // release the camera for other applications
        mCamera = null;
    }
}



/** Create a File for saving an image or video */
private  File getOutputMediaFile(int type){
    // To be safe, you should check that the SDCard is mounted
    // using Environment.getExternalStorageState() before doing this.

    File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");


    // This location works best if you want the created images to be shared
    // between applications and persist after your app has been uninstalled.

    // Create the storage directory if it does not exist
    if (! mediaStorageDir.exists()){
        if (! mediaStorageDir.mkdirs()){
            return null;
        }
    }

    // Create a media file name
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
    File mediaFile;
    String tmpSide = "REAR_";
    if (currcam==1){
    	tmpSide = "FRONT_";
    }
    if (type == MEDIA_TYPE_IMAGE){
    	//check if this needs to be initial image
    	if(initpic==true){
    		initpic=false;
    		mediaFile = new File(mediaStorageDir.getPath() + File.separator + tmpSide + "INIT_IMG_"+ timeStamp + ".jpg");
    		Log.d(TAG, "initpic");
    		//this.notify();
    	}
    	else{
    		mediaFile = new File(mediaStorageDir.getPath() + File.separator + tmpSide + "IMG_"+ timeStamp + ".jpg");
    	}
    }
    else {
        return null;
    }
    
    
    return mediaFile;
}




@Override
public void onAccuracyChanged(Sensor arg0, int arg1) {
	// TODO Auto-generated method stub
	
}

public void checkImage(byte[] base, byte[] current){
	// TODO algo to compare byte arrays and set diffpic to false if they are the same
	return;
}


//shift array
public void shifta(float[] a){
	for (int i = 0; i < a.length - 1; i++) {
		a[i+1] = a[i];
	}
}

//central difference formula first derivative, order 4
//5 element array 
public float CDF1O4(float[] a){
	float f;
	f = ((-a[4])+(8*a[3])-(8*a[1])+a[0])*12;
	
	return(f);
}


public void onSensorChanged(SensorEvent event) {
	if(senspause == false){
		if((event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)&&(accele==true)){
			//shift old values
			shifta(xacel);
			shifta(yacel);
			shifta(zacel);
			//assign values
			xacel[0]=event.values[0];
			yacel[0]=event.values[1];
			zacel[0]=event.values[2];
			//find derivative
			float dx = CDF1O4(xacel);
			float dy = CDF1O4(yacel);
			float dz = CDF1O4(zacel);
			//change display
			xCoor.setText("X: "+dx);
			yCoor.setText("Y: "+dy);
			zCoor.setText("Z: "+dz);
			
			
			if((dx > vibrationfactor)||(dy > vibrationfactor)||(dz > vibrationfactor)){
				Log.d(TAG, "vibration1");
				sensdet = true;
				senspause = true;
				takepic();
			}
			
		}
		if((event.sensor.getType()==Sensor.TYPE_LIGHT)&&(lighte==true)){
			
			shifta(llevel);
			llevel[0]=event.values[0];
			float dl = CDF1O4(llevel);
			lightLev.setText("L: "+dl);
			
			//if(dl > lightfactor){
			if(llevel[0] > lightfactor){
				Log.d(TAG, "light1");
				sensdet = true;
				senspause = true;
				takepic();
			}
			
		}
		if((event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD)&&(magnete==true)){
			
			shifta(mlevel);
			mlevel[0]=event.values[0];
			float dm = CDF1O4(mlevel);
			magLev.setText("M: "+dm);
			
			if(dm > magnetfactor){
			//if(mlevel[0] > magnetfactor){
				Log.d(TAG, "ferrous2");
				sensdet = true;
				senspause = true;
				takepic();
			}
			
		}
		if(event.sensor.getType()==Sensor.TYPE_AMBIENT_TEMPERATURE){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_GRAVITY){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_GYROSCOPE){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_LINEAR_ACCELERATION){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_ORIENTATION){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_PRESSURE){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_PROXIMITY){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_RELATIVE_HUMIDITY){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR){
			
		}
		if(event.sensor.getType()==Sensor.TYPE_TEMPERATURE){
			
		}
	
		
		//is sound enabled
		if(sounde==true){
			shifta(spresure);
			spresure[0]=mRecorder.getMaxAmplitude();
			float ds = CDF1O4(spresure);
			
			soundLev.setText("S: "+ds);
			
			if(ds > soundfactor){
				Log.d(TAG, "noise2");
				sensdet = true;
				senspause = true;
				takepic();
			}
		}
	}
}


private void startRecording() {
    mRecorder = new MediaRecorder();
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    mRecorder.setOutputFile("/sdcard/Pictures/audiorecordtest.3gp");
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

    try {
        mRecorder.prepare();
    } catch (IOException e) {
        Log.e(TAG, "prepare() failed");
    }

    mRecorder.start();
}

private void stopRecording() {
    mRecorder.stop();
    mRecorder.release();
    mRecorder = null;
}


}