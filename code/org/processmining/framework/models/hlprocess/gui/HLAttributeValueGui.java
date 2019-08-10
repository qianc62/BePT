package org.processmining.framework.models.hlprocess.gui;

import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * GUI for some attribute value.
 * <p>
 * The parent will be automatically notified for changes, if it has been
 * provided.
 */
public abstract class HLAttributeValueGui implements GuiDisplayable,
		GuiNotificationTarget {

	protected HLAttributeGui parent;

	/**
	 * Creates a new attribute GUI and registers the given parent.
	 * <p>
	 * The parent will be automatically notified for changes.
	 * 
	 * @param theParent
	 */
	protected HLAttributeValueGui(HLAttributeGui theParent) {
		parent = theParent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		if (parent != null) {
			parent.updateGUI();
		}
	}

	/**
	 * Sub classes should return the attribute value they are providing the GUI
	 * for.
	 */
	public abstract HLAttributeValue getValue();

}
