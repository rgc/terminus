
import org.eclipse.jetty.webapp.WebAppContext;

public class TerminusWebServerContext {
	
	private WebAppContext webAppContext;
	
	public WebAppContext buildWebAppContext(){
		webAppContext = new WebAppContext();
		webAppContext.setDescriptor(webAppContext + "/WEB-INF/web.xml");
		webAppContext.setResourceBase("www");
		webAppContext.setContextPath("/");
		return webAppContext;
	}
}  