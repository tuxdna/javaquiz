package hs.kwords;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface WordCountStrategy {
	public void compute() throws IOException;

	public Map<String, List<WordCountPair>> get();
}