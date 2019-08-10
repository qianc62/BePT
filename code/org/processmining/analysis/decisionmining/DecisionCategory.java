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

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.hlprocess.expr.HLDataExpression;
import org.processmining.framework.models.hlprocess.expr.HLExpressionElement;
import org.processmining.framework.models.hlprocess.expr.operator.HLOrOperator;

/**
 * A decision category represents one alternative branch spawned by a decision
 * point. It is characterized by one or more log events, each indicating that
 * the corresponding path has been taken with respect to an event log. <br>
 * The case that there are more than one log event characterizing the decision
 * category occurs if there are invisible tasks directly following the decision
 * point (which in turn spawn further parallel or alternative paths). The list
 * of log events must then be understood as a disjunction, i.e., the occurrence
 * of each of the events in the log is sufficient to determine that that path
 * has been taken. <br>
 * A category can also be empty if there are no visible tasks contained in that
 * alternative branch (i.e., from a log perspective it cannot be determined
 * whether this path has been taken).
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionCategory {

	/**
	 * The list of log events characterizing this decision category with respect
	 * to the log.
	 */
	private ArrayList<LogEvent> myLogEvents;

	/** Parent decision point. */
	private DecisionPoint myDecisionPoint;

	/** Local ID assigning a position within the parent decision point. */
	private int myID;

	/**
	 * The expression object representing the overall decision rule for this
	 * branch
	 */
	private HLDataExpression expression;

	/**
	 * The target node of the data dependency associated to this decision
	 * category
	 */
	private ModelGraphVertex myTargetNode;

	/**
	 * Creates a new decision category for the given decision point.
	 * 
	 * @param parent
	 *            the belonging decisin point
	 * @param ID
	 *            the local ID with respect to the belonging decision point
	 * @param logEvents
	 *            the log events associated to this category
	 * @param target
	 *            the target node of the data dependency associated to this
	 *            decision category (necessary for exporting a simulation model
	 *            to CPN)
	 */
	public DecisionCategory(DecisionPoint parent, int ID,
			ArrayList<LogEvent> logEvents, ModelGraphVertex target) {
		myDecisionPoint = parent;
		myID = ID;
		myLogEvents = logEvents;
		myTargetNode = target;
		initDecisionRules();
	}

	/**
	 * Retrieves the target node of the data dependency associated to this
	 * decision category (i.e., the first transition on this alternative
	 * branch).
	 * 
	 * @return the target node of this decision class
	 */
	public ModelGraphVertex getTargetNode() {
		return myTargetNode;
	}

	/**
	 * Creates a compound ID of the form
	 * "ParentDecisionPointID.DecisionCategoryID", which can be, e.g., used for
	 * serialization.
	 * 
	 * @return the global ID of this decision category
	 */
	public String getID() {
		String result = "";
		result = result + myDecisionPoint.getID() + "." + myID;
		return result;
	}

	/**
	 * Creates a name for this decision category based on the associated log
	 * events.
	 * 
	 * @return a descriptive string containing the ID and the associated log
	 *         events for this decision category
	 */
	public String toString() {
		String result = "Branch " + getID() + getBranchSpecification();
		return result;
	}

	/**
	 * Returns the list of log events that indicate the occurrence of this
	 * alternative branch.
	 * 
	 * @return the list of log events for this branch
	 */
	public String getBranchSpecification() {
		String result = " { ";
		for (LogEvent current : myLogEvents) {
			result = result + current.getModelElementName() + "/"
					+ current.getEventType() + " ";
		}
		result = result + "}";
		return result;
	}

	/**
	 * Determines whether there is at least one log event associated to this
	 * decision category that indicates its occurrence with respect to the log.
	 * 
	 * @return <code>false</code> if there is at least one log event assigned to
	 *         this category, <code>true</code> otherwise
	 */
	public boolean isEmpty() {
		if (myLogEvents.size() == 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Determines whether the given log event is associated to this decision
	 * category.
	 * 
	 * @param toCompare
	 *            the log event to be compared with the assigned log events
	 * @return <code>true</code> if there could be a log event found in the list
	 *         that is equal to the given one, <code>false</code> otherwise
	 */
	public boolean contains(LogEvent toCompare) {
		for (LogEvent current : myLogEvents) {
			if (current.equals(toCompare)) {
				return true;
			}
		}
		// given log event is not associated to this category
		return false;
	}

	/**
	 * Retrieves the log events associated to this decision class. If the first
	 * task in an alternative branch is an invisible or duplicate task it must
	 * be traced and there may be more than one log event associated. In this
	 * case each of the log events indicates the occurrence of this decision
	 * class.
	 * 
	 * @return the list of log events
	 */
	public ArrayList<LogEvent> getAssociatedLogEvents() {
		return myLogEvents;
	}

	/**
	 * Adds the given sub rule to the expression tree (eventually, all sub rules
	 * will be connected by an OR operator).
	 * 
	 * @param rule
	 *            the sub rule to be added to the expression
	 */
	public void addDecisionRule(HLExpressionElement subRule) {
		if (expression != null) {
			HLExpressionElement root = expression.getRootExpressionElement();
			if ((root instanceof HLOrOperator) == false) {
				expression = new HLDataExpression();
				HLExpressionElement orConnector = new HLOrOperator();
				expression.setRootExpressionElement(orConnector);
				orConnector.addSubExpression(root.getExpressionNode());
				orConnector.addSubExpression(subRule.getExpressionNode());
			} else {
				root.addSubExpression(subRule.getExpressionNode());
			}
		} else {
			expression = new HLDataExpression(subRule);
		}
	}

	/**
	 * Removes all decision rules attached to this data dependency. This method
	 * should be called as soon as new rules have been calculated and should be
	 * updated.
	 */
	public void initDecisionRules() {
		expression = null;
	}

	/**
	 * Returns the decision rule for this branch as a high level data
	 * expression.
	 * 
	 * @return the expression for this decision category
	 */
	public HLDataExpression getDataExpression() {
		return expression;
	}

}
