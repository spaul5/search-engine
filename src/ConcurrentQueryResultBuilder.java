import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ConcurrentQueryResultBuilder extends AbstractQueryResultBuilder {

	/**
	 * stores a query mapped to the list of results
	 */
	private final LinkedHashMap<String, ArrayList<QueryResult>> queryInfoMap;

	/** Work queue used to handle multithreading for this class. */
	private final WorkQueue workers;

	/**
	 * synchronizedIndex object
	 */
	private final SynchronizedIndex synchronizedIndex;

	/**
	 * constructor that initializes the LinkedHashMap and the synchronizedIndex
	 * 
	 * @param synchronizedIndex
	 *            synchronizedIndex object
	 */
	public ConcurrentQueryResultBuilder(SynchronizedIndex synchronizedIndex, int threads) {
		queryInfoMap = new LinkedHashMap<>();
		this.synchronizedIndex = synchronizedIndex;
		workers = new WorkQueue(threads);
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

		private String line;

		public Minion(String line) {
			this.line = line;
		}

		@Override
		public void run() {
			line = line.trim();
			String[] queries = WordParser.split(line);
			ArrayList<QueryResult> queryList = synchronizedIndex.partialSearch(queries);

			synchronized (queryInfoMap) {
				queryInfoMap.put(line, queryList);
			}
		}
	}

	/**
	 * parses a line of queries to call the partial search method with
	 * 
	 * @see partialSearch(String[] queries)
	 * 
	 * @param line
	 *            line is the query
	 */
	@Override
	public void parseLine(String line) {
		synchronized (queryInfoMap) {
			queryInfoMap.put(line, null);
		}

		workers.execute(new Minion(line));
	}

	/**
	 * calls JSONTreeWriter's writeQueryInfo to create the JSON file from the
	 * map of queries.
	 * 
	 * @param output
	 *            output file that is written onto by JSONTreeWriter
	 */
	@Override
	public boolean writeJSON(Path output) {
		synchronized (queryInfoMap) {
			return JSONTreeWriter.writeQueryInfo(output, queryInfoMap);
		}
	}
	
	public ArrayList<QueryResult> getResults(String query) {
		ArrayList<QueryResult> aList = new ArrayList<QueryResult>();
		synchronized (queryInfoMap) {
			aList = queryInfoMap.get(query);
		}
		return aList;
	}

	/**
	 * Returns a string representation of the synchronized index.
	 */
	@Override
	public String toString() {
		synchronized (queryInfoMap) {
			return queryInfoMap.toString();
		}
	}
}
