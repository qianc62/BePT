/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

package org.processmining.analysis.epc;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.ui.DoubleClickTable;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class InitialEventChoserPanel extends JPanel {

	HashMap initialEvents;
	ModelGraphPanel graphPanel;
	private SetTableModel gridModel;

	JSplitPane jSplitPaneMain = new JSplitPane();

	public InitialEventChoserPanel(HashMap initialEvents, ModelGraphPanel panel) {
		this.initialEvents = initialEvents;
		this.graphPanel = panel;

		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {
		gridModel = new SetTableModel("<html>Sets of initial<br>events</html>");

		setLayout(new BorderLayout());

		jSplitPaneMain.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		// Take care of the left part;
		JPanel leftPanel = new JPanel(new BorderLayout());

		JPanel eventPanelContainer = new JPanel(new GridBagLayout());
		HashSet eventPanels = new HashSet();

		Iterator it = initialEvents.keySet().iterator();
		int i = 0;
		while (it.hasNext()) {
			EventPanel p = new EventPanel((EPCEvent) it.next(), graphPanel);
			eventPanels.add(p);
			eventPanelContainer.add(p, new GridBagConstraints(0, i++, 1, 1,
					0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
					new Insets(0, 0, 0, 0), 0, 0));
		}
		JScrollPane eventPanelScrollPane = new JScrollPane(eventPanelContainer);

		leftPanel.add(eventPanelScrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout());
		JButton addButton = new JButton("Add to list >>>");
		addButton.addActionListener(new AddButtonActionListener(eventPanels,
				gridModel));
		buttonPanel.add(addButton);
		leftPanel.add(buttonPanel, BorderLayout.SOUTH);

		jSplitPaneMain.add(leftPanel, JSplitPane.LEFT);
		leftPanel.setPreferredSize(new Dimension(250, 40));
		leftPanel.setMinimumSize(new Dimension(250, 40));
		// Done with the left part

		// Do the right part:
		JPanel rightPanel = new JPanel(new BorderLayout());

		JButton showButton = new JButton("Show");

		JTable grid = new DoubleClickTable(showButton);
		grid.setModel(gridModel);
		grid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane gridScrollPane = new JScrollPane(grid);

		JPanel buttonPanel2 = new JPanel(new GridLayout(2, 1));

		showButton.addActionListener(new ShowSelButtonActionListener(grid,
				graphPanel));
		buttonPanel2.add(showButton);

		JButton removeButton = new JButton("Remove <<<");
		removeButton.addActionListener(new RemoveButtonActionListener(grid));
		buttonPanel2.add(removeButton);

		rightPanel.add(gridScrollPane, BorderLayout.CENTER);
		rightPanel.add(buttonPanel2, BorderLayout.SOUTH);

		rightPanel.setPreferredSize(removeButton.getSize());
		jSplitPaneMain.setResizeWeight(1.0);

		jSplitPaneMain.add(rightPanel, JSplitPane.RIGHT);

		// Done with the right part

		this.add(jSplitPaneMain, BorderLayout.CENTER);
	}

	public HashSet getInitialEventSets() {
		HashSet set = new HashSet();
		int i = 0;
		while (i < gridModel.getRowCount()) {
			set.add(gridModel.getSet(i));
			i++;
		}
		return set;
	}

}

class EventPanel extends JPanel {
	private EPCEvent event;
	private JCheckBox box;
	private JButton button;

	public EventPanel(EPCEvent e, ModelGraphPanel graphPanel) {
		event = e;
		String s = e.toString().replaceAll("\\\\" + "n", "<br>");

		setLayout(new BorderLayout());

		box = new JCheckBox("<html>" + s + "</html>");
		box.setSelected(true);
		JPanel butpan = new JPanel(new FlowLayout(FlowLayout.CENTER));
		button = new JButton("Show");
		ArrayList a = new ArrayList();
		a.add(e);
		button.addActionListener(new ShowButtonActionListener(a, graphPanel));
		butpan.add(button);

		add(butpan, BorderLayout.WEST);
		add(box, BorderLayout.EAST);
	}

	public EPCEvent getEvent() {
		return event;
	}

	public EPCEvent isSelected() {
		return (box.isSelected() ? event : null);
	}
}

class ShowButtonActionListener implements ActionListener {
	private Collection nodes;
	private ModelGraphPanel panel;

	public ShowButtonActionListener(Collection nodes, ModelGraphPanel panel) {
		this.nodes = nodes;
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent e) {
		panel.unSelectAll();
		panel.selectElements(nodes);
	}

}

class ShowSelButtonActionListener implements ActionListener {
	private JTable table;
	private ModelGraphPanel panel;

	public ShowSelButtonActionListener(JTable table, ModelGraphPanel panel) {
		this.table = table;
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent e) {
		panel.unSelectAll();
		if (table.getSelectedRow() != -1) {
			panel.selectElements(((SetTableModel) table.getModel())
					.getSet(table.getSelectedRow()));
		}
	}

}

class AddButtonActionListener implements ActionListener {
	private HashSet panels;
	private SetTableModel table;

	public AddButtonActionListener(HashSet eventPanels, SetTableModel table) {
		this.panels = eventPanels;
		this.table = table;
	}

	public void actionPerformed(ActionEvent e) {
		HashSet events = new HashSet();
		Iterator it = panels.iterator();
		while (it.hasNext()) {
			EventPanel p = (EventPanel) it.next();
			if (p.isSelected() != null) {
				events.add(p.getEvent());
			}
		}
		table.add(events);

	}

}

class RemoveButtonActionListener implements ActionListener {
	private JTable table;

	public RemoveButtonActionListener(JTable table) {
		this.table = table;
	}

	public void actionPerformed(ActionEvent e) {
		if (table.getSelectedRow() != -1) {
			((SetTableModel) table.getModel()).remove(table.getSelectedRow());
		}
	}

}
