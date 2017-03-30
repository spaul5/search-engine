import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class InvertedIndex {

	/**
	 * Maps words to which the position they are found within a file
	 */
	private final TreeMap<String, TreeMap<String, TreeSet<Integer>>> invertedIndexMap;

	/**
	 * Constructor that initializes an empty inverted index.
	 */
	public InvertedIndex() {
		invertedIndexMap = new TreeMap<>();
	}

	/**
	 * Adds a word and its position into inverted index. Consider how to handle
	 * duplicates and words with mixed case and extra spaces. Use the other
	 * methods in this class as appropriate!
	 * 
	 * @param word
	 *            word to add to index
	 * @param fileName
	 *            filename word was found in
	 * @position position of word in file
	 * @return boolean method added correctly.
	 */
	private boolean unlockedAdd(String word, String fileName, int position) {
		if (invertedIndexMap.containsKey(word) == false) {
			invertedIndexMap.put(word, new TreeMap<String, TreeSet<Integer>>());
		}

		if (invertedIndexMap.get(word).get(fileName) == null) {
			invertedIndexMap.get(word).put(fileName, new TreeSet<Integer>());
		}

		invertedIndexMap.get(word).get(fileName).add(position);

		return true;
	}

	/**
	 * Adds a word and its position into inverted index. Consider how to handle
	 * duplicates and words with mixed case and extra spaces. Use the other
	 * methods in this class as appropriate!
	 * 
	 * @param word
	 *            word to add to index
	 * @param fileName
	 *            filename word was found in
	 * @position position of word in file
	 * @return boolean method added correctly.
	 */
	public boolean add(String word, String fileName, int position) {
		return unlockedAdd(word, fileName, position);
	}

	/**
	 * Adds all elements sequentially from array into inverted index. Consider
	 * how to handle duplicates and words with mixed case and extra spaces. Use
	 * the other methods in this class as appropriate!
	 * 
	 * @param words
	 *            array of words to add to index
	 * @return the position, in case the next addAll call is on words in the
	 *         same file.
	 */
	public int addAll(String[] words, String fileName, int position) {
		for (String word : words) {
			unlockedAdd(word, fileName, position++);
		}
		return position;
	}

	/**
	 * calls JSONTreeWriter's writeNestedObject to create the JSON file from the
	 * inverted index.
	 * 
	 * @param output
	 *            output file that is written onto by JSONTreeWriter
	 */
	public void writeJSON(Path output) {
		JSONTreeWriter.writeInvertedIndex(output, invertedIndexMap);
	}

	/**
	 * searches invertedIndex for anything that starts with the query and
	 * returns a list of the results
	 * 
	 * @param queries
	 *            queries to search for
	 * 
	 * @return list of QueryResult objects
	 */
	public ArrayList<QueryResult> partialSearch(String[] queries) {
		HashMap<String, QueryResult> infoMap = new HashMap<String, QueryResult>();
		ArrayList<QueryResult> results = new ArrayList<>();

		for (String query : queries) {
			for (String key = invertedIndexMap.ceilingKey(query); key != null
					&& key.startsWith(query); key = invertedIndexMap.higherKey(key)) {
				for (String fileName : invertedIndexMap.get(key).keySet()) {
					if (!infoMap.containsKey(fileName)) {
						infoMap.put(fileName, new QueryResult(fileName, invertedIndexMap.get(key).get(fileName).size(),
								invertedIndexMap.get(key).get(fileName).first()));

						QueryResult result = new QueryResult(fileName, invertedIndexMap.get(key).get(fileName).size(),
								invertedIndexMap.get(key).get(fileName).first());
						infoMap.put(fileName, result);
						results.add(result);
					} else {
						infoMap.get(fileName).updateValues(invertedIndexMap.get(key).get(fileName).size(),
								invertedIndexMap.get(key).get(fileName).first());
					}
				}

			}

		}

		Collections.sort(results);
		return results;
	}

	/**
	 * Returns the number of times a word was found (i.e. the number of
	 * positions associated with a word in the index). Consider how words are
	 * converted before being stored in your index!
	 *
	 * @param word
	 *            word to look for
	 * @return number of times word was found
	 */
	public int countWords(String word) {
		int count = 0;

		if (invertedIndexMap.containsKey(word)) {
			for (String file : invertedIndexMap.get(word).keySet()) {
				count += invertedIndexMap.get(word).get(file).size();
			}
		} else {
			return 0;
		}
		return count;
	}

	/**
	 * Returns the number of files a word was found
	 *
	 * @param word
	 *            word to look for
	 * @return number of files word was found
	 */
	public int countFiles(String word) {
		if (invertedIndexMap.containsKey(word)) {
			return invertedIndexMap.get(word).size();
		} else {
			return 0;
		}
	}

	/**
	 * Returns the total number of words stored in the index.
	 * 
	 * @return number of words
	 */
	public int words() {
		return invertedIndexMap.size();
	}

	/**
	 * Tests whether the index contains the specified word. Consider how you are
	 * storing words in your index!
	 * 
	 * @param word
	 *            word to look for
	 * @return true if the word is stored in the index
	 */
	public boolean contains(String word) {
		return invertedIndexMap.containsKey(word);
	}

	/**
	 * Returns a string representation of the inverted index.
	 */
	@Override
	public String toString() {
		return invertedIndexMap.toString();
	}

	/**
	 * Adds all the words from the given inverted index to the inverted index
	 * member of this class.
	 * 
	 * @param other
	 *            InvertedIndex object
	 */
	public void addAll(InvertedIndex other) {
		for (String word : other.invertedIndexMap.keySet()) {
			if (this.invertedIndexMap.containsKey(word) == false) {
				this.invertedIndexMap.put(word, other.invertedIndexMap.get(word));
			} else {
				for (String file : other.invertedIndexMap.get(word).keySet()) {
					if (this.invertedIndexMap.get(word).containsKey(file) == false) {
						this.invertedIndexMap.get(word).put(file, other.invertedIndexMap.get(word).get(file));
					} else {
						this.invertedIndexMap.get(word).get(file).addAll(other.invertedIndexMap.get(word).get(file));
					}
				}
			}
		}
	}

}
