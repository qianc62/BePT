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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.hlprocess.HLActivity;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.att.HLAttributeManager;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.models.hlprocess.hlmodel.HLPetriNet;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.models.petrinet.Transition;
import org.processmining.framework.util.GUIPropertyListWithoutGlue;
import org.processmining.framework.util.GuiNotificationTarget;

import weka.core.Attribute;
import weka.core.FastVector;

/**
 * Represents a case attribute as it has been observed in the log. <br>
 * Maintains the corresponding GUI panel where the attribute may be included or
 * excluded from analysis, and the proper attribute type may be chosen (such as
 * numeric or nominal). <br>
 * Furthermore it creates a link to the data mining application (see
 * #getWekaAttribute getWekaAttribute).
 * 
 * @author arozinat
 */
public class DecisionAttribute implements GuiNotificationTarget {

	/** numeric value statistics (may be empty) */
	private SummaryStatistics myNumericValues = SummaryStatistics.newInstance();
	/** which tasks have written this attribute */
	private HashSet<LogEvent> myLogEvents = new HashSet<LogEvent>();

	private HLPetriNet hlPetriNet;
	/**
	 * simulation model attributes (need to be updated as soon as attribute is,
	 * e.g., deselected)
	 */
	private HLProcess hlProcess;
	/** the high level attribute linked to this decision attribute */
	protected HLAttribute hlAttribute;
	protected HLNominalAttribute nominalBackup;

	private JPanel myPanel;
	private JCheckBox myNameCheckBox;
	private GUIPropertyListWithoutGlue myTypeGuiRepresenation;

	/**
	 * Default constructor.
	 * 
	 * @param name
	 *            the name of the attribute
	 * @param values
	 *            the set of values observed in the log
	 */
	public DecisionAttribute(String name, HLPetriNet highLevelPN) {
		hlAttribute = new HLNominalAttribute(name, highLevelPN.getHLProcess());
		hlPetriNet = highLevelPN;
		hlProcess = highLevelPN.getHLProcess();
	}

	/**
	 * Returns the name of this attribute.
	 * 
	 * @return the name of this attribute
	 */
	public String getName() {
		return hlAttribute.getName();
	}

	/**
	 * Adds the passed value to the values already held for this attribute.
	 * Duplicate values will not be added twice, so the collection of values
	 * corresponds to a set semantics.
	 * 
	 * @param newValues
	 *            the new value to be added
	 */
	public void addValue(String newValue) {
		// add value to enumerated values
		if (newValue != null) {
			((HLNominalAttribute) hlAttribute).addPossibleValue(newValue);
			// if is numeric also add value to statistics
			try {
				long numericValue = Long.parseLong(newValue);
				myNumericValues.addValue(numericValue);
			} catch (NumberFormatException ex) {
				// do nothing as value is simply not numeric
			}
		}
	}

	/**
	 * Adds the given log event to the list of log events held for this
	 * attribute. This is to keep track on which tasks have written this
	 * attribute.
	 * 
	 * @param logEvent
	 *            the log event to be added
	 */
	public void addLogEvent(LogEvent logEvent) {
		if (logEvent != null) {
			myLogEvents.add(logEvent);
		}
	}

