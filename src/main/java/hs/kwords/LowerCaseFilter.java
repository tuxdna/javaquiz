package hs.kwords;

public class LowerCaseFilter implements TokenFilter {
	public String filter(String w) {
		if (w == null)
			return w;
		else
			return w.toLowerCase();
	}
}