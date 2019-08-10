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

import org.processmining.framework.models.hlprocess.HLTypes;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;

/**
 * Represents an enum type for the type of an attribute. todo: consider to
 * replace by real enumeration type available in later Java versions
 * 
 * @author arozinat (a.rozinat@tm.tue.nl)
 */
public class DecisionAttributeType {

	/**
	 * The name of the attribute type.
	 */
	private final String name;

	/**
	 * Denotes a nominal attribute type (i.e., an enumeration type).
	 */
	public static final DecisionAttributeType NOMINAL = new DecisionAttributeType(
			"nominal");

	/**
	 * Denotes a numeric attribute type (i.e., represents a continous numeric
	 * type space).
	 */
	public static final DecisionAttributeType NUMERIC = new DecisionAttributeType(
			"numeric");

	/**
	 * The constructor is private to only allow for pre-defined attribute types.
	 * 
	 * @param name
	 *            The name of the attribute type
	 */
	private DecisionAttributeType(String name) {
		this.name = name;
	}

	/**
	 * Returns the corresponding attribute type for a high level data attribute.
	 * 
	 * @return the attribute type
	 */
	public HLTypes.AttributeType getHighLevelAttributeType() {
		// TODO - the whole DecisionAttributeType should be replaced by that
		// AttributeType enum
		if (this.name.equals(this.NOMINAL.name) == true) {
			return HLTypes.AttributeType.Nominal;
		} else if (this.name.equals(this.NUMERIC.name) == true) {
			return HLTypes.AttributeType.Numeric;
		} else {
			// should not happen
			return null;
		}
	}

	/**
	 * Get the current attribute type.
	 * 
	 * @return the current attribute type string
	 */
	public String toString() {
		return name;
	}
}
