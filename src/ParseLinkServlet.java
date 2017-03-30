

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ParseLinkServlet extends HttpServlet {

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
	public static void prepareLink(String title, HttpServletResponse response) throws IOException {

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

		out.printf("<form action=\"parse\" method=\"post\">");
		out.printf("<label for=\"link\">Enter a link to store into the database.</label>");
		out.printf("<input id=\"link\" name=\"link\" value=\"\">");
		out.printf("</form>");

	}

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
	public static void finishLink(HttpServletRequest request, HttpServletResponse response) throws IOException {

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

		prepareLink("Search Engine", res);

		
		String link = req.getParameter("link");
		if (link != null) {
			writer.write("ParseLinkServlet");
			String[] args = { "-seed", link, "-thread", "05" };
			Driver.main(args);
		}

		finishLink(req, res);
		res.setContentType("text/html");
		res.setStatus(HttpServletResponse.SC_OK);
		res.flushBuffer();
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String link = req.getParameter("link");
		res.sendRedirect("/search?link="+link);
	}
}
