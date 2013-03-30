package edu.buffalo.cse.terminus.client;

import java.util.ArrayList;

import edu.buffalo.cse.terminus.client.sensors.TerminusSensorManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.LinearLayout;

public class GetSensorsActivity extends Activity
{
	public static final String SENSORS_PARAM = "SensorList";
	
	private class SensorCheckBox extends CheckBox
	{
		int sensorType;
		
		public SensorCheckBox(Context context) 
		{
			super(context);
		}
	}
	
	private ArrayList<SensorCheckBox> sensorCheckBoxes;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_get_sensors);
		
		addCheckboxes();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
	}
	
	private void addCheckboxes()
	{
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.sensorLayout);
		
		sensorCheckBoxes = new ArrayList<SensorCheckBox>();
		
		int[] sensors = TerminusSensorManager.getSupportedSensors(this);
		
		for (int i = 0; i < sensors.length; i++)
		{
			SensorCheckBox cb = new SensorCheckBox(this);
			sensorCheckBoxes.add(cb);
			cb.sensorType = sensors[i];
			cb.setText(TerminusSensorManager.getSensorDescription(cb.sensorType));
			cb.setChecked(true);
			layout.addView(cb);
		}
	}
	
	public void okClicked(View view)
	{
		int count = 0;
		for (int i = 0 ; i < sensorCheckBoxes.size(); i++)
		{
			if (sensorCheckBoxes.get(i).isChecked())
				count++;
		}
		
		if (count == 0)
		{
			Dialogs.getOKOnlyAlert("At least one sensor must be selected", this).show();
			return;
		}
		
		int[] res = new int[count];
		count = 0;
		for (int i = 0 ; i < sensorCheckBoxes.size(); i++)
		{
			if (sensorCheckBoxes.get(i).isChecked())
				res[count++] = sensorCheckBoxes.get(i).sensorType;
		}
		
		Intent intent = new Intent();
		intent.putExtra(SENSORS_PARAM, res);
		setResult(RESULT_OK, intent);
		finish();
	}
	
	public void cancelClicked(View view)
	{
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}
}
