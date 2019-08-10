package org.processmining.framework.models.hlprocess.gui.att.dist;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution.DistributionEnum;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Provides an editable view on a general distribution object.
 * <p>
 * Allows to select the probabilistic view among different distributions.
 * <p>
 * If one particular distribution is desired (rather than a meta view), it
 * should be directly displayed instead.
 */
public class HLGeneralDistributionGui extends HLDistributionGui {

	protected JPanel myPanel;
	protected GUIPropertyListEnumeration myDistributions;
	protected GuiNotificationTarget myTarget;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLGeneralDistributionGui(HLGeneralDistribution theParent,
			GuiNotificationTarget target) {
		super(theParent);
		myTarget = target;
	}

	/**
	 * Creates GUI panel representing this general distribution, ready to
	 * display in some settings dialog. <br>
	 * Note that it will be possible to change between the different
	 * distribution views. If one particular distribution is desired, it should
	 * be requested and directly displayed instead.
	 * 
	 * @return the graphical panel representing this distribution
	 */
	public JPanel getPanel() {
		if (myPanel == null) {
			myPanel = new JPanel();
			myPanel.setLayout(new BoxLayout(myPanel, BoxLayout.PAGE_AXIS));
			myDistributions = new GUIPropertyListEnumeration(
					"Distribution Type",
					"Select one of the distributions available",
					((HLGeneralDistribution) parent)
							.getAvailableDistributions(), this, 200);
			myDistributions.setValue(((HLGeneralDistribution) parent)
					.getBestDistributionType());
			createPanel();
		}
		return myPanel;
	}

	private void createPanel() {
		// add distribution type
		myPanel.add(myDistributions.getPropertyPanel());
		myPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		// add current distribution panel
		DistributionEnum currentType = (DistributionEnum) myDistributions
				.getValue();
		HLDistributionGui distGui = HLDistributionGuiManager
				.getDistributionGui(currentType, (HLGeneralDistribution) parent);
		myPanel.add(distGui.getPanel());
		// set the value of best distribution
		((HLGeneralDistribution) parent)
				.setBestDistributionType((DistributionEnum) (myDistributions
						.getValue()));
		SlickerSwingUtils.injectTransparency(myPanel);
	}

	/**
	 * This method is called as soon the attribute type of this data attribute
	 * is changed. The reason is that the possible values and initial values are
	 * different for the different types of data attributes.
	 */
	public void updateGUI() {
		myPanel.removeAll();
		createPanel();
		myPanel.validate();
		myPanel.repaint();
		// parent GUI needs to be updated as distribution panel might have
		// different size etc.
		if (myTarget != null) {
			myTarget.updateGUI();
		}
	}

}
