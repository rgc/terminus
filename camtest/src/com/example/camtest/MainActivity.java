package com.example.camtest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener{

private Camera mCamera;
private CameraPreview mPreview;
public static final int MEDIA_TYPE_IMAGE = 1;
private SensorManager mSensorManager;
public boolean sensdet = false;
public float[] xacel = new float[5];
public float[] yacel = new float[5];
public float[] zacel = new float[5];
public float[] spresure = new float[5];
public float[] llevel = new float[5];
public float[] mlevel = new float[5];
//how sensitive do you want detection
public static final int vibrationfactor=20,soundfactor=100000,lightfactor=20,magnetfactor=20;
TextView xCoor,yCoor,zCoor,soundLev,lightLev,magLev;
private static final String TAG = "MyCameraMain";
private MediaRecorder mRecorder = null;
//enable sensors
public boolean sounde=true, accele=true;


@Override
public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Button captureButton = (Button) findViewById(R.id.button_capture);
    System.out.println("Starting!");

    // Create an instance of Camera
    mCamera = getCameraInstance();
    // Create our Preview view and set it as the content of our activity.
    mPreview = new CameraPreview(this, mCamera);
    FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
    preview.addView(mPreview);

/*    final PictureCallback mPicture = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

            if (pictureFile == null){
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
            } catch (FileNotFoundException e) {

            } catch (IOException e) {

            }
          }
        };*/





     // Add a listener to the Capture button
        captureButton.setOnClickListener(

            new View.OnClickListener() {

                public void onClick(View v) {
                    // get an image from the camera   

                    System.out.println("Photo Taking!");
                    mCamera.takePicture(null, null, mPicture);



                }
            }
        );

        // Get the SensorManager 
        mSensorManager= (SensorManager) getSystemService(SENSOR_SERVICE);
        xCoor=(TextView)findViewById(R.id.xcoor); // create X axis object
		yCoor=(TextView)findViewById(R.id.ycoor); // create Y axis object
		zCoor=(TextView)findViewById(R.id.zcoor); // create Z axis object
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),SensorManager.SENSOR_DELAY_UI);
		
		lightLev=(TextView)findViewById(R.id.lightLev);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT),SensorManager.SENSOR_DELAY_UI);
		
		magLev=(TextView)findViewById(R.id.magLev);
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_UI);
		Log.d(TAG, "0");
		
		soundLev=(TextView)findViewById(R.id.soundLev);
		startRecording();

}

final PictureCallback mPicture = new PictureCallback() {

    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);

        if (pictureFile == null){
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), pictureFile.getAbsolutePath(), pictureFile.getName(), pictureFile.getName());
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }
      }
};


/** A safe way to get an instance of the Camera object. */
public static Camera getCameraInstance(){
    Camera c = null;
    try {
        c = Camera.open(); // attempt to get a Camera instance
    }
    catch (Exception e){
        // Camera is not available (in use or does not exist)
    }
    return c; // returns null if camera is unavailable
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
    String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
    File mediaFile;
    if (type == MEDIA_TYPE_IMAGE){
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg");
    } else {
        return null;
    }

    return mediaFile;
}




@Override
public void onAccuracyChanged(Sensor arg0, int arg1) {
	// TODO Auto-generated method stub
	
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
	if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
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
			mCamera.takePicture(null, null, mPicture);
		}
		
	}
	if(event.sensor.getType()==Sensor.TYPE_LIGHT){
		
		shifta(llevel);
		llevel[0]=event.values[0];
		float dl = CDF1O4(llevel);
		lightLev.setText("L: "+dl);
		
		//if(dl > lightfactor){
		if(llevel[0] > lightfactor){
			Log.d(TAG, "light1");
			sensdet = true;
			//mCamera.takePicture(null, null, mPicture);
		}
		
	}
	if(event.sensor.getType()==Sensor.TYPE_MAGNETIC_FIELD){
		
		shifta(mlevel);
		mlevel[0]=event.values[0];
		float dm = CDF1O4(mlevel);
		magLev.setText("M: "+dm);
		
		if(dm > magnetfactor){
		//if(mlevel[0] > magnetfactor){
			Log.d(TAG, "ferrous2");
			sensdet = true;
			//mCamera.takePicture(null, null, mPicture);
		}
		
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
			//mCamera.takePicture(null, null, mPicture);
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