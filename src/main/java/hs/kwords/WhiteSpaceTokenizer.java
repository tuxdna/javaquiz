package hs.kwords;

import java.io.InputStream;
import java.util.Scanner;

public class WhiteSpaceTokenizer extends Tokenizer {
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