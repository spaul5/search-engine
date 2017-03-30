

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ResultsServlet extends HttpServlet {

	/**
	 * Prepares the HTTP response by setting the content type and adding header
	 * HTML code.
	 *
	 * @param title
	 *            - web page title
	 * @param response
	 *            - HTTP response
	 * @throws IOException
	 * @see #finishResponse(HttpServletRequest, HttpServletResponse)
	 */
	public static void prepareResults(String title, HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		out.printf("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\"");
		out.printf("\"http://www.w3.org/TR/html4/strict.dtd\">%n%n");
		out.printf("<html>%n%n");
		out.printf("<head>%n");
		out.printf("\t<title>%s</title>%n", title);
		out.printf("\t<meta http-equiv=\"Content-Type\" ");
		out.printf("content=\"text/html;charset=utf-8\">%n");
		out.printf("</head>%n%n");
		out.printf("<body>%n%n");

		out.printf("<form action=\"results\" method=\"post\">");
		out.printf("<label for=\"query\">Results</label>");
//		out.printf("<input id=\"query\" name=\"query\" value=\"\">");
		out.printf("</form>");

	}
	
//	public static void prepareResults(HttpServletResponse response) throws IOException {
//		PrintWriter out = response.getWriter();
//		
//		out.printf("<form action=\"results\" method=\"post\">");
//		out.printf("<label for=\"query\">Results</label>");
////		out.printf("<input id=\"query\" name=\"query\" value=\"\">");
//		out.printf("</form>");
//	}

	/**
	 * Finishes the HTTP response by adding footer HTML code and setting the
	 * response code.
	 *
	 * @param request
	 *            - HTTP request
	 * @param response
	 *            - HTTP response
	 * @throws IOException
	 * @see #prepareResponse(String, HttpServletResponse)
	 */
	public static void finishResults(HttpServletRequest request, HttpServletResponse response) throws IOException {

		PrintWriter out = response.getWriter();

		out.printf("%n");
		out.printf("<p style=\"font-size: 10pt; font-style: italic; text-align: center;");
		out.printf("border-top: 1px solid #eeeeee; margin-bottom: 1ex;\">");

		out.printf("Page <a href=\"%s\">%s</a>", request.getRequestURL(), request.getRequestURL());

		out.printf("</p>%n%n");
		out.printf("</body>%n");
		out.printf("</html>%n");

		out.flush();

		response.setStatus(HttpServletResponse.SC_OK);
		response.flushBuffer();
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		PrintWriter writer = res.getWriter();

		prepareResults("Search Engine", res);

		
		String query = req.getParameter("query");
		String link = req.getParameter("link");
		ArrayList<QueryResult> results = new ArrayList();
//		if (query != null) {
//			writer.write("ResultsServlet");
//			String[] args = { "-seed", link, "-thread", "05" };
//			Driver.main(args);
//			results = Driver.
//		}

		finishResults(req, res);
		res.setContentType("text/html");
		res.setStatus(HttpServletResponse.SC_OK);
		res.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
//		String query = req.getParameter("query");
//		res.sendRedirect("/results?query="+query);
	}
}
