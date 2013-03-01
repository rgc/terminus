package edu.buffalo.cse.terminus.client;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.Props;

import edu.buffalo.cse.terminus.common.message.*;

// convert to TerminusClientActor

public class TerminusClientListener extends UntypedActor {
  
  	private ActorRef server;
	private boolean registered;

	@Override
	public void preStart() {

		registered = false;

 		server = getContext().actorFor("akka://TerminusServer@127.0.0.1:3003/user/TerminusServer");
		this.getContext().watch(server);

  		// registering with other actors
		//  someService.tell(Register(getSelf());
		registerClient();
	}
  
	@Override
  	public void onReceive(Object message) throws Exception {
      		System.out.println(message.getClass().getName());

		if(message instanceof String) {
      			System.out.println("Received String \""+message+"\"");
		
		} else if (message instanceof OptionsMessage) {
      			System.out.println("Received Options Message");
			this.registered = true;

			this.setClientOptions((OptionsMessage)message);

		} else if (message instanceof Terminated) {

			final Terminated t = (Terminated) message;

			if(t.getActor() == server) {
				System.out.println("Server terminated.");
			}

		} else {
			// unhandled!
		}

  	}

	public void registerClient () {

		while(!registered) {
      			System.out.println("Sending Registration message!");
      			server.tell("hey", getSelf());

			try {
    				Thread.sleep(1000);
			} catch(InterruptedException ex) {
    				Thread.currentThread().interrupt();
			}

		}

	}

	public void setClientOptions (OptionsMessage message) {

	}
  
}
