package eventserver;

import shared.ATerminusConnection;
import edu.buffalo.cse.terminus.messages.*;

/*
 * Callback interface for terminus events
 * 
 * The UI should implement this too
 */
public interface IEventCallbacks
{
	public void connectionAdded(ATerminusConnection connection);
	public void connectionDropped(ATerminusConnection connection);
	public void messageReceived(TerminusMessage msg);
}
