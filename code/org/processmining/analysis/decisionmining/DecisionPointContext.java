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

import javax.swing.JPanel;

import weka.core.FastVector;

/**
 * A decision point context keeps track of the case attributes that have been
 * selected for analysis of the belonging decision point (and their type).
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public interface DecisionPointContext {

	/**
	 * Data type for specifying the different scoping rules that can be applied
	 * during the decision point analysis.
	 */
	public enum AttributeSelectionScope {

		ALL_BEFORE("all before"), JUST_BEFORE("just before"), WHOLE_CASE(
				"whole case");

		/**
		 * Constructor for the enumeration type TimeUnit
		 */
		private AttributeSelectionScope(String name) {
			myName = name;
		}

		/**
		 * Returns a list with all possible values
		 * 
		 * @param init
		 *            the given default will be the first in the list
		 * @return ArrayList a list with all possible values
		 */
		public static ArrayList<AttributeSelectionScope> getValues(
				AttributeSelectionScope init) {
			ArrayList<AttributeSelectionScope> returnValues = new ArrayList<AttributeSelectionScope>();

			// TODO : think of smarter way to initialize
			if (init == AttributeSelectionScope.ALL_BEFORE) {
				returnValues.add(AttributeSelectionScope.ALL_BEFORE);
				returnValues.add(AttributeSelectionScope.JUST_BEFORE);
				returnValues.add(AttributeSelectionScope.WHOLE_CASE);
			} else if (init == AttributeSelectionScope.WHOLE_CASE) {
				returnValues.add(AttributeSelectionScope.WHOLE_CASE);
				returnValues.add(AttributeSelectionScope.ALL_BEFORE);
				returnValues.add(AttributeSelectionScope.JUST_BEFORE);
			} else if (init == AttributeSelectionScope.JUST_BEFORE) {
				returnValues.add(AttributeSelectionScope.JUST_BEFORE);
				returnValues.add(AttributeSelectionScope.ALL_BEFORE);
				returnValues.add(AttributeSelectionScope.WHOLE_CASE);
			}
			// if, e.g., no init is given
			else {
				returnValues.add(AttributeSelectionScope.ALL_BEFORE);
				returnValues.add(AttributeSelectionScope.JUST_BEFORE);
				returnValues.add(AttributeSelectionScope.WHOLE_CASE);
			}

			return returnValues;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Enum#toString()
		 */
		public String toString() {
			return myName;
		}

		// the string to be displayed
		private String myName;
	}

	/**
	 * Returns the current attributes view for the belonging decision point.
	 * 
	 * @return the panel reflecting the content of the attributes view tab for
	 *         the belonging decision point
	 */
	public JPanel getAttributesViewPanel();

	/**
	 * Returns the current model view for the belonging decision point.
	 * 
	 * @return the panel reflecting the content of the model view tab for the
	 *         belonging decision point
	 */
	public JPanel getModelViewPanel();

	/**
	 * Returns the current result view for the belonging decision point.
	 * 
	 * @return the panel reflecting the content of the results tab for the
	 *         belonging decision point
	 */
	public JPanel getResultViewPanel();

	/**
	 * Sets the new result view content for the belonging decision point.
	 * 
	 * @param the
	 *            panel containing the new result content
	 */
	public void setResultViewPanel(JPanel newResult);

	/**
	 * Returns the current result evaluation view for the belonging decision
	 * point.
	 * 
	 * @return the panel reflecting the content of the evaluation tab for the
	 *         belonging decision point
	 */
	public JPanel getEvaluationViewPanel();

	/**
	 * Sets the new evaluation view content for the belonging decision point.
	 * 
	 * @param the
	 *            panel containing the new evaluation content
	 */
	public void setEvaluationViewPanel(JPanel newResult);

	/**
	 * Returns a vector of those attributes that have been selected for analysis
	 * (can be directly used to build up a data set).
	 * 
	 * @return a vector of {link Attribute Attribute} objects
	 */
	public FastVector getAttributeInfo();

	/**
	 * Returns the current attribute selection scope as a String.
	 * 
	 * @return the currently selected scope for looking at the data attributes
	 *         in the log
	 */
	public AttributeSelectionScope getAttributeSelectionScope();
}
