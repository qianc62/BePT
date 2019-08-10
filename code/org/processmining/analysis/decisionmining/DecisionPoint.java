/**
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Copyright (c) 2003-2006 TU/e Eindhoven
 *    by Eindhoven University of Technology
 *    Department of Information Systems
 *    http://is.tm.tue.nl
 *
 ************************************************************************/

package org.processmining.analysis.decisionmining;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLChoice;
import org.processmining.framework.models.hlprocess.HLCondition;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.att.HLNominalValue;
import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.operand.HLAttributeOperand;
import org.processmining.framework.models.hlprocess.expr.operand.HLValueOperand;
import org.processmining.framework.models.hlprocess.expr.operator.HLAndOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLGreaterOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLNotEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerEqualOperator;
import org.processmining.framework.models.hlprocess.expr.operator.HLSmallerOperator;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.ui.Message;

import weka.core.Attribute;
import weka.core.FastVector;

/**
 * Represents a decision point in a model-independent way. More precisely, the
 * occurrence of one of the specified log events per category determines whether
 * the belonging alternative has been taken or not. <br>
 * Note that the model is assumed not to contain any duplicate tasks, and that
 * more complex loop semantics are ignored for this first simple version.
 * 
 * @author Anne Rozinat (a.rozinat@tm.tue.nl)
 */
public abstract class DecisionPoint {

	/** The name of the decision point (to be displayed in the UI) */
	private String myName;

	/**
	 * Represents the concept to be learned (i.e., all the alternative paths
	 * that can be taken). Each category is specified by a list of log events
	 * determining that the corresponding branch has been taken.
	 */
	private ArrayList<DecisionCategory> myTargetConcept = new ArrayList<DecisionCategory>();

	/** Contains those attributes that are within the selected scope */
	protected DecisionPointContext myContext;

	/** ID for this decision point */
	private int myID;

	/** ID counter for all decision points */
	private static int myIdCounter = 1;

	/**
	 * ID counter for the associated decision categories (i.e., children of this
	 * decision point)
	 */
	private int myCategoryCounter = 1;

	/** the enclosing analysis result frame */
	private DecisionPointAnalysisResult myParent;

	/**
	 * the node reflecting this choice construct (i.e., the source node for all
	 * data dependencies discovered for this decision point)
	 */
	protected ModelGraphVertex myNode; // TODO check how this can be aligned

	// with the place stored for a
	// DecisionPointPetriNet

	/**
	 * Creates an empty decision point.
	 * 
	 * @param name
	 *            the name of the decision point
	 * @param parent
	 *            the enclosing parent analysis result (providing access to the
	 *            model and the log being analysed)
	 */
	public DecisionPoint(String name, DecisionPointAnalysisResult parent) {
		myName = name;
		myID = myIdCounter++;
		myParent = parent;
	}

	// //////////////////// GET METHODS
	// /////////////////////////////////////////

	/**
	 * Retrieves the name for this decision point.
	 * 
	 * @return the name
	 */
	public String getName() {
		return "Choice " + myID + " \"" + myName + "\"";
	}

	/**
	 * Retrieves the ID for this decision point.
	 * 
	 * @return the ID
	 */
	public int getID() {
		return myID;
	}

	/**
	 * Retrieves the target concept associated to this decision point. That is,
	 * the list of alternative branches that can be taken from here (each
	 * containing another list of log events which all indicate that a certain
	 * path has been taken).
	 * 
	 * @return the list of alternative branches spawned from this decision point
	 */
	public List<DecisionCategory> getTargetConcept() {
		return myTargetConcept;
	}

	/**
	 * Retrieves the target concept associated to this decision point as a weka
	 * attribute. That is, an enumeration type (i.e., a nominal one) listing the
	 * name of the log event associated to each of the paths. Note that empty
	 * categories are not treated properly yet (future work).
	 * 
	 * @return the nominal weka attribute representing the target concept for
	 *         this decision point
	 */
	public Attribute getTargetConceptAsWekaAttribute() {
		Attribute targetConcept = null;
		FastVector my_nominal_values = new FastVector(myTargetConcept.size());
		for (DecisionCategory currentCat : myTargetConcept) {
			String categoryLabel = currentCat.toString();
			my_nominal_values.addElement(categoryLabel);
		}
		// add vector to attribute
		targetConcept = new Attribute("class", my_nominal_values);
		return targetConcept;
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		String result = "\n" + this.getName() + "\n";
		for (DecisionCategory current : myTargetConcept) {
			result = result + current.toString() + "\n";
		}
		return result;
	}

