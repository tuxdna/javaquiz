package hs.psil;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

enum TokenType {
	NUMERIC, ALPHABETIC, LEFT_PAREN, RIGHT_PAREN, ADD, MULTIPLY, SUBTRACT, BIND
}

enum SymbolType {
	ADD, MULTIPLY, BIND, SUBTRACT
}

abstract class Expression {
	abstract int evaluate();
}

abstract class Atom extends Expression {

}

class Symbol extends Atom {
	SymbolType symtype;

	public Symbol(SymbolType symtype) {
		this.symtype = symtype;
	}

	@Override
	int evaluate() {
		return 0;
	}
}

class AddSymbol extends Symbol {
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

class MultiplySymbol extends Symbol {
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

class SubtractSymbol extends Symbol {
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

class BindSymbol extends Symbol {
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

class Number extends Atom {
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

class Variable extends Atom {
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

class GenericException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public GenericException(String message) {
		super(message);
	}
}

class NoSuchKeyException extends Error {
	private static final long serialVersionUID = 1L;

	public NoSuchKeyException(String message) {
		super(message);
	}
}

class SExpression extends Expression {
	List<Expression> expressions = new ArrayList<Expression>();

	int evaluate() {
		int i = 0;
		int rv = 0;
		if (expressions.size() == 0) {
			throw new GenericException("Invalid program: Empty S-Expression");
		}
		Expression a = expressions.get(i++);
		// System.out.println(a);
		if (a instanceof SExpression) {
			for (int j = 0; j < expressions.size(); j++) {
				Expression e = expressions.get(j);
				rv = e.evaluate();
			}
		} else if (a instanceof Symbol) {
			Symbol s = (Symbol) a;
			switch (s.symtype) {
			case BIND: {
				// b must be a variable name
				Expression b = expressions.get(1);
				if (b instanceof Variable) {
					Expression c = expressions.get(2);
					Variable bvar = (Variable) b;
					BindSymbol bs = (BindSymbol) a;
					rv = bs.evaluate(bvar.name, c.evaluate());
				}
			}
				break;
			case ADD: {
				int arr[] = new int[expressions.size() - 1];
				if (expressions.size() < 2) {
					throw new GenericException(
							"Invalid program: ADD has invalid input");
				}
				while (i < expressions.size()) {
					arr[i - 1] = expressions.get(i).evaluate();
					i++;
				}
				// System.out.println(expressions);
				rv = ((AddSymbol) s).evaluate(arr);
			}
				break;
			case MULTIPLY: {
				int arr[] = new int[expressions.size() - 1];
				if (expressions.size() < 2) {
					throw new GenericException(
							"Invalid program: MUL invalid has input");
				}
				while (i < expressions.size()) {
					arr[i - 1] = expressions.get(i).evaluate();
					i++;
				}
				rv = ((MultiplySymbol) s).evaluate(arr);
			}
				break;
			case SUBTRACT: {
				if (expressions.size() < 2) {
					throw new GenericException(
							"Invalid program: SUBTRACT invalid has input");
				}
				int arr[] = new int[expressions.size() - 1];
				while (i < expressions.size()) {
					arr[i - 1] = expressions.get(i).evaluate();
					i++;
				}
				SubtractSymbol sub = ((SubtractSymbol) s);
				if (arr.length == 1) {
					rv = sub.evaluate(arr[0]);
				} else {
					int x = arr[0];
					arr[0] = 0;
					rv = sub.evaluate(x, arr);
					// System.out.println(rv);
				}
			}
				break;
			default:
				break;
			}
		} else if (a instanceof Variable) {
			if (expressions.size() > 1) {
				throw new GenericException(
						"Invalid program: no other expression should follow");
			}
			Variable var = ((Variable) a);
			try {
				rv = var.evaluate();
			} catch (NoSuchKeyException e) {
				throw new GenericException(
						"Invalid program: variable not defined: " + var.name);
			}
		} else if (a instanceof Number) {
			if (expressions.size() > 1) {
				throw new GenericException(
						"Invalid program: no other expression should follow");
			}
			rv = ((Number) a).evaluate();
		}
		return rv;
	}

	@Override
	public String toString() {
		return String.format("(%s)", expressions);
	}

}

class SymbolTable {
	private Map<String, Integer> map = new HashMap<String, Integer>();

	public int get(String name) {
		if (map.containsKey(name))
			return map.get(name);
		else {
			throw new NoSuchKeyException(name);
		}
	}

	public void put(String name, int value) {
		map.put(name, value);
	}
}

class Token {
	String value;
	TokenType type;

	public Token(String v, TokenType t) {
		this.value = v;
		this.type = t;
	}

	@Override
	public String toString() {
		return String.format("Token(%s, %s)", value, type);
	}
}

enum LexerState {
	START, ALPHABETS, NUMBERS, LPAREN, RPAREN, ADD, MULTIPLY, END, SUBTRACT
}

class Lexer {
	// ATOM: number, variable, symbol
	//

	BufferedReader in = null;

	char[] bufc = new char[1];
	boolean bufferConsumed = false;

	public Lexer(InputStream in) {
		Reader r = new InputStreamReader(in);
		this.in = new BufferedReader(r);
	}

	private char get() throws IOException, EOFException {
		char cc;
		if (bufferConsumed) {
			cc = bufc[0];
			bufferConsumed = false;
		} else {
			char b[] = new char[1];
			int status = in.read(b);
			if (status == -1)
				throw new EOFException();
			else
				cc = b[0];
		}
		return cc;
	}

