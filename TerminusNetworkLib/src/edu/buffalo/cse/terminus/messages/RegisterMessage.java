package edu.buffalo.cse.terminus.messages;

public class RegisterMessage extends TerminusMessage {
	
	public static final int REG_TYPE_EVENT = 1;
	public static final int REG_TYPE_CONSUMER = 2;
	
	protected int regType;
	protected String location = "";
	protected String nickname = "";
	
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
	
	public String getLocation()
	{
		return location;
	}
	
	public void setLocation(String l)
	{
		location = l;
	}
	
	public String getNickname()
	{
		return nickname;
	}
	
	public void setNickname(String n)
	{
		nickname = n;
	}
}
