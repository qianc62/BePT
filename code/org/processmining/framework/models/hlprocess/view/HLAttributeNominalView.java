package org.processmining.framework.models.hlprocess.view;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiPropertyStringIntegerMap;

/**
 * Represents the GUI of a nominal data attribute, i.e., the possible values
 * correspond to an enumeration of String values.
 */
public class HLAttributeNominalView implements GuiDisplayable {

	protected HLNominalAttribute parent;
	protected GuiPropertyStringIntegerMap possibleValuesNominal;
	protected JPanel panel;

	/**
	 * Default constructor. Creates object based on values of associated
	 * HLAttribute object.
	 */
	public HLAttributeNominalView(HLNominalAttribute theParent) {
		parent = theParent;
		possibleValuesNominal = new GuiPropertyStringIntegerMap(null, null,
				parent.getPossibleValues().getValuesAndFrequencies(), null,
				false);
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
			fillPanel();
		}
		return panel;
	}

	/**
	 * Fills the property panel with current values (assumes existing panel).
	 */
	protected void fillPanel() {
		// JPanel labelPanel = new JPanel();
		// labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
		// JLabel label = new JLabel("Observed Values");
		// label.setToolTipText("The distribution of values observed for this attribute");
		// labelPanel.add(label);
		// labelPanel.add(Box.createHorizontalGlue());
		// panel.add(labelPanel);

		// panel.add(possibleValuesNominal.getPanel());
		// panel = new JPanel();
		// panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		// panel.setBorder(BorderFactory.createEmptyBorder(5, 20, 15, 0));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
		JLabel label = new JLabel("Nominal distribution:");
		label.setForeground(new Color(100, 100, 100));
		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());
		panel.add(labelPanel);
		panel.add(Box.createRigidArea(new Dimension(0, 5)));
		panel.add(possibleValuesNominal.getPropertyPanel());
	}

}
