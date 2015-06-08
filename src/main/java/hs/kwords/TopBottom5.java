package hs.kwords;

import java.io.*;
import java.util.*;

class WordCountPair {
	String word;
	int count;

	public WordCountPair(String w, int c) {
		this.word = w;
		this.count = c;
	}
}

interface TokenFilter {
	public String filter(String w);
}

class LowerCaseFilter implements TokenFilter {
	public String filter(String w) {
		if (w == null)
			return w;
		else
			return w.toLowerCase();
	}
}

class StopWordFilter implements TokenFilter {
	Set<String> stopWords = new HashSet<String>();

	public StopWordFilter(String stopWordsPath) throws FileNotFoundException {
		File f = new File(stopWordsPath);
		if (f.exists()) {
			InputStream stream = new FileInputStream(f);
			Scanner sc = new Scanner(stream);
			while (sc.hasNext()) {
				String w = sc.next();
				stopWords.add(w);
			}
			sc.close();
		}
	}

	private boolean isStopWord(String w) {
		return stopWords.contains(w);
	}

	public String filter(String w) {
		if (isStopWord(w)) {
			return null;
		} else
			return w;
	}
}

abstract class Tokenizer {
	public Tokenizer(InputStream in) {

	}

	abstract public boolean hasNext();

	abstract public String next();
}

class WhiteSpaceTokenizer extends Tokenizer {
	Scanner sc = null;

	public WhiteSpaceTokenizer(InputStream in) {
		super(in);
		this.sc = new Scanner(in);
	}

	public boolean hasNext() {
		return sc.hasNext();
	}

	public String next() {
		return sc.next();
	}

	@Override
	protected void finalize() throws Throwable {
		if (sc != null) {
			sc.close();
		}
		super.finalize();
	}
}

class EnglishTokenizer extends WhiteSpaceTokenizer {
	public EnglishTokenizer(InputStream in) {
		super(in);
	}

	public boolean hasNext() {
		return super.hasNext();
	}

	public String next() {
		String s = super.next();
		s = s.replaceAll("[+*-.:;,!?(){}\'\"\\/\\\\]+", "");
		return s;
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}
}

class TokenStream {
	TokenFilter[] tokFilters = null;
	Tokenizer tokenizer = null;

	public TokenStream(Tokenizer tokenizer, TokenFilter... filters) {
		this.tokenizer = tokenizer;
		this.tokFilters = filters;
	}

	String nextToken = null;

	private boolean moveNext() {
		boolean found = false;
		while (!found) {
			if (tokenizer.hasNext()) {
				String w = tokenizer.next();
				String x = w;
				for (TokenFilter filter : tokFilters) {
					x = filter.filter(x);
					if (x == null)
						break;
				}
				String filtered = x;
				if (filtered != null) {
					found = true;
					nextToken = filtered;
					break;
				}
			} else {
				found = false;
				break;
			}
		}

		return found;
	}

	public boolean hasNext() {
		boolean rv = false;
		if (nextToken != null)
			rv = true;
		else {
			boolean found = moveNext();
			rv = found;
		}
		return rv;
	}

	public String next() {
		if (nextToken == null) {
			boolean found = moveNext();
			if (!found) {
				throw new NoSuchElementException(
						"The stream has no next element");
			}
		}
		String rv = nextToken;
		// mark that the token is consumed now
		nextToken = null;
		return rv;
	}
}

interface WordCountStrategy {
	public void compute() throws IOException;

	public Map<String, List<WordCountPair>> get();
}

class InMemoryWordCountStrategy implements WordCountStrategy {
	private TokenStream ts;
	Map<String, List<WordCountPair>> rv = new HashMap<String, List<WordCountPair>>();

	{
		rv.put("top", new ArrayList<WordCountPair>());
		rv.put("bottom", new ArrayList<WordCountPair>());
	}

	public InMemoryWordCountStrategy(TokenStream ts) {
		this.ts = ts;
	}

	public void compute() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		while (ts.hasNext()) {
			String token = ts.next();
			// System.out.println(token);
			int count = 1;
			if (map.containsKey(token)) {
				count += map.get(token);
			}
			map.put(token, count);
		}

		WordCountPair entries[] = new WordCountPair[map.size()];

		int c = 0;
		for (String k : map.keySet()) {
			entries[c] = new WordCountPair(k, map.get(k));
			c++;
		}

		Comparator<WordCountPair> comparator = new Comparator<WordCountPair>() {
			public int compare(WordCountPair o1, WordCountPair o2) {
				return o1.count - o2.count;
			}
		};

