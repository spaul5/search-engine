import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConcurrentInvertedIndexHelper {

	/** Work queue used to handle multithreading for this class. */
	private final WorkQueue workers;

	public ConcurrentInvertedIndexHelper(int threads) {
		workers = new WorkQueue(threads);
	}

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
	public void traverse(Path directory, SynchronizedIndex synchronizedIndex) {

		if (Files.isDirectory(directory)) {
			try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
				for (Path entry : stream) {
					traverse(entry, synchronizedIndex);
				}
			} catch (IOException e) {
				System.out.println("Incorrect input directory entered.");
			}
		} else {
			if (Files.isReadable(directory) && directory.toString().toLowerCase().endsWith(".txt")) {
				workers.execute(new Minion(directory, synchronizedIndex));
			}
		}
	}

	/**
	 * Shutsdown the work queue after all pending work is finished.
	 */
	public void shutdown() {
		workers.finish();
		workers.shutdown();
	}

	/**
	 * Handles updating inverted index
	 */
	private class Minion implements Runnable {

		private Path path;
		private SynchronizedIndex synchronizedIndex;

		public Minion(Path path, SynchronizedIndex synchronizedIndex) {
			this.path = path;
			this.synchronizedIndex = synchronizedIndex;
		}

		@Override
		public void run() {
			InvertedIndex local = new InvertedIndex();
			InvertedIndexHelper.writePath(path, local);
			synchronizedIndex.addAll(local);
		}
	}
}
