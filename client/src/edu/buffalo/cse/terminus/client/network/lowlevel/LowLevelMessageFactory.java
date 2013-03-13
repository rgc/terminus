package edu.buffalo.cse.terminus.client.network.lowlevel;

import edu.buffalo.cse.terminus.client.network.ITerminusMessageFactory;
import edu.buffalo.cse.terminus.lowlevel.LowLevelTestMessage;
import edu.buffalo.cse.terminus.messages.TestMessage;

public class LowLevelMessageFactory implements ITerminusMessageFactory
{
	@Override
	public TestMessage getTestMessage(String id)
	{
		return new LowLevelTestMessage(id);
	}

}
