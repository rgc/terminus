package edu.buffalo.cse.terminus.terminusnotifier;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;

/*
 * Static function to create common dialogs
 */
public class Dialogs
{
	public static AlertDialog getOKOnlyAlert(String message, Context c)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		
		builder
			.setTitle(message)
			.setCancelable(false)
			.setPositiveButton("OK", null);
		return builder.create();
	}
	
	public static ProgressDialog getSpinningProgress(String message, Context c)
	{
		ProgressDialog hourglass = new ProgressDialog(c);
		hourglass.setMessage(message);
		hourglass.setIndeterminate(true);
		hourglass.setCancelable(false);
		return hourglass;
	}
}