	/**
	 * Retrieves the context for this decision point.
	 * 
	 * @return the belonging context object
	 */
	public DecisionPointContext getContext() {
		return myContext;
	}

	/**
	 * Retrieves the enclosing parent analysis result (providing access to the
	 * model and the log being analysed).
	 * 
	 * @return the belonging parent result panel
	 */
	public DecisionPointAnalysisResult getParent() {
		return myParent;
	}

	/**
	 * Retrieves the specified decision class from this decision point.
	 * 
	 * @param index
	 *            the global ID of the decision class to be found (i.e.,
	 *            decision point index + "." + decision class index)
	 * @return the decision class if found, <code>null</code> otherwise
	 */
	public DecisionCategory getDecisionCategory(String index) {
		// remove white spaces at start and end
		index = index.trim();
		for (DecisionCategory current : myTargetConcept) {
			// compare given index with decision class ID (e.g., "1.2")
			if (index.equals(current.getID()) == true) {
				return current;
			}
		}
		// not found
		return null;
	}

	// ///////////////////// SET / ADD METHODS
	// //////////////////////////////////////////

	/**
	 * Adds a new category to the target concept.
	 * 
	 * @param logEvents
	 *            the list of log events representing the new category to be
	 *            added to this target concept
	 */
	public void addTargetCategory(ArrayList logEvents,
			ModelGraphVertex targetNode) {
		DecisionCategory newCategory = new DecisionCategory(this,
				myCategoryCounter, logEvents, targetNode);
		myTargetConcept.add(newCategory);
		myCategoryCounter++;
	}

	/**
	 * Updates the discovered data dependencies for this decision point. <br>
	 * Assumes that no "[" or ":" characters are present in the attribute names.
	 * 
	 * @param tree
	 *            the decision tree in prefix notation
	 * @param highLevelPN
	 *            the simulation model to export discovered data dependencies
	 */
	public void setDataDependencies(String tree, HLPetriNet highLevelPN) {
		// TODO - in general: better retrieve decision rules (i.e., data
		// dependencies) directly
		// from the weka tree --> requires modification of the weka sources!

		// first remove old decision rules (from previous analysis)
		cleanDataDependencies();

		// find the corresponding choice object in high level data structure
		PetriNet pn = (PetriNet) highLevelPN.getProcessModel();
		HLProcess hlProcess = highLevelPN.getHLProcess();
		ModelGraphVertex sourceNodeSimPN = pn.findPlace(myNode.getIdentifier());
		HLChoice choice = highLevelPN.findChoice(sourceNodeSimPN);

		try {
			// split the prefix notation tree into branches (corresponds to no.
			// of rules)
			String[] prefixFragments = tree.split(",\n");

			// first fragment starts with "[" followed by the attribute name and
			// ":"
			String first = prefixFragments[0];
			int startIndex = first.indexOf("["); // should be 0
			int endIndex = first.indexOf(":");

			if (endIndex > 0) {
				// get attribute name without the "[" and ":"
				String rootAttribute = first
						.substring(startIndex + 1, endIndex);
				// rest of the fragment contains leftmost attribute value
				ArrayList<HLExpressionElement> expressions = new ArrayList<HLExpressionElement>();
				expressions.add(createAttributeExpression(rootAttribute, first
						.substring(endIndex + 1), hlProcess));

				// recursively parse prefix notation of decision tree and assign
				// decision rules
				// to respective decision category
				followRules(prefixFragments, 1, rootAttribute, expressions,
						null, hlProcess);

				// create DataDependencies
				for (DecisionCategory current : myTargetConcept) {
					// get the expression related to this dependency
					// (can be converted to CPN guard condition notation from
					// there)
					HLDataExpression expression = current.getDataExpression();
					// find target node in high level model
					ModelGraphVertex targetNodeSimPN = pn
							.findTransition((Transition) current
									.getTargetNode());
					HLActivity targetAct = highLevelPN
							.findActivity(targetNodeSimPN);
					HLCondition condition = choice.getCondition(targetAct
							.getID());
					condition.setExpression(expression);
				}
			}
		} catch (Exception e) {
			Message.add("Creation of data dependencies failed.\n"
					+ e.toString(), 2);
		}
	}

