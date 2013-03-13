package lowlevelserver;

import java.io.*;
import java.net.*;

import eventserver.IEventCallbacks;
import edu.buffalo.cse.terminus.messages.*;
import edu.buffalo.cse.terminus.lowlevel.*;

import shared.ATerminusConnection;

public class LowLevelConnection extends ATerminusConnection
{
	private Socket socket;
	private DataOutputStream out;
	private IEventCallbacks eventClient;

	public LowLevelConnection(Socket s, IEventCallbacks client)
	{
		super();
		this.socket = s;
		this.eventClient = client;

		try
		{
			this.out = new DataOutputStream(s.getOutputStream());
			startReceiveThread();
		}
		catch (IOException e)
		{
			return;
		}
	}

	private void startReceiveThread()
	{
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				LowLevelReader reader = null;
				
				try
				{
					reader = new LowLevelReader(LowLevelConnection.this.socket);
				}
				catch (IOException e1)
				{
					LowLevelConnection.this.shutdown();
					LowLevelConnection.this.eventClient
							.connectionDropped(LowLevelConnection.this);
					return;
				}
				
				while (true)
				{
					try
					{
						//TerminusMessage message = LowLevelShared.getNextMessage(socket);
						TerminusMessage message = reader.getNextMessage();
						eventClient.messageReceived(message);
					}
					catch (IOException e)
					{
						LowLevelConnection.this.shutdown();
						LowLevelConnection.this.eventClient
								.connectionDropped(LowLevelConnection.this);
						return;
					}
				}
			}
		}).start();
	}

	@Override
	public void sendMessage(TerminusMessage m)
	{
		try
		{
			ILowLevelMessage llm = (ILowLevelMessage) m;
			byte[] msg = llm.getBytes();
			if (msg != null)
			{
				out.write(msg);
			}
		}
		catch (IOException e)
		{
			// TODO: Handle error
		}
	}

	@Override
	public void shutdown()
	{
		try
		{
			this.socket.close();
		}
		catch (IOException e)
		{
			// Ignore
		}
	}
}
