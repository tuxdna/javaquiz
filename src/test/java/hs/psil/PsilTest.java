package hs.psil;

import static org.junit.Assert.*;
import hs.psil.exceptions.GenericException;
import hs.psil.expression.SExpression;
import hs.psil.lexer.Lexer;
import hs.psil.lexer.Token;
import hs.psil.lexer.TokenType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class PsilTest {

	@Test
	public void test() {
		String myString = "123"; // "(1 2 hello)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		Lexer lexer = new Lexer(in);
		try {
			Token token = lexer.next();
			assertEquals(token.type, TokenType.NUMERIC);
			assertEquals(token.value, "123");
		} catch (IOException e) {
			assertTrue(false);
		}
	}

	@Test
	public void testSyntaxTree() throws Exception {
		String myString = "(bind hello (+ 1 2))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 3);
	}

	@Test
	public void testMultiExpression() throws Exception {
		String myString = "(bind length 10)  \n\t(bind breadth 10)  \n\t(* length breadth)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 100);
	}

	@Test
	public void testPlusWithBind() throws Exception {
		String myString = "(+ 2 (bind hello 2))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 4);
	}

	@Test
	public void testInvalidPlus() throws Exception {
		String myString = "(+ (* 1 2) + (* 3 4))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		try {
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testNumber() throws Exception {
		String myString = "123";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 123);
	}

	@Test
	public void testPlus() throws Exception {
		String myString = "(+ 1 2)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 3);
	}

	@Test
	public void testMul() throws Exception {
		String myString = "(* 3 4)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 12);
	}

	@Test
	public void testMulMulti() throws Exception {
		String myString = "(* 5 4 3 2 1)";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 120);
	}

	@Test
	public void testMultiLinePlusMul() throws Exception {
		String myString = "(+ 1  \n\t  (* 2 3) \n\t (* 4 2))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 15);
	}

	@Test
	public void testBindSimple() throws Exception {
		String myString = "(bind radius 12)"; // 12
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 12);
	}

	@Test
	public void testBindSequence() throws Exception {
		String myString = "(bind length 10)  \n\t(bind breadth 10)  \n\t(* length breadth)"; // 100
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 100);
	}

	@Test
	public void testMoreThanOneExpressions() throws Exception {
		String myString = "(bind length 10) (+ 1 2 3 4) (bind breadth 10) (* length breadth)"; // 100
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 100);
	}

	@Test
	public void testBindPlusMul() throws Exception {
		String myString = "(bind length 10) \n\t (bind breadth (+ length 1)) \n\t (bind length 11) \n\t (* length breadth)"; // 121
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 121);
	}

	@Test
	public void testBindBindBindPlus() throws Exception {
		String myString = "(bind a 10) \n\t (bind b a) \n\t (bind a 11) \n\t (+ a b)"; // 21
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(root.evaluate(), 21);
	}

	@Test
	public void testEmptySExpression() throws Exception {
		String myString = "()";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		try {
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testInvalidIdentifier() throws Exception {
		String myString = "a";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		try {
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testTrailingParen() throws Exception {
		String myString = "(+ 1) 2)"; // Invalid program
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		try {
			SExpression root = streeBuilder.build(in);
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testMultiNumbers() throws Exception {
		String myString = "(1 2 3 4)"; // Invalid program
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		try {
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testPlusInvalidArg() throws Exception {
		String myString = "(+ (* 1 2) + (* 3 4))";
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		try {
			root.evaluate();
		} catch (GenericException e) {
			assertTrue(true);
		}
	}

	@Test
	public void testNegate() throws Exception {
		String myString = "(- 10)"; // Invalid program
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(-10, root.evaluate());
	}

	@Test
	public void testSubtract() throws Exception {
		String myString = "(- 10 1)"; // Invalid program
		InputStream in = new ByteArrayInputStream(myString.getBytes());
		SyntaxTreeBuilder streeBuilder = new SyntaxTreeBuilder();
		SExpression root = streeBuilder.build(in);
		assertEquals(9, root.evaluate());
	}

}
