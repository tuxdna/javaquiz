package hs.kwords;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExternalSortWordCountStrategy implements WordCountStrategy {
	private TokenStream ts;
	private final int K = 5;
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
		// phase 3 - read top K and bottom K
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
		 * Phase 3 - read top K and bottom K frequent words
		 */
		{
			File finalFile = new File(tmpPath, SORT_BY_COUNT_FILE);
			Scanner sc = new Scanner(finalFile);

			LinkedList<WordCountPair> botList = new LinkedList<WordCountPair>();
			LinkedList<WordCountPair> topList = new LinkedList<WordCountPair>();
			while (sc.hasNext()) {
				String l = sc.nextLine();
				WordCountPair e = readFromString(l);
				if (botList.size() < K) {
					botList.addLast(e);
				}

				if (topList.size() < K) {
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