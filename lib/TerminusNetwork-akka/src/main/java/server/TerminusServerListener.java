package edu.buffalo.cse.terminus.server;

import edu.buffalo.cse.terminus.common.*;
import edu.buffalo.cse.terminus.common.message.*;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;
import akka.actor.Props;

import java.util.*;

public class TerminusServerListener extends UntypedActor {
 
  private final Map<String, ActorRef> sessions = new HashMap<String, ActorRef>();

  public void onReceive(Object message) throws Exception {
    if(message instanceof String) {
      final String string = (String) message;
      System.out.println("Received String: \""+string+"\"");

    } else if(message instanceof RegisterMessage) {
	ActorRef client = getSender();
	registerClient((RegisterMessage)message, client);

    } else {
	System.out.println("bad message");
    }
  }

  private void registerClient(RegisterMessage message, ActorRef client) {
	String id = message.getId();
      	
	sessions.put(id, client);

	System.out.println("Registration from ID: " + id);
	
	OptionsMessage m = new OptionsMessage(id);
	client.tell(m, getSelf());

  }
  
}
