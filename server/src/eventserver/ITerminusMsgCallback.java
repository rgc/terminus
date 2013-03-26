package eventserver;

import shared.ATerminusConnection;
import edu.buffalo.cse.terminus.messages.*;

/*
 * Callback interface for terminus events
 * 
 * The UI should implement this too
 */
public interface ITerminusMsgCallback
{
	public void messageReceived(ATerminusConnection connection, TerminusMessage msg);
}
