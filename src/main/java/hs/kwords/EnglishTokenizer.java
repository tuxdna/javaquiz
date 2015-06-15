package hs.kwords;

import java.io.InputStream;

public class EnglishTokenizer extends WhiteSpaceTokenizer {
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