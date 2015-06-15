package hs.psil.expression;

import hs.psil.exceptions.GenericException;
import hs.psil.exceptions.NoSuchKeyException;

import java.util.ArrayList;
import java.util.List;

public class SExpression extends Expression {
	public List<Expression> expressions = new ArrayList<Expression>();

	public int evaluate() {
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