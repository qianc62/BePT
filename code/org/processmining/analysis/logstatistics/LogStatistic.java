package org.processmining.analysis.logstatistics;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.processmining.analysis.logstatistics.OptionPanel.GraphViewProperties;
import org.processmining.analysis.logstatistics.OptionPanel.ResultConfiguration;
import org.processmining.analysis.logstatistics.OptionPanel.ResultType;
import org.processmining.analysis.logstatistics.OptionPanel.TextViewProperties;
import org.processmining.analysis.logstatistics.OptionPanel.ViewMode;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution;
import org.processmining.framework.ui.MainUI;

/**
 * Provides some simple log statistics related to the duration of and the
 * distance between activities.
 * 
 * @author anne
 */
public class LogStatistic extends JPanel {

	private static final long serialVersionUID = -5762586193646797431L;

	// HTML styles
	private static final String H1 = "h1";
	private static final String BOLD = "b";
	private static final String PAR = "p";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

	/**
	 * Keeps the statistics for the duration of each activity. This means the
	 * time that has passed from the Start to the Complete of it. [key =
	 * activity name, value = SummaryStatistics object]
	 */
	private HashMap<String, SummaryStatistics> myActivityDurationStatistics = new HashMap<String, SummaryStatistics>();

	/**
	 * Keeps all values for the activity durations, including model references
	 * of the activities.
	 */
	private HashMap<String, List<ValueWithModelReferences>> myActivityDurationValues = new HashMap<String, List<ValueWithModelReferences>>();

	/**
	 * Keeps the statistics about all the time stamps, so that Min and Max
	 * correspond to the earliest and the latest timestamp respectively.
	 */
	private SummaryStatistics myTimestamps = SummaryStatistics.newInstance();

	/**
	 * Keeps the statistics for the distance between subsequent activities. This
	 * means the time that has passed from a Complete event to the next Start
	 * event.
	 */
	private SummaryStatistics myActivityDistanceStatistics = SummaryStatistics
			.newInstance();

	/**
	 * The name of the entity for which this statistic is collected (to be
	 * included in the HTML representation that is generated).
	 */
	private String myName;

	/**
	 * Pointer to the overall display settings (such as whether HTML or
	 * Graphical view should be displayed etc).
	 */
	private OptionPanel myDisplayOptions;

	/**
	 * The model references of this item.
	 */
	private Collection<String> myModelReferences;

	private String[] keys; // used to store the sorted keys (in order to avoid
	// resorting all the time)
	private JFreeChart chart; // the bar chart object
	private JTextPane myTextPane; // the HTML pane object

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            the name to be included in the HTML representation of this
	 *            statistic
	 */
	public LogStatistic(String name, Collection<String> modelReferences,
			OptionPanel displayOptions) {
		myName = name;
		myModelReferences = modelReferences;
		myDisplayOptions = displayOptions;
	}

	/**
	 * Initializes this log statistic based on the given set of activity names. <br>
	 * Needed to ensure that both global and instance statistics refer to the
	 * same set of activities as it may be that not every process instance
	 * executed all activities. In this case, different colors would be
	 * generated in the graphical view, which is confusing for the user.
	 * 
	 * @param modelElements
	 *            the model elements that were found in the log
	 */
	public void initialize(String[] modelElements) {
		for (int i = 0; i < modelElements.length; i++) {
			SummaryStatistics stat = SummaryStatistics.newInstance();
			stat.addValue(0);
			myActivityDurationStatistics.put(modelElements[i], stat);
		}
	}

