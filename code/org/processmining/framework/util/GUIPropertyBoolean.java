package org.processmining.framework.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * A boolean property that can be readily displayed as it maintains its own GUI
 * panel. The property will be graphically represented as a check box. If a
 * description has been provided, it will be displayed as a tool tip. <br>
 * Changes performed via the GUI will be immediately propagated to the
 * internally held property value. Furthermore, a {@link GuiNotificationTarget
 * notification target} may be specified in order to be informed as soon as the
 * value has been changed. <br>
 * <br>
 * A typical usage scenario looks as follows: <br>
 * <br>
 * <code>
 * JPanel testPanel = new Panel(); // create parent panel <br>
 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS)); <br>
 * GUIPropertyBoolean vegetarian = new GUIPropertyBoolean("Vegetarian", false); <br>
 * testPanel.add(vegetarian.getPropertyPanel()); // add one property <br>
 * GUIPropertyBoolean breakfast = new GUIPropertyBoolean("Breakfast", "5 Euro extra", true); <br>
 * testPanel.add(breakfast.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GUIPropertyBoolean {

	// property attributes
	private String myName;
	private String myDescription;
	private GuiNotificationTarget myTarget;
	// GUI attributes
	private JPanel myPanel;
	private JCheckBox myCheckBox;

	/**
	 * Creates a boolean property without a description and notification.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GUIPropertyBoolean(String name, boolean defaultValue) {
		this(name, null, defaultValue, null);
	}

	/**
	 * Creates a boolean property without a description.
	 * 
	 * @param name
	 *            the name of this property
	 * @param defaultValue
	 *            the default value of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 */
	public GUIPropertyBoolean(String name, boolean defaultValue,
			GuiNotificationTarget target) {
		this(name, null, defaultValue, target);
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
	public GUIPropertyBoolean(String name, String description,
			boolean defaultValue) {
		this(name, description, defaultValue, null);
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
	public GUIPropertyBoolean(String name, String description,
			boolean defaultValue, GuiNotificationTarget target) {
		myName = name;
		myDescription = description;
		myTarget = target;
		// initialize GUI part
		initialize(defaultValue);
	}

	/**
	 * The method to be invoked when the value of this property is to be used.
	 * 
	 * @return the current value of this property
	 */
	public boolean getValue() {
		return myCheckBox.isSelected();
	}

	/**
	 * Prevents that this property may be manipulated via the GUI panel. <br>
	 * Note, however, that invoking this method will only have any effect as
	 * soon as the GUI panel (and therefore the checkbox object will be built).
	 * 
	 * @see #enable()
	 */
	public void disable() {
		myCheckBox.setEnabled(false);
	}

	/**
	 * Re-activates the possibility to manipulate this property via the GUI
	 * panel.
	 * 
	 * @see #disable()
	 */
	public void enable() {
		myCheckBox.setEnabled(true);
	}

	/**
	 * Sets the state of the boolean property.
	 * 
	 * @param b
	 *            boolean true if the radio button needs to be selected,
	 *            otherwise false
	 */
	public void setSelected(boolean b) {
		myCheckBox.setSelected(b);
	}

	/**
	 * Check whether the checkbox is enabled or disabled
	 * 
	 * @return boolean true if the checkbox is enabled, false otherwise
	 */
	public boolean isEnabled() {
		return myCheckBox.isEnabled();
	}

	/**
	 * Creates GUI panel containing this property, ready to display in some
	 * settings dialog.
	 * 
	 * @return the graphical panel representing this property
	 */
	public JPanel getPropertyPanel() {
		return myPanel;
	}

	protected void initialize(boolean value) {
		myCheckBox = new JCheckBox(myName);
		myCheckBox.setSelected(value);
		myCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (myTarget != null) {
					myTarget.updateGUI();
				}
			}
		});
		myCheckBox.setOpaque(false);
		myPanel = new JPanel();
		myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.LINE_AXIS));
		myPanel.setBorder(BorderFactory.createEmptyBorder());
		myPanel.setOpaque(false);
		myPanel.add(myCheckBox);
		if (myDescription != null) {
			myCheckBox.setToolTipText(myDescription);
		}
		// align left
		myPanel.add(Box.createHorizontalGlue());
	}
}
