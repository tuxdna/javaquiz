package hs.psil.expression;

import hs.psil.SymbolType;

public class MultiplySymbol extends Symbol {
	public MultiplySymbol() {
		super(SymbolType.MULTIPLY);
	}

	int evaluate(int... vals) {
		int prod = 1;
		for (int v : vals) {
			prod *= v;
		}
		return prod;
	}

	@Override
	public String toString() {
		return String.format("MultiplySymbol()");
	}
}