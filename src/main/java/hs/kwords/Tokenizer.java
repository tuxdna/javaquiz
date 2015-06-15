package hs.kwords;

import java.io.InputStream;

public abstract class Tokenizer {
	public Tokenizer(InputStream in) {

	}

	abstract public boolean hasNext();

	abstract public String next();
}