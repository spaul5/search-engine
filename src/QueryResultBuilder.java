import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class QueryResultBuilder extends AbstractQueryResultBuilder {

	/**
	 * stores a query mapped to the list of results
	 */
	private final LinkedHashMap<String, ArrayList<QueryResult>> queryInfoMap;

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
	public QueryResultBuilder(SynchronizedIndex synchronizedIndex) {
		queryInfoMap = new LinkedHashMap<>();
		this.synchronizedIndex = synchronizedIndex;
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
		line = line.trim();
		String[] queries = WordParser.split(line);

		ArrayList<QueryResult> queryList = synchronizedIndex.partialSearch(queries);
		queryInfoMap.put(line, queryList);
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
		return JSONTreeWriter.writeQueryInfo(output, queryInfoMap);
	}

	/**
	 * Returns a string representation of the synchronized index.
	 */
	@Override
	public String toString() {
		return queryInfoMap.toString();
	}
}
