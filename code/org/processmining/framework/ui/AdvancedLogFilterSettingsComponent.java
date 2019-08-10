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

package org.processmining.framework.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.filter.AbstractLogFilter;
import org.processmining.framework.log.filter.LogFilterCollection;
import org.processmining.framework.log.filter.LogFilterParameterDialog;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.util.ToolTipComboBox;

/**
 * <p>
 * Title:
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public class AdvancedLogFilterSettingsComponent extends JPanel {

	private JButton addButton = new JButton("Add");
	private JButton hlpButton = new JButton("Help");

	private final static int WIDTH = 300;

	private JButton delButton = new JButton("Remove");
	private JButton upButton = new JButton("<html>Move<br>up</html>");
	private JButton dwnButton = new JButton("<html>Move<br>down</html>");
	private JButton chngButton = new JButton("<html>Change<br>settings</html>");

	private LogFilterTableModel tableModel = new LogFilterTableModel();

	private JTable filterTable = new JTable(tableModel);

	private ToolTipComboBox comboBox;

	public void addLogFilterToBegin(LogFilter filter, String label) {
		tableModel.add(filter, label, 0);
		filterTable.setRowSelectionInterval(0, tableModel.getRowCount() - 1);
	}

	public AdvancedLogFilterSettingsComponent(LogSummary summary) {
		initialize(summary);
	}

	public AdvancedLogFilterSettingsComponent(final OpenLogSettings settings) {
		initialize(settings.getLogSummary());
		JPanel selectionPanel = new JPanel(new BorderLayout());
		comboBox = new ToolTipComboBox();
		buildFilterCombo(settings);
		selectionPanel.add(comboBox, BorderLayout.CENTER);
		JPanel btPan = new JPanel();
		btPan.add(addButton);
		btPan.add(hlpButton);
		selectionPanel.add(btPan, BorderLayout.EAST);
		selectionPanel.setPreferredSize(new Dimension(WIDTH,
				(int) selectionPanel.getPreferredSize().getHeight()));
		add(selectionPanel, BorderLayout.SOUTH);
		this.setPreferredSize(new Dimension(DefaultLogFilterUI.WIDTH, 100));
		this.setMinimumSize(new Dimension(DefaultLogFilterUI.WIDTH, 100));
	}

	private void initialize(final LogSummary summary) {
		setLayout(new BorderLayout());
		filterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane sp = new JScrollPane(filterTable,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		sp.setPreferredSize(new Dimension(WIDTH, 100));

		add(sp, BorderLayout.CENTER);

		JPanel removePanel = new JPanel(new GridLayout(4, 1));
		removePanel.add(upButton);
		removePanel.add(dwnButton);
		removePanel.add(delButton);
		removePanel.add(chngButton);
		JPanel removePanelWrap = new JPanel();
		removePanelWrap.add(removePanel);
		add(removePanelWrap, BorderLayout.EAST);

		addButton.setMinimumSize(delButton.getPreferredSize());
		addButton.setSize(delButton.getPreferredSize());
		addButton.setPreferredSize(delButton.getPreferredSize());

		hlpButton.setMinimumSize(delButton.getPreferredSize());
		hlpButton.setSize(delButton.getPreferredSize());
		hlpButton.setPreferredSize(delButton.getPreferredSize());

		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProvidedObjectComboItem ce = (ProvidedObjectComboItem) comboBox
						.getSelectedItem();
				LogFilter f = (LogFilter) ((ProvidedObject) ce.getObject())
						.getObjects()[0];
				LogFilterParameterDialog d = f.getParameterDialog(summary);
				if (d == null || d.showDialog()) {
					tableModel.add(d);
					int i = filterTable.getRowCount() - 1;
					filterTable.setRowSelectionInterval(i, i);
				}
			}
		});
		chngButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int i = filterTable.getSelectedRow();
				if (i != -1) {
					LogFilter f = tableModel.getLogFilter(i);
					LogFilterParameterDialog d = f.getParameterDialog(summary);
					if (d.showDialog()) {
						tableModel.remove(i);
						f = null;
						tableModel.add(d.getNewLogFilter(), d.getLabel(), i);
						filterTable.setRowSelectionInterval(i, i);
					}
				}
			}
		});
		hlpButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ProvidedObjectComboItem ce = (ProvidedObjectComboItem) comboBox
						.getSelectedItem();
				LogFilter f = (LogFilter) ((ProvidedObject) ce.getObject())
						.getObjects()[0];
				f.getParameterDialog(summary).showHelpDialog(f);
			}
		});
		delButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (filterTable.getSelectedRow() != -1) {
					int i = filterTable.getSelectedRow();
					tableModel.remove(i);
					if ((i - 1 < filterTable.getRowCount()) && (i - 1 >= 0)) {
						filterTable.setRowSelectionInterval(i - 1, i - 1);
					} else if (filterTable.getRowCount() != 0) {
						filterTable.setRowSelectionInterval(0, 0);
					}
				}
			}
		});
		upButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (filterTable.getSelectedRow() != -1) {
					int i = filterTable.getSelectedRow();
					if (tableModel.moveUp(i)) {
						filterTable.setRowSelectionInterval(i - 1, i - 1);
					}
				}
			}
		});
		dwnButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (filterTable.getSelectedRow() != -1) {
					int i = filterTable.getSelectedRow();
					if (tableModel.moveDown(i)) {
						filterTable.setRowSelectionInterval(i + 1, i + 1);
					}
				}
			}
		});

	}

	public LogFilter getLogFilter() {
		Iterator it = tableModel.iterator();
		LogFilter filter = null;
		while (it.hasNext()) {
			LogFilter f = (LogFilter) ((Object[]) it.next())[0];
			f.setLowLevelFilter(filter);
			filter = f;
		}
		return filter;
	}

	public void buildFilterCombo(OpenLogSettings curSettingsFrame) {
		comboBox.removeAllItems();

		for (int i = 0; i < LogFilterCollection.getInstance().size(); i++) {
			LogFilter f = LogFilterCollection.getInstance().get(i);
			comboBox.addItem(makeComboItem(f));
		}

		for (int i = MainUI.getInstance().getDesktop().getAllFrames().length - 1; i >= 0; i--) {
			JInternalFrame frame = MainUI.getInstance().getDesktop()
					.getAllFrames()[i];
			if ((frame instanceof Provider) && (frame != curSettingsFrame)) {
				ProvidedObject[] obs = ((Provider) frame).getProvidedObjects();
				fillLogFilterCombo(obs, frame.getTitle());
			}
		}
		fillLogFilterCombo(MainUI.getInstance().getGlobalProvidedObjects(),
				"Global");
		comboBox.setPreferredSize(new Dimension(100, (int) comboBox
				.getPreferredSize().getHeight()));
		comboBox.setSize(new Dimension(100, (int) comboBox.getPreferredSize()
				.getHeight()));
	}

	private ProvidedObjectComboItem makeComboItem(LogFilter f) {
		return new ProvidedObjectComboItem(f.getName(), new ProvidedObject(
				"changes: " + (f.changesLog() ? "yes" : "no")
						+ "; complexity: " + f.getComplexityAsString(),
				new Object[] { f }));

	}

	private void fillLogFilterCombo(ProvidedObject[] obs, String label) {
		for (int j = obs.length - 1; j >= 0; j--) {
			ProvidedObject po = obs[j];
			boolean filterFound = false;
			LogFilter f = null;
			for (int k = po.getObjects().length - 1; (k >= 0) && !filterFound; k--) {
				if (po.getObjects()[k] instanceof LogFilter) {
					f = (LogFilter) po.getObjects()[k];
					filterFound = true;
				} else if (po.getObjects()[k] instanceof LogReader) {
					LogReader r = (LogReader) po.getObjects()[k];
					if (r.getLogFilter() != null) {
						f = new AbstractLogFilter(r.getFile().getShortName()
								+ " Log Filter", r.getLogFilter());
						filterFound = true;
					}
				}
			}
			if (filterFound) {
				comboBox.addItem(makeComboItem(f));
			}
		}
	}
}

class LogFilterTableModel extends AbstractTableModel {

	private LinkedList logFilters = new LinkedList();
	private String[] columns = { "Log Filter", "Changes Log", "Complexity" };
	private static final String YES = "Yes";
	private static final String NO = "No";

	public LogFilterTableModel() {
	}

	public String getColumnName(int col) {
		return columns[col];
	}

	public int getRowCount() {
		return logFilters.size();
	}

	public int getColumnCount() {
		return 3;
	}

	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return ((Object[]) logFilters.get(row))[1];
		} else if (column == 1) {
			return ((Object[]) logFilters.get(row))[2];
		} else {
			return ((Object[]) logFilters.get(row))[3];
		}
	}

	public void add(LogFilter f, String label, int index) {
		f.setName(label);
		logFilters.add(index, new Object[] { f, label,
				(f.changesLog() ? YES : NO), f.getComplexityAsString() });
		fireTableRowsInserted(index, index);

	}

	public void add(LogFilterParameterDialog d) {
		int k = -1;
		for (int i = 0; (k == -1) && (i < logFilters.size()); i++) {
			if (getValueAt(i, 1).equals(NO)) {
				k = i;
			}
		}
		// First NO is at row k;
		LogFilter f = d.getNewLogFilter();
		if ((k == -1) || !f.changesLog()) {
			k = logFilters.size();
		}
		add(f, d.getLabel(), k);
	}

	public LogFilter getLogFilter(int i) {
		return (LogFilter) ((Object[]) logFilters.get(i))[0];
	}

	public String getName(int i) {
		return (String) getLogFilter(i).getName();
	}

	public void remove(int i) {
		logFilters.remove(i);
		fireTableRowsDeleted(i, i);
	}

	public Iterator iterator() {
		return logFilters.iterator();
	}

	public boolean moveUp(int i) {
		if ((i > 0) /*
					 * removed constraint for modifying / non-modifying &&
					 * (getValueAt(i, 1).equals(getValueAt(i - 1, 1)))
					 */) {
			Object o = logFilters.get(i);
			logFilters.remove(i);
			logFilters.add(i - 1, o);
			fireTableRowsUpdated(i - 1, i);
			return true;
		}
		return false;
	}

	public boolean moveDown(int i) {
		if ((i < logFilters.size() - 1) /*
										 * removed constraint for modifying /
										 * non-modifying && (getValueAt(i,
										 * 1).equals(getValueAt(i + 1, 1)))
										 */) {
			Object o = logFilters.get(i);
			logFilters.remove(i);
			logFilters.add(i + 1, o);
			fireTableRowsUpdated(i, i + 1);
			return true;
		}
		return false;
	}

}
