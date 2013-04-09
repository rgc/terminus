package edu.buffalo.cse.terminus.messages;

/*
 * This message may be sent for two reasons:
 * 	1) Server -> Client: Indicates the session has been terminated
 * 		and the client needs to re-register.  This is different from
 * 		telling a node to shutdown.  This could be sent if a client hasn't
 * 		registered and starts sending messages.
 * 
 *  2) Client -> Server:  Indicates the client is closing the session
 */
public class UnregisterMessage extends TerminusMessage 
{	
	/*
	 * At this point, no fields.  This is just an informational message
	 */
	public UnregisterMessage() 
	{
		this.type = TerminusMessage.MSG_UNREGISTER;
	}

	public UnregisterMessage(String id) 
	{
		super(id);
		this.type = TerminusMessage.MSG_UNREGISTER;
	}

}
