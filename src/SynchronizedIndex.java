import java.nio.file.Path;
import java.util.ArrayList;

public class SynchronizedIndex extends InvertedIndex {

	/** lock variable used to protect shared info during multi-threading */
	private final ReadWriteLock lock;

	/**
	 * Constructor that calls the constructor for InvertedIndex.
	 */
	public SynchronizedIndex() {
		super();
		lock = new ReadWriteLock();
	}

	@Override
	public boolean add(String word, String fileName, int position) {
		lock.lockReadWrite();

		try {
			return super.add(word, fileName, position);
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public int addAll(String[] words, String fileName, int position) {
		lock.lockReadWrite();

		try {
			return super.addAll(words, fileName, position);
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public void addAll(InvertedIndex other) {
		lock.lockReadWrite();

		try {
			super.addAll(other);
		} finally {
			lock.unlockReadWrite();
		}
	}

	@Override
	public void writeJSON(Path output) {
		lock.lockReadOnly();

		try {
			super.writeJSON(output);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public ArrayList<QueryResult> partialSearch(String[] queries) {
		lock.lockReadOnly();

		try {
			return super.partialSearch(queries);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int countWords(String word) {
		lock.lockReadOnly();

		try {
			return super.countWords(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int countFiles(String word) {
		lock.lockReadOnly();

		try {
			return super.countFiles(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public int words() {
		lock.lockReadOnly();

		try {
			return super.words();
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public boolean contains(String word) {
		lock.lockReadOnly();

		try {
			return super.contains(word);
		} finally {
			lock.unlockReadOnly();
		}
	}

	@Override
	public String toString() {
		lock.lockReadOnly();

		try {
			return super.toString();
		} finally {
			lock.unlockReadOnly();
		}
	}
}
