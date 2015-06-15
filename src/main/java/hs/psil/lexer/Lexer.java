package hs.psil.lexer;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class Lexer {
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