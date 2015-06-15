package hs.psil;

import hs.psil.exceptions.NoSuchKeyException;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	private Map<String, Integer> map = new HashMap<String, Integer>();

	public int get(String name) {
		if (map.containsKey(name))
			return map.get(name);
		else {
			throw new NoSuchKeyException(name);
		}
	}

	public void put(String name, int value) {
		map.put(name, value);
	}
}