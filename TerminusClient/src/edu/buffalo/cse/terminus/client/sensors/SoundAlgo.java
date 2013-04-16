package edu.buffalo.cse.terminus.client.sensors;

import java.io.IOException;

import android.hardware.Sensor;
import android.media.MediaRecorder;
import android.util.Log;

import edu.buffalo.cse.terminus.client.TerminusController;

public class SoundAlgo {
	
	private MediaRecorder Recorder = null;
	public float[] slevel;
	public static int SOUND_FACTOR=100000;
	public static boolean FirstSndPri = false;
	
	private long[] time;
	private float t;
	
	TerminusController controller;
	
	public void setController(TerminusController c)
	{
		this.controller = c;
	}
	
	public void newSoundSample(){
		if (slevel == null)
			startRecording();
		
		CDFFunctions.shifta(slevel);
		slevel[0]=Recorder.getMaxAmplitude();
		float ds = CDFFunctions.CDF1O4(slevel, t);
		
		if(ds > SOUND_FACTOR)
		{
			int SndPri = 0;
			if(FirstSndPri == false){
				FirstSndPri = true;
				SndPri+=30;
			}
			SndPri+=(ds/100000);
			this.controller.soundEventSensed(SndPri);
		}
	}
	
	private void startRecording() {
	    Recorder = new MediaRecorder();
	    Recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
	    Recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
	    Recorder.setOutputFile("/sdcard/Pictures/audiorecordtest.3gp");
	    Recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

	    try {
	        Recorder.prepare();
	    } catch (IOException e) {
	        
	    }
	    slevel = new float[5];
	    Recorder.start();
	}
	
	
	private void stopRecording() {
	    Recorder.stop();
	    Recorder.release();
	    Recorder = null;
	    slevel = null;
	}
}