		Arrays.sort(entries, comparator);

		List<WordCountPair> bottom = new ArrayList<WordCountPair>();
		for (int i = 0; i < 5 && i < entries.length; i++) {
			WordCountPair e = entries[i];
			bottom.add(e);
		}

		List<WordCountPair> top = new ArrayList<WordCountPair>();
		for (int i = 0; i < 5 && entries.length - i >= 0; i++) {
			WordCountPair e = entries[entries.length - i - 1];
			top.add(e);
		}

		rv.put("top", top);
		rv.put("bottom", bottom);
	}

	public Map<String, List<WordCountPair>> get() {
		return rv;
	}
}

class ExternalSortWordCountStrategy implements WordCountStrategy {
	private TokenStream ts;
	private Comparator<WordCountPair> wordComparator = new Comparator<WordCountPair>() {
		public int compare(WordCountPair o1, WordCountPair o2) {
			return o1.word.compareTo(o2.word);
		}
	};

	private Comparator<WordCountPair> countComparator = new Comparator<WordCountPair>() {
		public int compare(WordCountPair o1, WordCountPair o2) {
			return o1.count - o2.count;
		}
	};

	private File tmpPath = null;
	private Map<String, List<WordCountPair>> rv = new HashMap<String, List<WordCountPair>>();

	{
		rv.put("top", new ArrayList<WordCountPair>());
		rv.put("bottom", new ArrayList<WordCountPair>());

		tmpPath = new File("tmp/");
		if (!tmpPath.exists())
			tmpPath.mkdir();
	}

	public ExternalSortWordCountStrategy(TokenStream ts) {
		this.ts = ts;
	}

	private String fileNameForPart(int partNumber) {
		String fname = String.format("%05d.part", partNumber);
		return fname;
	}

	private File fileForPart(int partNumber) {
		String fname = fileNameForPart(partNumber);
		return new File(tmpPath, fname);
	}

	private void writeSortedEntries(int partNumber,
			List<WordCountPair> entries, Comparator<WordCountPair> comparator)
			throws FileNotFoundException {
		String fname = fileNameForPart(partNumber);
		File outFile = new File(tmpPath, fname);
		PrintWriter writer = new PrintWriter(outFile);
		Collections.sort(entries, comparator);
		for (WordCountPair p : entries) {
			writer.println(String.format("%s\t%d", p.word, p.count));
		}
		writer.close();
	}

	private WordCountPair readFromString(String s) {
		String[] entries = s.split("\t");
		String word = entries[0];
		int count = Integer.parseInt(entries[1]);
		return new WordCountPair(word, count);
	}

	private void write(PrintWriter writer, WordCountPair p) {
		writer.println(String.format("%s\t%d", p.word, p.count));

	}

	private void merge(int pn1, int pn2, File targetFile,
			Comparator<WordCountPair> comparator) throws IOException {
		File f1 = fileForPart(pn1);
		File f2 = fileForPart(pn2);

		Scanner sc1 = new Scanner(f1);
		Scanner sc2 = new Scanner(f2);
		PrintWriter writer = new PrintWriter(targetFile);
		WordCountPair p1 = null;
		WordCountPair p2 = null;

		if (sc1.hasNext())
			p1 = readFromString(sc1.nextLine());
		else {
			p1 = null;
		}
		if (sc2.hasNext())
			p2 = readFromString(sc2.nextLine());
		else {
			p2 = null;
		}

		while (true) {
			while (p1 != null && p2 != null && comparator.compare(p1, p2) < 0) {
				write(writer, p1);
				if (sc1.hasNext())
					p1 = readFromString(sc1.nextLine());
				else {
					p1 = null;
				}
			}

			while (p1 != null && p2 != null && comparator.compare(p1, p2) >= 0) {
				write(writer, p2);
				if (sc2.hasNext())
					p2 = readFromString(sc2.nextLine());
				else {
					p2 = null;
				}
			}

			if (p1 == null || p2 == null) {
				break;
			}
		}

		if (p1 != null) {
			// write remaining p1 entries
			write(writer, p1);
			while ((sc1.hasNext())) {
				p1 = readFromString(sc1.nextLine());
				write(writer, p1);
			}
		}

		if (p2 != null) {
			// write remaining p2 entries
			write(writer, p2);
			while ((sc2.hasNext())) {
				p2 = readFromString(sc2.nextLine());
				write(writer, p2);
			}
		}
		writer.close();
		sc2.close();
		sc1.close();
	}

