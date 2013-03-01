package edu.buffalo.cse.terminus.common.message;

public class OptionsMessage extends TerminusMessage {

	// refactor to server objects
	// private Server managementServer;
	// private Server eventServer;
	private String managementServerAddress;
	private int managementServerPort;

	private String eventServerAddress;
	private int eventServerPort;

	public OptionsMessage(String id) {
		super(id);
	}

	public OptionsMessage(String serverAddress, int serverPort, String id) {
		super(id);
		this.setManagementServerAddress(serverAddress);
		this.setManagementServerPort(serverPort);
		this.setEventServerAddress(serverAddress);
		this.setEventServerPort(serverPort);
	}

	public OptionsMessage(String managementServerAddress, int managementServerPort, String eventServerAddress, int eventServerPort, String id) {
		super(id);
		this.setManagementServerAddress(managementServerAddress);
		this.setManagementServerPort(managementServerPort);
		this.setEventServerAddress(eventServerAddress);
		this.setEventServerPort(eventServerPort);
		
	}

	public String getManagementServerAddress() {
		return this.managementServerAddress;
	}

	public void setManagementServerAddress( String address ) {
		this.managementServerAddress = address;
	}

	public int getManagementServerPort() {
		return this.managementServerPort;
	}

	public void setManagementServerPort( int port ) {
		this.managementServerPort = port;
	}

	public String getEventServerAddress() {
		return this.eventServerAddress;
	}

	public void setEventServerAddress( String address ) {
		this.eventServerAddress = address;
	}

	public int getEventServerPort() {
		return this.eventServerPort;
	}

	public void setEventServerPort( int port ) {
		this.eventServerPort = port;
	}

  
}
