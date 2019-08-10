package org.processmining.framework.models.coloredcontrolflownet.expression;

public class IntegerExpression implements PrimitiveExpression {

	public final int value;

	public IntegerExpression(int value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "INT(" + value + ")";
	}

}
