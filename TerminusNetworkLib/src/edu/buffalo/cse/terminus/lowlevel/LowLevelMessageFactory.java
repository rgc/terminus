package edu.buffalo.cse.terminus.lowlevel;

import edu.buffalo.cse.terminus.lowlevel.LowLevelRegisterMessage;
import edu.buffalo.cse.terminus.lowlevel.LowLevelTestMessage;
import edu.buffalo.cse.terminus.messages.UnregisterMessage;
import edu.buffalo.cse.terminus.messages.EventMessage;
import edu.buffalo.cse.terminus.messages.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.messages.RegisterMessage;
import edu.buffalo.cse.terminus.messages.RegistrationResponse;
import edu.buffalo.cse.terminus.messages.TestMessage;

public class LowLevelMessageFactory implements ITerminusMessageFactory
{
	@Override
	public TestMessage getTestMessage(String id)
	{
		return new LowLevelTestMessage(id);
	}

	@Override
	public RegisterMessage getRegisterMessage(String id) 
	{
		return new LowLevelRegisterMessage(id);
	}

	@Override
	public RegistrationResponse getRegistrationResponse(String id) 
	{
		return new LowLevelRegResponse(id);
	}

	@Override
	public EventMessage getEventMessage(String id) 
	{
		return new LowLevelEventMessage(id);
	}

	@Override
	public UnregisterMessage getUnregisterMessage(String id) 
	{
		return new LowLevelUnregisterMessage(id);
	}
}
