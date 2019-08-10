/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess;

import java.io.IOException;
import java.io.Writer;

import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.ui.Message;

/**
 * An attribute represents an attribute of the high level process. This may be,
 * for example, an aggregate case data attribute that is available to activities
 * in the process (i.e., they can read it and provide new values).
 * <p>
 * Each data attribute may have:
 * <ul>
 * <li>a name</li>
 * <li>a current value (concrete: value currently held by the atribute)</li>
 * <li>an initial value (aggregate: default or initialization value)</li>
 * <li>a range of possible values (aggregate: including a probabilistic
 * distribution)</li>
 * </ul>
 * Note that to change the type of the attribute, you will need to create a new
 * one of the desired type and then replace it at the high level process level
 * via calling the method {@link HLProcess#replaceAttribute(HLID, HLAttribute)}.
 * 
 * @see #getAttributeType()
 * @see HLActivity
 * @see HLProcess
 */
public abstract class HLAttribute extends HLProcessElement implements Cloneable {

	/**
	 * <p>
	 * The value with which this attribute is initialized.
	 * <p>
	 * To obtain this information, this may be specified along with the type
	 * (like, e.g., in YAWL), read from the log, or otherwise defined for
	 * simulation purposes.
	 */
	protected HLAttributeValue initialValue;
	/**
	 * <p>
	 * Indicates whether an initial value is available.
	 * <p>
	 * Note that, currently, the initial value needs to be a fixed value rather
	 * than allowing for a probabilistic value range distribution.
	 * <p>
	 * If this causes problems as, for example, in a simulation setting data
	 * must be used based on its initialized value, then a value according to
	 * the possible value range distribution should be used for that.
	 */
	protected boolean useInitialValue = true;
	/**
	 * <p>
	 * The possible value range of this attribute along with a probabilistic
	 * distribution.
	 * <p>
	 * Represents which values are possible for this attribue, and how frequent
	 * or likely each value is.
	 */
	protected HLAttributeValue possibleValues;
	/**
	 * <p>
	 * The value currently held by this attribute.
	 * <p>
	 * Can be used to fill placeholder variables in data expressions with actual
	 * values in order to evaluate them.
	 */
	protected HLAttributeValue currentValue;

	/**
	 * Constructor should be called in deriving sub classes. Registers this
	 * attribute at the high level process.
	 */
	protected HLAttribute(String aName, HLProcess aProc) {
		super(aName, aProc);
		process.attributes.put(getID(), this);
	}

	/**
	 * Constructor should be called in deriving sub classes. Registers this
	 * attribute at the high level process.
	 */
	protected HLAttribute(String aName, HLProcess aProc, HLID id) {
		super(aName, aProc, id);
		process.attributes.put(getID(), this);
	}

	/**
	 * Retrieves the default value for this attribute.
	 * 
	 * @see #initialValue
	 * @return the initial value if specified, <code>null</code> otherwise
	 */
	public HLAttributeValue getInitialValue() {
		return initialValue;
	}

	/**
	 * Indicates whether an initial value is available for this attribute.
	 * 
	 * @return whether there is an initial value
	 */
	public boolean usesInitialValue() {
		return useInitialValue;
	}

	/**
	 * Retrieves the possible values and their likelihood.
	 * 
	 * @see #possibleValues
	 * @return the possible values if specified, <code>null</code> otherwise
	 */
	public HLAttributeValue getPossibleValues() {
		return possibleValues;
	}

	/**
	 * Retrieves the value currently held by this attribute.
	 * 
	 * @see #currentValue
	 * @return the current value if specified, <code>null</code> otherwise
	 */
	public HLAttributeValue getCurrentValue() {
		return currentValue;
	}

