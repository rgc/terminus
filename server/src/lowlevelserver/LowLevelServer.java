package lowlevelserver;

import java.net.*;
import java.io.*;

import eventserver.ITerminusMsgCallback;

import shared.ITerminusServer;
import shared.ServerCloseException;

public class LowLevelServer implements ITerminusServer
{
	private int acceptPort;
	private ServerSocket server = null;
	private ITerminusMsgCallback callback;

	public LowLevelServer(ITerminusMsgCallback c, int port)
	{
		this.callback = c;
		this.acceptPort = port;
	}

	@Override
	public void start() throws ServerCloseException
	{
		if (this.server != null && !this.server.isClosed())
		{
			stop();
		}

		try
		{
			server = new ServerSocket(this.acceptPort);
		}
		catch (IOException e)
		{
			throw new ServerCloseException(e.getMessage());
		}

		/* Connection Accept Thread */
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					Socket s = null;

					try
					{
						s = server.accept();
						LowLevelConnection llc = new LowLevelConnection(s,
								LowLevelServer.this.callback);
						llc.setAddress(s.getInetAddress());
						llc.setPort(s.getPort());
					}
					catch (IOException e)
					{
						// TODO: This should propegate back to UI, or just be
						// ignored
						return;
					}
				}
			}
		}).start();
	}

	@Override
	public void stop()
	{
		try
		{
			server.close();
		}
		catch (IOException e)
		{
			// Just supress this, we're shutting down anyways.
		}
	}
}
