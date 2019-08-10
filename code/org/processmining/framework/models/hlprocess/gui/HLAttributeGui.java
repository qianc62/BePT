/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2007 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.framework.models.hlprocess.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.att.HLAttributeManager;
import org.processmining.framework.models.hlprocess.gui.att.HLAttributeGuiManager;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyString;
import org.processmining.framework.util.GuiDisplayable;
import org.processmining.framework.util.GuiNotificationTarget;

/**
 * Gui class for high-level attribute objects. <br>
 * Allows to view and edit the attribute characteristics through a graphical
 * user interface. <br>
 * Allows to choose a different type and transparently replaces the old
 * attribute by the one with the new type.
 * 
 * @see HLProcessGui
 */
public class HLAttributeGui implements GuiDisplayable, GuiNotificationTarget {

	// The actual data attribute reflected by this GUI class
	protected HLAttribute hlAttribute;
	// GUI structures
	protected GUIPropertyString name;
	protected JComboBox attType;
	protected GUIPropertyBoolean useInitialValue;
	protected HLAttributeValueGui initialValue;
	protected HLAttributeValueGui possibleValues;
	// GUI attributes
	protected JPanel initialValuePanel;
	protected JPanel possibleValuesPanel;
	protected JPanel panel;

	/**
	 * Creates a new Gui object that allows to view and manipulate the data
	 * attribute object.
	 * 
	 * @param att
	 *            the attribute object reflected by this gui object
	 */
	public HLAttributeGui(HLAttribute att) {
		hlAttribute = att;
		name = new GUIPropertyString("Attribute Name",
				"Specifies the name of this data attribute", hlAttribute
						.getName(), this, 200);
		useInitialValue = new GUIPropertyBoolean(
				"Use initial value",
				"Indicates whether a fixed initial or default value is available",
				hlAttribute.usesInitialValue(), this);
		attType = new JComboBox();
		attType.setSize(new Dimension(200, (int) attType.getPreferredSize()
				.getHeight()));
		attType.setPreferredSize(new Dimension(200, (int) attType
				.getPreferredSize().getHeight()));
		attType.setMaximumSize(new Dimension(200, (int) attType
				.getPreferredSize().getHeight()));
		attType.setMinimumSize(new Dimension(200, (int) attType
				.getPreferredSize().getHeight()));
		attType.addItem(AttributeType.Nominal);
		attType.addItem(AttributeType.Numeric);
		attType.addItem(AttributeType.Boolean);
		attType.setSelectedItem(hlAttribute.getType());
		initialValue = HLAttributeGuiManager.getAttributeValueGui(hlAttribute
				.getInitialValue(), this);
		possibleValues = HLAttributeGuiManager.getAttributeValueGui(hlAttribute
				.getPossibleValues(), this);
		initAttributePanel();
		registerGuiActionListener();
	}

	private void registerGuiActionListener() {
		attType.addActionListener(new ActionListener() {
			// specify the action to be taken when changing the attribute's type
			public void actionPerformed(ActionEvent e) {
				AttributeType type = (AttributeType) attType.getSelectedItem();
				hlAttribute = HLAttributeManager.switchAttributeType(type,
						hlAttribute);
				initAttributePanel();
				// update the GUI
				panel.removeAll();
				createPanel();
				panel.validate();
				panel.repaint();
			}
		});
	}

	private void initAttributePanel() {
		initialValue = HLAttributeGuiManager.getAttributeValueGui(hlAttribute
				.getInitialValue(), this);
		possibleValues = HLAttributeGuiManager.getAttributeValueGui(hlAttribute
				.getPossibleValues(), this);
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
			panel.setOpaque(false);
			createPanel();
		}
		return panel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() {
		// update the actual attribute properties that might have changed
		hlAttribute.setName(name.getValue());
		hlAttribute.useInitialValue(useInitialValue.getValue());
		if (initialValue.getValue() != null) { // null for nominal attributes as
			// long as there are no values
			hlAttribute.setInitialValue(initialValue.getValue());
		}
		hlAttribute.setPossibleValues(possibleValues.getValue());
		// update gui as, for example, new possible values might be available
		// for
		// initial value selection
		initAttributePanel();
		createInitialValuePanel();
		createPossibleValuesPanel();
		panel.validate();
		panel.repaint();
		SlickerSwingUtils.injectTransparency(panel);
	}

	/**
	 * Creates the Gui panel from its current values.
	 */
	protected void createPanel() {
		JPanel content = new JPanel();
		content.setLayout(new BoxLayout(content, BoxLayout.PAGE_AXIS));
		content.add(name.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		JPanel typePanel = new JPanel();
		typePanel.setLayout(new BoxLayout(typePanel, BoxLayout.LINE_AXIS));
		JLabel typeLabel = new JLabel(" Attribute Type");
		typePanel.add(typeLabel);
		typePanel.add(Box.createHorizontalGlue());
		typePanel.add(attType);
		content.add(typePanel);
		content.add(Box.createRigidArea(new Dimension(0, 5)));
		content.add(useInitialValue.getPropertyPanel());
		content.add(Box.createRigidArea(new Dimension(0, 5)));

		createInitialValuePanel();
		content.add(initialValuePanel);
		createPossibleValuesPanel();
		content.add(possibleValuesPanel);

		panel.add(Box.createHorizontalGlue());
		panel.add(content);
		panel.add(Box.createHorizontalGlue());
		SlickerSwingUtils.injectTransparency(panel);
	}

	private void createPossibleValuesPanel() {
		if (possibleValuesPanel == null) {
			possibleValuesPanel = new JPanel();
			possibleValuesPanel.setLayout(new BoxLayout(possibleValuesPanel,
					BoxLayout.PAGE_AXIS));
			possibleValuesPanel.setOpaque(false);
		}
		possibleValuesPanel.removeAll();
		JPanel possValuePanel = new JPanel();
		possValuePanel.setLayout(new BoxLayout(possValuePanel,
				BoxLayout.LINE_AXIS));
		JLabel possValLabel = new JLabel(" Possible values");
		possValuePanel.add(possValLabel);
		possValuePanel.add(Box.createHorizontalGlue());
		possibleValuesPanel.add(possValuePanel);
		possibleValuesPanel.add(possibleValues.getPanel());
		possibleValuesPanel.validate();
		possibleValuesPanel.repaint();
	}

	private void createInitialValuePanel() {
		if (initialValuePanel == null) {
			initialValuePanel = new JPanel();
			initialValuePanel.setLayout(new BoxLayout(initialValuePanel,
					BoxLayout.PAGE_AXIS));
			initialValuePanel.setOpaque(false);
		}
		initialValuePanel.removeAll();
		// only put initial value if should be used
		if (useInitialValue.getValue() == true) {

			JPanel initValuePanel = new JPanel();
			initValuePanel.setLayout(new BoxLayout(initValuePanel,
					BoxLayout.LINE_AXIS));
			JLabel initValLabel = new JLabel(" Initial value");
			initValuePanel.add(initValLabel);
			initValuePanel.add(Box.createHorizontalGlue());
			initialValuePanel.add(initValuePanel);
			initialValuePanel.add(initialValue.getPanel());
			initialValuePanel.add(Box.createRigidArea(new Dimension(0, 5)));
		}
		initialValuePanel.validate();
		initialValuePanel.repaint();
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
