package org.processmining.analysis.eventdata;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.jfree.chart.axis.NumberAxis;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import java.io.IOException;
import java.lang.Math.*;
import java.util.*;
import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.xy.*;
import org.jfree.data.category.*;
import org.jfree.ui.*;
import org.jfree.base.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.DeviationRenderer;

import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.NaturalSort;
import org.processmining.framework.log.*;
import org.processmining.mining.prediction.SpringUtils;

/**
 * @author Ronald Crooy
 * 
 */
public class EventDataAttributesAnalysis {
	private JPanel chartPanel;
	private JPanel optionsPanel;
	private JPanel mainPanel;
	private LogReader mylog;
	private IntervalXYDataset data;
	private JList attributeslist;
	private JComboBox xbox;
	private JComboBox timebox;
	private JSpinner BSpinner;
	private boolean no_intervals = false;
	private JCheckBox squaredinput;

	@Analyzer(name = "Event Data Attribute Visualizer", names = { "Log" })
	public JComponent analyze(LogReader log) {
		/*
		 * this plugin takes a log, shows a list of available data-attributes
		 * and a list of cases After selecting a data-attribute (and possibly a
		 * case) a graph is made of the value of the data-attribute against
		 * either time or against the sequence of events.
		 * 
		 * input: log with data attributes gui-input: select a data-attribute,
		 * choose time or event-sequence, and possibly a case
		 * 
		 * internally : if not caseselected -> scatterplot of values against
		 * time/event-number else show graph for value against time/event-number
		 * 
		 * createGraph : check number / string
		 */
		mylog = log;
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		chartPanel = new JPanel();
		chartPanel.setLayout(new BoxLayout(chartPanel, BoxLayout.PAGE_AXIS));

		optionsPanel = new JPanel();
		optionsPanel.setLayout(new SpringLayout());

		JLabel attributelabel = new JLabel("Select attributes to use");
		attributeslist = new JList(getAttributes());
		attributeslist
				.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		attributeslist.setLayoutOrientation(JList.VERTICAL);
		optionsPanel.add(attributelabel);
		optionsPanel.add(attributeslist);

		JLabel xlabel = new JLabel("Chart type");
		String[] xvalues = new String[4];
		xvalues[0] = " Attribute values against event sequence";
		xvalues[1] = "Attribute values against timestamps";
		xvalues[2] = "Average attribute values against event sequence";
		xvalues[3] = "Average attribute values against timestamps";
		xbox = new JComboBox(xvalues);
		xbox.setMaximumSize(xbox.preferredSize());
		xbox.setSelectedIndex(1);

		xbox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				if (cb.getSelectedIndex() == 3) {
					BSpinner.setVisible(true);
				} else {
					BSpinner.setVisible(false);
				}
			};
		});

		optionsPanel.add(xlabel);
		optionsPanel.add(xbox);

		JLabel timelabel = new JLabel("show time by ");
		String[] timevalues = new String[4];
		timevalues[0] = "second";
		timevalues[1] = "minute";
		timevalues[2] = "hour";
		timevalues[3] = "day";
		timebox = new JComboBox(timevalues);
		timebox.setMaximumSize(timebox.preferredSize());
		timebox.setSelectedIndex(1);
		optionsPanel.add(timelabel);
		optionsPanel.add(timebox);

		SpinnerModel Bmodel = new SpinnerNumberModel(10, 2, 1000000, 1);
		BSpinner = new JSpinner(Bmodel);
		JLabel BLabel = new JLabel("Select histogram barsize");
		JLabel B2Label = new JLabel("used for average against timestamps");
		BSpinner.setMaximumSize(BSpinner.preferredSize());
		BSpinner.setVisible(false);
		optionsPanel.add(BLabel);
		optionsPanel.add(B2Label);
		optionsPanel.add(BSpinner);

		JButton updatebutton = new JButton("update");
		updatebutton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				Object[] sels = attributeslist.getSelectedValues();
				String[] els = new String[sels.length];
				for (int i = 0; i < sels.length; i++) {
					els[i] = sels[i].toString();
				}

				long timesize = 1000;
				switch (timebox.getSelectedIndex()) {
				case 0:
					timesize = 1000;
					break;
				case 1:
					timesize = 1000 * 60;
					break;
				case 2:
					timesize = 1000 * 60 * 60;
					break;
				case 3:
					timesize = 1000 * 60 * 60 * 24;
					break;
				}

				String xname = null;
				JFreeChart mychart = null;
				if (xbox.getSelectedIndex() == 0) {
					data = getDataAttributes(els, false, timesize);
					xname = "Event sequence";
					mychart = ChartFactory.createScatterPlot(
							"Scatterplot of all values", xname,
							"attribute value", data, PlotOrientation.VERTICAL,
							true, true, false);
				} else if (xbox.getSelectedIndex() == 1) {
					data = getDataAttributes(els, true, timesize);
					xname = "Time(" + timebox.getSelectedItem()
							+ ") since beginning of the process";
					mychart = ChartFactory.createScatterPlot(
							"Scatterplot of all values", xname,
							"attribute value", data, PlotOrientation.VERTICAL,
							true, true, false);
				} else if (xbox.getSelectedIndex() == 2) {
					xname = "Event sequence";
					data = getHistrogrammedDataAttributes(els, 1, 1);
					mychart = ChartFactory.createXYLineChart("Average values",
							xname, "attribute value", data,
							PlotOrientation.VERTICAL, true, true, false);
					mychart.setBackgroundPaint(Color.white);
					XYPlot plot = mychart.getXYPlot();

					plot.setBackgroundPaint(Color.white);
					plot.setDomainGridlinePaint(Color.white);
					plot.setRangeGridlinePaint(Color.white);

					DeviationRenderer renderer = new DeviationRenderer(true,
							true);
					renderer.setSeriesStroke(0, new BasicStroke(3.0f,
							BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					renderer.setSeriesStroke(0, new BasicStroke(2.0f));
					renderer.setSeriesStroke(1, new BasicStroke(2.0f));
					renderer.setSeriesStroke(2, new BasicStroke(2.0f));
					renderer.setSeriesStroke(3, new BasicStroke(2.0f));
					renderer.setSeriesFillPaint(0, Color.red);
					renderer.setSeriesFillPaint(1, Color.blue);
					renderer.setSeriesFillPaint(2, Color.green);
					renderer.setSeriesFillPaint(3, Color.orange);
					plot.setRenderer(renderer);
					// change the auto tick unit selection to integer units
					// only...
					NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
					// yAxis.setAutoRangeIncludesZero(false);
					yAxis.setStandardTickUnits(NumberAxis
							.createIntegerTickUnits());

					NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();
					xAxis.setStandardTickUnits(NumberAxis
							.createIntegerTickUnits());
				} else {
					xname = "Time(" + timebox.getSelectedItem()
							+ "s) since beginning of the process";
					data = getHistrogrammedDataAttributes(els,
							((Integer) BSpinner.getValue()) * timesize,
							timesize);
					mychart = ChartFactory.createXYLineChart("Average values",
							xname, "attribute value", data,
							PlotOrientation.VERTICAL, true, true, false);
					mychart.setBackgroundPaint(Color.white);
					XYPlot plot = mychart.getXYPlot();

					plot.setBackgroundPaint(Color.white);
					plot.setDomainGridlinePaint(Color.white);
					plot.setRangeGridlinePaint(Color.white);

					DeviationRenderer renderer = new DeviationRenderer(true,
							true);
					renderer.setSeriesStroke(0, new BasicStroke(3.0f,
							BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
					renderer.setSeriesStroke(0, new BasicStroke(2.0f));
					renderer.setSeriesStroke(1, new BasicStroke(2.0f));
					renderer.setSeriesStroke(2, new BasicStroke(2.0f));
					renderer.setSeriesStroke(3, new BasicStroke(2.0f));
					renderer.setSeriesFillPaint(0, Color.red);
					renderer.setSeriesFillPaint(1, Color.blue);
					renderer.setSeriesFillPaint(2, Color.green);
					renderer.setSeriesFillPaint(3, Color.orange);
					plot.setRenderer(renderer);
					// change the auto tick unit selection to integer units
					// only...
					NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
					// yAxis.setAutoRangeIncludesZero(false);
					yAxis.setStandardTickUnits(NumberAxis
							.createIntegerTickUnits());
				}

				ChartPanel mychartpanel = new ChartPanel(mychart);
				mychartpanel.setBackground(Color.white);
				chartPanel.removeAll();
				chartPanel.add(mychartpanel);
				chartPanel.updateUI();
			};
		});
		optionsPanel.add(updatebutton);

		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				optionsPanel, chartPanel);

		SpringUtils.makeCompactGrid(optionsPanel, 10, 1, // rows, cols
				6, 2, // initX, initY
				6, 2); // xPad, yPad

		mainPanel.add(splitPanel, BorderLayout.CENTER);
		return mainPanel;
	}

	/**
	 * 
	 * @return String[] with all unique attributes on element level
	 */
	private String[] getAttributes() {
		HashSet<String> results = new HashSet();
		for (ProcessInstance pi : mylog.getInstances()) {
			for (AuditTrailEntry ate : pi.getListOfATEs()) {
				results.addAll(ate.getAttributes().keySet());
			}
		}
		String[] t = new String[results.size()];
		return results.toArray(t);
	}

	private long timediff(Date start, Date end) {
		return end.getTime() - start.getTime();
	}

	/**
	 * precondition : 'attribute' MUST contain number values only
	 * 
	 * @param cases
	 * @param attribute
	 * @return DefaultXYDataset with all values of 'attribute' against event
	 *         numbers or timestamps.
	 */
	private XYSeriesCollection getDataAttributes(String[] attributes,
			boolean byTime, double timesize) {
		XYSeriesCollection result = new XYSeriesCollection();
		for (int index = 0; index < attributes.length; index++) {
			String attribute = attributes[index];
			Integer i = 0;
			XYSeries row = new XYSeries(attribute);
			for (ProcessInstance pi : mylog.getInstances()) {
				Integer x = 0;// count event number
				Date begin = pi.getAuditTrailEntries().first().getTimestamp();// starting
				// time
				// of
				// process
				// instance

				for (AuditTrailEntry ate : pi.getListOfATEs()) {
					if (ate.getAttributes().containsKey(attribute)) {
						Double val;
						val = Double
								.valueOf(ate.getAttributes().get(attribute));
						if (byTime) {
							row.add(timediff(begin, ate.getTimestamp())
									/ timesize, val.doubleValue());
						} else {
							row.add(x.doubleValue(), val.doubleValue());
						}
					}
					x++;// event number in case
				}
				i++;// case number
			}
			result.addSeries(row);
		}
		return result;
	}

	private IntervalXYDataset getHistrogrammedDataAttributes(
			String[] attributes, long barsize, long timesize) {
		IntervalXYDataset dataset = null;
		if (no_intervals) {
			dataset = new XYSeriesCollection();
		} else {
			dataset = new YIntervalSeriesCollection();
		}
		for (int index = 0; index < attributes.length; index++) {
			Histogram histogram = new Histogram(barsize);
			String attribute = attributes[index];
			for (ProcessInstance pi : mylog.getInstances()) {
				Date begin;
				try {
					begin = pi.getAuditTrailEntryList().get(0).getTimestamp();
				} catch (Exception e) {
					Message.add(e.getMessage(), Message.ERROR);
					return null;
				}// starting time of process instance
				int j = 0;
				for (AuditTrailEntry ate : pi.getListOfATEs()) {
					if (ate.getAttributes().containsKey(attribute)) {
						Double val;
						val = Double
								.valueOf(ate.getAttributes().get(attribute));
						if (xbox.getSelectedIndex() == 2) {
							histogram.addValue(j, val);
						}
						if (xbox.getSelectedIndex() == 3) {
							histogram.addValue(timediff(begin, ate
									.getTimestamp()), val);
						}
						j++;
					}
				}
			}
			if (no_intervals) {
				((XYSeriesCollection) dataset).addSeries(histogram.getXYSeries(
						attribute, timesize));
			} else {
				((YIntervalSeriesCollection) dataset).addSeries(histogram
						.getYIntervalSeries(attribute, timesize));
			}
		}
		return dataset;
	}

	private class Histogram {
		private HashMap<Integer, Double> ysum;
		private HashMap<Integer, Integer> count;
		private HashMap<Integer, Long> xsum;
		private HashMap<Integer, Vector<Double>> values;
		private long barsize;

		public Histogram(long size) {
			barsize = size;
			ysum = new HashMap<Integer, Double>();
			xsum = new HashMap<Integer, Long>();
			count = new HashMap<Integer, Integer>();
			values = new HashMap<Integer, Vector<Double>>();
		}

		public void addValue(long x, double y) {
			// calculate the bar in which this value should go
			int bar = (int) (x / barsize);

			double Y = y;
			if (ysum.containsKey(bar)) {

				double newyvalue = Y + ysum.get(bar);
				long newxvalue = x + xsum.get(bar);

				Integer newcount = count.get(bar) + 1;

				ysum.put(bar, newyvalue);
				xsum.put(bar, newxvalue);
				count.put(bar, newcount);
				Vector<Double> barvalues = values.get(bar);
				barvalues.add(Y);
				values.put(bar, barvalues);

			} else {
				ysum.put(bar, Y);
				xsum.put(bar, x);
				count.put(bar, 1);
				Vector<Double> barvalues = new Vector<Double>();
				barvalues.add(Y);
				values.put(bar, barvalues);
			}

		}

		public YIntervalSeries getYIntervalSeries(String rowname, long factor) {
			YIntervalSeries mydataset = new YIntervalSeries(rowname);
			double sdcount = 0;
			double total = 0;
			Vector<Double> totalvalues = new Vector<Double>();
			double avgtotal = 0;
			double minus = 0;
			double plus = 0;
			double zero = 0;
			for (Integer key : ysum.keySet()) {
				Double value = ysum.get(key) / (double) count.get(key);
				Double point = (double) xsum.get(key) / (double) count.get(key);
				Vector<Double> listofvalues = values.get(key);
				double sumofdiff = 0.0;
				for (Double onevalue : listofvalues) {
					sumofdiff += Math.pow(onevalue - value, 2);
					sdcount++;
					total += Math.pow(onevalue, 2);
					avgtotal += onevalue;
					totalvalues.add(onevalue);
					if (onevalue == 1) {
						plus++;
					}
					;
					if (onevalue == -1) {
						minus++;
					}
					;
					if (onevalue == 0) {
						zero++;
					}
					;
				}
				double sd = Math.sqrt(sumofdiff / count.get(key));
				// mydataset.add(point/factor, value,value+sd,value-sd);
				// mydataset.add(point/factor, value,value,value);
				mydataset.add(point / factor, value, value + 1.96
						* (sd / Math.sqrt(count.get(key))), value - 1.96
						* (sd / Math.sqrt(count.get(key))));
			}
			double sdtotal = 0;
			double avgsd = total / sdcount;
			double test = 0;
			for (Double onevalue : totalvalues) {
				sdtotal += Math.pow(Math.pow(onevalue, 2) - (total / sdcount),
						2);
				test += onevalue;
			}
			// System.out.println(rowname+" mean square: "+avgsd+" +/-95%:"+1.96*Math.sqrt(sdtotal/sdcount)/Math.sqrt(sdcount));
			// System.out.println("total -1:"+minus+" total +1:"+plus+" zero: "+zero
			// +" total:"+sdcount);
			return mydataset;
		}

		public XYSeries getXYSeries(String rowname, long factor) {
			XYSeries mydataset = new XYSeries(rowname);
			for (Integer key : ysum.keySet()) {
				Double value = ysum.get(key) / (double) count.get(key);
				Double point = (double) xsum.get(key) / (double) count.get(key);
				mydataset.add(point / factor, value);

			}
			return mydataset;
		}
	}
}
