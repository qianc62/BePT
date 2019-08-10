package org.processmining.framework.models.coloredcontrolflownet.expression;

import java.util.List;

public class FunctionCallExpression implements Expression {

	public final String id;
	public final List<Expression> arguments;

	public FunctionCallExpression(String id, List<Expression> arguments) {
		this.id = id;
		this.arguments = arguments;
	}

}
