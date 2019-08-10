package org.processmining.framework.models.hlprocess.view;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanDistribution;
import org.processmining.framework.models.hlprocess.gui.att.HLBooleanDistributionGui;
import org.processmining.framework.util.GuiDisplayable;

public class HLAttributeBooleanView implements GuiDisplayable {

	protected HLBooleanAttribute parent;
	protected HLBooleanDistributionGui possibleValuesBoolean;
	protected JPanel panel;

	/**
	 * Default constructor. Creates object based on values of associated
	 * HLAttribute object.
	 */
	public HLAttributeBooleanView(HLBooleanAttribute theParent) {
		parent = theParent;
		possibleValuesBoolean = new HLBooleanDistributionGui(
				(HLBooleanDistribution) parent.getPossibleValues(), null);
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
			// JPanel labelPanel = new JPanel();
			// labelPanel.setLayout(new BoxLayout(labelPanel,
			// BoxLayout.LINE_AXIS));
			// JLabel label = new JLabel("Possible Values");
			// label.setToolTipText("The distribution of possible values observed or specified for this attribute");
			// labelPanel.add(label);
			// labelPanel.add(Box.createHorizontalGlue());
			// panel.add(labelPanel);
			panel.add(possibleValuesBoolean.getPanel());
		}
		return panel;
	}

}
