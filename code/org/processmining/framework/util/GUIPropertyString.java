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
 * A string property that can be readily displayed as it maintains its own GUI
 * panel. The property will be graphically represented as a (single line) text
 * field that can be edited. If a description has been provided, it will be
 * displayed as a tool tip. <br>
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
 * GUIPropertyString name = new GUIPropertyString("Name", "Anne"); <br>
 * testPanel.add(name.getPropertyPanel()); // add one property <br>
 * GUIPropertyString city = new GUIPropertyString("City", "Eindhoven"); <br>
 * testPanel.add(city.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyString implements KeyListener {

	// property attributes
	protected String myName;
	protected String myDescription;
	protected String myValue;
	protected int myWidth;
	protected GuiNotificationTarget myTarget;
	protected boolean myEditable;
	// GUI attributes
	protected JTextField myTextField;

	/**
	 * Creates a boolean property without a discription and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyString(String name, String defaultValue) {
		this(name, null, defaultValue);
	}

	/**
	 * Creates a boolean property without notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param description
	 *            of this property (to be displayed as a tool tip)
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyString(String name, String description,
			String defaultValue) {
		this(name, description, defaultValue, null, 100);
	}

	/**
	 * Creates a boolean property without a discription.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyString(String name, String defaultValue,
			GuiNotificationTarget target) {
		this(name, null, defaultValue, target, 100);
	}

	/**
	 * Creates a boolean property.
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
	public GUIPropertyString(String name, String description,
			String defaultValue, GuiNotificationTarget target) {
		this(name, description, defaultValue, target, 100);
	}

	/**
	 * Creates a boolean property.
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
	 * @param width
	 *            a custom width may be specified (default value is 100
	 *            otherwise)
	 */
	public GUIPropertyString(String name, String description,
			String defaultValue, GuiNotificationTarget target, int width) {
		this(name, description, defaultValue, target, width, true);
	}

	/**
	 * Creates a boolean property.
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
	 * @param width
	 *            a custom width may be specified (default value is 100
	 *            otherwise)
	 * @param editable
	 *            whether the string should be editable or not
	 */
	public GUIPropertyString(String name, String description,
			String defaultValue, GuiNotificationTarget target, int width,
			boolean editable) {
		myName = name;
		myDescription = description;
		myValue = defaultValue;
		myWidth = width;
		myTarget = target;
		myEditable = editable;
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
		// not used
		myValue = myTextField.getText();
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
	public String getValue() {
		return myValue;
	}

	/**
	 * Creates GUI panel containg this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		if (myTextField == null) {
			myTextField = new JTextField();
			myTextField.setSize(new Dimension(myWidth, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setPreferredSize(new Dimension(myWidth,
					(int) myTextField.getPreferredSize().getHeight()));
			myTextField.setMaximumSize(new Dimension(myWidth, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setMinimumSize(new Dimension(myWidth, (int) myTextField
					.getPreferredSize().getHeight()));
			myTextField.setText(myValue);
			myTextField.addKeyListener(this);
			myTextField.setEditable(myEditable);
		}
		JPanel resultPanel = new JPanel();
		resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
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
