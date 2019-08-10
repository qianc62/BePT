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
 * A float property that can be readily displayed as it maintains its own GUI
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
 * GUIPropertyFloat width = new GUIPropertyFloat("Width", 0.0, 0.0, 10.0, 0.1); <br>
 * testPanel.add(width.getPropertyPanel()); // add one property <br>
 * GUIPropertyFloat height = new GUIPropertyFloat("Height", "Height in cm", 0.0, 0.0, 20.0, 0.1); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyFloat implements ChangeListener {

	// property attributes
	private String myName;
	private String myDescription;
	private float myValue;
	private GuiNotificationTarget myTarget;
	// GUI attributes
	private float myMinValue;
	private float myMaxValue;
	private float myStepSize;
	private JSpinner mySpinner;
	private SpinnerNumberModel myModel;

	/**
	 * Creates a float property without a discription and notification.
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
	public GUIPropertyFloat(String name, float defaultValue, float minValue,
			float maxValue, float stepSize) {
		this(name, null, defaultValue, minValue, maxValue, stepSize);
	}

	/**
	 * Creates a float property without a notification.
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
	public GUIPropertyFloat(String name, String description,
			float defaultValue, float minValue, float maxValue, float stepSize) {
		this(name, description, defaultValue, minValue, maxValue, stepSize,
				null);
	}

	/**
	 * Creates a float property without a discription.
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
	public GUIPropertyFloat(String name, float defaultValue, float minValue,
			float maxValue, float stepSize, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, stepSize, target);
	}

	/**
	 * Creates a float property.
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
	public GUIPropertyFloat(String name, String description,
			float defaultValue, float minValue, float maxValue, float stepSize,
			GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myTarget = target;
		myMinValue = minValue;
		myMaxValue = maxValue;
		myStepSize = stepSize;
		// GUI attributes
		// myModel = new SpinnerNumberModel(myValue /*initial value*/, minValue
		// /*min*/, maxValue /*max*/, stepSize /*step*/);
		// mySpinner = new JSpinner(myModel);
		// mySpinner.setSize(new Dimension(100, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setPreferredSize(new Dimension(100, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setMaximumSize(new Dimension(100, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setMinimumSize(new Dimension(100, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// myModel.addChangeListener(this);
	}

	/**
	 * The method automatically invoked when changing the spinner status.
	 * 
	 * @param e
	 *            The passed change event (not used).
	 */
	public void stateChanged(ChangeEvent e) {
		myValue = myModel.getNumber().floatValue();
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
	public float getValue() {
		return myValue;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (myModel == null) {
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