	/**
	 * Adds a new value to the duration statistics for the given activity. <br>
	 * Note that the value itself will be not kept in memory and therefore some
	 * statistical methods such as calculating the Median are not possible
	 * afterwards.
	 * 
	 * @param activity
	 *            the activity for which the new value is provided
	 * @param value
	 *            the new measurement that is provided
	 */
	public void addActivityDurationValue(String activity,
			Collection<String> elementModelReferences,
			Collection<String> originatorModelReferences, long value) {
		// get the corresponding statistics object
		SummaryStatistics durationValues = (SummaryStatistics) myActivityDurationStatistics
				.get(activity);
		// if activity has not been measured yet, create a new entry
		if (durationValues == null) {
			durationValues = SummaryStatistics.newInstance();
		}
		// add the new value
		durationValues.addValue(value);
		myActivityDurationStatistics.put(activity, durationValues);

		if ((elementModelReferences != null && elementModelReferences.size() > 0)
				|| (originatorModelReferences != null && originatorModelReferences
						.size() > 0)) {
			List<ValueWithModelReferences> valuesWithMR = myActivityDurationValues
					.get(activity);
			if (valuesWithMR == null) {
				valuesWithMR = new ArrayList<ValueWithModelReferences>();
				myActivityDurationValues.put(activity, valuesWithMR);
			}
			valuesWithMR.add(new ValueWithModelReferences(value,
					elementModelReferences, originatorModelReferences));
		}
	}

	/**
	 * Adds a new value to the overall activity distance statistics. <br>
	 * Note that the value itself will be not kept in memory and therefore some
	 * statistical methods such as calculating the Median are not possible
	 * afterwards.
	 * 
	 * @param value
	 *            the new measurement that is provided
	 */
	public void addActivityDistanceValue(long value) {
		myActivityDistanceStatistics.addValue(value);
	}

	/**
	 * Adds a new value to the overall timestamp statistics. <br>
	 * Note that the value itself will be not kept in memory and therefore some
	 * statistical methods such as calculating the Median are not possible
	 * afterwards.
	 * 
	 * @param value
	 *            the new measurement that is provided
	 */
	public void addTimestampValue(long value) {
		myTimestamps.addValue(value);
	}

	// /////////////////////////////////// READ METHODS
	// ////////////////////////////////

	/**
	 * Retrieves the general time stamp statistics for this log statistic.
	 * 
	 * @return the time stamp statistics (earliest timetamp is min(), latest is
	 *         max() etc.)
	 */
	public SummaryStatistics getTimeStampStatistics() {
		return myTimestamps;
	}

	/**
	 * Retrieves all the activity duration statistics for this log statistic.
	 * 
	 * @return the activity duration statistics
	 */
	public HashMap<String, SummaryStatistics> getActivityDurations() {
		return myActivityDurationStatistics;
	}

	/**
	 * Retrieves all the activity duration statistics for this log statistic.
	 * 
	 * @return the activity duration statistics
	 */
	public Map<String, List<ValueWithModelReferences>> getActivityDurationsWithModelReferences() {
		return myActivityDurationValues;
	}

	/**
	 * Returns a distribution object that represents the given statistics.
	 * 
	 * @param statistc
	 *            the statistics object that shall be represented as a
	 *            distribution
	 * @return the distribution object
	 */
	public static HLDistribution getDistributionObject(
			SummaryStatistics statistic) {
		HLGeneralDistribution result = new HLGeneralDistribution(statistic
				.getMean(), // constant
				statistic.getMean(), // mean
				statistic.getVariance(), // variance
				statistic.getMin(), // min
				statistic.getMax(), // max
				0.0, // intensity?
				1.0, // probability?
				(int) statistic.getN(), // no. of experiments
				1, // degrees freedom?
				1 // emergence n events?
		);
		result
				.setBestDistributionType(HLDistribution.DistributionEnum.NORMAL_DISTRIBUTION); // best
		// distribution)
		return result;
	}