	private void unget(char c) {
		bufc[0] = c;
		bufferConsumed = true;
	}

	public Token next() throws IOException {
		StringBuffer sb = new StringBuffer();

		TokenType ttype = null;
		String tvalue = null;

		LexerState lexerState = LexerState.START;
		while (lexerState != LexerState.END) {
			char ch = 0;
			try {
				ch = get();
			} catch (EOFException ex) {
				// ex.printStackTrace();
				if (lexerState == LexerState.START) {
					return null;
				} else
					ch = 0;
			}
			switch (lexerState) {
			case START:
				// if current character is
				if (Character.isWhitespace(ch)) {
					// ignore
				} else if (ch == '(') {
					sb.append(ch);
					lexerState = LexerState.LPAREN;
				} else if (ch == ')') {
					sb.append(ch);
					lexerState = LexerState.RPAREN;
				} else if (Character.isLetter(ch)) {
					sb.append(ch);
					lexerState = LexerState.ALPHABETS;
				} else if (Character.isDigit(ch)) {
					sb.append(ch);
					lexerState = LexerState.NUMBERS;
				} else if (ch == '+') {
					sb.append(ch);
					lexerState = LexerState.ADD;
				} else if (ch == '*') {
					sb.append(ch);
					lexerState = LexerState.MULTIPLY;
				} else if (ch == '-') {
					sb.append(ch);
					lexerState = LexerState.SUBTRACT;
				}
				break;
			case LPAREN:
				unget(ch);
				ttype = TokenType.LEFT_PAREN;
				lexerState = LexerState.END;
				break;
			case RPAREN:
				unget(ch);
				ttype = TokenType.RIGHT_PAREN;
				lexerState = LexerState.END;
				break;
			case ADD:
				unget(ch);
				ttype = TokenType.ADD;
				lexerState = LexerState.END;
				break;
			case MULTIPLY:
				unget(ch);
				ttype = TokenType.MULTIPLY;
				lexerState = LexerState.END;
				break;
			case SUBTRACT:
				unget(ch);
				ttype = TokenType.SUBTRACT;
				lexerState = LexerState.END;
				break;
			case ALPHABETS:
				if (!Character.isLetter(ch)) {
					unget(ch);
					ttype = TokenType.ALPHABETIC;
					lexerState = LexerState.END;
				} else {
					sb.append(ch);
				}
				break;
			case NUMBERS:
				if (!Character.isDigit(ch)) {
					unget(ch);
					ttype = TokenType.NUMERIC;
					lexerState = LexerState.END;
					break;
				} else {
					sb.append(ch);
				}
				break;
			case END:
				break;
			default:
				break;
			}
			// System.out.println(lexerState);
		}
		tvalue = sb.toString();
		switch (ttype) {
		case ADD:
			break;
		case ALPHABETIC:
			if (tvalue.equals("bind")) {
				ttype = TokenType.BIND;
			}
			break;
		case LEFT_PAREN:
			break;
		case MULTIPLY:
			break;
		case NUMERIC:
			break;
		case RIGHT_PAREN:
			break;
		default:
			break;

		}
		Token tok = new Token(tvalue, ttype);
		return tok;
	}
}

class SyntaxTreeBuilder {
	public SExpression build(InputStream in) {
		Lexer lexer = new Lexer(in);
		SymbolTable symtab = new SymbolTable();
		Stack<SExpression> stack = new Stack<SExpression>();
		SExpression root = new SExpression();
		SExpression currentSxp = root;
		while (true) {
			try {
				Token token = lexer.next();
				// System.out.println(token);
				if (token == null)
					break;
				switch (token.type) {
				case LEFT_PAREN:
					// start a new S-Expression

					SExpression nxp = new SExpression();
					stack.push(currentSxp);
					currentSxp.expressions.add(nxp);
					currentSxp = nxp;
					break;
				case RIGHT_PAREN:
					// end current S-Expression
					if (stack.empty()) {
						throw new GenericException(
								"Invalid program: Extraneous Right Parentheses");
					}
					currentSxp = stack.pop();
					break;
				case ADD: {
					AddSymbol add = new AddSymbol();
					currentSxp.expressions.add(add);
				}
					break;
				case BIND: {
					BindSymbol bind = new BindSymbol(symtab);
					currentSxp.expressions.add(bind);
				}
					break;
				case MULTIPLY: {
					MultiplySymbol mult = new MultiplySymbol();
					currentSxp.expressions.add(mult);
				}
					break;
				case SUBTRACT: {
					SubtractSymbol mult = new SubtractSymbol();
					currentSxp.expressions.add(mult);
				}
					break;
				case ALPHABETIC: {
					Variable var = new Variable(token.value, symtab);
					currentSxp.expressions.add(var);
				}
					break;
				case NUMERIC: {
					int num = Integer.parseInt(token.value);
					Number var = new Number(num);
					currentSxp.expressions.add(var);
				}
					break;
				default:
					break;
				}
				// System.out.println("Token: " + token);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return root;
	}
}

public class Psil {
	public static void main(String[] args) {
		SyntaxTreeBuilder builder = new SyntaxTreeBuilder();
		SExpression root = builder.build(System.in);
		// System.out.println(root);
		System.out.println(root.evaluate());
	}
}
