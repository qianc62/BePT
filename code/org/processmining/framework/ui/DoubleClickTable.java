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

import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

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

public class DoubleClickTable extends JTable {

	public DoubleClickTable(int numRows, int numColumns, JButton button) {
		super(numRows, numColumns);
		addButton(button);
	}

	public DoubleClickTable(JButton button) {
		super();
		addButton(button);
	}

	public DoubleClickTable(Object[][] rowData, Object[] columnNames,
			JButton button) {
		super(rowData, columnNames);
		addButton(button);
	}

	public DoubleClickTable(TableModel dm, JButton button) {
		super(dm);
		addButton(button);
	}

	public DoubleClickTable(TableModel dm, TableColumnModel cm, JButton button) {
		super(dm, cm);
		addButton(button);
	}

	public DoubleClickTable(TableModel dm, TableColumnModel cm,
			ListSelectionModel sm, JButton button) {
		super(dm, cm, sm);
		addButton(button);
	}

	public DoubleClickTable(Vector rowData, Vector columnNames, JButton button) {
		super(rowData, columnNames);
		addButton(button);
	}

	private void addButton(final JButton button) {

		final MyPopupMenu rightMenu = new MyPopupMenu("select",
				DoubleClickTable.this);

		MouseListener correctMouse = new MouseAdapter() {

			public void mousePressed(MouseEvent e) {
				maybeShowPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				maybeShowPopup(e);
			}

			private void maybeShowPopup(MouseEvent e) {
				if (e.isPopupTrigger()) {
					rightMenu.show(e.getComponent(), e.getX(), e.getY());
				}
			}

			public void mouseClicked(MouseEvent me) {

				if (me.getComponent() == DoubleClickTable.this) {
					// it is the table which is selected, so visualise the

					if ((me.getClickCount() == 2)
							&& (me.getButton() == MouseEvent.BUTTON1)) {
						// There is a double click with left mouse button
						// check if the component clicked on is the JTable

						int row = DoubleClickTable.this.getSelectedRow();

						if (row >= 0) {
							button.doClick();

						}
						// else do nothing because there is nothing selected.
					}
				}
			}
		};
		this.addMouseListener(correctMouse);

	}

	private class MyPopupMenu extends JPopupMenu implements ActionListener {

		private JMenuItem itemFind;
		private DoubleClickTable table;

		public MyPopupMenu(DoubleClickTable table) {
			super();
			this.table = table;
			createMenuItems();

		}

		public MyPopupMenu(String label, DoubleClickTable table) {
			super(label);
			this.table = table;
			createMenuItems();
		}

		private void createMenuItems() {
			itemFind = new JMenuItem("Find...");
			itemFind.addActionListener(this);
			this.add(itemFind);

		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == itemFind) {

				FindDialog findDialog = new FindDialog(this);

				if (findDialog.getTextToFind().length() > 0) {
					if (!findDialog.getKeepPreviousSelection()) {
						table.clearSelection();
					}
					boolean elementFound = false;
					for (int column = 0; column < table.getColumnCount(); column++) {
						for (int row = 0; row < table.getRowCount(); row++) {
							String value = (String) table.getModel()
									.getValueAt(row, column);
							boolean result = false;
							if (findDialog.getUseWildcards()) {
								if (value.indexOf(findDialog.getTextToFind()) >= 0) {
									result = true;
								}
							} else {
								result = value.equals(findDialog
										.getTextToFind());
							}
							if (result) {
								table.getSelectionModel().addSelectionInterval(
										row, row);
								table.getColumnModel().getSelectionModel()
										.addSelectionInterval(column, column);
								elementFound = true;
							}
						}
					}
					if (!elementFound) {
						JOptionPane.showMessageDialog(null, "'"
								+ findDialog.getTextToFind() + "' not found!");
					}
				}
			}

		}
	}

	private class FindDialog implements ActionListener {
		private JLabel label;
		private JCheckBox useWildcards;
		private JCheckBox keepPreviousSelection;
		private JButton ok;
		private JButton cancel;
		private JTextField textToFind;
		private JDialog d;

		public FindDialog(Component parent) {
			super();
			d = new JOptionPane().createDialog(parent, "Find");
			label = new JLabel("Text to find");
			textToFind = new JTextField("", 25);
			useWildcards = new JCheckBox("Use wildcards");
			keepPreviousSelection = new JCheckBox("Keep previous selection");
			ok = new JButton("OK");
			ok.addActionListener(this);
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);

			Panel textPanel = new Panel(new FlowLayout());
			textPanel.add(label);
			textPanel.add(textToFind);

			Panel checkBoxesPanel = new Panel(new FlowLayout());
			checkBoxesPanel.add(useWildcards);
			checkBoxesPanel.add(keepPreviousSelection);

			Panel buttonsPanel = new Panel(new FlowLayout());
			buttonsPanel.add(ok);
			buttonsPanel.add(cancel);

			Container c = d.getContentPane();
			c.removeAll();
			GridLayout layout = new GridLayout(3, 1);
			c.setLayout(layout);
			c.add(textPanel);
			c.add(checkBoxesPanel);
			c.add(buttonsPanel);

			d.pack();
			d.setVisible(true);

		}

		public String getTextToFind() {
			return textToFind.getText();
		}

		public boolean getUseWildcards() {
			return useWildcards.isSelected();
		}

		public boolean getKeepPreviousSelection() {
			return keepPreviousSelection.isSelected();
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals(ok.getText())) {
				d.setVisible(false);
			} else if (e.getActionCommand().equals(cancel.getText())) {
				textToFind.setText("");
				d.setVisible(false);
			}

		}

	}

}
