package org.processmining.framework.models.erlang;

import org.processmining.framework.models.erlangnet.expression.Expression;

public class AssignmentStatement extends Statement {

	public final String variable;
	public final Expression expression;

	public AssignmentStatement(String variable, Expression expression) {
		super();
		this.variable = variable;
		this.expression = expression;
	}

	@Override
	public String toString(String pad) {
		return pad + variable + " = " + expression;
	}

}
