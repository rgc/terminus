package edu.buffalo.cse.terminus.client;

import edu.buffalo.cse.terminus.common.message.*;

public class TerminusClientTest {
  
  public static void main(String[] args) {

	// example use
	TerminusClient network = new TerminusClient();

	// network will auto-register with server and determine
	// where to send events, etc 

	// you detect motion, decided to send it...
	// we excluded the image buffer here
	EventMovementDetected e = new EventMovementDetected("lala");

	network.send(e);

	System.out.println("sleeping...");
	try {
		Thread.sleep(8000);
	} catch (InterruptedException ex) {
		System.out.println("exception");
	}
	System.out.println("awake...");
	network.send("awake");
	
  }
  
}
