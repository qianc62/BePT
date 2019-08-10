package org.processmining.framework.models.erlangnet.expression;

public class StringExpression implements Expression {

	public final String value;

	public StringExpression(String value) {
		super();
		this.value = value;
	}

	@Override
	public String toString() {
		return "\"" + value + "\"";
	}

}
