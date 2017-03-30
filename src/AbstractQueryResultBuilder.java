import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractQueryResultBuilder {

	/**
	 * reads the lines of a path. Uses the lines as queries
	 * 
	 * @see parseLine(String line)
	 * 
	 * @param file
	 *            file of queries
	 */
	public void readPath(Path file) {
		String fileName = file.toString();

		if (fileName.toLowerCase().endsWith("txt") && Files.isReadable(file)) {

			try (BufferedReader br = Files.newBufferedReader(file, Charset.forName("UTF-8"))) {
				String line;
				while ((line = br.readLine()) != null) {
					parseLine(line);
				}
			} catch (IOException e) {
				System.out.println("Error. Query file not found.");
			}
		}
	}

	abstract void parseLine(String line);

	abstract boolean writeJSON(Path output);
}
