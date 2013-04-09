package edu.buffalo.cse.terminus.messages;

public interface ITerminusMessageFactory
{
	public TestMessage getTestMessage(String id);
	public RegisterMessage getRegisterMessage(String id);
	public UnregisterMessage getUnregisterMessage(String id);
	public RegistrationResponse getRegistrationResponse(String id);
	public EventMessage getEventMessage(String id);
}
