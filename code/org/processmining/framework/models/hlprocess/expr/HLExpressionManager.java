package org.processmining.framework.models.hlprocess.expr;

import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.expr.operator.HLNotOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLOrOperator;

public class HLExpressionManager {

	/**
	 * Negates the given expression.
	 * 
	 * @param toBeNegated
	 *            root element of the expression to be negated
	 * @return the root element of the negated expression
	 */
	public static HLExpressionOperator negateExpression(
			HLDataExpression toBeNegated) {
		// make as NOT(operand1 OR operand2 .. etc.
		HLNotOperator notOp = new HLNotOperator();
		HLDataExpression clonedExpr = (HLDataExpression) toBeNegated.clone();
		notOp.addSubExpression(clonedExpr.getRootExpressionElement()
				.getExpressionNode());
		return notOp;
	}

	/**
	 * Encapsulates {@link #connectExpressionsWithOr(List)} to take a list of
	 * conditions of which the expressions should be connected.
	 */
	public static HLExpressionElement connectConditionsWithOr(
			List<HLCondition> conditions) {
		ArrayList<HLDataExpression> expressions = new ArrayList<HLDataExpression>();
		for (HLCondition cond : conditions) {
			expressions.add(cond.getExpression());
		}
		return connectExpressionsWithOr(expressions);
	}

	/**
	 * Connects the expressions of the given list of conditions with an OR
	 * operator.
	 * 
	 * @param conditions
	 *            the conditions of which the expressions should be connected
	 * @return the root element of the OR-connected expression
	 */
	public static HLExpressionElement connectExpressionsWithOr(
			List<HLDataExpression> expressions) {
		if (expressions.size() > 1) {
			HLOrOperator orOp = new HLOrOperator();
			for (HLDataExpression expr : expressions) {
				HLExpressionElement otherExpr = expr.getRootExpressionElement();
				if (otherExpr != null) {
					HLDataExpression clonedExpr = (HLDataExpression) expr
							.clone();
					orOp.addSubExpression(clonedExpr.getRootExpressionElement()
							.getExpressionNode());
				}
			}
			return orOp;
		} else {
			HLDataExpression clonedExpr = (HLDataExpression) expressions.get(0)
					.clone();
			return clonedExpr.getRootExpressionElement();
		}

	}

}
