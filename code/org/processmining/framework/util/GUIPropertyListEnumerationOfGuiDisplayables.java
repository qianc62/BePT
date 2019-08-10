package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GUIPropertyListEnumerationOfGuiDisplayables implements
		ActionListener {

	// property attributes
	protected String myName;
	protected String myDescription;
	protected List<GuiDisplayable> myPossibleValues;
	protected GuiDisplayable myValue;
	protected GuiNotificationTarget myTarget;
	// GUI attributes
	protected JComboBox myComboBox;
	protected int myWidth = 0;
	protected JPanel myPanel;
	protected JPanel myContentPanel;

	/**
	 * Creates an enumeration property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param values
	 *            the possible values of this property. The objects in this list
	 *            should either be simple strings or override the toString()
	 *            method, which is then displayed as the name of this value in
	 *            the ComboBox. The first value in the list is considered as the
	 *            default value
	 */
	public GUIPropertyListEnumerationOfGuiDisplayables(String name,
			List<GuiDisplayable> values) {
		this(name, null, values);
	}

	/**
	 * Creates an enumeration property without notification.
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
	 */
	public GUIPropertyListEnumerationOfGuiDisplayables(String name,
			String description, List<GuiDisplayable> values) {
		this(name, description, values, null, 100);
	}

	/**
	 * Creates an enumeration property without a discription.
	 * 
	 * @param name
	 *            the name of this property
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
	public GUIPropertyListEnumerationOfGuiDisplayables(String name,
			List<GuiDisplayable> values, GuiNotificationTarget target) {
		this(name, null, values, target, 100);
	}

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
	public GUIPropertyListEnumerationOfGuiDisplayables(String name,
			String description, List<GuiDisplayable> values,
			GuiNotificationTarget target) {
		this(name, description, values, target, 100);
	}

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
	 * @param width
	 *            a custom width may be specified (default value is 100
	 *            otherwise)
	 */
	public GUIPropertyListEnumerationOfGuiDisplayables(String name,
			String description, List<GuiDisplayable> values,
			GuiNotificationTarget target, int width) {
		myName = name;
		myDescription = description;
		myPossibleValues = values;
		myTarget = target;
		myWidth = width;
		// assign first value (i.e., default value) as the current value)
		Iterator<GuiDisplayable> it = myPossibleValues.iterator();
		while (it.hasNext()) {
			GuiDisplayable val = it.next();
			// assign first value (ie, default value) as the current value
			if (myValue == null) {
				myValue = val;
				break;
			}
		}
	}

	/**
	 * The method automatically invoked when changing the combobox status.
	 * 
	 * @param e
	 *            the passed action event (not used)
	 */
	public void actionPerformed(ActionEvent e) {
		myValue = (GuiDisplayable) myComboBox.getSelectedItem();
		myContentPanel.removeAll();
		myContentPanel.add(this.getValue().getPanel());
		myContentPanel.validate();
		myContentPanel.repaint();
		myPanel.validate();
		myPanel.repaint();

		if (myTarget != null) {
			// notify owner of this property if specified
			myTarget.updateGUI();
		}
	}

	/**
	 * Manually notify the target that the state of the radiolist has been
	 * changed.
	 */
	public void notifyTarget() {
		if (myTarget != null) {
			myTarget.updateGUI();
		}
	}

	/**
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public GuiDisplayable getValue() {
		return myValue;
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values (including the current value)
	 */
	public List<GuiDisplayable> getAllValues() {
		return myPossibleValues;
	}

	/**
	 * Removes an enumeration value, if present
	 * 
	 * @param value
	 *            Object the enumeration value to be removed
	 */
	public void removeValue(GuiDisplayable value) {
		if (myPossibleValues.contains(value)) {
			myPossibleValues.remove(value);
			myComboBox.removeItem(value);
			myComboBox.validate();
			myComboBox.repaint();
		}
	}

	/**
	 * Adds an enumeration value. If it is already present it will not be added
	 * again
	 * 
	 * @param value
	 *            Object The enumeration value to be added
	 */
	public void addValue(GuiDisplayable value) {
		if (!myPossibleValues.contains(value)) {
			myPossibleValues.add(value);
			myComboBox.addItem(value);
			myComboBox.validate();
			myComboBox.repaint();
		}
	}

	/**
	 * Prevents that this property may be manipulated via the GUI panel.
	 * 
	 * @see #enable()
	 */
	public void disable() {
		myComboBox.setEnabled(false);
	}

	/**
	 * Re-activates the possibility to manipulate this property via the GUI
	 * panel.
	 * 
	 * @see #disable()
	 */
	public void enable() {
		myComboBox.setEnabled(true);
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
			myComboBox.addActionListener(this);
		}
		if (myPanel == null) {
			myPanel = new JPanel();
			// myPanel.setBorder(BorderFactory.createEtchedBorder());
			myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.LINE_AXIS));
			JLabel myNameLabel = new JLabel(" " + myName);
			if (myDescription != null) {
				myNameLabel.setToolTipText(myDescription);
			}
			myPanel.add(myNameLabel);
			myPanel.add(Box.createRigidArea(new Dimension(5, 0)));
			myPanel.add(myComboBox);
		}
		if (myContentPanel == null) {
			myContentPanel = this.getValue().getPanel();
			// myContentPanel.setBorder(BorderFactory.createEtchedBorder());
		}

		myPanel.add(Box.createHorizontalGlue());
		myPanel.add(myContentPanel);

		return myPanel;
	}
}
