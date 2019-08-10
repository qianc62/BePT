package org.processmining.analysis.abstractions.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Set;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.framework.ui.MainUI;

@SuppressWarnings("serial")
public class ActivitySelectionPanel extends JPanel implements ActionListener {
	protected JTable table;
	protected TableSorter sorter;

	Set<String> selectedActivities;

	public Set<String> getSelectedActivities() {
		return selectedActivities;
	}

	protected Color colorListBg = new Color(60, 60, 60);
	protected Color colorListBgSelected = new Color(10, 90, 10);
	protected Color colorListFg = new Color(200, 200, 200, 160);
	protected Color colorListFgSelected = new Color(230, 230, 230, 200);
	protected Color colorListEnclosureBg = new Color(150, 150, 150);
	protected Color colorListHeader = new Color(10, 10, 10);
	protected Color colorListDescription = new Color(60, 60, 60);

	protected Color colorEnclosureBg = new Color(250, 250, 250, 105);
	protected Color colorTitleFg = new Color(20, 20, 20, 230);
	protected Color colorInfoBg = new Color(60, 60, 60, 160);
	protected Color colorInfoBgMouseOver = new Color(60, 60, 60, 240);
	protected Color colorInfoLabel = new Color(210, 210, 210);
	protected Color colorInfoValue = new Color(255, 255, 255);
	CustomTableModel model;

	public ActivitySelectionPanel(Set<String> absSet,
			Set<String> selectedActivites) {
		this.selectedActivities = selectedActivites;
		model = new CustomTableModel();

		model.fillTable(selectedActivites);

		sorter = new TableSorter(model);
		setupGui();

	}

	public void setSelectedActivities(Set<String> selectedActivities) {
		model.fillTable(selectedActivities);
	}

	protected void setupGui() {
		final AutoFocusButton okButton = new AutoFocusButton("OK");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		final SlickerButton cancelButton = new SlickerButton("Cancel");
		cancelButton.setActionCommand("Cancel");
		cancelButton.addActionListener(this);
		final SlickerButton selectButton = new SlickerButton("Select all");
		selectButton.setActionCommand("Select");
		selectButton.addActionListener(this);
		final SlickerButton deselectButton = new SlickerButton("Deselect all");
		deselectButton.setActionCommand("Deselect");
		deselectButton.addActionListener(this);

		table = new JTable(sorter);
		TableHeaderSorter tableHeaderSorter = new TableHeaderSorter();
		tableHeaderSorter.install(table);

		table.setFont(table.getFont().deriveFont(13f));
		table.setBackground(colorListBg);
		table.setForeground(colorListFg);
		table.setSelectionBackground(colorListBgSelected);
		table.setSelectionForeground(colorListFgSelected);
		table.setFont(table.getFont().deriveFont(12f));

		table.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);

		table.getColumnModel().getColumn(0).setPreferredWidth(150);

		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.setRowSelectionAllowed(true);
		table.setColumnSelectionAllowed(false);

		JScrollPane tableSelectedSystems = new JScrollPane(table);
		tableSelectedSystems.setAutoscrolls(true);

		// Lay out the buttons from left to right.
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(deselectButton);
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(selectButton);
		buttonPane.add(Box.createHorizontalGlue());
		buttonPane.add(cancelButton);
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.add(okButton);
		buttonPane.add(Box.createHorizontalStrut(10));
		buttonPane.setBackground(new Color(100, 100, 100));
		buttonPane.setOpaque(false);

