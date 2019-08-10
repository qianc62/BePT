package org.processmining.analysis.performance.basicperformance.chart;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

import org.processmining.analysis.performance.basicperformance.BasicPerformanceAnalysisUI;
import org.processmining.analysis.performance.basicperformance.model.StatisticUnit;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance;
import org.processmining.analysis.performance.basicperformance.model.AbstractPerformance2D;

import org.processmining.analysis.performance.basicperformance.model.instance.InstancePerformance;

public class TableChart extends AbstractChart implements ActionListener {
	// HTML styles
	private static final String H1 = "h1";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

	private JTextPane myTextPane; // the HTML pane object
	protected JCheckBox avgCheckBox = new JCheckBox("Average");
	protected JCheckBox freqCheckBox = new JCheckBox("Frequency");
	protected JCheckBox stdCheckBox = new JCheckBox("Stadev");
	protected JCheckBox minCheckBox = new JCheckBox("Minimum");
	protected JCheckBox medianCheckBox = new JCheckBox("Median");
	protected JCheckBox maxCheckBox = new JCheckBox("Maximum");
	protected JButton updateButton;

	public TableChart() {
		super("Text view", "Text view");
		initUI();
	}

	public TableChart(BasicPerformanceAnalysisUI pm) {
		super("Text view", "Text view", pm);
		initUI();
	}