	/**
	 * Delivers the basic statistic values for the given statistic object in the
	 * requested result type (e.g., minimum or sum value).
	 * 
	 * @param type
	 *            the type of result value that is requested
	 * @param statistic
	 *            the statistic object from which the value is requested
	 * @return the requensted distance measurement value
	 */
	public static double getStatisticValue(SummaryStatistics statistic,
			ResultType type) {
		if (type == ResultType.MIN) {
			return statistic.getMin();
		} else if (type == ResultType.MAX) {
			return statistic.getMax();
		} else if (type == ResultType.ARITHMETIC_MEAN) {
			return statistic.getMean();
		} else if (type == ResultType.VARIANCE) {
			return statistic.getVariance();
		} else if (type == ResultType.STANDARD_DEVIATION) {
			return statistic.getStandardDeviation();
		} else if (type == ResultType.GEOMETRIC_MEAN) {
			return statistic.getGeometricMean();
		} else if (type == ResultType.SUM) {
			return statistic.getSum();
		} else if (type == ResultType.NO_MEASUREMENTS) {
			// TODO : check how to deal with different return type!!
			return statistic.getN();
		} else {
			// "Activity" value must not be requested
			return -1;
		}
	}

	/**
	 * Dispatches the creation of the correct view mode properties.
	 * 
	 * @param properties
	 *            the current view mode properties
	 * @return the panel displaying the results according to the view mode
	 *         properties
	 */
	public JPanel getResultPanel(ViewMode properties) {
		if (properties instanceof TextViewProperties) {
			return this.getResultPanel((TextViewProperties) properties);
		} else if (properties instanceof GraphViewProperties) {
			return this.getResultPanel((GraphViewProperties) properties);
		} else {
			// TODO: throw exception
			return null;
		}
	}

	/**
	 * Delivers a HTML representation of the statistic results according to the
	 * current display settings. <br>
	 * The reason for displaying HTML is that it can easily be copied and pasted
	 * from the framework to, e.g., a word processing program.
	 * 
	 * @param properties
	 *            the current display settings
	 * @return the panel containing the text pane and the "save as HTML" button
	 */
	public JPanel getResultPanel(TextViewProperties properties) {
		// by which result value the text view should be ordered
		ResultType orderByType = properties.getValue();
		ArrayList<ResultType> selectedTypes = new ArrayList<ResultType>();
		// check which result values should be included
		for (ResultType result : ResultType.values()) {
			ResultConfiguration typeConfiguration = myDisplayOptions
					.getProperty(result);
			if (typeConfiguration != null && typeConfiguration.isEnabled()) {
				selectedTypes.add(result);
			}
		}

		// fill the text pane
		myTextPane = new JTextPane();
		myTextPane.setContentType("text/html");
		myTextPane.setText(this.getHtmlRepresentation(orderByType,
				selectedTypes));
		myTextPane.setEditable(false);
		myTextPane.setCaretPosition(0);

		JPanel result = new JPanel();
		result.setLayout(new BoxLayout(result, BoxLayout.PAGE_AXIS));
		result.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		result.add(new JScrollPane(myTextPane), BorderLayout.CENTER);

		// add a panel with button to allow saving the log summary to
		// a CSV and a HTML file
		final ResultType tempOrderByType = orderByType;
		final ArrayList<ResultType> tempselectedTypes = selectedTypes;
		JButton saveCSVButton = new JButton("Save as CSV");
		saveCSVButton
				.setToolTipText("Saves the global log statistics as CSV file");
		saveCSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// actually save to file
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("ProM_LogStatistics.csv"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(getCsvRepresentation(tempOrderByType,
								tempselectedTypes));
						outWriter.flush();
						outWriter.close();
						JOptionPane.showMessageDialog(MainUI.getInstance(),
								"Log statistics have been saved\nto CSV file!",
								"Log statistics saved.",
								JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JButton saveHTMLButton = new JButton("Save as HTML");
		saveHTMLButton
				.setToolTipText("Saves the global log statistics as HTML file");
		saveHTMLButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// actually save to file
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("ProM_LogStatistics.html"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(myTextPane.getText());
						outWriter.flush();
						outWriter.close();
						JOptionPane
								.showMessageDialog(
										MainUI.getInstance(),
										"Log statistics have been saved\nto HTML file!",
										"Log statistics saved.",
										JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		JPanel savePanel = new JPanel();
		savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.X_AXIS));
		savePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
		savePanel.add(Box.createHorizontalGlue());
		savePanel.add(saveCSVButton);
		savePanel.add(Box.createHorizontalStrut(5));
		savePanel.add(saveHTMLButton);
		result.add(savePanel, BorderLayout.SOUTH);

		return result;
	}

	/**
	 * Displays the results as a bar chart according to the current display
	 * settings.
	 * 
	 * @param properties
	 *            the current graphical display settings
	 * @return the panel containing the chart and the "save as PNG" button
	 */
	public JPanel getResultPanel(GraphViewProperties properties) {
		long earliest = (long) myTimestamps.getMin();
		long latest = (long) myTimestamps.getMax();
		long spannedDuration = latest - earliest;
		spannedDuration = spannedDuration / 1000; // (from miliseconds) to
		// seconds
		spannedDuration = spannedDuration / 60; // to min
		spannedDuration = spannedDuration / 60; // to hours

		// the result type to be displayed (such as mean or sum of values)
		ResultType type = properties.getValue();
		// the configuration of this type (which time unit)
		ResultConfiguration typeConfiguration = myDisplayOptions
				.getProperty(type);

		JPanel result = new JPanel(new BorderLayout());
		result.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		// The panel containing the buttons to export the visual representation
		// as a JPEG or PNG.
		JPanel saveGraphPanel = new JPanel();
		saveGraphPanel
				.setLayout(new BoxLayout(saveGraphPanel, BoxLayout.X_AXIS));
		// The "save as .." buttons themselves
		JButton saveGraphToPNG = new JButton("Save as PNG");
		saveGraphToPNG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsPNG(chart);
			}
		});

		saveGraphPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 10));
		saveGraphPanel.add(Box.createHorizontalGlue());
		saveGraphPanel.add(saveGraphToPNG);

		// Ordering the keys for the X axis (i.e., activities)
		keys = new String[myActivityDurationStatistics.size()];
		Iterator iterator = myActivityDurationStatistics.keySet().iterator();
		for (int i = 0; i < myActivityDurationStatistics.size(); i++) {
			keys[i] = (String) iterator.next();
		}
		Arrays.sort(keys);

		// adding the measurements to the data set
		for (int i = 0; i < keys.length; i++) {
			// get statistics
			SummaryStatistics statistic = (SummaryStatistics) myActivityDurationStatistics
					.get(keys[i]);
			// add to data set for chart
			double duration = getStatisticValue(statistic, type);
			// check if conversion is necessary
			if (typeConfiguration.hasTimeUnit()) {
				int conversionValue = typeConfiguration.getTimeUnit()
						.getConversionValue();
				dataset.addValue(duration / conversionValue, keys[i], keys[i]);
			} else {
				dataset.addValue(duration, keys[i], keys[i]);
			}
		}

		// create the Y axis label
		StringBuffer label = new StringBuffer();
		label.append(type.toString());
		label.append(" of Activity Durations ");
		if (typeConfiguration.hasTimeUnit()) {
			label.append("(in ");
			label.append(typeConfiguration.getTimeUnit().toString());
			label.append(")");
			// add spanned time as reference point
			label.append(" - Spanned Time: ");
			label.append((spannedDuration / 24) + " days");
			label.append(" (" + spannedDuration + " hours)");
		}

		chart = ChartFactory.createStackedBarChart3D("", // chart title
				"Activity", // domain axis label
				label.toString(), // range axis label
				dataset, // data
				PlotOrientation.HORIZONTAL, // orientation
				false, // include legend
				true, // tooltips?
				false // URLs?
				);
		chart.getCategoryPlot().getRenderer().setItemLabelGenerator(
				new CategoryItemLabelGenerator() {

					public String generateColumnLabel(CategoryDataset dataset,
							int column) {
						return null;
					}

					public String generateLabel(CategoryDataset dataset,
							int row, int column) {
						// keys array is still sorted
						for (int i = 0; i < keys.length; i++) {
							if (row == 0) {
								// return name of activity
								return keys[i];
							}
							row--;
						}
						return null;
					}

					public String generateRowLabel(CategoryDataset dataset,
							int row) {
						return null;
					}
				});
		chart.getCategoryPlot().getRenderer().setItemLabelsVisible(true);
		setCategoryChartProperties(chart);
		ChartPanel chartPanel = new ChartPanel(chart, false);
		result.add(new JScrollPane(chartPanel), BorderLayout.CENTER);
		result.add(saveGraphPanel, BorderLayout.SOUTH);

		return result;
	}

	/**
	 * Delivers a HTML representation of the statistic results. The reason for
	 * displaying HTML is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param by
	 *            which result type the table should be ordered
	 * @param which
	 *            result types shall be included in the overview
	 * @return the String containing a HTML representation of the results
	 */
	private String getHtmlRepresentation(ResultType orderByType,
			ArrayList<ResultType> selectedTypes) {

		long earliest = (long) myTimestamps.getMin();
		long latest = (long) myTimestamps.getMax();
		long duration = latest - earliest;
		duration = duration / 1000; // (from miliseconds) to seconds
		duration = duration / 60; // to min
		duration = duration / 60; // to hours
		Date earliestDate = new Date(earliest);
		Date latestDate = new Date(latest);

		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("General Statistics of " + myName, H1));
		sb.append(tag(tag("Earliest timestamp: ", BOLD)
				+ earliestDate.toString(), PAR));
		sb.append(tag(tag("Latest timestamp: ", BOLD) + latestDate.toString(),
				PAR));
		sb.append(tag(tag("Overall duration: ", BOLD) + duration + " hours ("
				+ (duration / 24) + " days)", PAR));

		// duration statistics
		sb.append(tag("Activity Duration Statistics of " + myName, H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("Activity", TH)); // add activity column header
		String timeUnit;
		for (ResultType type : selectedTypes) {
			ResultConfiguration conf = myDisplayOptions.getProperty(type);
			if (conf.hasTimeUnit()) {
				timeUnit = " (in " + conf.getTimeUnit().toString() + ")";
			} else {
				timeUnit = "";
			}
			tableHeader.append(tag(type.toString() + timeUnit, TH)); // current
			// result
			// type
			// column
			// header
		}
		sb.append(tag(tableHeader.toString(), TR));

		// write duration statistics table contents
		sb.append(convertToOrderedString(myActivityDurationStatistics,
				orderByType, selectedTypes));
		sb.append("</table>");

		// distance statistics
		sb.append(tag("Activity Distance Statistics of " + myName, H1));
		timeUnit = "";
		int conversionValue = 1;
		for (ResultType type : selectedTypes) {
			ResultConfiguration conf = myDisplayOptions.getProperty(type);
			if (conf.hasTimeUnit()) {
				timeUnit = " " + conf.getTimeUnit().toString();
				conversionValue = conf.getTimeUnit().getConversionValue();
			} else {
				timeUnit = "";
				conversionValue = 1;
			}
			// current result type for activity Distance statistics
			sb.append(tag(
					tag(type.toString() + ": ", BOLD)
							+ (getStatisticValue(myActivityDistanceStatistics,
									type) / conversionValue) + timeUnit, PAR));
		}

		sb.append("</body></html>");
		return sb.toString();
	}

	/**
	 * Delivers a CSV representation of the statistic results. The reason for
	 * displaying CSV is that it can easily be copied and pasted from the
	 * framework to, e.g., a word processing program.
	 * 
	 * @param by
	 *            which result type the table should be ordered
	 * @param which
	 *            result types shall be included in the overview
	 * @return the String containing a CSV representation of the results
	 */
	private String getCsvRepresentation(ResultType orderByType,
			ArrayList<ResultType> selectedTypes) {

		long earliest = (long) myTimestamps.getMin();
		long latest = (long) myTimestamps.getMax();
		long duration = latest - earliest;
		duration = duration / 1000; // (from miliseconds) to seconds
		duration = duration / 60; // to min
		duration = duration / 60; // to hours
		Date earliestDate = new Date(earliest);
		Date latestDate = new Date(latest);

		// distance statistics
		StringBuffer sb = new StringBuffer();
		sb.append("General Statistics of " + myName + "\n\n");
		sb.append("Earliest timestamp: " + earliestDate.toString() + "\n");
		sb.append("Latest timestamp: " + latestDate.toString() + "\n");
		sb.append("Overall duration: " + duration + " hours ("
				+ (duration / 24) + " days)" + "\n\n");

		// duration statistics
		sb.append("Activity Duration Statistics of " + myName + "\n\n");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append("Activity" + ","); // add activity column header
		String timeUnit;
		for (ResultType type : selectedTypes) {
			ResultConfiguration conf = myDisplayOptions.getProperty(type);
			if (conf.hasTimeUnit()) {
				timeUnit = " (in " + conf.getTimeUnit().toString() + ")";
			} else {
				timeUnit = "";
			}
			tableHeader.append(type.toString() + timeUnit + ","); // current
			// result
			// type
			// column
			// header
		}
		sb.append(tableHeader.toString() + "\n");

		// write duration statistics table contents
		sb.append(convertToCSVOrderedString(myActivityDurationStatistics,
				orderByType, selectedTypes));

		// distance statistics
		sb.append("Activity Distance Statistics of " + myName + "\n");
		timeUnit = "";
		int conversionValue = 1;
		for (ResultType type : selectedTypes) {
			ResultConfiguration conf = myDisplayOptions.getProperty(type);
			if (conf.hasTimeUnit()) {
				timeUnit = " " + conf.getTimeUnit().toString();
				conversionValue = conf.getTimeUnit().getConversionValue();
			} else {
				timeUnit = "";
				conversionValue = 1;
			}
			// current result type for activity Distance statistics
			sb
					.append(type.toString()
							+ ": "
							+ (getStatisticValue(myActivityDistanceStatistics,
									type) / conversionValue) + timeUnit + "\n");
		}

		return sb.toString();
	}

	private void setCategoryChartProperties(JFreeChart chart) {
		chart.setBackgroundPaint(Color.white);
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setDomainGridlinePaint(Color.black);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinePaint(Color.black);
		final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
	}

	/**
	 * Saves the given chart as a PNG file.
	 * 
	 * @param chart
	 *            the chart to be saved
	 */
	public void saveAsPNG(JFreeChart chart) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File arg0) {
				return arg0.getAbsolutePath().endsWith("png")
						|| arg0.isDirectory();
			}

			@Override
			public String getDescription() {
				return "PNG files";
			}

		});
		chooser.showSaveDialog(this);
		File file = chooser.getSelectedFile();
		if (file != null) {
			try {
				ChartUtilities.saveChartAsPNG(file, chart, 1024, 768);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public Collection<String> getModelReferences() {
		return myModelReferences;
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
	private String convertToOrderedString(HashMap hash, ResultType orderByType,
			ArrayList<ResultType> selectedTypes) {

		StringBuffer string = new StringBuffer();

		// Ordering the keys.
		String[] keys = new String[hash.size()];

		if (orderByType == ResultType.ACTIVITY) {
			Iterator iterator = hash.keySet().iterator();
			for (int i = 0; i < hash.size(); i++) {
				keys[i] = (String) iterator.next();
			}
			Arrays.sort(keys);
		} else {
			OrderByHelperClass[] namesAndValues = new OrderByHelperClass[hash
					.size()];
			Iterator iterator = hash.keySet().iterator();
			String name;
			double value;
			for (int i = 0; i < hash.size(); i++) {
				name = (String) iterator.next();
				// get statistics
				SummaryStatistics statistic = (SummaryStatistics) hash
						.get(name);
				// add to data set for chart
				value = getStatisticValue(statistic, orderByType);
				namesAndValues[i] = new OrderByHelperClass(name, value);
			}
			Arrays.sort(namesAndValues);

			// now put the activity names in the right order
			for (int i = 0; i < namesAndValues.length; i++) {
				keys[i] = namesAndValues[i].getName();
			}
		}

		// Building the ordered string representation.
		for (int i = 0; i < keys.length; i++) {
			// get statistics
			SummaryStatistics statistic = (SummaryStatistics) hash.get(keys[i]);

			StringBuffer rowContent = new StringBuffer();
			rowContent.append(tag(keys[i] + "", TD)); // add activity column
			// content
			int conversionValue = 1;
			for (ResultType type : selectedTypes) {
				ResultConfiguration conf = myDisplayOptions.getProperty(type);
				if (conf.hasTimeUnit()) {
					conversionValue = conf.getTimeUnit().getConversionValue();
				} else {
					conversionValue = 1;
				}
				rowContent.append(tag(
						(getStatisticValue(statistic, type) / conversionValue)
								+ "", TD)); // current result type column
				// content
			}
			string.append(tag(rowContent.toString(), TR));
		}
		return string.toString();
	}

	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private String convertToCSVOrderedString(HashMap hash,
			ResultType orderByType, ArrayList<ResultType> selectedTypes) {

		StringBuffer string = new StringBuffer();

		// Ordering the keys.
		String[] keys = new String[hash.size()];

		if (orderByType == ResultType.ACTIVITY) {
			Iterator iterator = hash.keySet().iterator();
			for (int i = 0; i < hash.size(); i++) {
				keys[i] = (String) iterator.next();
			}
			Arrays.sort(keys);
		} else {
			OrderByHelperClass[] namesAndValues = new OrderByHelperClass[hash
					.size()];
			Iterator iterator = hash.keySet().iterator();
			String name;
			double value;
			for (int i = 0; i < hash.size(); i++) {
				name = (String) iterator.next();
				// get statistics
				SummaryStatistics statistic = (SummaryStatistics) hash
						.get(name);
				// add to data set for chart
				value = getStatisticValue(statistic, orderByType);
				namesAndValues[i] = new OrderByHelperClass(name, value);
			}
			Arrays.sort(namesAndValues);

			// now put the activity names in the right order
			for (int i = 0; i < namesAndValues.length; i++) {
				keys[i] = namesAndValues[i].getName();
			}
		}

		// Building the ordered string representation.
		for (int i = 0; i < keys.length; i++) {
			// get statistics
			SummaryStatistics statistic = (SummaryStatistics) hash.get(keys[i]);

			StringBuffer rowContent = new StringBuffer();
			rowContent.append(keys[i] + ","); // add activity column content
			int conversionValue = 1;
			for (ResultType type : selectedTypes) {
				ResultConfiguration conf = myDisplayOptions.getProperty(type);
				if (conf.hasTimeUnit()) {
					conversionValue = conf.getTimeUnit().getConversionValue();
				} else {
					conversionValue = 1;
				}
				rowContent
						.append((getStatisticValue(statistic, type) / conversionValue)
								+ ","); // current result type column content
			}
			string.append(rowContent.toString() + "\n");
		}

		string.append("\n");
		return string.toString();
	}

	/**
	 * Helps to order the activities by a certain result type while keeping the
	 * link to the activity itself.
	 * 
	 * @author anne
	 */
	class OrderByHelperClass implements Comparable {

		private String myActivityName;
		private double myResultValue;

		public OrderByHelperClass(String name, double value) {
			myActivityName = name;
			myResultValue = value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		public int compareTo(Object obj) {
			double comparedValue = ((OrderByHelperClass) obj).myResultValue;
			if (myResultValue > comparedValue) {
				return -1;
			} else if (myResultValue == comparedValue) {
				return 0;
			} else {
				return 1;
			}
		}

		public String getName() {
			return myActivityName;
		}
	}
}
