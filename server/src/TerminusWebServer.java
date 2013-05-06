import java.util.Properties;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public class TerminusWebServer {

	private Server server;
	private	ContextHandlerCollection contexts;

	public TerminusWebServer() {
		this(80);
	}
	public TerminusWebServer(Integer runningPort) {
		
    	// disable the ridiculous amount of logging from jetty
		//org.eclipse.jetty.util.log.Log.setLog(null);
		
		server = new Server(runningPort);
		
		contexts = new ContextHandlerCollection();
		contexts.setHandlers(
				new Handler[] { new TerminusWebServerContext().buildWebAppContext()});

		this.setHandler(contexts);
		
	}
	
	public void setHandler(ContextHandlerCollection contexts) {
		server.setHandler(contexts);
	}
	
	public void start() throws Exception {
		server.start();
	}
	
	public void stop() throws Exception {
		server.stop();
		server.join();
	}
	
	public boolean isStarted() {
		return server.isStarted();
	}
	
	public boolean isStopped() {
		return server.isStopped();
	}
}