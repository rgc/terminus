package edu.buffalo.cse.terminus.client;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class TerminusClientListener extends UntypedActor {
  
  final ActorRef server = getContext().actorFor("akka://TerminusServer@127.0.0.1:3003/user/TerminusServer");

@Override
public void preStart() {
  // registering with other actors
//  someService.tell(Register(getSelf());
}
  
  public void onReceive(Object message) throws Exception {
      final String ping = "ping";
      System.out.println("Send String(\""+ping+"\") message!");
      server.tell(ping, getSelf());
  }
  
}
