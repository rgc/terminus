package edu.buffalo.cse.terminus.client;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.ActorRef;
import akka.actor.DeadLetter;
import akka.remote.RemoteLifeCycleEvent;

public class TerminusClient {
  
  private ActorSystem system;
  private ActorRef clientActor;

  public TerminusClient () {
	this.startup();

  }
  
  public void startup() {
  	
	system 		= ActorSystem.create("TerminusClient", ConfigFactory.load().getConfig("TerminusClient")); 
  	clientActor 	= system.actorOf(new Props(TerminusClientListener.class), "TerminusClient");

	// clientActor will recieve all remote lifecycle events
	system.eventStream().subscribe(clientActor, RemoteLifeCycleEvent.class);

	// clientActor will recieve all deadletter events - deadletter is where
	// messages that weren't able to be sent are placed
	system.eventStream().subscribe(clientActor, DeadLetter.class);

  }

  public void send(Object o) {
	clientActor.tell(o);
  }
  
  public void shutdown() {
    	system.shutdown();
  }
  
}
