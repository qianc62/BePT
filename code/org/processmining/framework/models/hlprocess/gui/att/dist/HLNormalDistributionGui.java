package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLNormalDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;

/**
 * Represents a normal distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by two spinners representing the mean value and the variance, respectively. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLNormalDistributionGui extends HLDistributionGui {

	protected GUIPropertyDoubleTextField myMean;
	protected GUIPropertyDoubleTextField myVariance;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLNormalDistributionGui(HLNormalDistribution theParent) {
		super(theParent);
		myMean = new GUIPropertyDoubleTextField("Mean value",
				((HLNormalDistribution) parent).getMean(), this);
		myVariance = new GUIPropertyDoubleTextField("Variance",
				((HLNormalDistribution) parent).getVariance(), 0.0, true, this);
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
	public HLNormalDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myMean = new GUIPropertyDoubleTextField("Mean value",
				((HLGeneralDistribution) parent).getMean(), this);
		myVariance = new GUIPropertyDoubleTextField("Variance",
				((HLGeneralDistribution) parent).getVariance(), 0.0, true, this);
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
		resultPanel.add(myMean.getPropertyPanel());
		resultPanel.add(myVariance.getPropertyPanel());
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
		if (parent instanceof HLNormalDistribution) {
			((HLNormalDistribution) parent).setMean(myMean.getValue());
			((HLNormalDistribution) parent).setVariance(myVariance.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent).setMean(myMean.getValue());
			((HLGeneralDistribution) parent).setVariance(myVariance.getValue());
		}
	}

}
