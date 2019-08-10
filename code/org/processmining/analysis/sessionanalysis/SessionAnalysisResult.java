package org.processmining.analysis.sessionanalysis;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
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
import org.processmining.analysis.AnalysisPlugin;
import org.processmining.analysis.sessionanalysis.SessionOptions.DisplayType;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.models.hlprocess.HLTypes.TimeUnit;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.slicker.ProgressPanel;
import org.processmining.framework.util.GUIPropertyDoubleTextField;
import org.processmining.framework.util.GUIPropertyListEnumeration;
import org.processmining.framework.util.GuiNotificationTarget;
import org.processmining.framework.util.GuiUtilities;
import org.processmining.framework.util.RuntimeUtils;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

public class SessionAnalysisResult extends JPanel implements Provider,
		GuiNotificationTarget {

	protected LogReader inputLog;
	protected LogReader sessionLog;
	protected AnalysisPlugin myAlgorithm;

	protected JComponent view;
	protected JPanel confPanel;
	protected JPanel resultPanel;
	protected ProgressPanel progress;
	protected JFreeChart chart;

	protected HashMap<String, SummaryStatistics> sessionStatistics = new HashMap<String, SummaryStatistics>();
	protected SessionOptions myDisplayOptions = new SessionOptions(this);
	protected String[] keys;

	protected GUIPropertyListEnumeration timeUnitProperty = new GUIPropertyListEnumeration(
			"Time unit: ", "Determine time unit to be used for analysis",
			Arrays.asList(TimeUnit.values()), null, 200);
	protected GUIPropertyDoubleTextField timeoutProperty = new GUIPropertyDoubleTextField(
			"Timeout (to split sessions): ",
			"Time out to be applied to split up sessions (interpreted in the given time unit)",
			1, null, 200);

	/**
	 * Default constructor.
	 * 
	 * @param log
	 *            the log to be analyzed for sessions
	 * @param algorithm
	 *            the SessionAnalysisPlugin (for displaying documentation)
	 */
	public SessionAnalysisResult(LogReader log, AnalysisPlugin algorithm) {
		inputLog = log;
		myAlgorithm = algorithm;
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(80, 80, 80));
		HeaderBar header = new HeaderBar("Session Analysis");
		this.add(header, BorderLayout.NORTH);
		progress = new ProgressPanel("Analyzing...");
		confPanel = constructConfigurationPanel();
		resultPanel = null;
		setView(confPanel);
		timeUnitProperty.setValue(TimeUnit.HOURS);
		timeoutProperty.setValue(1.0);
	}

	/**
	 * Updates the view by updating the content of the window by the given GUI
	 * component.
	 * 
	 * @param comp
	 *            the component to be displayed in the result frame.
	 */
	protected void setView(JComponent comp) {
		if (view != null) {
			this.remove(view);
		}
		view = comp;
		this.add(view, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {
		if (sessionLog != null) {
			ProvidedObject[] objects = {
					new ProvidedObject("Input Log", new Object[] { inputLog }),
					new ProvidedObject("Session Log",
							new Object[] { sessionLog }) };
			return objects;
		} else {
			ProvidedObject[] objects = { new ProvidedObject("Input Log",
					new Object[] { inputLog }) };
			return objects;
		}
	}

	/**
	 * Constructs configuration view of the plugin.
	 * 
	 * @return the configuration panel
	 */
	protected JPanel constructConfigurationPanel() {
		RoundedPanel confPanel = new RoundedPanel(10, 5, 5);
		confPanel.setBackground(new Color(140, 140, 140));
		confPanel.setLayout(new BoxLayout(confPanel, BoxLayout.Y_AXIS));
		JButton startButton = new AutoFocusButton("Start analysis");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			startButton.setOpaque(false);
		}
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				runAnalysis();
			}
		});
		JButton docsButton = new SlickerButton("Help...");
		docsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MainUI.getInstance().showReference(myAlgorithm);
			}
		});
		JPanel optPanel = new JPanel();
		optPanel.setOpaque(false);
		optPanel.setLayout(new BoxLayout(optPanel, BoxLayout.Y_AXIS));
		optPanel.add(timeUnitProperty.getPropertyPanel());
		optPanel.add(Box.createVerticalStrut(5));
		optPanel.add(timeoutProperty.getPropertyPanel());
		confPanel.add(GuiUtilities.packCenterHorizontally(GuiUtilities
				.packMiddleVertically(GuiUtilities
						.packCenterHorizontally(optPanel))));
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(10));
		packed.add(docsButton);
		packed.add(Box.createHorizontalGlue());
		packed.add(startButton);
		int height = (int) startButton.getMinimumSize().getHeight();
		packed.setMinimumSize(startButton.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		confPanel.add(packed);
		return confPanel;
	}

	/**
	 * Start analysis based on current "compactness" and "preciseness" measure
	 * and switch to result view.
	 */
	protected void runAnalysis() {
		setView(progress.getPanel());
		splitSessions();
		analyzeSessions();
		resultPanel = constructResultPanel();
		setView(resultPanel);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.util.GuiNotificationTarget#updateGUI()
	 */
	public void updateGUI() { // invoked when display type or time unit changes
		resultPanel = constructResultPanel();
		setView(resultPanel);
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void splitSessions() {
		try {
			TimeUnit timeUnit = (TimeUnit) timeUnitProperty.getValue();
			int conversionValue = timeUnit.getConversionValue();
			// TODO generalize conversion values (based on seconds right now)
			if (timeUnit != TimeUnit.MILLISECONDS) {
				conversionValue = conversionValue * 1000;
			}
			long timeoutInMilliseconds = (long) (timeoutProperty.getValue() * conversionValue);
			File outputFile = File.createTempFile("SessionAnalysisTemp",
					".mxml.gz");
			FileOutputStream output = new FileOutputStream(outputFile);
			BufferedOutputStream out = new BufferedOutputStream(
					new GZIPOutputStream(output));
			LogPersistencyStream persistency = new LogPersistencyStream(out,
					false);
			Process process = inputLog.getProcess(0);
			ProcessInstance instance = null;
			AuditTrailEntryList ateList = null;
			String name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}
			String description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}
			String source = inputLog.getLogSummary().getSource().getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}
			persistency.startLogfile(name, description, source);
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					name = instance.getName();
					if (name == null || name.length() == 0) {
						name = "UnnamedProcessInstance";
					}
					description = instance.getDescription();
					if (description == null || description.length() == 0) {
						description = name + " exported by MXMLib @ P-stable";
					}
					// one new process per process instance (potential previous
					// processes
					// will be lost)
					persistency.startProcess(name, description,
							new HashMap<String, String>());
					HashMap<String, String> sessionAtt = new HashMap<String, String>();
					sessionAtt.put("Origin", name);
					Date previousTimeStamp = null;
					int sessionCounter = 0;
					ateList = instance.getAuditTrailEntryList();
					for (int k = 0; k < ateList.size(); k++) {
						Date currentTimeStamp = ateList.get(k).getTimestamp();
						if (currentTimeStamp == null) {
							break;
						} else if (previousTimeStamp == null) { // first session
							persistency.startProcessInstance("Session"
									+ sessionCounter,
									"Session split up from intance " + name,
									sessionAtt);

						} else if (currentTimeStamp.getTime()
								- previousTimeStamp.getTime() > (timeoutInMilliseconds)) {
							persistency.endProcessInstance();
							sessionCounter++;
							persistency.startProcessInstance("Session"
									+ sessionCounter,
									"Session split up from intance " + name,
									sessionAtt);
						}
						persistency
								.addAuditTrailEntry(promAte2mxmlibAte(ateList
										.get(k)));
						previousTimeStamp = currentTimeStamp;
					}
					persistency.endProcessInstance(); // end last session
					// instance
					persistency.endProcess();
				}
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			sessionLog = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Helper method to convert the given log entry in an mxmllib log entry.
	 * 
	 * @param promAte
	 *            the log entry
	 * @return the mxml log entry
	 */
	protected org.processmining.lib.mxml.AuditTrailEntry promAte2mxmlibAte(
			AuditTrailEntry promAte) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(promAte.getElement());
		mxmlibAte.setEventType(EventType.getType(promAte.getType()));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

	/**
	 * Actually analyzes the sessions per process.
	 */
	protected void analyzeSessions() {
		if (sessionLog != null) {
			try {
				Process process = null;
				ProcessInstance instance = null;
				AuditTrailEntryList ateList = null;
				Date firstAte = null;
				Date lastAte = null;
				TimeUnit timeUnit = (TimeUnit) timeUnitProperty.getValue();
				for (int i = 0; i < sessionLog.numberOfProcesses(); i++) {
					process = sessionLog.getProcess(i);
					SummaryStatistics sessionTimes = SummaryStatistics
							.newInstance();
					for (int j = 0; j < process.size(); j++) {
						instance = process.getInstance(j);
						ateList = instance.getAuditTrailEntryList();
						firstAte = ateList.get(0).getTimestamp();
						lastAte = ateList.get(ateList.size() - 1)
								.getTimestamp();
						double sessionTime = (double) (lastAte.getTime() - firstAte
								.getTime());
						double conversionValue = 1.0;
						if (timeUnit != TimeUnit.MILLISECONDS) {
							conversionValue = timeUnit.getConversionValue() * 1000;
						}
						sessionTimes.addValue(sessionTime / conversionValue); // record
						// in
						// seconds
					}
					sessionStatistics.put(process.getName(), sessionTimes);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	/**
	 * Creates a graphical representation of the currently selected result type
	 * and returns the panel containing this graphical representation.
	 * 
	 * @return the graph panel
	 */
	protected JPanel visualizeSessions() {
		DisplayType type = (DisplayType) myDisplayOptions.myDisplayProperty
				.getValue();
		TimeUnit timeUnit = (TimeUnit) timeUnitProperty.getValue();
		JPanel result = new JPanel(new BorderLayout());
		result.setOpaque(false);
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		// Ordering the keys for the X axis (i.e., activities)
		keys = new String[sessionStatistics.keySet().size()];
		Iterator iterator = sessionStatistics.keySet().iterator();
		for (int i = 0; i < sessionStatistics.size(); i++) {
			keys[i] = (String) iterator.next();
		}
		Arrays.sort(keys);
		// adding the measurements to the data set
		for (int i = 0; i < keys.length; i++) {
			// get statistics
			SummaryStatistics statistic = (SummaryStatistics) sessionStatistics
					.get(keys[i]);
			// add to data set for chart
			double duration = SessionOptions.getStatisticValue(statistic, type);
			dataset.addValue(duration, keys[i], keys[i]);
		}
		// create the Y axis label
		StringBuffer label = new StringBuffer();
		label.append(type.toString());
		label.append(" of Session Times ");
		if (type.isTimed()) {
			label.append("(in ");
			label.append(timeUnit.toString());
			label.append(")");
		}
		chart = ChartFactory.createStackedBarChart3D("", // chart title
				"Instance", // domain axis label
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
		return result;
	}

	/**
	 * Helper method initializing the chart object used for the graphical
	 * representation.
	 * 
	 * @param chart
	 *            the chart object
	 */
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

	/**
	 * Constructs result view of the plugin.
	 * 
	 * @return the resutl panel
	 */
	protected JPanel constructResultPanel() {
		RoundedPanel resultPanel = new RoundedPanel(10, 5, 5);
		resultPanel.setBackground(new Color(160, 160, 160));
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));
		JButton backButton = new AutoFocusButton("  Go back  ");
		if (RuntimeUtils.isRunningMacOsX() == true) {
			backButton.setOpaque(false);
		}
		backButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setView(confPanel);
			}
		});
		JButton saveGraphToPNG = new SlickerButton("Save as PNG");
		saveGraphToPNG.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsPNG(chart);
			}
		});
		JButton saveCSVButton = new SlickerButton("Save as CSV");
		saveCSVButton.setToolTipText("Saves the session metrics as CSV file");
		saveCSVButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// actually save to file
				JFileChooser saveDialog = new JFileChooser();
				saveDialog.setSelectedFile(new File("SessionTimes.csv"));
				if (saveDialog.showSaveDialog(MainUI.getInstance()) == JFileChooser.APPROVE_OPTION) {
					File outFile = saveDialog.getSelectedFile();
					try {
						BufferedWriter outWriter = new BufferedWriter(
								new FileWriter(outFile));
						outWriter.write(getCsvRepresentation());
						outWriter.flush();
						outWriter.close();
						JOptionPane
								.showMessageDialog(
										MainUI.getInstance(),
										"Session statistics have been saved\nto CSV file!",
										"Session statistics saved.",
										JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		resultPanel.add(GuiUtilities.packCenterHorizontally(myDisplayOptions
				.getPanel()));
		resultPanel.add(Box.createVerticalStrut(10));
		resultPanel.add(visualizeSessions());
		resultPanel.add(Box.createVerticalStrut(20));
		JPanel packed = new JPanel();
		packed.setOpaque(false);
		packed.setLayout(new BoxLayout(packed, BoxLayout.X_AXIS));
		packed.add(Box.createHorizontalStrut(10));
		packed.add(saveGraphToPNG);
		packed.add(Box.createHorizontalStrut(5));
		packed.add(saveCSVButton);
		packed.add(Box.createHorizontalGlue());
		packed.add(backButton);
		int height = (int) backButton.getMinimumSize().getHeight();
		packed.setMinimumSize(backButton.getMinimumSize());
		packed.setMaximumSize(new Dimension(4000, (int) height));
		packed.setPreferredSize(new Dimension(4000, (int) height));
		resultPanel.add(packed);
		return resultPanel;
	}

	/**
	 * Creates a CSV representation of the Session metrics (to be opened in
	 * MSExcel).
	 * 
	 * @return the string to be written to the CSV file
	 */
	protected String getCsvRepresentation() {
		DisplayType[] allTypes = DisplayType.values();
		TimeUnit timeUnit = (TimeUnit) timeUnitProperty.getValue();
		StringBuffer sb = new StringBuffer();
		sb.append("Session Time Statistics in " + timeUnit.toString() + "\n");
		sb.append("Instance,");
		for (int i = 0; i < allTypes.length; i++) {
			sb.append(allTypes[i].toString());
			if (i < (allTypes.length - 1)) {
				sb.append(",");
			}
		}
		sb.append("\n");
		// Ordering the keys for the X axis
		keys = new String[sessionStatistics.keySet().size()];
		Iterator iterator = sessionStatistics.keySet().iterator();
		for (int i = 0; i < sessionStatistics.size(); i++) {
			keys[i] = (String) iterator.next();
		}
		Arrays.sort(keys);
		for (int i = 0; i < keys.length; i++) {
			SummaryStatistics statistic = (SummaryStatistics) sessionStatistics
					.get(keys[i]);
			sb.append(keys[i] + ",");
			for (int j = 0; j < allTypes.length; j++) {
				double duration = SessionOptions.getStatisticValue(statistic,
						allTypes[j]);
				sb.append(duration);
				if (j < (allTypes.length - 1)) {
					sb.append(",");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}

}
