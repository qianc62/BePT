package org.processmining.framework.util;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * A double property that can be readily displayed as it maintains its own GUI
 * panel. The property will be graphically represented as a label containing the
 * name of the property and a textfield. If a description has been provided, it
 * will be displayed as a tool tip. <br>
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
 * GUIPropertyDouble width = new GUIPropertyDoubleTextField("Width", 0.0, 0.0, 10.0, 0.1); <br>
 * testPanel.add(width.getPropertyPanel()); // add one property <br>
 * GUIPropertyDouble height = new GUIPropertyDouble("Height", "Height in cm", 0.0, 0.0, 20.0, 0.1); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyDoubleTextField implements KeyListener {

	// property attributes
	private String myName;
	private String myDescription;
	private double myValue;
	private GuiNotificationTarget myTarget;
	double myMinValue = 0.0;
	double myMaxValue = 0.0;
	// needs a minimal value to be specified or not
	boolean myNoMin = false;
	// needs a maximal value to be specified or not
	boolean myNoMax = false;
	// GUI attributes
	private JTextField myTextField;
	protected int myWidth = 0;

	/**
	 * Creates a double property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 * @param stepSize
	 *            the distance between two adjacent values
	 */
	public GUIPropertyDoubleTextField(String name, double defaultValue,
			double minValue, double maxValue) {
		this(name, null, defaultValue, minValue, maxValue);
	}

	/**
	 * Creates a double property without notification.
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
	 * @param stepSize
	 *            the distance between two adjacent values
	 */
	public GUIPropertyDoubleTextField(String name, String description,
			double defaultValue, double minValue, double maxValue) {
		this(name, description, defaultValue, minValue, maxValue, null);
	}

	/**
	 * Creates a double property without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param minValue
	 *            the minimal value that can be assigned to this property
	 * @param maxValue
	 *            the maximal value that can be assigned to this property
	 * @param stepSize
	 *            the distance between two adjacent values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyDoubleTextField(String name, double defaultValue,
			double minValue, double maxValue, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, target);
	}

	/**
	 * Creates a double property.
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
	 * @param stepSize
	 *            the distance between two adjacent values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyDoubleTextField(String name, String description,
			double defaultValue, double minValue, double maxValue,
			GuiNotificationTarget target) {
		this(name, description, defaultValue, target, false, minValue, false,
				maxValue, 100);
	}

	/**
	 * Creates a double property.
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
	public GUIPropertyDoubleTextField(String name, String description,
			double defaultValue, GuiNotificationTarget target, int width) {
		this(name, description, defaultValue, target, true, 0, true, 0, width);
	}

	/**
	 * Creates an integer property without no minimal and no maximal value.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyDoubleTextField(String name, double defaultValue) {
		this(name, null, defaultValue, null, true, 0, true, 0, 100);
	}

	/**
	 * Creates an integer property without no minimal and no maximal value and a
	 * GuiNotificationTarget.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyDoubleTextField(String name, double defaultValue,
			GuiNotificationTarget target) {
		this(name, null, defaultValue, target, true, 0, true, 0, 100);
	}

	public GUIPropertyDoubleTextField(String name, String description,
			double defaultValue, GuiNotificationTarget target, boolean noMin,
			double minValue, boolean noMax, double maxValue, int width) {
		myName = name;
		myDescription = name;
		myValue = defaultValue;
		myTarget = target;
		myNoMin = noMin;
		myMinValue = minValue;
		myNoMax = noMax;
		myMaxValue = maxValue;
		myWidth = width;
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
	 *            if true then with value the minimal value will be specified
	 *            (and there is no maximum value), otherwise the maximal value
	 *            will be specified (and there is no minimal value)
	 */
	public GUIPropertyDoubleTextField(String name, double defaultValue,
			double value, boolean minOrMax, GuiNotificationTarget target) {
		this(name, null, defaultValue, target, (minOrMax ? false : true),
				(minOrMax ? value : 0), (minOrMax ? true : false),
				(minOrMax ? 0 : value), 100);
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
			double retrievedValue = Double.parseDouble(myTextField.getText());
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
				myTextField.setText(Double.toString(myValue));
			}
			if (myTarget != null) {
				// notify owner of this property if specified
				myTarget.updateGUI();
			}
		} catch (NumberFormatException exc) {
			myTextField.setText(Double.toString(myValue));
			myTextField.validate();
			myTextField.updateUI();
		}
	}

	/**
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public double getValue() {
		return myValue;
	}

	/**
	 * Sets the value of this property
	 * 
	 * @param value
	 *            double
	 */
	public void setValue(double value) {
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
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		myTextField = new JTextField();
		myTextField.setText(new Double(myValue).toString());
		myTextField.setSize(new Dimension(myWidth, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setPreferredSize(new Dimension(myWidth, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setMaximumSize(new Dimension(myWidth, (int) myTextField
				.getPreferredSize().getHeight()));
		myTextField.setMinimumSize(new Dimension(myWidth, (int) myTextField
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
