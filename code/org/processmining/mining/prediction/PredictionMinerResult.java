package org.processmining.mining.prediction;

import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.jfree.base.*;
import org.jfree.chart.plot.*;

import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import org.processmining.framework.log.LogReader;
import org.processmining.mining.Miner;
import org.processmining.framework.plugin.*;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.mining.*;

public class PredictionMinerResult extends JTabbedPane implements Provider,
		MiningResult {
	private LogReader reslog;

	public PredictionMinerResult(LogReader log) {
		reslog = log;
	}

	public ProvidedObject[] getProvidedObjects() {
		return new ProvidedObject[] { new ProvidedObject("updated log", reslog)
		// new ProvidedObject("Table", this)
		};
	}

	public JComponent getVisualization() {
		return this;
	}

	public LogReader getLogReader() {
		return reslog;
	}

	public void createTablePane(Vector rows, Vector header, String name) {
		JTable newtable = new JTable(rows, header);
		ExcelAdapter myAd = new ExcelAdapter(newtable);
		JScrollPane scrollpane = new JScrollPane(newtable);
		this.addTab(name, null, scrollpane, name);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public void createTablePane(Object[][] rows, Object[] header, String name) {
		JTable newtable = new JTable(rows, header);
		ExcelAdapter myAd = new ExcelAdapter(newtable);
		JScrollPane scrollpane = new JScrollPane(newtable);
		this.addTab(name, null, scrollpane, name);
		this.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
	}

	public void createTablePane(double[] rows, String head) {
		Object[][] table = new Object[rows.length][1];
		for (int i = 0; i < rows.length; i++) {
			table[i][0] = new Double(rows[i]);
		}
		Object[] header = { head };
		createTablePane(table, header, head);
	}

	public void createTablePane(int[][] table, Object[] header, String name) {
		Object[][] t = new Object[table.length][table[0].length];
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				t[i][j] = new Integer(table[i][j]);
			}
		}
		createTablePane(t, header, name);
	}

	public void createTablePane(double[][] table, Object[] header, String name) {
		Object[][] t = new Object[table.length][table[0].length];
		for (int i = 0; i < table.length; i++) {
			for (int j = 0; j < table[0].length; j++) {
				t[i][j] = new Double(table[i][j]);
			}
		}
		createTablePane(t, header, name);
	}

	public void createComponentPane(JComponent component, String name) {
		this.addTab(name, component);
	}

	public void createTabbedTabPane(Vector<Vector<Vector>> tables,
			Vector<Vector<String>> headers, String name, String tabnameprefix) {
		JTabbedPane p = new JTabbedPane();
		int tabid = 0;
		for (Vector<Vector> table : tables) {
			Vector<String> header = headers.get(tabid);
			JTable newtable = new JTable(table, header);
			ExcelAdapter myAd = new ExcelAdapter(newtable);
			JScrollPane scrollpane = new JScrollPane(newtable);
			String tabname = tabnameprefix + "_" + tabid;
			p.addTab(tabname, null, scrollpane, tabname);
			p.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabid++;
		}
		this.addTab(name, null, p, name);
	}

	public void createChartPane(XYSeriesCollection dataset, String xname,
			String yname, String title) {
		JFreeChart mychart = ChartFactory.createXYLineChart(title, xname,
				yname, dataset, PlotOrientation.VERTICAL, true, true, false);
		ChartPanel mychartpanel = new ChartPanel(mychart);
		this.addTab(title, mychartpanel);
	}

}
