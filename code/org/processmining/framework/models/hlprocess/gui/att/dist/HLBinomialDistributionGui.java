package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLBinomialDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyIntegerTextField;

/**
 * Represents a bernoulli distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by two spinners representing the number of experiments value and the
 * probability, respectively. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLBinomialDistributionGui extends HLDistributionGui {

	protected GUIPropertyIntegerTextField myNumberExperiments;
	protected GUIPropertyDoubleTextField myProbability;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLBinomialDistributionGui(HLBinomialDistribution theParent) {
		super(theParent);
		myNumberExperiments = new GUIPropertyIntegerTextField(
				"Number of experiments", ((HLBinomialDistribution) parent)
						.getNumberOfExperiments(), 1, true, this);
		myProbability = new GUIPropertyDoubleTextField("Probability value",
				((HLBinomialDistribution) parent).getProbability(), 0.0, 1.0);
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
	public HLBinomialDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myNumberExperiments = new GUIPropertyIntegerTextField(
				"Number of experiments", ((HLGeneralDistribution) parent)
						.getNumberExperiments(), 1, true, this);
		myProbability = new GUIPropertyDoubleTextField("Probability value",
				((HLGeneralDistribution) parent).getProbability(), 0.0, 1.0);
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
		resultPanel.add(myNumberExperiments.getPropertyPanel());
		resultPanel.add(myProbability.getPropertyPanel());
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
		if (parent instanceof HLBinomialDistribution) {
			((HLBinomialDistribution) parent)
					.setNumberOfExperiments(myNumberExperiments.getValue());
			((HLBinomialDistribution) parent).setProbability(myProbability
					.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent)
					.setNumberExperiments(myNumberExperiments.getValue());
			((HLGeneralDistribution) parent).setProbability(myProbability
					.getValue());
		}
	}

}
