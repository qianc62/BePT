package org.processmining.framework.models.hlprocess.view;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.models.hlprocess.gui.HLProcessGui;
import org.processmining.framework.util.GUIPropertyString;
import org.processmining.framework.util.GuiDisplayable;

/**
 * Gui class for high-level attribute objects. <br>
 * Allows to view but not to edit the attribute characteristics through a
 * graphical user interface.
 * 
 * @see HLProcessGui
 */
public class HLAttributeView implements GuiDisplayable {

	// The actual data attribute reflected by this GUI class
	protected HLAttribute hlAttribute;
	// GUI structures
	protected GUIPropertyString name;
	protected GuiDisplayable attPanel;
	protected JPanel panel;

	/**
	 * Creates a new Gui object that allows to view and manipulate the data
	 * attribute object.
	 * 
	 * @param att
	 *            the attribute object reflected by this gui object
	 */
	public HLAttributeView(HLAttribute att) {
		hlAttribute = att;
		name = new GUIPropertyString("Attribute Name",
				"Specifies the name of this data attribute", hlAttribute
						.getName(), null, 200, false);
		if (att instanceof HLBooleanAttribute) {
			attPanel = new HLAttributeBooleanView((HLBooleanAttribute) att);
		} else if (att instanceof HLNumericAttribute) {
			attPanel = new HLAttributeNumericView((HLNumericAttribute) att);
		} else if (att instanceof HLNominalAttribute) {
			attPanel = new HLAttributeNominalView((HLNominalAttribute) att);
		}
	}

	/**
	 * Retrieves the underlying high level attribute object.
	 * 
	 * @return the high level attribute for this gui object
	 */
	public HLAttribute getHLAttribute() {
		return hlAttribute;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiDisplayable#getPanel()
	 */
	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			createPanel();
		}
		return panel;
	}

	/**
	 * Creates the Gui panel from its current values.
	 */
	protected void createPanel() {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(name.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		JPanel labelPanel = new JPanel();
		labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.LINE_AXIS));
		JLabel label = new JLabel(" Observed Values");
		label
				.setToolTipText("The distribution of values observed for this attribute");
		labelPanel.add(label);
		labelPanel.add(Box.createHorizontalGlue());
		content.add(labelPanel);
		content.add(attPanel.getPanel());
		panel.add(Box.createHorizontalGlue());
		panel.add(content);
		panel.add(Box.createHorizontalGlue());
	}

	/**
	 * Returns the name of the attribute for this gui displayable.
	 * 
	 * @return the name of the attribute
	 */
	public String toString() {
		return hlAttribute.getName();
	}

}
