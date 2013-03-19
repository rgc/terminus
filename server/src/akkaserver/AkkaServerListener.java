package akkaserver;

import edu.buffalo.cse.terminus.messages.*;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;

import eventserver.IEventCallbacks;

public class AkkaServerListener extends UntypedActor {
 
  private IEventCallbacks callback;
  
  public void onReceive(Object message) throws Exception {
    if(message instanceof String) {
      final String string = (String) message;
      System.out.println("Received String: \""+string+"\"");
      
    } else if(message instanceof IEventCallbacks) {
    	this.callback = (IEventCallbacks)message;
    
    } else if(message instanceof RegisterMessage) {
    	registerClient((RegisterMessage)message, getSender());

    } else if(message instanceof EventMessage) {
    	eventDetected((EventMessage)message, getSender());
    	
    } else {
    	System.out.println("bad message");
    }
  }

  private void registerClient(RegisterMessage message, ActorRef client) {

	  AkkaConnection ac = new AkkaConnection(getSelf(), client);
	  this.callback.connectionAdded(ac);
	  
	  System.out.println("Registration");
	
	  //OptionsMessage m = new OptionsMessage(id);
	  //client.tell(m, getSelf());

  }

  private void eventDetected(EventMessage message, ActorRef client) {
	  
	  AkkaConnection ac = new AkkaConnection(getSelf(), client);
	  this.callback.messageReceived(ac, message);
	  
	  System.out.println("Movement Detected");
  }
  
}
