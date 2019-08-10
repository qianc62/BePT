package org.processmining.framework.util;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A double property that can be readily displayed as it maintains its own GUI
 * panel. The property will be graphically represented as a label containing the
 * name of the property and a spinner according to the provided value range and
 * step size. If a description has been provided, it will be displayed as a tool
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
 * GUIPropertyDouble width = new GUIPropertyDouble("Width", 0.0, 0.0, 10.0, 0.1); <br>
 * testPanel.add(width.getPropertyPanel()); // add one property <br>
 * GUIPropertyDouble height = new GUIPropertyDouble("Height", "Height in cm", 0.0, 0.0, 20.0, 0.1); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyDouble implements ChangeListener {

	// property attributes
	private String myName;
	private String myDescription;
	private double myValue;
	private GuiNotificationTarget myTarget;
	// attributes needed for the spinner
	// added 11-10-2006
	double myMinValue = 0.0;
	double myMaxValue = 0.0;
	double myStepSize = 1.0;
	boolean myNoMinAndMax = false;
	// end addition
	// GUI attributes
	private JSpinner mySpinner;
	private SpinnerNumberModel myModel;

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
	public GUIPropertyDouble(String name, double defaultValue, double minValue,
			double maxValue, double stepSize) {
		this(name, null, defaultValue, minValue, maxValue, stepSize);
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
	public GUIPropertyDouble(String name, String description,
			double defaultValue, double minValue, double maxValue,
			double stepSize) {
		this(name, description, defaultValue, minValue, maxValue, stepSize,
				null);
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
	public GUIPropertyDouble(String name, double defaultValue, double minValue,
			double maxValue, double stepSize, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, stepSize, target);
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
	public GUIPropertyDouble(String name, String description,
			double defaultValue, double minValue, double maxValue,
			double stepSize, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		//
		myMinValue = minValue;
		myMaxValue = maxValue;
		myStepSize = stepSize;
	}

	/**
	 * Creates a double property without a minimal or maximal value.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param defaultValue
	 *            the default value of this property
	 * @param stepSize
	 *            the distance between two adjacent values
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyDouble(String name, String description,
			double defaultValue, double stepSize, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		//
		myMinValue = 0.0;
		myMaxValue = 0.0;
		myStepSize = stepSize;
		myNoMinAndMax = true;
	}

	/**
	 * The method automatically invoked when changing the spinner status.
	 * 
	 * @param e
	 *            The passed change event (not used).
	 */
	public void stateChanged(ChangeEvent e) {
		myValue = myModel.getNumber().doubleValue();
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
		myValue = value;
		myMinValue = value - 10000;
		myMaxValue = value + 10000;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		// create the spinner, when the guipropertydouble is asked for its panel
		// added 11-10-2006
		// if clause removed 16-02-2007
		// if (mySpinner == null) {
		if (myNoMinAndMax == true) {
			myModel = new SpinnerNumberModel(myValue, null, null, myStepSize);
			mySpinner = new JSpinner(myModel);
			mySpinner.setSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setPreferredSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMaximumSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMinimumSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			myModel.addChangeListener(this);

		} else {
			myModel = new SpinnerNumberModel(myValue /* initial value */,
					myMinValue /* min */, myMaxValue /* max */, myStepSize /* step */);
			mySpinner = new JSpinner(myModel);
			mySpinner.setSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setPreferredSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMaximumSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMinimumSize(new Dimension(100, (int) mySpinner
					.getPreferredSize().getHeight()));
			myModel.addChangeListener(this);
		}
		// }

		// end addition

		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.LINE_AXIS));
		JLabel myNameLabel = new JLabel(" " + myName);
		if (myDescription != null) {
			myNameLabel.setToolTipText(myDescription);
		}
		resultPanel.add(myNameLabel);
		resultPanel.add(Box.createRigidArea(new Dimension(5, 0)));
		resultPanel.add(Box.createHorizontalGlue());
		resultPanel.add(mySpinner);
		return resultPanel;
	}
}
