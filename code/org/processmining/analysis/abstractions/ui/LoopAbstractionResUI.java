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
import java.util.Arrays;
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
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.Process;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.MainUI;
import org.processmining.lib.mxml.EventType;
import org.processmining.lib.mxml.LogException;
import org.processmining.lib.mxml.writing.persistency.LogPersistencyStream;

@SuppressWarnings("serial")
public class LoopAbstractionResUI extends JPanel {

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
			// return s1.compareTo(s2) * (s1.length() < s2.length() ? 1 : -1);//
			// *
			// (-1);
		}
	}

	public static Color colorBg = new Color(120, 120, 120);
	public static Color colorInnerBg = new Color(140, 140, 140);
	public static Color colorFg = new Color(30, 30, 30);
	public static Color colorTextAreaBg = new Color(160, 160, 160);

	/**
	 * The bottom panel where the patterns and configuration/filtering is
	 * provided
	 */
	protected JScrollPane tandemRepeatScrollPane;
	protected JScrollPane tandemRepeatInfoScrollPane;
	protected JScrollPane tandemRepeatConfigurationScrollPane;

	JTable table;
	JSlider tandemRepeatFreqFilterSlider, tandemRepeatSizeFilterSlider;
	protected int tandemRepeatFreqFilterThreshold,
			tandemRepeatSizeFilterThreshold;
	JLabel noAlphabetsLabel, freqFilterLabel, sizeFilterLabel;

	/**
	 * The top panel where the abstraction results and configuration are
	 * provided
	 */

	protected JPanel abstractionResultPanel;
	JScrollPane abstractionResultScrollPane;
	protected JPanel abstractionPanel, abstractionParameterPanel;
	protected JPanel abstractionConfigurationPanel;
	JPanel abstractionSettingsPanel;

	protected JPanel abstractionStrategyPanel;
	protected ButtonGroup abstractionStrategyButtonGroup;
	protected JRadioButton structuredRadioButton, unstructuredRadioButton;
	protected boolean isStructured = false;

	JLabel commonElementsLabel, differentElementsLabel;
	protected JSlider noCommonElementsSlider, noDifferentElementsSlider;
	protected double commonElementsThreshold, differentElementsThreshold;

	int noAbstractions;
	JLabel abstractionCountLabel;

	SlickerButton findExtendedAbstractionsButton;

	Object[][] originalData, displayData;

	int encodingLength;
	HashMap<String, String> charActivityMap;
	HashMap<String, String> activityCharMap;

	HashMap<TreeSet<String>, TreeSet<String>> alphabetRepeatMap;
	HashMap<String, TreeSet<String>> repeatAlphabetMap;
	HashMap<TreeSet<String>, Integer> alphabetCountMap;
	/**
	 * After finding the abstractions, process them and assign the abstraction
	 * corresponding to each repeat alphabet; It might be the case that a repeat
	 * alphabet can map to more than one abstraction; Special care needs to be
	 * taken
	 */
	HashMap<TreeSet<String>, TreeSet<String>> alphabetAbstractionNameSetMap;
	HashMap<TreeSet<String>, HashSet<String>> missedAlphabetAbstractionNameSetMap;
	/**
	 * All repeats starting with a particular symbol; Used in pre-processing
	 * logs to replace repeats with abstractions
	 */
	HashMap<String, TreeSet<String>> startSymbolRepeatSetMap;

	HashMap<TreeSet<String>, TreeSet<String>> originalModifiedAlphabetMap;

	HashMap<Integer, ArrayList<TreeSet<String>>> lengthAlphabetMap;
	HashMap<Integer, ArrayList<TreeSet<String>>> lengthMaximalElementMap;

	HashMap<Set<String>, Set<Set<String>>> mergedAbstractionMap;
	ArrayList<AbstractionResultConfigurationComponent> abstracionResultConfigurationComponentList;

	/**
	 * Data structures to store the maximal element and conceptually similar
	 * elements The original abstraction (used to finally modify the repeat
	 * patterns from the alphabet) intermediate abstraction map during the user
	 * manipulations (merge, removal of symbols in an alphabet) modified ->
	 * original/intermediate
	 * 
	 */
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> originalMaximalElementSubsumedElementSetMap;

	/**
	 * All processing will happen in the intermediate data structure than
	 * modifying the original one
	 */

	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> intermediateMaximalElementSubsumedElementSetMap;
	/**
	 * Store the abstract maximal elements that are merged The key set involves
	 * the merged alphabet set and the value corresponds to the maximal
	 * elements/abstractions that were merged
	 */
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> mergedAbstractionElementMaximalElementSetMap;

	/**
	 * Certain symbols can be removed from a maximal element; Store the map
	 * between the modified abstraction and the original abstraction it
	 * corresponds to
	 */
	HashMap<TreeSet<String>, TreeSet<String>> newAbstractionOriginalAbstractionMap;

	/**
	 * Store the abstraction names for the abstraction alphabets
	 */

	HashMap<TreeSet<String>, String> abstractionAlphabetAbstractionNameMap;

	/**
	 * The elements subsumed in an abstraction might also have to be modified
	 * (in case the user deletes one/more symbols) A map between the modified
	 * subsumed element and the original subsumed alphabet is required to later
	 * process the repeats corresponding to the alphabet
	 */
	HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> originalRepeatAlphabetNewRepeatAlphabetMap;

	HashMap<TreeSet<String>, TreeSet<String>> modifiedOrignalAbstractionMap;

	String transformLogFileName, abstractionDetailFileName;

	public LoopAbstractionResUI(int encodingLength,
			HashMap<TreeSet<String>, TreeSet<String>> alphabetRepeatMap,
			HashMap<TreeSet<String>, Integer> alphabetCountMap,
			HashMap<String, String> charActivityMap) {
		this.encodingLength = encodingLength;
		this.alphabetRepeatMap = alphabetRepeatMap;
		this.alphabetCountMap = alphabetCountMap;
		this.charActivityMap = charActivityMap;

		/**
		 * Convert the charActivityMap to activityCharMap
		 */
		this.activityCharMap = new HashMap<String, String>();
		for (String symbol : charActivityMap.keySet())
			activityCharMap.put(charActivityMap.get(symbol).trim(), symbol
					.trim());

		this.mergedAbstractionMap = new HashMap<Set<String>, Set<Set<String>>>();

		this.originalMaximalElementSubsumedElementSetMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();
		this.modifiedOrignalAbstractionMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		this.originalRepeatAlphabetNewRepeatAlphabetMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();

		populateOriginalData();
		setupGui();
	}

	private void populateOriginalData() {
		int noElements = alphabetRepeatMap.size();
		originalData = new Object[noElements][3];

		int index = 0;
		TreeSet<String> decodedAlphabetSet;
		TreeSet<String> decodedAlphabetRepeatSet;
		TreeSet<String> alphabetRepeatSet;
		String decodedRepeat;
		int trLength;

		// System.out.println("Alphabet Count Map");
		for (TreeSet<String> alphabetSet : alphabetRepeatMap.keySet()) {
			// System.out.println(alphabetSet+" @ "+alphabetCountMap.get(alphabetSet));
			decodedAlphabetSet = new TreeSet<String>();
			for (String symbol : alphabetSet)
				decodedAlphabetSet.add(charActivityMap.get(symbol));

			originalData[index][0] = decodedAlphabetSet;

			alphabetRepeatSet = alphabetRepeatMap.get(alphabetSet);
			decodedAlphabetRepeatSet = new TreeSet<String>();

			decodedRepeat = "";
			for (String tr : alphabetRepeatSet) {
				trLength = tr.length() / encodingLength;
				decodedRepeat += "<";
				for (int i = 0; i < trLength - 1; i++) {
					decodedRepeat += charActivityMap.get(tr.substring(i
							* encodingLength, (i + 1) * encodingLength))
							+ ",";
				}
				decodedRepeat += charActivityMap.get(tr.substring(
						(trLength - 1) * encodingLength, (trLength)
								* encodingLength))
						+ ">\n\n";

				decodedAlphabetRepeatSet.add(decodedRepeat);
			}

			// originalData[index][1] = decodedAlphabetRepeatSet;
			originalData[index][1] = decodedRepeat;
			originalData[index][2] = alphabetCountMap.get(alphabetSet);
			index++;
		}
		displayData = originalData.clone();
		// alphabetRepeatMap = null;
		alphabetCountMap = null;
	}

	private void setupGui() {
		this.removeAll();
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setLayout(new BorderLayout());
		this.setBackground(new Color(90, 90, 90));

		JPanel lowerPanel = prepareLowerPanel();

		/**
		 * Prepare the upper panel where the abstraction settings and
		 * abstraction results would be displayed
		 */
		JPanel upperPanel = new JPanel();
		upperPanel.setOpaque(true);
		upperPanel.setLayout(new BoxLayout(upperPanel, BoxLayout.Y_AXIS));
		upperPanel.setBackground(new Color(140, 140, 140));
		upperPanel.setBorder(BorderFactory.createEmptyBorder());

		RoundedPanel abstractionPanel = new RoundedPanel(10, 5, 5);
		abstractionPanel.setBackground(new Color(100, 100, 100));
		abstractionPanel.setLayout(new BoxLayout(abstractionPanel,
				BoxLayout.X_AXIS));

		prepareAbstractionStrategyPanel();
		//		
		JPanel abstractionParameterPanel = new JPanel();
		abstractionParameterPanel.setOpaque(false);
		abstractionParameterPanel.setLayout(new BoxLayout(
				abstractionParameterPanel, BoxLayout.Y_AXIS));
		abstractionParameterPanel.setBackground(new Color(100, 100, 100));
		abstractionParameterPanel.setBorder(BorderFactory.createEmptyBorder());

		JPanel abstractionThresholdPanel = new JPanel();
		abstractionThresholdPanel.setOpaque(false);
		abstractionThresholdPanel.setLayout(new BoxLayout(
				abstractionThresholdPanel, BoxLayout.X_AXIS));
		abstractionThresholdPanel.setBackground(new Color(100, 100, 100));
		abstractionThresholdPanel.setBorder(BorderFactory.createEmptyBorder());

		JPanel commonElementsPanel = new JPanel();
		commonElementsPanel.setOpaque(false);
		commonElementsPanel.setLayout(new BoxLayout(commonElementsPanel,
				BoxLayout.Y_AXIS));
		commonElementsPanel.setBackground(new Color(140, 140, 140));
		commonElementsPanel.setBorder(BorderFactory.createEmptyBorder());

		commonElementsLabel = new JLabel("% Common Elements: " + 1.0);
		noCommonElementsSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		noCommonElementsSlider.setOpaque(false);
		noCommonElementsSlider.setValue(100);
		noCommonElementsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				commonElementsThreshold = noCommonElementsSlider.getValue() / 100.0;

				if (noCommonElementsSlider.getValueIsAdjusting() == false) {
					commonElementsLabel.setText("% Common Elements: "
							+ commonElementsThreshold);
				}
				revalidate();
				repaint();
			}
		});

		commonElementsPanel.add(commonElementsLabel);
		commonElementsPanel.add(noCommonElementsSlider);

		JPanel differentElementsPanel = new JPanel();
		differentElementsPanel.setOpaque(false);
		differentElementsPanel.setLayout(new BoxLayout(differentElementsPanel,
				BoxLayout.Y_AXIS));
		differentElementsPanel.setBackground(new Color(140, 140, 140));
		differentElementsPanel.setBorder(BorderFactory.createEmptyBorder());

		differentElementsLabel = new JLabel("% Diff. Elements: " + 0);
		noDifferentElementsSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		noDifferentElementsSlider.setValue(0);
		noDifferentElementsSlider.setOpaque(false);
		noDifferentElementsSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				differentElementsThreshold = noDifferentElementsSlider
						.getValue() / 100.0;

				if (noDifferentElementsSlider.getValueIsAdjusting() == false) {
					differentElementsLabel.setText("% Diff. Elements: "
							+ differentElementsThreshold);
				}
				revalidate();
				repaint();
			}
		});

		differentElementsPanel.add(differentElementsLabel);
		differentElementsPanel.add(noDifferentElementsSlider);

		SlickerButton findAbstractionsButton = new SlickerButton(
				"Find Abstractions");
		findAbstractionsButton.setOpaque(false);
		findAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isStructured) {
					commonElementsThreshold = noCommonElementsSlider.getValue() / 100.0;
					differentElementsThreshold = noDifferentElementsSlider
							.getValue() / 100.0;
					if (abstracionResultConfigurationComponentList == null)
						abstracionResultConfigurationComponentList = new ArrayList<AbstractionResultConfigurationComponent>();
					else {
						/**
						 * Already one or more findAbstractions() invocations
						 * had been done Get the modifications made over the
						 * abstractions
						 */
					}

					HashSet<TreeSet<String>> alphabetSet = new HashSet<TreeSet<String>>();
					for (int i = 0; i < displayData.length; i++)
						alphabetSet.add((TreeSet<String>) displayData[i][0]);

					AbstractionSetTheory a = new AbstractionSetTheory();
					a
							.findMaximalElementSubsumedElementMap(
									alphabetSet,
									a
											.getMaximalElementsApproximateSubsumption(
													alphabetSet,
													commonElementsThreshold,
													differentElementsThreshold,
													AbstractionSetTheory.DiffStrategy.MIN_SIZE));
					originalMaximalElementSubsumedElementSetMap = (HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>) a
							.getMaximalElementSubsumedElementMap();

					intermediateMaximalElementSubsumedElementSetMap = (HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>) originalMaximalElementSubsumedElementSetMap
							.clone();
					prepareAbstractionResult(a.getMaximalElementList());

					// Free Memory
					a = null;

					abstractionCountLabel
							.setText("No. Abstractions: "
									+ abstracionResultConfigurationComponentList
											.size());
					revalidate();
					repaint();
				} else {
					MainUI.getInstance()
							.showGlassDialog("Structured Abstraction",
									"Implementation Underway");
				}
			}
		});

		JPanel findAbstractionsPanel = new JPanel();
		// if(abstracionResultConfigurationComponentList == null)
		// abstracionResultConfigurationComponentList = new
		// ArrayList<AbstractionResultConfigurationComponent>();
		findAbstractionsPanel.setLayout(new BoxLayout(findAbstractionsPanel,
				BoxLayout.X_AXIS));
		findAbstractionsPanel.add(findAbstractionsButton);
		// findAbstractionsPanel.add(findExtendedAbstractionsButton);

		abstractionThresholdPanel.add(commonElementsPanel);
		abstractionThresholdPanel.add(differentElementsPanel);

		// abstractionParameterPanel.add(abstractionStrategyPanel);
		abstractionParameterPanel.add(abstractionThresholdPanel);
		abstractionParameterPanel.add(findAbstractionsPanel);
		// abstractionParameterPanel.add(findExtendedAbstractionsButton);
		// findAbstractions();
		// prepareAbstractionResult();
		abstractionResultPanel = new JPanel();
		abstractionResultPanel.setOpaque(true);
		abstractionResultPanel.setBackground(new Color(140, 140, 140));
		abstractionResultScrollPane = new JScrollPane(abstractionResultPanel);

		abstractionPanel.add(abstractionResultScrollPane);
		abstractionPanel.add(Box.createHorizontalStrut(5));
		abstractionPanel.add(abstractionParameterPanel);

		JPanel abstractionLowerPanel = new JPanel();
		abstractionLowerPanel.setOpaque(true);
		abstractionLowerPanel.setBackground(new Color(140, 140, 140));
		abstractionLowerPanel.setLayout(new BoxLayout(abstractionLowerPanel,
				BoxLayout.X_AXIS));

		SlickerButton mergeAbstractionsButton = new SlickerButton(
				"Merge Abstractions");
		mergeAbstractionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				/**
				 * Check if there are any modified abstractions (where one or
				 * more symbols have been removed) If so, let the user click
				 * refresh abstractions first before doing a merge
				 */
				boolean hasModified = false;

				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (a.isModified) {
						hasModified = true;
						break;
					}
				}
				if (hasModified) {
					MainUI
							.getInstance()
							.showGlassDialog(
									"Action Required",
									"Certain Abstractions are modified; Click Refresh Buttons and then Click Merge Abstractions");
					return;
				}

				int noSelected = 0;
				TreeSet<String> newAlphabetSet = new TreeSet<String>();
				TreeSet<TreeSet<String>> mergedAlphabetSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> mergedAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<String> mergedMaximalElement = new TreeSet<String>();
				ArrayList<AbstractionResultConfigurationComponent> removeAbstrationList = new ArrayList<AbstractionResultConfigurationComponent>();
				int index = 0;

				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (a.activeBox.isSelected()) {
						newAlphabetSet.addAll(a.selectedActivities);
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.selectedActivities)) {
							mergedAbstractionSubsumedElementSet
									.addAll(intermediateMaximalElementSubsumedElementSetMap
											.get(a.selectedActivities));
						} else {

							// System.out.println("In Merge Abstractions\nSomething wrong: the selected activities doesn't have a map: "+a.selectedActivities);
							// System.out.println(intermediateMaximalElementSubsumedElementSetMap.size()+" @ "+abstracionResultConfigurationComponentList.size());
							// System.exit(0);
						}
						mergedMaximalElement.addAll(a.selectedActivities);

						mergedAlphabetSet.add(a.originalAbstractionActivities);
						// System.out.println(a.selectedActivities);
						noSelected++;
						removeAbstrationList.add(a);
						index++;
					}
				}

				/**
				 * Because of the merge, there can be abstractions that now
				 * satisfy the criteria (%common and %diff) for the union of the
				 * merged abstractions; Check for all such abstractions and
				 * remove them
				 */
				HashSet<String> tempSet;
				int noCommonElements, noDifferentElements, minSize;
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (!a.activeBox.isSelected()) {
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.selectedActivities)) {
							minSize = (a.selectedActivities.size() < mergedMaximalElement
									.size()) ? a.selectedActivities.size()
									: mergedMaximalElement.size();

							tempSet = new HashSet<String>();
							tempSet.addAll(a.selectedActivities);
							tempSet.retainAll(mergedMaximalElement);

							noCommonElements = tempSet.size();

							tempSet = new HashSet<String>();
							tempSet.addAll(a.selectedActivities);
							tempSet.removeAll(mergedMaximalElement);

							noDifferentElements = tempSet.size();

							if ((noCommonElements >= Math
									.ceil(commonElementsThreshold * minSize))
									&& (noDifferentElements <= Math
											.ceil(differentElementsThreshold
													* minSize))) {
								removeAbstrationList.add(a);
								mergedMaximalElement
										.addAll(a.selectedActivities);
								mergedAbstractionSubsumedElementSet
										.addAll(intermediateMaximalElementSubsumedElementSetMap
												.get(a.selectedActivities));
							}
						} else {
							// System.out.println("In Merge Abstractions2\nSomething wrong: the selected activities doesn't have a map: "+a.selectedActivities);
							// System.out.println(intermediateMaximalElementSubsumedElementSetMap.size()+" @ "+abstracionResultConfigurationComponentList.size());
							// System.exit(0);
						}
					}
				}

				intermediateMaximalElementSubsumedElementSetMap.put(
						mergedMaximalElement,
						mergedAbstractionSubsumedElementSet);

				/**
				 * Remove the abstractions that were merged from the
				 * intermediateMaximalElementSubsumedElementSetMap
				 */
				for (AbstractionResultConfigurationComponent a : removeAbstrationList) {
					if (intermediateMaximalElementSubsumedElementSetMap
							.containsKey(a.originalAbstractionActivities))
						intermediateMaximalElementSubsumedElementSetMap
								.remove(a.originalAbstractionActivities);
					else if (intermediateMaximalElementSubsumedElementSetMap
							.containsKey(a.selectedActivities))
						intermediateMaximalElementSubsumedElementSetMap
								.remove(a.selectedActivities);
				}

				for (AbstractionResultConfigurationComponent a : removeAbstrationList)
					abstracionResultConfigurationComponentList.remove(a);

				if (mergedAbstractionElementMaximalElementSetMap == null) {
					mergedAbstractionElementMaximalElementSetMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();
				}

				if (mergedAbstractionElementMaximalElementSetMap
						.containsKey(newAlphabetSet)) {
					mergedAlphabetSet
							.addAll(mergedAbstractionElementMaximalElementSetMap
									.get(newAlphabetSet));
				}
				mergedAbstractionElementMaximalElementSetMap.put(
						newAlphabetSet, mergedAlphabetSet);

				AbstractionResultConfigurationComponent b = new AbstractionResultConfigurationComponent(
						"Abs " + noAbstractions++, newAlphabetSet);
				abstracionResultConfigurationComponentList.add(b);
				abstractionResultPanel.removeAll();
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList)
					abstractionResultPanel.add(a);
				abstractionCountLabel.setText("No. Abstractions: "
						+ abstracionResultConfigurationComponentList.size());
				revalidate();
				repaint();
				// System.out.println("No. Selected: "+noSelected);
			}
		});

		SlickerButton removeAbstractionButton = new SlickerButton(
				"Remove Abstractions");
		removeAbstractionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ArrayList<AbstractionResultConfigurationComponent> removeAbstrationList = new ArrayList<AbstractionResultConfigurationComponent>();
				TreeSet<TreeSet<String>> removeAbstractionSubsumedElementSet, tempAbstractionAlphabetSubsumedElementSet;
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (a.activeBox.isSelected()) {
						// System.out.println("Removing Abs: "+a.originalAbstractionActivities);
						// Before removing the abstraction, see whether the
						// subsumed elements can be put in other abstractions;
						// Currently, use the criteria of complete subsumption
						removeAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
								new TreeSetComparator());
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.originalAbstractionActivities)) {
							// Add the elements that are subsumed in this
							// abstraction
							removeAbstractionSubsumedElementSet
									.addAll(intermediateMaximalElementSubsumedElementSetMap
											.get(a.originalAbstractionActivities));
							intermediateMaximalElementSubsumedElementSetMap
									.remove(a.originalAbstractionActivities);
							// System.out.println("Putting "+a.originalAbstractionActivities);
							originalRepeatAlphabetNewRepeatAlphabetMap.put(
									a.originalAbstractionActivities,
									new TreeSet<TreeSet<String>>());
						} else {
							// System.out.println("Can't find the abstraction to be removed in the intermediate map");
							// System.exit(0);
						}

						/**
						 * for each remaining abstraction, A, check whether
						 * elements in the subsumed alphabet set (of the
						 * currently removed abstraction) can be put in the
						 * abstraction A
						 * 
						 */
						// System.out.println(removeAbstractionSubsumedElementSet);
						Set<String> tempSet;
						int noCommonElements, noDifferentElements;
						boolean alphabetAvailableInOtherAbstraction;
						for (TreeSet<String> subsumedAlphabetRemovedAbstraction : removeAbstractionSubsumedElementSet) {
							alphabetAvailableInOtherAbstraction = false;
							for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
									.keySet()) {
								tempAbstractionAlphabetSubsumedElementSet = new TreeSet<TreeSet<String>>(
										new TreeSetComparator());
								tempAbstractionAlphabetSubsumedElementSet
										.addAll(intermediateMaximalElementSubsumedElementSetMap
												.get(abstractionAlphabet));

								if (!tempAbstractionAlphabetSubsumedElementSet
										.contains(subsumedAlphabetRemovedAbstraction)) {
									if (abstractionAlphabet.size() >= subsumedAlphabetRemovedAbstraction
											.size()) {
										if (abstractionAlphabet
												.containsAll(subsumedAlphabetRemovedAbstraction)) {
											// System.out.println("Adding "+subsumedAlphabetRemovedAbstraction+" to "+abstractionAlphabet);
											alphabetAvailableInOtherAbstraction = true;
											tempAbstractionAlphabetSubsumedElementSet
													.add((TreeSet<String>) subsumedAlphabetRemovedAbstraction);
										} else {
											tempSet = new HashSet<String>();
											tempSet
													.addAll(subsumedAlphabetRemovedAbstraction);
											tempSet
													.retainAll(abstractionAlphabet);
											noCommonElements = tempSet.size();

											tempSet = new HashSet<String>();
											tempSet
													.addAll(subsumedAlphabetRemovedAbstraction);
											tempSet
													.removeAll(abstractionAlphabet);
											noDifferentElements = tempSet
													.size();

											if (noCommonElements >= commonElementsThreshold
													&& noDifferentElements <= differentElementsThreshold) {
												tempAbstractionAlphabetSubsumedElementSet
														.add((TreeSet<String>) subsumedAlphabetRemovedAbstraction
																.clone());
												alphabetAvailableInOtherAbstraction = true;
												// System.out.println("Adding "+subsumedAlphabetRemovedAbstraction+" to "+abstractionAlphabet);
											}
										}
									}
								}
								intermediateMaximalElementSubsumedElementSetMap
										.put(abstractionAlphabet,
												tempAbstractionAlphabetSubsumedElementSet);
							}
							if (!alphabetAvailableInOtherAbstraction) {
								// System.out.println("Putting "+subsumedAlphabetRemovedAbstraction);
								originalRepeatAlphabetNewRepeatAlphabetMap.put(
										subsumedAlphabetRemovedAbstraction,
										new TreeSet<TreeSet<String>>());
							}

						}

						removeAbstrationList.add(a);
					}
				}
				for (AbstractionResultConfigurationComponent a : removeAbstrationList)
					abstracionResultConfigurationComponentList.remove(a);

				abstractionResultPanel.removeAll();
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList)
					abstractionResultPanel.add(a);
				abstractionCountLabel.setText("No. Abstractions: "
						+ abstracionResultConfigurationComponentList.size());
				revalidate();
				repaint();
			}
		});

		SlickerButton refreshAbstractionButton = new SlickerButton(
				"Refresh Abstractions");
		refreshAbstractionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TreeSet<TreeSet<String>> selectedAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> selectedAbstractionFilteredSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> superAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
						new TreeSetComparator());
				TreeSet<TreeSet<String>> originalRepeatAlphabetNewRepeatAlphabetSet;
				ArrayList<AbstractionResultConfigurationComponent> removeAbstractionConfigurationComponentList = new ArrayList<AbstractionResultConfigurationComponent>();
				ArrayList<AbstractionResultConfigurationComponent> addAbstractionConfigurationComponentList = new ArrayList<AbstractionResultConfigurationComponent>();
				ArrayList<TreeSet<String>> removeAbstractionList = new ArrayList<TreeSet<String>>();

				HashSet<String> removedElements;
				TreeSet<String> tempSet;
				int noCommonElements, noDifferentElements;
				boolean isModifiedAbstractionSubsumed;
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (a.isModified) {
						isModifiedAbstractionSubsumed = false;
						removedElements = new HashSet<String>();
						removedElements.addAll(a.originalAbstractionActivities);
						removedElements.removeAll(a.selectedActivities);
						// System.out.println("Modified Abstraction");
						// System.out.println(a.originalAbstractionActivities+" @ "+removedElements+" @ "+a.selectedActivities);
						/**
						 * Add the original modified alphabet set
						 */
						if (originalRepeatAlphabetNewRepeatAlphabetMap
								.containsKey(a.originalAbstractionActivities))
							originalRepeatAlphabetNewRepeatAlphabetSet = originalRepeatAlphabetNewRepeatAlphabetMap
									.get(a.originalAbstractionActivities);
						else
							originalRepeatAlphabetNewRepeatAlphabetSet = new TreeSet<TreeSet<String>>(
									new TreeSetComparator());
						originalRepeatAlphabetNewRepeatAlphabetSet
								.add(a.selectedActivities);
						originalRepeatAlphabetNewRepeatAlphabetMap.put(
								a.originalAbstractionActivities,
								originalRepeatAlphabetNewRepeatAlphabetSet);

						// Check if the removal of some symbols in an alphabet
						// leads to a situation
						// where the new alphabet is already
						// present/is_a_subset_of in the abstraction maps
						// If so, then put all manifestations of the alphabet
						// under the subsumed list of the other alphabet
						// System.out.println("SIZE: "+intermediateMaximalElementSubsumedElementSetMap.size());
						// System.out.println(intermediateMaximalElementSubsumedElementSetMap.keySet());
						if (intermediateMaximalElementSubsumedElementSetMap
								.containsKey(a.originalAbstractionActivities)) {
							selectedAbstractionSubsumedElementSet.clear();
							selectedAbstractionSubsumedElementSet
									.addAll(intermediateMaximalElementSubsumedElementSetMap
											.get(a.originalAbstractionActivities));
							// modifiedAbstractionSubsumedElementSet =
							// originalMaximalElementSubsumedElementSetMap.get(a.originalAbstractionActivities);
							// System.out.println("S: "+selectedAbstractionSubsumedElementSet);
							selectedAbstractionFilteredSubsumedElementSet
									.clear();
							for (TreeSet<String> originalSubsumedAlphabet : selectedAbstractionSubsumedElementSet) {
								tempSet = new TreeSet<String>();
								tempSet.addAll(originalSubsumedAlphabet);
								tempSet.removeAll(removedElements);
								if (tempSet.size() > 0) {
									selectedAbstractionFilteredSubsumedElementSet
											.add(tempSet);
								}

								if (!originalSubsumedAlphabet.equals(tempSet)
										&& tempSet.size() > 0) {
									if (originalRepeatAlphabetNewRepeatAlphabetMap
											.containsKey(originalSubsumedAlphabet)) {
										originalRepeatAlphabetNewRepeatAlphabetSet = originalRepeatAlphabetNewRepeatAlphabetMap
												.get(originalSubsumedAlphabet);
									} else {
										originalRepeatAlphabetNewRepeatAlphabetSet = new TreeSet<TreeSet<String>>(
												new TreeSetComparator());
									}
									originalRepeatAlphabetNewRepeatAlphabetSet
											.add(tempSet);
									originalRepeatAlphabetNewRepeatAlphabetMap
											.put(originalSubsumedAlphabet,
													originalRepeatAlphabetNewRepeatAlphabetSet);
								}
							}
							// System.out.println("F: "+selectedAbstractionFilteredSubsumedElementSet);

							/**
							 * Check if the abstraction map contains keys that
							 * either subsumes the filtered activity list or is
							 * within the set limits of approximation
							 */

							for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
									.keySet()) {
								if (!abstractionAlphabet
										.equals(a.originalAbstractionActivities)
										&& abstractionAlphabet.size() >= a.selectedActivities
												.size()) {
									if (abstractionAlphabet
											.containsAll(a.selectedActivities)) {
										// superAbstractionSubsumedElementSet =
										// new TreeSet<TreeSet<String>>(new
										// TreeSetComparator());
										// superAbstractionSubsumedElementSet.addAll(intermediateMaximalElementSubsumedElementSetMap.get(abstractionAlphabet));
										superAbstractionSubsumedElementSet = intermediateMaximalElementSubsumedElementSetMap
												.get(abstractionAlphabet);
										superAbstractionSubsumedElementSet
												.addAll(selectedAbstractionFilteredSubsumedElementSet);
										intermediateMaximalElementSubsumedElementSetMap
												.put(abstractionAlphabet,
														superAbstractionSubsumedElementSet);

										isModifiedAbstractionSubsumed = true;
										removeAbstractionConfigurationComponentList
												.add(a);
										removeAbstractionList
												.add(a.originalAbstractionActivities);

										// System.out.println("Modified Abs Subsumed in: "+abstractionAlphabet);
									} else {
										tempSet = new TreeSet<String>();
										tempSet.addAll(a.selectedActivities);
										tempSet.retainAll(abstractionAlphabet);
										noCommonElements = tempSet.size();

										tempSet = new TreeSet<String>();
										tempSet.addAll(a.selectedActivities);
										tempSet.removeAll(abstractionAlphabet);
										noDifferentElements = tempSet.size();

										if (noCommonElements >= commonElementsThreshold
												&& noDifferentElements <= differentElementsThreshold) {
											superAbstractionSubsumedElementSet = new TreeSet<TreeSet<String>>(
													new TreeSetComparator());
											superAbstractionSubsumedElementSet
													.addAll(intermediateMaximalElementSubsumedElementSetMap
															.get(abstractionAlphabet));
											superAbstractionSubsumedElementSet
													.addAll(selectedAbstractionFilteredSubsumedElementSet);
											intermediateMaximalElementSubsumedElementSetMap
													.put(abstractionAlphabet,
															superAbstractionSubsumedElementSet);

											isModifiedAbstractionSubsumed = true;
											removeAbstractionConfigurationComponentList
													.add(a);
											removeAbstractionList
													.add(a.originalAbstractionActivities);

											// System.out.println("Modified Abs Subsumed in: "+abstractionAlphabet);
										}
									}
								}
							}

						} else {
							// System.out.println("STRANGE: The selection is not in the intermediate Map");
							// System.exit(0);
						}

						/**
						 * Remove from the intermediate abstraction map
						 */
						if (isModifiedAbstractionSubsumed) {
							// System.out.println("Removing "+a.originalAbstractionActivities+" from intermediate");
							intermediateMaximalElementSubsumedElementSetMap
									.remove(a.originalAbstractionActivities);
						} else {
							// If it is not subsumed, still we need to remove
							// from intermediateMap the original one and replace
							// it with the modified abstraction
							// System.out.println("Removing "+a.originalAbstractionActivities+" from intermediate");
							// System.out.println("Adding "+a.selectedActivities+"@"+selectedAbstractionFilteredSubsumedElementSet+" to intermediate");
							intermediateMaximalElementSubsumedElementSetMap
									.remove(a.originalAbstractionActivities);
							intermediateMaximalElementSubsumedElementSetMap
									.put(a.selectedActivities,
											selectedAbstractionFilteredSubsumedElementSet);
							removeAbstractionConfigurationComponentList.add(a);
							addAbstractionConfigurationComponentList
									.add(new AbstractionResultConfigurationComponent(
											"Abs " + noAbstractions++,
											a.selectedActivities));
							/**
							 * Also we need ot keep track of the orignal repeat
							 * alphabet and modified repeat alphabet
							 */
							// TODO TO-DO above clause
						}
						a.isModified = false;
					}
				}

				for (AbstractionResultConfigurationComponent a : removeAbstractionConfigurationComponentList)
					abstracionResultConfigurationComponentList.remove(a);

				for (AbstractionResultConfigurationComponent b : addAbstractionConfigurationComponentList)
					abstracionResultConfigurationComponentList.add(b);

				abstractionResultPanel.removeAll();
				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList)
					abstractionResultPanel.add(a);
				abstractionCountLabel.setText("No. Abstractions: "
						+ abstracionResultConfigurationComponentList.size());

				revalidate();
				repaint();
			}
		});

		SlickerButton transformLogButton = new SlickerButton("Transform Log");
		transformLogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * Check if there are any modified abstractions and that the
				 * refresh button is not clicked
				 */
				boolean hasModified = false;

				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (a.isModified) {
						hasModified = true;
						break;
					}
				}
				if (hasModified) {
					MainUI
							.getInstance()
							.showGlassDialog(
									"Action Required",
									"Certain Abstractions are modified; Click Refresh Buttons and then Click Transform Log");
					return;
				}

				/**
				 * Check if there are more than one abstraction alphabet with
				 * the same abstraction name; this would be the case when the
				 * user hasn't given any meaningful names and left it as per the
				 * default names based on repeats
				 */

				HashSet<String> abstractionNameSet = new HashSet<String>();
				HashSet<String> duplicateAbstractionNameSet = new HashSet<String>();

				for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
					if (abstractionNameSet.contains(a.getAbstractionName()))
						duplicateAbstractionNameSet.add(a.getAbstractionName());
					else
						abstractionNameSet.add(a.getAbstractionName());
				}

				if (duplicateAbstractionNameSet.size() > 0) {
					MainUI
							.getInstance()
							.showGlassDialog(
									"Action Required",
									"There exist more than one abstraction with the same name \n"
											+ duplicateAbstractionNameSet
											+ " \n\n Modify the abstraction name and then Click Transform Log");
					return;
				}

				/**
				 * Pop up a file save dialog
				 */
				JFrame saveFileFrame = new JFrame();
				File file = null;
				JFileChooser fc = new JFileChooser();
				File fFile = new File("LoopRemoved.mxml.gz");

				// Start in current directory
				fc.setCurrentDirectory(new File("."));

				// Set to a default name for save.
				fc.setSelectedFile(fFile);

				// Open chooser dialog
				int result = fc.showSaveDialog(saveFileFrame);

				if (result == JFileChooser.CANCEL_OPTION) {
					return;
				} else if (result == JFileChooser.APPROVE_OPTION) {
					fFile = fc.getSelectedFile();
					if (fFile.exists()) {
						int response = JOptionPane.showConfirmDialog(null,
								"Overwrite existing file?",
								"Confirm Overwrite",
								JOptionPane.OK_CANCEL_OPTION,
								JOptionPane.QUESTION_MESSAGE);
						if (response == JOptionPane.CANCEL_OPTION)
							return;
					}
					transformLogFileName = fFile.getAbsolutePath();
					// System.out.println("FileName: "+fFile.getAbsolutePath());
				}

				// int noProcessInstances =
				// LoopAbstractionUI.log.getInstances().size();
				// System.out.println("No. Instances: "+noProcessInstances);

				getAbstractionNames();

				// for(int i = 0; i < noProcessInstances; i++){
				// processInstanceWithAbstractions(LoopAbstractionUI.log.getInstance(i));
				// }

				processAlphabetRepeatMap();
				// System.out.println("Original Repeat Alphabet - New Repeat Alphabet Size: "+originalRepeatAlphabetNewRepeatAlphabetMap.size());
				// for(TreeSet<String> originalRepeatAlphabet :
				// originalRepeatAlphabetNewRepeatAlphabetMap.keySet()){
				// System.out.println(originalRepeatAlphabet+ " @ "+
				// originalRepeatAlphabetNewRepeatAlphabetMap.get(originalRepeatAlphabet));
				// }

				processLogWithAbstractions();
				// System.out.println("Done");
			}
		});

		abstractionCountLabel = new JLabel("No. Abstractions: " + 0);

		abstractionLowerPanel.add(abstractionCountLabel);

		abstractionLowerPanel.add(Box.createHorizontalStrut(150));
		abstractionLowerPanel.add(mergeAbstractionsButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(removeAbstractionButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(refreshAbstractionButton);

		abstractionLowerPanel.add(Box.createHorizontalStrut(5));
		abstractionLowerPanel.add(transformLogButton);

		upperPanel.add(abstractionPanel);
		upperPanel.add(abstractionLowerPanel);

		this.add(upperPanel, BorderLayout.CENTER);
		this.add(lowerPanel, BorderLayout.SOUTH);

	}

	private JPanel prepareLowerPanel() {
		JPanel lowerPanel = new JPanel();
		lowerPanel.setOpaque(true);
		lowerPanel.setLayout(new BorderLayout());
		lowerPanel.setBackground(new Color(40, 40, 40));
		lowerPanel.setBorder(BorderFactory.createEmptyBorder());

		RoundedPanel tandemRepeatPanel = new RoundedPanel(10, 5, 5);
		tandemRepeatPanel.setBackground(new Color(90, 90, 90));
		tandemRepeatPanel.setLayout(new BoxLayout(tandemRepeatPanel,
				BoxLayout.X_AXIS));

		prepareTandemRepeatInfo();

		JScrollPane tandemRepeatInfoScrollPane = new JScrollPane(table);

		JPanel tandemRepeatParameterPanel = new JPanel();
		tandemRepeatParameterPanel.setOpaque(true);
		tandemRepeatParameterPanel.setLayout(new BoxLayout(
				tandemRepeatParameterPanel, BoxLayout.Y_AXIS));
		tandemRepeatParameterPanel.setBackground(new Color(100, 100, 100));
		tandemRepeatParameterPanel.setBorder(BorderFactory.createEmptyBorder());

		noAlphabetsLabel = new JLabel("No. Alphabets: " + displayData.length);
		freqFilterLabel = new JLabel("Freq Filter Threshold: " + 0);

		tandemRepeatFreqFilterSlider = new JSlider(JSlider.VERTICAL, 0, 100, 1);
		tandemRepeatFreqFilterSlider.setOpaque(false);
		tandemRepeatFreqFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				tandemRepeatFreqFilterThreshold = tandemRepeatFreqFilterSlider
						.getValue();

				if (tandemRepeatFreqFilterSlider.getValueIsAdjusting() == false) {
					filterTandemRepeats();
					table.setModel(new AlphabetRepeatTableModel(displayData));
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
						+ tandemRepeatFreqFilterThreshold);
				revalidate();
				repaint();
			}
		});

		sizeFilterLabel = new JLabel("Alphabet Size. Threshold: " + 0);
		tandemRepeatSizeFilterSlider = new JSlider(JSlider.VERTICAL, 0, 5, 1);
		tandemRepeatSizeFilterSlider.setValue(0);
		tandemRepeatSizeFilterSlider.setOpaque(false);
		tandemRepeatSizeFilterSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				tandemRepeatSizeFilterThreshold = tandemRepeatSizeFilterSlider
						.getValue();

				if (tandemRepeatSizeFilterSlider.getValueIsAdjusting() == false) {
					filterTandemRepeats();
					table.setModel(new AlphabetRepeatTableModel(displayData));
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
				sizeFilterLabel.setText("Alphabet Size. Threshold: "
						+ tandemRepeatSizeFilterThreshold);
				revalidate();
				repaint();
			}
		});

		tandemRepeatParameterPanel.add(noAlphabetsLabel);

		JPanel tandemRepeatFreqFilterPanel = new JPanel();
		tandemRepeatFreqFilterPanel.setOpaque(true);
		tandemRepeatFreqFilterPanel.setBackground(new Color(140, 140, 140));

		tandemRepeatFreqFilterPanel.setLayout(new BoxLayout(
				tandemRepeatFreqFilterPanel, BoxLayout.Y_AXIS));
		tandemRepeatFreqFilterPanel.add(tandemRepeatFreqFilterSlider);
		tandemRepeatFreqFilterPanel.add(Box.createVerticalStrut(5));
		tandemRepeatFreqFilterPanel.add(freqFilterLabel);

		JPanel tandemRepeatSizeFilterPanel = new JPanel();
		tandemRepeatSizeFilterPanel.setOpaque(true);
		tandemRepeatSizeFilterPanel.setBackground(new Color(140, 140, 140));

		tandemRepeatSizeFilterPanel.setLayout(new BoxLayout(
				tandemRepeatSizeFilterPanel, BoxLayout.Y_AXIS));
		tandemRepeatSizeFilterPanel.add(tandemRepeatSizeFilterSlider);
		tandemRepeatSizeFilterPanel.add(Box.createVerticalStrut(5));
		tandemRepeatSizeFilterPanel.add(sizeFilterLabel);

		JPanel tandemRepeatFilterPanel = new JPanel();
		tandemRepeatFilterPanel.setOpaque(true);
		tandemRepeatFilterPanel.setBackground(new Color(40, 40, 40));

		tandemRepeatFilterPanel.setLayout(new BoxLayout(
				tandemRepeatFilterPanel, BoxLayout.X_AXIS));
		tandemRepeatFilterPanel.add(tandemRepeatFreqFilterPanel);
		tandemRepeatFilterPanel.add(Box.createHorizontalStrut(5));
		tandemRepeatFilterPanel.add(tandemRepeatSizeFilterPanel);

		tandemRepeatParameterPanel.add(tandemRepeatFilterPanel);
		JScrollPane tandemRepeatParameterScrollPane = new JScrollPane(
				tandemRepeatParameterPanel);

		tandemRepeatPanel.add(tandemRepeatInfoScrollPane);
		tandemRepeatPanel.add(Box.createHorizontalStrut(5));
		tandemRepeatPanel.add(tandemRepeatParameterScrollPane);
		lowerPanel.add(tandemRepeatPanel, BorderLayout.CENTER);

		return lowerPanel;
	}

	private void prepareAbstractionStrategyPanel() {

		structuredRadioButton = new JRadioButton("Structured");
		structuredRadioButton.setOpaque(false);
		structuredRadioButton.setBackground(new Color(140, 140, 140));
		structuredRadioButton.setSelected(false);
		// structuredRadioButton.setEnabled(false);
		structuredRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (structuredRadioButton.isSelected())
					isStructured = true;
			}
		});
		unstructuredRadioButton = new JRadioButton("UnStructured");
		unstructuredRadioButton.setOpaque(false);
		unstructuredRadioButton.setBackground(new Color(140, 140, 140));
		unstructuredRadioButton.setSelected(true);
		// unstructuredRadioButton.setEnabled(true);
		unstructuredRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionEvent) {
				if (unstructuredRadioButton.isSelected())
					isStructured = false;
			}
		});

		abstractionStrategyButtonGroup = new ButtonGroup();
		abstractionStrategyButtonGroup.add(structuredRadioButton);
		abstractionStrategyButtonGroup.add(unstructuredRadioButton);

		abstractionStrategyPanel = new JPanel();
		abstractionStrategyPanel.setOpaque(false);
		abstractionStrategyPanel.setBackground(new Color(140, 140, 140));
		abstractionStrategyPanel.setLayout(new BoxLayout(
				abstractionStrategyPanel, BoxLayout.X_AXIS));
		abstractionStrategyPanel.add(structuredRadioButton);
		abstractionStrategyPanel.add(Box.createHorizontalStrut(5));
		abstractionStrategyPanel.add(unstructuredRadioButton);
	}

	private void prepareTandemRepeatInfo() {
		table = new JTable(new AlphabetRepeatTableModel(displayData));
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
		 * HV: Lines removed due to errors in nightly build: [javac]
		 * D:/Hudson_home/jobs/ProM5 Nightly
		 * build/workspace/trunk/src/plugins/org
		 * /processmining/analysis/abstractions
		 * /ui/LoopAbstractionResUI.java:1072: cannot find symbol [javac] symbol
		 * : method setFillsViewportHeight(boolean) [javac] location: class
		 * javax.swing.JTable [javac] table.setFillsViewportHeight(true);
		 * [javac] ^ [javac] D:/Hudson_home/jobs/ProM5 Nightly
		 * build/workspace/trunk
		 * /src/plugins/org/processmining/analysis/abstractions
		 * /ui/LoopAbstractionResUI.java:1073: cannot find symbol [javac] symbol
		 * : method setAutoCreateRowSorter(boolean) [javac] location: class
		 * javax.swing.JTable [javac] table.setAutoCreateRowSorter(true);
		 * [javac] ^ table.setFillsViewportHeight(true);
		 * table.setAutoCreateRowSorter(true);
		 */
	}

	private void prepareAbstractionResult(
			List<TreeSet<String>> maximalElementList) {
		ArrayList<String> abstractionNameList = new ArrayList<String>();
		ArrayList<Set<String>> abstractionSymbolDecodedList = new ArrayList<Set<String>>();
		this.noAbstractions = 0;
		for (TreeSet<String> maximalElement : maximalElementList) {
			abstractionNameList.add("Abs " + noAbstractions++);
			abstractionSymbolDecodedList.add(maximalElement);
		}

		if (abstractionResultPanel != null)
			abstractionResultPanel.removeAll();
		else {
			abstractionResultPanel = new JPanel();
		}
		abstractionResultPanel.setBackground(colorBg);
		abstractionResultPanel.setLayout(new BoxLayout(abstractionResultPanel,
				BoxLayout.Y_AXIS));
		abstractionResultPanel.setBorder(BorderFactory.createEmptyBorder());

		abstracionResultConfigurationComponentList.clear();
		AbstractionResultConfigurationComponent arc;
		for (int i = 0; i < abstractionNameList.size(); i++) {
			arc = new AbstractionResultConfigurationComponent(
					abstractionNameList.get(i), abstractionSymbolDecodedList
							.get(i));
			abstracionResultConfigurationComponentList.add(arc);
			abstractionResultPanel.add(arc);
		}

		if (abstractionResultScrollPane == null) {
			abstractionResultScrollPane = new JScrollPane(
					abstractionResultPanel);
		}
		abstractionResultScrollPane.setBorder(BorderFactory
				.createLineBorder(new Color(90, 90, 90)));
		abstractionResultScrollPane.setBackground(colorBg);
		// abstractionResultScrollPane.setMinimumSize(new Dimension(400, 1000));
		// abstractionResultScrollPane.setMaximumSize(new Dimension(430, 1000));
		// abstractionResultScrollPane.setPreferredSize(new Dimension(420,
		// 1000));
		abstractionResultScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		abstractionResultScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		abstractionResultScrollPane.getVerticalScrollBar()
				.setBlockIncrement(25);
		abstractionResultScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		abstractionCountLabel.setText("No. Abstractions: "
				+ abstracionResultConfigurationComponentList.size());
		// abstractionResultPanel.repaint();
		// abstractionResultScrollPane.repaint();
	}

	private void prepareAbstractionResult() {
		int[] sortedLengths = new int[lengthMaximalElementMap.size()];
		int index = 0;
		for (Integer I : lengthMaximalElementMap.keySet()) {
			sortedLengths[index++] = I.intValue();
			// System.out.println(lengthMaximalElementMap.get(I));
		}
		Arrays.sort(sortedLengths);

		int noDistinctLengths = sortedLengths.length;
		index = 0;
		ArrayList<String> abstractionNameList = new ArrayList<String>();
		ArrayList<Set<String>> abstractionSymbolDecodedList = new ArrayList<Set<String>>();

		ArrayList<TreeSet<String>> lengthMaximalElementList;
		noAbstractions = 0;
		for (int i = noDistinctLengths - 1; i >= 0; i--) {
			lengthMaximalElementList = lengthMaximalElementMap
					.get(sortedLengths[i]);
			for (TreeSet<String> maximalElement : lengthMaximalElementList) {
				abstractionNameList.add("Abs " + noAbstractions++);
				abstractionSymbolDecodedList.add(maximalElement);
			}
		}

		if (abstractionResultPanel != null)
			abstractionResultPanel.removeAll();
		else {
			abstractionResultPanel = new JPanel();
		}
		abstractionResultPanel.setBackground(colorBg);
		abstractionResultPanel.setLayout(new BoxLayout(abstractionResultPanel,
				BoxLayout.Y_AXIS));
		abstractionResultPanel.setBorder(BorderFactory.createEmptyBorder());

		abstracionResultConfigurationComponentList.clear();
		AbstractionResultConfigurationComponent arc;
		for (int i = 0; i < abstractionNameList.size(); i++) {
			arc = new AbstractionResultConfigurationComponent(
					abstractionNameList.get(i), abstractionSymbolDecodedList
							.get(i));
			abstracionResultConfigurationComponentList.add(arc);
			abstractionResultPanel.add(arc);
		}

		if (abstractionResultScrollPane == null) {
			abstractionResultScrollPane = new JScrollPane(
					abstractionResultPanel);
		}
		abstractionResultScrollPane.setBorder(BorderFactory
				.createLineBorder(new Color(90, 90, 90)));
		abstractionResultScrollPane.setBackground(colorBg);
		// abstractionResultScrollPane.setMinimumSize(new Dimension(400, 1000));
		// abstractionResultScrollPane.setMaximumSize(new Dimension(430, 1000));
		// abstractionResultScrollPane.setPreferredSize(new Dimension(420,
		// 1000));
		abstractionResultScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		abstractionResultScrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		abstractionResultScrollPane.getVerticalScrollBar()
				.setBlockIncrement(25);
		abstractionResultScrollPane.getVerticalScrollBar().setUnitIncrement(25);
		// abstractionResultPanel.repaint();
		// abstractionResultScrollPane.repaint();
	}

	@SuppressWarnings("unchecked")
	private void filterTandemRepeats() {
		int noElements = originalData.length;
		int index = 0, noAboveThreshold = 0;

		for (int i = 0; i < noElements; i++) {
			if (((Integer) originalData[i][2]).intValue() > tandemRepeatFreqFilterThreshold
					&& ((TreeSet<String>) originalData[i][0]).size() > tandemRepeatSizeFilterThreshold) {
				noAboveThreshold++;
			}
		}

		displayData = new Object[noAboveThreshold][3];
		for (int i = 0; i < noElements; i++) {
			if (((Integer) originalData[i][2]).intValue() > tandemRepeatFreqFilterThreshold
					&& ((TreeSet<String>) originalData[i][0]).size() > tandemRepeatSizeFilterThreshold) {
				displayData[index][0] = originalData[i][0];
				displayData[index][1] = originalData[i][1];
				displayData[index++][2] = originalData[i][2];
			}
		}
	}

	class AlphabetRepeatTableModel extends AbstractTableModel {
		private String[] columnNames = { "Repeat Alphabet",
				"Tandem Repeat Set", "Count" };

		private Object[][] data;

		public AlphabetRepeatTableModel(Object[][] data) {
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

		/*
		 * JTable uses this method to determine the default renderer/ editor for
		 * each cell. If we didn't implement this method, then the last column
		 * would contain text ("true"/"false"), rather than a check box.
		 */
		@SuppressWarnings("unchecked")
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's editable.
		 */
		public boolean isCellEditable(int row, int col) {
			// Note that the data/cell address is constant,
			// no matter where the cell appears onscreen.
			if (col < 2) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's data can
		 * change.
		 */
		public void setValueAt(Object value, int row, int col) {

			data[row][col] = value;
		}
	}

	class TextAreaRenderer extends JTextArea implements TableCellRenderer {
		private final DefaultTableCellRenderer adaptee = new DefaultTableCellRenderer();
		/** map from table to map of rows to map of column heights */
		private final Map cellSizes = new HashMap();

		public TextAreaRenderer() {
			setLineWrap(true);
			setWrapStyleWord(true);
		}

		public Component getTableCellRendererComponent(
				//
				JTable table, Object obj, boolean isSelected, boolean hasFocus,
				int row, int column) {
			// set the colours, etc. using the standard for that platform
			adaptee.getTableCellRendererComponent(table, obj, isSelected,
					hasFocus, row, column);
			setForeground(adaptee.getForeground());
			setBackground(adaptee.getBackground());
			setBorder(adaptee.getBorder());
			setFont(adaptee.getFont());
			setText(adaptee.getText());

			// This line was very important to get it working with JDK1.4
			TableColumnModel columnModel = table.getColumnModel();
			setSize(columnModel.getColumn(column).getWidth(), 100000);
			int height_wanted = (int) getPreferredSize().getHeight();
			addSize(table, row, column, height_wanted);
			height_wanted = findTotalMaximumRowSize(table, row);
			if (height_wanted != table.getRowHeight(row)) {
				table.setRowHeight(row, height_wanted);
			}
			return this;
		}

		private void addSize(JTable table, int row, int column, int height) {
			Map rows = (Map) cellSizes.get(table);
			if (rows == null) {
				cellSizes.put(table, rows = new HashMap());
			}
			Map rowheights = (Map) rows.get(new Integer(row));
			if (rowheights == null) {
				rows.put(new Integer(row), rowheights = new HashMap());
			}
			rowheights.put(new Integer(column), new Integer(height));
		}

		/**
		 * Look through all columns and get the renderer. If it is also a
		 * TextAreaRenderer, we look at the maximum height in its hash table for
		 * this row.
		 */
		private int findTotalMaximumRowSize(JTable table, int row) {
			int maximum_height = 0;
			Enumeration columns = table.getColumnModel().getColumns();
			while (columns.hasMoreElements()) {
				TableColumn tc = (TableColumn) columns.nextElement();
				TableCellRenderer cellRenderer = tc.getCellRenderer();
				if (cellRenderer instanceof TextAreaRenderer) {
					TextAreaRenderer tar = (TextAreaRenderer) cellRenderer;
					maximum_height = Math.max(maximum_height, tar
							.findMaximumRowSize(table, row));
				}
			}
			return maximum_height;
		}

		private int findMaximumRowSize(JTable table, int row) {
			Map rows = (Map) cellSizes.get(table);
			if (rows == null)
				return 0;
			Map rowheights = (Map) rows.get(new Integer(row));
			if (rowheights == null)
				return 0;
			int maximum_height = 0;
			for (Iterator it = rowheights.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				int cellHeight = ((Integer) entry.getValue()).intValue();
				maximum_height = Math.max(maximum_height, cellHeight);
			}
			return maximum_height;
		}
	}

	class TextAreaEditor extends DefaultCellEditor {
		public TextAreaEditor() {
			super(new JTextField());
			final JTextArea textArea = new JTextArea();
			textArea.setWrapStyleWord(true);
			textArea.setLineWrap(true);
			JScrollPane scrollPane = new JScrollPane(textArea);
			scrollPane.setBorder(null);
			editorComponent = scrollPane;

			delegate = new DefaultCellEditor.EditorDelegate() {
				public void setValue(Object value) {
					textArea.setText((value != null) ? value.toString() : "");
				}

				public Object getCellEditorValue() {
					return textArea.getText();
				}
			};
		}
	}

	public String encodeActivity(AuditTrailEntry ate) {
		return ate.getElement() + "--" + ate.getType();
	}

	private void processInstanceWithAbstractions(ProcessInstance pi) {
		try {
			String currentCharStream = "";
			AuditTrailEntryList ateList = pi.getAuditTrailEntryList();
			for (int a = 0; a < ateList.size(); a++) {
				currentCharStream += activityCharMap.get(encodeActivity(ateList
						.get(a)));
			}
		} catch (IndexOutOfBoundsException e) {
			// System.out.println("Index Out of Bounds Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		} catch (IOException e) {
			// System.out.println("IO Exception while Reading Audit TrailEntryList in LoopAbstractionUI scanThread");
		}
	}

	/**
	 * Get the abstraction names for the abstraction alphabet; this is required
	 * for performance enhancements; While transforming the log, when a
	 * subsequence matching a repeat pattern is found, it needs to be replaced
	 * with the abstraction name In case this map is not stored, we need a
	 * sequential scan over all the abstraction alphabets to find out the name
	 */
	private void getAbstractionNames() {
		// System.out.println("In GetAbstractionNames");
		abstractionAlphabetAbstractionNameMap = new HashMap<TreeSet<String>, String>();
		// boolean foundAbstractionName;
		// System.out.println(intermediateMaximalElementSubsumedElementSetMap.size()+" @ "+abstracionResultConfigurationComponentList.size());
		// for(TreeSet<String> abstractionAlphabet :
		// intermediateMaximalElementSubsumedElementSetMap.keySet()){
		// foundAbstractionName = false;
		for (AbstractionResultConfigurationComponent a : abstracionResultConfigurationComponentList) {
			// if(a.selectedActivities.equals(abstractionAlphabet)){
			// System.out.println(abstractionAlphabet+" @ "+a.getAbstractionName());
			abstractionAlphabetAbstractionNameMap.put(a.selectedActivities, a
					.getAbstractionName());
			// foundAbstractionName = true;
			// break;
			// }
		}
		// if(!foundAbstractionName){
		// System.out.println("Couldn't find abstraction name; Something wrong when comparing abstraction configuration component with the abstraction alphabet: "+abstractionAlphabet);
		// System.exit(0);
		// System.out.println(abstractionAlphabet+" @ NONAME");
		// }
		// }
	}

	private void processAlphabetRepeatMap() {
		// System.out.println("In Process AlphabetRepeat map");
		/**
		 * Due to the removal of certain symbols, the repeat alphabets that were
		 * earlier there may not exist; We need to remove such repeat alphabets;
		 * 
		 * For e.g., let {a,b,c} be an original repeat alphabet; let acb cab are
		 * two repeats of this alphabet Now, suppose that b is removed from the
		 * alphabet, then the repeats should be modified accordingly to ac and
		 * ca *
		 */
		HashSet<TreeSet<String>> toRemoveRepeatAlphabetSet = new HashSet<TreeSet<String>>();
		/*
		 * Look in the original repeat alphabet modified repeat alphabet map and
		 * get the repeat alphabets that got changed
		 */

		TreeSet<TreeSet<String>> decodedModifiedAlphabetSet;
		TreeSet<String> removedSymbolSet;
		TreeSet<String> originalAlphabetRepeatSet;
		TreeSet<TreeSet<String>> modifiedRepeatSet;

		// for(TreeSet<String> originalAlphabet :
		// originalRepeatAlphabetNewRepeatAlphabetMap.keySet())
		// System.out.println(originalAlphabet+" @ "+originalRepeatAlphabetNewRepeatAlphabetMap.get(originalAlphabet).size()+" @ "+originalRepeatAlphabetNewRepeatAlphabetMap.get(originalAlphabet));

		String modifiedRepeat;

		/**
		 * The originalRepeatAlphabetNewRepeatAlphabetMap contains the decoded
		 * alphabets; First process it to have an encoded mapping; This is
		 * required because the alphabetRepeatMap is in encoded form.
		 */
		TreeSet<String> encodedOriginalAlphabet;
		TreeSet<TreeSet<String>> encodedModifiedAlphabetSet;
		TreeSet<String> encodedModifiedAlphabet;
		HashMap<TreeSet<String>, TreeSet<TreeSet<String>>> originalEncodedRepeatAlphabetNewRepeatAlphabetMap = new HashMap<TreeSet<String>, TreeSet<TreeSet<String>>>();

		for (TreeSet<String> decodedOriginalAlphabet : originalRepeatAlphabetNewRepeatAlphabetMap
				.keySet()) {
			encodedOriginalAlphabet = new TreeSet<String>();
			for (String alp : decodedOriginalAlphabet)
				encodedOriginalAlphabet.add(activityCharMap.get(alp));

			decodedModifiedAlphabetSet = originalRepeatAlphabetNewRepeatAlphabetMap
					.get(decodedOriginalAlphabet);
			encodedModifiedAlphabetSet = new TreeSet<TreeSet<String>>(
					new TreeSetComparator());

			for (TreeSet<String> decodedModifiedAlphabet : decodedModifiedAlphabetSet) {
				encodedModifiedAlphabet = new TreeSet<String>();
				for (String alp2 : decodedModifiedAlphabet)
					encodedModifiedAlphabet.add(activityCharMap.get(alp2));
				encodedModifiedAlphabetSet.add(encodedModifiedAlphabet);
			}
			originalEncodedRepeatAlphabetNewRepeatAlphabetMap.put(
					encodedOriginalAlphabet, encodedModifiedAlphabetSet);
			// System.out.println(encodedOriginalAlphabet+" @ "+encodedModifiedAlphabetSet);
		}

		TreeSet<String> repeatSet, tempRepeatSet, mrAlphabet;
		TreeSet<String> tempSet = new TreeSet<String>();

		String replaceSymbol = "";
		for (int i = 0; i < encodingLength; i++)
			replaceSymbol += "#";

		// System.out.println("AlphabetRepeatMap Size: "+alphabetRepeatMap.size());

		String[] modifiedRepeatSplit;
		String mr;
		int mrLength;
		for (TreeSet<String> encodedAlphabet : originalEncodedRepeatAlphabetNewRepeatAlphabetMap
				.keySet()) {
			if (alphabetRepeatMap.containsKey(encodedAlphabet)) {
				repeatSet = alphabetRepeatMap.get(encodedAlphabet);
				encodedModifiedAlphabetSet = originalEncodedRepeatAlphabetNewRepeatAlphabetMap
						.get(encodedAlphabet);
				for (TreeSet<String> modifiedAlphabet : encodedModifiedAlphabetSet) {
					tempSet.clear();
					tempSet.addAll(encodedAlphabet);
					tempSet.removeAll(modifiedAlphabet);

					/**
					 * For each repeat, split the repeat based on the removed
					 * alphabets; get the substrings after the removal Now these
					 * substrings can either define a new alphabet or can be
					 * part of existing alphabet In case if the new split repeat
					 * alphabet already exists, put the repeat in its repeat set
					 * else create a new alphabetRepeatMap with the split repeat
					 */
					for (String repeat : repeatSet) {
						modifiedRepeat = repeat;
						for (String symbol : tempSet)
							modifiedRepeat = modifiedRepeat.replaceAll(symbol,
									replaceSymbol);
						modifiedRepeatSplit = modifiedRepeat
								.split(replaceSymbol);
						for (int i = 0; i < modifiedRepeatSplit.length; i++) {
							mr = modifiedRepeatSplit[i].trim();
							mrLength = mr.length() / encodingLength;
							mrAlphabet = new TreeSet<String>();
							if (mrLength > 0) {
								for (int j = 0; j < mrLength; j++)
									mrAlphabet.add(mr.substring(j
											* encodingLength, (j + 1)
											* encodingLength));
								if (alphabetRepeatMap.containsKey(mrAlphabet)) {
									tempRepeatSet = alphabetRepeatMap
											.get(mrAlphabet);
									if (!tempRepeatSet.contains(mr)) {
										tempRepeatSet.add(mr);
										alphabetRepeatMap.put(mrAlphabet,
												tempRepeatSet);
									}
								} else {
									// System.out.println("Adding new alphabet and repeat to alphabetRepeatMap: "+mr+"@"+mrAlphabet);
									tempRepeatSet = new TreeSet<String>();
									tempRepeatSet.add(mr);
									alphabetRepeatMap.put(mrAlphabet,
											tempRepeatSet);
								}
							}
						}
					}
				}
			} else {
				/**
				 * It could be quite ok for the alphbaet repeat map to not have
				 * the alphabet; this would be the case when transform log has
				 * been invoked multiple times; like you do some modifications
				 * to the abstractions and click transform log you later do some
				 * more modifications and click transform log again;
				 * 
				 * now the alphabetRepeatMap would have been modified after the
				 * first transform log and appropriate actions would have been
				 * taken; on the second invocation, we would not see the
				 * originalAlphabet
				 * 
				 * To avoid this, we need to clean the
				 * originalRepeatAlphabetNewRepeatAlphabet
				 */
				// System.out.println("In Process Alphabet Repeat Map");
				// System.out.println("The alphabet doesn't exist in the alphabet repeat map: "+encodedAlphabet);
				// System.exit(0);
			}
		}

		/**
		 * Clean the originalRepeatAlphabetNewRepeatALphabetMap
		 */

		originalRepeatAlphabetNewRepeatAlphabetMap.clear();

		/**
		 * Now that all alphabets that have been modified are processed Remove
		 * all alphabets that have been modified from the alphabetRepeatMap
		 */

		for (TreeSet<String> encodedAlphabet : originalEncodedRepeatAlphabetNewRepeatAlphabetMap
				.keySet()) {
			if (alphabetRepeatMap.containsKey(encodedAlphabet))
				alphabetRepeatMap.remove(encodedAlphabet);
		}

		// System.out.println("Final AlphabetRepeatMap Size: "+alphabetRepeatMap.size());

		/**
		 * Prepare the alphabet abstractionNameSet Map; Not all alphabets have
		 * an abstraction. Those symbols would be left untouched in the
		 * transformation of logs For each alphabet, get the abstraction name;
		 * There can be cases where a repeat alphabet contributes to more than
		 * one abstraction; Get all the abstractions for which the repeat
		 * alphabet contributes to; THe
		 * intermediateMaximalElementSubsumedElementMap contains the information
		 */

		/*
		 * The intermediateMaximalElementSubsumedElementMap would contain
		 * decodedAlphabets; We need to first get a decoded-encoded map
		 */

		TreeSet<TreeSet<String>> abstractionAlphabetSubsumedAlphabetSet = new TreeSet<TreeSet<String>>();

		HashMap<TreeSet<String>, TreeSet<String>> decodedEncodedAlphabetMap = new HashMap<TreeSet<String>, TreeSet<String>>();

		for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
				.keySet()) {
			if (!decodedEncodedAlphabetMap.containsKey(abstractionAlphabet)) {
				encodedOriginalAlphabet = new TreeSet<String>();
				for (String decodedSymbol : abstractionAlphabet) {
					encodedOriginalAlphabet.add(activityCharMap
							.get(decodedSymbol));
				}
				decodedEncodedAlphabetMap.put(abstractionAlphabet,
						encodedOriginalAlphabet);
			}
			abstractionAlphabetSubsumedAlphabetSet = intermediateMaximalElementSubsumedElementSetMap
					.get(abstractionAlphabet);
			for (TreeSet<String> subsumedAlphabet : abstractionAlphabetSubsumedAlphabetSet) {
				if (!decodedEncodedAlphabetMap.containsKey(subsumedAlphabet)) {
					encodedOriginalAlphabet = new TreeSet<String>();
					for (String decodedSymbol : subsumedAlphabet) {
						encodedOriginalAlphabet.add(activityCharMap
								.get(decodedSymbol));
					}
					decodedEncodedAlphabetMap.put(subsumedAlphabet,
							encodedOriginalAlphabet);
				}
			}
		}

		alphabetAbstractionNameSetMap = new HashMap<TreeSet<String>, TreeSet<String>>();
		TreeSet<String> alphabetAbstractionNameSet;
		for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
				.keySet()) {

			if (alphabetRepeatMap.containsKey(decodedEncodedAlphabetMap
					.get(abstractionAlphabet))) {
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
				alphabetAbstractionNameSetMap.put(decodedEncodedAlphabetMap
						.get(abstractionAlphabet), alphabetAbstractionNameSet);
			}

			abstractionAlphabetSubsumedAlphabetSet.clear();
			abstractionAlphabetSubsumedAlphabetSet
					.addAll(intermediateMaximalElementSubsumedElementSetMap
							.get(abstractionAlphabet));
			for (TreeSet<String> subsumedAlphabet : abstractionAlphabetSubsumedAlphabetSet) {
				if (alphabetRepeatMap.containsKey(decodedEncodedAlphabetMap
						.get(subsumedAlphabet))) {
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
					alphabetAbstractionNameSetMap.put(decodedEncodedAlphabetMap
							.get(subsumedAlphabet), alphabetAbstractionNameSet);
				}
				// else{
				// System.out.println("Wrong: Subsumed alphabet "+decodedEncodedAlphabetMap.get(subsumedAlphabet)+" in abs "+decodedEncodedAlphabetMap.get(abstractionAlphabet)+" not present in alphabet Repeat Map");
				// System.exit(0);
				// }
			}
		}

		/**
		 * Print all alphabets which contribute to more than one abstraction
		 */
		// System.out.println("Size: "+alphabetAbstractionNameSetMap.size());
		// for(TreeSet<String> alphabet :
		// alphabetAbstractionNameSetMap.keySet())
		// System.out.println(alphabet+ " @ "
		// +alphabetAbstractionNameSetMap.get(alphabet));
		// System.out.println("Alphabets with more than one abstraction name Start: ");
		// for(TreeSet<String> alphabet :
		// alphabetAbstractionNameSetMap.keySet()){
		// if(alphabetAbstractionNameSetMap.get(alphabet).size() > 1)
		// System.out.println(alphabet+" @ "+alphabetAbstractionNameSetMap.get(alphabet));
		// }
		// System.out.println("Alphabets with more than one abstraction name End");
		/**
		 * Find alphabets for which there is no name
		 */
		HashSet<TreeSet<String>> tempSet1 = new HashSet<TreeSet<String>>();
		tempSet1.addAll(alphabetRepeatMap.keySet());
		tempSet1.removeAll(alphabetAbstractionNameSetMap.keySet());
		// System.out.println("No. Alphabets with no Abstraction: "+tempSet1.size());
		// for(TreeSet<String> alp : tempSet1)
		// System.out.println(alp);

		missedAlphabetAbstractionNameSetMap = new HashMap<TreeSet<String>, HashSet<String>>();
		HashSet<String> missedAlphabetAbstractionNameSet;
		for (TreeSet<String> alphabet : tempSet1) {
			for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
					.keySet()) {
				if (decodedEncodedAlphabetMap.get(abstractionAlphabet)
						.containsAll(alphabet)) {
					if (missedAlphabetAbstractionNameSetMap
							.containsKey(alphabet)) {
						missedAlphabetAbstractionNameSet = missedAlphabetAbstractionNameSetMap
								.get(alphabet);
					} else {
						missedAlphabetAbstractionNameSet = new HashSet<String>();
					}
					missedAlphabetAbstractionNameSet
							.add(abstractionAlphabetAbstractionNameMap
									.get(abstractionAlphabet));
					missedAlphabetAbstractionNameSetMap.put(alphabet,
							missedAlphabetAbstractionNameSet);
				}
			}

		}

		// System.out.println("No.Missed Alphabets; "+missedAlphabetAbstractionNameSetMap.size());
		// for(TreeSet<String> alphabet :
		// missedAlphabetAbstractionNameSetMap.keySet())
		// System.out.println(alphabet+
		// " @ "+missedAlphabetAbstractionNameSetMap.get(alphabet));

		/**
		 * Recompute alphabets with noAbstraction Some of the initial alphabets
		 * with no abstraction would have been resolved in
		 * missedAlphabetAbstractionNameSetMap
		 */

		tempSet1.removeAll(missedAlphabetAbstractionNameSetMap.keySet());

		// System.out.println("No. Alphabets with No Abstraction (Refined): "+tempSet1.size());

		/**
		 * Generate the repeat alphabet map; Consider only repeats for which
		 * abstraction is defined; the other repeats can be filtered repeats
		 * Again in filtered repeats, ignore completely repeats based on
		 * frequency filtering however, consider repeats whose size is 1
		 * irrespective of its frequency
		 */
		repeatAlphabetMap = new HashMap<String, TreeSet<String>>();
		for (TreeSet<String> alphabet : alphabetRepeatMap.keySet()) {
			// if((!tempSet1.contains(alphabet)) || (tempSet1.contains(alphabet)
			// && alphabet.size()==1)){
			if ((!tempSet1.contains(alphabet))) {
				repeatSet = alphabetRepeatMap.get(alphabet);
				for (String repeat : repeatSet)
					repeatAlphabetMap.put(repeat, alphabet);
			}
		}

		// System.out.println("No. Repeats: "+repeatAlphabetMap.size());

		startSymbolRepeatSetMap = new HashMap<String, TreeSet<String>>();
		String startSymbol;
		TreeSet<String> startSymbolRepeatSet;
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

		// for(String symbol : startSymbolRepeatSetMap.keySet())
		// System.out.println(symbol+" @ "+startSymbolRepeatSetMap.get(symbol));

	}

	protected void processLogWithAbstractions() {
		// System.out.println("In processLogWithAbstractions()");
		// System.out.println("IntermediateAbstractionSize: "+intermediateMaximalElementSubsumedElementSetMap.size()+" @ AbstractionListGUISize: "+abstracionResultConfigurationComponentList.size());

		// for(TreeSet<String> abstractionAlphabet :
		// intermediateMaximalElementSubsumedElementSetMap.keySet())
		// System.out.println(abstractionAlphabet);

		/**
		 * Find the activities involved in abstraction
		 */
		HashSet<String> activitiesInAbstractionSet = new HashSet<String>();
		for (TreeSet<String> abstractionAlphabet : intermediateMaximalElementSubsumedElementSetMap
				.keySet()) {
			for (String decodedActivity : abstractionAlphabet)
				activitiesInAbstractionSet.add(activityCharMap
						.get(decodedActivity));
		}

		// System.out.println("Activities Involved in Abstraction: "+activitiesInAbstractionSet.size());
		// for(String activity : activitiesInAbstractionSet)
		// System.out.println(activity);

		LogPersistencyStream persistency = null;
		LogPersistencyStream expandedPersistency = null;
		BufferedOutputStream out, expandedOut;
		String name, description, source;
		Process process;
		try {
			out = new BufferedOutputStream(new GZIPOutputStream(
					new FileOutputStream(transformLogFileName)));
			expandedOut = new BufferedOutputStream(new GZIPOutputStream(
					new FileOutputStream("D:\\JC\\ExpandedAbs.mxml.gz")));
			persistency = new LogPersistencyStream(out, false);
			expandedPersistency = new LogPersistencyStream(expandedOut, false);

			process = LoopAbstractionUI.log.getProcess(0);
			name = process.getName();
			if (name == null || name.length() == 0) {
				name = "UnnamedProcess";
			}
			description = process.getDescription();
			if (description == null || description.length() == 0) {
				description = name + " exported by MXMLib @ P-stable";
			}
			source = LoopAbstractionUI.log.getLogSummary().getSource()
					.getName();
			if (source == null || source.length() == 0) {
				source = "UnknownSource";
			}
			persistency.startLogfile(name, description, source);
			persistency
					.startProcess(name, description, process.getAttributes());

			expandedPersistency.startLogfile(name, description, source);
			expandedPersistency.startProcess(name, description, process
					.getAttributes());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LogException e) {
			e.printStackTrace();
		}

		int noProcessInstances = LoopAbstractionUI.log.numberOfInstances();
		ProcessInstance currentProcessInstance;
		String currentCharStream, currentSymbol, modifiedCharStream, matchingSequence, innerSymbol, innerMatchingSequence;
		int currentCharStreamLength, repeatLength, matchingSequenceLength, noMatches, noInnerMatches, innerMatchingSequenceLength, innerRepeatLength;
		TreeSet<String> repeatAlphabet = new TreeSet<String>();
		HashMap<Integer, AuditTrailEntry> abstractionIndexDetailMap;
		TreeSet<String> currentSymbolStartRepeatSet, innerSymbolStartRepeatSet;

		Pattern p, p1;
		Matcher m, m1;
		boolean repeatExists, innerRepeatExists;
		ArrayList<org.processmining.lib.mxml.AuditTrailEntry> modifiedAuditTrailEntryList;
		ArrayList<org.processmining.lib.mxml.AuditTrailEntry> expandedModifiedAuditTrailEntryList;
		org.processmining.lib.mxml.AuditTrailEntry ate;

		String abstractedCharStream;

		String abstractionSymbol = "#";
		for (int i = 1; i < encodingLength; i++)
			abstractionSymbol += "#";

		Map<String, String> ateAttributeMap;
		FileIO io = new FileIO();
		ArrayList<String> wfmeList = new ArrayList<String>();
		AuditTrailEntryList ateList;
		ProcessInstance instance = null;
		try {
			process = LoopAbstractionUI.log.getProcess(0);
			for (int i = 0; i < noProcessInstances; i++) {
				instance = process.getInstance(i);
				name = instance.getName();
				if (name == null || name.length() == 0) {
					name = "UnnamedProcessInstance";
				}
				description = instance.getDescription();
				if (description == null || description.length() == 0) {
					description = name + " exported by MXMLib @ P-stable";
				}
				persistency.startProcessInstance(name, description, instance
						.getAttributes());
				expandedPersistency.startProcessInstance(name, description,
						instance.getAttributes());
				modifiedAuditTrailEntryList = new ArrayList<org.processmining.lib.mxml.AuditTrailEntry>();

				currentProcessInstance = LoopAbstractionUI.log.getInstance(i);
				wfmeList.clear();
				ateList = currentProcessInstance.getAuditTrailEntryList();
				try {
					for (int jj = 0; jj < ateList.size(); jj++)
						wfmeList.add(ateList.get(jj).getName());
				} catch (Exception e) {
					e.printStackTrace();
				}
				// io.writeToFile("D:\\JC", "pi_"+i+".txt", wfmeList);

				currentCharStream = LoopAbstractionUI.charStreamsList.get(i);
				// System.out.println(i+" C: "+currentCharStream);

				modifiedCharStream = "";
				abstractedCharStream = "";

				currentCharStreamLength = currentCharStream.length()
						/ encodingLength;
				for (int j = 0; j < currentCharStreamLength; j++) {
					currentSymbol = currentCharStream.substring(j
							* encodingLength, (j + 1) * encodingLength);
					/**
					 * Check if there exists a pattern starting at this position
					 */
					if (startSymbolRepeatSetMap.containsKey(currentSymbol)) {
						currentSymbolStartRepeatSet = startSymbolRepeatSetMap
								.get(currentSymbol);
						repeatExists = false;
						for (String repeat : currentSymbolStartRepeatSet) {
							repeatLength = repeat.length() / encodingLength;

							p = Pattern.compile("(" + repeat + "){1,}");
							m = p.matcher(currentCharStream);

							repeatExists = false;
							// There exists a repeat starting at this position
							if (m.find(j * encodingLength)
									&& m.start() == j * encodingLength) {
								repeatExists = true;
								matchingSequence = m.group();
								matchingSequenceLength = matchingSequence
										.length()
										/ encodingLength;
								noMatches = matchingSequenceLength
										/ repeatLength;
								// System.out.println("Repeat Exists at Position: ("+j*encodingLength+","+repeat+","+noMatches+")");
								/**
								 * Check if there exists a match of a longer
								 * repeat starting within this matching sequence
								 */
								innerRepeatExists = false;
								for (int k = j + 1; k < j
										+ matchingSequenceLength; k++) {
									innerSymbol = currentCharStream.substring(k
											* encodingLength, (k + 1)
											* encodingLength);
									innerRepeatExists = false;
									if (startSymbolRepeatSetMap
											.containsKey(innerSymbol)) {
										innerSymbolStartRepeatSet = startSymbolRepeatSetMap
												.get(innerSymbol);
										for (String innerRepeat : innerSymbolStartRepeatSet) {

											if (innerRepeat.length() < repeat
													.length()) {
												/**
												 * The inner repeat is shorter
												 * than the repeat so no need to
												 * process any further
												 */
												break;
											}
											innerRepeatLength = innerRepeat
													.length()
													/ encodingLength;
											p1 = Pattern.compile("("
													+ innerRepeat + "){1,}");
											m1 = p1.matcher(currentCharStream);
											if (m1.find(k * encodingLength)
													&& m1.start() == k
															* encodingLength) {
												innerMatchingSequence = m1
														.group();
												innerMatchingSequenceLength = innerMatchingSequence
														.length()
														/ encodingLength;
												noInnerMatches = innerMatchingSequenceLength
														/ innerRepeatLength;
												if (noInnerMatches > noMatches) {
													innerRepeatExists = true;
													// System.out.println("\tLonger Repeat Exists at Position: ("+k*encodingLength+","+innerRepeat+","+noInnerMatches+")");
													noMatches = (k - j)
															/ repeatLength;
													if (noMatches >= 1) {
														modifiedCharStream += repeat;
														repeatAlphabet.clear();

														for (int jj = 0; jj < repeatLength; jj++)
															repeatAlphabet
																	.add(repeat
																			.substring(
																					jj
																							* encodingLength,
																					(jj + 1)
																							* encodingLength));

														ate = new org.processmining.lib.mxml.AuditTrailEntry();
														ate.addAttribute(
																"startIndex", j
																		+ "");
														ate.addAttribute(
																"endIndex",
																(k - 1) + "");
														// System.out.println("ra: "+repeatAlphabet+" @ "+alphabetAbstractionNameSetMap.size());
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
																		.addAttribute(
																				"resolved",
																				"false");
																ate
																		.setWorkflowModelElement(repeat);
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
																		.addAttribute(
																				"resolved",
																				"false");
																ate
																		.setWorkflowModelElement(repeat);
															}
														}
														modifiedAuditTrailEntryList
																.add(ate);
													} else {
														/**
														 * The earlier match can
														 * be of just 1
														 * iteration; Now that
														 * the longer repeat
														 * starts somewhere
														 * within the earlier
														 * repeat So, we need to
														 * append the
														 * subsequence of the
														 * earlier repeat until
														 * the start of the
														 * inner repeat
														 */
														modifiedCharStream += currentCharStream
																.substring(
																		j
																				* encodingLength,
																		k
																				* encodingLength);
														repeatAlphabet.clear();
														String tempStr = currentCharStream
																.substring(
																		j
																				* encodingLength,
																		k
																				* encodingLength);
														int tempStrLength = tempStr
																.length()
																/ encodingLength;
														for (int jj = 0; jj < tempStrLength; jj++)
															repeatAlphabet
																	.add(tempStr
																			.substring(
																					jj
																							* encodingLength,
																					(jj + 1)
																							* encodingLength));

														ate = new org.processmining.lib.mxml.AuditTrailEntry();
														if (alphabetAbstractionNameSetMap
																.containsKey(repeatAlphabet)) {
															/**
															 * The whole
															 * substring
															 * alphabet defines
															 * an abstraction;
															 * so add a single
															 * ate
															 */
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
																		.addAttribute(
																				"resolved",
																				"false");
																ate
																		.setWorkflowModelElement(repeat);
															}
															modifiedAuditTrailEntryList
																	.add(ate);
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
																		.addAttribute(
																				"resolved",
																				"false");
																ate
																		.setWorkflowModelElement(repeat);
															}
														} else {
															/**
															 * check if the
															 * repeat alphabet
															 * is wholly
															 * subsumed in other
															 * repeat alphabet;
															 * get the
															 * abstraction of
															 * that code to be
															 * written;
															 * temporarily store
															 * the repeat with
															 * status resolved =
															 * false;
															 */
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
															ate.addAttribute(
																	"resolved",
																	"false");
															ate
																	.setWorkflowModelElement(tempStr);
															modifiedAuditTrailEntryList
																	.add(ate);
														}
													}
													j = k - 1;
												}

											}
										}
										if (innerRepeatExists)
											break;
									}
								}
								if (!innerRepeatExists) {
									modifiedCharStream += repeat;
									repeatAlphabet.clear();

									for (int jj = 0; jj < repeatLength; jj++)
										repeatAlphabet.add(repeat.substring(jj
												* encodingLength, (jj + 1)
												* encodingLength));

									ate = new org.processmining.lib.mxml.AuditTrailEntry();
									ate.addAttribute("startIndex", j + "");
									ate.addAttribute("endIndex", (j
											+ matchingSequenceLength - 1)
											+ "");

									if (alphabetAbstractionNameSetMap
											.containsKey(repeatAlphabet)) {
										if (alphabetAbstractionNameSetMap.get(
												repeatAlphabet).size() == 1) {
											ate
													.setWorkflowModelElement(alphabetAbstractionNameSetMap
															.get(repeatAlphabet)
															.toString()
															.replaceAll("\\[",
																	"")
															.replaceAll("\\]",
																	""));
											ate
													.addAttribute("resolved",
															"true");
										} else {
											ate.addAttribute("resolved",
													"false");
											ate.setWorkflowModelElement(repeat);
										}
									} else if (missedAlphabetAbstractionNameSetMap
											.containsKey(repeatAlphabet)) {
										if (missedAlphabetAbstractionNameSetMap
												.get(repeatAlphabet).size() == 1) {
											ate
													.setWorkflowModelElement(missedAlphabetAbstractionNameSetMap
															.get(repeatAlphabet)
															.toString()
															.replaceAll("\\[",
																	"")
															.replaceAll("\\]",
																	""));
											ate
													.addAttribute("resolved",
															"true");
										} else {
											ate.addAttribute("resolved",
													"false");
											ate.setWorkflowModelElement(repeat);
										}
									} else {
										/**
										 * Couldn't solve this case; currently
										 * add individual symbols
										 */
										ate.setWorkflowModelElement(repeat);
										ate.addAttribute("resolved", "false");
									}
									modifiedAuditTrailEntryList.add(ate);

									j += matchingSequenceLength - 1;
									break;
								}
							}
							if (repeatExists)
								break;
						}
						if (!repeatExists) {
							modifiedCharStream += currentSymbol;

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
							modifiedAuditTrailEntryList.add(ate);
						}
					} else {
						/**
						 * no repeat exists that start with the current symbol
						 */
						modifiedCharStream += currentSymbol;
						ate = new org.processmining.lib.mxml.AuditTrailEntry();
						ate.addAttribute("startIndex", j + "");
						ate.addAttribute("endIndex", j + "");

						if (activitiesInAbstractionSet.contains(currentSymbol)) {
							ate.setWorkflowModelElement(currentSymbol);
							ate.addAttribute("resolved", "false");
						} else {
							ate.setWorkflowModelElement(charActivityMap
									.get(currentSymbol));
							ate.addAttribute("resolved", "true");
						}
						modifiedAuditTrailEntryList.add(ate);
					}
				}
				// System.out.println("M: "+modifiedCharStream);

				// System.out.println("Modified AuditTrailEntryList Size; "+modifiedAuditTrailEntryList.size());
				// for(int jj = 0; jj < modifiedAuditTrailEntryList.size();
				// jj++){
				// ate = modifiedAuditTrailEntryList.get(jj);
				// ateAttributeMap = ate.getAttributes();
				// System.out.println(ateAttributeMap.get("startIndex")+","+ateAttributeMap.get("endIndex")+","+ateAttributeMap.get("resolved")+" @ "+ate.getWorkflowModelElement());
				// }

				/**
				 * Pass 1: Process the ateList and resolve all entries
				 */

				HashMap<TreeSet<String>, HashSet<String>> resolvedSymbolAbstractionNameSetMap = new HashMap<TreeSet<String>, HashSet<String>>();
				HashSet<String> resolvedSymbolAbstractionNameSet;
				int noLookAheadEntries = 5, noLookBackEntries = 5;

				String wfme;
				int wfmeLength;
				TreeSet<String> wfmeAlphabet;

				org.processmining.lib.mxml.AuditTrailEntry pate, sate;
				boolean pfound, sfound;
				boolean resolutionFound;
				for (int jj = 0; jj < modifiedAuditTrailEntryList.size(); jj++) {
					ate = modifiedAuditTrailEntryList.get(jj);
					ateAttributeMap = ate.getAttributes();
					if (ateAttributeMap.get("resolved").equals("false")) {
						wfme = ate.getWorkflowModelElement().trim();
						wfmeLength = wfme.length() / encodingLength;
						wfmeAlphabet = new TreeSet<String>();
						for (int kk = 0; kk < wfmeLength; kk++)
							wfmeAlphabet.add(wfme.substring(
									kk * encodingLength, (kk + 1)
											* encodingLength));

						// System.out.println(wfme+" @ "+wfmeAlphabet);
						if (resolvedSymbolAbstractionNameSetMap
								.containsKey(wfmeAlphabet)) {
							resolvedSymbolAbstractionNameSet = resolvedSymbolAbstractionNameSetMap
									.get(wfmeAlphabet);
							if (resolvedSymbolAbstractionNameSet.size() == 1) {
								ate
										.setWorkflowModelElement(resolvedSymbolAbstractionNameSet
												.toString().replaceAll("\\[",
														"").replaceAll("\\]",
														""));
							} else {
								/**
								 * More than one abstraction name is possible;
								 * use the heuristic of nearby abstraction names
								 */
								pfound = false;
								for (int kk = jj - 1; kk > 0
										&& kk > jj - noLookBackEntries; kk--) {
									pate = modifiedAuditTrailEntryList.get(kk);
									if (pate.getAttributes().get("resolved")
											.equals("false")) {
										// System.out.println("The previous entry is not yet resolved; strange, can't be the case");
										// System.exit(0);
									} else {
										if (resolvedSymbolAbstractionNameSet
												.contains(pate
														.getWorkflowModelElement()
														.trim())) {
											/*
											 * Found a previous ate with an
											 * abstraction which is one of the
											 * abstractions for this ate
											 */
											ate.setWorkflowModelElement(pate
													.getWorkflowModelElement()
													.trim());
											pfound = true;
											break;
										}
									}
								}

								sfound = false;
								if (!pfound) {
									/**
									 * Look for successor entries with an
									 * abstraction name
									 */
									for (int kk = jj + 1; kk < modifiedAuditTrailEntryList
											.size()
											&& kk < jj + noLookAheadEntries; kk++) {
										sate = modifiedAuditTrailEntryList
												.get(kk);
										if (sate.getAttributes()
												.get("resolved").equals("true")) {
											if (resolvedSymbolAbstractionNameSet
													.contains(sate
															.getWorkflowModelElement()
															.trim())) {
												/*
												 * Found a previous ate with an
												 * abstraction which is one of
												 * the abstractions for this ate
												 */
												ate
														.setWorkflowModelElement(sate
																.getWorkflowModelElement()
																.trim());
												sfound = true;
												break;
											}
										}
									}
								}

								if (!pfound && !sfound) {
									/**
									 * Pick a random one; the first one
									 */
									ate
											.setWorkflowModelElement(resolvedSymbolAbstractionNameSet
													.toArray()[0].toString()
													.replaceAll("\\[", "")
													.replaceAll("\\]", ""));
								}
							}
						} else {
							/**
							 * resolvedSymbolAbstractionNameSetMap doesn't
							 * contain the entry; find if the symbol is either
							 * in the missedMap or the original map first if
							 * yes, add it else find all abstractionalphabets
							 * that subsume this symbol and put the abstraction
							 * names in the resolvedMap
							 */
							resolutionFound = false;
							if (missedAlphabetAbstractionNameSetMap
									.containsKey(wfmeAlphabet)) {
								resolvedSymbolAbstractionNameSetMap.put(
										wfmeAlphabet,
										missedAlphabetAbstractionNameSetMap
												.get(wfmeAlphabet));
								// System.out.println("Resolved "+wfmeAlphabet+" to "+missedAlphabetAbstractionNameSetMap.get(wfmeAlphabet));
								resolutionFound = true;
							} else if (abstractionAlphabetAbstractionNameMap
									.containsKey(wfmeAlphabet)) {
								resolvedSymbolAbstractionNameSet = new HashSet<String>();
								resolvedSymbolAbstractionNameSet
										.add(abstractionAlphabetAbstractionNameMap
												.get(wfmeAlphabet));
								resolvedSymbolAbstractionNameSetMap.put(
										wfmeAlphabet,
										resolvedSymbolAbstractionNameSet);
								// System.out.println("Resolved "+wfmeAlphabet+" to "+resolvedSymbolAbstractionNameSet);
								resolutionFound = true;
							} else {
								// System.out.println("Here; "+wfmeAlphabet+" @ "+abstractionAlphabetAbstractionNameMap.size());
								resolvedSymbolAbstractionNameSet = new HashSet<String>();
								for (TreeSet<String> alp : alphabetAbstractionNameSetMap
										.keySet()) {
									if (alp.containsAll(wfmeAlphabet)) {
										resolvedSymbolAbstractionNameSet
												.addAll(alphabetAbstractionNameSetMap
														.get(alp));
									}
								}
								if (resolvedSymbolAbstractionNameSet.size() > 0) {
									resolvedSymbolAbstractionNameSetMap.put(
											wfmeAlphabet,
											resolvedSymbolAbstractionNameSet);
									// System.out.println("Resolved "+wfmeAlphabet+" to "+resolvedSymbolAbstractionNameSet);
									resolutionFound = true;
								}

								resolvedSymbolAbstractionNameSet = new HashSet<String>();
								for (TreeSet<String> alp : missedAlphabetAbstractionNameSetMap
										.keySet()) {
									if (alp.containsAll(wfmeAlphabet)) {
										resolvedSymbolAbstractionNameSet
												.addAll(missedAlphabetAbstractionNameSetMap
														.get(alp));
									}
								}
								if (resolvedSymbolAbstractionNameSet.size() > 0) {
									resolvedSymbolAbstractionNameSetMap.put(
											wfmeAlphabet,
											resolvedSymbolAbstractionNameSet);
									// System.out.println("Resolved "+wfmeAlphabet+" to "+resolvedSymbolAbstractionNameSet);
									resolutionFound = true;
								}

							}

							if (resolutionFound) {
								resolvedSymbolAbstractionNameSet = resolvedSymbolAbstractionNameSetMap
										.get(wfmeAlphabet);
								if (resolvedSymbolAbstractionNameSet.size() == 1) {
									ate
											.setWorkflowModelElement(resolvedSymbolAbstractionNameSet
													.toString().replaceAll(
															"\\[", "")
													.replaceAll("\\]", ""));
								} else {
									/**
									 * More than one abstraction name is
									 * possible; use the heuristic of nearby
									 * abstraction names
									 */
									pfound = false;
									for (int kk = jj - 1; kk > 0
											&& kk > jj - noLookBackEntries; kk--) {
										pate = modifiedAuditTrailEntryList
												.get(kk);
										if (pate.getAttributes()
												.get("resolved")
												.equals("false")) {
											// System.out.println("The previous entry is not yet resolved; strange, can't be the case");
											// System.exit(0);
										} else {
											if (resolvedSymbolAbstractionNameSet
													.contains(pate
															.getWorkflowModelElement()
															.trim())) {
												/*
												 * Found a previous ate with an
												 * abstraction which is one of
												 * the abstractions for this ate
												 */
												ate
														.setWorkflowModelElement(pate
																.getWorkflowModelElement()
																.trim());
												pfound = true;
												break;
											}
										}
									}

									sfound = false;
									if (!pfound) {
										/**
										 * Look for successor entries with an
										 * abstraction name
										 */
										for (int kk = jj + 1; kk < modifiedAuditTrailEntryList
												.size()
												&& kk < jj + noLookAheadEntries; kk++) {
											sate = modifiedAuditTrailEntryList
													.get(kk);
											if (sate.getAttributes().get(
													"resolved").equals("true")) {
												if (resolvedSymbolAbstractionNameSet
														.contains(sate
																.getWorkflowModelElement()
																.trim())) {
													/*
													 * Found a previous ate with
													 * an abstraction which is
													 * one of the abstractions
													 * for this ate
													 */
													ate
															.setWorkflowModelElement(sate
																	.getWorkflowModelElement()
																	.trim());
													sfound = true;
													break;
												}
											}
										}
									}

									if (!pfound && !sfound) {
										/**
										 * Pick a random one; the first one
										 */
										ate
												.setWorkflowModelElement(resolvedSymbolAbstractionNameSet
														.toArray()[0]
														.toString().replaceAll(
																"\\[", "")
														.replaceAll("\\]", ""));
									}
								}
							} else {
								/*
								 * wfme can be a repeat in itself
								 */
								// ate.setWorkflowModelElement(charActivityMap.get(wfme.trim()));
								// System.out.println("Resolution Not Found: Strange "+wfme);
								// System.out.println("Instance: "+i);
								// System.exit(0);
							}
						}

						ateAttributeMap.put("resolved", "true");
						ate.setAttributes(ateAttributeMap);
					}
				}
				// System.out.println("CharStreamSize: "+currentCharStreamLength);
				// System.out.println("ATESize: "+modifiedAuditTrailEntryList.size());

				for (int jj = 0; jj < modifiedAuditTrailEntryList.size(); jj++) {
					ate = modifiedAuditTrailEntryList.get(jj);
					ateAttributeMap = ate.getAttributes();
					ate.setEventType(EventType.COMPLETE);
					persistency.addAuditTrailEntry(ate);
					// System.out.println(ateAttributeMap.get("startIndex")+","+ateAttributeMap.get("endIndex")+","+ateAttributeMap.get("resolved")+" @ "+ate.getWorkflowModelElement());
				}

				/**
				 * Prepare the expanded abstractions Existing process discovery
				 * algorithms have an issue with the fitness metrics when the
				 * number of activities reduces because of abstraction hence put
				 * abstraction for each activity in a trace so the final o/p
				 * would be a log with as many events as in the original log but
				 * with less number of event classes
				 * 
				 */
				// expandedModifiedAuditTrailEntryList = new
				// ArrayList<org.processmining.lib.mxml.AuditTrailEntry>();
				int noExpansions, startIndex, endIndex;

				for (int jj = 0; jj < modifiedAuditTrailEntryList.size(); jj++) {
					ate = modifiedAuditTrailEntryList.get(jj);
					ateAttributeMap = ate.getAttributes();
					ate.setEventType(EventType.COMPLETE);
					startIndex = new Integer(ateAttributeMap.get("startIndex"))
							.intValue();
					endIndex = new Integer(ateAttributeMap.get("endIndex"))
							.intValue();
					noExpansions = endIndex - startIndex + 1;

					for (int rp = 0; rp < noExpansions; rp++) {
						ate.addAttribute("startIndex", (startIndex + rp + ""));
						ate.addAttribute("endIndex", (startIndex + rp + ""));
						expandedPersistency.addAuditTrailEntry(ate);
						// expandedModifiedAuditTrailEntryList.add(ate);
					}
				}

				/**
				 * Pass 2: Run Length Encoding of ATEList
				 */
				/*
				 * ArrayList<org.processmining.lib.mxml.AuditTrailEntry>
				 * runLengthAuditTrailEntryList = new
				 * ArrayList<org.processmining.lib.mxml.AuditTrailEntry>(); int
				 * kk; org.processmining.lib.mxml.AuditTrailEntry rate; for(int
				 * jj = 0; jj < modifiedAuditTrailEntryList.size(); jj++){ ate =
				 * modifiedAuditTrailEntryList.get(jj); for(kk = jj+1; kk <
				 * modifiedAuditTrailEntryList.size(); kk++){
				 * if(!ate.getWorkflowModelElement
				 * ().equalsIgnoreCase(modifiedAuditTrailEntryList
				 * .get(kk).getWorkflowModelElement())) break; } if(kk > jj+1){
				 * rate = new org.processmining.lib.mxml.AuditTrailEntry();
				 * rate.addAttribute("startIndex",
				 * ate.getAttributes().get("startIndex"));
				 * rate.addAttribute("endIndex"
				 * ,modifiedAuditTrailEntryList.get(kk
				 * -1).getAttributes().get("endIndex"));
				 * rate.addAttribute("resolved","true");
				 * rate.setWorkflowModelElement(ate.getWorkflowModelElement());
				 * jj = kk-1; }else{ rate = new
				 * org.processmining.lib.mxml.AuditTrailEntry();
				 * rate.addAttribute("startIndex",
				 * ate.getAttributes().get("startIndex"));
				 * rate.addAttribute("endIndex"
				 * ,ate.getAttributes().get("endIndex"));
				 * rate.addAttribute("resolved","true");
				 * rate.setWorkflowModelElement(ate.getWorkflowModelElement());
				 * } runLengthAuditTrailEntryList.add(rate);
				 * 
				 * }
				 * 
				 * System.out.println(i+" @ "+currentCharStreamLength+" @ "+
				 * modifiedAuditTrailEntryList
				 * .size()+" @ "+runLengthAuditTrailEntryList.size()); for(int
				 * jj = 0; jj < runLengthAuditTrailEntryList.size(); jj++){ ate
				 * = runLengthAuditTrailEntryList.get(jj);
				 * ate.setEventType(EventType.COMPLETE); ateAttributeMap =
				 * ate.getAttributes(); //
				 * System.out.println(ateAttributeMap.get
				 * ("startIndex")+","+ateAttributeMap
				 * .get("endIndex")+","+ateAttributeMap
				 * .get("resolved")+" @ "+ate.getWorkflowModelElement());
				 * persistency.addAuditTrailEntry(ate); }
				 */
				persistency.endProcessInstance();
				expandedPersistency.endProcessInstance();

			}
			persistency.endProcess();
			persistency.endLogfile();
			persistency.finish();

			expandedPersistency.endProcess();
			expandedPersistency.endLogfile();
			expandedPersistency.finish();

		} catch (LogException e) {
			e.printStackTrace();
		}

	}
}
