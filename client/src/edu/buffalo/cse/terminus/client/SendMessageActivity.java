package edu.buffalo.cse.terminus.client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
	
	@Override
	protected void onPause() 
	{
		super.onPause();
		terminusConnection.disconnect();
	}
	
	@Override
	protected void onStop() 
	{
		super.onStop();
		terminusConnection.disconnect();
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

	//////////////////////   Network Callbacks   //////////////////////
	
	@Override
	public void onConnectionComplete() 
	{
		hourglass.dismiss();
	}

	@Override
	public void onConnectionError(IOException e) 
	{
		connectionError("Error: " + e.getMessage());
	}

	@Override
	public void onConnectionError(SocketTimeoutException e) 
	{
		connectionError("Error: Connection Timed Out");
	}

	@Override
	public void onConnectionError(UnknownHostException e) 
	{
		connectionError("Error: Unknown Host");
	}

	@Override
	public void onDisconnectComplete() 
	{	
	}

	private void connectionError(String message)
	{
		hourglass.dismiss();
		AlertDialog ad = Dialogs.getOKOnlyAlert(message, this);
		ad.setOnDismissListener(new CloseActivityDialogListener(this));
		ad.show();
	}
	
	@Override
	public void onConnectionDropped() 
	{	
	}

	@Override
	public void onMessageReceived(TerminusMessage msg) 
	{
		if (msg.getMessageType() == TerminusMessage.MSG_TEST)
		{
			TestMessage tm = (TestMessage) msg;
			sentMessages.add(tm.message);
			lvMessages.invalidateViews();
		}	
	}

	@Override
	public void onSendComplete() 
	{
	}

	@Override
	public void onMessageFailed(TerminusMessage msg) 
	{	
		Dialogs.getOKOnlyAlert("An error occurred while sending the message", this);
	}
}
