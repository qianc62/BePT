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

package org.processmining.mining.semanticorganizationmining.ui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Task;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMiningResult;

/**
 * @author not attributable
 * @version 1.0
 */
public class TaskOrgEntityTablePanel extends JSplitPane implements
		TableModelListener {

	private List<Task> tasks = null;
	private List<OrgEntity> orgEntities = null;
	private OrgModel orgModel = null;
	private SemanticOrgMiningResult parentPanel = null;

	private JTable taskTable;
	private JTable orgEntitySelectTable;
	private JScrollPane activityPanel = null;
	private JScrollPane groupSelectPanel = null;

	public TaskOrgEntityTablePanel(OrgModel orgmod,
			SemanticOrgMiningResult parentpanel) {
		super(JSplitPane.HORIZONTAL_SPLIT);

		this.orgModel = orgmod;
		this.parentPanel = parentpanel;
		tasks = orgModel.getTaskList();
		orgEntities = orgModel.getOrgEntityList();

		init();

	}

	public void init() {

		taskTable = new JTable(new TaskTable());
		orgEntitySelectTable = new JTable(new OrgEntitySelectTable());

		activityPanel = new JScrollPane(taskTable);
		groupSelectPanel = new JScrollPane(orgEntitySelectTable);
		groupSelectPanel.setPreferredSize(new Dimension(130, 180));

		this.setLeftComponent(activityPanel);
		this.setRightComponent(groupSelectPanel);

		this.setDividerLocation(300);
		this.setOneTouchExpandable(true);
		this.setDividerSize(3);

		taskTable.getModel().addTableModelListener(this);
		orgEntitySelectTable.getModel().addTableModelListener(this);

		taskTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					Point p = e.getPoint();
					int row = taskTable.rowAtPoint(p);
					((OrgEntitySelectTable) orgEntitySelectTable.getModel())
							.updateTable(((TaskTable) taskTable.getModel())
									.getTask(row));
				}
			}
		});

	}

	public void tableChanged(TableModelEvent e) {
		if (e.getSource() instanceof OrgEntitySelectTable) {
			if (e.getColumn() == 0) {
				int grp_row = taskTable.getSelectedRow();
				int src_row = orgEntitySelectTable.getSelectedRow();

				if (grp_row >= 0) {
					Task t = ((TaskTable) taskTable.getModel())
							.getTask(grp_row);
					OrgEntity orgEntity = ((OrgEntitySelectTable) orgEntitySelectTable
							.getModel()).getOrgEntity(src_row);
					if ((Boolean) orgEntitySelectTable.getValueAt(src_row, 0)) {
						t.addOrgEntity(orgEntity);
					} else {
						t.removeOrgEntity(orgEntity);
					}
				} else
					((OrgEntitySelectTable) orgEntitySelectTable.getModel())
							.updateTable();
			}
		}

	}

	public OrgEntity getSeletedOrgEntity() {
		if (orgEntitySelectTable.getSelectedRow() < 0)
			return null;
		else
			return ((OrgEntitySelectTable) orgEntitySelectTable.getModel())
					.getOrgEntity(orgEntitySelectTable.getSelectedRow());
	}

	public Task getSeletedTask() {
		if (taskTable.getSelectedRow() < 0)
			return null;
		else
			return ((TaskTable) taskTable.getModel()).getTask(taskTable
					.getSelectedRow());
	}

	/**
	 * Private data structure for the table containing the tasks.
	 */
	private class TaskTable extends AbstractTableModel {
		private String[] columnName = { "Task Name", "Event Type" };

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of single column
			return columnName[col];
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of tasks in the model
		 */
		public int getRowCount() {
			if (tasks == null)
				return 0;
			else
				return tasks.size();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 1.
		 */
		public int getColumnCount() {
			return 2;
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			if (row < 0) {
				return null;
			}
			if (column == 0)
				return ((Task) tasks.get(row)).getName();
			else
				return ((Task) tasks.get(row)).getEventType();
		}

		public void setValueAt(Object value, int row, int col) {
			fireTableCellUpdated(row, col);
		}

		public Task getTask(int row) {
			return ((Task) tasks.get(row));
		}

	}

	/**
	 * Private data structure for the table containing the tasks.
	 */
	private class OrgEntitySelectTable extends AbstractTableModel {

		private String[] columnName = { "Selected", "Org Entity ID", "Type" };
		private ArrayList<Boolean> choose = new ArrayList<Boolean>();

		public OrgEntitySelectTable() {
			if (orgEntities != null)
				for (int i = 0; i < orgEntities.size(); i++)
					choose.add(new Boolean(false));
		}

		/**
		 * Specify the headings for the columns.
		 * 
		 * @param col
		 *            The column specified.
		 * @return The heading of the respective column.
		 */
		public String getColumnName(int col) {
			// heading of single column
			return columnName[col];
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of tasks in the model
		 */
		public int getRowCount() {
			int num = 0;
			if (orgEntities != null)
				num += orgEntities.size();
			return num;
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 3.
		 */
		public int getColumnCount() {
			return 3;
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 0)
				return true;
			return false;
		}

		/**
		 * Method to fill a certain field of the table with contents.
		 * 
		 * @param row
		 *            The specified row.
		 * @param column
		 *            The specified column.
		 * @return The content to display at the table field specified.
		 */
		public Object getValueAt(int row, int column) {
			if (row < 0)
				return null;
			// fill in name of the decision point
			switch (column) {
			case 0:
				return (Boolean) choose.get(row);
			case 1:
				return orgEntities.get(row).getID();
			case 2:
				return orgEntities.get(row).getEntityType();
			}
			return null;
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 0) {
				choose.set(row, (Boolean) value);

			}
			fireTableCellUpdated(row, col);
		}

		public void updateTable(Task task) {
			if (orgEntities != null) {
				int numRes = orgEntities.size();

				for (int i = 0; i < numRes; i++) {
					choose.set(i, Boolean.valueOf(task.hasOrgEntity(orgEntities
							.get(i))));
				}

				super.fireTableDataChanged();
			}
		}

		public void updateTable() {
			if (orgEntities == null)
				return;
			int numRes = orgEntities.size();
			for (int i = 0; i < numRes; i++) {
				choose.set(i, false);
			}

			super.fireTableDataChanged();
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public OrgEntity getOrgEntity(int row) {
			return orgModel.getOrgEntity((String) getValueAt(row, 1));
		}

	}

}
