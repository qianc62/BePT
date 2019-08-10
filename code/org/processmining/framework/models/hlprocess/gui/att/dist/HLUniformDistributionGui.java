package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLUniformDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;

/**
 * Represents a uniform distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by two spinners representing the minimum value and the maximum, respectively. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values. It is assumed that the min value that is
 * provided will be less than the max value that is provided.
 */
public class HLUniformDistributionGui extends HLDistributionGui {

	private GUIPropertyDoubleTextField myMin;
	private GUIPropertyDoubleTextField myMax;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLUniformDistributionGui(HLUniformDistribution theParent) {
		super(theParent);
		myMin = new GUIPropertyDoubleTextField("Minimum value",
				((HLUniformDistribution) parent).getMin(), this);
		myMax = new GUIPropertyDoubleTextField("Maximum value",
				((HLUniformDistribution) parent).getMax(), this);
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
	public HLUniformDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myMin = new GUIPropertyDoubleTextField("Minimum value",
				((HLGeneralDistribution) parent).getMin(), this);
		myMax = new GUIPropertyDoubleTextField("Maximum value",
				((HLGeneralDistribution) parent).getMax(), this);
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
		resultPanel.add(myMin.getPropertyPanel());
		resultPanel.add(myMax.getPropertyPanel());
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
		if (parent instanceof HLUniformDistribution) {
			((HLUniformDistribution) parent).setMin(myMin.getValue());
			((HLUniformDistribution) parent).setMax(myMax.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent).setMin(myMin.getValue());
			((HLGeneralDistribution) parent).setMax(myMax.getValue());
		}
	}

}
