package hs.kwords;

import java.util.NoSuchElementException;

public class TokenStream {
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