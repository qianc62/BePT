/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.protos;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;
import org.processmining.framework.models.protos.*;

import org.w3c.dom.*;

/**
 * <p>
 * Title: Protos expression
 * </p>
 * 
 * <p>
 * Description: Holds a Protos expression
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Eric Verbeek
 * @version 1.0
 */
public class ProtosExpression {
	static int ExpressionBooleanConstant = 0;
	static int ExpressionIntegerConstant = 1;
	static int ExpressionFloatConstant = 2;
	static int ExpressionStringConstant = 3;
	static int ExpressionTimeConstant = 4;
	static int ExpressionDataExpression = 5;
	static int ExpressionEnclosedExpression = 6; // Expression holding other
	// expressions
	static int ExpressionMonadicExpression = 7; // Expression with operator +
	// one expression (< expr1)
	static int ExpressionDyadicExpression = 8; // Expression with operator + two
	// expressions (expr1 >= expr2)
	static int ExpressionChoiceExpression = 9; // Expression for if, then, else
	// construction (if expr1, then
	// expr2, else expr3)
	static int ExpressionFunctionExpression = 10; // Expression with a name and
	// arguments

	private int type; // One of the values above

	private boolean booleanConstant; // BooleanConstant
	private int integerConstant; // IntegerConstant, TimeConstant
	private float floatConstant; // FloatConstant
	private String stringConstant; // StringConstant, TimeConstant,
	// DataExpression
	private String operator; // MonadicExpression, DyadicExpression
	private String name; // FunctionExpression
	private ProtosExpression expr1; // EnclosedExpression
	// MonadicExpression.restExpression
	// DyadicExpression.leftExpression
	// ChoiceExpression.conditionExpression
	private ProtosExpression expr2; // DyadicExpression.rightExpression
	// ChoiceExpression.thenExpression
	private ProtosExpression expr3; // ChoiceExpression.elseExpression
	private HashMap arguments; // FunctionExpression (from 0..N-1 to N

	// Expressions)

	public ProtosExpression() {
		expr1 = null; // Do not initialize here, that would lead to endless
		// recursion...
		expr2 = null;
		expr3 = null;
		arguments = new HashMap();
	}

	public String getName() {
		return name;
	}

	public int getType() {
		return type;
	}

	public boolean getBooleanConstant() {
		return booleanConstant;
	}

	public int getIntegerConstant() {
		return integerConstant;
	}

	public float getFloatConstant() {
		return floatConstant;
	}

	public String getStringConstant() {
		return stringConstant;
	}

	public String getOperator() {
		return operator;
	}

	public ProtosExpression getSubExpression1() {
		return expr1;
	}

	public ProtosExpression getSubExpression2() {
		return expr2;
	}

	public ProtosExpression getSubExpression3() {
		return expr3;
	}

	public HashMap getArguments() {
		return arguments;
	}

