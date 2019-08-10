package org.processmining.analysis.abstractions.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.deckfour.slickerbox.components.RoundedPanel;
import org.deckfour.slickerbox.components.SlickerButton;
import org.processmining.analysis.abstractions.util.AbstractionSetTheory;
import org.processmining.analysis.abstractions.util.FileIO;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.MainUI;
import org.processmining.framework.util.Logger;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.LogException;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

@SuppressWarnings("serial")
public class RepeatAbstractionResUI extends JPanel {

	class TreeSetComparator implements Comparator<TreeSet<String>> {
		public int compare(TreeSet<String> t1, TreeSet<String> t2) {
			return compare(t1.toString(), t2.toString());
		}

		int compare(String s1, String s2) {
			return s1.equals(s2) ? 0 : (s1.length() <= s2.length() ? (s1
					.length() < s2.length() ? 1 : s2.compareTo(s1)) : -1);
		}
	}

	class DescendingStrLengthComparator implements Comparator<String> {
		public int compare(String s1, String s2) {
			return s1.equals(s2) ? 0 : (s1.length() <= s2.length() ? (s1
					.length() < s2.length() ? 1 : s2.compareTo(s1)) : -1);
		}
	}

	public static Color colorBg = new Color(120, 120, 120);

	/*
	 * The encoding length of the activities for charStreams
	 */
	int encodingLength;

	/*
	 * The charActivity Map which defines the encoding of activities
	 */
	static HashMap<String, String> charActivityMap;
	static HashMap<String, String> activityCharMap;

	/*
	 * The repeatAlphabet, repeat Map; a single alphabet can have different
	 * manifestations of repeats
	 */
	HashMap<TreeSet<String>, TreeSet<String>> alphabetRepeatMap;
	HashMap<String, TreeSet<String>> repeatAlphabetMap;
	/*
	 * The count of alphabets in the input log; the count of a repeat alphabet
	 * is defined as the sum of counts of repeats under the equivalence class of
	 * the repeat alphabet
	 */
	HashMap<TreeSet<String>, Integer> alphabetCountMap;

	/*
	 * The original tabular information of repeats and the filtered information
	 * (display data) used in displaying the data in tabular format in the lower
	 * panel
	 */
	Object[][] originalData, displayData;
	JTable table;

	JLabel noAlphabetsLabel, freqFilterLabel, sizeFilterLabel;
	JSlider repeatFreqFilterSlider, repeatSizeFilterSlider;
	int repeatFreqFilterThreshold, repeatSizeFilterThreshold;

	JLabel commonElementsFilterLabel, differentElementsFilterLabel;
	JSlider commonElementsFilterSlider, differentElementsFilterSlider;
	double commonElementsFilterThreshold, differentElementsFilterThreshold;

	ArrayList<RepeatAbstractionResultConfigurationComponent> repeatAbstractionResultConfigurationComponentList;
	HashMap<TreeSet<String>, TreeSet<String>> originalAlphabetDecodedAlphabetMap;
	HashMap<TreeSet<String>, TreeSet<String>> decodedAlphabetOriginalAlphabetMap;

	JLabel abstractionCountLabel;
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> originalMaximalElementSubsumedElementSetMap;
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> intermediateMaximalElementSubsumedElementSetMap;
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> originalAlphabetNewAlphabetMap;

	HashMap<TreeSet<String>, String> abstractionAlphabetAbstractionNameMap;
	HashMap<TreeSet<String>, TreeSet<String>> alphabetAbstractionNameSetMap;
	HashMap<TreeSet<String>, TreeSet<String>> missedAlphabetAbstractionNameSetMap;
	HashMap<TreeSet<String>, TreeSet<String>> resolvedAlphabetAbstractionNameSetMap;

	ArrayList<org.processmining.lib.mxml.AuditTrailEntry> modifiedATEList;

	HashMap<String, TreeSet<String>> startSymbolRepeatSetMap;

	JPanel abstractionResultPanel;
	JScrollPane abstractionResultScrollPane;

	int noAbstractions;
	String transformLogFileName;

	String outputDir = "D:\\JC\\TempAbs";
	String delim = "^";
	boolean debug = false;

	public RepeatAbstractionResUI(int encodingLength,
			HashMap<TreeSet<String>, TreeSet<String>> alphabetRepeatMap,
			HashMap<TreeSet<String>, Integer> alphabetCountMap,
			HashMap<String, String> charActivityMap) {
		this.encodingLength = encodingLength;
		RepeatAbstractionResUI.charActivityMap = charActivityMap;
		this.alphabetRepeatMap = alphabetRepeatMap;
		this.alphabetCountMap = alphabetCountMap;

		if (debug)
			Logger.startLog();

		// FileIO io = new FileIO();
		// io.writeToFile(outputDir, "charActivityMap.txt",
		// charActivityMap,delim);
		// io.writeToFile(outputDir, "originalAlphabetCountMap.txt",
		// alphabetCountMap,delim);

		/**
		 * Generate the activityCharMap from the charActivityMap; This could
		 * have been passed from the calling method, but this is done here just
		 * to save memory (stack space)
		 */

		RepeatAbstractionResUI.activityCharMap = new HashMap<String, String>();
		for (String charEncoding : charActivityMap.keySet())
			activityCharMap.put(charActivityMap.get(charEncoding).trim(),
					charEncoding.trim());

		populateRepeatInformation();
		setupGui();
	}

	private void populateRepeatInformation() {
		if (debug)
			Logger.printCall("Populate Repeat Information");

		int noEntries = alphabetRepeatMap.size();

		// The three columns correspond to the repeatAlphabet, repeats
		// equivalent to the alphabet and the repeatAlphabet count
		originalData = new Object[noEntries][3];

		originalAlphabetDecodedAlphabetMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		decodedAlphabetOriginalAlphabetMap = new HashMap<TreeSet<String>, TreeSet<String>>();

		TreeSet<String> decodedRepeatAlphabet;
		TreeSet<String> alphabetRepeatSet;

		String decodedRepeat;
		int repeatLength;

		int index = 0;
		for (TreeSet<String> alphabet : alphabetRepeatMap.keySet()) {
			decodedRepeatAlphabet = new TreeSet<String>();
			for (String symbol : alphabet) {
				decodedRepeatAlphabet.add(charActivityMap.get(symbol));
			}

			originalAlphabetDecodedAlphabetMap.put(alphabet,
					decodedRepeatAlphabet);
			decodedAlphabetOriginalAlphabetMap.put(decodedRepeatAlphabet,
					alphabet);

			originalData[index][0] = decodedRepeatAlphabet;

			alphabetRepeatSet = alphabetRepeatMap.get(alphabet);

			decodedRepeat = "";
			for (String repeat : alphabetRepeatSet) {
				repeatLength = repeat.length() / encodingLength;
				decodedRepeat += "<";
				for (int i = 0; i < repeatLength - 1; i++) {
					decodedRepeat += charActivityMap.get(repeat.substring(i
							* encodingLength, (i + 1) * encodingLength))
							+ ",";
				}
				decodedRepeat += charActivityMap.get(repeat.substring(
						(repeatLength - 1) * encodingLength, repeatLength
								* encodingLength))
						+ ">\n\n";
			}

			originalData[index][1] = decodedRepeat;
			originalData[index][2] = alphabetCountMap.get(alphabet);
			index++;
		}

		displayData = originalData.clone();
		alphabetCountMap = null;

		// FileIO io = new FileIO();
		// io.writeToFile(outputDir, "originalAlphabetDecodedAlphabetMap.txt",
		// originalAlphabetDecodedAlphabetMap,delim);
		// io.writeToFile(outputDir, "decodedAlphabetOriginalAlphabetMap.txt",
		// decodedAlphabetOriginalAlphabetMap,delim);

		if (debug)
			Logger.printReturn("Populate Repeat Information");
	}

	private void setupGui() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(colorBg);

		JPanel lowerPanel = prepareLowerPanel();
		JPanel upperPanel = prepareUpperPanel();

