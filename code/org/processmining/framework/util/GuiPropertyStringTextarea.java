package org.processmining.framework.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * This simple string description property that can be readily displayed as it
 * maintains its own GUI panel. The property will be graphically represented as
 * a (multiple line) text area that cannot be edited. It will stretch across the
 * whole width of the enclosing panel, and words will automatically wrapped at
 * the end of each line. <br>
 * <br>
 * A typical usage scenario looks as follows: <br>
 * <br>
 * <code>
 * JPanel testPanel = new Panel(); // create parent panel <br>
 * testPanel.setLayout(new BoxLayout(testPanel, BoxLayout.PAGE_AXIS)); <br>
 * GuiPropertyStringTextarea name = new GuiPropertyStringTextarea("Test description"); <br>
 * testPanel.add(name.getPropertyPanel()); // add one property <br>
 * GuiPropertyStringTextarea city = new GuiPropertyStringTextarea("Another test description"); <br>
 * testPanel.add(city.getPropertyPanel()); // add another property <br>
 * return testPanel; <br>
 * <code/>
 * 
 * @see getValue
 * @see getPropertyPanel
 */
public class GuiPropertyStringTextarea {

	// property attributes
	protected String myValue;
	protected int myHeight;
	// GUI attributes
	protected JTextArea myTextArea;

	/**
	 * Creates a string description property with default height value.
	 * 
	 * @param defaultValue
	 *            the default value of this property
	 */
	public GuiPropertyStringTextarea(String defaultValue) {
		this(defaultValue, 1);
	}

	/**
	 * Creates a string description property with given height value.
	 * 
	 * @param defaultValue
	 *            the default value of this property
	 * @param target
	 *            the object to be notified as soon the state of this property
	 *            changes
	 * @param width
	 *            a custom width may be specified (default value is 100
	 *            otherwise)
	 */
	public GuiPropertyStringTextarea(String defaultValue, int height) {
		myValue = defaultValue;
		myHeight = height;
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
		if (myTextArea == null) {
			myTextArea = new JTextArea();
			myTextArea.setMargin(new Insets(5, 5, 5, 5));
			myTextArea.setText(myValue);
			myTextArea.setEditable(false);
			myTextArea.setLineWrap(true);
			myTextArea.setWrapStyleWord(true);
			myTextArea.setBackground(new Color(220, 220, 220));
			myTextArea.setFont(new Font("Serif", Font.PLAIN, 14));
		}
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		resultPanel.add(myTextArea);
		resultPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		return resultPanel;
	}

}
