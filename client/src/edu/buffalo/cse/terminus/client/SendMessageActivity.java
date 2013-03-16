package edu.buffalo.cse.terminus.client;

import java.util.ArrayList;

import edu.buffalo.cse.terminus.client.R;

import edu.buffalo.cse.terminus.client.network.*;
import edu.buffalo.cse.terminus.messages.*;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SendMessageActivity extends Activity implements INetworkCallbacks
{
	private ProgressDialog hourglass;
	private EditText txtMessage;
	private ListView lvMessages;
	private TerminusConnection terminusConnection;
	private ArrayList<String> sentMessages;
	private ListAdapter messageViewAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		terminusConnection = new TerminusConnection(new UIEventBridge(this, this), this);
		
		setActivityControls();
		
		hourglass = Dialogs.getSpinningProgress("Connecting...", this);
		hourglass.show();
		
		String ip = this.getIntent().getStringExtra("ipAddress");
		int port = this.getIntent().getIntExtra("portNumber", 0);
		terminusConnection.connect(ip, port);
	}
	
	private void setActivityControls()
	{
		txtMessage = (EditText) this.findViewById(R.id.edit_message);
		lvMessages = (ListView) this.findViewById(R.id.listview_messages);
		
		sentMessages = new ArrayList<String>();
		messageViewAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sentMessages);
		lvMessages.setAdapter(messageViewAdapter);
		lvMessages.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void sendMessage(View view)
	{	
		terminusConnection.sendTestMessage(txtMessage.getText().toString());
	}

	public void sendEvent(View view)
	{	
		terminusConnection.sendEventMessage();
	}
	
	//////////////////////   Connection Manager Callbacks   //////////////////////
	
	@Override
	public void connectionFinished(final ConnectionResult result)
	{
		hourglass.dismiss();
		String errText = "";
		
		switch (result.status)
		{
		case Success:
			return;
			
		case IOError:
			errText = "Error: " + result.exception.getMessage();
			break;
			
		case TimeOut:
			errText = "Error: Connection Timed Out";
			break;
			
		case UnknownHost:
			errText = "Error: Unknown Host";
			break;
			
		default:
			errText = "*** UNKNOWN RESULT CODE ***";
			break;
		}
		
		AlertDialog ad = Dialogs.getOKOnlyAlert(errText, this);
		ad.setOnDismissListener(new CloseActivityDialogListener(this));
		ad.show();
	}
	
	private class CloseActivityDialogListener implements OnDismissListener
	{
		Activity activity;
		
		public CloseActivityDialogListener(Activity a)
		{
			activity = a;
		}
		
		@Override
		public void onDismiss(DialogInterface dialog)
		{
			activity.finish();
		}
	}
	
	@Override
	public void messageFinished(final ConnectionResult result)
	{
		switch (result.status)
		{
		case IOError:
			Dialogs.getOKOnlyAlert("An error occurred while sending the message", this);
			break;
		default:
			break;
		}
	}
	
	@Override
	public void disconnectFinished(ConnectionResult result)
	{

	}

	@Override
	public void messageReceived(final TerminusMessage msg)
	{
		if (msg.getMessageType() == TerminusMessage.MSG_TEST)
		{
			TestMessage tm = (TestMessage) msg;
			sentMessages.add(tm.message);
			lvMessages.invalidateViews();
		}
	}
}
