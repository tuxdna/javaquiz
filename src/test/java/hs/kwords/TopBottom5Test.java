package hs.kwords;

import hs.kwords.EnglishTokenizer;
import hs.kwords.LowerCaseFilter;
import hs.kwords.StopWordFilter;
import hs.kwords.TokenStream;
import hs.kwords.Tokenizer;
import hs.kwords.WhiteSpaceTokenizer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import junit.framework.TestCase;

public class TopBottom5Test extends TestCase {
	@Test
	public void testTokenizer() throws Exception {
		String myString = "This is an amazing world!";
		InputStream is = new ByteArrayInputStream(myString.getBytes());
		WhiteSpaceTokenizer tokenizer = new WhiteSpaceTokenizer(is);

		String[] parts = myString.split(" ");
		List<String> lst = new ArrayList<String>();

		while (tokenizer.hasNext()) {
			String w = tokenizer.next();
			System.out.println(w);
			lst.add(w);
		}

		for (int i = 0; i < parts.length; i++) {
			assertEquals(parts[i], lst.get(i));
		}
	}

	@Test
	public void testStopWordsFilter() throws Exception {
		String stopWordsFilePath = "/home/tuxdna/work/helpshift/stop_words.txt";
		StopWordFilter filter = new StopWordFilter(stopWordsFilePath);
		assertNull(filter.filter("is"));
		assertNull(filter.filter("an"));
		assertNotNull(filter.filter("amazing"));
	}

	@Test
	public void testTokenStream() throws Exception {
		String myString = "This is an amazing world!";
		InputStream is = new ByteArrayInputStream(myString.getBytes());
		Tokenizer tokenizer = new EnglishTokenizer(is);
		String stopWordsFilePath = "/home/tuxdna/work/helpshift/stop_words.txt";
		StopWordFilter stopWordFilter = new StopWordFilter(stopWordsFilePath);
		LowerCaseFilter lCaseFilter = new LowerCaseFilter();
		TokenStream ts = new TokenStream(tokenizer, lCaseFilter, stopWordFilter);
		while (ts.hasNext()) {
			System.out.println(ts.next());
		}
	}
}
