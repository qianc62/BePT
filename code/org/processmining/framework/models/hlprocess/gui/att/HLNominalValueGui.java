package org.processmining.framework.models.hlprocess.gui.att;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNominalDistribution;
import org.processmining.framework.models.hlprocess.att.HLNominalValue;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.util.GUIPropertyListEnumeration;

/**
 * GUI for a nominal value.
 */
public class HLNominalValueGui extends HLAttributeValueGui {

	protected GUIPropertyListEnumeration value;
	protected JPanel panel;

	/**
	 * Default Constructor
	 * 
	 * @param parent
	 *            the boolean distribution to be edited through this GUI
	 */
	public HLNominalValueGui(HLNominalValue val, HLAttributeGui theParent) {
		super(theParent);
		HLNominalDistribution values = (HLNominalDistribution) theParent
				.getHLAttribute().getPossibleValues();
		value = new GUIPropertyListEnumeration("Value",
				"Please select the value you want", values.getValues(), this,
				200);
		if (val != null) {
			value.setValue(val);
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
			panel.add(value.getPropertyPanel());
		}
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui#
	 * getValue()
	 */
	public HLNominalValue getValue() {
		if (value.getValue() != null) {
			return new HLNominalValue((String) value.getValue());
		} else {
			return null;
		}
	}

}
