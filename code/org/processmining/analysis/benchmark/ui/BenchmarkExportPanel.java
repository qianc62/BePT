/*
 * Copyright (c) 2007 Christian W. Guenther (christian@deckfour.org)
 * 
 * LICENSE:
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * 
 * EXEMPTION:
 * 
 * License to link and use is also granted to open source programs which
 * are not licensed under the terms of the GPL, given that they satisfy one
 * or more of the following conditions:
 * 1) Explicit license is granted to the ProM and ProMimport programs for
 *    usage, linking, and derivative work.
 * 2) Carte blance license is granted to all programs developed at
 *    Eindhoven Technical University, The Netherlands, or under the
 *    umbrella of STW Technology Foundation, The Netherlands.
 * For further exemptions not covered by the above conditions, please
 * contact the author of this code.
 * 
 */
package org.processmining.analysis.benchmark.ui;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.processmining.analysis.benchmark.BenchmarkItem;
import org.processmining.analysis.benchmark.metric.BenchmarkMetric;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.UISettings;

/**
 * @author christian
 */
public class BenchmarkExportPanel extends JPanel {

	protected List<BenchmarkItem> items;
	protected List<BenchmarkMetric> metrics;

	protected JCheckBox columnHeaderBox;
	protected JCheckBox rowHeaderBox;
	protected JCheckBox semicolonBox;

	public BenchmarkExportPanel(List<BenchmarkItem> benchmarkItems,
			List<BenchmarkMetric> benchmarkMetrics) {
		setBackground(new Color(180, 180, 180));
		items = benchmarkItems;
		metrics = benchmarkMetrics;
		setBorder(BorderFactory.createEmptyBorder());
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		JPanel innerPanel = new JPanel();
		innerPanel.setBackground(new Color(180, 180, 180));
		innerPanel.setBorder(BorderFactory.createEmptyBorder());
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
		columnHeaderBox = new JCheckBox("include column headers (metric names)");
		columnHeaderBox.setSelected(true);
		columnHeaderBox.setBackground(new Color(180, 180, 180));
		rowHeaderBox = new JCheckBox("include row headers (item names)");
		rowHeaderBox.setSelected(true);
		rowHeaderBox.setBackground(new Color(180, 180, 180));
		semicolonBox = new JCheckBox(
				"use semicolon separators (MS Excel compliant)");
		semicolonBox.setSelected(true);
		semicolonBox.setBackground(new Color(180, 180, 180));
		JButton exportButton = new AutoFocusButton("export...");
		exportButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				export(columnHeaderBox.isSelected(), rowHeaderBox.isSelected(),
						semicolonBox.isSelected());
			}
		});
		innerPanel.add(Box.createVerticalGlue());
		innerPanel.add(new JLabel("Export benchmark data as CSV file"));
		innerPanel.add(Box.createVerticalStrut(20));
		innerPanel.add(columnHeaderBox);
		innerPanel.add(rowHeaderBox);
		innerPanel.add(Box.createVerticalStrut(10));
		innerPanel.add(semicolonBox);
		innerPanel.add(Box.createVerticalStrut(20));
		innerPanel.add(exportButton);
		innerPanel.add(Box.createVerticalGlue());
		add(Box.createHorizontalGlue());
		add(innerPanel);
		add(Box.createHorizontalGlue());
	}

	public void export(boolean exportColumnHeaders, boolean exportRowHeaders,
			boolean useSemicolons) {
		String separator = ", ";
		if (useSemicolons == true) {
			separator = "; ";
		}
		FileDialog saveDialog = new FileDialog(MainUI.getInstance(),
				"Choose save location...", FileDialog.SAVE);
		File lastSave = UISettings.getInstance().getLastExportLocation();
		saveDialog.setDirectory(lastSave.getParent());
		saveDialog.setVisible(true);
		if (saveDialog.getFile() != null && saveDialog.getFile().length() > 0) {
			File saveFile = new File(saveDialog.getDirectory(), saveDialog
					.getFile());
			try {
				BufferedWriter buf = new BufferedWriter(
						new FileWriter(saveFile));
				writeCsv(buf, exportColumnHeaders, exportRowHeaders, separator);
				buf.flush();
				buf.close();
			} catch (IOException e) {
				// error
				e.printStackTrace();
			}
		}
	}

	protected void writeCsv(Writer writer, boolean exportColumnHeaders,
			boolean exportRowHeaders, String separator) throws IOException {
		if (exportColumnHeaders == true) {
			if (exportRowHeaders == true) {
				writer.write("Benchmark items" + separator);
			}
			for (int i = 0; i < metrics.size(); i++) {
				writer.write(metrics.get(i).name());
				if (i < (metrics.size() - 1)) {
					writer.write(separator);
				} else {
					writer.write("\n");
				}
			}
		}
		for (int i = 0; i < items.size(); i++) {
			BenchmarkItem item = items.get(i);
			if (exportRowHeaders == true) {
				writer.write(item.getName() + separator);
			}
			for (int j = 0; j < metrics.size(); j++) {
				double value = item.getMeasurement(metrics.get(j).name());
				if (value >= 0.0) {
					writer.write(Double.toString(value));
				} else {
					writer.write("INVALID");
				}
				if (j < (metrics.size() - 1)) {
					writer.write(separator);
				} else {
					writer.write("\n");
				}
			}
		}
	}

}
