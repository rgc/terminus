package edu.buffalo.cse.terminus.client.sensors;

import java.io.IOException;

import android.hardware.Sensor;
import android.media.MediaRecorder;
import android.util.Log;

import edu.buffalo.cse.terminus.client.TerminusController;

public class SoundAlgo {
	
	private MediaRecorder Recorder = null;
	public float[] slevel;
	public static float SOUND_FACTOR=20000;
	public static boolean FirstSndPri = false;
	public static int samplerate = 1;
	public static double sound;
	
	//private long[] time;
	private double t;
	
	private TerminusController controller;
	
	public void setController(TerminusController c)
	{
		this.controller = c;
	}
	
	public void newSoundSample(){
		if (slevel == null)
			startRecording();
		
		CDFFunctions.shifta(slevel);
		//CDFFunctions.shifta(time);
		
		slevel[0]=Recorder.getMaxAmplitude();
		//time[0] = event.timestamp;
		
		//t = CDFFunctions.avgt(time);
		t=samplerate;
		t/=1000;
		//t=0.001;
		//sound = t;
		float ds = CDFFunctions.CDF1O8(slevel, t);
		//sound = slevel[0];
		
		
		if(ds > SOUND_FACTOR)
		{
			sound = ds;
			int SndPri = 0;
			if(FirstSndPri == false){
				FirstSndPri = true;
				SndPri+=30;
			}
			SndPri+=(ds/50000);
			this.controller.soundEventSensed(SndPri);
		}
	}
	
	public void clearPriority()
	{
		FirstSndPri = false;	
	}
	
	public void startRecording() {
	    Recorder = new MediaRecorder();
	    Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    Recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    Recorder.setOutputFile("/dev/null");
	    Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	    try {
	        Recorder.prepare();
	    } catch (IOException e) {
	        
	    }
	    slevel = new float[9];
	    //time = new long[6];
	    Recorder.start();
	    TerminusSensorManager.handler.postDelayed(TerminusSensorManager.sampletask, samplerate);
	}
	
	
	public void stopRecording() {
	    if(Recorder != null){
			Recorder.stop();
		    Recorder.release();
		    Recorder = null;
		    slevel = null;
	    }
	}
}
