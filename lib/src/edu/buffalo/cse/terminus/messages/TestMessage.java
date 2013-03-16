package edu.buffalo.cse.terminus.messages;

public class TestMessage extends TerminusMessage
{
	public String message = "";
	
	public TestMessage()
	{
		super();
		this.type = TerminusMessage.MSG_TEST;
	}
	
	public TestMessage(String id)
	{
		super(id);
		this.type = TerminusMessage.MSG_TEST;
	}
}
