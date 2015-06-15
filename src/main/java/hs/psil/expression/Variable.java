package hs.psil.expression;

import hs.psil.SymbolTable;

public class Variable extends Atom {
	String name;
	SymbolTable symtab;

	public Variable(String n, SymbolTable symtab) {
		this.name = n;
		this.symtab = symtab;
	}

	int evaluate() {
		return symtab.get(name);
	}

	@Override
	public String toString() {
		return String.format("Variable(%s)", name);
	}
}