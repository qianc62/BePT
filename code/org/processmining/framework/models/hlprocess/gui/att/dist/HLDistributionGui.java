package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * GUI for some numeric distribution.
 */
public abstract class HLDistributionGui implements GuiDisplayable,
		GuiNotificationTarget {

	protected HLDistribution parent;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	protected HLDistributionGui(HLDistribution theParent) {
		parent = theParent;
	}

	/**
	 * Retrieves the distribution object represented by this GUI.
	 * 
	 * @return the distribution
	 */
	public HLDistribution getDistribution() {
		return parent;
	}

	/**
	 * Distribution GUIs are expected to provide an editable view on the give
	 * distribution.
	 */
	public abstract JPanel getPanel();

	/**
	 * Changes to distribution parameters via the GUI are expected to be
	 * propagated back to the distribution object held by this GUI.
	 */
	public abstract void updateGUI();

}