	public void compute() throws IOException {
		String WCFINAL = "wordcount.final";
		String SORT_BY_COUNT_FILE = "sortByCount.final";
		// phase 1 - read parts, sort by word, merge all parts, calculate counts
		// phase 2 - read parts, sort by count, merge all parts
		// phase 3 - read top 5 and bottom 5
		{
			/*
			 * Phase 1 - read parts, sort by word, merge all parts, calculate
			 * counts
			 */
			int M = 10000;
			List<WordCountPair> entries = new ArrayList<WordCountPair>();

			int tokenCount = 0;
			int numberOfParts = 0;
			while (ts.hasNext()) {
				String token = ts.next();
				// System.out.println(token);
				WordCountPair e = new WordCountPair(token, 1);
				entries.add(e);
				tokenCount++;
				// after every M words, create a new file and spill to disk
				if (entries.size() == M) {
					numberOfParts++;
					writeSortedEntries(numberOfParts, entries, wordComparator);
					entries.clear();
				}
			}

			// write final semi-filled block
			if (entries.size() > 0) {
				numberOfParts++;
				writeSortedEntries(numberOfParts, entries, wordComparator);
			}

			// merge all files
			// pick two files at a time and merge them
			int partsToProcess = numberOfParts;
			while (partsToProcess > 1) {
				System.out.println("Parts to process: " + partsToProcess);
				int i = 1;
				int numPairs = partsToProcess / 2;
				System.out.println("Num pairs: " + numPairs);
				for (; i <= numPairs; i++) {
					int part1 = 2 * i - 1;
					int part2 = 2 * i;
					String mergedFile = String.format("%05d.merge", i);
					File targetFile = new File(tmpPath, mergedFile);
					System.out.println(String.format("part1: %d, part2: %d",
							part1, part2));

					merge(part1, part2, targetFile, wordComparator);

					// delete intermediary files
					File part1File = fileForPart(part1);
					File part2File = fileForPart(part2);
					part1File.delete();
					part2File.delete();
					File newPart = fileForPart(i);
					targetFile.renameTo(newPart);
				}

				if (partsToProcess % 2 == 1) {
					System.out.println("Odd one out! i = " + i);
					File targetFile = fileForPart(partsToProcess);
					File newPart = fileForPart(i);
					targetFile.renameTo(newPart);
					partsToProcess = i;
				} else {
					partsToProcess = numPairs;
				}
			}

			System.out.println("Token Count: " + tokenCount);

			// calculate word counts
			File finalPartFile = new File(tmpPath, fileNameForPart(1));
			Scanner sc = new Scanner(finalPartFile);
			WordCountPair pfirst = null;
			WordCountPair pnext = null;

			File finalWordCountFile = new File(tmpPath, WCFINAL);
			PrintWriter writer = new PrintWriter(finalWordCountFile);
			int count = 0;
			if (sc.hasNext()) {
				String l = sc.nextLine();
				pfirst = readFromString(l);
				count++;
			}
			do {
				if (sc.hasNext()) {
					String l = sc.nextLine();
					pnext = readFromString(l);
					if (wordComparator.compare(pfirst, pnext) == 0) {
						count++;
					} else {
						// write word,count
						write(writer, new WordCountPair(pfirst.word, count));
						pfirst = pnext;
						count = 1;
					}
				} else {
					write(writer, new WordCountPair(pfirst.word, count));
					pnext = null;
				}
			} while (pnext != null);

			writer.close();
			sc.close();
		}

		/*
		 * Phase 2 - read parts, sort by word, merge all parts, calculate counts
		 */

		{
			int M = 100;
			List<WordCountPair> entries = new ArrayList<WordCountPair>();
			File finalWordCountFile = new File(tmpPath, WCFINAL);
			Scanner scfinal = new Scanner(finalWordCountFile);
			int numberOfParts = 0;
			while (scfinal.hasNext()) {
				String l = scfinal.nextLine();
				WordCountPair e = readFromString(l);
				entries.add(e);
				// after every M words, create a new file and spill to disk
				if (entries.size() == M) {
					numberOfParts++;
					writeSortedEntries(numberOfParts, entries, countComparator);
					entries.clear();
				}
			}
			scfinal.close();

			// write final semi-filled block
			if (entries.size() > 0) {
				numberOfParts++;
				writeSortedEntries(numberOfParts, entries, countComparator);
			}

			// merge all files
			// pick two files at a time and merge them
			int partsToProcess = numberOfParts;
			while (partsToProcess > 1) {
				System.out.println("Parts to process: " + partsToProcess);
				int i = 1;
				int numPairs = partsToProcess / 2;
				System.out.println("Num pairs: " + numPairs);
				for (; i <= numPairs; i++) {
					int part1 = 2 * i - 1;
					int part2 = 2 * i;
					String mergedFile = String.format("%05d.merge", i);
					File targetFile = new File(tmpPath, mergedFile);
					System.out.println(String.format("part1: %d, part2: %d",
							part1, part2));

					merge(part1, part2, targetFile, countComparator);

					// delete intermediary files
					File part1File = fileForPart(part1);
					File part2File = fileForPart(part2);
					part1File.delete();
					part2File.delete();
					File newPart = fileForPart(i);
					targetFile.renameTo(newPart);
				}

				if (partsToProcess % 2 == 1) {
					System.out.println("Odd one out! i = " + i);
					File targetFile = fileForPart(partsToProcess);
					File newPart = fileForPart(i);
					targetFile.renameTo(newPart);
					partsToProcess = i;
				} else {
					partsToProcess = numPairs;
				}
			}

			File finalFile = new File(tmpPath, SORT_BY_COUNT_FILE);
			File targetFile = fileForPart(1);
			targetFile.renameTo(finalFile);
		}

		/*
		 * Phase 3 - read top 5 and bottom 5 frequent words
		 */
		{
			File finalFile = new File(tmpPath, SORT_BY_COUNT_FILE);
			Scanner sc = new Scanner(finalFile);

			LinkedList<WordCountPair> botList = new LinkedList<WordCountPair>();
			LinkedList<WordCountPair> topList = new LinkedList<WordCountPair>();
			while (sc.hasNext()) {
				String l = sc.nextLine();
				WordCountPair e = readFromString(l);
				if (botList.size() < 5) {
					botList.addLast(e);
				}

				if (topList.size() < 5) {
					topList.addLast(e);
				} else {
					topList.removeFirst();
					topList.addLast(e);
				}

			}
			sc.close();

			rv.put("top", topList);
			rv.put("bottom", botList);

		}
	}

