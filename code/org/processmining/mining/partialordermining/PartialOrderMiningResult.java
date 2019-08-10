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

package org.processmining.mining.partialordermining;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogReaderFactory;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.ProcessInstanceVisualization;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.DoubleClickTable;
import org.processmining.mining.MiningResult;

/**
 * @author Peter van den Brand
 * @version 1.0
 */

public class PartialOrderMiningResult extends JPanel implements MiningResult,
		Provider {
	BorderLayout borderLayout1 = new BorderLayout();
	JSplitPane jSplitPane1 = new JSplitPane();
	LogReader log;
	LogSummary summary;
	JScrollPane jScrollPane1 = new JScrollPane();
	JTable jTable1;
	ArrayListTableModel tableModel;
	JPanel jPanel1 = new JPanel(new BorderLayout());
	JButton jButton1 = new JButton();
	JScrollPane jScrollPane2;
	int[] allPIs;
	JPanel jPanel2 = new JPanel();
	ProcessInstance visualised = null;

	public PartialOrderMiningResult(LogReader log, ArrayList PINames) {
		this.log = log;
		this.summary = summary;
		tableModel = new ArrayListTableModel(PINames);
		jTable1 = new DoubleClickTable(tableModel, jButton1);
		allPIs = new int[PINames.size()];
		for (int i = 0; i < PINames.size(); i++) {
			allPIs[i] = i;
		}
	}

	public ProvidedObject[] getProvidedObjects() {
		ArrayList a = new ArrayList();
		String s = log.getFile().getShortName();
		// The log should not be empty...
		if (s.indexOf(File.pathSeparator) > 0) {
			s = s.substring(s.lastIndexOf(File.pathSeparator) + 1);
		}
		if (s.indexOf(File.separator) > 0) {
			s = s.substring(s.lastIndexOf(File.separator) + 1);
		}
		a.add(new ProvidedObject("Whole of " + s, new Object[] { log }));

		if (visualised != null) {
			a.add(new ProvidedObject("Instance " + visualised.getName()
					+ " of " + s, new Object[] { visualised }));
		}

		if (jTable1.getSelectedRowCount() > 0) {
			try {
				a.add(new ProvidedObject("Selection of " + s,
						new Object[] { LogReaderFactory.createInstance(log,
								jTable1.getSelectedRows()) }));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		ProvidedObject[] objects = new ProvidedObject[a.size()];
		for (int i = 0; i < a.size(); i++) {
			objects[i] = (ProvidedObject) a.get(i);
		}

		return objects;

	}

	public JComponent getVisualization() {
		try {
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	public LogReader getLogReader() {
		return log;
	}

	private ProcessInstance loadProcessInstance(int i) {
		// simply walk through the log and find the right instance
		int j = 0;

		log.reset();
		while (log.hasNext()) {
			ProcessInstance pi = log.next();
			if (i == j) {
				return pi;
			}
			j++;
		}
		return null;
	}

	private void jbInit() throws Exception {
		this.setLayout(borderLayout1);
		jSplitPane1.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
		jButton1.setText("Show Process Instance");
		jTable1
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		jButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				int i = jTable1.getSelectedRow();
				if (i >= 0) {
					visualised = loadProcessInstance(i);
					jScrollPane1.getViewport().add(
							(new ProcessInstanceVisualization(visualised))
									.getGrappaVisualization());

				}
			}
		});
		this.add(jSplitPane1, BorderLayout.CENTER);
		jSplitPane1.add(jScrollPane1, JSplitPane.RIGHT);
		jSplitPane1.add(jPanel1, JSplitPane.LEFT);
		jSplitPane1.setDividerLocation(180);
		jSplitPane1.setOneTouchExpandable(true);
		jPanel1.add(jPanel2, BorderLayout.SOUTH);
		jPanel2.add(jButton1);
		jScrollPane2 = new JScrollPane(jTable1);
		jPanel1.add(jScrollPane2, BorderLayout.CENTER);
	}
}

class ArrayListTableModel extends AbstractTableModel {

	protected ArrayList data;

	public ArrayListTableModel(ArrayList data) {
		this.data = data;
	}

	public String getColumnName(int col) {
		return "Process instances";
	}

	public int getRowCount() {
		return data.size();
	}

	public int getColumnCount() {
		return 1;
	}

	public Object getValueAt(int row, int column) {
		return data.get(row);
	}
}
