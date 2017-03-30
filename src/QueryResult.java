
public class QueryResult implements Comparable<QueryResult> {

	/**
	 * filename the query object was found in
	 */
	private final String fileName;

	/**
	 * times the query was found
	 */
	private int count;

	/**
	 * first index the query was found in
	 */
	private int index;

	/**
	 * Constructor that initializes the fileName, count, and index.
	 */
	public QueryResult(String fileName, int count, int index) {
		this.fileName = fileName;
		this.count = count;
		this.index = index;
	}

	/**
	 * returns the filename the query was found in
	 * 
	 * @return filename the query was found in
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * returns the count of the query
	 * 
	 * @return count of the query
	 */
	public int getCount() {
		return count;
	}

	/**
	 * returns the index of the query
	 * 
	 * @return index of the query
	 */
	public int getIndex() {
		return index;
	}

	@Override
	public int compareTo(QueryResult result) {
		if (this.count < result.count) {
			return 1;
		} else if (this.count == result.count) {
			if (this.index < result.index) {
				return -1;
			} else if (this.index == result.index) {
				return fileName.compareTo(result.fileName);
			} else {
				return 1;
			}
		} else {
			return -1;
		}
	}

	/**
	 * updates the count and index for a QueryResult object
	 * 
	 * @param count
	 *            count to be added to the old count
	 * 
	 * @param index
	 *            first index the query was found in
	 */
	public void updateValues(int count, int index) {

		this.count += count;
		if (this.index > index) {
			this.index = index;
		}

	}

	@Override
	public String toString() {
		return fileName + ", " + count + ", " + index;
	}
}
