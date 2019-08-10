package org.processmining.framework.models.hlprocess.gui.att.dist;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.distribution.HLErlangDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyIntegerTextField;

/**
 * Represents an erlang distribution that can be readily displayed as it
 * maintains its own GUI panel. The distribution will be graphically represented
 * by two spinners representing the number of drawings and the intensity value. <br>
 * Changes performed via the GUI will be immedeately propagated to the
 * internally held property values.
 */
public class HLErlangDistributionGui extends HLDistributionGui {

	protected GUIPropertyIntegerTextField myEmergenceEvents;
	protected GUIPropertyDoubleTextField myIntensity;

	/**
	 * Default Constructor.
	 * 
	 * @param theParent
	 *            the distribution to be made editable via this GUI
	 */
	public HLErlangDistributionGui(HLErlangDistribution theParent) {
		super(theParent);
		myEmergenceEvents = new GUIPropertyIntegerTextField(
				"Emergence of n events", ((HLErlangDistribution) parent)
						.getEmergenceOfEvents(), 1, true, this);
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLErlangDistribution) parent).getIntensity(),
				0.00000000000001, true, this);
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
	public HLErlangDistributionGui(HLGeneralDistribution theParent) {
		super(theParent);
		myEmergenceEvents = new GUIPropertyIntegerTextField(
				"Emergence of n events", ((HLGeneralDistribution) parent)
						.getEmergenceofEvents(), 1, true, this);
		myIntensity = new GUIPropertyDoubleTextField("Intensity value",
				((HLGeneralDistribution) parent).getIntensity(),
				0.00000000000001, true, this);
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
		resultPanel.add(myEmergenceEvents.getPropertyPanel());
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
		if (parent instanceof HLErlangDistribution) {
			((HLErlangDistribution) parent)
					.setEmergenceOfEvents(myEmergenceEvents.getValue());
			((HLErlangDistribution) parent)
					.setIntensity(myIntensity.getValue());
		} else if (parent instanceof HLGeneralDistribution) {
			((HLGeneralDistribution) parent)
					.setEmergenceOfEvents(myEmergenceEvents.getValue());
			((HLGeneralDistribution) parent).setIntensity(myIntensity
					.getValue());
		}
	}

}
