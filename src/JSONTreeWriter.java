import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Utility class that outputs various Tree structures as JSON objects or arrays
 * to file. Designed to demonstrate basic file processing and exception
 * handling, and the JSON format.
 * 
 * You are welcome to add additional helper methods to this class. If you go
 * about this in a general way, there will be very little repeated copy/pasted
 * code in this class.
 */
public class JSONTreeWriter {

	/**
	 * Helper method to indent several times by 2 spaces each time. For example,
	 * indent(0) will return an empty string, indent(1) will return 2 spaces,
	 * and indent(2) will return 4 spaces.
	 * 
	 * <p>
	 * <em>Using this method is optional!</em>
	 * </p>
	 * 
	 * @param times
	 * @return
	 * @throws IOException
	 */
	public static String indent(int times) throws IOException {
		return times > 0 ? String.format("%" + (times * 2) + "s", " ") : "";
	}

	/**
	 * Helper method to quote text for output. This requires escaping the
	 * quotation mark " as \" for use in Strings. For example:
	 * 
	 * <pre>
	 * String text = "hello world";
	 * System.out.println(text); // output: hello world
	 * System.out.println(quote(text)); // output: "hello world"
	 * </pre>
	 * 
	 * @param text
	 *            input to surround with quotation marks
	 * @return quoted text
	 */
	public static String quote(String text) {
		return "\"" + text + "\"";
	}

	/**
	 * Writes the elements as a JSON object with nested array values to the
	 * specified output path using the "UTF-8" character set. The output is in a
	 * "pretty" format with 2 spaces per indent level.
	 * 
	 * <pre>
	 * {
	 *   "key1": {
	 *     "filename1": [
	 *       value1,
	 *       value2
	 *     ],
	 *     "filename2": [
	 *       value1
	 *     ]
	 *   },
	 *     
	 *   "key2": {
	 *     "filename1": [
	 *       value
	 *     ]
	 *   }
	 * }
	 * </pre>
	 * 
	 * <p>
	 * Note that there is not a trailing space after the second value, the key
	 * should be in quotes, and this method should NOT throw an exception.
	 * </p>
	 * 
	 * @param output
	 *            file to write
	 * @param elements
	 *            to write as a JSON array
	 * @return true if there were no problems or exceptions
	 */
	public static boolean writeInvertedIndex(Path output, TreeMap<String, TreeMap<String, TreeSet<Integer>>> elements) {
		boolean status = false;
		int mapSize = elements.size();
		int countMap = 0;
		int countInnerMap = 0;
		int countSet = 0;

		try (BufferedWriter writer = Files.newBufferedWriter(output, Charset.forName("UTF-8"))) {
			writer.write('{');
			writer.newLine();
			for (String key : elements.keySet()) {
				countMap++;
				writer.write(indent(1) + quote(key) + ": {");
				writer.newLine();

				int innerMapSize = elements.get(key).size();
				for (String fileName : elements.get(key).keySet()) {
					countInnerMap++;
					writer.write(indent(2) + quote(fileName) + ": [");
					writer.newLine();

					int setSize = elements.get(key).get(fileName).size();
					for (Integer entry : elements.get(key).get(fileName)) {
						countSet++;
						if (countSet != setSize) {
							writer.write(indent(3) + Integer.toString(entry) + ",");
							writer.newLine();
						} else {
							writer.write(indent(3) + Integer.toString(entry));
							writer.newLine();
						}
					}
					countSet = 0;

					if (countInnerMap != innerMapSize) {
						writer.write(indent(2) + "],");
						writer.newLine();
					} else {
						writer.write(indent(2) + "]");
						writer.newLine();
					}
				}
				countInnerMap = 0;

				if (countMap != mapSize) {
					writer.write(indent(1) + "},");
					writer.newLine();
				} else {
					writer.write(indent(1) + "}");
					writer.newLine();
				}

			}
			writer.write('}');
			writer.newLine();
			status = true;
		} catch (IOException x) {
			status = false;
			System.out.println("Error in JSONTreeWriter. Incorrect output path specified.");
		}

		return status;
	}

	/**
	 * Writes the elements as a JSON object with nested array values to the
	 * specified output path using the "UTF-8" character set. The output is in a
	 * "pretty" format with 2 spaces per indent level.
	 * 
	 * * @param output file to write
	 * 
	 * @param elements
	 *            to write as a JSON array
	 * @return true if there were no problems or exceptions
	 */
	public static boolean writeQueryInfo(Path output, LinkedHashMap<String, ArrayList<QueryResult>> elements) {
		boolean status = false;
		int mapSize = elements.size();
		int countMap = 0;

		try (BufferedWriter writer = Files.newBufferedWriter(output, Charset.forName("UTF-8"))) {
			writer.write('{');
			writer.newLine();
			for (String key : elements.keySet()) {
				countMap++;
				int countList = 0;
				int listSize = elements.get(key).size();

				writer.write(indent(1) + quote(key) + ": [");
				writer.newLine();

				for (QueryResult queryResult : elements.get(key)) {
					countList++;
					writer.write(indent(2) + "{");
					writer.newLine();
					writer.write(indent(3) + quote("where") + ": " + quote(queryResult.getFileName()) + ",");
					writer.newLine();
					writer.write(indent(3) + quote("count") + ": " + queryResult.getCount() + ",");
					writer.newLine();
					writer.write(indent(3) + quote("index") + ": " + queryResult.getIndex());
					writer.newLine();

					if (countList == listSize) {
						writer.write(indent(2) + "}");
						writer.newLine();
					} else {
						writer.write(indent(2) + "},");
						writer.newLine();
					}
				}
				countList = 0;

				if (countMap == mapSize) {
					writer.write(indent(1) + "]");
					writer.newLine();
				} else {
					writer.write(indent(1) + "],");
					writer.newLine();
				}
			}
			writer.write("}");
			writer.newLine();
			status = true;
		} catch (IOException e) {
			status = false;
			System.out.println("Error in JSONTreeWriter. Incorrect output path specified.");
		}

		return status;
	}
}
