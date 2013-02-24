package server;

/*
 * Callback interface for terminus events
 * 
 * The UI should implement this to 
 */
public interface ITerminusCallbacks
{
	public void connectionAdded();
	public void connectionDropped();
	public void messageReceived(TerminusMessage msg);

	//TODO: Error event?
}
