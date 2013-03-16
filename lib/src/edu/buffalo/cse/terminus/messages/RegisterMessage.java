package edu.buffalo.cse.terminus.messages;

public class RegisterMessage extends TerminusMessage {
	
	public static final int REG_TYPE_EVENT = 1;
	public static final int REG_TYPE_CONSUMER = 2;
	
	protected int regType;
	
	public RegisterMessage() 
	{
		super();
		this.type = TerminusMessage.MSG_REGISTER;
	}
	
	public RegisterMessage(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_REGISTER;
	}
	
	public int getRegistrationType()
	{
		return this.regType;
	}
	
	public void setRegistrationType(int rtype)
	{
		this.regType = rtype;
	}
}
