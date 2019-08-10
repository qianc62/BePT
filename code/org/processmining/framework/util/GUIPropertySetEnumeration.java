package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;

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
 * HashSet<String> values = new HashSet<String>();
 * values.add("Male");
 * values.add("Female");
 * GUIPropertySetEnumeration gender = new GUIPropertySetEnumeration("Gender", values); <br>
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
public class GUIPropertySetEnumeration implements ActionListener {

	// property attributes
	private String myName;
	private String myDescription;
	private Set myPossibleValues;
	private Object myValue;
	private GuiNotificationTarget myTarget;
	// GUI attributes
	private JComboBox myComboBox;

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
	public GUIPropertySetEnumeration(String name, Set values) {
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
	public GUIPropertySetEnumeration(String name, String description, Set values) {
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
	public GUIPropertySetEnumeration(String name, Set values,
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
	public GUIPropertySetEnumeration(String name, String description,
			Set values, GuiNotificationTarget target) {
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
	public GUIPropertySetEnumeration(String name, String description,
			Set values, GuiNotificationTarget target, int width) {
		myName = name;
		myDescription = description;
		myPossibleValues = values;
		myTarget = target;
		// GUI attributes
		myComboBox = new JComboBox();
		// rmans, check whether myPossibleValues is Null
		if (myPossibleValues != null) {
			Iterator it = myPossibleValues.iterator();
			while (it.hasNext()) {
				Object val = it.next();
				// assign first value (ie, default value) as the current value
				if (myValue == null) {
					myValue = val;
				}
				myComboBox.addItem(val);
			}
		}
		myComboBox.setSize(new Dimension(width, (int) myComboBox
				.getPreferredSize().getHeight()));
		myComboBox.setPreferredSize(new Dimension(width, (int) myComboBox
				.getPreferredSize().getHeight()));
		myComboBox.setMaximumSize(new Dimension(width, (int) myComboBox
				.getPreferredSize().getHeight()));
		myComboBox.setMinimumSize(new Dimension(width, (int) myComboBox
				.getPreferredSize().getHeight()));
		myComboBox.addActionListener(this);
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
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public Object getValue() {
		return myValue;
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values (including the current value)
	 */
	public Set getAllValues() {
		return myPossibleValues;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
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
