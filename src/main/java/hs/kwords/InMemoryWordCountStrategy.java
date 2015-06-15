package hs.kwords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryWordCountStrategy implements WordCountStrategy {
	private TokenStream ts;
	private final int K = 5;
	Map<String, List<WordCountPair>> rv = new HashMap<String, List<WordCountPair>>();

	{
		rv.put("top", new ArrayList<WordCountPair>());
		rv.put("bottom", new ArrayList<WordCountPair>());
	}

	public InMemoryWordCountStrategy(TokenStream ts) {
		this.ts = ts;
	}

	public void compute() {
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

		Comparator<WordCountPair> comparator = new Comparator<WordCountPair>() {
			public int compare(WordCountPair o1, WordCountPair o2) {
				return o1.count - o2.count;
			}
		};

		Arrays.sort(entries, comparator);

		List<WordCountPair> bottom = new ArrayList<WordCountPair>();
		for (int i = 0; i < K && i < entries.length; i++) {
			WordCountPair e = entries[i];
			bottom.add(e);
		}

		List<WordCountPair> top = new ArrayList<WordCountPair>();
		for (int i = 0; i < K && entries.length - i >= 0; i++) {
			WordCountPair e = entries[entries.length - i - 1];
			top.add(e);
		}

		rv.put("top", top);
		rv.put("bottom", bottom);
	}

	public Map<String, List<WordCountPair>> get() {
		return rv;
	}
}