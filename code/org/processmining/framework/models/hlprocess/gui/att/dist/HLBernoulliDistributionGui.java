package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLBernoulliDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;

/**
 * Represents a bernoulli distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by one spinner representing the probability value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLBernoulliDistributionGui extends HLDistributionGui {

	protected GUIPropertyDoubleTextField myProbability;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLBernoulliDistributionGui(HLBernoulliDistribution theParent) {
		super(theParent);
		myProbability = new GUIPropertyDoubleTextField("Probability value",
				((HLBernoulliDistribution) parent).getProbability(), 0.0, 1.0,
				this);
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
	public HLBernoulliDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myProbability = new GUIPropertyDoubleTextField("Probability value",
				((HLGeneralDistribution) parent).getProbability(), 0.0, 1.0,
				this);
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
		if (parent instanceof HLBernoulliDistribution) {
			((HLBernoulliDistribution) parent).setProbability(myProbability
					.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent).setProbability(myProbability
					.getValue());
		}
	}

}
