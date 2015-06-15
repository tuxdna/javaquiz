package hs.psil;

import hs.psil.exceptions.GenericException;
import hs.psil.expression.AddSymbol;
import hs.psil.expression.BindSymbol;
import hs.psil.expression.MultiplySymbol;
import hs.psil.expression.Number;
import hs.psil.expression.SExpression;
import hs.psil.expression.SubtractSymbol;
import hs.psil.expression.Variable;
import hs.psil.lexer.Lexer;
import hs.psil.lexer.Token;

import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

public class SyntaxTreeBuilder {
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