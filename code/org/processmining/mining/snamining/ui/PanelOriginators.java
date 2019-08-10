package org.processmining.mining.snamining.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import org.processmining.framework.ui.filters.GenericMultipleExtFilter;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.mining.snamining.model.OriginatorsModel;

public class PanelOriginators extends JSplitPane implements TableModelListener {

	private JPanel jButtonPane = new JPanel();
	private JScrollPane jScrollPane;
	private JLabel setFilteringLabel = new JLabel();
	private JLabel setFrequencyLabel = new JLabel();
	private JButton selectAllButton = new JButton("Select All");
	private JButton deselectAllButton = new JButton("Deselect All");
	private JButton filterUserButton = new JButton("Filter");
	private JTextField setFreqMin = new JTextField("0");
	private JTextField setFreqMax = new JTextField();
	private JLabel setMaximumFreqLabel = new JLabel();
	private JLabel setMinimumFreqLabel = new JLabel();
	private JTextField orgModelFile = new JTextField();
	private JButton chooseOrgModelButton = new JButton();
	private GUIPropertyBoolean assignOrgModelChecked = new GUIPropertyBoolean(
			"Assign Org Model", false);

	private OriginatorTableModel orgTableModel;

	private JTable usersTable;

	private OriginatorsModel orgModel;

	public PanelOriginators(OriginatorsModel orgModel) {

		super(JSplitPane.HORIZONTAL_SPLIT);
		this.setDividerLocation(155);
		this.setOneTouchExpandable(true);

		this.orgModel = orgModel;
		orgTableModel = new OriginatorTableModel(orgModel);
		init();
	}

	private void jbInit() throws Exception {

	}

	public void init() {
		jButtonPane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;

		usersTable = new JTable(orgTableModel);

		usersTable.getModel().addTableModelListener(this);

		setMinimumFreqLabel.setText("Minimum  = ");
		setMaximumFreqLabel.setText("Maximum  = ");
		setFreqMin.setText(String.valueOf(orgModel.getMinFrequency()));

		setFreqMax.setText(String.valueOf(orgModel.getMaxFrequency()));

		orgModelFile.setMinimumSize(new Dimension(150, 21));
		orgModelFile.setPreferredSize(new Dimension(350, 21));
		orgModelFile.setEditable(false);
		chooseOrgModelButton.setMaximumSize(new Dimension(120, 25));
		chooseOrgModelButton.setMinimumSize(new Dimension(120, 25));
		chooseOrgModelButton.setPreferredSize(new Dimension(120, 25));
		chooseOrgModelButton.setActionCommand("");
		chooseOrgModelButton.setText("Browse...");

		filterUserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				orgModel.filterOriginator(Integer
						.parseInt(setFreqMin.getText()), Integer
						.parseInt(setFreqMax.getText()));
				orgTableModel.updateTable(orgModel);
			};
		});

		selectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				orgModel.selectAll();
				orgTableModel.updateTable(orgModel);
			};
		});

		deselectAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				orgModel.deselectAll();
				orgTableModel.updateTable(orgModel);
			};
		});

		chooseOrgModelButton
				.addActionListener(new java.awt.event.ActionListener() {
					public void actionPerformed(ActionEvent e) {
						chooseOrgModelButton_actionPerformed(e);
					}
				});

		c.weightx = 0.5;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		jButtonPane.add(selectAllButton, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		jButtonPane.add(deselectAllButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		jButtonPane.add(filterUserButton, c);
		c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 1;
		jButtonPane.add(setMinimumFreqLabel, c);
		c.gridx = 1;
		c.gridy = 4;
		c.gridwidth = 1;
		jButtonPane.add(setFreqMin, c);
		c.gridx = 0;
		c.gridy = 5;
		c.gridwidth = 1;
		jButtonPane.add(setMaximumFreqLabel, c);
		c.gridx = 1;
		c.gridy = 5;
		c.gridwidth = 1;
		jButtonPane.add(setFreqMax, c);
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;
		jButtonPane.add(assignOrgModelChecked.getPropertyPanel(), c);
		c.gridx = 0;
		c.gridy = 7;
		c.gridwidth = 2;
		jButtonPane.add(orgModelFile, c);
		c.gridx = 0;
		c.gridy = 8;
		c.gridwidth = 2;
		jButtonPane.add(chooseOrgModelButton, c);

		selectAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		deselectAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		setFilteringLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		setFrequencyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		setMinimumFreqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		setMaximumFreqLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		setFreqMin.setAlignmentX(Component.CENTER_ALIGNMENT);
		setFreqMax.setAlignmentX(Component.CENTER_ALIGNMENT);

		jButtonPane.setPreferredSize(new Dimension(155, 300));
		jScrollPane = new JScrollPane(usersTable);
		jScrollPane.setPreferredSize(new Dimension(300, 300));

		this.setLeftComponent(jButtonPane);
		this.setRightComponent(jScrollPane);

	}

	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();

		if (column == 2) {
			orgModel.changeSelect(row);
		}
	}

	public String getFreqMin() {
		return setFreqMin.getText();
	}

	public String getFreqMax() {
		return setFreqMax.getText();
	}

	private void chooseOrgModelButton_actionPerformed(ActionEvent e) {
		JFileChooser chooser = new JFileChooser();

		chooser.setFileFilter(new GenericMultipleExtFilter(
				new String[] { "xml" }, "XML file (*.xml)"));
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			String name = chooser.getSelectedFile().getPath();
			setChosenXMLFile(name);
		}
	}

	public boolean isAssignOrgModelChecked() {
		return assignOrgModelChecked.getValue()
				&& (orgModelFile.getText() != "");
	}

	private void setChosenXMLFile(String logFileName) {
		orgModelFile.setText(logFileName);
	}

	public String getOrgModelFileName() {
		return orgModelFile.getText();
	}
}

class OriginatorTableModel extends AbstractTableModel {

	private String[] columnName = { "Originators", "Frequency", "Selected" };
	private Object[][] data;

	public OriginatorTableModel(OriginatorsModel orgModel) {
		int numOriginators = orgModel.getNumberofOriginators();

		data = new Object[numOriginators][3];

		for (int i = 0; i < numOriginators; i++) {
			data[i][0] = new String(orgModel.getOriginator(i));
			data[i][1] = new String(orgModel.getFrequency((String) data[i][0]));
			data[i][2] = new Boolean(true);
		}
	}

	public String getColumnName(int col) {
		return columnName[col];
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return columnName.length;
	}

	public Object getValueAt(int row, int column) {
		return data[row][column];
	}

	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		if (col < 2) {
			return false;
		} else {
			return true;
		}
	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	public void updateTable(OriginatorsModel orgModel) {
		int numOriginators = orgModel.getNumberofOriginators();
		for (int i = 0; i < numOriginators; i++) {
			data[i][2] = Boolean.valueOf(orgModel.isSelected(i));
		}
		super.fireTableDataChanged();
	}
}
