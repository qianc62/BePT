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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.processmining.framework.models.ModelGraphPanel;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.petrinet.Place;
import org.processmining.framework.models.petrinet.State;
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

public class FinalEventSetSelectorPanel extends JPanel {
	private ArrayList finalStatesToKeep;
	private ArrayList finalStatesToRemove;
	private ModelGraphPanel graphPanel;
	protected HashMap setToState;

	private SetTableModel keepGridModel;
	private SetTableModel removeGridModel;
	private JButton keepShowButton;
	private JButton keepTransferButton;
	private JButton removeShowButton;
	private JButton removeTransferButton;

	JSplitPane jSplitPaneMain = new JSplitPane();

	public FinalEventSetSelectorPanel(ArrayList finalStatesToKeep,
			ArrayList finalStatesToRemove, ModelGraphPanel panel) {
		this.finalStatesToKeep = finalStatesToKeep;
		this.finalStatesToRemove = finalStatesToRemove;
		this.graphPanel = panel;
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jbInit() throws Exception {

		keepGridModel = new SetTableModel(
				"<html>Final states <br>to keep</html>");

		removeGridModel = new SetTableModel(
				"<html>Final states <br>to ignore</html>");

		setToState = new HashMap();

		// Fill the keep table
		int i = 0;
		Iterator it = finalStatesToKeep.iterator();
		while (it.hasNext()) {
			State s = (State) it.next();
			HashSet set = new HashSet();
			int tokens = 0;
			Iterator it2 = s.iterator();
			while (it2.hasNext()) {
				Place p = (Place) it2.next();
				if (p.getIdentifier().startsWith(
						EPCCorrectnessCheckerUI.COUNTER)) {
					continue;
				}
				tokens += s.getOccurances(p);
				EPCEvent e = ((EPCEvent) p.object);
				if (e != null) {
					set.add(((HashSet) e.object2).iterator().next());
				}
			}
			if (!set.isEmpty()) {
				set.add(new Integer(i));
				keepGridModel.add(set, "Set " + i++ + " e: " + (set.size() - 1)
						+ " t: " + tokens);
				setToState.put(set, s);
			}
		}

		// Fill the remove table
		it = finalStatesToRemove.iterator();
		while (it.hasNext()) {
			State s = (State) it.next();
			HashSet set = new HashSet();
			int tokens = 0;
			Iterator it2 = s.iterator();
			while (it2.hasNext()) {
				Place p = (Place) it2.next();
				if (p.getIdentifier().startsWith(
						EPCCorrectnessCheckerUI.COUNTER)) {
					continue;
				}
				tokens += s.getOccurances(p);
				EPCEvent e = ((EPCEvent) p.object);
				if (e != null) {
					set.add(((HashSet) e.object2).iterator().next());
				}
			}
			if (!set.isEmpty()) {
				set.add(new Integer(i));
				removeGridModel.add(set, "Set " + i++ + " e: " + set.size()
						+ " t: " + tokens);
				setToState.put(set, s);
			}
		}

		setLayout(new BorderLayout());

		jSplitPaneMain.setOrientation(JSplitPane.HORIZONTAL_SPLIT);

		// Start the right panel
		JPanel rightPanel = new JPanel(new BorderLayout());

		keepShowButton = new JButton("Show");

		JTable keepGrid = new DoubleClickTable(keepShowButton);
		keepGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		keepGrid.setModel(keepGridModel);

		keepShowButton.addActionListener(new ShowStateButtonActionListener(
				keepGrid, graphPanel));

		JPanel keepButtonPanel = new JPanel(new GridLayout(2, 1));

		keepButtonPanel.add(keepShowButton);

		JScrollPane keepGridScrollPane = new JScrollPane(keepGrid);

		removeShowButton = new JButton("Show");

		JTable removeGrid = new DoubleClickTable(removeShowButton);
		removeGrid.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		removeGrid.setModel(removeGridModel);

		keepTransferButton = new JButton("<<<");
		keepTransferButton.addActionListener(new TransferButtonActionListener(
				keepGrid, removeGrid));
		keepButtonPanel.add(keepTransferButton);

		rightPanel.add(keepGridScrollPane, BorderLayout.CENTER);
		rightPanel.add(keepButtonPanel, BorderLayout.SOUTH);

		jSplitPaneMain.add(rightPanel, JSplitPane.RIGHT);
		// Finished the right panel

		// Start the right panel
		JPanel leftPanel = new JPanel(new BorderLayout());

		JScrollPane removeGridScrollPane = new JScrollPane(removeGrid);

		JPanel removeButtonPanel = new JPanel(new GridLayout(2, 1));

		removeShowButton.addActionListener(new ShowStateButtonActionListener(
				removeGrid, graphPanel));
		removeButtonPanel.add(removeShowButton);

		removeTransferButton = new JButton(">>>");
		removeTransferButton
				.addActionListener(new TransferButtonActionListener(removeGrid,
						keepGrid));
		removeButtonPanel.add(removeTransferButton);

		leftPanel.add(removeGridScrollPane, BorderLayout.CENTER);
		leftPanel.add(removeButtonPanel, BorderLayout.SOUTH);

		jSplitPaneMain.add(leftPanel, JSplitPane.LEFT);
		jSplitPaneMain.setResizeWeight(0.5);
		// Finished the right panel

		add(jSplitPaneMain, BorderLayout.CENTER);
	}

	public ArrayList getCorrectFinalStates() {
		ArrayList r = new ArrayList();
		for (int i = 0; i < keepGridModel.getRowCount(); i++) {
			r.add(setToState.get(keepGridModel.getSet(i)));
		}
		return r;
	}

}

class TransferButtonActionListener implements ActionListener {
	private JTable source;
	private JTable target;
	private SetTableModel sourceModel;
	private SetTableModel targetModel;

	public TransferButtonActionListener(JTable source, JTable target) {
		this.source = source;
		this.target = target;
		this.sourceModel = (SetTableModel) source.getModel();
		this.targetModel = (SetTableModel) target.getModel();
	}

	public void actionPerformed(ActionEvent e) {
		int i = source.getSelectedRow();
		if (i != -1) {
			targetModel.add(sourceModel.getSet(i), sourceModel.getName(i));
			sourceModel.remove(i);
		}
	}

}

class ShowStateButtonActionListener implements ActionListener {
	private JTable table;
	private ModelGraphPanel panel;

	public ShowStateButtonActionListener(JTable table, ModelGraphPanel panel) {
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
