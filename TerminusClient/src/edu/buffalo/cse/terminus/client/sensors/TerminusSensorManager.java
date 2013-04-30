package edu.buffalo.cse.terminus.client.sensors;

import java.util.ArrayList;
import java.util.List;

import edu.buffalo.cse.terminus.client.TerminusController;
import edu.buffalo.cse.terminus.client.TerminusSettings;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class TerminusSensorManager implements SensorEventListener 
{	
	/*
	 * This is just for UI updates/debugging
	 */
	private SensorEventListener sensorCallback;
	
	private SensorManager sensorManager;
	private TerminusSettings settings;
	private TerminusController controller;
	
	private SensorAlgo accelerometerAlgo;
	private SensorAlgo magnetometerAlgo;
	private SensorAlgo lightAlgo;
	private static SoundAlgo soundAlgo;
	
	//used for sound sampling
	public static Handler handler = new Handler();
	
	public static Runnable sampletask = new Runnable() {
		public void run() {
			soundAlgo.newSoundSample();
			handler.postDelayed(sampletask, SoundAlgo.samplerate);
		}
	};


	public TerminusSensorManager(TerminusSettings settings, SensorEventListener listener, 
			TerminusController controller, Activity activity)
	{
		this.settings = settings;
		this.controller = controller;
		this.sensorCallback = listener;
		
		sensorManager = (SensorManager) activity.getSystemService(android.content.Context.SENSOR_SERVICE);
		createSensorAlgos();
		createSoundAlgos();
	}
	
	private void createSensorAlgos()
	{
		for (int i = 0; i < settings.sensorList.size(); i++)
		{
			switch (settings.sensorList.get(i))
			{
			case Sensor.TYPE_ACCELEROMETER:
				accelerometerAlgo = new AccelCDFAlgo(controller); 
				break;
				
			case Sensor.TYPE_LIGHT:
				lightAlgo = new LightCDFAlgo(controller);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				magnetometerAlgo = new MagCDFAlgo(controller);
				break;
				
			default:
				break;
			}
		}
	}
	
	public void createSoundAlgos(){
		if(settings.useSound==true){
			soundAlgo = new SoundAlgo();
			soundAlgo.setController(controller);
		}
	}
	
	private void startSensorAlgos()
	{
		if (accelerometerAlgo != null)
			accelerometerAlgo.startAlgo();
		
		if (magnetometerAlgo != null)
			magnetometerAlgo.startAlgo();
		
		if (lightAlgo != null)
			lightAlgo.startAlgo();
	}
	
	public void startSoundAlgos(){
		if((soundAlgo != null)&&(settings.useSound==true))
			soundAlgo.startRecording();
	}
	
	private void stopSensorAlgos()
	{
		if (accelerometerAlgo != null)
			accelerometerAlgo.stopAlgo();
		
		if (magnetometerAlgo != null)
			magnetometerAlgo.stopAlgo();
		
		if (lightAlgo != null)
			lightAlgo.stopAlgo();
	}
	
	public void clearSensorsPriority()
	{
		if (accelerometerAlgo != null)
			accelerometerAlgo.clearPriority();
		
		if (magnetometerAlgo != null)
			magnetometerAlgo.clearPriority();
		
		if (lightAlgo != null)
			lightAlgo.clearPriority();
		
		if(soundAlgo != null)
			soundAlgo.clearPriority();
	}
	
	public void stopSoundAlgos()
	{
		if(soundAlgo != null)
			soundAlgo.stopRecording();
	}
	
	public void start()
	{
		/*
		 * Register all sensors that were selected
		 */
		for (int i = 0; i < settings.sensorList.size(); i++)
		{
			int type = settings.sensorList.get(i);
			sensorManager.registerListener(this, sensorManager.getDefaultSensor(type),SensorManager.SENSOR_DELAY_UI);	
	    }
		startSensorAlgos();
		startSoundAlgos();
	}
	
	public void stop()
	{
		/*
		 * Unregister all sensors that were selected in start()
		 */
		sensorManager.unregisterListener(this);
		stopSensorAlgos();
		stopSoundAlgos();
		handler.removeCallbacks(sampletask);
	}
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		//Nothing
	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
        /*
		boolean send = false;
		for(int i=0;i<controller.PriorityLevels.length;i++){
			if(controller.TotPriority<=(settings.PriorityLimit*i)){
				if(controller.PriorityLevels[i]==true){
					controller.PriorityLevels[i]=false;
					send=true;
				}
			}
		}
		if(send==true){
			//TODO send message since priority level changed
		}
		
		if(controller.TotPriority > 0){
			controller.TotPriority-=1;
			if(controller.TotPriority == 0){
				LightCDFAlgo.FirstLitPri = false;
				MagCDFAlgo.FirstMagPri = false;
				AccelCDFAlgo.FirstAclPri = false;
				SoundAlgo.FirstSndPri = false;
			}
		}
        */
        
		switch (event.sensor.getType())
		{
		case Sensor.TYPE_ACCELEROMETER:
			if (accelerometerAlgo != null)
			{
				accelerometerAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		case Sensor.TYPE_MAGNETIC_FIELD:
			if (magnetometerAlgo != null)
			{
				magnetometerAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		case Sensor.TYPE_LIGHT:
			if (lightAlgo != null)
			{
				lightAlgo.onSensorChanged(event);
				
				if (sensorCallback != null)
					sensorCallback.onSensorChanged(event);
			}
			
			break;
			
		default:
			
			return;
		}
	}
	
	public static List<Integer> getSupportedSensors(Activity context)
	{
		/*
		 * Both of the following need to be true in order to use
		 * a specific sensor:
		 * 	- We need to support it
		 *  - The device needs to support it 
		 */
		
		SensorManager sm = (SensorManager) context.getSystemService(Activity.SENSOR_SERVICE);
		List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ALL);
		ArrayList<Integer> supported = new ArrayList<Integer>();
		
		
		
		for (Sensor s : sensors)
		{
			switch (s.getType())
			{
			case Sensor.TYPE_ACCELEROMETER:
				supported.add(Sensor.TYPE_ACCELEROMETER);
				break;
				
			case Sensor.TYPE_LIGHT:
				supported.add(Sensor.TYPE_LIGHT);
				break;
				
			case Sensor.TYPE_MAGNETIC_FIELD:
				supported.add(Sensor.TYPE_MAGNETIC_FIELD);
				break;
				
			default:
				break;
			}
		}
		
		return supported;
	}
	
	public static String getSensorDescription(int type)
	{
		String out = "";
		
		switch (type)
		{
		case Sensor.TYPE_ACCELEROMETER:
			out = "Accelerometer";
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			out = "Magnetometer";
			break;
		case Sensor.TYPE_LIGHT:
			out = "Ambient Light";
			break;
		default:
			out = "";
			break;
		}
		
		return out;
	}
	
	public static String getSensorSensitivity(int type)
	{
		String out = "";
		
		switch (type)
		{
		case Sensor.TYPE_ACCELEROMETER:
			out = String.valueOf(AccelCDFAlgo.VIBRATION_FACTOR);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			out = String.valueOf(MagCDFAlgo.MAGNET_FACTOR);
			break;
		case Sensor.TYPE_LIGHT:
			out = String.valueOf(LightCDFAlgo.LIGHT_FACTOR);
			break;
		default:
			out = "";
			break;
		}
		
		return out;
	}
	
	public static String getSoundSensitivity(){
		return String.valueOf(SoundAlgo.SOUND_FACTOR);
	}
	
	public static String getSoundInterval(){
		return String.valueOf(SoundAlgo.samplerate);
	}
	
	public static void setSensorSensitivity(int type, int value)
	{	
		switch (type)
		{
		case Sensor.TYPE_ACCELEROMETER:
			AccelCDFAlgo.VIBRATION_FACTOR = value;
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			MagCDFAlgo.MAGNET_FACTOR = value;
			break;
		case Sensor.TYPE_LIGHT:
			LightCDFAlgo.LIGHT_FACTOR = value;
			break;
		default:
			
			break;
		}
	}
	
	public static void setSoundSensitivity(int value){
		SoundAlgo.SOUND_FACTOR = value;
	}
	
	public static void setSoundInterval(int value){
		SoundAlgo.samplerate = value;
	}
}