	/**
	 * Recursive helper method to parse the decision rules from the weka
	 * decision tree.
	 * 
	 * @param fragments
	 *            contains all the fragments from the prefix notation
	 * @param pos
	 *            indicates the current fragment being processed
	 * @param currentAttribute
	 *            the decision attribute that is currently traced
	 * @param expressions
	 *            the list of rule fragments (currently traced)
	 * @param exprPrefix
	 *            the started rule fragment to be be expanded
	 * @param hlProcess
	 *            the high level process to retrieve attribute objects
	 * @return the index to be processed next
	 */
	private int followRules(String[] fragments, int pos,
			String currentAttribute,
			ArrayList<HLExpressionElement> expressions,
			HLExpressionElement exprPrefix, HLProcess hlProcess) {
		// process fragment at current position
		int i = pos;
		while (i < fragments.length) {
			HLExpressionElement expression;
			// check whether represents last branch on that level
			// --> "[" indicates that now the leafs (or subtrees) are listed for
			// all branches before
			if (fragments[i].indexOf("[") >= 0) { // -1 is returned if "[" is
				// not contained
				// make rule from attribute value at the beginning of this
				// fragment
				int splitIndex = fragments[i].indexOf("[");
				String attributeValue = fragments[i].substring(0, splitIndex);
				if (exprPrefix == null) {
					expression = createAttributeExpression(currentAttribute,
							attributeValue, hlProcess);
				} else {
					// connect to previous rules
					HLExpressionElement newExpr = createAttributeExpression(
							currentAttribute, attributeValue, hlProcess);
					expression = concatExpressions(exprPrefix, newExpr);
				}
				expressions.add(expression);

				// Note: according to the prefix format from weka the number of
				// started rules matches the number
				// of leafs or nodes now listed after the last branch attribute
				// value
				Iterator<HLExpressionElement> exIt = expressions.iterator();
				// remove the attribute value at the beginning of this fragment
				// and the first "["
				fragments[i] = fragments[i].substring(splitIndex + 1);
				while (exIt.hasNext()) {
					HLExpressionElement currentExpr = exIt.next();
					// if next child node is new attribute test: evaluate
					// subtree
					// (substring cannot contain "]" in that case)
					if (fragments[i].indexOf("]") < 0) { // -1 is returned if
						// "]" is not
						// contained
						// determine new attribute name from start to before ":"
						int colonIndex = fragments[i].indexOf(":");
						String newAttribute = fragments[i].substring(0,
								colonIndex);
						// determine first attribute value from after ":" until
						// end
						String attValue = fragments[i]
								.substring(colonIndex + 1);
						ArrayList<HLExpressionElement> expandedExpressions = new ArrayList<HLExpressionElement>();
						HLExpressionElement expandedExpression = new HLAndOperator();
						// clone to keep recursive iterations isolated
						HLDataExpression dataExpr = (HLDataExpression) new HLDataExpression(
								currentExpr).clone();
						HLExpressionElement clonedCurrentExpr = dataExpr
								.getRootExpressionElement();
						// now combine
						expandedExpression.addSubExpression(clonedCurrentExpr
								.getExpressionNode());
						HLExpressionElement newExpression = createAttributeExpression(
								newAttribute, attValue, hlProcess);
						expandedExpression.addSubExpression(newExpression
								.getExpressionNode());
						expandedExpressions.add(expandedExpression);

						// start recursive procedure
						i = followRules(fragments, pos + 1, newAttribute,
								expandedExpressions, currentExpr, hlProcess);
					}
					// if not: add current rule to belonging decision category
					else {
						// find decision category that belongs to this leaf node
						int curlyIndex = fragments[i].indexOf("{");
						// start reading decision class index after "Branch" und
						// stop before "{"
						String classID = fragments[i].substring(6,
								curlyIndex - 1);
						DecisionCategory decisionClass = getDecisionCategory(classID);
						decisionClass.addDecisionRule(currentExpr);

						// remove the substring that has been just processed
						splitIndex = fragments[i].indexOf("[");
						fragments[i] = fragments[i].substring(splitIndex + 1);
					}
				}
				// tree (or subtree) finished
				return i;
			} else {
				// make rule from attribute value contained in this fragment
				String attributeValue = fragments[i];
				if (exprPrefix == null) {
					expression = createAttributeExpression(currentAttribute,
							attributeValue, hlProcess);
				} else {
					// connect to previous rules
					HLExpressionElement newExpr = createAttributeExpression(
							currentAttribute, attributeValue, hlProcess);
					expression = concatExpressions(exprPrefix, newExpr);
				}
				expressions.add(expression);
			}
			// move to next fragment
			i++;
		}
		// should never be reached
		return -1;
	}

