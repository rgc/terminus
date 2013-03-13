package shared;

public class ServerCloseException extends Exception
{
	private static final long serialVersionUID = 1L;
	private String message;
	
	public ServerCloseException()
	{
		this.message = "";
	}
	
	public ServerCloseException(String m)
	{
		this.message = m;
	}
	
	@Override
	public String getMessage()
	{
		return this.message;
	}
}
