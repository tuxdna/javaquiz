package hs.psil.expression;

import hs.psil.SymbolType;

public class Symbol extends Atom {
	SymbolType symtype;

	public Symbol(SymbolType symtype) {
		this.symtype = symtype;
	}

	@Override
	int evaluate() {
		return 0;
	}
}