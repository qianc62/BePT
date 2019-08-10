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
 * A long property that can be readily displayed as it maintains its own GUI
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
 * GUIPropertyLong age = new GUIPropertyLong("Age", 0, 0, 100); <br>
 * testPanel.add(age.getPropertyPanel()); // add one property <br>
 * GUIPropertyLong height = new GUIPropertyLong("Height", "Height in cm", 0, 50, 250); <br>
 * testPanel.add(height.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyLong implements ChangeListener {

	// property attributes
	private String myName;
	private String myDescription;
	private long myValue;
	private long myMinValue;
	private long myMaxValue;
	private GuiNotificationTarget myTarget;
	// GUI attributes
	private JSpinner mySpinner;
	private SpinnerNumberModel myModel;

	/**
	 * Creates a long property without a description and notification.
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
	public GUIPropertyLong(String name, long defaultValue, long minValue,
			long maxValue) {
		this(name, null, defaultValue, minValue, maxValue);
	}

	/**
	 * Creates a long property without notification.
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
	public GUIPropertyLong(String name, String description, long defaultValue,
			long minValue, long maxValue) {
		this(name, description, defaultValue, minValue, maxValue, null);
	}

	/**
	 * Creates a long property without a description.
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
	public GUIPropertyLong(String name, long defaultValue, long minValue,
			long maxValue, GuiNotificationTarget target) {
		this(name, null, defaultValue, minValue, maxValue, target);
	}

	/**
	 * Creates a long property.
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
	public GUIPropertyLong(String name, String description, long defaultValue,
			long minValue, long maxValue, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myMinValue = minValue;
		myMaxValue = maxValue;
		myTarget = target;
		// GUI attributes
		// myModel = new SpinnerNumberModel(myValue /*initial value*/, minValue
		// /*min*/, maxValue /*max*/, 1 /*step*/);
		// mySpinner = new JSpinner(myModel);
		// mySpinner.setSize(new Dimension(70, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setPreferredSize(new Dimension(70, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setMaximumSize(new Dimension(70, (int)
		// mySpinner.getPreferredSize().getHeight()));
		// mySpinner.setMinimumSize(new Dimension(70, (int)
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
		myValue = myModel.getNumber().longValue();
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
	public long getValue() {
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
					myMinValue /* min */, myMaxValue /* max */, 1 /* step */);
			mySpinner = new JSpinner(myModel);
			mySpinner.setSize(new Dimension(70, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setPreferredSize(new Dimension(70, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMaximumSize(new Dimension(70, (int) mySpinner
					.getPreferredSize().getHeight()));
			mySpinner.setMinimumSize(new Dimension(70, (int) mySpinner
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
