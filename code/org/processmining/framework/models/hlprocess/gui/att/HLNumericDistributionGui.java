package org.processmining.framework.models.hlprocess.gui.att;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNumericDistribution;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGui;
import org.processmining.framework.models.hlprocess.gui.att.dist.HLDistributionGuiManager;

/**
 * Represents the GUI of a numeric data attribute, i.e., the possible values
 * correspond to some numeric random distribution.
 */
public class HLNumericDistributionGui extends HLAttributeValueGui {

	protected HLDistributionGui possibleValuesNumeric;
	protected JPanel panel;

	/**
	 * Default Constructor.
	 * <p>
	 * Note that the given numeric distribution is assumed to be an
	 * HLGeneralDistribution.
	 * 
	 * @param parent
	 *            the numeric distribution to be edited through this GUI
	 */
	public HLNumericDistributionGui(HLNumericDistribution dist,
			HLAttributeGui parent) {
		super(parent);
		possibleValuesNumeric = HLDistributionGuiManager.getDistributionGui(
				dist.getValue(), this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
		}
		panel.removeAll();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 0));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
		JLabel label = new JLabel("Numeric Distribution:");
		label
				.setToolTipText("Specifies the distribution of the numeric values");
		label.setForeground(new Color(100, 100, 100));
		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());
		panel.add(labelPanel);
		panel.add(possibleValuesNumeric.getPanel());
		panel.validate();
		panel.repaint();
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui#
	 * getValue()
	 */
	public HLNumericDistribution getValue() {
		return new HLNumericDistribution(possibleValuesNumeric
				.getDistribution());
	}

}
