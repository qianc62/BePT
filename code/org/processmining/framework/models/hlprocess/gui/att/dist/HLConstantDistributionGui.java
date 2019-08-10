package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLConstantDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;

/**
 * Represents a constant distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by one spinner representing the constant value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLConstantDistributionGui extends HLDistributionGui {

	protected GUIPropertyDoubleTextField myConstant;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLConstantDistributionGui(HLConstantDistribution theParent) {
		super(theParent);
		myConstant = new GUIPropertyDoubleTextField("Constant value",
				((HLConstantDistribution) parent).getConstant(), this);
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
	public HLConstantDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myConstant = new GUIPropertyDoubleTextField("Constant value",
				((HLGeneralDistribution) parent).getConstant(), this);
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
		resultPanel.add(myConstant.getPropertyPanel());
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
		if (parent instanceof HLConstantDistribution) {
			((HLConstantDistribution) parent)
					.setConstant(myConstant.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent).setConstant(myConstant.getValue());
		}
	}

}
