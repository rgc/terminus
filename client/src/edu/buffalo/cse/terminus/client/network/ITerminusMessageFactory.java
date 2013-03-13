package edu.buffalo.cse.terminus.client.network;

import edu.buffalo.cse.terminus.messages.*;

public interface ITerminusMessageFactory
{
	public TestMessage getTestMessage(String id);
}
