package org.processmining.analysis.causality;

import java.util.List;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.plugin.*;
import org.processmining.framework.util.*;
import org.processmining.analysis.epc.similarity.Similarities;

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
public class FootprintSimilarityResultUI extends JPanel implements Provider {

	private FootprintSimilarityResult result;
	private JTable table;
	private JScrollPane scrollPane;

	public FootprintSimilarityResultUI(FootprintSimilarityResult result) {
		this.result = result;

		scrollPane = new JScrollPane();
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		table = new JTable(new SimilarityTableModel(result.getBasePaths(),
				result.getBaseEpcs(), result.getCompareToPaths(), result
						.getCompareToEpcs(), result.getSimilarities())) {
			// Implement table header tool tips.
			protected JTableHeader createDefaultTableHeader() {
				return new JTableHeader(columnModel) {
					public String getToolTipText(MouseEvent e) {
						java.awt.Point p = e.getPoint();
						int index = columnModel.getColumnIndexAtX(p.x);
						int realIndex = columnModel.getColumn(index)
								.getModelIndex() - 1;
						if (realIndex < 0) {
							return "";
						} else {
							String tip = "<html>"
									+ FootprintSimilarityResultUI.this.result
											.getCompareToPaths().get(realIndex)
											.replace(".", "<br>") + "</html>";

							return tip;
						}
					}
				};
			}

		};

		scrollPane.setViewportView(table);

		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.CENTER);

	}

	public ProvidedObject[] getProvidedObjects() {
		ProvidedObject simMatrix = new ProvidedObject("Similarity matrix",
				new Object[] { result });
		return new ProvidedObject[] { simMatrix };
	}

}

class SimilarityTableModel extends AbstractTableModel {

	private List<String> basePaths;
	private List<ConfigurableEPC> baseEpcs;
	private List<String> compPaths;
	private List<ConfigurableEPC> compEpcs;
	private Similarities sim;

	public SimilarityTableModel(List<String> basePaths,
			List<ConfigurableEPC> baseEpcs, List<String> compPaths,
			List<ConfigurableEPC> compEpcs, Similarities sim) {
		this.basePaths = basePaths;
		this.sim = sim;
		this.baseEpcs = baseEpcs;
		this.compEpcs = compEpcs;
		this.compPaths = compPaths;
	}

	public int getRowCount() {
		return basePaths.size();
	}

	public int getColumnCount() {
		return compPaths.size() + 1;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	public String getColumnName(int col) {
		if (col == 0) {
			return "";
		} else {
			return StringNormalizer.normalize(compEpcs.get(col - 1)
					.getIdentifier());
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return StringNormalizer.normalize(baseEpcs.get(rowIndex)
					.getIdentifier());
		}
		return sim.get(rowIndex, columnIndex - 1);
	}

};
