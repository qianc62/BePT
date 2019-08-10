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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.analysis.orgsimilarity.SimilarityModel;

/**
 * On the panel originator by task matrix is shown. The matrix show how frequent
 * each originator executes specific tasks. <br/>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class MinmatchAnalysisUI extends JPanel implements Provider {

	private static final long serialVersionUID = 8850726697682188878L;

	private Mismatch2DTableModel matrix;
	private SimilarityModel simModel;

	private JTable table;

	public MinmatchAnalysisUI(SimilarityModel simModel,
			Mismatch2DTableModel matrix) {
		this.simModel = simModel;
		this.matrix = matrix;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {
		int maxElement = matrix.getMaxElement();

		table = new JTable(matrix);

		for (int i = 1; i < table.getColumnCount(); i++) {
			DefaultTableCellRenderer renderer = new ColoredCellRenderer2(
					maxElement);

			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		this.setLayout(new BorderLayout());
		this.add(new JScrollPane(table), BorderLayout.CENTER);

		// / PLUGIN TEST START
		Message.add("<OriginatorByTaskMatrix>", Message.TEST);
		matrix.writeToTestLog();
		// Message.add("<SummaryOfMatrix sumOfCells=\"" +
		// matrix.getSumOfOTMatrix() + "\" numberOfEvents=\"" +
		// log.getLogSummary().getNumberOfAuditTrailEntries() + "\">",
		// Message.TEST);
		// Message.add("</OriginatorByTaskMatrix>", Message.TEST);
		// // PLUGIN TEST END
	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject[] objects = new ProvidedObject[0];
		if (simModel != null) {
			objects = new ProvidedObject[] { new ProvidedObject(
					"Mismatch Matrix", new Object[] { simModel, matrix }) };
		}
		return objects;
	}
}

class ColoredCellRenderer2 extends DefaultTableCellRenderer {

	private static final long serialVersionUID = -8540474103226881230L;
	private int maxValue;

	public ColoredCellRenderer2(int maxValue) {
		this.maxValue = maxValue * 100;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		// int freq = (int)
		// Math.round(100*Double.parseDouble((value).toString()));
		// int color = maxValue <= 0 ? 0 : freq * 0xFF / maxValue;

		if (String.valueOf(value).equals("")) {
			setText("");
			setBackground(Color.white);
		} else {
			if (column == 0)
				setBackground(Color.LIGHT_GRAY);
			else if (column == table.getColumnCount() - 1
					|| row == table.getRowCount() - 1)
				setBackground(new Color(0xFF - 50, 0xFF, 0xFF - 50));
			else
				setBackground(Color.green);
			setText(String.valueOf(value));
		}
		// setText("" + freq/100);
		setForeground(table.getForeground());
		// setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		return this;
	}
}
