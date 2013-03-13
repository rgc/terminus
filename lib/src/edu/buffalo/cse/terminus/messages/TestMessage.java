package edu.buffalo.cse.terminus.messages;

public class TestMessage extends TerminusMessage
{
	public String message = "";
	
	public TestMessage()
	{
		super();
	}
	
	public TestMessage(String id)
	{
		super(id);
	}
}
