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

package org.processmining.analysis.originator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.NumberFormat;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.Dot;
import org.processmining.mining.snamining.model.SocialNetworkMatrix;

import att.grappa.Graph;
import att.grappa.GrappaAdapter;
import att.grappa.GrappaPanel;
import cern.colt.matrix.DoubleMatrix2D;

public class GroupSNAResults extends JPanel implements Provider {

	public GroupSNAResults() {
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private JSplitPane splitPane;
	private JSplitPane splitPaneTop;
	private JPanel menuPane = new JPanel();
	private JPanel graphPanel;
	private JScrollPane jscroll;
	private JScrollPane jscrollTable;

	private JTable table;

	private SocialNetworkMatrix snMatrix;
	private ResultTableModel dataMatrix;

	private JLabel thresholdLabel = new JLabel();
	private JTextField thresholdField = new JTextField();
	private JButton thresholdButton = new JButton(" Apply ");
	private JButton removeButton = new JButton(
			"          Remove isolated nodes          ");

	public GroupSNAResults(SocialNetworkMatrix snMatrix) {

		this.snMatrix = snMatrix;

		thresholdLabel.setText("Threshold Value =  ");
		thresholdField.setPreferredSize(new Dimension(50, 21));
		thresholdField.setText("0");

		thresholdButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getSNMatrix().applyThresholdValue(
						Double.valueOf(thresholdField.getText()).doubleValue());
				redrawGraphPanel();
				redrawMatrixPanel();
			};
		});

		removeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getSNMatrix().removeDisconnectedOriginator();
				redrawGraphPanel();
				redrawMatrixPanel();
			};
		});

		menuPane.setLayout(new GridBagLayout());
		menuPane.add(thresholdLabel, new GridBagConstraints(0, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		menuPane.add(thresholdField, new GridBagConstraints(1, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));

		menuPane.add(thresholdButton, new GridBagConstraints(2, 0, 1, 1, 0.0,
				0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		menuPane.add(removeButton, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0,
						0, 0, 0), 0, 0));

		// draw matrix panel
		drawMatrixPanel();

		splitPaneTop = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, menuPane,
				jscrollTable);
		splitPaneTop.setDividerLocation(0.3);
		splitPaneTop.setResizeWeight(0.3);

		// draw graph panel
		drawGraphPanel();
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, splitPaneTop,
				jscroll);

		splitPane.setDividerLocation(0.5);
		splitPane.setResizeWeight(0.5);
		splitPane.setOneTouchExpandable(true);
		this.setLayout(new BorderLayout());
		this.add(splitPane, BorderLayout.CENTER);
	}

	public SocialNetworkMatrix getSNMatrix() {
		return snMatrix;
	}

	private JPanel getGraphPanel(String[] users, DoubleMatrix2D matrix) {
		BufferedWriter bw;
		Graph graph;
		NumberFormat nf = NumberFormat.getInstance();
		File dotFile;

		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);
		try {
			// create temporary DOT file
			dotFile = File.createTempFile("pmt", ".dot");
			bw = new BufferedWriter(new FileWriter(dotFile, false));
			bw
					.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
			bw.write("fontname=\"Arial\";rankdir=\"LR\"; \n");
			bw
					.write("edge [arrowsize=\"0.5\",decorate=true,fontname=\"Arial\",fontsize=\"8\"];\n");
			bw
					.write("node [height=\".1\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");

			for (int i = 0; i < users.length; i++) {
				bw.write("t" + i + " [shape=\"box\",label=\"" + users[i]
						+ "\"];\n");
			}

			for (int i = 0; i < matrix.rows(); i++) {
				for (int j = 0; j < matrix.columns(); j++) {
					double value = matrix.get(i, j);

					if (value > 0) {
						bw.write("t" + i + " -> t" + j + " [label=\""
								+ nf.format(value) + "\"];\n");
					}
				}
			}
			bw.write("}\n");
			bw.close();

			// execute dot and parse the output of dot to create a Graph
			System.out.println(dotFile.getAbsolutePath());
			graph = Dot.execute(dotFile.getAbsolutePath());

			// adjust some settings
			graph.setEditable(true);
			graph.setMenuable(true);
			graph.setErrorWriter(new PrintWriter(System.err, true));

			// create the visual component and return it
			GrappaPanel gp = new GrappaPanel(graph);
			gp.addGrappaListener(new GrappaAdapter());
			gp.setScaleToFit(true);

			return gp;
		} catch (Exception ex) {
			Message.add("Error while performing graph layout: "
					+ ex.getMessage(), Message.ERROR);
			return null;
		}
	}

	public void drawMatrixPanel() {
		jscrollTable = null;
		dataMatrix = null;

		dataMatrix = new ResultTableModel(snMatrix.getNodeNames(), snMatrix
				.getMatrix());

		final ColorRenderer mcr = new ColorRenderer();
		table = new JTable(dataMatrix) {
			public ColorRenderer getCellRenderer(int row, int column) {
				return mcr;
			}
		};

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		jscrollTable = new JScrollPane(table);
	}

	public void redrawMatrixPanel() {
		drawMatrixPanel();
		splitPaneTop.setRightComponent(jscrollTable);
		splitPaneTop.validate();
		splitPaneTop.repaint();
	}

	public void drawGraphPanel() {
		jscroll = null;
		graphPanel = null;
		graphPanel = getGraphPanel(snMatrix.getNodeNames(), snMatrix
				.getMatrix());
		jscroll = new JScrollPane(graphPanel);
	}

	public void redrawGraphPanel() {
		drawGraphPanel();
		splitPane.setRightComponent(jscroll);
		splitPane.validate();
		splitPane.repaint();
	}

	public JComponent getVisualization() {
		return this;
	}

	public ProvidedObject[] getProvidedObjects() {
		return (new ProvidedObject[] { new ProvidedObject("SNA",
				new Object[] { snMatrix }) });
	}

	private void jbInit() throws Exception {
	}

}

class ResultTableModel extends AbstractTableModel {

	private Object[][] data;
	private String[] users;

	public ResultTableModel(String[] users, DoubleMatrix2D data) {
		this.users = new String[users.length + 1];
		this.data = new Object[data.rows()][data.columns() + 1];

		this.users[0] = "";

		for (int i = 0; i < data.rows(); i++) {
			this.users[i + 1] = users[i];
			this.data[i][0] = users[i];
			for (int j = 0; j < data.columns(); j++)
				this.data[i][j + 1] = data.get(i, j);
		}

	}

	public String getColumnName(int col) {
		return users[col];
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return users.length;
	}

	public Object getValueAt(int row, int column) {
		return data[row][column];
	}
}

class ColorRenderer extends DefaultTableCellRenderer {

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Obtains default cell settings
		Component cell = super.getTableCellRendererComponent(table, value,
				isSelected, hasFocus, row, column);

		if (column == 0)
			cell.setBackground(new Color(240, 240, 239));
		else
			cell.setBackground(Color.white);

		return cell;
	}
}
