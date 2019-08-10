package org.processmining.framework.models.erlangnet.expression;

import java.util.List;

public class FunctionCallExpression implements Expression {

	public final String functionId;
	public final List<Expression> arguments;

	public FunctionCallExpression(String functionId, List<Expression> arguments) {
		super();
		this.functionId = functionId;
		this.arguments = arguments;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(functionId + "(");
		boolean first = true;
		for (Expression expression : arguments) {
			if (first)
				first = false;
			else
				builder.append(", ");
			builder.append(expression.toString());
		}
		builder.append(")");
		return builder.toString();
	}

}
