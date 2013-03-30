package akkaserver;

import akkaserver.AkkaServerListener;

import java.net.*;

import eventserver.ITerminusMsgCallback;

import shared.ITerminusServer;
import shared.ServerCloseException;

import com.typesafe.config.ConfigFactory;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.ActorRef;

public class AkkaServer implements ITerminusServer
{
	private int acceptPort;
	private ServerSocket server = null;
	private ITerminusMsgCallback callback;
	
	final ActorSystem system;
	
	public AkkaServer(ITerminusMsgCallback c, int port)
	{
		system = ActorSystem.create("TerminusServer", ConfigFactory.load().getConfig("TerminusServer"));
		
		this.callback = c;
		this.acceptPort = port;
	}

	@Override
	public void start() throws ServerCloseException
	{
		if (this.server != null && !this.server.isClosed())
		{
			stop();
		}

	    final ActorRef server = system.actorOf(new Props(AkkaServerListener.class), "TerminusServer");
	    System.out.println("Started Terminus Server!");
	    server.tell(this.callback);
	}

	@Override
	public void stop()
	{
		system.shutdown();
	}
}