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

package org.processmining.analysis.ltlchecker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.analysis.ltlchecker.formulatree.AlwaysNode;
import org.processmining.analysis.ltlchecker.formulatree.AndNode;
import org.processmining.analysis.ltlchecker.formulatree.BiimpliesNode;
import org.processmining.analysis.ltlchecker.formulatree.CompNode;
import org.processmining.analysis.ltlchecker.formulatree.DateCompNode;
import org.processmining.analysis.ltlchecker.formulatree.DateValueNode;
import org.processmining.analysis.ltlchecker.formulatree.EventuallyNode;
import org.processmining.analysis.ltlchecker.formulatree.ExistsNode;
import org.processmining.analysis.ltlchecker.formulatree.ForallNode;
import org.processmining.analysis.ltlchecker.formulatree.FormulaNode;
import org.processmining.analysis.ltlchecker.formulatree.ImpliesNode;
import org.processmining.analysis.ltlchecker.formulatree.NexttimeNode;
import org.processmining.analysis.ltlchecker.formulatree.NotNode;
import org.processmining.analysis.ltlchecker.formulatree.NumberCompNode;
import org.processmining.analysis.ltlchecker.formulatree.NumberValueNode;
import org.processmining.analysis.ltlchecker.formulatree.OrNode;
import org.processmining.analysis.ltlchecker.formulatree.SetCompNode;
import org.processmining.analysis.ltlchecker.formulatree.SetValueNode;
import org.processmining.analysis.ltlchecker.formulatree.StringCompNode;
import org.processmining.analysis.ltlchecker.formulatree.StringValueNode;
import org.processmining.analysis.ltlchecker.formulatree.TreeNode;
import org.processmining.analysis.ltlchecker.formulatree.UntilNode;
import org.processmining.analysis.ltlchecker.formulatree.ValueNode;
import org.processmining.analysis.ltlchecker.parser.ASTFormulaCall;
import org.processmining.analysis.ltlchecker.parser.ASTQuantification;
import org.processmining.analysis.ltlchecker.parser.ASTStringList;
import org.processmining.analysis.ltlchecker.parser.Attribute;
import org.processmining.analysis.ltlchecker.parser.ConceptSetAttribute;
import org.processmining.analysis.ltlchecker.parser.DateAttribute;
import org.processmining.analysis.ltlchecker.parser.FormulaParameter;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.analysis.ltlchecker.parser.Node;
import org.processmining.analysis.ltlchecker.parser.NumberAttribute;
import org.processmining.analysis.ltlchecker.parser.SetAttribute;
import org.processmining.analysis.ltlchecker.parser.SimpleNode;
import org.processmining.analysis.ltlchecker.parser.StringAttribute;
import org.processmining.framework.models.ontology.OntologyCollection;

/**
 * TreeBuilder builds a formula tree from a parsetree and the name of a formula.
 * 
 * @version 0.2
 * @author HT de Beer
 */
public class TreeBuilder {

	// FIELDS

	/**
	 * The parser with the parse data from which the formula tree is build up.
	 */
	private LTLParser parser;

	/**
	 * The formula name from which a tree must be build.
	 */
	private String formulaName;

	/**
	 * The sets created from the defined set type attributes.
	 */
	private SetsSet sets;

	private OntologyCollection ontologies;

	// CONSTRUCTORS

	public TreeBuilder(LTLParser parser, String formulaName, SetsSet ss,
			OntologyCollection ontologies) {
		this.parser = parser;
		this.formulaName = formulaName;
		this.sets = ss;
		this.ontologies = ontologies;
	}

	// METHODS

