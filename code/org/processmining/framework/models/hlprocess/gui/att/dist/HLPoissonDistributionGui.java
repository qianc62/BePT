package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLPoissonDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;

/**
 * Represents a poisson distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by one spinner representing the intensity value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLPoissonDistributionGui extends HLDistributionGui {

	protected GUIPropertyDoubleTextField myIntensity;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLPoissonDistributionGui(HLPoissonDistribution theParent) {
		super(theParent);
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLPoissonDistribution) parent).getIntensity(),
				0.00000000000000001, true, this);
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
	public HLPoissonDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLGeneralDistribution) parent).getIntensity(),
				0.00000000000000001, true, this);
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
		resultPanel.add(myIntensity.getPropertyPanel());
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
		if (parent instanceof HLPoissonDistribution) {
			((HLPoissonDistribution) parent).setIntensity(myIntensity
					.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLPoissonDistribution) parent).setIntensity(myIntensity
					.getValue());
		}
	}

}
