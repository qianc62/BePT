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

package org.processmining.analysis.orgsimilarity.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.orgmodel.OrgEntity;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.analysis.orgsimilarity.SimilarityResultTableModel;
import org.processmining.analysis.orgsimilarity.SimilarityModel;

/**
 * On the panel originator by task matrix is shown. The matrix show how frequent
 * each originator executes specific tasks. <br/>
 * 
 * @author Minseok Song
 * @version 1.0
 */
public class SimilarityResultTableUI extends RoundedPanel {
	private SimilarityUI sUI;
	private SimilarityResultTableModel matrix;
	protected static Color COLOR_BG = new Color(140, 140, 140);
	private JTable table;

	public SimilarityResultTableUI(SimilarityUI sUI,
			SimilarityResultTableModel matrix) {
		super(10, 5, 5);

		setBackground(COLOR_BG);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		// add(Box.createVerticalStrut(8));
		// add(Box.createVerticalStrut(8));
		this.sUI = sUI;
		this.matrix = matrix;
		try {
			jbInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void jbInit() throws Exception {

		table = new JTable(matrix);
		table.setBackground(COLOR_BG);

		for (int i = 1; i < table.getColumnCount(); i++) {
			DefaultTableCellRenderer renderer = new ColoredCellRenderer(matrix,
					sUI.getSelectedMetric());

			renderer.setHorizontalAlignment(SwingConstants.RIGHT);
			table.getColumnModel().getColumn(i).setCellRenderer(renderer);
		}

		JScrollPane listScrollPane = new JScrollPane(table);
		listScrollPane.setBorder(BorderFactory.createEmptyBorder());
		listScrollPane.setBackground(COLOR_BG);

		this.add(listScrollPane);

		// / PLUGIN TEST START
		// Message.add("<OriginatorByTaskMatrix>", Message.TEST);
		// matrix.writeToTestLog();
		// Message.add("<SummaryOfMatrix sumOfCells=\"" +
		// matrix.getSumOfOTMatrix() + "\" numberOfEvents=\"" +
		// log.getLogSummary().getNumberOfAuditTrailEntries() + "\">",
		// Message.TEST);
		// Message.add("</OriginatorByTaskMatrix>", Message.TEST);
		// PLUGIN TEST END
	}
}

class ColoredCellRenderer extends DefaultTableCellRenderer {

	private HashMap<OrgEntity, OrgEntity> mapping;
	private SimilarityResultTableModel matrix;
	private String metric;

	public ColoredCellRenderer(SimilarityResultTableModel mtx, String metric) {
		this.metric = metric;
		this.matrix = mtx;
		this.mapping = (HashMap<OrgEntity, OrgEntity>) matrix.getMapping();
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// Color color = new Color(0xFF, 0xFF, 0xFF);
		// for(OrgEntity entity:mapping.keySet()){
		// if(entity.getID().equals(matrix.getRowIndex(row))){
		// if(column!=0&&mapping.get(entity).getID().equals(matrix.getColumnIndex(column-1))){
		// color = new Color(0xFF, 0xFF, 0xAA);
		// }
		// }
		// }
		// setText("" + freq);
		// setForeground(table.getForeground());
		// setBackground(color);
		if (metric == SimilarityUI.HURESTIC) {
			int freq = (int) Math.round(Double.parseDouble(value.toString()));
			int maxvalue = (int) Math.round(matrix.getMaximumInRow(row));
			int color = maxvalue <= 0 ? 0 : freq * 0xFF / maxvalue;
			setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
			setText("" + freq);
			setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		} else {
			double freq = (double) Double.parseDouble(value.toString());
			Color color = new Color(0xFF, 0xFF, 0xFF);
			for (OrgEntity entity : mapping.keySet()) {
				if (entity.getID().equals(matrix.getRowIndex(row))) {
					if (column != 0
							&& mapping.get(entity).getID().equals(
									matrix.getColumnIndex(column - 1))) {
						color = new Color(0, 0xFF, 0);
					}
				}
			}
			setText("" + freq);
			setBackground(color);
		}
		// else if(metric==SimilarityUI.HURESTIC){
		// int freq = (int) Math.round(Double.parseDouble(value.toString()));
		// int maxvalue = (int) Math.round(matrix.getMaximumInRow(row));
		// int color = maxvalue <= 0 ? 0 : freq * 0xFF / maxvalue;
		// setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		// setText("" + freq);
		// setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		// } else if(matrix.getMaximumInRow(row)<=1.0){
		// double freq = (double)Double.parseDouble(value.toString());
		// Color color = new Color(0xFF, 0xFF, 0xFF);
		// for(OrgEntity entity:mapping.keySet()){
		// if(entity.getID().equals(matrix.getRowIndex(row))){
		// if(column!=0&&mapping.get(entity).getID().equals(matrix.getColumnIndex(column-1))){
		// color = new Color(0, 0xFF, 0);
		// }
		// }
		// }
		// setText("" + freq);
		// setBackground(color);
		// } else {
		// double freq = Double.parseDouble(value.toString());
		// int maxvalue = (int) Math.round(matrix.getMaximumInRow(row));
		// int color = maxvalue <= 0 ? 0 : (int)freq * 0xFF / maxvalue;
		// if(color>0xFF) color=0xFF;
		// setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		// setText("" + freq);
		// setBackground(new Color(0xFF - color, 0xFF, 0xFF - color));
		//			
		// }
		// int freq = (int) Math.round(Double.parseDouble(value.toString()));
		// int color = maxValue <= 0 ? 0 : freq * 0xFF / maxValue;
		setForeground(table.getForeground());
		return this;

	}
}
