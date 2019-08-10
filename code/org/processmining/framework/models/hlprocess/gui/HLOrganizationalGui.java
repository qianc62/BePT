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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.deckfour.slickerbox.components.SlickerButton;
import org.deckfour.slickerbox.util.SlickerSwingUtils;
import org.processmining.framework.models.hlprocess.HLGroup;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLResource;
import org.processmining.framework.util.GuiPropertyStringTextarea;

/**
 * Creates a gui representation for the groups and resources of the high level
 * process. <br>
 * Allows to view and edit the organizational characteristics through a
 * graphical user interface.
 * 
 * @see HLProcessGui
 */
public class HLOrganizationalGui extends JPanel {

	protected HLProcess process;
	// GUI attributes
	private JButton addGroupButton = new SlickerButton("Add Group");
	/** adds a new group */
	private JButton removeUnassignedButton = new SlickerButton("Remove Unused");
	/** removes all unused groups */
	private JButton addResourceButton = new SlickerButton("Add Resource");
	/** adds a new resource */
	private JButton removeSelectedButton = new SlickerButton("Remove Selected");
	/** removes existing resource */
	private int indexGrp = 0;
	private int indexRsc = 0;

	private JPanel grpresPanel = new JPanel();
	private GroupsPanel groupsPanel;

	/**
	 * Creates organizational gui.
	 * 
	 * @param hlprocess
	 *            the high level process of which the organizational
	 *            characteristics are to be displayed
	 */
	public HLOrganizationalGui(HLProcess hlprocess) {
		process = hlprocess;
		// create the double click table and the content view for activity
		// overview
		createResourceView();
		// connect functionality to GUI elements
		registerGuiActionListener();
	}