		this.add(upperPanel, BorderLayout.CENTER);
		this.add(lowerPanel, BorderLayout.SOUTH);
	}

	private JPanel prepareUpperPanel() {
		if (debug)
			Logger.printCall("Prepare Upper Panel");

		JPanel upperPanel = new JPanel();
		upperPanel.setOpaque(true);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		upperPanel.setBackground(new Color(140, 140, 140));
		upperPanel.setBorder(BorderFactory.createEmptyBorder());

		JPanel commonElementsFilterPanel = new JPanel();
		commonElementsFilterPanel.setOpaque(true);
		commonElementsFilterPanel.setLayout(new BoxLayout(
				commonElementsFilterPanel, BoxLayout.Y_AXIS));
		commonElementsFilterPanel.setBackground(new Color(140, 140, 140));
		commonElementsFilterPanel.setBorder(BorderFactory.createEmptyBorder());

		commonElementsFilterLabel = new JLabel("% common elements: " + 1.0);
		commonElementsFilterSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		commonElementsFilterSlider.setOpaque(false);
		commonElementsFilterSlider.setValue(100);
		commonElementsFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				commonElementsFilterThreshold = commonElementsFilterSlider
						.getValue() / 100.0;

				if (!commonElementsFilterSlider.getValueIsAdjusting()) {
					commonElementsFilterLabel.setText("% common elements: "
							+ commonElementsFilterThreshold);
				}

				revalidate();
				repaint();
			}
		});

		commonElementsFilterPanel.add(commonElementsFilterLabel);
		commonElementsFilterPanel.add(commonElementsFilterSlider);

		JPanel differentElementsFilterPanel = new JPanel();
		differentElementsFilterPanel.setOpaque(true);
		differentElementsFilterPanel.setLayout(new BoxLayout(
				differentElementsFilterPanel, BoxLayout.Y_AXIS));
		differentElementsFilterPanel.setBackground(new Color(140, 140, 140));
		differentElementsFilterPanel.setBorder(BorderFactory
				.createEmptyBorder());

		differentElementsFilterLabel = new JLabel("% diff elements: " + 0.0);
		differentElementsFilterSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		differentElementsFilterSlider.setOpaque(false);
		differentElementsFilterSlider.setValue(0);
		differentElementsFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				differentElementsFilterThreshold = differentElementsFilterSlider
						.getValue() / 100.0;

				if (!differentElementsFilterSlider.getValueIsAdjusting()) {
					differentElementsFilterLabel.setText("% diff elements: "
							+ differentElementsFilterThreshold);
				}

				revalidate();
				repaint();
			}
		});

		differentElementsFilterPanel.add(differentElementsFilterLabel);
		differentElementsFilterPanel.add(differentElementsFilterSlider);

		abstractionCountLabel = new JLabel("No. Abstractions: " + 0);

		SlickerButton findAbstractionsButton = new SlickerButton(
				"Find Abstractions");
		findAbstractionsButton.setOpaque(false);
		findAbstractionsButton.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent arg0) {
				commonElementsFilterThreshold = commonElementsFilterSlider
						.getValue() / 100.0;
				differentElementsFilterThreshold = differentElementsFilterSlider
						.getValue() / 100.0;

				if (repeatAbstractionResultConfigurationComponentList == null) {
					repeatAbstractionResultConfigurationComponentList = new ArrayList<RepeatAbstractionResultConfigurationComponent>();
				}

				HashSet<TreeSet<String>> alphabetSet = new HashSet<TreeSet<String>>();
				for (int i = 0; i < displayData.length; i++)
					alphabetSet.add(decodedAlphabetOriginalAlphabetMap
							.get((TreeSet<String>) displayData[i][0]));

				if (debug)
					Logger.println("Alphabet Size for Abstraction: "
							+ alphabetSet.size());
				// FileIO io = new FileIO();
				// io.writeToFile(outputDir, "alphabetSetForAbstraction.txt",
				// alphabetSet);

				AbstractionSetTheory a = new AbstractionSetTheory();
				a.findMaximalElementSubsumedElementMap(alphabetSet, a
						.getMaximalElementsApproximateSubsumption(alphabetSet,
								commonElementsFilterThreshold,
								differentElementsFilterThreshold,
								AbstractionSetTheory.DiffStrategy.MIN_SIZE));

				originalMaximalElementSubsumedElementSetMap = a
						.getMaximalElementSubsumedElementMap();
				intermediateMaximalElementSubsumedElementSetMap = (HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>) originalMaximalElementSubsumedElementSetMap
						.clone();

				prepareAbstractionResult(a.getMaximalElementList());
				abstractionCountLabel.setText("No. Abstractions: "
						+ repeatAbstractionResultConfigurationComponentList
								.size());
				if (debug)
					Logger.println("No Abstractions: "
							+ repeatAbstractionResultConfigurationComponentList
									.size());

				// Free Memory
				a = null;

				revalidate();
				repaint();
			}
		});

		JPanel findAbstractionsPanel = new JPanel();
		findAbstractionsPanel.setLayout(new BoxLayout(findAbstractionsPanel,
				BoxLayout.X_AXIS));
		findAbstractionsPanel.add(findAbstractionsButton);

		JPanel abstractionFilterPanel = new JPanel();
		abstractionFilterPanel.setLayout(new BoxLayout(abstractionFilterPanel,
				BoxLayout.X_AXIS));
		abstractionFilterPanel.add(commonElementsFilterPanel);
		abstractionFilterPanel.add(differentElementsFilterPanel);

		JPanel abstractionParameterPanel = new JPanel();
		abstractionParameterPanel.setLayout(new BoxLayout(
				abstractionParameterPanel, BoxLayout.Y_AXIS));
		abstractionParameterPanel.setBackground(new Color(100, 100, 100));
		abstractionParameterPanel.setBorder(BorderFactory.createEmptyBorder());

		abstractionParameterPanel.add(abstractionFilterPanel);
		abstractionParameterPanel.add(findAbstractionsPanel);

		if (abstractionResultPanel == null)
			abstractionResultPanel = new JPanel();
		abstractionResultPanel.setOpaque(true);
		abstractionResultPanel.setBackground(new Color(140, 140, 140));

		if (abstractionResultScrollPane == null)
			abstractionResultScrollPane = new JScrollPane(
					abstractionResultPanel);

		RoundedPanel abstractionPanel = new RoundedPanel(10, 5, 5);
		abstractionPanel.setLayout(new BoxLayout(abstractionPanel,
				BoxLayout.X_AXIS));
		abstractionPanel.add(abstractionResultScrollPane);
		abstractionPanel.add(Box.createHorizontalStrut(5));
		abstractionPanel.add(abstractionParameterPanel);

		JPanel abstractionLowerPanel = prepareAbstractionLowerPanel();
		upperPanel.add(abstractionPanel);
		upperPanel.add(abstractionLowerPanel);

		if (debug)
			Logger.printReturn("Prepare Upper Panel");

		return upperPanel;
	}

	private JPanel prepareLowerPanel() {

		if (debug)
			Logger.printCall("Prepare Lower Panel");

		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(true);
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.setBackground(new Color(40, 40, 40));
		lowerPanel.setBorder(BorderFactory.createEmptyBorder());

		RoundedPanel repeatInfoPanel = new RoundedPanel(10, 5, 5);
		repeatInfoPanel.setBackground(new Color(90, 90, 90));
		repeatInfoPanel.setLayout(new BoxLayout(repeatInfoPanel,
				BoxLayout.X_AXIS));

		prepareRepeatInfoTable();
		JScrollPane repeatInfoScrollPane = new JScrollPane(table);

		JPanel repeatParameterPanel = new JPanel();
		repeatParameterPanel.setOpaque(true);
		repeatParameterPanel.setLayout(new BoxLayout(repeatParameterPanel,
				BoxLayout.Y_AXIS));
		repeatParameterPanel.setBackground(new Color(100, 100, 100));
		repeatParameterPanel.setBorder(BorderFactory.createEmptyBorder());

		noAlphabetsLabel = new JLabel("No. Alphabets: " + displayData.length);
		freqFilterLabel = new JLabel("Freq. Threshold: " + 0);

		repeatFreqFilterSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		repeatFreqFilterSlider.setOpaque(false);
		repeatFreqFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				repeatFreqFilterThreshold = repeatFreqFilterSlider.getValue();

				if (repeatFreqFilterSlider.getValueIsAdjusting() == false) {
					filterRepeats();

					table.setModel(new RepeatTableModel(displayData));

					table.getColumnModel().getColumn(0).setWidth(
							table.getWidth() / 3);
					table.getColumnModel().getColumn(1).setWidth(
							table.getWidth() / 2);
					table.getColumnModel().getColumn(2).setWidth(
							table.getWidth() / 6);

					table.getColumnModel().getColumn(0).setCellRenderer(
							new TextAreaRenderer());
					table.getColumnModel().getColumn(1).setCellRenderer(
							new TextAreaRenderer());
					table.getColumnModel().getColumn(2).setCellRenderer(
							new TextAreaRenderer());
				}
				noAlphabetsLabel
						.setText("No. Alphabets: " + displayData.length);
				freqFilterLabel.setText("Freq. Threshold: "
						+ repeatFreqFilterThreshold);

				revalidate();
				repaint();
			}
		});

		sizeFilterLabel = new JLabel("Size. Threshold: " + 0);
		repeatSizeFilterSlider = new JSlider(JSlider.VERTICAL, 0, 10, 1);
		repeatSizeFilterSlider.setOpaque(false);
		repeatSizeFilterSlider.setValue(0);
		repeatSizeFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				repeatSizeFilterThreshold = repeatSizeFilterSlider.getValue();

				if (!repeatSizeFilterSlider.getValueIsAdjusting()) {
					filterRepeats();
					table.setModel(new RepeatTableModel(displayData));

					table.getColumnModel().getColumn(0).setWidth(
							table.getWidth() / 3);
					table.getColumnModel().getColumn(1).setWidth(
							table.getWidth() / 2);
					table.getColumnModel().getColumn(2).setWidth(
							table.getWidth() / 6);

					table.getColumnModel().getColumn(0).setCellRenderer(
							new TextAreaRenderer());
					table.getColumnModel().getColumn(1).setCellRenderer(
							new TextAreaRenderer());
					table.getColumnModel().getColumn(2).setCellRenderer(
							new TextAreaRenderer());
				}

				noAlphabetsLabel
						.setText("No. Alphabets: " + displayData.length);
				sizeFilterLabel.setText("Size. Threshold: "
						+ repeatSizeFilterThreshold);

				revalidate();
				repaint();
			}
		});

		JPanel repeatFreqFilterPanel = new JPanel();
		repeatFreqFilterPanel.setOpaque(true);
		repeatFreqFilterPanel.setBackground(new Color(140, 140, 140));
		repeatFreqFilterPanel.setLayout(new BoxLayout(repeatFreqFilterPanel,
				BoxLayout.Y_AXIS));
		repeatFreqFilterPanel.add(repeatFreqFilterSlider);
		repeatFreqFilterPanel.add(Box.createVerticalStrut(5));
		repeatFreqFilterPanel.add(freqFilterLabel);

		JPanel repeatSizeFilterPanel = new JPanel();
		repeatSizeFilterPanel.setOpaque(true);
		repeatSizeFilterPanel.setBackground(new Color(140, 140, 140));
		repeatSizeFilterPanel.setLayout(new BoxLayout(repeatSizeFilterPanel,
				BoxLayout.Y_AXIS));
		repeatSizeFilterPanel.add(repeatSizeFilterSlider);
		repeatSizeFilterPanel.add(Box.createVerticalStrut(5));
		repeatSizeFilterPanel.add(sizeFilterLabel);

		JPanel repeatFilterPanel = new JPanel();
		repeatFilterPanel.setOpaque(true);
		repeatFilterPanel.setBackground(new Color(40, 40, 40));
		repeatFilterPanel.setLayout(new BoxLayout(repeatFilterPanel,
				BoxLayout.X_AXIS));
		repeatFilterPanel.add(repeatFreqFilterPanel);
		repeatFilterPanel.add(Box.createHorizontalStrut(5));
		repeatFilterPanel.add(repeatSizeFilterPanel);

		repeatParameterPanel.add(noAlphabetsLabel);
		repeatParameterPanel.add(repeatFilterPanel);

		repeatInfoPanel.add(repeatInfoScrollPane);
		repeatInfoPanel.add(Box.createHorizontalStrut(5));
		repeatInfoPanel.add(repeatParameterPanel);

		lowerPanel.add(repeatInfoPanel, BorderLayout.CENTER);

		if (debug)
			Logger.printReturn("Prepare Lower Panel");

		return lowerPanel;
	}

	private JPanel prepareAbstractionLowerPanel() {
		if (debug)
			Logger.printCall("Prepare AbstractionLowerPanel");

		JPanel abstractionLowerPanel = new JPanel();
		abstractionLowerPanel.setOpaque(true);
		abstractionLowerPanel.setBackground(new Color(140, 140, 140));
		abstractionLowerPanel.setLayout(new BoxLayout(abstractionLowerPanel,
				BoxLayout.X_AXIS));

		SlickerButton mergeAbstractionsButton = new SlickerButton(
				"Merge Abstractions");
		mergeAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (debug)
					Logger.printCall("Merge Abstractions");

				/*
				 * Check if there are any modified abstractions; If so, let the
				 * user first click refresh abstractions
				 */
				boolean hasModifiedAbstractions = false;
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (a.isModified) {
						hasModifiedAbstractions = true;
						break;
					}
				}
				if (hasModifiedAbstractions) {
					MainUI
							.getInstance()
							.showGlassDialog(
									"Action Required",
									"Certain Abstractions are modified; Click Refresh Buttons and then Merge Abstractions");
					return;
				}

				TreeSet<TreeSet<String>> allMergeAbstractionsSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				ArrayList<RepeatAbstractionResultConfigurationComponent> removeRepeatAbstractionConfigurationComponentList = new ArrayList<RepeatAbstractionResultConfigurationComponent>();

				TreeSet<String> newMergedAlphabetSet = new TreeSet<String>();
				int noAbstractionsToMerge = 0;
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (a.activeBox.isSelected()) {
						noAbstractionsToMerge++;

						if (debug)
							Logger.println("Merging Alphabet: "
									+ a.selectedActivities);

						newMergedAlphabetSet.addAll(a.selectedActivities);
						allMergeAbstractionsSubsumedElementSet
								.add(a.selectedActivities);
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.selectedActivities)) {
							allMergeAbstractionsSubsumedElementSet
									.addAll(intermediateMaximalElementSubsumedElementSetMap
											.get(a.selectedActivities));
							intermediateMaximalElementSubsumedElementSetMap
									.remove(a.selectedActivities);
						} else {
							// System.out.println("S: In Merge Abstractions: Abstraction to merge "+a.selectedActivities+" not present in intermediate map");
							// System.exit(0);
						}

						removeRepeatAbstractionConfigurationComponentList
								.add(a);
					}

					if (debug) {
						Logger.println("New Merged Alphabet: "
								+ newMergedAlphabetSet);
						Logger
								.println("All Merged Abstractions Subsumed Element Set: "
										+ allMergeAbstractionsSubsumedElementSet);
					}
				}

				/*
				 * Because of the merge, there can be abstractions that now
				 * satisfy the criteria (%common and %diff) for the union of the
				 * merged abstractions; Check for all such abstractions and
				 * remove them
				 */
				HashSet<String> tempSet = new HashSet<String>();
				TreeSet<TreeSet<String>> tempAbstractionSubsumedElementSet;
				int noCommonElements, noDifferentElements;
				boolean isMergedAlphabetSubsumed = false;
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					// check only the abstractions other than the selected ones
					// to merge
					if (!a.activeBox.isSelected()) {
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.selectedActivities)) {
							tempSet.clear();
							tempSet.addAll(a.selectedActivities);
							tempSet.retainAll(newMergedAlphabetSet);
							noCommonElements = tempSet.size();

							if (a.selectedActivities.size() <= newMergedAlphabetSet
									.size()) {
								tempSet.clear();
								tempSet.addAll(a.selectedActivities);
								tempSet.removeAll(newMergedAlphabetSet);
								noDifferentElements = tempSet.size();

								if (noCommonElements >= Math
										.ceil(commonElementsFilterThreshold
												* a.selectedActivities.size())
										&& noDifferentElements <= Math
												.ceil(differentElementsFilterThreshold
														* a.selectedActivities
																.size())) {
									allMergeAbstractionsSubsumedElementSet
											.add(a.selectedActivities);
									allMergeAbstractionsSubsumedElementSet
											.addAll(intermediateMaximalElementSubsumedElementSetMap
													.get(a.selectedActivities));
									removeRepeatAbstractionConfigurationComponentList
											.add(a);

									intermediateMaximalElementSubsumedElementSetMap
											.remove(a.selectedActivities);

									if (debug)
										Logger
												.println(a.selectedActivities
														+ " is now subsumed in the merged alphabet "
														+ newMergedAlphabetSet);
								}
							} else {
								tempSet.clear();
								tempSet.addAll(newMergedAlphabetSet);
								tempSet.removeAll(a.selectedActivities);
								noDifferentElements = tempSet.size();

								if (noCommonElements >= Math
										.ceil(commonElementsFilterThreshold
												* newMergedAlphabetSet.size())
										&& noDifferentElements <= Math
												.ceil(differentElementsFilterThreshold
														* newMergedAlphabetSet
																.size())) {
									tempAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
											new TreeSetComparator());
									tempAbstractionSubsumedElementSet
											.addAll(intermediateMaximalElementSubsumedElementSetMap
													.get(a.selectedActivities));
									tempAbstractionSubsumedElementSet
											.add(newMergedAlphabetSet);
									tempAbstractionSubsumedElementSet
											.addAll(allMergeAbstractionsSubsumedElementSet);

									intermediateMaximalElementSubsumedElementSetMap
											.put(a.selectedActivities,
													tempAbstractionSubsumedElementSet);
									isMergedAlphabetSubsumed = true;

									if (debug)
										Logger.println("newMergedAlphabet "
												+ newMergedAlphabetSet
												+ " is subsumed in "
												+ a.selectedActivities);
								}
							}
						} else {
							// System.out.println("S: In Merge Abstractions..checking abstractions for subsumption in mergedAbstraction: Abstraction "+a.selectedActivities+" not present in intermediate map");
							// System.exit(0);
						}
					}
				}
				if (!isMergedAlphabetSubsumed) {
					if (debug)
						Logger.println("Adding Merged Abstraction: "
								+ newMergedAlphabetSet + " in intermediateMap");

					intermediateMaximalElementSubsumedElementSetMap.put(
							newMergedAlphabetSet,
							allMergeAbstractionsSubsumedElementSet);
					repeatAbstractionResultConfigurationComponentList
							.add(new RepeatAbstractionResultConfigurationComponent(
									"Abs " + noAbstractions++,
									newMergedAlphabetSet));
				}

				for (RepeatAbstractionResultConfigurationComponent a : removeRepeatAbstractionConfigurationComponentList)
					repeatAbstractionResultConfigurationComponentList.remove(a);

				abstractionResultPanel.removeAll();
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList)
					abstractionResultPanel.add(a);

				abstractionCountLabel.setText("No Abstractions "
						+ repeatAbstractionResultConfigurationComponentList
								.size());
				if (debug)
					Logger.println("No Abstractions "
							+ repeatAbstractionResultConfigurationComponentList
									.size());

				revalidate();
				repaint();

				if (debug)
					Logger.printReturn("Merge Abstractions");
			}
		});

		SlickerButton removeAbstractionsButton = new SlickerButton(
				"Remove Abstractions");
		removeAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println("In Remove Abstractions");
				ArrayList<RepeatAbstractionResultConfigurationComponent> removedRepeatAbstractionConfigurationComponentList = new ArrayList<RepeatAbstractionResultConfigurationComponent>();
				TreeSet<TreeSet<String>> removedAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>();
				TreeSet<TreeSet<String>> tempAbstractionSubsumedElementSet;
				boolean isSubsumedAlphabetInOtherAbstraction;
				HashSet<String> tempSet = new HashSet<String>();
				int noCommonElements, noDifferentElements;
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (a.activeBox.isSelected()) {
						if (debug)
							Logger.println("Removing abstraction: "
									+ a.originalAbstractionActivities);
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.originalAbstractionActivities)) {
							removedAbstractionSubsumedElementSet = intermediateMaximalElementSubsumedElementSetMap
									.get(a.originalAbstractionActivities);
							intermediateMaximalElementSubsumedElementSetMap
									.remove(a.originalAbstractionActivities);
							originalAlphabetNewAlphabetMap.put(
									a.originalAbstractionActivities,
									new TreeSet<TreeSet<String>>());

							/*
							 * Check if the subsumed element is in other
							 * abstractions or can be put in other abstractions
							 */
							if (debug)
								Logger
										.println("Removed Abstraction subsumed element set: "
												+ removedAbstractionSubsumedElementSet);

							for (TreeSet<String> subsumedAlphabetRemovedAbstraction : removedAbstractionSubsumedElementSet) {
								isSubsumedAlphabetInOtherAbstraction = false;
								for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
										.keySet()) {
									if (!intermediateMaximalElementSubsumedElementSetMap
											.get(abstractionAlphabet)
											.contains(
													subsumedAlphabetRemovedAbstraction)) {
										if (abstractionAlphabet.size() >= subsumedAlphabetRemovedAbstraction
												.size()) {
											tempSet.clear();
											tempSet.addAll(abstractionAlphabet);
											tempSet
													.retainAll(subsumedAlphabetRemovedAbstraction);
											noCommonElements = tempSet.size();

											tempSet.clear();
											tempSet
													.addAll(subsumedAlphabetRemovedAbstraction);
											tempSet
													.removeAll(abstractionAlphabet);
											noDifferentElements = tempSet
													.size();

											if (noCommonElements >= Math
													.ceil(commonElementsFilterThreshold
															* subsumedAlphabetRemovedAbstraction
																	.size())
													&& noDifferentElements <= Math
															.ceil(differentElementsFilterThreshold
																	* subsumedAlphabetRemovedAbstraction
																			.size())) {
												isSubsumedAlphabetInOtherAbstraction = true;
												tempAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
														new TreeSetComparator());
												tempAbstractionSubsumedElementSet
														.addAll(intermediateMaximalElementSubsumedElementSetMap
																.get(abstractionAlphabet));
												tempAbstractionSubsumedElementSet
														.add(subsumedAlphabetRemovedAbstraction);
												intermediateMaximalElementSubsumedElementSetMap
														.put(
																abstractionAlphabet,
																tempAbstractionSubsumedElementSet);

												if (debug)
													Logger
															.println(subsumedAlphabetRemovedAbstraction
																	+ " subsumed in "
																	+ abstractionAlphabet);
											}
										}
									} else {
										if (debug)
											Logger
													.println(subsumedAlphabetRemovedAbstraction
															+ " subsumed in "
															+ abstractionAlphabet);
									}
								}
								if (!isSubsumedAlphabetInOtherAbstraction) {
									if (debug)
										Logger
												.println(subsumedAlphabetRemovedAbstraction
														+ " not subsumed in any abstraction");
									originalAlphabetNewAlphabetMap.put(
											subsumedAlphabetRemovedAbstraction,
											new TreeSet<TreeSet<String>>());
								}
							}
						} else {
							if (debug)
								Logger
										.println("In Remove Abstractions: intermediate Map doesn't contain key: "
												+ a.originalAbstractionActivities);
						}
						removedRepeatAbstractionConfigurationComponentList
								.add(a);
					}
				}

				for (RepeatAbstractionResultConfigurationComponent a : removedRepeatAbstractionConfigurationComponentList) {
					repeatAbstractionResultConfigurationComponentList.remove(a);
				}

				abstractionResultPanel.removeAll();
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList)
					abstractionResultPanel.add(a);

				abstractionCountLabel.setText("No. Abstractions: "
						+ repeatAbstractionResultConfigurationComponentList
								.size());
				if (debug)
					Logger.println("No. Abstractions: "
							+ repeatAbstractionResultConfigurationComponentList
									.size());

				revalidate();
				repaint();

				if (debug)
					Logger.printReturn("Exiting Remove Abstractions");
			}
		});

		SlickerButton refreshAbstractionsButton = new SlickerButton(
				"Refresh Abstractions");
		refreshAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (debug)
					Logger.printCall("Entering Refresh Abstractions ");

				TreeSet<TreeSet<String>> selectedAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> selectedAbstractionFilteredSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> superAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());

				TreeSet<TreeSet<String>> originalAlphabetNewAlphabetSet;

				ArrayList<RepeatAbstractionResultConfigurationComponent> removeAbstractionConfigurationComponentList = new ArrayList<RepeatAbstractionResultConfigurationComponent>();
				ArrayList<RepeatAbstractionResultConfigurationComponent> addAbstractionConfigurationComponentList = new ArrayList<RepeatAbstractionResultConfigurationComponent>();

				HashSet<String> removedActivitySymbolSet;
				TreeSet<String> tempSet;
				int noCommonElements, noDifferentElements;
				boolean isModifiedAbstractionSubsumed;

				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (a.isModified) {
						isModifiedAbstractionSubsumed = false;

						removedActivitySymbolSet = new HashSet<String>();
						removedActivitySymbolSet
								.addAll(a.originalAbstractionActivities);
						removedActivitySymbolSet
								.removeAll(a.selectedActivities);

						if (debug)
							Logger.println("Has Modified Abstraction: "
									+ a.originalAbstractionActivities + " @ "
									+ a.selectedActivities + " @removed: "
									+ removedActivitySymbolSet);

						/**
						 * Add the originalRepeatALphabet NewRepeatAlphabetMap
						 */
						if (originalAlphabetNewAlphabetMap
								.containsKey(a.originalAbstractionActivities)) {
							originalAlphabetNewAlphabetSet = originalAlphabetNewAlphabetMap
									.get(a.originalAbstractionActivities);
						} else {
							originalAlphabetNewAlphabetSet = new TreeSet<TreeSet<String>>(
									new TreeSetComparator());
						}
						originalAlphabetNewAlphabetSet
								.add(a.selectedActivities);
						originalAlphabetNewAlphabetMap.put(
								a.originalAbstractionActivities,
								originalAlphabetNewAlphabetSet);

						/*
						 * Check if the removal of some symbols in an alphabet
						 * leads to a situation where the new alphabet is
						 * already present/is_a_subset_of in the abstraction
						 * maps If so, then put all manifestations of the
						 * alphabet under the subsumed list of the other
						 * alphabet
						 */

						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.originalAbstractionActivities)) {
							selectedAbstractionSubsumedElementSet.clear();
							selectedAbstractionSubsumedElementSet
									.addAll(intermediateMaximalElementSubsumedElementSetMap
											.get(a.originalAbstractionActivities));

							selectedAbstractionFilteredSubsumedElementSet
									.clear();
							for (TreeSet<String> subsumedRepeatAlphabet : selectedAbstractionSubsumedElementSet) {
								tempSet = new TreeSet<String>();
								tempSet.addAll(subsumedRepeatAlphabet);
								tempSet.removeAll(removedActivitySymbolSet);
								if (tempSet.size() > 0) {
									selectedAbstractionFilteredSubsumedElementSet
											.add(tempSet);
								}

								/*
								 * put the originalAlphabet NewAlphabetMap there
								 * can be no change of the subsumedAlphabet
								 * (because, the removed symbols are not part of
								 * the subsumed alphabet) or the subsumed
								 * alphabet vanishes completely because of the
								 * removal
								 */
								if (!subsumedRepeatAlphabet.equals(tempSet)
										&& tempSet.size() > 0) {
									if (originalAlphabetNewAlphabetMap
											.containsKey(subsumedRepeatAlphabet)) {
										originalAlphabetNewAlphabetSet = originalAlphabetNewAlphabetMap
												.get(subsumedRepeatAlphabet);
									} else {
										originalAlphabetNewAlphabetSet = new TreeSet<TreeSet<String>>(
												new TreeSetComparator());
									}
									originalAlphabetNewAlphabetSet.add(tempSet);
									originalAlphabetNewAlphabetMap.put(
											subsumedRepeatAlphabet,
											originalAlphabetNewAlphabetSet);
								}
							}
							if (debug) {
								Logger
										.println("SelectedAbstractionSubsumedElementSet: "
												+ selectedAbstractionSubsumedElementSet);
								Logger
										.println("SelectedAbstractionFilteredSubsumedElementSet @ "
												+ selectedAbstractionFilteredSubsumedElementSet);
							}

							/*
							 * Check if the abstraction map contains keys that
							 * either subsumes the filtered activity list or is
							 * within the set limits of approximation
							 */
							if (debug)
								Logger
										.println("Checking whether the filtered abstraction is subsumed in any other abstraction");

							for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
									.keySet()) {
								if (!abstractionAlphabet
										.equals(a.originalAbstractionActivities)
										&& abstractionAlphabet.size() > a.selectedActivities
												.size()) {
									tempSet = new TreeSet<String>();
									tempSet.addAll(abstractionAlphabet);
									tempSet.retainAll(a.selectedActivities);
									noCommonElements = tempSet.size();

									tempSet.clear();
									tempSet.addAll(a.selectedActivities);
									tempSet.removeAll(abstractionAlphabet);
									noDifferentElements = tempSet.size();

									if (noCommonElements >= Math
											.ceil(commonElementsFilterThreshold
													* a.selectedActivities
															.size())
											&& noDifferentElements <= Math
													.ceil(differentElementsFilterThreshold
															* a.selectedActivities
																	.size())) {
										superAbstractionSubsumedElementSet = intermediateMaximalElementSubsumedElementSetMap
												.get(abstractionAlphabet);
										superAbstractionSubsumedElementSet
												.add(a.selectedActivities);
										superAbstractionSubsumedElementSet
												.addAll(selectedAbstractionFilteredSubsumedElementSet);
										intermediateMaximalElementSubsumedElementSetMap
												.put(abstractionAlphabet,
														superAbstractionSubsumedElementSet);

										isModifiedAbstractionSubsumed = true;
										if (debug)
											Logger
													.println("Modified Abstraction: "
															+ a.selectedActivities
															+ " subsumed in "
															+ abstractionAlphabet);
									}
								}
							}
						} else {
							if (debug)
								Logger.println("In Refresh Abstractions: "
										+ a.originalAbstractionActivities
										+ " not in intermediateMap");
							System.exit(0);
						}

						/**
						 * Remove abstraction from intermediateMap
						 */
						intermediateMaximalElementSubsumedElementSetMap
								.remove(a.originalAbstractionActivities);
						removeAbstractionConfigurationComponentList.add(a);
						/*
						 * Add the modified abstraction provided it is not
						 * subsumed in another abstraction
						 */
						if (!isModifiedAbstractionSubsumed) {
							intermediateMaximalElementSubsumedElementSetMap
									.put(a.selectedActivities,
											selectedAbstractionFilteredSubsumedElementSet);
							addAbstractionConfigurationComponentList
									.add(new RepeatAbstractionResultConfigurationComponent(
											"Abs " + noAbstractions++,
											a.selectedActivities));
						}
						a.isModified = false;
					}
				}
				for (RepeatAbstractionResultConfigurationComponent a : removeAbstractionConfigurationComponentList)
					repeatAbstractionResultConfigurationComponentList.remove(a);

				for (RepeatAbstractionResultConfigurationComponent b : addAbstractionConfigurationComponentList)
					repeatAbstractionResultConfigurationComponentList.add(b);

				abstractionResultPanel.removeAll();
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList)
					abstractionResultPanel.add(a);
				abstractionCountLabel.setText("No. Abstractions: "
						+ repeatAbstractionResultConfigurationComponentList
								.size());

				if (debug)
					Logger.println("No. Abstractions: "
							+ repeatAbstractionResultConfigurationComponentList
									.size());

				revalidate();
				repaint();

				if (debug)
					Logger.printReturn("Exiting Refresh Abstractions");
			}
		});

		SlickerButton saveAbstractionsButton = new SlickerButton(
				"Save Abstractions");
		saveAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.out.println("Save Abstractions Clicked");
			}
		});

		SlickerButton transformLogButton = new SlickerButton("Transform Log");
		transformLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (debug)
					Logger.printCall("Entering Transform Log");
				/*
				 * Check if there are any modified abstractions; If so, then
				 * force the user to click "Refresh Abstractions" before
				 * clicking transform log
				 */
				boolean hasModifiedAbstractions = false;
				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (a.isModified) {
						hasModifiedAbstractions = true;
						break;
					}
				}

				if (hasModifiedAbstractions) {
					MainUI
							.getInstance()
							.showGlassDialog(
									"Action Required",
									"Certain abstractions were modified; Click Refresh Abstractions before Transforming Log");
					return;
				}

				/*
				 * Check whether there exist duplicate abstraction names; If so,
				 * let the user modify them before transform log
				 */

				HashSet<String> abstractionNameSet = new HashSet<String>();
				HashSet<String> duplicateAbstractionNameSet = new HashSet<String>();

				for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList) {
					if (abstractionNameSet.contains(a.getAbstractionName())) {
						duplicateAbstractionNameSet.add(a.getAbstractionName());
					} else {
						abstractionNameSet.add(a.getAbstractionName());
					}
				}

				if (duplicateAbstractionNameSet.size() > 0) {
					MainUI.getInstance().showGlassDialog(
							"Action Required",
							"There exist duplicate abstraction names; Resolve them before Transform Log \n"
									+ duplicateAbstractionNameSet);
					return;
				}

				/*
				 * Pop-up a file save dialog
				 */
				JFrame saveFileFrame = new JFrame();
				File file = new File("RepeatAbstraction.mxml.gz");
				JFileChooser fc = new JFileChooser();

				// Start in current directory
				fc.setCurrentDirectory(new File("."));

				// Set to a default name for save.
				fc.setSelectedFile(file);

				// Open chooser dialog
				int result = fc.showSaveDialog(saveFileFrame);

				if (result == JFileChooser.CANCEL_OPTION) {
					return;
				} else if (result == JFileChooser.APPROVE_OPTION) {
					file = fc.getSelectedFile();
					if (file.exists()) {
						int response = JOptionPane.showConfirmDialog(null,
								"Overwrite existing file?",
								"Confirm Overwrite",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.CANCEL_OPTION)
							return;
					}
					transformLogFileName = file.getAbsolutePath();
					System.out.println("FileName: " + file.getAbsolutePath());
				}

				transformLog();

				if (debug)
					Logger.printReturn("Exiting Transform Log");
			}
		});

		abstractionLowerPanel.add(abstractionCountLabel);
		abstractionLowerPanel.add(Box.createHorizontalStrut(100));
		abstractionLowerPanel.add(mergeAbstractionsButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(removeAbstractionsButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(refreshAbstractionsButton);

		// abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		// abstractionLowerPanel.add(saveAbstractionsButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(transformLogButton);

		if (debug)
			Logger.printReturn("Prepare AbstractionLowerPanel");

		return abstractionLowerPanel;
	}

	@SuppressWarnings("unchecked")
	private void filterRepeats() {
		if (debug)
			Logger.printCall("Entering Filter Repeats");

		int noEntries = originalData.length;
		int noEntriesAboveThreshold = 0;
		for (int i = 0; i < noEntries; i++) {
			if (((Integer) originalData[i][2]).intValue() > repeatFreqFilterThreshold
					&& ((TreeSet<String>) originalData[i][0]).size() > repeatSizeFilterThreshold)
				noEntriesAboveThreshold++;
		}

		displayData = new Object[noEntriesAboveThreshold][3];
		int index = 0;
		for (int i = 0; i < noEntries; i++) {
			if (((Integer) originalData[i][2]).intValue() > repeatFreqFilterThreshold
					&& ((TreeSet<String>) originalData[i][0]).size() > repeatSizeFilterThreshold) {
				displayData[index][0] = originalData[i][0];
				displayData[index][1] = originalData[i][1];
				displayData[index][2] = originalData[i][2];
				index++;
			}
		}

		if (debug)
			Logger.printReturn("Exiting Filter Repeats");
	}

	private void prepareAbstractionResult(
			List<TreeSet<String>> maximalElementList) {
		if (debug)
			Logger.printCall("Entering Prepare Abstraction Result");

		ArrayList<String> abstractionNameList = new ArrayList<String>();
		ArrayList<Set<String>> abstractionAlphabetList = new ArrayList<Set<String>>();

		noAbstractions = 0;
		for (TreeSet<String> maximalElement : maximalElementList) {
			abstractionNameList.add("Abs " + noAbstractions++);
			abstractionAlphabetList.add(maximalElement);
		}

		if (abstractionResultPanel != null)
			abstractionResultPanel.removeAll();
		else
			abstractionResultPanel = new JPanel();

		abstractionResultPanel.setBackground(colorBg);
		abstractionResultPanel.setLayout(new BoxLayout(abstractionResultPanel,
				BoxLayout.Y_AXIS));
		abstractionResultPanel.setBorder(BorderFactory.createEmptyBorder());

		repeatAbstractionResultConfigurationComponentList.clear();
		RepeatAbstractionResultConfigurationComponent rarcc;

		noAbstractions = abstractionNameList.size();
		for (int i = 0; i < noAbstractions; i++) {
			rarcc = new RepeatAbstractionResultConfigurationComponent(
					abstractionNameList.get(i), abstractionAlphabetList.get(i));
			repeatAbstractionResultConfigurationComponentList.add(rarcc);
			abstractionResultPanel.add(rarcc);
		}

		if (abstractionResultScrollPane == null)
			abstractionResultScrollPane = new JScrollPane(
					abstractionResultPanel);

		abstractionResultScrollPane.setBackground(colorBg);
		abstractionResultScrollPane.setBorder(BorderFactory
				.createLineBorder(new Color(90, 90, 90)));
		abstractionResultScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		abstractionResultScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		abstractionResultScrollPane.getVerticalScrollBar()
				.setBlockIncrement(25);
		abstractionResultScrollPane.getVerticalScrollBar().setUnitIncrement(25);

		abstractionCountLabel.setText("No. Abstractions: "
				+ repeatAbstractionResultConfigurationComponentList.size());

		originalAlphabetNewAlphabetMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();

		if (debug)
			Logger.printReturn("Exiting Prepare Abstraction Result");
	}

	private void transformLog() {
		getAbstractionNames();
		processAlphabetRepeatMap();
		processLogWithAbstractions();
	}

	/**
	 * Get the abstraction names for the abstraction alphabet; this is required
	 * for performance enhancements; While transforming the log, when a
	 * subsequence matching a repeat pattern is found, it needs to be replaced
	 * with the abstraction name In case this map is not stored, we need a
	 * sequential scan over all the abstraction alphabets to find out the name
	 */

	private void getAbstractionNames() {
		if (debug)
			Logger.printCall("Entering Get Abstraction Names");

		abstractionAlphabetAbstractionNameMap = new HashMap<TreeSet<String>, String>();
		for (RepeatAbstractionResultConfigurationComponent a : repeatAbstractionResultConfigurationComponentList)
			abstractionAlphabetAbstractionNameMap.put(a.selectedActivities, a
					.getAbstractionName());

		if (debug)
			Logger.printReturn("Exiting Get Abstraction Names");
	}

	private void processAlphabetRepeatMap() {
		if (debug)
			Logger.printCall("Entering Process Alphabet Repeat Map");

		/*
		 * the user control over the abstractions is only on the alphabets and
		 * not on the repeats there could have been cases where certain
		 * alphabets have been completely removed and cases where certain
		 * symbols in the alphabet have been removed; Now, repeats which
		 * corresponded to the original alphabets no longer can qualify as
		 * repeats in its entirety
		 * 
		 * for e.g., let {a, b, c} be a repeat alphabet with repeats 'abc'
		 * 'bac'; suppose the user had chosen to remove 'c' from {a, b, c}, then
		 * the new repeats would be 'ab' and 'ba'
		 * 
		 * this mapping between original and modified alphabet is available in
		 * the orignalAlphabetNewAlphabetMap for alphabets that have been
		 * completely removed, we would have a null (empty set) association for
		 * the newAlphabet
		 */

		String replaceSymbol = "#";
		for (int i = 1; i < encodingLength; i++)
			replaceSymbol += "#";

		if (debug)
			Logger.println("Actual AlphabetRepeatSize: "
					+ alphabetRepeatMap.size());

		// FileIO io = new FileIO();
		// io.writeToFile(outputDir, "originalAlphabetRepeatMap.txt",
		// alphabetRepeatMap,delim);

		if (debug) {
			Logger.println("OriginalNewAlphabetMapSize: "
					+ originalAlphabetNewAlphabetMap.size());
			Logger
					.println("Processing originalNewALphabetMap for alphabets and the corresponding repeats that have been modified");
		}

		TreeSet<TreeSet<String>> newAlphabetSet;
		TreeSet<String> originalAlphabetRepeatSet, mrAlphabetRepeatSet, mrAlphabet;
		TreeSet<String> removedSymbolSet = new TreeSet<String>();
		String modifiedRepeat;
		String[] modifiedRepeatSplit;
		int mrLength;
		for (TreeSet<String> originalAlphabet : originalAlphabetNewAlphabetMap
				.keySet()) {
			if (alphabetRepeatMap.containsKey(originalAlphabet)) {
				originalAlphabetRepeatSet = alphabetRepeatMap
						.get(originalAlphabet);
				newAlphabetSet = originalAlphabetNewAlphabetMap
						.get(originalAlphabet);

				if (debug)
					Logger.println("OriginalAlphabet: " + originalAlphabet
							+ " @OrgAlpRepSet: " + originalAlphabetRepeatSet);

				if (newAlphabetSet.size() > 1) {
					if (debug)
						Logger
								.println("Original Alphabet: "
										+ originalAlphabet
										+ " has more than one assocation for newAlphabet "
										+ newAlphabetSet);
				}

				for (TreeSet<String> newAlphabet : newAlphabetSet) {
					removedSymbolSet.clear();
					removedSymbolSet.addAll(originalAlphabet);
					removedSymbolSet.removeAll(newAlphabet);

					for (String originalRepeat : originalAlphabetRepeatSet) {
						modifiedRepeat = originalRepeat;
						for (String removedSymbol : removedSymbolSet)
							modifiedRepeat = modifiedRepeat.replaceAll(
									removedSymbol, replaceSymbol);

						modifiedRepeatSplit = modifiedRepeat
								.split(replaceSymbol);
						for (String mr : modifiedRepeatSplit) {
							mrLength = mr.trim().length() / encodingLength;
							if (mrLength > 0) {
								mrAlphabet = new TreeSet<String>();
								for (int i = 0; i < mrLength; i++) {
									mrAlphabet.add(mr.substring(i
											* encodingLength, (i + 1)
											* encodingLength));
								}
								if (alphabetRepeatMap.containsKey(mrAlphabet)) {
									mrAlphabetRepeatSet = alphabetRepeatMap
											.get(mrAlphabet);
								} else {
									mrAlphabetRepeatSet = new TreeSet<String>();
								}
								mrAlphabetRepeatSet.add(mr);
								alphabetRepeatMap.put(mrAlphabet,
										mrAlphabetRepeatSet);

								if (debug)
									Logger.println("Adding " + mrAlphabet
											+ " @ " + mrAlphabetRepeatSet
											+ " in alphabetRepeatMap");
							}
						}
					}
				}
			} else {
				if (debug)
					Logger
							.println("In ProcessAlphabetRepeatMap: the alphabetRepeatMap doesn't contain the alphabet "
									+ originalAlphabet);
			}
		}

		/*
		 * Remove the modified alphabets from the original alphabetRepeatMap
		 */
		if (debug)
			Logger
					.println("Removing alphabets that have been modified from the alphabetRepeatMap");

		for (TreeSet<String> originalAlphabet : originalAlphabetNewAlphabetMap
				.keySet())
			alphabetRepeatMap.remove(originalAlphabet);

		// io.writeToFile(outputDir, "alteredAlphabetRepeatMap.txt",
		// alphabetRepeatMap,"^");

		originalAlphabetNewAlphabetMap.clear();

		if (debug)
			Logger.println("Altered AlphabetRepeatMap Size: "
					+ alphabetRepeatMap.size());

		getRepeatAlphabetAbstractionNameMap();

		if (debug)
			Logger.printReturn("Exiting Process Alphabet Repeat Map");
	}

	private void getRepeatAlphabetAbstractionNameMap() {

		if (debug)
			Logger
					.printCall("Entering Get Repeat Alphabet Abstraction Name Map");
		/*
		 * Prepare the repeat alphabet abstractionNameSet Map; Not all alphabets
		 * have an abstraction. Those symbols would be left untouched in the
		 * transformation of logs. For each alphabet, get the abstraction name;
		 * There can be cases where a repeat alphabet contributes to more than
		 * one abstraction; Get all the abstractions for which the repeat
		 * alphabet contributes to;
		 */

		alphabetAbstractionNameSetMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		TreeSet<String> alphabetAbstractionNameSet;
		TreeSet<TreeSet<String>> abstractionAlphabetSubsumedElementSet = new TreeSet<TreeSet<String>>(
				new TreeSetComparator());

		for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
				.keySet()) {
			if (alphabetRepeatMap.containsKey(abstractionAlphabet)) {
				if (alphabetAbstractionNameSetMap
						.containsKey(abstractionAlphabet)) {
					alphabetAbstractionNameSet = alphabetAbstractionNameSetMap
							.get(abstractionAlphabet);
				} else {
					alphabetAbstractionNameSet = new TreeSet<String>();
				}
				alphabetAbstractionNameSet
						.add(abstractionAlphabetAbstractionNameMap
								.get(abstractionAlphabet));
				alphabetAbstractionNameSetMap.put(abstractionAlphabet,
						alphabetAbstractionNameSet);
			}

			/*
			 * Process for all alphabets that are subsumed by this abstraction
			 * alphabet
			 */
			abstractionAlphabetSubsumedElementSet.clear();
			abstractionAlphabetSubsumedElementSet
					.addAll(intermediateMaximalElementSubsumedElementSetMap
							.get(abstractionAlphabet));

			for (TreeSet<String> subsumedAlphabet : abstractionAlphabetSubsumedElementSet) {
				if (alphabetRepeatMap.containsKey(subsumedAlphabet)) {
					if (alphabetAbstractionNameSetMap
							.containsKey(subsumedAlphabet)) {
						alphabetAbstractionNameSet = alphabetAbstractionNameSetMap
								.get(subsumedAlphabet);
					} else {
						alphabetAbstractionNameSet = new TreeSet<String>();
					}
					alphabetAbstractionNameSet
							.add(abstractionAlphabetAbstractionNameMap
									.get(abstractionAlphabet));
					alphabetAbstractionNameSetMap.put(subsumedAlphabet,
							alphabetAbstractionNameSet);
				}
			}
		}

		if (debug)
			Logger.println("AlphabetAbstractionNameSetMap Size: "
					+ alphabetAbstractionNameSetMap.size());

		/**
		 * Print all alphabets with more than one abstraction
		 */
		if (debug)
			Logger.println("Alphabets with more than one Abstraction Start");
		for (TreeSet<String> alphabet : alphabetAbstractionNameSetMap.keySet()) {
			if (debug && alphabetAbstractionNameSetMap.get(alphabet).size() > 1) {
				Logger.println(alphabet + " @ "
						+ alphabetAbstractionNameSetMap.get(alphabet));
			}
		}
		if (debug)
			Logger.println("Alphabets with more than one Abstraction End");

		/*
		 * Find alphabets for which there is no abstraction name associated
		 */

		HashSet<TreeSet<String>> missedAlphabetSet = new HashSet<TreeSet<String>>();
		missedAlphabetSet.addAll(alphabetRepeatMap.keySet());
		missedAlphabetSet.removeAll(alphabetAbstractionNameSetMap.keySet());

		if (debug) {
			Logger.println("No. Alphabets with NO Abstraction Name: "
					+ missedAlphabetSet.size());
			Logger
					.println("Checking if alphabets with no abstraction name is subsumed in any abstraction alphabet");
		}

		/*
		 * Check if the missed alphabet is subsumed in any abstractionAlphabet
		 * under the constraints of noCommonElements and noDifferentElements
		 */
		missedAlphabetAbstractionNameSetMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		TreeSet<String> missedAlphabetAbstractionNameSet;
		HashSet<String> tempSet = new HashSet<String>();
		int noCommonElements, noDifferentElements;
		for (TreeSet<String> missedAlphabet : missedAlphabetSet) {
			for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
					.keySet()) {
				tempSet.clear();
				tempSet.addAll(missedAlphabet);
				tempSet.retainAll(abstractionAlphabet);
				noCommonElements = tempSet.size();

				tempSet.clear();
				tempSet.addAll(missedAlphabet);
				tempSet.removeAll(abstractionAlphabet);
				noDifferentElements = tempSet.size();

				if (noCommonElements >= Math.ceil(commonElementsFilterThreshold
						* missedAlphabet.size())
						&& noDifferentElements <= Math
								.ceil(differentElementsFilterThreshold
										* missedAlphabet.size())) {
					if (missedAlphabetAbstractionNameSetMap
							.containsKey(missedAlphabet)) {
						missedAlphabetAbstractionNameSet = missedAlphabetAbstractionNameSetMap
								.get(missedAlphabet);
					} else {
						missedAlphabetAbstractionNameSet = new TreeSet<String>();
					}
					missedAlphabetAbstractionNameSet
							.add(abstractionAlphabetAbstractionNameMap
									.get(abstractionAlphabet));
					missedAlphabetAbstractionNameSetMap.put(missedAlphabet,
							missedAlphabetAbstractionNameSet);

					if (debug)
						Logger.println("missed Alphabet: " + missedAlphabet
								+ " subsumed in " + abstractionAlphabet);
				}
			}
		}

		if (debug)
			Logger.println("No. resolved Missed Alphabets: "
					+ missedAlphabetAbstractionNameSetMap.size());

		/*
		 * Find how many alphabets still miss an abstraction name
		 */
		missedAlphabetSet.removeAll(missedAlphabetAbstractionNameSetMap
				.keySet());

		if (debug)
			Logger.println("No. Alphabets with NO abstraction name (Refined): "
					+ missedAlphabetSet.size());

		/*
		 * Find the repeatRepeatAlphabetMap; this is required for quicker
		 * substitution when processing logs Ignore all repeats for which we
		 * couldn't find an abstraction name
		 */
		if (debug)
			Logger.println("Finding Repeat Alphabet Map");

		repeatAlphabetMap = new HashMap<String, TreeSet<String>>();
		TreeSet<String> alphabetRepeatSet;
		for (TreeSet<String> alphabet : alphabetRepeatMap.keySet()) {
			if (!missedAlphabetSet.contains(alphabet)) {
				alphabetRepeatSet = alphabetRepeatMap.get(alphabet);
				for (String repeat : alphabetRepeatSet) {
					repeatAlphabetMap.put(repeat, alphabet);
				}
			}
		}
		// FileIO io = new FileIO();
		// io.writeToFile(outputDir, "alphabetAbstractionNameSetMap.txt",
		// alphabetAbstractionNameSetMap,delim);
		// io.writeToFile(outputDir, "missedAlphabetAbstractionNameSetMap.txt",
		// missedAlphabetAbstractionNameSetMap, delim);
		// io.writeToFile(outputDir, "alphabetsNOAbstraction.txt",
		// missedAlphabetSet);
		// io.writeToFile(outputDir, "repeatAlphabetMap.txt",
		// repeatAlphabetMap,delim);
		if (debug) {
			Logger.println("No. Repeats: " + repeatAlphabetMap.size());
			Logger.println("Finding startSymbolRepeatSetMap");
		}

		/*
		 * Define equivalence class of repeats that start with a particular
		 * symbol
		 */

		startSymbolRepeatSetMap = new HashMap<String, TreeSet<String>>();
		TreeSet<String> startSymbolRepeatSet;
		String startSymbol;
		for (String repeat : repeatAlphabetMap.keySet()) {
			startSymbol = repeat.substring(0, encodingLength);
			if (startSymbolRepeatSetMap.containsKey(startSymbol)) {
				startSymbolRepeatSet = startSymbolRepeatSetMap.get(startSymbol);
			} else {
				startSymbolRepeatSet = new TreeSet<String>(
						new DescendingStrLengthComparator());
			}
			startSymbolRepeatSet.add(repeat);
			startSymbolRepeatSetMap.put(startSymbol, startSymbolRepeatSet);
		}

		// io.writeToFile(outputDir, "startSymbolRepeatSetMap.txt",
		// startSymbolRepeatSetMap,delim);

		if (debug)
			Logger
					.printReturn("Exiting Get Repeat Alphabet Abstraction Name Map");
	}

	private void processLogWithAbstractions() {
		if (debug)
			Logger.printCall("Entering Process Log With Abstractions");

		HashSet<String> activitiesInAbstractionSet = new HashSet<String>();
		for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
				.keySet()) {
			activitiesInAbstractionSet.addAll(abstractionAlphabet);
		}

		if (debug)
			Logger.println("No. Activities in Abstraction: "
					+ activitiesInAbstractionSet.size());

		LogPersistencyStream persistency = null;
		BufferedOutputStream out;
		String name, description, source;
		Process process;

		try {
			out = new BufferedOutputStream(new GZIPOutputStream(
					new FileOutputStream(transformLogFileName)));
			persistency = new LogPersistencyStream(out, false);

			process = RepeatAbstractionUI.log.getProcess(0);

			name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}

			description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}

			source = RepeatAbstractionUI.log.getLogSummary().getSource()
					.getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}

			persistency.startLogfile(name, description, source);
			persistency
					.startProcess(name, description, process.getAttributes());

		} catch (IOException e) {
			e.printStackTrace();
		} catch (LogException e) {
			e.printStackTrace();
		}

		int noProcessInstances = RepeatAbstractionUI.log.numberOfInstances();
		ProcessInstance currentProcessInstance;
		ArrayList<String> wfmeNameList = new ArrayList<String>();
		AuditTrailEntryList ateList;

		String currentCharStream, modifiedCharStream, currentSymbol, innerSymbol;
		int currentCharStreamLength;

		modifiedATEList = new ArrayList<org.processmining.lib.mxml.AuditTrailEntry>();
		org.processmining.lib.mxml.AuditTrailEntry ate;

		TreeSet<String> startSymbolRepeatSet, innerStartSymbolRepeatSet;
		TreeSet<String> repeatAlphabet = new TreeSet<String>();
		String matchingSequence, innerMatchingSequence;
		boolean repeatExists, innerRepeatExists;
		int repeatLength, innerRepeatLength, matchingSequenceLength, innerMatchingSequenceLength, noMatches, noInnerMatches;
		Pattern pattern, innerPattern;
		Matcher matcher, innerMatcher;

		resolvedAlphabetAbstractionNameSetMap = new HashMap<TreeSet<String>, TreeSet<String>>();

		if (debug)
			Logger.println("No. Process Instances: " + noProcessInstances);

		try {
			process = RepeatAbstractionUI.log.getProcess(0);
			for (int i = 0; i < noProcessInstances; i++) {
				currentProcessInstance = process.getInstance(i);

				name = currentProcessInstance.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcessInstance";
				}

				description = currentProcessInstance.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}

				persistency.startProcessInstance(name, description,
						currentProcessInstance.getAttributes());

				wfmeNameList.clear();
				ateList = currentProcessInstance.getAuditTrailEntryList();
				int noAuditTrailEntries = ateList.size();

				for (int j = 0; j < noAuditTrailEntries; j++)
					wfmeNameList.add(ateList.get(j).getName());

				modifiedATEList.clear();

				currentCharStream = RepeatAbstractionUI.charStreamsList.get(i);
				currentCharStreamLength = currentCharStream.length()
						/ encodingLength;

				if (debug) {
					FileIO io = new FileIO();
					io.writeToFile(outputDir, "PI_" + i + ".txt", wfmeNameList);
				}

				if (currentCharStreamLength != wfmeNameList.size()) {
					if (debug) {
						Logger
								.println("Lengths not matching between charStream and wfmeNameList for Instance: "
										+ (i + 1));
					}
					System.exit(0);
				}

				if (debug) {
					Logger.println("Processing Instance: " + i);
					Logger.println(currentCharStream + " @ "
							+ currentCharStreamLength);
				}

				modifiedCharStream = "";
				for (int j = 0; j < currentCharStreamLength; j++) {
					currentSymbol = currentCharStream.substring(j
							* encodingLength, (j + 1) * encodingLength);
					if (activitiesInAbstractionSet.contains(currentSymbol)) {
						/*
						 * check if there exist a repeat starting at this symbol
						 */
						repeatExists = false;
						if (startSymbolRepeatSetMap.containsKey(currentSymbol)) {
							startSymbolRepeatSet = startSymbolRepeatSetMap
									.get(currentSymbol);

							for (String repeat : startSymbolRepeatSet) {
								pattern = Pattern.compile("(" + repeat
										+ "){1,}");
								matcher = pattern.matcher(currentCharStream);

								// check if a match exists at this position
								if (matcher.find(j * encodingLength)
										&& matcher.start() == j
												* encodingLength) {
									repeatExists = true;

									matchingSequence = matcher.group();
									matchingSequenceLength = matchingSequence
											.length()
											/ encodingLength;
									repeatLength = repeat.length()
											/ encodingLength;
									noMatches = matchingSequenceLength
											/ repeatLength;

									if (debug)
										Logger
												.println("Repeat " + repeat
														+ " exists at pos: "
														+ j + "; No. Matches: "
														+ noMatches);

									/*
									 * Check if there exists a longer repeats
									 * within this matching sequence
									 */
									innerRepeatExists = false;
									for (int k = j + 1; k < j
											+ matchingSequenceLength; k++) {
										innerSymbol = currentCharStream
												.substring(
														k * encodingLength,
														(k + 1)
																* encodingLength);
										if (startSymbolRepeatSetMap
												.containsKey(innerSymbol)) {
											innerStartSymbolRepeatSet = startSymbolRepeatSetMap
													.get(innerSymbol);
											for (String innerRepeat : innerStartSymbolRepeatSet) {
												if (innerRepeat.length() < repeat
														.length()) {
													// the inner repeat is
													// shorter than the current
													// repeat;
													break;
												}
												innerPattern = Pattern
														.compile("("
																+ innerRepeat
																+ "){1,}");
												innerMatcher = innerPattern
														.matcher(currentCharStream);

												if (innerMatcher.find(k
														* encodingLength)
														&& innerMatcher.start() == k
																* encodingLength) {
													innerMatchingSequence = innerMatcher
															.group();
													innerMatchingSequenceLength = innerMatchingSequence
															.length()
															/ encodingLength;

													innerRepeatLength = innerRepeat
															.length()
															/ encodingLength;
													noInnerMatches = innerMatchingSequenceLength
															/ innerRepeatLength;

													if (debug)
														Logger
																.println("Inner Repeat "
																		+ innerRepeat
																		+ " Exists at Pos "
																		+ k
																		+ "; No. Inner Matches: "
																		+ noInnerMatches);

													if (noInnerMatches > noMatches) {
														innerRepeatExists = true;
														noMatches = (k - j)
																/ repeatLength;
														if (noMatches >= 1) {
															repeatAlphabet
																	.clear();
															repeatAlphabet
																	.addAll(repeatAlphabetMap
																			.get(repeat));

															ate = new org.processmining.lib.mxml.AuditTrailEntry();
															ate
																	.addAttribute(
																			"startIndex",
																			j
																					+ "");
															ate
																	.addAttribute(
																			"endIndex",
																			(k - 1)
																					+ "");
															if (alphabetAbstractionNameSetMap
																	.containsKey(repeatAlphabet)) {
																if (alphabetAbstractionNameSetMap
																		.get(
																				repeatAlphabet)
																		.size() == 1) {
																	ate
																			.setWorkflowModelElement(alphabetAbstractionNameSetMap
																					.get(
																							repeatAlphabet)
																					.toString()
																					.replaceAll(
																							"\\[",
																							"")
																					.replaceAll(
																							"\\]",
																							""));
																	ate
																			.addAttribute(
																					"resolved",
																					"true");
																} else {
																	ate
																			.setWorkflowModelElement(repeat);
																	ate
																			.addAttribute(
																					"resolved",
																					"false");
																}
																modifiedCharStream += repeat;
																if (debug)
																	Logger
																			.println("m: "
																					+ modifiedCharStream
																					+ " @ "
																					+ ate
																							.getWorkflowModelElement()
																					+ " @ "
																					+ ate
																							.getAttributes()
																							.get(
																									"resolved"));
															} else if (missedAlphabetAbstractionNameSetMap
																	.containsKey(repeatAlphabet)) {
																if (missedAlphabetAbstractionNameSetMap
																		.get(
																				repeatAlphabet)
																		.size() == 1) {
																	ate
																			.setWorkflowModelElement(missedAlphabetAbstractionNameSetMap
																					.get(
																							repeatAlphabet)
																					.toString()
																					.replaceAll(
																							"\\[",
																							"")
																					.replaceAll(
																							"\\]",
																							""));
																	ate
																			.addAttribute(
																					"resolved",
																					"true");
																} else {
																	ate
																			.setWorkflowModelElement(repeat);
																	ate
																			.addAttribute(
																					"resolved",
																					"false");
																}
																modifiedCharStream += repeat;
																if (debug)
																	Logger
																			.println("m: "
																					+ modifiedCharStream
																					+ " @ "
																					+ ate
																							.getWorkflowModelElement()
																					+ " @ "
																					+ ate
																							.getAttributes()
																							.get(
																									"resolved"));
															} else {
																if (debug)
																	Logger
																			.println("Repeat alphabet "
																					+ repeatAlphabet
																					+ " doesn't have a name map");
																System.exit(0);
															}
															modifiedATEList
																	.add(ate);
														} else {
															/*
															 * It can be the
															 * case that there
															 * exist only one
															 * iteration of the
															 * repeat,but a
															 * longer repeat
															 * start within that
															 * so the noMatches
															 * would be zero; in
															 * such a case, we
															 * need to copy the
															 * individual
															 * symbols until the
															 * longer repeat
															 */
															String subStr = currentCharStream
																	.substring(
																			j
																					* encodingLength,
																			k
																					* encodingLength);
															int subStrLength = subStr
																	.length()
																	/ encodingLength;
															repeatAlphabet
																	.clear();
															for (int jj = 0; jj < subStrLength; jj++)
																repeatAlphabet
																		.add(subStr
																				.substring(
																						jj
																								* encodingLength,
																						(jj + 1)
																								* encodingLength));

															ate = new org.processmining.lib.mxml.AuditTrailEntry();
															ate
																	.addAttribute(
																			"startIndex",
																			j
																					+ "");
															ate
																	.addAttribute(
																			"endIndex",
																			(k - 1)
																					+ "");
															if (alphabetAbstractionNameSetMap
																	.containsKey(repeatAlphabet)) {
																// the whole
																// substring
																// defines an
																// abstraction
																if (alphabetAbstractionNameSetMap
																		.get(
																				repeatAlphabet)
																		.size() == 1) {
																	ate
																			.setWorkflowModelElement(alphabetAbstractionNameSetMap
																					.get(
																							repeatAlphabet)
																					.toString()
																					.replaceAll(
																							"\\[",
																							"")
																					.replaceAll(
																							"\\]",
																							""));
																	ate
																			.addAttribute(
																					"resolved",
																					"true");
																} else {
																	ate
																			.setWorkflowModelElement(subStr);
																	ate
																			.addAttribute(
																					"resolved",
																					"false");
																}
															} else if (missedAlphabetAbstractionNameSetMap
																	.containsKey(repeatAlphabet)) {
																if (missedAlphabetAbstractionNameSetMap
																		.get(
																				repeatAlphabet)
																		.size() == 1) {
																	ate
																			.setWorkflowModelElement(missedAlphabetAbstractionNameSetMap
																					.get(
																							repeatAlphabet)
																					.toString()
																					.replaceAll(
																							"\\[",
																							"")
																					.replaceAll(
																							"\\]",
																							""));
																	ate
																			.addAttribute(
																					"resolved",
																					"true");
																} else {
																	ate
																			.setWorkflowModelElement(subStr);
																	ate
																			.addAttribute(
																					"resolved",
																					"false");
																}
															} else {
																ate
																		.setWorkflowModelElement(subStr);
																ate
																		.addAttribute(
																				"resolved",
																				"false");
															}
															modifiedCharStream += subStr;
															modifiedATEList
																	.add(ate);
															if (debug)
																Logger
																		.println("m: "
																				+ modifiedCharStream
																				+ " @ "
																				+ ate
																						.getWorkflowModelElement()
																				+ " @ "
																				+ ate
																						.getAttributes()
																						.get(
																								"resolved"));
														}
														j = k - 1;
													}// noInnerMatches >
													// noMatches
												}

											}// for (String innerRepeat

											if (innerRepeatExists)
												break;
										}
									}

									if (!innerRepeatExists) {
										repeatAlphabet.clear();
										repeatAlphabet.addAll(repeatAlphabetMap
												.get(repeat));

										ate = new org.processmining.lib.mxml.AuditTrailEntry();
										ate.addAttribute("startIndex", j + "");
										ate.addAttribute("endIndex", (j
												+ matchingSequenceLength - 1)
												+ "");

										if (alphabetAbstractionNameSetMap
												.containsKey(repeatAlphabet)) {
											if (alphabetAbstractionNameSetMap
													.get(repeatAlphabet).size() == 1) {
												ate
														.setWorkflowModelElement(alphabetAbstractionNameSetMap
																.get(
																		repeatAlphabet)
																.toString()
																.replaceAll(
																		"\\[",
																		"")
																.replaceAll(
																		"\\]",
																		""));
												ate.addAttribute("resolved",
														"true");
											} else {
												ate
														.setWorkflowModelElement(repeat);
												ate.addAttribute("resolved",
														"false");
											}
										} else if (missedAlphabetAbstractionNameSetMap
												.containsKey(repeatAlphabet)) {
											if (missedAlphabetAbstractionNameSetMap
													.get(repeatAlphabet).size() == 1) {
												ate
														.setWorkflowModelElement(missedAlphabetAbstractionNameSetMap
																.get(
																		repeatAlphabet)
																.toString()
																.replaceAll(
																		"\\[",
																		"")
																.replaceAll(
																		"\\]",
																		""));
												ate.addAttribute("resolved",
														"true");
											} else {
												ate
														.setWorkflowModelElement(repeat);
												ate.addAttribute("resolved",
														"false");
											}
										} else {
											ate.setWorkflowModelElement(repeat);
											ate.addAttribute("resolved",
													"false");
										}
										modifiedCharStream += repeat;
										modifiedATEList.add(ate);
										j += matchingSequenceLength - 1;

										if (debug)
											Logger
													.println("m: "
															+ modifiedCharStream
															+ " @ "
															+ ate
																	.getWorkflowModelElement()
															+ " @ "
															+ ate
																	.getAttributes()
																	.get(
																			"resolved"));
										break;
									}
								}
								if (repeatExists)
									break;
							}

							if (!repeatExists) {
								ate = new org.processmining.lib.mxml.AuditTrailEntry();
								ate.addAttribute("startIndex", j + "");
								ate.addAttribute("endIndex", j + "");

								if (activitiesInAbstractionSet
										.contains(currentSymbol)) {
									ate.setWorkflowModelElement(currentSymbol);
									ate.addAttribute("resolved", "false");
								} else {
									ate.setWorkflowModelElement(charActivityMap
											.get(currentSymbol));
									ate.addAttribute("resolved", "true");
								}
								modifiedCharStream += currentSymbol;
								modifiedATEList.add(ate);

								if (debug)
									Logger.println("m: "
											+ modifiedCharStream
											+ " @ "
											+ ate.getWorkflowModelElement()
											+ " @ "
											+ ate.getAttributes().get(
													"resolved"));
							}
						} else {
							/*
							 * No repeat exists at this position; but still this
							 * activity is in the abstraction set; so put the
							 * abstraction name
							 */
							ate = new org.processmining.lib.mxml.AuditTrailEntry();
							ate.addAttribute("startIndex", j + "");
							ate.addAttribute("endIndex", j + "");

							if (activitiesInAbstractionSet
									.contains(currentSymbol)) {
								ate.setWorkflowModelElement(currentSymbol);
								ate.addAttribute("resolved", "false");
							} else {
								ate.setWorkflowModelElement(charActivityMap
										.get(currentSymbol));
								ate.addAttribute("resolved", "true");
							}
							modifiedCharStream += currentSymbol;
							modifiedATEList.add(ate);

							if (debug)
								Logger.println("m: " + modifiedCharStream
										+ " @ " + ate.getWorkflowModelElement()
										+ " @ "
										+ ate.getAttributes().get("resolved"));
						}

					} else {
						/*
						 * simple case: The current symbol doesn't contribute to
						 * any abstraction; just copy it as is
						 */
						ate = new org.processmining.lib.mxml.AuditTrailEntry();
						ate.addAttribute("startIndex", j + "");
						ate.addAttribute("endIndex", j + "");
						ate.setWorkflowModelElement(charActivityMap
								.get(currentSymbol));
						ate.addAttribute("resolved", "true");
						modifiedATEList.add(ate);

						modifiedCharStream += currentSymbol;
						if (debug)
							Logger.println("m: " + modifiedCharStream + " @ "
									+ ate.getWorkflowModelElement() + " @ "
									+ ate.getAttributes().get("resolved"));
					}
				}

				if (debug) {
					Logger.println("M: " + modifiedCharStream);
					Logger.println(currentCharStreamLength + " @ "
							+ (modifiedCharStream.length() / encodingLength)
							+ " @ " + modifiedATEList.size());
				}
				/*
				 * Pass 1: Process the modifiedATEList and resolve all entries
				 * which were set to false
				 */

				Map<String, String> ateAttributeMap;
				String resolvedAbstraction;
				for (int j = 0; j < modifiedATEList.size(); j++) {
					ate = modifiedATEList.get(j);
					ateAttributeMap = ate.getAttributes();

					if (ateAttributeMap.get("resolved").equals("false")) {
						resolvedAbstraction = getResolvedAbstraction(ate
								.getWorkflowModelElement().trim(), j);
						if (debug)
							Logger
									.printReturn("Exiting Get Resolved Abstraction");

						if (resolvedAbstraction != null) {
							ate.setWorkflowModelElement(resolvedAbstraction);
							ateAttributeMap.put("resolved", "true");
							ate.setAttributes(ateAttributeMap);
						} else {
							// System.out.println("S: Resolution Not Found for Instance "+i+" for repeat "+ate.getWorkflowModelElement().trim()+" at index "+ateAttributeMap.get("startIndex")+","+ateAttributeMap.get("endIndex"));
							// System.exit(0);
						}
					}
				}

				for (int j = 0; j < modifiedATEList.size(); j++) {
					ate = modifiedATEList.get(j);
					ate.setEventType(EventType.COMPLETE);
					persistency.addAuditTrailEntry(ate);
				}

				persistency.endProcessInstance();
			}

			persistency.endProcess();
			persistency.endLogfile();
			persistency.finish();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (LogException e) {
			e.printStackTrace();
		}

		if (debug)
			Logger.printReturn("Process Log With Abstractions");
	}

	private String getResolvedAbstraction(String repeat, int index) {
		if (debug)
			Logger.printCall("Entering Get Resolved Abstraction: " + repeat);

		int noLookAheadEntries = 5, noLookBackEntries = 5;

		TreeSet<String> repeatAlphabet = new TreeSet<String>();
		int repeatLength = repeat.length() / encodingLength;

		for (int i = 0; i < repeatLength; i++)
			repeatAlphabet.add(repeat.substring(i * encodingLength, (i + 1)
					* encodingLength));

		if (debug)
			Logger.println("ra: " + repeatAlphabet);

		org.processmining.lib.mxml.AuditTrailEntry ate;
		Map<String, String> ateAttributes;
		TreeSet<String> resolvedAlphabetAbstractionNameSet;
		boolean isResolutionFound = false;

		if (!resolvedAlphabetAbstractionNameSetMap.containsKey(repeatAlphabet)) {
			/*
			 * resolvedSymbolAbstractionNameSetMap doesn't contain the entry;
			 * find if the symbol is either in the missedMap or the original map
			 * first if yes, add it else find all abstraction alphabets that
			 * subsume this symbol and put the abstraction names in the
			 * resolvedMap
			 */

			HashSet<String> tempSet = new HashSet<String>();
			int noCommonElements, noDifferentElements;
			if (alphabetAbstractionNameSetMap.containsKey(repeatAlphabet)) {
				resolvedAlphabetAbstractionNameSetMap.put(repeatAlphabet,
						alphabetAbstractionNameSetMap.get(repeatAlphabet));
				isResolutionFound = true;

				if (debug)
					Logger
							.println("Found Resolution for Repeat: "
									+ repeat
									+ " @ "
									+ repeatAlphabet
									+ " @ "
									+ alphabetAbstractionNameSetMap
											.get(repeatAlphabet));
			} else if (missedAlphabetAbstractionNameSetMap
					.containsKey(repeatAlphabet)) {
				resolvedAlphabetAbstractionNameSetMap
						.put(repeatAlphabet,
								missedAlphabetAbstractionNameSetMap
										.get(repeatAlphabet));
				isResolutionFound = true;

				if (debug)
					Logger.println("Found Resolution for Repeat: "
							+ repeat
							+ " @ "
							+ repeatAlphabet
							+ " @ "
							+ missedAlphabetAbstractionNameSetMap
									.get(repeatAlphabet));
			} else {
				resolvedAlphabetAbstractionNameSet = new TreeSet<String>();
				for (TreeSet<String> alphabet : alphabetAbstractionNameSetMap
						.keySet()) {
					if (alphabet.size() >= repeatAlphabet.size()) {
						tempSet.clear();
						tempSet.addAll(repeatAlphabet);
						tempSet.retainAll(alphabet);
						noCommonElements = tempSet.size();

						tempSet.clear();
						tempSet.addAll(repeatAlphabet);
						tempSet.removeAll(alphabet);
						noDifferentElements = tempSet.size();

						if (noCommonElements >= Math
								.ceil(commonElementsFilterThreshold
										* repeatAlphabet.size())
								&& noDifferentElements <= Math
										.ceil(differentElementsFilterThreshold
												* repeatAlphabet.size())) {
							if (debug)
								Logger.println("ra: " + repeatAlphabet
										+ " @alp:  " + alphabet + " @ "
										+ noCommonElements + ","
										+ noDifferentElements + " @ "
										+ alphabetAbstractionNameSetMap.size());
							resolvedAlphabetAbstractionNameSet
									.addAll(alphabetAbstractionNameSetMap
											.get(alphabet));
						}
					}
				}

				for (TreeSet<String> missedAlphabet : missedAlphabetAbstractionNameSetMap
						.keySet()) {
					if (missedAlphabet.size() >= repeatAlphabet.size()) {
						tempSet.clear();
						tempSet.addAll(repeatAlphabet);
						tempSet.retainAll(missedAlphabet);
						noCommonElements = tempSet.size();

						tempSet.clear();
						tempSet.addAll(repeatAlphabet);
						tempSet.removeAll(missedAlphabet);
						noDifferentElements = tempSet.size();

						if (noCommonElements >= Math
								.ceil(commonElementsFilterThreshold
										* repeatAlphabet.size())
								&& noDifferentElements <= Math
										.ceil(differentElementsFilterThreshold
												* repeatAlphabet.size())) {
							if (debug)
								Logger.println("ra: " + repeatAlphabet
										+ " @alp:  " + missedAlphabet + " @ "
										+ noCommonElements + ","
										+ noDifferentElements);
							resolvedAlphabetAbstractionNameSet
									.addAll(missedAlphabetAbstractionNameSetMap
											.get(missedAlphabet));
						}
					}
				}

				if (resolvedAlphabetAbstractionNameSet.size() > 0) {
					isResolutionFound = true;
					resolvedAlphabetAbstractionNameSetMap.put(repeatAlphabet,
							resolvedAlphabetAbstractionNameSet);
					if (debug)
						Logger.println("Found Resolution for Repeat: " + repeat
								+ " @ " + repeatAlphabet + " @ "
								+ resolvedAlphabetAbstractionNameSet);
				}
			}

		} else {
			isResolutionFound = true;
		}

		if (isResolutionFound
				&& resolvedAlphabetAbstractionNameSetMap
						.containsKey(repeatAlphabet)) {
			resolvedAlphabetAbstractionNameSet = resolvedAlphabetAbstractionNameSetMap
					.get(repeatAlphabet);
			if (resolvedAlphabetAbstractionNameSet.size() == 1) {
				return resolvedAlphabetAbstractionNameSet.toString()
						.replaceAll("\\[", "").replaceAll("\\]", "");
			} else {
				/*
				 * More than one abstraction name is possible; use the heuristic
				 * of nearby abstraction names
				 */

				// look back
				for (int i = index - 1; i > 0 && i >= index - noLookBackEntries; i--) {
					ate = modifiedATEList.get(i);

					ateAttributes = ate.getAttributes();
					if (ateAttributes.get("resolved").equals("false")) {
						// System.out.println("S: shouldn't be the case, the previous entry has an unresolved entry");
						// System.exit(0);
					}

					if (resolvedAlphabetAbstractionNameSet.contains(ate
							.getWorkflowModelElement().trim())) {
						return ate.getWorkflowModelElement().trim();
					}
				}

				// look ahead
				for (int i = index + 1; i <= index + noLookAheadEntries
						&& i < modifiedATEList.size(); i++) {
					ate = modifiedATEList.get(i);
					ateAttributes = ate.getAttributes();
					if (ateAttributes.get("resolved").equals("true")) {
						if (resolvedAlphabetAbstractionNameSet.contains(ate
								.getWorkflowModelElement().trim())) {
							return ate.getWorkflowModelElement().trim();
						}
					}
				}

				// couldn't be found either in the previous/succeeding entries
				return resolvedAlphabetAbstractionNameSet.toArray()[0]
						.toString().replaceAll("\\[", "").replaceAll("\\]", "");
			}
		} else {
			return null;
		}
	}

	private void prepareRepeatInfoTable() {
		table = new JTable(new RepeatTableModel(displayData));

		table.setBackground(new Color(100, 100, 100));

		table.getColumnModel().getColumn(0).setWidth(table.getWidth() / 3);
		table.getColumnModel().getColumn(1).setWidth(table.getWidth() / 2);
		table.getColumnModel().getColumn(2).setWidth(table.getWidth() / 6);

		table.getColumnModel().getColumn(0).setCellRenderer(
				new TextAreaRenderer());
		table.getColumnModel().getColumn(1).setCellRenderer(
				new TextAreaRenderer());
		table.getColumnModel().getColumn(2).setCellRenderer(
				new TextAreaRenderer());

		/*
		 * HV: Lines removed due to erros in nightly build: [javac]
		 * D:/Hudson_home/jobs/ProM5 Nightly
		 * build/workspace/trunk/src/plugins/org
		 * /processmining/analysis/abstractions
		 * /ui/RepeatAbstractionResUI.java:2062: cannot find symbol [javac]
		 * symbol : method setFillsViewportHeight(boolean) [javac] location:
		 * class javax.swing.JTable [javac] table.setFillsViewportHeight(true);
		 * [javac] ^ [javac] D:/Hudson_home/jobs/ProM5 Nightly
		 * build/workspace/trunk
		 * /src/plugins/org/processmining/analysis/abstractions
		 * /ui/RepeatAbstractionResUI.java:2063: cannot find symbol [javac]
		 * symbol : method setAutoCreateRowSorter(boolean) [javac] location:
		 * class javax.swing.JTable [javac] table.setAutoCreateRowSorter(true);
		 * [javac] ^ table.setFillsViewportHeight(true);
		 * table.setAutoCreateRowSorter(true);
		 */
	}

	class RepeatTableModel extends AbstractTableModel {
		private String[] columnNames = { "Repeat Alphabet", "Repeat Set",
				"Count" };
		private Object[][] data;

		public RepeatTableModel(Object[][] data) {
			this.data = data;
		}

		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		@SuppressWarnings("unchecked")
		public Class getColumnClass(int col) {
			return getValueAt(0, col).getClass();
		}
	}

	class TextAreaRenderer extends JTextArea implements TableCellRenderer {
		private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
		@SuppressWarnings("unchecked")
		private final Map cellSizes = new HashMap();

		public TextAreaRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int col) {
			adaptee.getTableCellRendererComponent(table, obj, isSelected,
					hasFocus, row, col);
			setForeground(adaptee.getForeground());
			setBackground(adaptee.getBackground());
			setBorder(adaptee.getBorder());
			setFont(adaptee.getFont());
			setText(adaptee.getText());

			TableColumnModel columnModel = table.getColumnModel();
			setSize(columnModel.getColumn(col).getWidth(), 100000);
			int height_wanted = (int) getPreferredSize().getHeight();
			addSize(table, row, col, height_wanted);
			height_wanted = findTotalMaximumRowSize(table, row);
			if (height_wanted != table.getRowHeight(row))
				table.setRowHeight(row, height_wanted);
			return this;
		}

		@SuppressWarnings("unchecked")
		private void addSize(JTable table, int row, int col, int height) {
			Map rows = (Map) cellSizes.get(table);
			if (rows == null)
				cellSizes.put(table, rows = new HashMap());

			Map rowHeights = (Map) rows.get(new Integer(row));
			if (rowHeights == null)
				rows.put(new Integer(row), rowHeights = new HashMap());

			rowHeights.put(new Integer(col), new Integer(height));

		}

		/**
		 * Look through all columns and get the renderer. If it is also a text
		 * area renderer, we look at the maximum height in its hash table for
		 * this row
		 */
		@SuppressWarnings("unchecked")
		private int findTotalMaximumRowSize(JTable table, int row) {
			int maxHeight = 0;
			Enumeration columns = table.getColumnModel().getColumns();
			while (columns.hasMoreElements()) {
				TableColumn tc = (TableColumn) columns.nextElement();
				TableCellRenderer cellRenderer = tc.getCellRenderer();
				if (cellRenderer instanceof TextAreaRenderer) {
					TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
					maxHeight = Math.max(maxHeight, tar.findMaximumRowSize(
							table, row));
				}
			}
			return maxHeight;
		}

		@SuppressWarnings("unchecked")
		private int findMaximumRowSize(JTable table, int row) {
			Map rows = (Map) cellSizes.get(table);

			if (rows == null)
				return 0;

			Map rowHeights = (Map) rows.get(new Integer(row));
			if (rowHeights == null)
				return 0;
			int maxHeight = 0;
			for (Iterator it = rowHeights.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int cellHeight = ((Integer) entry.getValue()).intValue();
				maxHeight = Math.max(maxHeight, cellHeight);
			}
			return maxHeight;
		}
	}
}
