package hs.kwords;

import java.io.*;
import java.util.*;

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