	/**
	 * Build an formulatree used in the LTLChecker from a (sub) parsetree, a
	 * list with substitutions ( parameter values ) and a bindernode, that is,
	 * the node some valuenodes are bounded to, to ensure formulareferention.
	 * 
	 * @param pNode
	 *            The subtree from the parser of the `selected' formula.
	 * @param substs
	 *            A list with substitutions, that is, a list with values for the
	 *            parameters of pNode's formula.
	 * @param binder
	 *            The operator of treenode to bind a valuenode to.
	 * 
	 * @return The startnode of the formulatree created from pNode, subst and
	 *         binder.
	 */
	public TreeNode build(Node pNode, Substitutes substs, TreeNode binder) {

		TreeNode result = new TreeNode();
		SimpleNode parserNode = (SimpleNode) pNode;

		switch (parserNode.getType()) {
		// Depending on the type of parser node, a new formula node is created.

		// Formula definitions:

		case SimpleNode.FORMULA: {
			// Return the build of the props child.
			// The parameterlist of this formula is to be found in the
			// parser.
			if (parserNode.jjtGetNumChildren() > 1) {
				result = build(parserNode.jjtGetChild(1), substs, binder);
			} else {
				result = build(parserNode.jjtGetChild(0), substs, binder);
			}
			;
		}
			;
			break;

		case SimpleNode.SUBFORMULA: {
			// Return the build of the props.
			// The parameterlists can be found in parser.
			if (parserNode.jjtGetNumChildren() > 1) {
				result = build(parserNode.jjtGetChild(1), substs, binder);
			} else {
				result = build(parserNode.jjtGetChild(0), substs, binder);
			}
			;
		}
			;
			break;

		// Binary Logical Operators:

		case SimpleNode.AND: {

			AndNode node = new AndNode();
			node.setLeftChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			node.setRightChild((FormulaNode) build(parserNode.jjtGetChild(1),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.OR: {

			OrNode node = new OrNode();
			node.setLeftChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			node.setRightChild((FormulaNode) build(parserNode.jjtGetChild(1),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.IMPLIES: {

			ImpliesNode node = new ImpliesNode();
			node.setLeftChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			node.setRightChild((FormulaNode) build(parserNode.jjtGetChild(1),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.BIIMPLIES: {

			BiimpliesNode node = new BiimpliesNode();
			node.setLeftChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			node.setRightChild((FormulaNode) build(parserNode.jjtGetChild(1),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.UNTIL: {

			UntilNode node = new UntilNode();
			node.setLeftChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			node.setRightChild((FormulaNode) build(parserNode.jjtGetChild(1),
					substs, node));
			result = node;
		}
			;
			break;

		// Unary logical operators:

		case SimpleNode.NOT: {

			NotNode node = new NotNode();
			node.setChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.ALWAYS: {

			AlwaysNode node = new AlwaysNode();
			node.setChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.EVENTUALLY: {

			EventuallyNode node = new EventuallyNode();
			node.setChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.NEXTTIME: {

			NexttimeNode node = new NexttimeNode();
			node.setChild((FormulaNode) build(parserNode.jjtGetChild(0),
					substs, node));
			result = node;
		}
			;
			break;

		// Quantifications:

		case SimpleNode.FORALL: {

			ASTQuantification qNode = (ASTQuantification) parserNode;

			ForallNode node = new ForallNode();

			// Get the set the quantification is about:
			Attribute var = qNode.getDummy();
			String attr = var.getAttributeId();

			// For each element in the set, create a subtree.
			for (SetsSetItem item : sets.getSet(attr, var.getScope())) {

				// Create the setliteral, and bind the value to this
				// forall node.
				SetValueNode svn = new SetValueNode(SetValueNode.VALUE);
				svn.setValue(new SetAttribute(item.getName(),
						Attribute.LITERAL, var, item.getModelReferences()));
				svn.setBinder(node);

				Substitutes s = (Substitutes) substs.clone();
				s.add(var.getValue(), svn);
				node
						.addChild((FormulaNode) build(qNode.jjtGetChild(1), s,
								node));
			}
			result = node;
		}
			;
			break;

		case SimpleNode.EXISTS: {

			ASTQuantification qNode = (ASTQuantification) parserNode;

			ExistsNode node = new ExistsNode();

			// Get the set the quantification is about:
			Attribute var = qNode.getDummy();
			String attr = var.getAttributeId();

			// For each element in the set, creat a subtree.
			for (SetsSetItem item : sets.getSet(attr, var.getScope())) {
				SetValueNode svn = new SetValueNode(SetValueNode.VALUE);
				svn.setValue(new SetAttribute(item.getName(),
						Attribute.LITERAL, var, item.getModelReferences()));
				svn.setBinder(node);

				Substitutes s = (Substitutes) substs.clone();
				s.add(var.getValue(), svn);
				node
						.addChild((FormulaNode) build(qNode.jjtGetChild(1), s,
								node));
			}
			result = node;
		}
			;
			break;

		// Comparisons:

		case SimpleNode.EQUAL: {

			result = compOp(parserNode, substs, CompNode.EQUAL, binder);
		}
			;
			break;

		case SimpleNode.NOTEQUAL: {

			result = compOp(parserNode, substs, CompNode.NOTEQUAL, binder);
		}
			;
			break;

		case SimpleNode.LESSOREQUAL: {

			result = compOp(parserNode, substs, CompNode.LESSEREQUAL, binder);
		}
			;
			break;

		case SimpleNode.BIGGEROREQUAL: {

			result = compOp(parserNode, substs, CompNode.BIGGEREQUAL, binder);
		}
			;
			break;

		case SimpleNode.LESSER: {

			result = compOp(parserNode, substs, CompNode.LESSER, binder);
		}
			;
			break;

		case SimpleNode.BIGGER: {

			result = compOp(parserNode, substs, CompNode.BIGGER, binder);
		}
			;
			break;

		case SimpleNode.REGEXPEQUAL: {

			result = compOp(parserNode, substs, CompNode.REGEXPEQUAL, binder);
		}
			;
			break;

		case SimpleNode.IN: {

			result = compOp(parserNode, substs, CompNode.IN, binder);
		}
			;
			break;

		// NUMOPS

		case SimpleNode.PLUS: {

			NumberValueNode node = new NumberValueNode(NumberValueNode.PLUS);
			node.setLeftChild((NumberValueNode) build(
					parserNode.jjtGetChild(0), substs, node));
			node.setRightChild((NumberValueNode) build(parserNode
					.jjtGetChild(1), substs, node));
			result = node;

		}
			;
			break;

		case SimpleNode.MINUS: {

			if (parserNode.jjtGetNumChildren() == 1) {
				// unary min

				NumberValueNode node = new NumberValueNode(
						NumberValueNode.UNMIN);
				node.setLeftChild((NumberValueNode) build(parserNode
						.jjtGetChild(0), substs, node));
				result = node;

			} else {
				// binary min

				NumberValueNode node = new NumberValueNode(NumberValueNode.PLUS);
				node.setLeftChild((NumberValueNode) build(parserNode
						.jjtGetChild(0), substs, node));
				node.setRightChild((NumberValueNode) build(parserNode
						.jjtGetChild(1), substs, node));
				result = node;
			}
			;
		}
			;
			break;

		case SimpleNode.MULT: {

			NumberValueNode node = new NumberValueNode(NumberValueNode.MULT);
			node.setLeftChild((NumberValueNode) build(
					parserNode.jjtGetChild(0), substs, node));
			node.setRightChild((NumberValueNode) build(parserNode
					.jjtGetChild(1), substs, node));
			result = node;
		}
			;
			break;

		case SimpleNode.DIV: {

			NumberValueNode node = new NumberValueNode(NumberValueNode.DIV);
			node.setLeftChild((NumberValueNode) build(
					parserNode.jjtGetChild(0), substs, node));
			node.setRightChild((NumberValueNode) build(parserNode
					.jjtGetChild(1), substs, node));
			result = node;
		}
			;
			break;

		// Literals

		case SimpleNode.INT: {

			NumberValueNode node = new NumberValueNode(NumberValueNode.VALUE);
			node.setValue((NumberAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		case SimpleNode.REAL: {

			NumberValueNode node = new NumberValueNode(NumberValueNode.VALUE);
			node.setValue((NumberAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		case SimpleNode.STRING: {

			StringValueNode node = new StringValueNode(StringValueNode.VALUE);
			node.setValue((StringAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		case SimpleNode.SETSTRING: {

			SetValueNode node = new SetValueNode(SetValueNode.VALUE);
			node.setValue((SetAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		case SimpleNode.CONCEPTSET: {

			SetValueNode node = new SetValueNode(
					SetValueNode.MODEL_REFERENCE_SET, ontologies);
			node.setValue((SetAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		case SimpleNode.DATESTRING: {

			DateValueNode node = new DateValueNode(DateValueNode.VALUE);
			node.setValue((DateAttribute) parserNode.getAttribute());
			node.setBinder(binder);
			result = node;
		}
			;
			break;

		// Local Vars

		case SimpleNode.ATTRIBUTE: {

			if (parser.existsAttribute(parserNode.getName())) {
				// It is global, so build a new valuenode
				Attribute attr = parser.getAttribute(parserNode.getName());
				switch (attr.getType()) {

				case Attribute.NUMBER: {
					NumberValueNode nvn = new NumberValueNode(
							NumberValueNode.VALUE);
					nvn.setValue((NumberAttribute) attr);
					nvn.setBinder(binder);
					result = nvn;
				}
					;
					break;

				case Attribute.DATE: {
					DateValueNode nvn = new DateValueNode(DateValueNode.VALUE);
					nvn.setValue((DateAttribute) attr);
					nvn.setBinder(binder);
					result = nvn;
				}
					;
					break;

				case Attribute.STRING: {
					StringValueNode nvn = new StringValueNode(
							StringValueNode.VALUE);
					nvn.setValue((StringAttribute) attr);
					nvn.setBinder(binder);
					result = nvn;
				}
					;
					break;

				case Attribute.SET: {
					SetValueNode nvn = new SetValueNode(SetValueNode.VALUE);
					nvn.setValue((SetAttribute) attr);
					nvn.setBinder(binder);
					result = nvn;
				}
					;
					break;

				case Attribute.CONCEPTSET: {
					SetValueNode nvn = new SetValueNode(
							SetValueNode.MODEL_REFERENCE_SET, ontologies);
					nvn.setValue((SetAttribute) attr);
					nvn.setBinder(binder);
					result = nvn;
				}
					;
					break;

				}
				;

			} else {
				// It is local, so have a look in the substitutions
				ValueNode val = substs.get(parserNode.getName());
				result = val;
			}
			;
		}
			;
			break;

		// Formula calls

		case SimpleNode.USEFORMULA: {

			ASTFormulaCall useNode = (ASTFormulaCall) parserNode;
			Substitutes s = new Substitutes();
			List<FormulaParameter> params = parser.getParameters(useNode
					.getName());
			ArrayList values = useNode.getValues();

			Iterator i = values.iterator();
			Iterator<FormulaParameter> j = params.iterator();

			while (i.hasNext() && j.hasNext()) {
				// Build an ValueNode out of the argument:

				Attribute attr = (Attribute) i.next();

				if (substs.hasId(attr.getValue())) {
					// Value is a 'call' to a local renaming, so use the
					// valuenode of the substitution.
					s.add(j.next().getParam().getValue(), substs.get(attr
							.getValue()));
				} else {
					// Value is either a call to a global attribute or
					// literal, so create a new valuenode.

					switch (attr.getType()) {

					case Attribute.NUMBER: {
						NumberValueNode nvn = new NumberValueNode(
								NumberValueNode.VALUE);
						nvn.setValue((NumberAttribute) attr);
						nvn.setBinder(binder);
						s.add(j.next().getParam().getValue(), nvn);
					}
						;
						break;

					case Attribute.DATE: {
						DateValueNode nvn = new DateValueNode(
								DateValueNode.VALUE);
						nvn.setValue((DateAttribute) attr);
						nvn.setBinder(binder);
						s.add(j.next().getParam().getValue(), nvn);
					}
						;
						break;

					case Attribute.STRING: {
						StringValueNode nvn = new StringValueNode(
								StringValueNode.VALUE);
						nvn.setValue((StringAttribute) attr);
						nvn.setBinder(binder);
						s.add(j.next().getParam().getValue(), nvn);
					}
						;
						break;

					case Attribute.SET: {
						SetValueNode nvn = new SetValueNode(SetValueNode.VALUE);
						nvn.setValue((SetAttribute) attr);
						nvn.setBinder(binder);
						s.add(j.next().getParam().getValue(), nvn);
					}
						;
						break;

					case Attribute.CONCEPTSET: {
						SetValueNode nvn = new SetValueNode(
								SetValueNode.MODEL_REFERENCE_SET, ontologies);
						nvn.setValue((SetAttribute) attr);
						nvn.setBinder(binder);
						s.add(j.next().getParam().getValue(), nvn);
					}
						;
						break;

					}
					;
				}
				;
			}
			;

			SimpleNode newFormula = parser.getFormula(useNode.getName());
			result = build(newFormula, s, binder);
		}
			;
			break;

		// Propositions

		case SimpleNode.PROPOSITION: {

			result = build(parserNode.jjtGetChild(0), substs, binder);
		}
			;
			break;

		}
		;

		return result;
	}

	private TreeNode compOp(Node pNode, Substitutes substs, int compType,
			TreeNode binder) {

		TreeNode result = new TreeNode();
		SimpleNode parserNode = (SimpleNode) pNode;
		Attribute attribute = parserNode.getAttribute();
		int type = attribute.getType();

		switch (type) {

		case Attribute.NUMBER: {
			NumberCompNode node = new NumberCompNode(compType);
			node.setAttribute((NumberAttribute) attribute);
			node.setValue((NumberValueNode) build(parserNode.jjtGetChild(1),
					substs, binder));
			result = node;
		}
			;
			break;

		case Attribute.STRING: {
			StringCompNode node = new StringCompNode(compType);
			node.setAttribute((StringAttribute) attribute);
			node.setValue((StringValueNode) build(parserNode.jjtGetChild(1),
					substs, binder));
			result = node;
		}
			;
			break;

		case Attribute.DATE: {
			DateCompNode node = new DateCompNode(compType);
			node.setAttribute((DateAttribute) attribute);
			node.setValue((DateValueNode) build(parserNode.jjtGetChild(1),
					substs, binder));
			result = node;
		}
			;
			break;

		case Attribute.SET: {
			SetCompNode node = new SetCompNode(compType);

			if (substs.hasId(attribute.getValue())) {
				ValueNode value = substs.get(attribute.getValue());

				if (value instanceof SetValueNode) {
					attribute = ((SetValueNode) value).getValue();
				}
			}

			node.setAttribute((SetAttribute) attribute);

			if (compType == CompNode.IN) {

				SetValueNode valNode = new SetValueNode(SetValueNode.SET);
				valNode.setSet(((ASTStringList) parserNode.jjtGetChild(1))
						.getStrings());
				node.setValue(valNode);

			} else {

				node.setValue((SetValueNode) build(parserNode.jjtGetChild(1),
						substs, binder));

			}
			;

			result = node;
		}
			;
			break;

		case Attribute.CONCEPTSET: {
			SetCompNode node = new SetCompNode(compType);
			node.setAttribute((ConceptSetAttribute) attribute);

			if (compType == CompNode.IN) {

				SetValueNode valNode = new SetValueNode(SetValueNode.SET);
				valNode.setSet(((ASTStringList) parserNode.jjtGetChild(1))
						.getStrings());
				node.setValue(valNode);

			} else {

				node.setValue((SetValueNode) build(parserNode.jjtGetChild(1),
						substs, binder));

			}
			;

			result = node;
		}
			;
			break;

		}
		return result;
	}

}