	/**
	 * Constructs an Expression object (except for its type) out of a
	 * "monadicExpression" Node.
	 * 
	 * @param expressionNode
	 *            Node The "monadicExpression" node that contains the
	 *            Expression.
	 * @return String Any error message.
	 */
	private String readXMLExportMonadicExpression(Node expressionNode) {
		String msg = "";
		NodeList nodes = expressionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Operator)) {
				operator = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.RestExpression)) {
				expr1 = new ProtosExpression();
				msg += expr1.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs an Expression object (except for its type) out of a
	 * "dyadicExpression" Node.
	 * 
	 * @param expressionNode
	 *            Node The "dyadicExpression" node that contains the Expression.
	 * @return String Any error message.
	 */
	private String readXMLExportDyadicExpression(Node expressionNode) {
		String msg = "";
		NodeList nodes = expressionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Operator)) {
				operator = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.LeftExpression)) {
				expr1 = new ProtosExpression();
				expr1.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.RightExpression)) {
				expr2 = new ProtosExpression();
				expr2.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs an Expression object (except for its type) out of a
	 * "choiceExpression" Node.
	 * 
	 * @param expressionNode
	 *            Node The "choiceExpression" node that contains the Expression.
	 * @return String Any error message.
	 */
	private String readXMLExportChoiceExpression(Node expressionNode) {
		String msg = "";
		NodeList nodes = expressionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.ConditionExpression)) {
				expr1 = new ProtosExpression();
				msg += expr1.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.ThenExpression)) {
				expr2 = new ProtosExpression();
				msg += expr2.readXMLExport(node);
			} else if (node.getNodeName().equals(ProtosString.ElseExpression)) {
				expr3 = new ProtosExpression();
				msg += expr3.readXMLExport(node);
			}
		}
		return msg;
	}

	/**
	 * Constructs an Expression object (except for its type) out of a
	 * "functionExpression" Node.
	 * 
	 * @param expressionNode
	 *            Node The "functionExpression" node that contains the
	 *            Expression.
	 * @return String Any error message.
	 */
	private String readXMLExportFunctionExpression(Node expressionNode) {
		String msg = "";
		NodeList nodes = expressionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.Name)) {
				name = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.Argument)) {
				ProtosExpression expression = new ProtosExpression();
				msg += expression.readXMLExport(node);
				Integer size = new Integer(arguments.size());
				arguments.put(size, expression);
			}
		}
		return msg;
	}

	/**
	 * Constructs an Expression object out of an expression Node.
	 * 
	 * @param expressionNode
	 *            Node The expression node that contains the Expression.
	 * @return String Any error message.
	 */
	public String readXMLExport(Node expressionNode) {
		String msg = "";
		NodeList nodes = expressionNode.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeName().equals(ProtosString.BooleanConstant)) {
				type = ExpressionBooleanConstant;
				booleanConstant = ProtosUtil.readBool(node);
			} else if (node.getNodeName().equals(ProtosString.IntegerConstant)) {
				type = ExpressionIntegerConstant;
				integerConstant = ProtosUtil.readInt(node);
			} else if (node.getNodeName().equals(ProtosString.FloatConstant)) {
				type = ExpressionFloatConstant;
				floatConstant = ProtosUtil.readFloat(node);
			} else if (node.getNodeName().equals(ProtosString.StringConstant)) {
				type = ExpressionStringConstant;
				stringConstant = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.TimeConstant)) {
				type = ExpressionTimeConstant;
				stringConstant = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(ProtosString.DataExpression)) {
				type = ExpressionDataExpression;
				// added by Mariska Netjes, the following line:
				stringConstant = ProtosUtil.readString(node);
			} else if (node.getNodeName().equals(
					ProtosString.EnclosedExpression)) {
				type = ExpressionEnclosedExpression;
				msg += expr1.readXMLExport(node);
			} else if (node.getNodeName()
					.equals(ProtosString.MonadicExpression)) {
				type = ExpressionMonadicExpression;
				msg += readXMLExportMonadicExpression(node);
			} else if (node.getNodeName().equals(ProtosString.DyadicExpression)) {
				type = ExpressionDyadicExpression;
				msg += readXMLExportDyadicExpression(node);
			} else if (node.getNodeName().equals(ProtosString.ChoiceExpression)) {
				type = ExpressionChoiceExpression;
				msg += readXMLExportChoiceExpression(node);
			} else if (node.getNodeName().equals(
					ProtosString.FunctionExpression)) {
				type = ExpressionFunctionExpression;
				msg += readXMLExportFunctionExpression(node);
			}
		}
		return msg;
	}

	/**
	 * Returns the Expression object in Protos XML Export format.
	 * 
	 * @param tag
	 *            String The tag to use for this Expression object.
	 * @return String The Expression object in Protos XML Export format.
	 */
	public String writeXMLExport(String tag) {
		String xml = "";
		xml += "<" + tag + ">";
		if (type == ExpressionBooleanConstant) {
			xml += ProtosUtil.writeBool(ProtosString.BooleanConstant,
					booleanConstant);
		} else if (type == ExpressionIntegerConstant) {
			xml += ProtosUtil.writeInt(ProtosString.IntegerConstant,
					integerConstant);
		} else if (type == ExpressionFloatConstant) {
			xml += ProtosUtil.writeFloat(ProtosString.FloatConstant,
					floatConstant);
		} else if (type == ExpressionStringConstant) {
			xml += ProtosUtil.writeString(ProtosString.StringConstant,
					stringConstant);
		} else if (type == ExpressionTimeConstant) {
			xml += ProtosUtil.writeString(ProtosString.TimeConstant,
					stringConstant);
		} else if (type == ExpressionDataExpression) {
			xml += ProtosUtil.writeString(ProtosString.DataExpression,
					stringConstant);
		} else if (type == ExpressionEnclosedExpression) {
			xml += expr1.writeXMLExport(ProtosString.EnclosedExpression);
		} else if (type == ExpressionMonadicExpression) {
			xml += "<" + ProtosString.MonadicExpression + ">";
			{
				xml += ProtosUtil.writeString(ProtosString.Operator, operator);
				xml += expr1.writeXMLExport(ProtosString.RestExpression);
			}
			xml += "<" + ProtosString.MonadicExpression + ">";
		} else if (type == ExpressionDyadicExpression) {
			xml += "<" + ProtosString.DyadicExpression + ">";
			{
				xml += expr1.writeXMLExport(ProtosString.LeftExpression);
				xml += ProtosUtil.writeString(ProtosString.Operator, operator);
				xml += expr2.writeXMLExport(ProtosString.RightExpression);
			}
			xml += "<" + ProtosString.DyadicExpression + ">";
		} else if (type == ExpressionChoiceExpression) {
			xml += "<" + ProtosString.ChoiceExpression + ">";
			{
				xml += expr1.writeXMLExport(ProtosString.ConditionExpression);
				xml += expr2.writeXMLExport(ProtosString.ThenExpression);
				xml += expr3.writeXMLExport(ProtosString.ElseExpression);
			}
			xml += "<" + ProtosString.ChoiceExpression + ">";
		} else if (type == ExpressionFunctionExpression) {
			xml += "<" + ProtosString.FunctionExpression + ">";
			{
				xml += ProtosUtil.writeString(ProtosString.Name, name);
				for (int i = 0; i < arguments.size(); i++) {
					ProtosExpression expr = (ProtosExpression) arguments
							.get(new Integer(i));
					xml += expr.writeXMLExport(ProtosString.Argument);
				}
			}
			xml += "<" + ProtosString.FunctionExpression + ">";
		}
		xml += "</" + tag + ">";
		return xml;
	}
}
