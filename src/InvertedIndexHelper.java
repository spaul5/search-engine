import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class InvertedIndexHelper {

	/**
	 * Traverses a given directory for text files, which are then added to the
	 * inverted index.
	 * 
	 * @param directory
	 *            directory to parse
	 * @param invertedIndex
	 *            inverted index to store parsed words
	 * 
	 * @see #writePath(Path, InvertedIndex)
	 */
	public static void traverse(Path directory, InvertedIndex invertedIndex) {

		if (Files.isDirectory(directory)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
				for (Path entry : stream) {
					traverse(entry, invertedIndex);
				}
			} catch (IOException e) {
				System.out.println("Incorrect input directory entered.");
			}
		} else {
			if (Files.isReadable(directory) && directory.toString().toLowerCase().endsWith(".txt")) {
				writePath(directory, invertedIndex);
			}
		}

	}

	/**
	 * Creates the inverted index by creating an array of words from each line
	 * of file. Then uses addAll to add them to the inverted index.
	 * 
	 * @param directory
	 *            File whose words are being added to the inverted index
	 * @param invertedIndex
	 *            Inverted index object needed to call addAll.
	 * 
	 * @see InvertedIndex#addAll()
	 */
	public static void writePath(Path directory, InvertedIndex invertedIndex) {
		String fileName = directory.toString();
		int position = 1;

		try (BufferedReader br = Files.newBufferedReader(directory, Charset.forName("UTF-8"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] words = WordParser.split(line);

				if (words.length > 0) {
					position = invertedIndex.addAll(words, fileName, position);
				}
			}
		} catch (IOException e) {
			System.out.println("Error. File not found. Could not create inverted index.");
		}
	}
}
