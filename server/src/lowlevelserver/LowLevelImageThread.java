package lowlevelserver;

import java.io.IOException;
import java.net.Socket;

import edu.buffalo.cse.terminus.lowlevel.LowLevelReader;
import edu.buffalo.cse.terminus.messages.TerminusMessage;
import eventserver.ITerminusMsgCallback;

public class LowLevelImageThread implements Runnable
{
	private ITerminusMsgCallback msgCallback;
	private Socket socket;
	
	public LowLevelImageThread(Socket s, ITerminusMsgCallback mc)
	{
		this.msgCallback = mc;
		this.socket = s;
	}
	
	@Override
	public void run()
	{
		while (true)
		{
			try
			{
				LowLevelReader reader = new LowLevelReader(socket);
				TerminusMessage tm = reader.getNextMessage();
				if (tm != null)
				{
					this.msgCallback.messageReceived(null, tm);
				}
			}
			catch (Exception e)
			{
				try
				{
					socket.close();
				}
				catch (IOException e1)
				{
				}
				return;
			}
		}
	}
}