	/**
	 * Concatenates the given expressions with a new AND connector only if the
	 * prefix is not already an AND connector (in which case the new expression
	 * is directly added as a new child.
	 * 
	 * @param prefix
	 *            the existing expression to which it should be connected as AND
	 * @param newExpr
	 *            the new expression to be concatenated
	 * @return the combined expression
	 */
	private HLExpressionElement concatExpressions(HLExpressionElement prefix,
			HLExpressionElement newExpr) {
		if (prefix instanceof HLAndOperator) {
			prefix.addSubExpression(newExpr.getExpressionNode());
			return prefix;
		} else {
			HLExpressionElement andConnector = new HLAndOperator();
			HLDataExpression dataExpr = (HLDataExpression) new HLDataExpression(
					prefix).clone();
			HLExpressionElement clonedCurrentExpr = dataExpr
					.getRootExpressionElement();
			andConnector
					.addSubExpression(clonedCurrentExpr.getExpressionNode());
			andConnector.addSubExpression(newExpr.getExpressionNode());
			return andConnector;
		}
	}

	/**
	 * Creates a sub expression referring to the given attribute and parsing the
	 * operator and value from the given String.
	 * 
	 * @param attName
	 *            the name of the attribute in the high level process (will
	 *            connect first matching one)
	 * @param operatorAndValue
	 *            assumes that operator and value are separated by a space, such
	 *            as "> 500".
	 * @return the operator expression element (corresponding tree node will
	 *         have sub nodes referring to the attribute and the value)
	 */
	private HLExpressionElement createAttributeExpression(String attName,
			String operatorAndValue, HLProcess hlProcess) {
		HLAttribute att = hlProcess.findAttributeByName(attName);
		if (att == null) {
			return null;
		}
		String trimmedString = operatorAndValue.trim();
		String[] opVal = trimmedString.split(" ");
		HLExpressionElement element = null;
		// detect the operator
		if (opVal[0].equals(">")) {
			element = new HLGreaterOperator();
		} else if (opVal[0].equals(">=")) {
			element = new HLGreaterEqualOperator();
		} else if (opVal[0].equals("<")) {
			element = new HLSmallerOperator();
		} else if (opVal[0].equals("<=")) {
			element = new HLSmallerEqualOperator();
		} else if (opVal[0].equals("=")) {
			element = new HLEqualOperator();
		} else if (opVal[0].equals("!=")) {
			element = new HLNotEqualOperator();
		} else {
			// TODO raise exception and write to error log
			return null;
		}
		// add attribute as sub node to operator in tree
		HLAttributeOperand attOperand = new HLAttributeOperand(att.getID(),
				hlProcess);
		element.addSubElement(attOperand);
		// parse and add value as sub node to operatoro in tree
		HLAttributeValue val = null;
		if (att.getType() == AttributeType.Numeric) {
			try {
				int numericValue = Integer.parseInt(opVal[1]);
				val = new HLNumericValue(numericValue);
			} catch (NumberFormatException ex) {
				// TODO raise exception and write to error log
			}
		} else {
			val = new HLNominalValue(opVal[1]);
		}
		HLValueOperand valOperand = new HLValueOperand(val);
		element.addSubElement(valOperand);
		return element;
	}

	/**
	 * Removes existing data dependencies from all decision categories. This
	 * happens as soon as new rules have been calculated and should be updated.
	 */
	private void cleanDataDependencies() {
		for (DecisionCategory current : myTargetConcept) {
			current.initDecisionRules();
		}
	}
}
