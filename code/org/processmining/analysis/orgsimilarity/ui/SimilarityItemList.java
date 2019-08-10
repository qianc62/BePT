/*
 * Copyright (c) 2007 Minseok Song
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.orgsimilarity.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.orgsimilarity.SimilarityItem;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.util.RuntimeUtils;

/**
 * @author Minseok Song
 */
public class SimilarityItemList extends RoundedPanel {

	private static final long serialVersionUID = 2015846398013137615L;

	protected static Color COLOR_BG = new Color(140, 140, 140);
	protected static Color COLOR_FG = new Color(30, 30, 30);
	protected static Color COLOR_ITEM_BG = new Color(60, 60, 60);
	protected static Color COLOR_ITEM_HL = new Color(100, 100, 100);
	protected static Color COLOR_ITEM_BG_SELECTED = new Color(20, 20, 20);
	protected static Color COLOR_ITEM_HL_SELECTED = new Color(80, 80, 80);
	protected static Color COLOR_ITEM_FG = new Color(200, 200, 200);

	protected SimilarityUI ui;
	protected DefaultListModel itemListModel;
	protected JList itemList;
	protected DefaultComboBoxModel modelBoxModel;
	protected JComboBox modelBox;
	protected JTextField itemNameField;

	public SimilarityItemList(SimilarityUI similarityUI) {
		super(10, 5, 5);
		setBackground(COLOR_BG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		ui = similarityUI;
		itemListModel = new DefaultListModel();
		itemList = new JList(itemListModel);
		itemList.setBackground(new Color(100, 100, 100));
		itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		itemList.setCellRenderer(new SimilarityItemListRenderer());
		modelBoxModel = new DefaultComboBoxModel(ui.getOrgModels().keySet()
				.toArray());
		modelBox = new JComboBox(modelBoxModel);
		modelBox.setMaximumSize(new Dimension(1000, 24));
		modelBox.setPreferredSize(new Dimension(300, 24));
		modelBox.setMinimumSize(new Dimension(100, 24));
		if (RuntimeUtils.isRunningMacOsX() == true) {
			modelBox.setOpaque(false);
		}
		// create adding configuration
		add(Box.createVerticalStrut(8));
		itemNameField = new JTextField();
		itemNameField.setMaximumSize(new Dimension(1000, 24));
		itemNameField.setPreferredSize(new Dimension(300, 24));
		itemNameField.setMinimumSize(new Dimension(100, 24));
		itemNameField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addItem();
			}
		});
		add(wrapNamedComponent("Org Model name:", itemNameField));
		add(Box.createVerticalStrut(5));
		add(wrapNamedComponent("Org Model:", modelBox));
		add(Box.createVerticalStrut(5));
		JButton addButton = new SlickerButton("add new organizational model");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			addButton.setOpaque(false);
		}
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				addItem();
			}
		});
		add(wrapNamedComponent(null, addButton));
		add(Box.createVerticalStrut(8));
		// create list of items
		JScrollPane listScrollPane = new JScrollPane(itemList);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		listScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		listScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		add(listScrollPane);
		// add remove button at bottom
		add(Box.createVerticalStrut(8));
		JButton removeButton = new SlickerButton("remove selected item");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			removeButton.setOpaque(false);
		}
		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				removeItem();
			}
		});
		add(wrapNamedComponent(null, removeButton));
	}

	public List<SimilarityItem> getSimilarityItems() {
		ArrayList<SimilarityItem> items = new ArrayList<SimilarityItem>();
		for (int i = 0; i < itemListModel.getSize(); i++) {
			items.add((SimilarityItem) itemListModel.get(i));
		}
		return items;
	}

	protected void addItem() {
		String itemName = itemNameField.getText().trim();
		String modelName = (String) modelBox.getSelectedItem();
		if (itemName != null && modelName != null && itemName.length() > 0
				&& modelName.length() > 0) {
			// valid element to be added
			OrgModel model = ui.getOrgModels().get(modelName);
			SimilarityItem item = new SimilarityItem(itemName, model, modelName);
			// add to list model and remove from input set
			itemListModel.addElement(item);
			modelBoxModel.removeElement(modelName);
			itemNameField.setText("");
			ui.checkStartEnabled();
		}
	}

	protected void removeItem() {
		SimilarityItem item = (SimilarityItem) itemList.getSelectedValue();
		if (item != null) {
			// remove element and make model available in combo box again
			itemListModel.removeElement(item);
			modelBoxModel.addElement(item.getModelName());
			ui.checkStartEnabled();
		}
	}

	protected static JPanel wrapNamedComponent(String name, JComponent component) {
		JPanel wrapped = new JPanel();
		wrapped.setMaximumSize(new Dimension(2000, 30));
		wrapped.setMinimumSize(new Dimension(100, 30));
		wrapped.setPreferredSize(new Dimension(300, 30));
		wrapped.setBorder(BorderFactory.createEmptyBorder());
		wrapped.setOpaque(false);
		wrapped.setLayout(new BoxLayout(wrapped, BoxLayout.X_AXIS));
		if (name != null) {
			JLabel title = new JLabel(name);
			title.setOpaque(false);
			title.setForeground(COLOR_FG);
			wrapped.add(title);
			wrapped.add(Box.createHorizontalStrut(10));
			wrapped.add(component);
			wrapped.add(Box.createHorizontalGlue());
		} else {
			wrapped.add(Box.createHorizontalGlue());
			wrapped.add(component);
		}
		return wrapped;
	}

	protected class SimilarityItemListRenderer implements ListCellRenderer {

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {
			SmoothPanel cell = new SmoothPanel();
			if (isSelected == true) {
				cell.setBackground(COLOR_ITEM_BG_SELECTED);
				cell.setHighlight(COLOR_ITEM_HL_SELECTED);
			} else {
				cell.setBackground(COLOR_ITEM_BG);
				cell.setHighlight(COLOR_ITEM_HL);
			}
			cell.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
			cell.setMinimumSize(new Dimension(80, 30));
			cell.setMaximumSize(new Dimension(2000, 30));
			cell.setPreferredSize(new Dimension(200, 30));
			cell.setBorderAlpha(120);
			cell.setLayout(new BoxLayout(cell, BoxLayout.X_AXIS));
			String name = ((SimilarityItem) value).getName();
			JLabel label = new JLabel(name);
			label.setForeground(COLOR_ITEM_FG);
			label.setOpaque(false);
			cell.add(label);
			cell.add(Box.createHorizontalGlue());
			return cell;
		}
	}
}
