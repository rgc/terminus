package shared;

public interface ITerminusServer
{
	public void start() throws ServerCloseException;
	public void stop();
}
