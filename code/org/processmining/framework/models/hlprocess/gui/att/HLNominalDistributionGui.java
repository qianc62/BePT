package org.processmining.framework.models.hlprocess.gui.att;

import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNominalDistribution;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.util.GuiPropertyStringIntegerMap;

/**
 * Represents the GUI of a nominal distribution. Allows to specify the relative
 * frequencies of all possible values and to add and remove values.
 */
public class HLNominalDistributionGui extends HLAttributeValueGui {

	protected GuiPropertyStringIntegerMap possibleValuesNominal;
	protected JPanel panel;

	/**
	 * Default Constructor
	 * 
	 * @param parent
	 *            the boolean distribution to be edited through this GUI
	 */
	public HLNominalDistributionGui(HLNominalDistribution dist,
			HLAttributeGui parent) {
		super(parent);
		if (dist != null) {
			possibleValuesNominal = new GuiPropertyStringIntegerMap(null, null,
					dist.getValuesAndFrequencies(), this, true);
		} else {
			possibleValuesNominal = new GuiPropertyStringIntegerMap(null, null,
					new HashMap<String, Integer>(), this, true);
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
			JLabel label = new JLabel("Nominal Distribution:");
			label
					.setToolTipText("<HTML>Specifies the relative frequency of the nominal values<br>"
							+ "If all frequencies are equal, then each value is equally probable</HTML>");
			label.setForeground(new Color(100, 100, 100));
			labelPanel.add(label);
			labelPanel.add(Box.createHorizontalGlue());
			panel.add(labelPanel);
			panel.add(Box.createRigidArea(new Dimension(0, 2)));
			panel.add(possibleValuesNominal.getPropertyPanel());
		}
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
	public HLNominalDistribution getValue() {
		HLNominalDistribution result = (HLNominalDistribution) parent
				.getHLAttribute().getPossibleValues().clone();
		result.setPossibleValues(possibleValuesNominal.getAllValues());
		return result;
	}

}
