

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;


/**
 * Demonstrates server-side and client-side sockets.
 *
 * @see SimpleServer
 * @see SimpleClient
 */
public class SearchEngineServer {
	
	private static int PORT = 8080;
	
	public static void main(String[] args) throws Exception {
		// look at message server in lectures
		// call constructors of servlets to pass in index object
		// create index object here

		// type of handler that supports sessions
		ServletHandler handler;

		// turn on sessions and set context
		handler = new ServletHandler(); //ServletContextHandler.SESSIONS);
		handler.addServletWithMapping(ParseLinkServlet.class, "/*");
		handler.addServletWithMapping(SearchServlet.class, "/search");
		handler.addServletWithMapping(ResultsServlet.class, "/results");

		// setup jetty server
		Server server = new Server(PORT);
		server.setHandler(handler);
		server.start();
		server.join();
	}
}