	public Map<String, List<WordCountPair>> get() {
		return rv;
	}
}

enum WCStrategy {
	INMEMORY, EXTERNALSORT
}

public class TopBottom5 {

	public static void main(String[] args) throws FileNotFoundException {
		String stopWordsFilePath = "stop_words.txt";
		StopWordFilter stopWordFilter = new StopWordFilter(stopWordsFilePath);
		LowerCaseFilter lCaseFilter = new LowerCaseFilter();

		String inputFilePath = "works-of-shakespeare.txt";
		WCStrategy wcstrategy = WCStrategy.EXTERNALSORT;

		if (args.length >= 1) {
			inputFilePath = args[0];
			if (!new File(inputFilePath).exists()) {
				System.err
						.println("This file does not exist: " + inputFilePath);
				System.exit(-1);
			}
		}

		if (args.length >= 2) {
			String strategyArg = args[1];
			if (strategyArg.equalsIgnoreCase("inmemory")) {
				wcstrategy = WCStrategy.INMEMORY;
			} else if (strategyArg.equalsIgnoreCase("externalsort")) {
				wcstrategy = WCStrategy.EXTERNALSORT;
			} else {
				System.err.println("Invalid argument for strategy: "
						+ strategyArg);
				System.exit(-1);
			}
		}

		InputStream is = new FileInputStream(inputFilePath);
		Tokenizer tokenizer = new EnglishTokenizer(is);
		TokenStream ts = new TokenStream(tokenizer, lCaseFilter, stopWordFilter);

		// assign straegy
		WordCountStrategy strategy = null;
		switch (wcstrategy) {
		case EXTERNALSORT:
			strategy = new ExternalSortWordCountStrategy(ts);
			break;
		case INMEMORY:
			strategy = new InMemoryWordCountStrategy(ts);
			break;
		default:
			strategy = new ExternalSortWordCountStrategy(ts);
			break;
		}

		// compute frequent word counts
		try {
			strategy.compute();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		Map<String, List<WordCountPair>> rv = strategy.get();

		List<WordCountPair> top = rv.get("top");
		List<WordCountPair> bottom = rv.get("bottom");

		System.out.println();
		System.out.println();

		System.out.println("Most frequent 5 words");
		for (WordCountPair e : top) {
			System.out.println(String.format("%s -> %d", e.word, e.count));
		}

		System.err.println();

		System.out.println("Least frequent 5 words");
		for (WordCountPair e : bottom) {
			System.out.println(String.format("%s -> %d", e.word, e.count));
		}
	}
}
