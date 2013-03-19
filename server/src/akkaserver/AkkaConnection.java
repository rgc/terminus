package akkaserver;

import edu.buffalo.cse.terminus.messages.*;

import shared.ATerminusConnection;

import akka.actor.ActorRef;


public class AkkaConnection extends ATerminusConnection
{
	private ActorRef serverActor;
	private ActorRef clientActor;

	public AkkaConnection(ActorRef serverActor, ActorRef clientActor)
	{
		super();
		this.serverActor = serverActor;
		this.clientActor = clientActor;
		return;
	}

	@Override
	public void sendMessage(TerminusMessage m)
	{
		this.clientActor.tell(m, this.serverActor);
		return;
	}

	@Override
	public void shutdown()
	{
		return;
	}
}
