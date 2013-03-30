package edu.buffalo.cse.terminus.client;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/*
 * This fragment represents the bottom status bar of the main activity.
 */
public class StatusFragment extends Fragment
{
	private TextView tvNetwork;
	private TextView tvSensors;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) 
	{
	    View view = inflater.inflate(R.layout.fragment_terminus_client_status,
	        container, false);
	    
	    /*
	     * This needs to be done here so it is available to the parent activity
	     * in its onResume() method
	     */
	    tvNetwork = (TextView) view.findViewById(R.id.tvNetwork);
	    tvSensors = (TextView) view.findViewById(R.id.tvSensors);
	    
	    return view;
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
	
	/*
	 * Try to set the textview's text/color
	 * 
	 * If this is called before the view has been created,
	 * nothing happens
	 */
	public void setNetworkText(String text)
	{
		if (tvNetwork != null)
		{
			tvNetwork.setText(text);
		}
	}
	
	public void setNetworkText(String text, int color)
	{
		if (tvNetwork != null)
		{
			tvNetwork.setText(text);
			tvNetwork.setTextColor(color);
		}
	}

	public void setSensorText(String text)
	{
		if (tvSensors != null)
		{
			tvSensors.setText(text);
		}
	}
	
	public void setSensorText(String text, int color)
	{
		if (tvSensors != null)
		{
			tvSensors.setText(text);
			tvSensors.setTextColor(color);
		}
	}
}
