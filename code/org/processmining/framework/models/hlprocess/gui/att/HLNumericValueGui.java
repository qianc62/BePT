package org.processmining.framework.models.hlprocess.gui.att;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.util.GUIPropertyInteger;

/**
 * GUI for numeric value.
 */
public class HLNumericValueGui extends HLAttributeValueGui {

	protected GUIPropertyInteger value;
	protected JPanel panel;

	/**
	 * Default Constructor
	 * 
	 * @param parent
	 *            the boolean distribution to be edited through this GUI
	 */
	public HLNumericValueGui(HLNumericValue val, HLAttributeGui theParent) {
		super(theParent);
		if (val != null) {
			value = new GUIPropertyInteger("Value",
					"Please insert the value you want", val.getValue(), this,
					200, true);
		} else {
			value = new GUIPropertyInteger("Value",
					"Please insert the value you want", 0, this, 200, true);
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
	public HLNumericValue getValue() {
		return new HLNumericValue(value.getValue());
	}

}
