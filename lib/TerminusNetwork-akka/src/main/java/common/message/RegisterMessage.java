package edu.buffalo.cse.terminus.common.message;

public class RegisterMessage extends TerminusMessage {

	private final String clientAddress;
	private final int clientPort;

	public RegisterMessage(String clientAddress, int clientPort, String id) {
		super(id);
		this.clientAddress = clientAddress;
		this.clientPort    = clientPort;
	}

	public String getClientAddress() {
		return clientAddress;
	}

	public int getClientPort() {
		return clientPort;
	}
  
}
