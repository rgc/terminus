package edu.buffalo.cse.terminus.client;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.ActorRef;
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

	// clientActor will recieve all local and remote events
	system.eventStream().subscribe(clientActor, RemoteLifeCycleEvent.class);

  }

  public void send(Object o) {
	clientActor.tell(o);
  }
  
  public void shutdown() {
    	system.shutdown();
  }
  
}
