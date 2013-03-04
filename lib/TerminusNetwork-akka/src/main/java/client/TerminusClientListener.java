package edu.buffalo.cse.terminus.client;

import akka.actor.UntypedActor;
import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.Props;
import akka.actor.DeadLetter;
import akka.remote.RemoteClientConnected;

import edu.buffalo.cse.terminus.common.MessageQueue;
import edu.buffalo.cse.terminus.common.message.*;

// convert to TerminusClientActor

public class TerminusClientListener extends UntypedActor {
  
  	private ActorRef server;
	private boolean registered;

	// we're only going to store 20 messages in the queue at a time
	private MessageQueue messageQueue = new MessageQueue(20);	

	@Override
	public void preStart() {

		registered = false;

 		server = getContext().actorFor("akka://TerminusServer@127.0.0.1:3003/user/TerminusServer");
		this.getContext().watch(server);
	}
  
	@Override
  	public void onReceive(Object message) throws Exception {

		if(message instanceof String) {
			// proxy to server
      			System.out.println("Proxy string to server");
			this.sendToServer(message);
		
		} else if (message instanceof DeadLetter) {

			// Messages generate as DeadLetter events when they can't be
			// sent to the remote server, so we'll queue them and 
			// attempt a resend when we detect a remoteClientConnected()
			
			DeadLetter d = (DeadLetter) message;
			this.addToMessageQueue(d.message());

		} else if (message instanceof RemoteClientConnected) {

			if(!registered) {
				this.sendRegisterMessage();
			}

			this.sendMessageQueue();

		} else if (message instanceof OptionsMessage) {
      			System.out.println("Received Options Message");
			this.setClientOptions((OptionsMessage)message);

		} else if (message instanceof EventMovementDetected) {
			// proxy to event server
			this.sendToServer(message);

		} else if (message instanceof Terminated) {

			final Terminated t = (Terminated) message;

			if(t.getActor() == server) {
				System.out.println("Server terminated.");
			}

		} else {
			// unhandled
			//System.out.println("Unhandled: " + message.getClass().getName());
		}

  	}

	public void sendToServer ( Object o ) {
		server.tell(o, getSelf());
	}

	public void sendMessageQueue() {
		for (Object o; (o = messageQueue.poll()) != null;){
			this.sendToServer(o);
		}
      		System.out.println("Message Queue Cleared...");
      		System.out.println("size of queue:" + messageQueue.size() );
	}

	public void addToMessageQueue(Object o) {
		messageQueue.add(o);
      		System.out.println("size of queue:" + messageQueue.size() );
	}

	public void sendRegisterMessage () {

      		System.out.println("Sending Registration message!");
		RegisterMessage m = new RegisterMessage("lalala");

      		this.sendToServer(m);
	
	}

	public void setClientOptions (OptionsMessage message) {
		this.registered = true;

	}
  
}
