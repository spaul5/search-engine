
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example class designed to make fetching the results of different HTTP
 * operations easier.
 */
public class WebCrawler {

	/** Port used by socket. For web servers, should be port 80. */
	public static final int PORT = 80;

	/** Version of HTTP used and supported. */
	public static final String version = "HTTP/1.1";

	/** Work queue used to handle multithreading for this class. */
	private final WorkQueue workers;

	/**
	 * SynchronizedIndex object
	 */
	private final SynchronizedIndex synchronizedIndex;

	/** data structure used to store links that are to be parsed */
	private final HashSet<URL> links;

	private URL firstURL;

	/** Valid HTTP method types. */
	private static enum HTTP {
		OPTIONS, GET, HEAD, POST, PUT, DELETE, TRACE, CONNECT
	};

	public WebCrawler(SynchronizedIndex synchronizedIndex, int threads) {
		this.synchronizedIndex = synchronizedIndex;
		workers = new WorkQueue(threads);
		links = new HashSet<URL>();
		firstURL = null;
	}

	
	/**
	 * begins the process of parsing the url for words to add to the index
	 * 
	 * @param urlStr
	 * 			String version of the url to be parsed.
	 */
	public void parseLink(String urlStr) {
		URL url = null;
		try {
			url = new URL(urlStr);
			firstURL = url;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		links.add(url);
		workers.execute(new Minion(url));
	}

	/**
	 * Parses the provided text for HTML links.
	 *
	 * @param text
	 *            - valid HTML code, with quoted attributes and URL encoded
	 *            links
	 * @return list of links found in HTML code
	 * @throws MalformedURLException
	 */
	private void listLinks(String text) throws MalformedURLException {
		Pattern p = Pattern.compile("(?i)<a\\s*[^>]*\\s*href[^>]?\\s*=\\s*\"([^\"]+)\">*?");
		Matcher m = p.matcher(text);

		URL url = null;
		String temp = "";

		while (m.find()) {
			synchronized (links) {
				temp = m.group(1);
				if (temp.startsWith("#") || temp.startsWith("mailto:")) {
					continue;
				}
				URI uri = URI.create(temp);
				URI baseURI = URI.create(firstURL.toString());
				uri = baseURI.resolve(uri.getPath());
				url = uri.toURL();

				if (!links.contains(url) && links.size() < 50) {
					links.add(url);
					workers.execute(new Minion(url));
				}
			}
		}

	}

	/**
	 * Will connect to the web server and fetch the URL using the HTTP request
	 * provided. It would be more efficient to operate on each line as returned
	 * instead of storing the entire result as a list.
	 *
	 * @param url
	 *            - url to fetch
	 * @param request
	 *            - full HTTP request
	 *
	 * @return the lines read from the web server
	 *
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	private List<String> fetchLines(URL url, String request) throws UnknownHostException, IOException {
		ArrayList<String> lines = new ArrayList<>();

		try (Socket socket = new Socket(url.getHost(), PORT);
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				PrintWriter writer = new PrintWriter(socket.getOutputStream());) {
			writer.println(request);
			writer.flush();

			String line = null;

			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}

		return lines;
	}

	/**
	 * Crafts a minimal HTTP/1.1 request for the provided method.
	 *
	 * @param url
	 *            - url to fetch
	 * @param type
	 *            - HTTP method to use
	 *
	 * @return HTTP/1.1 request
	 *
	 * @see {@link HTTP}
	 */
	private String craftHTTPRequest(URL url, HTTP type) {
		String host = url.getHost();
		String resource = url.getFile().isEmpty() ? "/" : url.getFile();

		return String.format("%s %s %s\n" + "Host: %s\n" + "Connection: close\n" + "\r\n", type.name(), resource,
				version, host);
	}

	/**
	 * Fetches the HTML for the specified URL (without headers).
	 *
	 * @param url
	 *            - url to fetch
	 * @return HTML as a single {@link String}, or null if not HTML
	 *
	 * @throws UnknownHostException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private String fetchHTML(String url) throws UnknownHostException, MalformedURLException, IOException {
		URL target = new URL(url);
		String request = craftHTTPRequest(target, HTTP.GET);
		List<String> lines = fetchLines(target, request);

		int start = 0;
		int end = lines.size();

		// Determines start of HTML versus headers.
		while (!lines.get(start).trim().isEmpty() && start < end) {
			start++;
		}

		// Double-check this is an HTML file.
		Map<String, String> fields = parseHeaders(lines.subList(0, start + 1));
		String type = fields.get("Content-Type");

		if (type != null && type.toLowerCase().contains("html")) {
			return String.join(System.lineSeparator(), lines.subList(start + 1, end));
		}

		return null;
	}

	/**
	 * Helper method that parses HTTP headers into a map where the key is the
	 * field name and the value is the field value. The status code will be
	 * stored under the key "Status".
	 *
	 * @param headers
	 *            - HTTP/1.1 header lines
	 * @return field names mapped to values if the headers are properly
	 *         formatted
	 */
	private Map<String, String> parseHeaders(List<String> headers) {
		Map<String, String> fields = new HashMap<>();

		if (headers.size() > 0 && headers.get(0).startsWith(version)) {
			fields.put("Status", headers.get(0).substring(version.length()).trim());

			for (String line : headers.subList(1, headers.size())) {
				String[] pair = line.split(":", 2);

				if (pair.length == 2) {
					fields.put(pair[0].trim(), pair[1].trim());
				}
			}
		}

		return fields;
	}

	/**
	 * Removes all style and script tags (and any text in between those tags),
	 * all HTML tags, and all special characters/entities.
	 *
	 * THIS METHOD IS PROVIDED FOR YOU. DO NOT MODIFY.
	 *
	 * @param html
	 *            html code to parse
	 * @return plain text
	 */
	private String cleanHTML(String html) {
		String text = html;
		text = stripElement("script", text);
		text = stripElement("style", text);
		text = stripTags(text);
		text = stripEntities(text);
		return text;
	}

	/**
	 * Removes everything between the element tags, and the element tags
	 * themselves. For example, consider the html code:
	 *
	 * <pre>
	 * &lt;style type="text/css"&gt;body { font-size: 10pt; }&lt;/style&gt;
	 * </pre>
	 *
	 * If removing the "style" element, all of the above code will be removed,
	 * and replaced with the empty string.
	 *
	 * @param name
	 *            name of the element to strip, like "style" or "script"
	 * @param html
	 *            html code to parse
	 * @return html code without the element specified
	 */
	private String stripElement(String name, String html) {
		String pattern = "(?is)<" + name + "(.*?)>" + "(.*?)/" + name + "[ ]*>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(html);
		String result = m.replaceAll("");
		return result;
	}

	/**
	 * Removes all HTML tags, which is essentially anything between the "<" and
	 * ">" symbols. The tag will be replaced by the empty string.
	 *
	 * @param html
	 *            html code to parse
	 * @return text without any html tags
	 */
	private String stripTags(String html) {
		String pattern = "(?is)<.*?>";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(html);
		String result = m.replaceAll("");
		return result;
	}

	/**
	 * Replaces all HTML entities in the text with an empty string. For example,
	 * "2010&ndash;2012" will become "20102012".
	 *
	 * @param html
	 *            the text with html code being checked
	 * @return text with HTML entities replaced by an empty string
	 */
	private String stripEntities(String html) {
		String pattern = "(?is)&(.*?);";
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(html);
		String result = m.replaceAll("");
		return result;
	}

	/**
	 * Shutsdown the work queue after all pending work is finished. After this
	 * point, all additional calls to {@link #parseTextFiles(Path, String)} will
	 * no longer work.
	 */
	public void shutdown() {
		workers.finish();
		workers.shutdown();
	}

	/**
	 * Handles updating inverted index
	 */
	private class Minion implements Runnable {

		private URL url;

		public Minion(URL url) {
			this.url = url;
		}

		@Override
		public void run() {
			InvertedIndex local = new InvertedIndex();
			String text = "";

			try {
				text = fetchHTML(url.toString());
			} catch (IOException e1) {
				System.out.println("Error in parsing url.");
				return;
			}

			if (links.size() < 50) {
				try {
					listLinks(text);
				} catch (MalformedURLException e) {
					System.out.println("Error in parsing url.");
					return;
				}
			}
			text = cleanHTML(text);
			String[] words = WordParser.split(text);
			local.addAll(words, url.toString(), 1);
			synchronizedIndex.addAll(local);
		}
	}
}