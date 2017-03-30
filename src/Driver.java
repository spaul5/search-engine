import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This software driver class provides a consistent entry point for the search
 * engine. Based on the arguments provided to {@link #main(String[])}, it
 * creates the necessary objects and calls the necessary methods to build an
 * inverted index, process search queries, configure multithreading, and launch
 * a web server (if appropriate).
 */
public class Driver {

	/**
	 * Flag used to indicate the following value is an input directory of text
	 * files to use when building the inverted index.
	 * 
	 * @see "Projects 1 to 5"
	 */
	public static final String INPUT_FLAG = "-input";

	/**
	 * Flag used to indicate the following value is the path to use when
	 * outputting the inverted index to a JSON file. If no value is provided,
	 * then {@link #INDEX_DEFAULT} should be used. If this flag is not provided,
	 * then the inverted index should not be output to a file.
	 * 
	 * @see "Projects 1 to 5"
	 */
	public static final String INDEX_FLAG = "-index";

	/**
	 * Flag used to indicate the following value is a text file of search
	 * queries.
	 * 
	 * @see "Projects 2 to 5"
	 */
	public static final String QUERIES_FLAG = "-query";

	/**
	 * Flag used to indicate the following value is the path to use when
	 * outputting the search results to a JSON file. If no value is provided,
	 * then {@link #RESULTS_DEFAULT} should be used. If this flag is not
	 * provided, then the search results should not be output to a file.
	 * 
	 * @see "Projects 2 to 5"
	 */
	public static final String RESULTS_FLAG = "-results";

	/**
	 * Flag used to indicate the following value is the number of threads to use
	 * when configuring multithreading. If no value is provided, then
	 * {@link #THREAD_DEFAULT} should be used. If this flag is not provided,
	 * then multithreading should NOT be used.
	 * 
	 * @see "Projects 3 to 5"
	 */
	public static final String THREAD_FLAG = "-threads";

	/**
	 * Flag used to indicate the following value is the seed URL to use when
	 * building the inverted index.
	 * 
	 * @see "Projects 4 to 5"
	 */
	public static final String SEED_FLAG = "-seed";

	/**
	 * Flag used to indicate the following value is the port number to use when
	 * starting a web server. If no value is provided, then
	 * {@link #PORT_DEFAULT} should be used. If this flag is not provided, then
	 * a web server should not be started.
	 */
	public static final String PORT_FLAG = "-port";

	/**
	 * Default to use when the value for the {@link #INDEX_FLAG} is missing.
	 */
	public static final String INDEX_DEFAULT = "index.json";

	/**
	 * Default to use when the value for the {@link #RESULTS_FLAG} is missing.
	 */
	public static final String RESULTS_DEFAULT = "results.json";

	/**
	 * Default to use when the value for the {@link #THREAD_FLAG} is missing.
	 */
	public static final int THREAD_DEFAULT = 5;

	/**
	 * Default to use when the value for the {@link #PORT_FLAG} is missing.
	 */
	public static final int PORT_DEFAULT = 8080;

	/**
	 * builds inverted index if correct command line arguments are inputted
	 * 
	 * @param argumentParser
	 *            argumentParser object
	 * @param invertedIndex
	 *            invertedIndex object
	 */
	public static void buildInvertedIndex(ArgumentParser argumentParser, SynchronizedIndex synchronizedIndex,
			ConcurrentInvertedIndexHelper concurrentInvertedIndexHelper) {
		if (argumentParser.getValue("-input") == null) {
			System.out.println("No directory inputed.");
			return;
		}

		Path input = Paths.get(argumentParser.getValue("-input"));

		if (!Files.isDirectory(input)) {
			System.out.println("Input is not a directory.");
			return;
		}

		if (argumentParser.hasFlag("-threads")) {
			concurrentInvertedIndexHelper.traverse(input, synchronizedIndex);
		} else {
			InvertedIndexHelper.traverse(input, synchronizedIndex);
		}
	}

	/**
	 * builds an index of QueryResults if correct command line arguments are
	 * inputted
	 * 
	 * @param argumentParser
	 *            argumentParser object
	 * @param synchronizedIndex
	 *            synchronizedIndex object
	 * @param queryResultBuilder
	 *            argumentParser object
	 * @param concurrentQueryResultBuilder
	 *            concurrentQueryResultBuilder object
	 */
	public static void buildQueryResultIndex(ArgumentParser argumentParser, SynchronizedIndex synchronizedIndex,
			QueryResultBuilder queryResultBuilder, ConcurrentQueryResultBuilder concurrentQueryResultBuilder) {
		if (argumentParser.getValue("-query") == null) {
			System.out.println("No query file inputted");
			return;
		}

		if (!argumentParser.hasFlag("-query")) {
			System.out.println("No query flag entered.");
			return;
		}

		Path query = Paths.get(argumentParser.getValue("-query"));

		if (!Files.isReadable(query)) {
			System.out.println("Query file is not readable.");
			return;
		}

		if (argumentParser.hasValue("-threads")) {
			concurrentQueryResultBuilder.readPath(query);
		} else {
			queryResultBuilder.readPath(query);
		}
	}
	

	/**
	 * Parses the provided arguments and, if appropriate, will build an inverted
	 * index from a directory or seed URL, process search queries, configure
	 * multithreading, and launch a web server.
	 * 
	 * @param args
	 *            set of flag and value pairs
	 */
	public static void main(String[] args) {
		System.out.println(Arrays.toString(args));

		SynchronizedIndex synchronizedIndex = new SynchronizedIndex();
		ConcurrentInvertedIndexHelper concurrentInvertedIndexHelper = null;
		WebCrawler webCrawler = null;
		QueryResultBuilder queryResultBuilder = null;
		ConcurrentQueryResultBuilder concurrentQueryResultBuilder = null;
		ArgumentParser argumentParser = new ArgumentParser();
		argumentParser.parseArguments(args);
		int threads = 9999;
		
		if (argumentParser.hasFlag("-threads")) {
			if (argumentParser.getValue("-threads") == null) {
				System.out.println("Incorrect value for -threads.");
				return;
			} else {
				if (argumentParser.getValue("-threads").contains(".")
						|| Character.isLetter(argumentParser.getValue("-threads").charAt(0))) {
					System.out.println("Incorrect value for -threads.");
					return;
				}
				threads = Integer.valueOf(argumentParser.getValue("-threads"));
			}
		}
		
		if (argumentParser.hasFlag("-input") || argumentParser.hasFlag("-seed")) {
			if (argumentParser.hasFlag("-threads")) {
				if (threads <= 10 && threads > 0 && !argumentParser.hasFlag("-seed")) {
					concurrentInvertedIndexHelper = new ConcurrentInvertedIndexHelper(threads);
				} else if (threads <= 10 && threads > 0 && argumentParser.hasFlag("-seed")) {
					webCrawler = new WebCrawler(synchronizedIndex, threads);
				} else if (threads == 9999) {
					webCrawler = new WebCrawler(synchronizedIndex, 5);
				} else if (threads < 0) {
					System.out.println("Incorrect value for -threads.");
				} else {
					concurrentInvertedIndexHelper = new ConcurrentInvertedIndexHelper(5);
				}
			}
			
			if (argumentParser.hasFlag("-seed")) {
				webCrawler.parseLink(argumentParser.getValue("-seed"));
			} else {
				buildInvertedIndex(argumentParser, synchronizedIndex, concurrentInvertedIndexHelper);
			}
		} else {
			System.out.println("No input flag entered.");
			return;
		}

		if (concurrentInvertedIndexHelper != null) {
			concurrentInvertedIndexHelper.shutdown();
		}
		
		if (webCrawler != null) {
			webCrawler.shutdown();
			synchronizedIndex.toString();
		}

		if (argumentParser.hasFlag("-index")) {
			String outputString = null;
			Path output = null;
			if (argumentParser.getValue("-index") != null) {
				outputString = argumentParser.getValue("-index");
			} else {
				outputString = "index.json";
			}
			output = Paths.get(outputString);
			synchronizedIndex.writeJSON(output);
		}

		if (argumentParser.hasFlag("-query")) {
			if (argumentParser.hasFlag("-threads") || argumentParser.hasFlag("-seed")) {
				if (threads <= 10 && threads > 0) {
					concurrentQueryResultBuilder = new ConcurrentQueryResultBuilder(synchronizedIndex, threads);
				} else {
					concurrentQueryResultBuilder = new ConcurrentQueryResultBuilder(synchronizedIndex, 5);
				}
			}
			queryResultBuilder = new QueryResultBuilder(synchronizedIndex);
			buildQueryResultIndex(argumentParser, synchronizedIndex, queryResultBuilder, concurrentQueryResultBuilder);
		} else {
			System.out.println("No query flag entered.");
			return;
		}

		if (concurrentQueryResultBuilder != null) {
			concurrentQueryResultBuilder.shutdown();
		}
		
//		if (argumentParser.hasFlag("-display")) {
//			getList(concurrentQueryResultBuilder, argumentParser.getValue("-query"));
//		}

		if (argumentParser.hasFlag("-results")) {
			String queryResultString = null;
			Path queryResult = null;
			if (argumentParser.getValue("-results") != null) {
				queryResultString = argumentParser.getValue("-results");
			} else {
				queryResultString = "results.json";
			}
			queryResult = Paths.get(queryResultString);
			if (argumentParser.hasValue("-threads")) {
				concurrentQueryResultBuilder.writeJSON(queryResult);
			} else {
				queryResultBuilder.writeJSON(queryResult);
			}
		}

		System.out.println("DONE.");
	}
	
//	public static ArrayList<QueryResult> getList(ConcurrentQueryResultBuilder concurrentQueryResultBuilder, String query) {
//		return concurrentQueryResultBuilder.getResults(query);
//	}

}