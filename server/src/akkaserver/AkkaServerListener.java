package akkaserver;

import edu.buffalo.cse.terminus.messages.*;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;

import eventserver.ITerminusMsgCallback;

public class AkkaServerListener extends UntypedActor {
 
  private ITerminusMsgCallback callback;
  
  public void onReceive(Object message) throws Exception {
    if(message instanceof String) {
      final String string = (String) message;
      System.out.println("Received String: \""+string+"\"");
      
    } else if(message instanceof ITerminusMsgCallback) {
    	this.callback = (ITerminusMsgCallback)message;
    
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
