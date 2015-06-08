package hs.kwords;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;

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

public class TopBottom5 {
	public static void main(String[] args) throws FileNotFoundException {
		String stopWordsFilePath = "/home/tuxdna/work/helpshift/stop_words.txt";
		StopWordFilter stopWordFilter = new StopWordFilter(stopWordsFilePath);
		LowerCaseFilter lCaseFilter = new LowerCaseFilter();

		String inputFilePath = "/home/tuxdna/work/helpshift/works-of-shakespeare.txt";

		if (args.length >= 1) {
			inputFilePath = args[0];
		}

		InputStream is = new FileInputStream(inputFilePath);
		Tokenizer tokenizer = new EnglishTokenizer(is);
		TokenStream ts = new TokenStream(tokenizer, lCaseFilter, stopWordFilter);

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

		Arrays.sort(entries, new Comparator<WordCountPair>() {
			public int compare(WordCountPair o1, WordCountPair o2) {
				return o1.count - o2.count;
			}
		});

		System.out.println("Bottom 5");
		for (int i = 0; i < 5 && i < entries.length; i++) {
			WordCountPair e = entries[i];
			System.out.println(String.format("%s -> %d", e.word, e.count));
		}

		System.out.println("Top 5");
		for (int i = 0; i < 5 && entries.length - i >= 0; i++) {
			WordCountPair e = entries[entries.length - i - 1];
			System.out.println(String.format("%s -> %d", e.word, e.count));
		}
	}
}
