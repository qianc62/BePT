package org.processmining.analysis.pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.deckfour.slickerbox.components.AutoFocusButton;
import org.deckfour.slickerbox.components.HeaderBar;
import org.deckfour.slickerbox.components.SmoothPanel;
import org.processmining.analysis.Analyzer;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFile;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

public class PatternAnalysisPlugin extends JPanel implements Provider {
	@Analyzer(name = "Pattern analyzer", names = { "Log" })
	public JPanel analyze(LogReader log) {
		inputLog = log;
		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);
		buildMainMenuGui();
		return this;
	}

	// HTML styles
	private static final String H1 = "h1";
	private static final String TR = "tr";
	private static final String TH = "th";
	private static final String TD = "td";

	protected LogReader inputLog;
	protected LogReader patternLog;
	protected ArrayList<ArrayList<ArrayList<AuditTrailEntry>>> patternList;
	protected ArrayList<LogReader> patternLogList;
	protected GUIPropertyInteger timeOffset;
	protected GUIPropertyInteger minSize;
	protected GUIPropertyInteger minCommonActivity;
	protected GUIPropertyInteger minDay;
	protected GUIPropertyBoolean provideSingleInstance;
	protected GUIPropertyBoolean makePatternWithRemainings;
	protected GUIPropertyBoolean makeDayPattern;
	protected int sizePattern;
	protected int sizeRemainder;
	protected JPanel configurationPanel;
	protected JPanel resultPanel;
	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);
	public ArrayList<HashSet<String>> patternSetList = null;

	public void buildMainMenuGui() {
		// create configuration panel
		JPanel confLowerPanel = new JPanel();
		confLowerPanel.setBackground(colorBg);
		confLowerPanel.setLayout(new BorderLayout());
		confLowerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		configurationPanel = new JPanel();
		configurationPanel.setBackground(colorTextAreaBg);
		configurationPanel.setForeground(colorFg);
		configurationPanel.setLayout(new BoxLayout(configurationPanel,
				BoxLayout.Y_AXIS));
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		// add scroll pane to the left of the configuration panel
		JPanel configSuperPanel = new JPanel();
		configSuperPanel.setBorder(BorderFactory
				.createEmptyBorder(5, 5, 10, 10));
		configSuperPanel.setBackground(colorBg);
		configSuperPanel.setMinimumSize(new Dimension(400, 2000));
		configSuperPanel.setMaximumSize(new Dimension(450, 2000));
		configSuperPanel.setPreferredSize(new Dimension(440, 2000));
		configSuperPanel.setLayout(new BorderLayout());
		JLabel configSuperLabel = new JLabel("Configuration");
		configSuperLabel.setBorder(BorderFactory
				.createEmptyBorder(0, 0, 10, 10));
		configSuperLabel.setForeground(colorFg);
		configSuperLabel.setOpaque(false);
		configSuperLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		configSuperLabel.setFont(configSuperLabel.getFont().deriveFont(16.0f));
		configSuperPanel.add(configSuperLabel, BorderLayout.NORTH);
		configSuperPanel.add(configurationPanel, BorderLayout.CENTER);

		confLowerPanel.add(configSuperPanel, BorderLayout.CENTER);

		// consider different originators for the same task
		JPanel originatorPanel = new JPanel();
		originatorPanel.setBackground(colorTextAreaBg);
		originatorPanel.setForeground(colorFg);
		originatorPanel.setLayout(new BoxLayout(originatorPanel,
				BoxLayout.Y_AXIS));
		originatorPanel.setBorder(BorderFactory.createEmptyBorder());
		originatorPanel.setPreferredSize(new Dimension(350, 250));
		originatorPanel.setMaximumSize(new Dimension(350, 250));
		GregorianCalendar calendar = new GregorianCalendar();
		int k = (calendar.getTimeZone().getDefault().getRawOffset() + calendar
				.getTimeZone().getDSTSavings()) / 3600000;
		timeOffset = new GUIPropertyInteger("Timezone Offset: ", "", k, 0, 24);
		originatorPanel.add(timeOffset.getPropertyPanel());
		minSize = new GUIPropertyInteger("Min pattern size: ", "", 8, 0, 100);
		;
		originatorPanel.add(minSize.getPropertyPanel());
		minCommonActivity = new GUIPropertyInteger("Min common activities: ",
				"", 3, 0, 100);
		;
		originatorPanel.add(minCommonActivity.getPropertyPanel());

		provideSingleInstance = new GUIPropertyBoolean(
				"Provide a single instance", false);
		originatorPanel.add(provideSingleInstance.getPropertyPanel());

		makePatternWithRemainings = new GUIPropertyBoolean(
				"Make pattern with Remainings", false);
		originatorPanel.add(makePatternWithRemainings.getPropertyPanel());

		makeDayPattern = new GUIPropertyBoolean("Make day pattern", false);
		originatorPanel.add(makeDayPattern.getPropertyPanel());

		configurationPanel.add(originatorPanel);

		// create right side, i.e. distance metric and clustering algorithm
		// choice
		SmoothPanel rightPanel = new SmoothPanel();
		rightPanel.setBackground(new Color(140, 140, 140));
		rightPanel.setHighlight(new Color(160, 160, 160));
		rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		rightPanel.add(Box.createHorizontalStrut(5));
		// add right side to configuration panel at center position
		JButton startButton = new AutoFocusButton("start calculation");
		startButton.setOpaque(false);
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (makeDayPattern.getValue()) {
					dayPattern();
				} else if (provideSingleInstance.getValue()) {
					splitLogs();
				} else {
					findGeneralizedSequencePattern();
					patternGeneralizedReplaceToOriginalLog();
					makePatternLog();
					initGraphPanel();
				}
			}
		});
		rightPanel.add(startButton);
		confLowerPanel.add(rightPanel, BorderLayout.EAST);

		// add header
		configurationPanel = new JPanel();
		configurationPanel.setBorder(BorderFactory.createEmptyBorder());
		configurationPanel.setLayout(new BorderLayout());
		configurationPanel.add(confLowerPanel, BorderLayout.CENTER);

		HeaderBar header = new HeaderBar("Pattern Analysis");
		header.setHeight(40);
		configurationPanel.add(header, BorderLayout.NORTH);
		// set configuration panel as displayed
		configurationPanel.revalidate();
		this.removeAll();
		this.add(configurationPanel, BorderLayout.CENTER);
		this.revalidate();
		this.repaint();
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void splitLogs() {
		try {
			File outputFile = File.createTempFile("PatternAnalysisTemp",
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
					int patternCounter = 0;
					// int remainingCounter = 0;
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					boolean bInstanceFind = false;
					boolean bFlag = true;
					persistency.startProcessInstance(name, "Original instance",
							new HashMap<String, String>());
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						boolean bFind = false;
						try {
							AuditTrailEntry entry = ateList.get(i1);
							if (entry.getTimestamp() == null)
								continue;
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									num++;
								} else if (date2 == date + 1) {
									num++;
									date++;
								} else {
									break;
								}
								if (num > minSize.getValue()) {
									bFind = true;
								}
							}

							if (bFind) {
								persistency.endProcessInstance();
								persistency.startProcessInstance(name
										+ "_pattern" + patternCounter++,
										"instance split up from intance "
												+ name,
										new HashMap<String, String>());
								for (int k = 0; k < num; k++) {
									persistency
											.addAuditTrailEntry(promAte2mxmlibAte(ateList
													.get(i1 + k)));
								}
								persistency.endProcessInstance(); // end last
								// session
								// instance
								i1 += num;
								if (i1 < ateList.size()) {
									bInstanceFind = true;
								} else {
									bFlag = false;
								}
							} else {
								if (bInstanceFind) {
									persistency.startProcessInstance(name
											+ "_remaining" + patternCounter++,
											"instance split up from intance "
													+ name,
											new HashMap<String, String>());
									bInstanceFind = false;
								}
								persistency
										.addAuditTrailEntry(promAte2mxmlibAte(ateList
												.get(i1)));
							}
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
					if (bFlag)
						persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			patternLog = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void dayPattern() {
		try {
			File outputFile = File.createTempFile("PatternAnalysisTemp",
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
					int patternCounter = 0;
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						persistency.startProcessInstance(name + "_day"
								+ patternCounter++, "Original instance",
								new HashMap<String, String>());
						try {
							AuditTrailEntry entry = ateList.get(i1);
							persistency
									.addAuditTrailEntry(promAte2mxmlibAte(entry));
							if (entry.getTimestamp() == null) {
								persistency.endProcessInstance();
								continue;
							}
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									persistency
											.addAuditTrailEntry(promAte2mxmlibAte(ateList
													.get(j1)));
									num++;
								} else {
									break;
								}
							}
							i1 += num;
							persistency.endProcessInstance();
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			patternLog = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	/*
	 * protected void findSequencePattern() {
	 * 
	 * patternSet = new HashSet<HashSet<String>>(); patternList = new
	 * ArrayList<ArrayList<ArrayList<AuditTrailEntry>>>(); try { Process process
	 * = inputLog.getProcess(0); ProcessInstance instance = null;
	 * AuditTrailEntryList ateList = null; // String name = process.getName();
	 * for (int i=0; i<inputLog.numberOfProcesses(); i++) { process =
	 * inputLog.getProcess(i); for (int j=0; j<process.size(); j++) { instance =
	 * process.getInstance(j); // int patternCounter = 0; // int
	 * remainingCounter = 0; ateList = instance.getAuditTrailEntryList();
	 * 
	 * long date = 0; // boolean bInstanceFind = false; // boolean bFlag = true;
	 * for (int i1 = 0; i1< ateList.size(); i1++) { boolean bFind = false; try {
	 * AuditTrailEntry entry = ateList.get(i1); if(entry.getTimestamp()==null)
	 * continue; date =
	 * (entry.getTimestamp().getTime()+timeOffset.getValue()*3600000L)/
	 * (long)86400000L; int num = 0; for (int j1 = i1+1; j1 < ateList.size();
	 * j1++) { AuditTrailEntry entry2 = ateList.get(j1); long date2 =
	 * (entry2.getTimestamp().getTime()+timeOffset.getValue()*3600000L)/
	 * (long)86400000L; if(date2==date) { num++; } else if(date2==date+1) {
	 * num++; date++; } else { break; } if(num>minSize.getValue()) { bFind =
	 * true; } }
	 * 
	 * if(bFind){ HashSet<String> pattern = new HashSet<String>(); for(int
	 * k=0;k<num;k++) { pattern.add(ateList.get(i1+k).getElement()); } boolean
	 * bbFlag = true; int k=0; for(HashSet<String> aPattern:patternSet) {
	 * if(aPattern.equals(pattern)) { bbFlag = false; break; } k++; }
	 * 
	 * ArrayList<AuditTrailEntry> list = new ArrayList<AuditTrailEntry>();
	 * 
	 * for(int l=0;l<num;l++) { list.add(ateList.get(i1+l)); }
	 * 
	 * if (bbFlag) { patternSet.add(pattern);
	 * ArrayList<ArrayList<AuditTrailEntry>> temp = new
	 * ArrayList<ArrayList<AuditTrailEntry>>(); temp.add(list);
	 * patternList.add(temp); } else { ArrayList<ArrayList<AuditTrailEntry>> ptr
	 * = patternList.get(k); ptr.add(list); } i1+=num; } } catch (IOException
	 * ex) { Message.add("Error while filtering instance: "+ex.getMessage(),
	 * Message.ERROR); } catch (IndexOutOfBoundsException ex) {
	 * Message.add("Error while filtering instance: "+ex.getMessage(),
	 * Message.ERROR); } } } }
	 * 
	 * } catch (Exception ex) { ex.printStackTrace(); }
	 * 
	 * for(HashSet<String> aPattern:patternSet) { for(String str:aPattern)
	 * System.out.print(str+":"); System.out.println(); } }
	 */

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void findGeneralizedSequencePattern() {

		patternSetList = new ArrayList<HashSet<String>>();
		patternList = new ArrayList<ArrayList<ArrayList<AuditTrailEntry>>>();
		try {
			Process process = inputLog.getProcess(0);
			ProcessInstance instance = null;
			AuditTrailEntryList ateList = null;
			// String name = process.getName();
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					// int patternCounter = 0;
					// int remainingCounter = 0;
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					// boolean bInstanceFind = false;
					// boolean bFlag = true;
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						boolean bFind = false;
						try {
							AuditTrailEntry entry = ateList.get(i1);
							if (entry.getTimestamp() == null)
								continue;
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									num++;
								} else if (date2 == date + 1) {
									num++;
									date++;
								} else {
									break;
								}
								if (num > minSize.getValue()) {
									bFind = true;
								}
							}

							if (bFind) {
								HashSet<String> pattern = new HashSet<String>();
								for (int k = 0; k < num; k++) {
									pattern.add(ateList.get(i1 + k)
											.getElement());
								}
								boolean bbFlag = true;
								int k = 0;
								for (HashSet<String> aPattern : patternSetList) {
									int num2 = 0;
									for (String str : aPattern) {
										for (String str2 : pattern) {
											if (str.equals(str2))
												num2++;
										}
									}
									if (num2 >= minCommonActivity.getValue()) {
										// System.out.println("test:k="+k+", "+aPattern.toString()+", "+pattern.toString());
										bbFlag = false;
										break;
									}
									k++;
								}

								ArrayList<AuditTrailEntry> list = new ArrayList<AuditTrailEntry>();

								for (int l = 0; l < num; l++) {
									list.add(ateList.get(i1 + l));
								}

								if (bbFlag) {
									patternSetList.add(pattern);
									ArrayList<ArrayList<AuditTrailEntry>> temp = new ArrayList<ArrayList<AuditTrailEntry>>();
									temp.add(list);
									patternList.add(temp);
									// for(String str:pattern)
									// System.out.print(str+":");
									// System.out.println();

								} else {
									ArrayList<ArrayList<AuditTrailEntry>> ptr = patternList
											.get(k);
									ptr.add(list);
								}
								i1 += num;
							}
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		int i = 0;
		// for(HashSet<String> aPattern:patternSetList)
		// {
		// for(String str:aPattern)
		// System.out.print(str+":");
		// System.out.println(", number = " + patternList.get(i++).size());
		//
		// }
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void findGeneralizedDayPattern() {

		patternSetList = new ArrayList<HashSet<String>>();
		patternList = new ArrayList<ArrayList<ArrayList<AuditTrailEntry>>>();
		try {
			Process process = inputLog.getProcess(0);
			ProcessInstance instance = null;
			AuditTrailEntryList ateList = null;
			// String name = process.getName();
			for (int i = 0; i < inputLog.numberOfProcesses(); i++) {
				process = inputLog.getProcess(i);
				for (int j = 0; j < process.size(); j++) {
					instance = process.getInstance(j);
					// int patternCounter = 0;
					// int remainingCounter = 0;
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					// boolean bInstanceFind = false;
					// boolean bFlag = true;
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						boolean bFind = false;
						try {
							AuditTrailEntry entry = ateList.get(i1);
							if (entry.getTimestamp() == null)
								continue;
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									num++;
								} else if (date2 == date + 1) {
									num++;
									date++;
								} else {
									break;
								}
								if (num > minSize.getValue()) {
									bFind = true;
								}
							}

							if (bFind) {
								HashSet<String> pattern = new HashSet<String>();
								for (int k = 0; k < num; k++) {
									pattern.add(ateList.get(i1 + k)
											.getElement());
								}
								boolean bbFlag = true;
								int k = 0;
								for (HashSet<String> aPattern : patternSetList) {
									int num2 = 0;
									for (String str : aPattern) {
										for (String str2 : pattern) {
											if (str.equals(str2))
												num2++;
										}
									}
									if (num2 >= minCommonActivity.getValue()) {
										bbFlag = false;
										break;
									}
									k++;
								}

								ArrayList<AuditTrailEntry> list = new ArrayList<AuditTrailEntry>();

								for (int l = 0; l < num; l++) {
									list.add(ateList.get(i1 + l));
								}

								if (bbFlag) {
									patternSetList.add(pattern);
									ArrayList<ArrayList<AuditTrailEntry>> temp = new ArrayList<ArrayList<AuditTrailEntry>>();
									temp.add(list);
									patternList.add(temp);
								} else {
									ArrayList<ArrayList<AuditTrailEntry>> ptr = patternList
											.get(k);
									ptr.add(list);
								}
								i1 += num;
							}
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// for(HashSet<String> aPattern:patternSetList)
		// {
		// for(String str:aPattern)
		// System.out.print(str+":");
		// System.out.println();
		// }
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void makePatternLog() {
		patternLogList = new ArrayList<LogReader>();
		try {
			for (int i = 0; i < patternList.size(); i++) {
				ArrayList<ArrayList<AuditTrailEntry>> tempList = patternList
						.get(i);
				File outputFile = File.createTempFile("PatternAnalysisTemp",
						".mxml.gz");
				FileOutputStream output = new FileOutputStream(outputFile);
				BufferedOutputStream out = new BufferedOutputStream(
						new GZIPOutputStream(output));
				LogPersistencyStream persistency = new LogPersistencyStream(
						out, false);
				String name = "Pattern_" + i;
				String description = name + " exported by MXMLib @ P-stable";
				String source = "UnknownSource";
				persistency.startLogfile(name, description, source);
				persistency.startProcess(name, description,
						new HashMap<String, String>());

				for (int j = 0; j < tempList.size(); j++) {
					persistency.startProcessInstance("instance_" + j,
							"pattern instance", new HashMap<String, String>());
					ArrayList<AuditTrailEntry> ateList = tempList.get(j);
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						persistency
								.addAuditTrailEntry(promAte2mxmlibAte(ateList
										.get(i1)));

					}
					persistency.endProcessInstance();
				}
				persistency.endProcess();
				// clean up
				persistency.endLogfile();
				persistency.finish();
				// read back again
				LogFile logFile = LogFile.getInstance(outputFile
						.getAbsolutePath());
				LogReader tempLog = BufferedLogReader
						.createInstance(new DefaultLogFilter(
								DefaultLogFilter.INCLUDE), logFile);
				patternLogList.add(tempLog);
				outputFile.deleteOnExit();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// for(HashSet<String> aPattern:patternSetList)
		// {
		// for(String str:aPattern)
		// System.out.print(str+":");
		// System.out.println();
		// }
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void patternReplaceToOriginalLog() {

		try {
			File outputFile = File.createTempFile("PatternAnalysisTemp",
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
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					boolean bFlag = true;
					persistency.startProcessInstance(name, "Original instance",
							new HashMap<String, String>());
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						boolean bFind = false;
						try {
							AuditTrailEntry entry = ateList.get(i1);
							if (entry.getTimestamp() == null)
								continue;
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									num++;
								} else if (date2 == date + 1) {
									num++;
									date++;
								} else {
									break;
								}
								if (num > minSize.getValue()) {
									bFind = true;
								}
							}

							if (bFind) {
								HashSet<String> pattern = new HashSet<String>();
								for (int k = 0; k < num; k++) {
									pattern.add(ateList.get(i1 + k)
											.getElement());
								}
								boolean bbFlag = false;
								int k = 0;
								for (HashSet<String> aPattern : patternSetList) {
									if (aPattern.equals(pattern)) {
										bbFlag = true;
										break;
									}
									k++;
								}
								if (bbFlag) {
									persistency.addAuditTrailEntry(addPattern(
											"pattern_" + k, ateList.get(i1),
											"start"));
									persistency.addAuditTrailEntry(addPattern(
											"pattern_" + k, ateList.get(i1
													+ num), "complete"));
								}
								i1 += num;
								if (i1 < ateList.size()) {

								} else {
									bFlag = false;
								}
							} else {
								persistency
										.addAuditTrailEntry(promAte2mxmlibAte(ateList
												.get(i1)));
							}
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
					if (bFlag)
						persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			patternLog = BufferedLogReader.createInstance(new DefaultLogFilter(
					DefaultLogFilter.INCLUDE), logFile);
			outputFile.deleteOnExit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Actually splits the given process into several processes and new
	 * instances based on the sessions.
	 */
	protected void patternGeneralizedReplaceToOriginalLog() {

		try {
			File outputFile = File.createTempFile("PatternAnalysisTemp",
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
					ateList = instance.getAuditTrailEntryList();

					long date = 0;
					boolean bFlag = true;
					persistency.startProcessInstance(name, "Original instance",
							new HashMap<String, String>());
					for (int i1 = 0; i1 < ateList.size(); i1++) {
						boolean bFind = false;
						try {
							AuditTrailEntry entry = ateList.get(i1);
							if (entry.getTimestamp() == null)
								continue;
							date = (entry.getTimestamp().getTime() + timeOffset
									.getValue() * 3600000L)
									/ (long) 86400000L;
							int num = 0;
							for (int j1 = i1 + 1; j1 < ateList.size(); j1++) {
								AuditTrailEntry entry2 = ateList.get(j1);
								long date2 = (entry2.getTimestamp().getTime() + timeOffset
										.getValue() * 3600000L)
										/ (long) 86400000L;
								if (date2 == date) {
									num++;
								} else if (date2 == date + 1) {
									num++;
									date++;
								} else {
									break;
								}
								if (num > minSize.getValue()) {
									bFind = true;
								}
							}

							if (bFind) {
								HashSet<String> pattern = new HashSet<String>();
								for (int k = 0; k < num; k++) {
									pattern.add(ateList.get(i1 + k)
											.getElement());
								}
								boolean bbFlag = false;
								int k = 0;
								for (HashSet<String> aPattern : patternSetList) {
									int num2 = 0;
									for (String str : aPattern) {
										for (String str2 : pattern) {
											if (str.equals(str2))
												num2++;
										}
									}
									if (num2 >= minCommonActivity.getValue()) {
										bbFlag = true;
										break;
									}
									k++;
								}
								if (bbFlag) {
									persistency.addAuditTrailEntry(addPattern(
											"pattern_" + k, ateList.get(i1),
											"start"));
									persistency.addAuditTrailEntry(addPattern(
											"pattern_" + k, ateList.get(i1
													+ num), "complete"));
								}
								i1 += num;
								if (i1 < ateList.size()) {
								} else {
									bFlag = false;
								}
							} else {
								persistency
										.addAuditTrailEntry(promAte2mxmlibAte(ateList
												.get(i1)));
							}
						} catch (IOException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						} catch (IndexOutOfBoundsException ex) {
							Message.add("Error while filtering instance: "
									+ ex.getMessage(), Message.ERROR);
						}
					}
					if (bFlag)
						persistency.endProcessInstance();
				}
				persistency.endProcess();
			}
			// clean up
			persistency.endLogfile();
			persistency.finish();
			// read back again
			LogFile logFile = LogFile.getInstance(outputFile.getAbsolutePath());
			patternLog = BufferedLogReader.createInstance(new DefaultLogFilter(
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
	 * Helper method to convert the given log entry in an mxmllib log entry.
	 * 
	 * @param promAte
	 *            the log entry
	 * @return the mxml log entry
	 */
	protected org.processmining.lib.mxml.AuditTrailEntry addPattern(
			String patternName, AuditTrailEntry promAte, String eventType) {
		org.processmining.lib.mxml.AuditTrailEntry mxmlibAte = new org.processmining.lib.mxml.AuditTrailEntry();
		mxmlibAte.setWorkflowModelElement(patternName);
		mxmlibAte.setEventType(EventType.getType(eventType));
		mxmlibAte.setOriginator(promAte.getOriginator());
		if (promAte.getTimestamp() != null) {
			mxmlibAte.setTimestamp(promAte.getTimestamp());
		}
		mxmlibAte.setAttributes(promAte.getAttributes());
		return mxmlibAte;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.processmining.framework.plugin.Provider#getProvidedObjects()
	 */
	public ProvidedObject[] getProvidedObjects() {

		if (patternLogList != null) {
			ProvidedObject[] objects = new ProvidedObject[2 + patternLogList
					.size()];
			objects[0] = new ProvidedObject("Input Log",
					new Object[] { inputLog });
			objects[1] = new ProvidedObject("Pattern Log",
					new Object[] { patternLog });
			for (int k = 0; k < patternLogList.size(); k++) {
				objects[2 + k] = new ProvidedObject("Pattern" + k + " ("
						+ patternLogList.get(k).numberOfInstances() + ")",
						new Object[] { patternLogList.get(k) });
			}
			return objects;
		} else if (patternLog != null) {
			ProvidedObject[] objects = {
					new ProvidedObject("Input Log", new Object[] { inputLog }),
					new ProvidedObject("Pattern Log",
							new Object[] { patternLog }) };
			return objects;
		} else {
			ProvidedObject[] objects = { new ProvidedObject("Input Log",
					new Object[] { inputLog }) };
			return objects;
		}
	}

	protected void initGraphPanel() {
		HeaderBar header = new HeaderBar("Pattern Analysis Result");
		header.setHeight(40);
		header.setCloseActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// reset to configuration view
				removeAll();
				add(configurationPanel, BorderLayout.CENTER);
				revalidate();
				repaint();
			}
		});

		resultPanel = new JPanel();
		resultPanel.setBorder(BorderFactory.createEmptyBorder());
		resultPanel.setLayout(new BorderLayout());
		resultPanel.add(header, BorderLayout.NORTH);
		resultPanel.add(getTextPanel(), BorderLayout.CENTER);
		// set configuration panel as displayed
		resultPanel.revalidate();
		this.removeAll();
		this.add(resultPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	protected JScrollPane getTextPanel() {
		String content = "";
		content = this.getHtmlRepresentation();

		// fill the text pane
		JTextPane myTextPane = new JTextPane();
		myTextPane.setContentType("text/html");
		myTextPane.setText(content);
		myTextPane.setEditable(false);
		myTextPane.setCaretPosition(0);

		JScrollPane scrollPane = new JScrollPane(myTextPane);
		scrollPane.setBackground(colorBg);
		return scrollPane;
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
	private String getHtmlRepresentation() {

		// distance statistics
		StringBuffer sb = new StringBuffer("<html><body>");
		sb.append(tag("Table view ", H1));
		sb.append("<table border=\"1\">");

		// write duration statistics table header
		StringBuffer tableHeader = new StringBuffer();
		tableHeader.append(tag("Name", TH)); // add activity column header
		tableHeader.append(tag("Activities", TH));
		sb.append(tag(tableHeader.toString(), TR));

		int k = 0;
		for (HashSet<String> aPattern : patternSetList) {
			StringBuffer tempBuffer = new StringBuffer();
			tempBuffer.append(tag("Pattern_" + k++, TD));
			String str2 = "";
			for (String str : aPattern)
				str2 += str + ":";
			tempBuffer.append(tag(str2, TD));
			sb.append(tag(tempBuffer.toString(), TR));
		}
		sb.append("</table>");
		sb.append("</body></html>");
		return sb.toString();
	}

	// helper method from the extended log summary - functionality will be moved
	// to the log summary at a later point in time
	private String tag(String s, String tag) {
		return "<" + tag + ">" + s + "</" + tag + ">";
	}

}
