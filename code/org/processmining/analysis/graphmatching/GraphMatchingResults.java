package org.processmining.analysis.graphmatching;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;

public class GraphMatchingResults extends JPanel {

	private static final long serialVersionUID = -864852014503773727L;

	@SuppressWarnings("unused")
	private double similarities[][];

	public GraphMatchingResults(List<String> searchModelNames,
			List<String> docModelNames, double similarities[][], String matches) {
		this.similarities = similarities;

		JTabbedPane tabbedPane = new JTabbedPane();

		JScrollPane scrollPane1 = new JScrollPane();
		scrollPane1
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane1
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JTable table = new JTable(new SimilarityTableModel(searchModelNames,
				docModelNames, similarities));
		scrollPane1.setViewportView(table);
		tabbedPane.add("Similarity", scrollPane1);

		JScrollPane scrollPane2 = new JScrollPane();
		scrollPane2
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane2
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		JTextArea matchingText = new JTextArea();
		matchingText.setText(matches);
		scrollPane2.setViewportView(matchingText);
		tabbedPane.add("Matches", scrollPane2);

		this.setLayout(new BorderLayout());
		this.add(tabbedPane, BorderLayout.CENTER);

	}
}

class SimilarityTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 5638589730513498635L;

	private List<String> searchModelNames;
	private List<String> docModelNames;
	private double similarities[][];

	public SimilarityTableModel(List<String> searchModelNames,
			List<String> docModelNames, double similarities[][]) {
		this.searchModelNames = searchModelNames;
		this.docModelNames = docModelNames;
		this.similarities = similarities;
	}

	public int getRowCount() {
		return docModelNames.size();
	}

	public int getColumnCount() {
		return searchModelNames.size() + 1;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		} else {
			return searchModelNames.get(col - 1);
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return docModelNames.get(rowIndex);
		} else {
			return new Double(similarities[rowIndex][columnIndex - 1]);
		}
	}

};