	/**
	 * Builds the left-hand side part of the activity view tab.
	 */
	private void createResourceView() {
		this.setLayout(new BorderLayout());
		this.setBackground(HLProcessGui.bgColor);
		String description = new String(
				"Here you can specify which resources and groups of resources are available in the process. "
						+ "Groups that have been created here can then be assigned to activities in the 'Activities' tab.");
		GuiPropertyStringTextarea helpText = new GuiPropertyStringTextarea(
				description, 2);
		this.add(helpText.getPropertyPanel(), BorderLayout.NORTH);
		// fill the table
		groupsPanel = new GroupsPanel();
		grpresPanel.setLayout(new BorderLayout());
		grpresPanel.setBorder(BorderFactory.createEmptyBorder());
		grpresPanel.add(groupsPanel);
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// set up tool tips
		addGroupButton.setToolTipText("Adds a new group to the process");
		addResourceButton.setToolTipText("Adds a new resource to the process");
		removeSelectedButton
				.setToolTipText("Removes currently selected groups and resources from the process");
		removeUnassignedButton
				.setToolTipText("Removes all unassigned groups and resources from the process");
		// add buttons
		buttonPanel.add(addGroupButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		buttonPanel.add(addResourceButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(removeSelectedButton);
		buttonPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		buttonPanel.add(removeUnassignedButton);
		this.add(grpresPanel, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		SlickerSwingUtils.injectTransparency(this);
	}

	/**
	 * Connect GUI elements with functionality to create interaction.
	 */
	private void registerGuiActionListener() {
		addGroupButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new group to simulation model
				HLGroup grp = new HLGroup("New Group" + indexGrp++, process);
				// redraw attributes section
				grpresPanel.removeAll();
				groupsPanel = null;
				groupsPanel = new GroupsPanel();
				grpresPanel.add(groupsPanel);
				grpresPanel.validate();
				grpresPanel.repaint();

			}
		});
		addResourceButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// add new group to simulation model
				String name = "New Resource" + indexRsc++;
				HLResource newSrc = new HLResource(name, process);
				// redraw attributes section
				grpresPanel.removeAll();
				groupsPanel = null;
				groupsPanel = new GroupsPanel();
				grpresPanel.add(groupsPanel);
				grpresPanel.validate();
				grpresPanel.repaint();
			}
		});
		removeSelectedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// remove all selected resources and groups from simulation
				// model
				for (HLResource res : groupsPanel.getSelectedResources()) {
					process.removeResource(res.getID());
				}
				for (HLGroup grp : groupsPanel.getSelectedGroups()) {
					process.removeGroup(grp.getID());
				}
				grpresPanel.removeAll();
				groupsPanel = null;
				groupsPanel = new GroupsPanel();
				grpresPanel.add(groupsPanel);
				grpresPanel.validate();
				grpresPanel.repaint();
			}
		});
		removeUnassignedButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				process.removeUnassignedGroupsAndResources();
				grpresPanel.removeAll();
				groupsPanel = null;
				groupsPanel = new GroupsPanel();
				grpresPanel.add(groupsPanel);
				grpresPanel.validate();
				grpresPanel.repaint();
			}
		});
	}

	class GroupsPanel extends JPanel implements TableModelListener {
		// GUI attributes
		private JTable groupTable;
		private JTable resourceTable;

		private JScrollPane groupPanel = null;
		private JScrollPane resourcePanel = null;

		public GroupsPanel() {
			this.setLayout(new BorderLayout());
			this.setBorder(BorderFactory.createEmptyBorder());
			// create the double click table and the content view for activity
			// overview
			createGrpResView();

			groupTable.addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					int row = 0;
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						row = (groupTable.getSelectedRow() - 1) <= 0 ? 0
								: (groupTable.getSelectedRow() - 1);
					} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						row = (groupTable.getSelectedRow() + 1) >= groupTable
								.getRowCount() ? groupTable.getRowCount() - 1
								: groupTable.getSelectedRow() + 1;
					} else
						return;

					((ResourceTable) resourceTable.getModel())
							.updateTable((HLGroup) groupTable
									.getValueAt(row, 0));
				}
			});

			groupTable.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 1) {
						Point p = e.getPoint();
						int row = groupTable.rowAtPoint(p);
						((ResourceTable) resourceTable.getModel())
								.updateTable((HLGroup) groupTable.getValueAt(
										row, 0));
					}
				}
			});
		}

		/**
		 * Builds the left-hand side part of the activity view tab.
		 */
		private void createGrpResView() {
			groupTable = new JTable(new GroupTable());
			resourceTable = new JTable(new ResourceTable());

			groupPanel = new JScrollPane(groupTable);
			groupPanel.setBorder(BorderFactory.createEmptyBorder());
			groupPanel.setPreferredSize(new Dimension(160, 70));
			resourcePanel = new JScrollPane(resourceTable);
			resourcePanel.setBorder(BorderFactory.createEmptyBorder());
			resourcePanel.setPreferredSize(new Dimension(160, 70));
			JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
					groupPanel, resourcePanel);
			splitPane.setDividerLocation(250);

			splitPane.setOneTouchExpandable(true);
			splitPane.setDividerSize(3);
			this.add(splitPane);
			groupTable.getModel().addTableModelListener(this);
			resourceTable.getModel().addTableModelListener(this);
		}

		public void tableChanged(TableModelEvent e) {
			if (e.getSource() instanceof ResourceTable) {
				if (e.getColumn() == 0) {
					int grp_row = groupTable.getSelectedRow();
					int src_row = resourceTable.getSelectedRow();
					;
					if (grp_row >= 0) {
						if ((Boolean) resourceTable.getValueAt(src_row, 0)) {
							HLGroup t = (HLGroup) groupTable.getValueAt(
									grp_row, 0);
							t.addResource((HLResource) resourceTable
									.getValueAt(src_row, 1));
						} else {
							HLGroup t = (HLGroup) groupTable.getValueAt(
									grp_row, 0);
							t.removeResource(((HLResource) resourceTable
									.getValueAt(src_row, 1)).getID());
						}
					} else
						((ResourceTable) resourceTable.getModel())
								.updateTable();
				}
			}
		}

		public List<HLGroup> getSelectedGroups() {
			List<HLGroup> selected = new ArrayList<HLGroup>();
			int[] indexArray = groupTable.getSelectedRows();
			for (int i = 0; i < indexArray.length; i++) {
				selected.add((HLGroup) groupTable.getValueAt(indexArray[i], 0));
			}
			return selected;
		}

		public List<HLResource> getSelectedResources() {
			List<HLResource> selected = new ArrayList<HLResource>();
			int[] indexArray = resourceTable.getSelectedRows();
			for (int i = 0; i < indexArray.length; i++) {
				selected.add((HLResource) resourceTable.getValueAt(
						indexArray[i], 1));
			}
			return selected;
		}

		/**
		 * Private data structure for the table containing the activities.
		 */
		private class GroupTable extends AbstractTableModel {

			/**
			 * Specify the headings for the columns.
			 * 
			 * @param col
			 *            The column specified.
			 * @return The heading of the respective column.
			 */
			public String getColumnName(int col) {
				// heading of single column
				return "Groups";
			}

			/**
			 * Specify the number of rows.
			 * 
			 * @return The number of tasks in the model
			 */
			public int getRowCount() {
				return process.getGroups().size();
			}

			/**
			 * Specifiy the number of columns.
			 * 
			 * @return Always 1.
			 */
			public int getColumnCount() {
				return 1;
			}

			public boolean isCellEditable(int row, int col) {
				return true;
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

				return process.getGroups().get(row);
			}

			public void setValueAt(Object value, int row, int col) {
				if (row < 0) {
					return;
				}
				process.getGroups().get(row).setName(value.toString());
				super.fireTableDataChanged();
			}

		}

		/**
		 * Private data structure for the table containing the activities.
		 */
		private class ResourceTable extends AbstractTableModel {

			private String[] columnName = { "Selected", "Resource" };
			private ArrayList<Boolean> choose = new ArrayList<Boolean>();

			public ResourceTable() {
				if (process.getResources().size() != 0) {
					for (int i = 0; i < process.getResources().size(); i++) {
						choose.add(new Boolean(false));
					}
				}
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
				return process.getResources().size();
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
				if (column == 0) {
					return (Boolean) choose.get(row);
				} else {
					return process.getResources().get(row);
				}
			}

			public boolean isCellEditable(int row, int col) {
				return true;
			}

			public Class getColumnClass(int c) {
				if (c == 0)
					return getValueAt(0, c).getClass();
				else
					return (new String()).getClass();
			}

			public void updateTable() {
				if (process.getResources().size() == 0)
					return;
				for (int i = 0; i < process.getResources().size(); i++) {
					choose.set(i, false);
				}
				super.fireTableDataChanged();
			}

			public void setValueAt(Object value, int row, int col) {
				if (col == 0) {
					choose.set(row, (Boolean) value);
				} else {
					process.getResources().get(row).setName(value.toString());
				}
				fireTableCellUpdated(row, col);
			}

			public void updateTable(HLGroup hgGroup) {
				if (process.getResources().size() == 0)
					return;

				for (int i = 0; i < process.getResources().size(); i++) {
					choose.set(i, Boolean.valueOf(hgGroup
							.isInGroup((HLResource) (process.getResources()
									.get(i)))));
				}
				super.fireTableDataChanged();
			}
		}
	}
}
