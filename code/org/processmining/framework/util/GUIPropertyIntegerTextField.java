package org.processmining.framework.util;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/**
 * An integer property that can be readily displayed as it maintains its own GUI
 * panel. The property will be graphically represented as a label containing the
 * name of the property and a spinner according to the provided value range. If
 * a description has been provided, it will be displayed as a tool tip. <br>
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
 * GUIPropertyIntegerTextField age = new GUIPropertyIntegerTextField("Age", 0, 0, 100); <br>
 * testPanel.add(age.getPropertyPanel()); // add one property <br>
 * GUIPropertyInteger height = new GUIPropertyIntegerTextField("Height", "Height in cm", 0, 50, 250); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyIntegerTextField implements KeyListener {

	// property attributes
	private String myName;
	private String myDescription;
	private int myValue;
	private GuiNotificationTarget myTarget;

	double myMinValue = 0.0;
	double myMaxValue = 0.0;
	// needs a minimal value to be specified or not
	boolean myNoMin = false;
	// needs a maximal value to be specified or not
	boolean myNoMax = false;
	// GUI attributes
	private JTextField myTextField;

	/**
	 * Creates an integer property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 */
	public GUIPropertyIntegerTextField(String name, int defaultValue,
			int minValue, int maxValue) {
		// this(name, null, defaultValue, minValue, maxValue);
		this(name, "", defaultValue, minValue, maxValue);
	}

	/**
	 * Creates an integer property without notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 */
	public GUIPropertyIntegerTextField(String name, String description,
			int defaultValue, int minValue, int maxValue) {
		this(name, description, defaultValue, minValue, maxValue, null);
	}

	/**
	 * Creates an integer property without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyIntegerTextField(String name, int defaultValue,
			int minValue, int maxValue, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, target);
	}

	/**
	 * Creates an integer property.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyIntegerTextField(String name, String description,
			int defaultValue, int minValue, int maxValue,
			GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		myMinValue = minValue;
		myMaxValue = maxValue;
	}

	/**
	 * Creates an integer property without no minimal or maximal value.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param defaultValue
	 *            the default value of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyIntegerTextField(String name, String description,
			int defaultValue, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		myNoMin = true;
		myNoMax = true;
	}

	/**
	 * Creates an integer property without no minimal or maximal value.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyIntegerTextField(String name, int defaultValue,
			GuiNotificationTarget target) {
		myName = name;
		myValue = defaultValue;
		myTarget = target;
		myNoMin = true;
		myNoMax = true;
	}

	/**
	 * Creates an integer property without no minimal or maximal value.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param value
	 *            the minimal or maximal value of this property, based on the
	 *            value set for minOrMax
	 * @param minOrMax
	 *            if true then with value the minimal value will be specified,
	 *            otherwise the maximal value will be specified
	 */
	public GUIPropertyIntegerTextField(String name, int defaultValue,
			int value, boolean minOrMax, GuiNotificationTarget target) {
		myName = name;
		myValue = defaultValue;
		myTarget = target;
		if (minOrMax == true) {
			myMinValue = value;
			myNoMax = true;
		} else {
			myMaxValue = value;
			myNoMin = true;
		}
	}

	/**
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public int getValue() {
		return myValue;
	}

	/**
	 * Checks whether the textfield is currently enabled or disabled.
	 * 
	 * @return <code>true</code> if is enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled() {
		if (myTextField != null) {
			return myTextField.isEnabled();
		} else {
			return false;
		}
	}

	/**
	 * Prevents that this property may be manipulated via the GUI panel.
	 * 
	 * @see #enable()
	 */
	public void disable() {
		if (myTextField != null) {
			myTextField.setEnabled(false);
		}
	}

	/**
	 * Re-activates the possibility to manipulate this property via the GUI
	 * panel.
	 * 
	 * @see #disable()
	 */
	public void enable() {
		if (myTextField != null) {
			myTextField.setEnabled(true);
		}
	}

	/**
	 * Sets the value of this property
	 * 
	 * @param value
	 *            int
	 */
	public void setValue(int value) {
		if (myNoMin == false && myNoMax == false && (myValue >= myMinValue)
				&& (myValue <= myMaxValue)) {
			myValue = value;
		} else if (myNoMax == true && myValue >= myMinValue) {
			myValue = value;
		} else if (myNoMin == true && myValue <= myMaxValue) {
			myValue = value;
		} else {
			myValue = value;
		}

	}

	/**
	 * The method automatically invoked when changing the text field status.
	 * 
	 * @param e
	 *            the passed action event (not used)
	 */
	public void keyTyped(KeyEvent e) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyPressed(java.awt.event.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		// not used
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		try {
			int retrievedValue = Integer.parseInt(myTextField.getText());
			// check whether the new value is between the minimal and maximal
			// value
			if (myNoMin == false && myNoMax == false && (myValue >= myMinValue)
					&& (myValue <= myMaxValue)) {
				myValue = retrievedValue;
			} else if (myNoMax == true && myValue >= myMinValue) {
				myValue = retrievedValue;
			} else if (myNoMin == true && myValue <= myMaxValue) {
				myValue = retrievedValue;
			} else if (myNoMin == true && myNoMax == true) {
				myValue = retrievedValue;
			} else {
				// set the text field back to the original value
				myTextField.setText(Integer.toString(myValue));
			}
			if (myTarget != null) {
				// notify owner of this property if specified
				myTarget.updateGUI();
			}
		} catch (NumberFormatException exc) {
			myTextField.setText(Integer.toString(myValue));
			myTextField.validate();
			myTextField.updateUI();
		}
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		// create the textfield, when the guipropertydouble is asked for its
		// panel
		if (myTextField == null) {
			myTextField = new JTextField();
			myTextField.setText(new Integer(myValue).toString());
		}
		myTextField.setSize(new Dimension(100, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setPreferredSize(new Dimension(100, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setMaximumSize(new Dimension(100, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setMinimumSize(new Dimension(100, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.addKeyListener(this);

		JPanel resultPanel = new JPanel();
		resultPanel.setOpaque(false);
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		resultPanel.add(myNameLabel);
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(myTextField);
		return resultPanel;
	}
}
