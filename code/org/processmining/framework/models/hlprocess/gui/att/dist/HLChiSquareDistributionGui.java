package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLChiSquareDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyIntegerTextField;

/**
 * Represents a chi square distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by one spinner representing the degrees of freedom value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLChiSquareDistributionGui extends HLDistributionGui {

	protected GUIPropertyIntegerTextField myDegreesFreedom;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLChiSquareDistributionGui(HLChiSquareDistribution theParent) {
		super(theParent);
		myDegreesFreedom = new GUIPropertyIntegerTextField(
				"Degrees of freedom value", ((HLChiSquareDistribution) parent)
						.getDegreesFreedom(), 1, true, this);
	}

	/**
	 * Constructor providing a view onto the given meta distribution.
	 * <p>
	 * Changes will be propagated directly back to the HLGeneralDistribution
	 * object.
	 * 
	 * @param theParent
	 *            the meta distribution underlying this view
	 */
	public HLChiSquareDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myDegreesFreedom = new GUIPropertyIntegerTextField(
				"Degrees of freedom value", ((HLGeneralDistribution) parent)
						.getDegreesOfFreedom(), 1, true, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui
	 * #getPanel()
	 */
	public JPanel getPanel() {
		JPanel resultPanel = new JPanel();
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.PAGE_AXIS));
		resultPanel.add(myDegreesFreedom.getPropertyPanel());

		return resultPanel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui
	 * #updateGUI()
	 */
	public void updateGUI() {
		if (parent instanceof HLChiSquareDistribution) {
			((HLChiSquareDistribution) parent)
					.setDegreesFreedom(myDegreesFreedom.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent)
					.setDegreesOfFreedom(myDegreesFreedom.getValue());
		}
	}

}
