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

import java.util.List;
import java.util.Set;

/**
 * Specifies the functionality that should be provided for any process model
 * type in order to be involved in some form of decision point analysis.
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public interface DecisionPointBuilder {

	/**
	 * Determines the number of decision points contained in the model.
	 * 
	 * @return the number of decision points in the model
	 */
	public int getNumberOfDecisionPoints();

	/**
	 * Retrieves the decision point at the given <code>index</code>.
	 * 
	 * @param index
	 *            the specified index value
	 * @return the decision point belonging to the given index if it exists,
	 *         <code>null</code> otherwise
	 */
	public DecisionPoint getDecisionPointAt(int index);

	/**
	 * Retrieves the list of decision points for this model. If no decision
	 * points have been determined yet, the list will be built before.
	 * 
	 * @return a list containing all decision points belonging to this model
	 */
	public List<DecisionPoint> getDecisionPoints();

	/**
	 * Retrieves the belonging log events of all directly preceding tasks with
	 * respect to the given decision point. Note that invisible tasks are
	 * transparently traced, without being counted as directly adjacent.
	 * 
	 * @param refPoint
	 *            the decision point for which the preceding tasks are to be
	 *            found
	 * @return a set of nodes belonging to the directly preceding tasks in the
	 *         model
	 */
	public Set getDirectPredecessors(DecisionPoint refPoint);

	/**
	 * Retrieves the belonging log events of all preceding tasks with respect to
	 * the given decision point. Note that invisible tasks do not have a log
	 * event associated and are not represented in that list.
	 * 
	 * @param refPoint
	 *            the decision point for which the preceding tasks are to be
	 *            found
	 * @return a set of nodes belonging to the preceding tasks in the model
	 */
	public Set getAllPredecessors(DecisionPoint refPoint);
}
