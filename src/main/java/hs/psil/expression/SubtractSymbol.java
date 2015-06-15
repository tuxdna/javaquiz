package hs.psil.expression;

import hs.psil.SymbolType;

public class SubtractSymbol extends Symbol {
	public SubtractSymbol() {
		super(SymbolType.SUBTRACT);
	}

	int evaluate(int x, int... vals) {
		int rv = x;
		for (int y : vals) {
			rv -= y;
		}
		return rv;
	}

	int evaluate(int x) {
		return -x;
	}

	@Override
	public String toString() {
		return String.format("SubtractSymbol()");
	}
}