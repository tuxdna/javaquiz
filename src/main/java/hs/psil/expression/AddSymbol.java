package hs.psil.expression;


public class AddSymbol extends Symbol {
	public AddSymbol() {
		super(SymbolType.ADD);
	}

	int evaluate(int... vals) {
		int sum = 0;
		for (int v : vals) {
			sum += v;
		}
		return sum;
	}

	@Override
	public String toString() {
		return String.format("AddSymbol()");
	}
}