	private void initUI() {
		avgCheckBox.setSelected(true);
		avgCheckBox.setBackground(colorBg);
		freqCheckBox.setSelected(true);
		freqCheckBox.setBackground(colorBg);
		stdCheckBox.setSelected(true);
		stdCheckBox.setBackground(colorBg);
		minCheckBox.setSelected(false);
		minCheckBox.setBackground(colorBg);
		medianCheckBox.setSelected(false);
		medianCheckBox.setBackground(colorBg);
		maxCheckBox.setSelected(false);
		maxCheckBox.setBackground(colorBg);
		updateButton = new JButton("Update");
		updateButton.setOpaque(false);
	}

	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();
		if ((source == minCheckBox) || (source == medianCheckBox)
				|| (source == maxCheckBox)) {
			if (splitPane != null && scrollPane != null) {
				splitPane.remove(scrollPane);
			}
			splitPane.setRightComponent(getGraphPanel());
		}
	}

	// Box-and-Whisker Chart
	// //////////////////////////////////////////////////////////////////////////////
	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param performance
	 *            model
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentation(AbstractPerformance2D performance2D) {

		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();

		tableHeader.append(tag("Activity", TH)); // add activity column header
		tableHeader.append(makeHeader("working"));
		tableHeader.append(makeHeader("waiting"));
		sb.append(tag(tableHeader.toString(), TR));

		List<String> items = performance2D.getItems();
		List<String> items2 = performance2D.getSecondItems();
		if (isItemA) {
			for (String aItem : items) {
				for (String bItem : items2) {
					String aName = aItem + " - " + bItem;
					StatisticUnit su = performance2D.getExecutionTimeSU(aName);
					StringBuffer tempBuffer = new StringBuffer();
					tempBuffer.append(tag(aName, TD));
					boolean flag = false;
					if (su != null && su.getStatistics().getN() != 0) {
						tempBuffer.append(makeRow(su.getStatistics()));
					} else {
						tempBuffer.append(makeDummyRow());
						flag = true;
					}
					StatisticUnit su2 = performance2D.getWaitingTimeSU(aName);
					if (su2 != null && su2.getStatistics().getN() != 0) {
						tempBuffer.append(makeRow(su2.getStatistics()));
					} else {
						if (flag)
							continue;
						tempBuffer.append(makeDummyRow());
					}
					sb.append(tag(tempBuffer.toString(), TR));
				}
			}
		} else {
			for (String bItem : items2) {
				for (String aItem : items) {
					String aName = aItem + " - " + bItem;
					StatisticUnit su = performance2D.getExecutionTimeSU(aName);
					StringBuffer tempBuffer = new StringBuffer();
					tempBuffer.append(tag(aName, TD));
					boolean flag = false;
					if (su != null && su.getStatistics().getN() != 0) {
						tempBuffer.append(makeRow(su.getStatistics()));
					} else {
						tempBuffer.append(makeDummyRow());
						flag = true;
					}
					StatisticUnit su2 = performance2D.getWaitingTimeSU(aName);
					if (su2 != null && su2.getStatistics().getN() != 0) {
						tempBuffer.append(makeRow(su2.getStatistics()));
					} else {
						if (flag)
							continue;
						tempBuffer.append(makeDummyRow());
					}
					sb.append(tag(tempBuffer.toString(), TR));
				}
			}
		}
		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param performance
	 *            model
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentation(AbstractPerformance performance) {

		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("Activity", TH)); // add activity column header
		if (absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			tableHeader.append(makeHeader("sojourn"));
		}
		tableHeader.append(makeHeader("working"));
		tableHeader.append(makeHeader("waiting"));
		sb.append(tag(tableHeader.toString(), TR));

		List<String> items = performance.getItems();
		for (String aItem : items) {
			StatisticUnit su = performance.getExecutionTimeSU(aItem);
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag(aItem, TD));
			boolean flag0 = false;
			boolean flag = false;
			if (absPerformance.getName().equals(
					InstancePerformance.getNameCode())) {
				StatisticUnit su0 = performance.getSojournTimeSU(aItem);
				if (su != null && su.getStatistics().getN() != 0) {
					tempBuffer.append(makeRow(su0.getStatistics()));
				} else {
					flag0 = true;
					tempBuffer.append(makeDummyRow());
				}
			}
			if (su != null && su.getStatistics().getN() != 0) {
				tempBuffer.append(makeRow(su.getStatistics()));
			} else {
				flag = true;
				tempBuffer.append(makeDummyRow());
			}
			StatisticUnit su2 = performance.getWaitingTimeSU(aItem);
			if (su2 != null && su2.getStatistics().getN() != 0) {
				tempBuffer.append(makeRow(su2.getStatistics()));
			} else {
				if (flag0 && flag)
					continue;
				tempBuffer.append(makeDummyRow());
			}
			sb.append(tag(tempBuffer.toString(), TR));
		}
		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	// /////////////////////// PRIVATE HELPER METHODS
	// //////////////////////////////

	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private String tag(String s, String tag) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private double round2(double d) {
		return (double) Math.round(d * 1000) / 1000.0;
	}

	private StringBuffer makeHeader(String str) {
		StringBuffer tableHeader = new StringBuffer();
		if (avgCheckBox.isSelected()) {
			tableHeader.append(tag("Average (" + str + ")", TH)); // add Average
			// column
			// header
		}
		if (freqCheckBox.isSelected()) {
			tableHeader.append(tag("Frequency (" + str + ")", TH)); // add
			// Frequency
			// column
			// header
		}
		if (stdCheckBox.isSelected()) {
			tableHeader.append(tag("Stadev (" + str + ")", TH)); // add Standard
			// Deviation
			// column
			// header
		}
		if (minCheckBox.isSelected()) {
			tableHeader.append(tag("Min (" + str + ")", TH)); // add Min column
			// header
		}
		if (medianCheckBox.isSelected()) {
			tableHeader.append(tag("Median (" + str + ")", TH)); // add Median
			// column
			// header
		}
		if (maxCheckBox.isSelected()) {
			tableHeader.append(tag("Max (" + str + ")", TH)); // add Max column
			// header
		}
		return tableHeader;
	}

	private String makeRow(DescriptiveStatistics ds) {
		String temp = "";
		if (avgCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getMean() / getTimeUnit())),
					TD);
		}
		if (freqCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getN())), TD);
		}
		if (stdCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getStandardDeviation()
					/ getTimeUnit())), TD);
		}
		if (minCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getMin() / getTimeUnit())), TD);
		}
		if (medianCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getPercentile(50)
					/ getTimeUnit())), TD);
		}
		if (maxCheckBox.isSelected()) {
			temp += tag(String.valueOf(round2(ds.getMax() / getTimeUnit())), TD);
		}

		return temp;
	}

	private String makeDummyRow() {
		String temp = "";
		if (avgCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		if (freqCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		if (stdCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		if (minCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		if (medianCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		if (maxCheckBox.isSelected()) {
			temp += tag("N/A", TD);
		}
		return temp;
	}

	protected JScrollPane getGraphPanel() {
		String content = "";
		if (absPerformance instanceof AbstractPerformance2D) {
			content = this
					.getHtmlRepresentation((AbstractPerformance2D) absPerformance);
		} else {
			content = this.getHtmlRepresentation(absPerformance);
		}

		// fill the text pane
		myTextPane = new JTextPane();
		myTextPane.setContentType("text/html");
		myTextPane.setText(content);
		myTextPane.setEditable(false);
		myTextPane.setCaretPosition(0);

		scrollPane = new JScrollPane(myTextPane);
		scrollPane.setBackground(colorBg);
		return scrollPane;
	}

	protected JPanel getOptionPanel() {
		initTimeSort();
		initDimSort();
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		if (!absPerformance.getName().equals(InstancePerformance.getNameCode())) {
			panel.add(dim1Sort.getPropertyPanel());
			panel.add(dim2Sort.getPropertyPanel());
		}

		panel.add(timeUnitSort.getPropertyPanel());
		panel.add(avgCheckBox);
		panel.add(freqCheckBox);
		panel.add(stdCheckBox);
		panel.add(minCheckBox);
		panel.add(medianCheckBox);
		panel.add(maxCheckBox);
		panel.add(updateButton);
		updateButton.addActionListener(this);

		panel.setBackground(colorBg);
		return panel;
	}

	public void actionPerformed(ActionEvent e) {
		updateGUI();
	}
}
