package edu.buffalo.cse.terminus.client;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.ActorRef;

public class TerminusClient {
  
  final ActorSystem system = ActorSystem.create("TerminusClient", ConfigFactory.load().getConfig("TerminusClient")); 
  
  public static void main(String[] args) {
    new TerminusClient().startup();
  }
  
  public void startup() {
    final ActorRef client = system.actorOf(new Props(TerminusClientListener.class), "TerminusClient");
    System.out.println("Started Terminus Client!");
    client.tell("hey");
  }
  
  public void shutdown() {
    system.shutdown();
  }
  
}
