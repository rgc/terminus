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

      ActorRef clientActor = getSender();
      clientActor.tell("lala");

    } else if(message instanceof RegisterMessage) {
	registerClient((RegisterMessage)message);

    } else {
	System.out.println("bad message");
    }
  }

  private void registerClient(RegisterMessage message) {
	System.out.println("register");
      	sessions.put(message.getId(), getSender());
  }
  
}
