package hs.kwords;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class StopWordFilter implements TokenFilter {
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