	/**
	 * Assigns a new initial value to this attribute.
	 * 
	 * @see #initialValue
	 * @param val
	 *            the new default value
	 * @return <code>true</code> if the value was successfully set,
	 *         <code>false</code> otherwise
	 */
	public boolean setInitialValue(HLAttributeValue val) {
		if (isValid(val)) {
			initialValue = val;
			return true;
		} else {
			Message.add("Refused to assign initial value for attribute "
					+ this.getName(), Message.ERROR);
			return false;
		}
	}

	/**
	 * Records whether the initial value of this attribute should be considered
	 * or not.
	 * 
	 * @param <code>true</code> if there is a valid initial value,
	 *        <code>false</code> if not
	 */
	public void useInitialValue(boolean use) {
		useInitialValue = use;
	}

	/**
	 * Assigns a new possible value distribution to this attribute.
	 * 
	 * @see #possibleValues
	 * @param values
	 *            the new value range definition
	 * @return <code>true</code> if the value was successfully set,
	 *         <code>false</code> otherwise
	 */
	public boolean setPossibleValues(HLAttributeValue values) {
		if (isValid(values)) {
			possibleValues = values;
			return true;
		} else {
			Message.add("Refused to assign possible value for attribute "
					+ this.getName(), Message.ERROR);
			return false;
		}
	}

	/**
	 * Assigns a new current value to this attribute.
	 * 
	 * @see #currentValue
	 * @param val
	 *            the new value to be held by this attribute
	 * @return <code>true</code> if the value was successfully set,
	 *         <code>false</code> otherwise
	 */
	public boolean setCurrentValue(HLAttributeValue val) {
		if (isValid(val)) {
			currentValue = val;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Method should be implemented by subclasses to perform type checks, such
	 * as, whether the given value is actually numerical for a numeric
	 * attribute, and whether it is in the possible range.
	 * 
	 * @return whether the given value is valid for this attribute
	 */
	public abstract boolean isValid(HLAttributeValue val);

	/**
	 * Determines the type of this attribute.
	 * 
	 * @return the type
	 */
	public abstract AttributeType getType();

	/**
	 * Makes a deep copy of this object while the ID remains the same. <br>
	 * Note that this method needs to be extended as soon as there are
	 * attributes added to the class which are not primitive or immutable.
	 */
	public Object clone() {
		HLAttribute o = null;
		try {
			o = (HLAttribute) super.clone();
			if (currentValue != null) {
				o.currentValue = (HLAttributeValue) currentValue.clone();
			}
			if (initialValue != null) {
				o.initialValue = (HLAttributeValue) initialValue.clone();
			}
			if (possibleValues != null) {
				o.possibleValues = (HLAttributeValue) possibleValues.clone();
			}
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	// TODO Anne: move to visualization classes (out of high level data
	// structure)
	/**
	 * Writes the highlevel data attribute to dot. <br>
	 * The general idea is that the relevant information of the highlevel data
	 * attribute is written in a box and if needed can be connected to another
	 * node in the dot file. In that case the connection has to be an undirected
	 * line.
	 * 
	 * @param boxId
	 *            the identifier of the box (in the DOT file) in which the
	 *            relevant information of the highlevel data attribute will be
	 *            written.
	 * @param nodeId
	 *            the identifier of the node (in the DOT file) to which the box
	 *            that will be created has to be connected. <code>""</code> has
	 *            to be provided if the box that will be created does not need
	 *            to be connected to another node in the DOT file.
	 * @param addText
	 *            additional text that needs to be filled in at the beginning of
	 *            the box
	 * @param bw
	 *            the BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public void writeDistributionToDot(String boxId, String nodeId,
			String addText, Writer bw) throws IOException {
		// write the box itself
		String label = "";
		label = label + addText + "\\n";
		label = label + getName() + "\\n";
		bw.write(boxId + " [shape=\"ellipse\", label=\"" + label + "\"];\n");
		// write the connection (if needed)
		if (!nodeId.equals("")) {
			bw.write(nodeId + " -> " + boxId + " [dir=none, style=dotted];\n");
		}
	}
}
