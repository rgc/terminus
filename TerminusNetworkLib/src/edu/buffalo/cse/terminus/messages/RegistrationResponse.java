package edu.buffalo.cse.terminus.messages;

public class RegistrationResponse extends TerminusMessage {

	public static int REGISTRATION_SUCCESS = 1;
	public static int REGISTRATION_FAILED = 2;
	
	protected int result = REGISTRATION_FAILED;
	
	public RegistrationResponse() 
	{
		super();
		this.type = TerminusMessage.MSG_REG_RESPONSE;
	}

	public RegistrationResponse(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_REG_RESPONSE;
	}
	
	public int getResult()
	{
		return this.result;
	}
	
	public void setResult(int r)
	{
		this.result = r;
	}
}
