package hs.psil;

import static org.junit.Assert.*;
import hs.psil.Lexer;
import hs.psil.SExpression;
import hs.psil.SyntaxTreeBuilder;
import hs.psil.Token;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class PsilTest {

	@Test
	public void test() {
		String myString = "(1 2 hello)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		Lexer lexer = new Lexer(in);
		while (true) {
			try {
				Token token = lexer.next();
				if (token == null)
					break;
				else
					System.out.println("Token: " + token);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void testSyntaxTree() throws Exception {
		String myString = "(bind hello (+ 1 2))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		System.out.println(root);
		System.out.println(root.evaluate());
	}

	@Test
	public void testPlus() throws Exception {
		String myString = "(+ 2 (bind hello 2))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		System.out.println(root);
		System.out.println(root.evaluate());
	}
}
