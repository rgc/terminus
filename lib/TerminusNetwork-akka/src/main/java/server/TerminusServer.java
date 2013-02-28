package edu.buffalo.cse.terminus.server;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Actor;
import akka.actor.Props;
import akka.actor.ActorRef;

public class TerminusServer {
  
  final ActorSystem system = ActorSystem.create("TerminusServer", ConfigFactory.load().getConfig("TerminusServer"));
  
  public static void main(String[] args) {
    new TerminusServer().startup();
  }
  
  public void startup() {
    final ActorRef server = system.actorOf(new Props(TerminusServerListener.class), "TerminusServer");
    System.out.println("Started Terminus Server!");
    server.tell("test");
  }
  
  public void shutdown() {
    system.shutdown();
  }

}