		JPanel containerPanel = new JPanel();
		containerPanel.setOpaque(false);
		containerPanel.setBackground(new Color(100, 100, 100));
		containerPanel
				.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));

		this.setBackground(new Color(100, 100, 100));
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JScrollPane tableScrollPane = new JScrollPane(table);
		tableScrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		tableScrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(40,
				40, 40)));
		containerPanel.add(Box.createVerticalStrut(5));
		containerPanel.add(tableScrollPane);
		containerPanel.add(Box.createVerticalStrut(10));
		containerPanel.add(buttonPane);
		containerPanel.add(Box.createVerticalStrut(5));

		this.add(containerPanel);
		revalidate();
		repaint();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("Deselect")) {
			table.clearSelection();
		} else if (e.getActionCommand().equalsIgnoreCase("Select")) {
			table.selectAll();
		} else if (e.getActionCommand().equalsIgnoreCase("Cancel")) {
			closeFrame();
		} else if (e.getActionCommand().equalsIgnoreCase("OK")) {
			int[] selectedRowsLocal = table.getSelectedRows();

			if (selectedRowsLocal.length > 0) {
				selectedActivities.clear();
				for (int i = 0; i < selectedRowsLocal.length; i++) {
					selectedActivities.add((String) table.getModel()
							.getValueAt(selectedRowsLocal[i], 0));
				}

			}
			closeFrame();
		}
	}

	protected void closeFrame() {
		/*
		 * The variable 'myself' must point to the component being added to the
		 * enclosing frame (usually the object itself). Must be final for
		 * anonymous inner class to work.
		 */
		final JComponent myself = this;

		// walk through all frames to find enclosing instances
		for (JInternalFrame frame : MainUI.getInstance().getDesktop()
				.getAllFrames()) {
			if (contains(frame.getContentPane(), myself) == true) {
				// maximize and set marker

				frame.dispose();
				return;
			}
		}
	}

	public void resizeFrame() {
		/*
		 * The variable 'myself' must point to the component being added to the
		 * enclosing frame (usually the object itself). Must be final for
		 * anonymous inner class to work.
		 */
		final JComponent myself = this;

		// walk through all frames to find enclosing instances
		for (JInternalFrame frame : MainUI.getInstance().getDesktop()
				.getAllFrames()) {
			if (contains(frame.getContentPane(), myself) == true) {
				// maximize and set marker
				frame.setSize(new Dimension(900, 550));
				return;
			}
		}
	}

	protected boolean contains(Container container, JComponent component) {
		for (Component target : container.getComponents()) {
			if (target == component) {
				return true;
			} else if (target instanceof Container) {
				Container nContainer = (Container) target;
				if (contains(nContainer, component) == true) {
					return true;
				}
			}
		}
		return false;
	}

	static class CustomTableModel extends AbstractTableModel {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		Vector<Object> values_activities = new Vector<Object>();

		String[] columnNames = { "Activity Name" };

		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		public int getRowCount() {
			return values_activities.size();
		}

		public Object getValueAt(int row, int column) {
			Object returnValue = null;

			if (column == 0) {
				returnValue = values_activities.elementAt(row);
			}
			return returnValue;
		}

		public synchronized void fillTable(Set<String> absSet) {
			values_activities.removeAllElements();

			for (String activityName : absSet) {
				values_activities.add(activityName);
			}

			fireTableDataChanged();
		}
	}

	class TableSorter extends TableMap implements TableModelListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		int indexes[] = new int[0];

		Vector<Integer> sortingColumns = new Vector<Integer>();

		boolean ascending = true;

		public TableSorter() {
			// No instatiation needed
		}

		public TableSorter(TableModel model) {
			setModel(model);
		}

		@Override
		public void setModel(TableModel model) {
			super.setModel(model);
			reallocateIndexes();
			sortByColumn(0);
			fireTableDataChanged();
		}

		public int compareRowsByColumn(int row1, int row2, int column) {
			Class<?> type = model.getColumnClass(column);

			if (column == 0) {
				type = Integer.class;
			}

			TableModel dataLocal = model;

			// Check for nulls

			Object o1 = dataLocal.getValueAt(row1, column);
			Object o2 = dataLocal.getValueAt(row2, column);

			// If both values are null return 0
			if (o1 == null && o2 == null) {
				return 0;
			} else if (o1 == null) { // Define null less than everything.
				return -1;
			} else if (o2 == null) {
				return 1;
			}

			if (type == String.class) {
				String s1 = (String) dataLocal.getValueAt(row1, column);
				String s2 = (String) dataLocal.getValueAt(row2, column);
				int result = s1.compareTo(s2);

				if (result < 0)
					return -1;
				else if (result > 0)
					return 1;
				else
					return 0;
			} else {
				Object v1 = dataLocal.getValueAt(row1, column);
				String s1 = v1.toString();
				Object v2 = dataLocal.getValueAt(row2, column);
				String s2 = v2.toString();
				int result = s1.compareTo(s2);

				if (result < 0)
					return -1;
				else if (result > 0)
					return 1;
				else
					return 0;
			}
		}

		public int compare(int row1, int row2) {
			for (int level = 0, n = sortingColumns.size(); level < n; level++) {
				Integer column = sortingColumns.elementAt(level);
				int result = compareRowsByColumn(row1, row2, column.intValue());
				if (result != 0) {
					return (ascending ? result : -result);
				}
			}
			return 0;
		}

		public void reallocateIndexes() {
			int rowCount = model.getRowCount();
			indexes = new int[rowCount];
			for (int row = 0; row < rowCount; row++) {
				indexes[row] = row;
			}
		}

		@Override
		public void tableChanged(TableModelEvent tableModelEvent) {
			super.tableChanged(tableModelEvent);
			reallocateIndexes();
			sortByColumn(0);
			fireTableStructureChanged();
		}

		public void checkModel() {
			if (indexes.length != model.getRowCount()) {
				System.err.println("Sorter not informed of a change in model.");
			}
		}

		public void sort() {
			checkModel();
			shuttlesort(indexes.clone(), indexes, 0, indexes.length);
			fireTableDataChanged();
		}

		public void shuttlesort(int from[], int to[], int low, int high) {
			if (high - low < 2) {
				return;
			}
			int middle = (low + high) / 2;
			shuttlesort(to, from, low, middle);
			shuttlesort(to, from, middle, high);

			int p = low;
			int q = middle;

			for (int i = low; i < high; i++) {
				if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
					to[i] = from[p++];
				} else {
					to[i] = from[q++];
				}
			}
		}

		@Override
		public Object getValueAt(int row, int column) {
			checkModel();
			return model.getValueAt(indexes[row], column);
		}

		@Override
		public void setValueAt(Object aValue, int row, int column) {
			checkModel();
			model.setValueAt(aValue, indexes[row], column);
		}

		public void sortByColumn(int column) {
			sortByColumn(column, true);
		}

		public void sortByColumn(int column, boolean ascendingA) {
			this.ascending = ascendingA;
			sortingColumns.removeAllElements();
			sortingColumns.addElement(new Integer(column));
			sort();
			super.tableChanged(new TableModelEvent(this));
		}
	}

	class TableHeaderSorter extends MouseAdapter {

		private boolean ascendingValue = true;

		private JTable tableLocal;

		TableHeaderSorter() {
			// No instatiation needed
		}

		public void install(JTable tableA) {
			TableHeaderSorter tableHeaderSorter = new TableHeaderSorter();
			tableHeaderSorter.tableLocal = tableA;
			JTableHeader tableHeader = tableA.getTableHeader();
			tableHeader.addMouseListener(tableHeaderSorter);
		}

		@Override
		public void mouseClicked(MouseEvent mouseEvent) {
			TableColumnModel columnModel = tableLocal.getColumnModel();
			int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
			int column = tableLocal.convertColumnIndexToModel(viewColumn);
			if (mouseEvent.getClickCount() == 1 && column != -1) {
				if (ascendingValue) {
					ascendingValue = false;
				} else {
					ascendingValue = true;
				}
				sorter.sortByColumn(column, ascendingValue);
			}
		}
	}

	class TableMap extends AbstractTableModel implements TableModelListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		TableModel model;

		public TableModel getModel() {
			return model;
		}

		public void setModel(TableModel model) {
			if (this.model != null) {
				this.model.removeTableModelListener(this);
			}
			this.model = model;
			if (this.model != null) {
				this.model.addTableModelListener(this);
			}
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return model.getColumnClass(column);
		}

		public int getColumnCount() {
			return ((model == null) ? 0 : model.getColumnCount());
		}

		@Override
		public String getColumnName(int column) {
			return model.getColumnName(column);
		}

		public int getRowCount() {
			return ((model == null) ? 0 : model.getRowCount());
		}

		public Object getValueAt(int row, int column) {
			return model.getValueAt(row, column);
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			model.setValueAt(value, row, column);
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return model.isCellEditable(row, column);
		}

		public void tableChanged(TableModelEvent tableModelEvent) {
			fireTableChanged(tableModelEvent);
		}
	}

}
