package org.processmining.framework.models.erlangnet.statement;

import org.processmining.framework.models.erlangnet.expression.Expression;

public class AssignmentStatement implements Statement {

	public final String variable;
	public final Expression expression;
	private final Statement environment;

	public AssignmentStatement(String variable, Expression expression,
			Statement environment) {
		super();
		this.variable = variable;
		this.expression = expression;
		this.environment = environment;
	}

	@Override
	public String toString() {
		return "set(\"" + variable + "\"," + expression + "," + environment
				+ ")";
	}

}
