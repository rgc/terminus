package eventserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lowlevelserver.LowLevelImageThread;
import shared.ITerminusServer;
import shared.ServerCloseException;

public class ImageServer implements ITerminusServer
{
	public static final int IMAGE_PORT = 34412;
	
	private ITerminusMsgCallback msgCallback;
	private ServerSocket server = null;
	
	public ImageServer(ITerminusMsgCallback mc)
	{
		this.msgCallback = mc;
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
			server = new ServerSocket(IMAGE_PORT);
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
						new Thread(new LowLevelImageThread(s, ImageServer.this.msgCallback)).start();
					}
					catch (IOException e)
					{
						// TODO: This should propagate back to UI, or just be ignored
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
			// Just suppress this, we're shutting down anyways.
		}
	}		
}
