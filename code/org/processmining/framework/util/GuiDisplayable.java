package org.processmining.framework.util;

import javax.swing.JPanel;

/**
 * This interface ensures that the implementing class provides a method to
 * display itself.
 * 
 * @see GenericTableModelPanel
 * 
 * @author arozinat
 */
public interface GuiDisplayable {

	/**
	 * Retrieves the GUI panel representing this object.
	 * 
	 * @return the GUI panel representing this object
	 */
	public JPanel getPanel();

	/**
	 * Implementing classes should specify the toString() value in a meaningful
	 * way as this will be used in Gui properties to display the name of this
	 * gui object.
	 * 
	 * @return the name of what this gui displayable represents
	 */
	public String toString();
}
