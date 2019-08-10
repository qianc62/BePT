package org.processmining.mining.semanticorganizationmining.ui;

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

import javax.swing.JSplitPane;
import javax.swing.event.TableModelListener;
import java.util.List;
import org.processmining.framework.models.orgmodel.OrgModel;
import org.processmining.framework.models.orgmodel.Resource;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import org.processmining.framework.models.orgmodel.OrgEntity;
import javax.swing.event.TableModelEvent;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import org.processmining.mining.semanticorganizationmining.SemanticOrgMiningResult;

/**
 * @author Minseok Song
 * @version 1.0
 */

public class OrgEntityResourceTablePanel extends JPanel implements
		TableModelListener {

	private OrgModel orgModel = null;
	private SemanticOrgMiningResult parentPanel = null;

	private List<OrgEntity> orgEntities = null;
	private List<Resource> resources = null;

	private JTable orgEntityTable;
	private JTable resourceTable;

	private JSplitPane splitPane = null;
	private JScrollPane groupPanel = null;
	private JScrollPane resourcePanel = null;

	public OrgEntityResourceTablePanel(OrgModel orgmod,
			SemanticOrgMiningResult parentpanel) {
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		this.orgModel = orgmod;
		this.parentPanel = parentpanel;

		orgEntities = orgModel.getOrgEntityList();
		resources = orgModel.getResourceList();

		init();
	}

	public void init() {
		orgEntityTable = new JTable(new OrgEntityTable());
		resourceTable = new JTable(new ResourceTable());

		groupPanel = new JScrollPane(orgEntityTable);
		resourcePanel = new JScrollPane(resourceTable);
		resourcePanel.setPreferredSize(new Dimension(130, 180));

		splitPane.setLeftComponent(groupPanel);
		splitPane.setRightComponent(resourcePanel);

		splitPane.setDividerLocation(300);

		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(3);
		splitPane.setPreferredSize(new Dimension(400, 800));

		orgEntityTable.getModel().addTableModelListener(this);
		resourceTable.getModel().addTableModelListener(this);

		orgEntityTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					Point p = e.getPoint();
					int row = orgEntityTable.rowAtPoint(p);
					OrgEntity orgEntity = ((OrgEntityTable) orgEntityTable
							.getModel()).getOrgEntity(row);
					((ResourceTable) resourceTable.getModel())
							.updateTable(orgEntity);
				}
			}
		});

		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	public void tableChanged(TableModelEvent e) {
		if (e.getSource() instanceof ResourceTable) {
			if (e.getColumn() == 0) {
				int grp_row = orgEntityTable.getSelectedRow();
				int src_row = resourceTable.getSelectedRow();
				if (grp_row >= 0) {
					if ((Boolean) resourceTable.getValueAt(src_row, 0)) {
						OrgEntity orgEntity = ((OrgEntityTable) orgEntityTable
								.getModel()).getOrgEntity(grp_row);
						((ResourceTable) resourceTable.getModel()).getResource(
								src_row).addOrgEntity(orgEntity);
					} else {
						OrgEntity orgEntity = ((OrgEntityTable) orgEntityTable
								.getModel()).getOrgEntity(grp_row);
						((ResourceTable) resourceTable.getModel()).getResource(
								src_row).removeOrgEntity(orgEntity);
					}
				} else
					((ResourceTable) resourceTable.getModel()).updateTable();
			}
		}
	}

	public OrgEntity getSeletedOrgEntity() {
		if (orgEntityTable.getSelectedRow() < 0)
			return null;
		else
			return ((OrgEntityTable) orgEntityTable.getModel())
					.getOrgEntity(orgEntityTable.getSelectedRow());
	}

	public Resource getSeletedResource() {
		if (resourceTable.getSelectedRow() < 0)
			return null;
		else
			return ((ResourceTable) resourceTable.getModel())
					.getResource(resourceTable.getSelectedRow());
	}

	/**
	 * Private data structure for the table containing the tasks.
	 */
	private class OrgEntityTable extends AbstractTableModel {
		private String[] columnName = { "Org Entity ID", "Type" };

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

			if (row < 0)
				return null;

			switch (column) {
			case 0:
				return orgEntities.get(row).getID();
			case 1:
				return orgEntities.get(row).getEntityType();
			}
			return null;
		}

		public void setValueAt(Object value, int row, int col) {
			// ((Role) roles.get(row)).setName(value.toString());
			fireTableCellUpdated(row, col);
		}

		public OrgEntity getOrgEntity(int row) {
			return (OrgEntity) orgEntities.get(row);
			// return orgModel.getOrgEntity((String) getValueAt(row, 0));
		}
	}

	/**
	 * Private data structure for the table containing the tasks.
	 */
	private class ResourceTable extends AbstractTableModel {

		/**
		 * Required for a serializable class (generated quickfix). Not directly
		 * used.
		 */
		// private static final long serialVersionUID = -6165029231436957878L;
		private String[] columnName = { "Selected", "Resource ID" };
		private ArrayList<Boolean> choose = new ArrayList<Boolean>();

		public ResourceTable() {
			if (resources != null)
				for (int i = 0; i < resources.size(); i++)
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
			return columnName[col];
		}

		/**
		 * Specify the number of rows.
		 * 
		 * @return The number of tasks in the model
		 */
		public int getRowCount() {
			if (resources == null)
				return 0;
			else
				return resources.size();
		}

		/**
		 * Specifiy the number of columns.
		 * 
		 * @return Always 2.
		 */
		public int getColumnCount() {
			return 2;
		}

		public Object getValueAt(int row, int column) {
			if (row < 0)
				return null;
			// fill in name of the decision point
			if (column == 0)
				return (Boolean) choose.get(row);
			else
				return (String) resources.get(row).getID();
		}

		public boolean isCellEditable(int row, int col) {
			if (col == 1)
				return false;
			return true;
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public void updateTable() {
			if (resources == null)
				return;
			int numRes = resources.size();
			for (int i = 0; i < numRes; i++) {
				choose.set(i, false);
			}

			super.fireTableDataChanged();
		}

		public void setValueAt(Object value, int row, int col) {
			if (col == 0)
				choose.set(row, (Boolean) value);
			else {
				// ((Resource) resources.get(row)).setName(value.toString());
			}
			fireTableCellUpdated(row, col);
		}

		public void updateTable(OrgEntity orgEntity) {
			if (resources == null)
				return;
			int numRes = resources.size();
			for (int i = 0; i < numRes; i++) {
				choose.set(i, Boolean.valueOf(resources.get(i).hasOrgEntity(
						orgEntity)));
			}
			super.fireTableDataChanged();
		}

		public Resource getResource(int row) {
			return (Resource) resources.get(row);
			// return orgModel.getResource((String) getValueAt(row, 1));

		}
	}

}
