package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * An enumeration property that can be readily displayed as it maintains its own
 * GUI panel. The property will be graphically represented as a combo box. If a
 * description has been provided, it will be displayed as a tool tip. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property value. Furthermore, a {@link GuiNotificationTarget
 * notification target} may be specified in order to be informed as soon as the
 * value has been changed. <br>
 * <br>
 * A typical usage scenario looks as follows: <br>
 * <br>
 * <code>
 * JPanel testPanel = new Panel(); // create parent panel <br>
 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS)); <br>
 * ArrayList<String> values = new ArrayList<String>();
 * values.add("Male");
 * values.add("Female");
 * GUIPropertyListEnumeration gender = new GUIPropertyListEnumeration("Gender", values); <br>
 * testPanel.add(gender.getPropertyPanel()); // add one property <br>
 * return testPanel; <br>
 * </code> <br>
 * Note that this property expects a list of possible values rather than a
 * simple java enumeration in order also manage the choice between arbitrary
 * objects. Any set of self-defined objects may be passed to the
 * GUIPropertyListEnumeration as long as these self-defined objects provide a
 * toString() method in a meaningful way.
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyListEnumeration implements ActionListener {

	// property attributes
	protected String myName;
	protected String myDescription;
	protected List myPossibleValues;
	protected Object myValue;
	protected GuiNotificationTarget myTarget;
	// GUI attributes
	protected JComboBox myComboBox;
	protected int myWidth = 0;

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
	public GUIPropertyListEnumeration(String name, List values) {
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
	public GUIPropertyListEnumeration(String name, String description,
			List values) {
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
	public GUIPropertyListEnumeration(String name, List values,
			GuiNotificationTarget target) {
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
	public GUIPropertyListEnumeration(String name, String description,
			List values, GuiNotificationTarget target) {
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
	public GUIPropertyListEnumeration(String name, String description,
			List values, GuiNotificationTarget target, int width) {
		myName = name;
		myDescription = description;
		myPossibleValues = values;
		myTarget = target;
		myWidth = width;
		// assign first value (i.e., default value) as the current value)
		Iterator it = myPossibleValues.iterator();
		while (it.hasNext()) {
			Object val = it.next();
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
		myValue = myComboBox.getSelectedItem();
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
	public Object getValue() {
		return myValue;
	}

	/**
	 * Sets the currently selected value of this property. Note that it is
	 * assumed that this value is in the list of possible values
	 * 
	 * @param value
	 *            the value to be selected in the combo box
	 */
	public boolean setValue(Object value) {
		if (getAllValues().contains(value) == false) {
			return false;
		} else {
			myValue = value;
			if (myComboBox != null) {
				myComboBox.setSelectedItem(myValue);
			}
			return true;
		}
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values (including the current value)
	 */
	public List getAllValues() {
		return myPossibleValues;
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
			// disable combo box if list of possible values is emptpy
			if (myPossibleValues.size() == 0) {
				myComboBox.setEnabled(false);
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
			myComboBox.setOpaque(false);
		}

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		resultPanel.setOpaque(false);
		resultPanel.setBorder(BorderFactory.createEmptyBorder());
		JLabel myNameLabel = new JLabel(" " + myName);
		myNameLabel.setOpaque(false);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		resultPanel.add(myNameLabel);
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(myComboBox);
		return resultPanel;
	}

}
