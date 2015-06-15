package hs.psil.expression;


public class Number extends Atom {
	int num;

	public Number(int n) {
		this.num = n;
	}

	int evaluate() {
		return num;
	}

	@Override
	public String toString() {
		return String.format("Number(%d)", num);
	}
}