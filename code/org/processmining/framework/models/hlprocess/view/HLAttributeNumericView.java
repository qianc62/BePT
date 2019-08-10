package org.processmining.framework.models.hlprocess.view;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.models.hlprocess.gui.att.HLNumericDistributionGui;
import org.processmining.framework.util.GuiDisplayable;

public class HLAttributeNumericView implements GuiDisplayable {

	protected HLNumericAttribute parent;
	protected HLNumericDistributionGui possibleValuesNumeric;
	protected JPanel panel;

	/**
	 * Default constructor. Creates object based on values of associated
	 * HLAttribute object.
	 */
	public HLAttributeNumericView(HLNumericAttribute theParent) {
		parent = theParent;
		possibleValuesNumeric = new HLNumericDistributionGui(parent
				.getPossibleValues(), null);
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
			// JLabel label = new JLabel(" Possible Values");
			// label.setToolTipText("The distribution of possible values observed or specified for this attribute");
			// labelPanel.add(label);
			// labelPanel.add(Box.createHorizontalGlue());
			// panel.add(labelPanel);
			panel.add(possibleValuesNumeric.getPanel());
		}
		return panel;
	}

}
