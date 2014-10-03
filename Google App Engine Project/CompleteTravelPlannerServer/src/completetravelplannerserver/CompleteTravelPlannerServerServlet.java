package completetravelplannerserver;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.*;
import javax.servlet.http.*;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class CompleteTravelPlannerServerServlet extends HttpServlet {
	private static final int maxThreads = 100;
	final Executor exec = Executors.newFixedThreadPool(maxThreads);
	private static final Logger log = Logger.getLogger(CompleteTravelPlannerServerServlet.class.getName());
	
	public void doGet(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain");
		resp.getOutputStream().println("Hurray !! You have connected to Complete Travel Planner Server");
	}
	
	public void doPost(final HttpServletRequest req, final HttpServletResponse resp)
			throws IOException {
		Client client = new Client(resp);
		try {
			InputStreamReader out = new InputStreamReader(req.getInputStream());
			StringBuilder line = new StringBuilder();
			int charCode = -1;
			while((charCode = out.read()) != -1){
				line.append((char) charCode);
			}
			client.handleIncoming(line.toString().trim());
		} catch (IOException ex) {
			log.severe(ex.toString());
		}
	}
}
