package edu.buffalo.cse.terminus.messages;

public abstract class TerminusMessage
{
	public static final int MSG_REGISTER = 0x1;
	public static final int MSG_UNREGISTER = 0x2;
	public static final int MSG_REG_RESPONSE = 0x3;
	public static final int MSG_EVENT = 0x4;
	public static final int MSG_IMAGE = 0x5;
	public static final int MSG_TEST = 0xFFFF;
	
	/*
	 * This is read-only.
	 * 
	 * This should be set when the message is constructed or by
	 * a method in the class itself.
	 */
	protected String uid;
	
	/*
	 * This is read-only.
	 * 
	 * Each new message type needs to extend this class and should
	 * set this when it's constructed.
	 */
	protected int type;
	
	/*
	 * This constructor can be used for incoming messages where the
	 * type needs to be read from some input stream.
	 */
	public TerminusMessage()
	{
		this.uid = "";
	}
	
	/*
	 * This constructor would be used by clients that know their id.
	 */
	public TerminusMessage(String id)
	{
		this.uid = id;
	}
	
	public String getID()
	{
		return this.uid;
	}
	
	public int getMessageType()
	{
		return this.type;
	}
}
