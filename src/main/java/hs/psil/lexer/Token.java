package hs.psil.lexer;


public class Token {
	public String value;
	public TokenType type;

	public Token(String v, TokenType t) {
		this.value = v;
		this.type = t;
	}

	@Override
	public String toString() {
		return String.format("Token(%s, %s)", value, type);
	}
}