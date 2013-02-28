package edu.buffalo.cse.terminus.client;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.Props;

public class TerminusClientListener extends UntypedActor {
  
  	private ActorRef server;

	@Override
	public void preStart() {
 		server = getContext().actorFor("akka://TerminusServer@127.0.0.1:3003/user/TerminusServer");
		this.getContext().watch(server);

  		// registering with other actors
		//  someService.tell(Register(getSelf());
	}
  
	@Override
  	public void onReceive(Object message) throws Exception {
      		System.out.println(message.getClass().getName());

		if(message instanceof String) {
      			System.out.println("Send String(\""+message+"\") message!");
      			server.tell(message, getSelf());
		
		} else if (message instanceof Terminated) {

			final Terminated t = (Terminated) message;

			if(t.getActor() == server) {
				System.out.println("Server terminated.");
			}

		} else {
			// unhandled!
		}

		


  	}
  
}
