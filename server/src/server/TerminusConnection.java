package server;

import java.io.*;
import java.net.*;

public class TerminusConnection implements Runnable
{
	Socket socket;
	ITerminusCallbacks eventClient;
	DataOutputStream out;
	
	public TerminusConnection(Socket s, ITerminusCallbacks client)
	{
		this.socket = s;
		this.eventClient = client;
		
		try
		{
			this.out = new DataOutputStream(s.getOutputStream());
		} 
		catch (IOException e)
		{
			//TODO: We'll have to handle or throw this...
			return;
		}
	}
	
	@Override
	public void run()
	{
		BufferedReader in = null;
		try 
		{
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		} 
		catch (IOException e) {
			//TODO: Raise an error for anyone interested
			//		close/invalidate connection (accounting)
			return;
		}
		
		while (true) {
			try {
				String lineIn = in.readLine();
				if (lineIn == null) {
					this.socket.close();
					return;
				}
				
				TerminusMessage m = new TerminusMessage();
				m.message = lineIn;
				this.eventClient.messageReceived(m);
				
				// Echo the message
				String msgOut = m.message + "\n";
				this.out.write(msgOut.getBytes());
				
			} catch (IOException e) {
				//TODO: close/invalidate connection (accounting)
				this.eventClient.connectionDropped();
				return;
			}
		}	
	}
}