	/**
	 * Determines whether this attribute was attached to audit trail entries
	 * which are associated to the given log event.
	 * 
	 * @param logEvent
	 *            the log event to be checked
	 * @return <code>true</code> if this attribute was observed for at least one
	 *         audit trail entry associated to the given log event,
	 *         <code>false</code> otherwise
	 */
	public boolean hasBeenObservedBy(LogEvent logEvent) {
		if (myLogEvents.contains(logEvent) == true) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Retrieves the set of log events that have provided this attribute.
	 * 
	 * @return the set of log events that specified this attribute in the given
	 *         log
	 */
	public Set<LogEvent> getLogEvents() {
		return myLogEvents;
	}

	/**
	 * Retrieves this attribute as a weka attribute (regardless whether it was
	 * selected by the user or not).
	 * 
	 * @return the weka attribute corresponding to this attribute
	 */
	public Attribute getWekaAttribute() {
		DecisionAttributeType myType = getAttributeType();
		// if no type has been chosen yet
		if (myType == null) {
			return null;
		}
		Attribute result = null;
		// build nominal attribute
		if (myType.equals(DecisionAttributeType.NOMINAL)) {
			List<String> values = ((HLNominalAttribute) hlAttribute)
					.getPossibleValues().getValues();
			FastVector my_nominal_values = new FastVector(values.size());
			for (String attValue : values) {
				my_nominal_values.addElement(attValue);
			}
			// add vector to attribute
			result = new Attribute(hlAttribute.getName(), my_nominal_values);
		} else if (myType.equals(DecisionAttributeType.NUMERIC)) {
			// make attribute a numeric one
			result = new Attribute(hlAttribute.getName());
		}
		return result;
	}

	/**
	 * Creates (and adds) a new data attribute object in the simulation model. <br>
	 * Adds the given simulation attribute to all high level activities in the
	 * associated simulation model that have provided this attribute at least
	 * once in the log.
	 * 
	 * @return the new high level data attribute for this decision attribute
	 */
	public void createSimulationAttribute() {
		AttributeType type = getAttributeType().getHighLevelAttributeType();
		// create nominal high level attribute
		if (type == AttributeType.Nominal
				&& hlAttribute instanceof HLNumericAttribute) {
			ArrayList<String> valList = new ArrayList<String>();
			Iterator it = nominalBackup.getPossibleValues().getValues()
					.iterator();
			while (it.hasNext()) {
				String val = (String) it.next();
				int freq = nominalBackup.getPossibleValues()
						.getFrequencyPossibleValueNominal(val);
				for (int i = 0; i < freq; i++) {
					valList.add(val);
				}
			}
			// initial value is the first value in list (can be changed before
			// export)
			HLAttribute previousAtt = hlAttribute;
			hlAttribute = new HLNominalAttribute(getName(), valList, hlProcess);
			hlProcess.replaceAttribute(previousAtt.getID(), hlAttribute);
		}
		// create numeric high level attribute
		else if (type == AttributeType.Numeric
				&& hlAttribute instanceof HLNominalAttribute) {
			nominalBackup = (HLNominalAttribute) hlAttribute;
			hlAttribute = HLAttributeManager
					.autoChangeType((HLNominalAttribute) hlAttribute);
		}
		// add the same attribute to all activities (in the simulation model)
		// that have written the attribute
		Iterator<LogEvent> logEventIt = getLogEvents().iterator();
		while (logEventIt.hasNext()) {
			LogEvent le = logEventIt.next();
			// find associated transitions in the process model
			PetriNet pn = (PetriNet) hlPetriNet.getProcessModel();
			Iterator transIt = pn.findTransitions(le).iterator();
			while (transIt.hasNext()) {
				Transition trans = (Transition) transIt.next();
				HLActivity hlTrans = hlPetriNet.findActivity(trans);
				hlTrans.addOutputDataAttribute(hlAttribute.getID());
			}
		}
	}

	/**
	 * Returns currently held simulation attribute. Note that as soon as the
	 * type of the attribute changes, a new simulation attribute object will be
	 * created and assigned to this decision attribute.
	 * 
	 * @return the current simulation attribute object
	 */
	public HLAttribute getSimulationAttribute() {
		return hlAttribute;
	}

	/**
	 * Retrieves the corresponding GUI panel for this attribute. If it has not
	 * been built yet, it will be built now.
	 * 
	 * @return the panel belonging to this attribute
	 */
	public JPanel getAttributePanel() {
		if (myPanel == null) {
			buildAttributePanel();
		}
		return myPanel;
	}

	/**
	 * Determines the selection state of the belonging check box item (i.e.,
	 * whether the user has or has not chosen the respective attribute to be
	 * included into analysis).
	 */
	public boolean isIncluded() {
		boolean result = false;
		if (myNameCheckBox != null) {
			result = myNameCheckBox.isSelected();
		}
		return result;
	}

	/**
	 * Overridden to specify when two DecisionAttribute objects are considered
	 * to be equal.
	 * 
	 * @param o
	 *            the <code>DecisionAttribute</code> to be compared with
	 * @return <code>true</code> if the identifiers are the same,
	 *         <code>false</code> otherwise.
	 */
	public boolean equals(Object o) {
		// check object identity first
		if (this == o) {
			return true;
		}
		// check type (which includes check for null)
		return (o instanceof DecisionAttribute)
				&& hlAttribute.getID().equals(
						((DecisionAttribute) o).hlAttribute.getID());
	}

	/**
	 * Overridden to produce the same hash code for equal objects.
	 * 
	 * @return the hash code calculated
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		result = 37 * result + hlAttribute.getID().hashCode();
		return result;
	}

	/*
	 * This method is called as soon as the attribute type changes.
	 * 
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		// remove old simulation attribute
		// (automatically removes attribute from contained activities)
		HLAttribute oldAtt = getSimulationAttribute();
		hlProcess.removeAttribute(oldAtt.getID());
		// create new simulation attribute (with new attribute type)
		// and add to related activities
		createSimulationAttribute();
	}

	// //////////////////// Private methods
	// //////////////////////////////////////////////

	/**
	 * Retrieves the current type of this attribute (i.e., numeric or nominal).
	 * 
	 * @return the attribute type if the graphical representation has been built
	 *         already, <code>null</code> otherwise
	 */
	protected DecisionAttributeType getAttributeType() {
		DecisionAttributeType result = null;
		if (myTypeGuiRepresenation != null) {
			result = (DecisionAttributeType) myTypeGuiRepresenation.getValue();
		} else {
			result = DecisionAttributeType.NOMINAL;
		}
		return result;
	}

	/**
	 * Sets the specified attribut type if possible.
	 * 
	 * @param type
	 *            the new type
	 */
	protected void setDecisionAttributeType(DecisionAttributeType type) {
		if (myTypeGuiRepresenation == null) {
			createAttributeTypeGui();
		}
		myTypeGuiRepresenation.setValue(type);
	}

	/**
	 * Builds a panel containing a checkbox to include or exclude this
	 * attribute, and a combobox in order to chose an attribute type.
	 */
	private void buildAttributePanel() {
		myPanel = new JPanel();
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel("Attribute name: ");
		myNameLabel.setForeground(new Color(100, 100, 100));
		myPanel.add(myNameLabel);
		myNameCheckBox = new JCheckBox(getName());
		// per default select everything
		myNameCheckBox.setSelected(true);

		// register check/uncheck action
		myNameCheckBox.addActionListener(new ActionListener() {
			// specify action when the attribute selection scope is changed
			public void actionPerformed(ActionEvent e) {
				JCheckBox cb = (JCheckBox) e.getSource();
				HLAttribute simAtt = getSimulationAttribute();
				if (cb.isSelected() == true) {
					myTypeGuiRepresenation.enable();
					// add attribute to simulation model
					// and to related activities
					createSimulationAttribute();
				} else {
					myTypeGuiRepresenation.disable();
					// remove attribute from simulation model
					// (automatically removes attribute from contained
					// activities)
					hlProcess.removeAttribute(simAtt.getID());
				}
			}
		});

		myPanel.add(myNameCheckBox);
		myPanel.add(Box.createHorizontalGlue());

		createAttributeTypeGui();
		// ArrayList<DecisionAttributeType> attributeTypes = new
		// ArrayList<DecisionAttributeType>();
		// // per default set "nominal"
		// attributeTypes.add(DecisionAttributeType.NOMINAL);
		// attributeTypes.add(DecisionAttributeType.NUMERIC);
		// myTypeGuiRepresenation = new
		// GUIPropertyListWithoutGlue("Attribute type:",
		// "Please determine the type of the attribute", attributeTypes, this);

		myPanel.add(myTypeGuiRepresenation.getPropertyPanel());
	}

	private void createAttributeTypeGui() {
		ArrayList<DecisionAttributeType> attributeTypes = new ArrayList<DecisionAttributeType>();
		// per default set "nominal"
		attributeTypes.add(DecisionAttributeType.NOMINAL);
		attributeTypes.add(DecisionAttributeType.NUMERIC);
		myTypeGuiRepresenation = new GUIPropertyListWithoutGlue(
				"Attribute type:",
				"Please determine the type of the attribute", attributeTypes,
				this);
	}

}
