package org.processmining.framework.models.hlprocess.gui.att;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLBooleanDistribution;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * Represents the GUI of a boolean distribution. Allows to specify the relative
 * frequency of the possible values <code>true</code> and <code>false</code>.
 */
public class HLBooleanDistributionGui extends HLAttributeValueGui {

	// protected HLBooleanDistribution dist;
	protected GUIPropertyInteger trueValueFrequency;
	protected GUIPropertyInteger falseValueFrequency;
	protected JPanel panel;

	/**
	 * Constructor creating a default boolean distribution GUI.
	 * 
	 * @param parent
	 *            the parent attribute gui
	 */
	public HLBooleanDistributionGui(HLAttributeGui parent) {
		this(null, parent);
	}

	/**
	 * Constructor creating a GUI based on the given boolean distribution.
	 * 
	 * @param parent
	 *            the boolean distribution to be edited through this GUI
	 */
	public HLBooleanDistributionGui(HLBooleanDistribution dist,
			HLAttributeGui parent) {
		super(parent);
		if (dist != null) {
			trueValueFrequency = new GUIPropertyInteger(
					"true",
					"Increase the relative frequency to increase the likelihood of the value 'true'",
					dist.getFrequency(true), 0, Integer.MAX_VALUE, this, 200,
					true);
			falseValueFrequency = new GUIPropertyInteger(
					"false",
					"Increase the relative frequency to increase the likelihood of the value 'false'",
					dist.getFrequency(false), 0, Integer.MAX_VALUE, this, 200,
					true);
		} else {
			trueValueFrequency = new GUIPropertyInteger(
					"true",
					"Increase the relative frequency to increase the likelihood of the value 'true'",
					1, 0, Integer.MAX_VALUE, this, 200, true);
			falseValueFrequency = new GUIPropertyInteger(
					"false",
					"Increase the relative frequency to increase the likelihood of the value 'false'",
					1, 0, Integer.MAX_VALUE, this, 200, true);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 0));
			JPanel labelPanel = new JPanel();
			labelPanel
					.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
			JLabel label = new JLabel("Boolean Distribution:");
			label
					.setToolTipText("<HTML>Specifies the relative frequency of <i>true</i> and <i>false</i> values<br>"
							+ "If both frequencies are equal, then the likelihood for each of the values is 0.5</HTML>");
			label.setForeground(new Color(100, 100, 100));
			labelPanel.add(label);
			labelPanel.add(Box.createHorizontalGlue());
			panel.add(labelPanel);
			panel.add(trueValueFrequency.getPropertyPanel());
			panel.add(falseValueFrequency.getPropertyPanel());
		}
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui#
	 * disable()
	 */
	public void disable() {
		trueValueFrequency.disable();
		falseValueFrequency.disable();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui#
	 * getValue()
	 */
	public HLBooleanDistribution getValue() {
		HLBooleanDistribution result = new HLBooleanDistribution();
		result.setTrueFrequency(trueValueFrequency.getValue());
		result.setFalseFrequency(falseValueFrequency.getValue());
		return result;
	}

	// /*
	// * (non-Javadoc)
	// * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	// */
	// public void updateGUI() {
	// parent.setTrueFrequency(trueValueFrequency.getValue());
	// parent.setFalseFrequency(falseValueFrequency.getValue());
	// }

}
