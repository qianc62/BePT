package org.processmining.framework.util;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
 * GUIPropertyInteger age = new GUIPropertyInteger("Age", 0, 0, 100); <br>
 * testPanel.add(age.getPropertyPanel()); // add one property <br>
 * GUIPropertyInteger height = new GUIPropertyInteger("Height", "Height in cm", 0, 50, 250); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyInteger implements ChangeListener {

	// property attributes
	private String myName;
	private String myDescription;
	private int myValue;
	private GuiNotificationTarget myTarget;
	protected int myWidth;
	protected boolean myEditable;

	double myMinValue = 0.0;
	double myMaxValue = 0.0;
	boolean myNoMinAndMax = false;
	// GUI attributes
	private JSpinner mySpinner;
	private SpinnerNumberModel myModel;

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
	public GUIPropertyInteger(String name, int defaultValue, int minValue,
			int maxValue) {
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
	public GUIPropertyInteger(String name, String description,
			int defaultValue, int minValue, int maxValue) {
		this(name, description, defaultValue, minValue, maxValue, null, 100,
				true);
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
	public GUIPropertyInteger(String name, int defaultValue, int minValue,
			int maxValue, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, target, 100, true);
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
	public GUIPropertyInteger(String name, String description,
			int defaultValue, int minValue, int maxValue,
			GuiNotificationTarget target, int width, boolean editable) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		myMinValue = minValue;
		myMaxValue = maxValue;
		myWidth = width;
		myEditable = editable;
	}

	/**
	 * Creates an integer property without a minimal and minimal value.
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
	public GUIPropertyInteger(String name, String description,
			int defaultValue, GuiNotificationTarget target, int width,
			boolean editable) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		myMinValue = 0;
		myMaxValue = 0;
		myNoMinAndMax = true;
		myWidth = width;
		myEditable = editable;
	}

	/**
	 * The method automatically invoked when changing the spinner status.
	 * 
	 * @param e
	 *            The passed change event (not used).
	 */
	public void stateChanged(ChangeEvent e) {
		myValue = myModel.getNumber().intValue();
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
	public int getValue() {
		return myValue;
	}

	/**
	 * Checks whether the spinner is currently enabled or disabled.
	 * 
	 * @return <code>true</code> if is enabled, <code>false</code> otherwise
	 */
	public boolean isEnabled() {
		if (mySpinner != null) {
			return mySpinner.isEnabled();
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
		if (mySpinner != null) {
			mySpinner.setEnabled(false);
		}
	}

	/**
	 * Re-activates the possibility to manipulate this property via the GUI
	 * panel.
	 * 
	 * @see #disable()
	 */
	public void enable() {
		if (mySpinner != null) {
			mySpinner.setEnabled(true);
		}
	}

	/**
	 * Sets the value of this property
	 * 
	 * @param value
	 *            double
	 */
	public void setValue(int value) {
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
		if (myNoMinAndMax == true) {
			myModel = new SpinnerNumberModel(myValue /* initial value */, null,
					null, 1 /* step */);
			mySpinner = new JSpinner(myModel);
			mySpinner.setSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setPreferredSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMaximumSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMinimumSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setOpaque(false);
			myModel.addChangeListener(this);
		} else {
			myModel = new SpinnerNumberModel(myValue /* initial value */,
					myMinValue /* min */, myMaxValue /* max */, 1 /* step */);
			mySpinner = new JSpinner(myModel);
			mySpinner.setSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setPreferredSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMaximumSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMinimumSize(new Dimension(myWidth, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setOpaque(false);
			myModel.addChangeListener(this);

		}
		mySpinner.setEnabled(myEditable);
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
		resultPanel.add(mySpinner);
		return resultPanel;
	}
}
