package hs.psil;

import hs.psil.expression.SExpression;

public class Psil {
	public static void main(String[] args) {
		SyntaxTreeBuilder builder = new SyntaxTreeBuilder();
		SExpression root = builder.build(System.in);
		System.out.println(root.evaluate());
	}
}
