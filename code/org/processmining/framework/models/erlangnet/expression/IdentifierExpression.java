package org.processmining.framework.models.erlangnet.expression;

public class IdentifierExpression implements Expression {

	public final String identifier;

	public IdentifierExpression(String identifier) {
		super();
		this.identifier = identifier;
	}

	@Override
	public String toString() {
		return identifier;
	}

}
