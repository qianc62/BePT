package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * A list of strings property that can be readily displayed as it maintains its
 * own GUI panel. The property will be graphically represented as a set of radio
 * buttons. If a description has been provided, it will be displayed as a tool
 * tip. <br>
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
 * GuiPropertyListRadio gender = new GuiPropertyListRadio("Gender", values); <br>
 * testPanel.add(gender.getPropertyPanel()); // add one property <br>
 * return testPanel; <br>
 * </code> <br>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GuiPropertyListRadio implements MouseListener {

	// property attributes
	protected String myName;
	protected String myDescription;
	protected List<String> myPossibleValues;
	protected String myValue;
	protected GuiNotificationTarget myTarget;
	// GUI attributes
	protected ButtonGroup myRadioButtons;

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
	public GuiPropertyListRadio(String name, List<String> values) {
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
	public GuiPropertyListRadio(String name, String description,
			List<String> values) {
		this(name, description, values, null);
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
	public GuiPropertyListRadio(String name, List<String> values,
			GuiNotificationTarget target) {
		this(name, null, values, target);
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
	public GuiPropertyListRadio(String name, String description,
			List<String> values, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myPossibleValues = values;
		myTarget = target;
		// GUI attributes
		myRadioButtons = new ButtonGroup();
		Iterator<String> it = myPossibleValues.iterator();
		while (it.hasNext()) {
			String val = it.next();
			JRadioButton button = new JRadioButton(val);
			button.firePropertyChange("kk", false, true);
			// assign first value (ie, default value) as the current value
			if (myValue == null) {
				myValue = val;
				button.setSelected(true);
			}
			myRadioButtons.add(button);
			// button.addActionListener(this);
			button.addMouseListener(this);
		}
	}

	/**
	 * The method automatically invoked when changing the combobox status.
	 * 
	 * @param e
	 *            the passed action event (not used)
	 */
	public void mouseClicked(MouseEvent e) {
		myValue = ((JRadioButton) e.getSource()).getText();
		if (myTarget != null) {
			// notify owner of this property if specified
			myTarget.updateGUI();
		}
	}

	public void mousePressed(MouseEvent e) {

	}

	public void mouseReleased(MouseEvent e) {

	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {

	}

	public void stateChanged() {

	}

	/**
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public String getValue() {
		return myValue;
	}

	/**
	 * Retrieves all the possible values specified for this property.
	 * 
	 * @return all possible values (including the current value)
	 */
	public List<String> getAllValues() {
		return myPossibleValues;
	}

	/**
	 * Prevents that this property may be manipulated via the GUI panel.
	 * 
	 * @see #enable(String)
	 * @param the
	 *            name of the radio button to disable
	 */
	public void disable(String toDisable) {
		Enumeration allButtons = myRadioButtons.getElements();
		while (allButtons.hasMoreElements()) {
			JRadioButton current = (JRadioButton) allButtons.nextElement();
			if (current.getText().equals(toDisable)) {
				current.setEnabled(false);
				break;
			}
		}
	}

	/**
	 * Re-activates the possibility to manipulate this property via the GUI
	 * panel.
	 * 
	 * @see #disable(String)
	 * @param the
	 *            name of the radio button to enable
	 */
	public void enable(String toEnable) {
		Enumeration allButtons = myRadioButtons.getElements();
		while (allButtons.hasMoreElements()) {
			JRadioButton current = (JRadioButton) allButtons.nextElement();
			if (current.getText().equals(toEnable)) {
				current.setEnabled(true);
				current.setSelected(true);
				break;
			}
		}
	}

	/**
	 * Sets the state of respective radio button. In the case that the
	 * radiobutton had been disabled and it needs to be selected, the call will
	 * be ignored.
	 * 
	 * @param toSetSelected
	 *            String the name of the radio button to select/deselect
	 * @param b
	 *            boolean true if the radio button is selected, otherwise false
	 */
	public void setSelected(String toSetSelected, boolean b) {
		Enumeration allButtons = myRadioButtons.getElements();
		while (allButtons.hasMoreElements()) {
			JRadioButton current = (JRadioButton) allButtons.nextElement();
			if (current.getText().equals(toSetSelected) && current.isEnabled()) {
				current.setSelected(b);
				myValue = current.getText();
				break;
			}
		}
	}

	/**
	 * Check whether one of the radio buttons is enabled or disabled
	 * 
	 * @param toCheck
	 *            String the name of the radiobutton
	 * @return boolean true when there exists in this property an radiobox with
	 *         the same name which is enabled, false otherwise
	 */
	public boolean isEnabled(String toCheck) {
		boolean returnBool = false;
		Enumeration allButtons = myRadioButtons.getElements();
		while (allButtons.hasMoreElements()) {
			JRadioButton current = (JRadioButton) allButtons.nextElement();
			if (current.getText().equals(toCheck)) {
				returnBool = current.isEnabled();
			}
		}
		return returnBool;
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
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.setOpaque(false);

		if (myName != "") {
			JPanel content = new JPanel();
			content.setLayout(new BoxLayout(content, BoxLayout.LINE_AXIS));
			content.setOpaque(false);
			JLabel myNameLabel = new JLabel(" " + myName);
			if (myDescription != null) {
				myNameLabel.setToolTipText(myDescription);
			}
			content.add(myNameLabel);
			content.add(Box.createRigidArea(new Dimension(5, 0)));
			content.add(Box.createHorizontalGlue());
			resultPanel.add(content);
		}

		Enumeration allButtons = myRadioButtons.getElements();
		while (allButtons.hasMoreElements()) {
			JRadioButton current = (JRadioButton) allButtons.nextElement();
			current.setOpaque(false);
			resultPanel.add(current);
		}

		JPanel propPanel = new JPanel();
		propPanel.setLayout(new BoxLayout(propPanel, BoxLayout.LINE_AXIS));
		propPanel.setOpaque(false);
		propPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		propPanel.add(resultPanel);
		propPanel.add(Box.createHorizontalGlue());
		return propPanel;
	}
}
