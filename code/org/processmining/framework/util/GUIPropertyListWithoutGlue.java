package org.processmining.framework.util;

import java.awt.Dimension;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Needed without glue for the Decision Miner. TODO: Solve differently as these
 * GUI properties grow too large and too specific - redesign GUI property pool!
 * 
 * @author Anne Rozinat
 */
public class GUIPropertyListWithoutGlue extends GUIPropertyListEnumeration {

	/**
	 * Creates an enumeration property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param values
	 *            the possible values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox. The first value in the list is considered as the
	 *            default value
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyListWithoutGlue(String name, String description,
			List values, GuiNotificationTarget target) {
		super(name, description, values, target, 100);
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (myComboBox == null) {
			myComboBox = new JComboBox();
			Iterator it = myPossibleValues.iterator();
			while (it.hasNext()) {
				Object val = it.next();
				myComboBox.addItem(val);
			}
			myComboBox.setSize(new Dimension(myWidth, (int) myComboBox
					.getPreferredSize().getHeight()));
			myComboBox.setPreferredSize(new Dimension(myWidth, (int) myComboBox
					.getPreferredSize().getHeight()));
			myComboBox.setMaximumSize(new Dimension(myWidth, (int) myComboBox
					.getPreferredSize().getHeight()));
			myComboBox.setMinimumSize(new Dimension(myWidth, (int) myComboBox
					.getPreferredSize().getHeight()));
			// set myValue as selected
			myComboBox.setSelectedItem(myValue);
			myComboBox.addActionListener(this);
		}

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		resultPanel.add(myNameLabel);
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		// resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(myComboBox);
		return resultPanel;
	}
}