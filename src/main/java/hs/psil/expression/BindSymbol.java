package hs.psil.expression;

import hs.psil.SymbolTable;

public class BindSymbol extends Symbol {
	SymbolTable symtab;

	public BindSymbol(SymbolTable symtab) {
		super(SymbolType.BIND);
		this.symtab = symtab;
	}

	int evaluate(String name, int value) {
		symtab.put(name, value);
		return value;
	}

	@Override
	public String toString() {
		return String.format("BindSymbol()");
	}
}