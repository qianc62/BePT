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

package org.processmining.framework.models.heuristics;

import java.io.*;
import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.models.*;
import cern.colt.matrix.*;

/**
 * 
 * <p>
 * Title: Heuristics Net
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2004
 * </p>
 * 
 * <p>
 * Company: Eindhoven University of Technology (TU/e)
 * </p>
 * 
 * @author Ana Karla A. de Medeiros
 * @version 1.0
 */
public class HeuristicsNet implements Comparable, DotFileWriter {

	private LogEvents events;
	private HNSet[] inputSets;
	private HNSet[] outputSets;
	private HNSubSet startTasks; // the net can have multiple starting tasks
	private HNSubSet endTasks; // the net can have multiple end tasks
	private double fitness;
	private int size;

	public static final String WME_HEADER = "Element";
	public static final char WME_NAME_DELIMITER = '\"';
	public static final String INPUT_SETS_HEADER = "In";
	public static final String OUTPUT_SETS_HEADER = "Out";

	public static final String EVENT_SEPARATOR = ":";
	public static final String EMPTY_SET = ".";
	public static final String AND_SEPARATOR = "&";
	public static final String OR_SEPARATOR = "|";
	public static final String SETS_SEPARATOR = "@";
	public static final String FIELD_SEPARATOR = "/////////////////////";

	public static String FIRST_NET_LABEL = "First net (>>>)";
	public static String SECOND_NET_LABEL = "Second net (<<<)";

	private int[] duplicatesMapping;
	private int[] duplicatesActualFiring;
	private HNSubSet[] reverseDuplicatesMapping;
	private DoubleMatrix2D arcUsage;

	private boolean showSplitJoinSemantics = false;

	/**
	 * This method builds a net without duplicate tasks.
	 * 
	 * @param event
	 *            LogEvents event log.
	 */
	public HeuristicsNet(LogEvents events) {
		// building the duplicatesMapping
		// Since it is a one-to-one mapping, we can do that directly

		int[] duplicatesMapping = new int[events.size()];
		HNSubSet[] reverseDuplicatesMapping = new HNSubSet[events.size()];
		for (int i = 0; i < events.size(); i++) {
			duplicatesMapping[i] = i;
			HNSubSet set = new HNSubSet();
			set.add(i);
			reverseDuplicatesMapping[i] = set;
		}
		initializeVariables(events, duplicatesMapping, reverseDuplicatesMapping);
	}

	/**
	 * This method builds a net that may contain duplicate tasks.
	 * 
	 * @param event
	 *            LogEvents event log.
	 * @param duplicatesMapping
	 *            int[] The mapping from the tasks in the net to the code in the
	 *            event log.
	 * @param reverseDuplicatesMapping
	 *            HNSubSet[] The mapping from the codes in the log to the tasks
	 *            in the net.
	 */

	public HeuristicsNet(LogEvents events, int[] duplicatesMapping,
			HNSubSet[] reverseDuplicatesMapping) {
		initializeVariables(events, duplicatesMapping, reverseDuplicatesMapping);
	}

	private void initializeVariables(LogEvents events, int[] duplicatesMapping,
			HNSubSet[] reverseDuplicatesMapping) {
		this.events = events;
		this.duplicatesMapping = duplicatesMapping;
		this.reverseDuplicatesMapping = reverseDuplicatesMapping;
		this.size = duplicatesMapping.length;
		inputSets = new HNSet[this.size];
		outputSets = new HNSet[this.size];
		this.fitness = 0;
		this.duplicatesActualFiring = new int[this.size];
		arcUsage = DoubleFactory2D.sparse.make(size, size, 0.0);

	}

	/**
	 * The duplicatesActualFiring variable keeps track of the duplicate tasks
	 * that are actually fired during the parsing of a log. The duplicates that
	 * do not fire are not shown to the end user.
	 */
	public void resetDuplicatesActualFiring() {
		for (int i = 0; i < duplicatesActualFiring.length; i++) {
			duplicatesActualFiring[i] = 0;
		}
	}

	/**
	 * The arcUsage variable keeps track of how often the arcs are actually used
	 * during the parsing of a log by this individual.
	 */
	public void resetArcUsage() {
		for (int row = 0; row < arcUsage.rows(); row++) {
			for (int column = 0; column < arcUsage.columns(); column++) {
				arcUsage.setQuick(row, column, 0.0);
			}
		}
	}

	public int[] getDuplicatesActualFiring() {
		return duplicatesActualFiring;
	}

	public DoubleMatrix2D getArcUsage() {
		return arcUsage;
	}

	public boolean setDuplicatesActualFiring(int[] newDuplicatesActualFiring) {

		if (duplicatesActualFiring.length == newDuplicatesActualFiring.length) {
			duplicatesActualFiring = newDuplicatesActualFiring;
			return true;
		}
		return false;
	}

	public boolean setArcUsage(DoubleMatrix2D newArcUsage) {

		if ((arcUsage.rows() == newArcUsage.rows())
				&& (arcUsage.columns() == newArcUsage.columns())) {
			arcUsage = newArcUsage;
			return true;
		}
		return false;
	}

	public String diffForSets(HeuristicsNet netToCompare) {

		StringBuffer differences = new StringBuffer();

		if (this.size != netToCompare.size()) {
			differences.append("The nets have a different number of tasks!");
		} else if (!this.events.containsAll(netToCompare.getLogEvents())
				|| !netToCompare.getLogEvents().containsAll(this.events)) {
			differences.append("The nets have different task labels!");
		} else {
			// really check if the nets have the same structure in terms of
			// labels

			// input sets
			TreeSet thisInput = toStringRepresentation(this.getInputSets(),
					this.getLogEvents(), this.getDuplicatesMapping());
			TreeSet toCompareInput = toStringRepresentation(netToCompare
					.getInputSets(), netToCompare.getLogEvents(), netToCompare
					.getDuplicatesMapping());

			TreeSet auxThisInput = (TreeSet) thisInput.clone();

			thisInput.removeAll(toCompareInput); // A - B
			toCompareInput.removeAll(auxThisInput); // B - A

			if (thisInput.size() > 0 || toCompareInput.size() > 0) {
				differences.append("\nINPUT sets ++++++++++++++++++\n");

				Iterator ts = thisInput.iterator();

				differences.append("\n\n ").append(FIRST_NET_LABEL)
						.append("\n");

				while (ts.hasNext()) {
					String entryThis = (String) ts.next();
					differences.append(entryThis).append("\n");
				}

				Iterator toCompareTs = toCompareInput.iterator();

				differences.append("\n\n ").append(SECOND_NET_LABEL).append(
						"\n");
				while (toCompareTs.hasNext()) {
					String entryNetToCompare = (String) toCompareTs.next();
					// entry is different!
					differences.append(entryNetToCompare).append("\n");
				}
			}

			// output sets
			TreeSet thisOutput = toStringRepresentation(this.getOutputSets(),
					this.getLogEvents(), this.getDuplicatesMapping());
			TreeSet toCompareOutput = toStringRepresentation(netToCompare
					.getOutputSets(), netToCompare.getLogEvents(), netToCompare
					.getDuplicatesMapping());

			TreeSet auxThisOutput = (TreeSet) thisOutput.clone();

			thisOutput.removeAll(toCompareOutput); // A - B
			toCompareOutput.removeAll(auxThisOutput); // B - A

			if (thisOutput.size() > 0 || toCompareOutput.size() > 0) {
				Iterator ts = thisOutput.iterator();

				differences.append("\nOUTPUT sets ++++++++++++++++++\n");

				differences.append("\n\n ").append(FIRST_NET_LABEL)
						.append("\n");

				while (ts.hasNext()) {
					String entryThis = (String) ts.next();
					differences.append(entryThis).append("\n");
				}

				Iterator toCompareTs = toCompareOutput.iterator();

				differences.append("\n\n ").append(SECOND_NET_LABEL).append(
						"\n");
				while (toCompareTs.hasNext()) {
					String entryNetToCompare = (String) toCompareTs.next();
					// entry is different!
					differences.append(entryNetToCompare).append("\n");
				}
			}
		}

		return differences.toString();
	}

	private TreeSet toStringRepresentation(HNSet[] set, LogEvents le,
			int[] duplicatesMapping) {
		TreeSet entries = new TreeSet();

		for (int i = 0; i < set.length; i++) {
			String entry = "Element = "
					+ le.getEvent(duplicatesMapping[i]).getModelElementName()
					+ " (" + le.getEvent(duplicatesMapping[i]).getEventType()
					+ ") ";
			TreeSet labelSet = new TreeSet(new TreeSetWithStringComparator());
			for (int j = 0; j < set[i].size(); j++) {
				HNSubSet subset = set[i].get(j);
				TreeSet labelSubset = new TreeSet();
				for (int k = 0; k < subset.size(); k++) {
					String label = le
							.getEvent(duplicatesMapping[subset.get(k)])
							.getModelElementName()
							+ " ("
							+ le.getEvent(duplicatesMapping[subset.get(k)])
									.getEventType() + ") ";
					labelSubset.add(label);
				}
				labelSet.add(labelSubset);
			}
			entry += " Set = " + labelSet.toString();
			entries.add(entry);
		}

		return entries;
	}

	/**
	 * Add 'amount' to the counter of the number of times that a duplicate
	 * actually fired.
	 */
	public void increaseElementActualFiring(int element, int amount) {
		// Note: it does not increase directly +1 because the traces may be
		// grouped.
		duplicatesActualFiring[element] += amount;
	}

	public void increaseArcUsage(int element, HNSubSet usedInputElements,
			int amount) {
		for (int inputElementPosition = 0; inputElementPosition < usedInputElements
				.size(); inputElementPosition++) {
			arcUsage.setQuick(usedInputElements.get(inputElementPosition),
					element, (arcUsage.getQuick(usedInputElements
							.get(inputElementPosition), element) + amount));
		}

	}

	public int[] getDuplicatesMapping() {
		return duplicatesMapping;
	}

	public HNSubSet[] getReverseDuplicatesMapping() {
		return reverseDuplicatesMapping;
	}

	public HNSubSet getStartTasks() {
		return startTasks;
	}

	public HNSubSet getEndTasks() {
		return endTasks;
	}

	public void setStartTasks(HNSubSet task) {
		startTasks = task;
	}

	public void setEndTasks(HNSubSet task) {
		endTasks = task;
	}

	public int size() {
		return this.size;
	}

	public void resetFitness() {
		setFitness(0);
	}

	public void setFitness(double d) {
		fitness = d;
	}

	public LogEvents getLogEvents() {
		return events;
	}

	/**
	 * This method changes the <code>LogEvents</code> associated to the
	 * <code>HeuristicsNet</code>.
	 * 
	 * @param events
	 *            LogEvents new events to be associated to this net.
	 */
	public void setLogEvents(LogEvents newEvents) {
		int[] newDuplicatesMapping = new int[this.duplicatesMapping.length];

		int leNumber = 0;
		// rebuilding the duplicates mapping first
		for (int i = 0; i < newDuplicatesMapping.length; i++) {

			try {
				LogEvent le = events.getEvent(duplicatesMapping[i]);
				leNumber = newEvents.findLogEventNumber(le
						.getModelElementName(), le.getEventType());
			} catch (ArrayIndexOutOfBoundsException exc) {
				leNumber = -1; // since the task does not exist in the log

			}
			newDuplicatesMapping[i] = leNumber;
		}

		// since the new duplicates mapping could be successfully created,
		// the new LogEvents can replace the old one.
		this.events = newEvents;
		this.duplicatesMapping = newDuplicatesMapping;
		this.reverseDuplicatesMapping = buildReverseDuplicatesMapping(this.duplicatesMapping);

	}

	/**
	 * Builds a reverse mapping to an array of duplicates mapping.
	 * 
	 * @param duplicatesMapping
	 *            int[] the array of int that contains the duplicates mapping.
	 * @return HNSubSet[] the reverse duplicates mapping.
	 */
	public static HNSubSet[] buildReverseDuplicatesMapping(
			int[] duplicatesMapping) {

		// getting the number unique events...
		HNSubSet uniqueEvents = new HNSubSet();
		for (int i = 0; i < duplicatesMapping.length; i++) {
			if (duplicatesMapping[i] >= 0) { // valid code
				uniqueEvents.add(duplicatesMapping[i]);
			}
		}

		HNSubSet[] reverseDuplicatesMapping = new HNSubSet[uniqueEvents.size()];
		for (int i = 0; i < duplicatesMapping.length; i++) {
			if (duplicatesMapping[i] >= 0
					&& duplicatesMapping[i] < reverseDuplicatesMapping.length) {
				if (reverseDuplicatesMapping[duplicatesMapping[i]] == null) {
					reverseDuplicatesMapping[duplicatesMapping[i]] = new HNSubSet();
				}
				reverseDuplicatesMapping[duplicatesMapping[i]].add(i);
			}
		}

		return reverseDuplicatesMapping;

	}

	public double getFitness() {
		return fitness;
	}

	public boolean setInputSet(int index, HNSet sets) {
		return setSet(this.inputSets, index, sets);
	}

	private boolean setSet(HNSet[] target, int index, HNSet sets) {
		if (index < size()) {
			target[index] = sets;
			return true;
		}
		return false;
	}

	public HNSet[] getInputSets() {
		return inputSets;
	}

	public HNSet[] getOutputSets() {
		return outputSets;
	}

	public HNSet getInputSet(int index) {
		return getSet(inputSets, index);
	}

	private HNSet getSet(HNSet[] target, int index) {
		if (index >= 0 && index < size()) {
			return target[index];
		}
		return null;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < size(); i++) {
			sb.append(WME_HEADER).append(" ").append(i).append(":\n");
			sb.append(INPUT_SETS_HEADER).append(": ").append(
					this.inputSets[i].toString()).append("\n");
			sb.append(OUTPUT_SETS_HEADER).append(": ").append(
					this.outputSets[i].toString()).append("\n");
		}
		return sb.toString();
	}

	public String toStringWithEvents() {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < size(); i++) {
			sb.append("\n\n").append(WME_HEADER).append(" ").append(
					WME_NAME_DELIMITER)
					.append(
							events.getEvent(duplicatesMapping[i])
									.getModelElementName()).append(" (id = ")
					.append(i).append(") ").append(" (").append(
							events.getEvent(duplicatesMapping[i])
									.getEventType()).append(")").append(
							WME_NAME_DELIMITER).append(":");

			// building IN part....
			sb.append("\n").append(INPUT_SETS_HEADER).append(": ");

			sb.append("[ ");
			buildVisualPresentation(sb, this.inputSets[i], events);
			sb.append(" ]");

			// building OUT part....
			sb.append("\n").append(OUTPUT_SETS_HEADER).append(": ");
			sb.append("[ ");
			buildVisualPresentation(sb, this.outputSets[i], events);
			sb.append(" ]");
		}
		return sb.toString();
	}

	private void buildVisualPresentation(StringBuffer sb, HNSet set,
			LogEvents events) {

		HNSubSet subset = null;
		int element = 0;

		if (set != null) {

			for (int i = 0; i < set.size(); i++) {
				subset = set.get(i);
				sb.append("[");
				for (int j = 0; j < subset.size(); j++) {
					element = duplicatesMapping[subset.get(j)];
					sb.append(" ").append(WME_NAME_DELIMITER).append(
							events.getEvent(element).getModelElementName())
							.append(" (id = ").append(subset.get(j)).append(
									") ").append(" (").append(
									events.getEvent(element).getEventType())
							.append(")").append(WME_NAME_DELIMITER);
				}
				sb.append(" ]");
			}
		} else {
			sb.append("null");
		}
	}

	private String convertToFileFormat(HNSet set, int[] nonDanglingTasks) {
		HNSubSet subset = null;
		StringBuffer convertedSet = null;

		convertedSet = new StringBuffer();
		if (set.size() == 0) {
			convertedSet.append(EMPTY_SET);
		} else {
			int i;
			for (i = 0; i < set.size(); i++) {
				subset = set.get(i);
				int j;
				for (j = 0; j < subset.size(); j++) {
					if (nonDanglingTasks[subset.get(j)] >= 0) {
						convertedSet.append(nonDanglingTasks[subset.get(j)])
								.append(OR_SEPARATOR);
					}
				}
				if (j > 0) {
					convertedSet.deleteCharAt(convertedSet.length() - 1);
				}
				convertedSet.append(AND_SEPARATOR);
			}
			if (i > 0) {
				convertedSet.deleteCharAt(convertedSet.length() - 1);
			}
		}

		return convertedSet.toString();
	}

	public void toFile(OutputStream output) throws IOException {

		BufferedWriter bw = null;
		StringBuffer line = null;
		LogEvent le = null;
		int[] nonDanglingTasks = new int[size];
		boolean thereAreNonDanglingTasks = false;

		Arrays.fill(nonDanglingTasks, -1);

		bw = new BufferedWriter(new OutputStreamWriter(output));

		// Checking for dangling tasks.
		// Dangling tasks are not exported.
		int newIndex = 0;
		for (int i = 0; i < size; i++) {
			if (((inputSets[i].size() > 0) || (outputSets[i].size() > 0))
					|| ((startTasks.contains(i)) || (endTasks.contains(i)))) {
				nonDanglingTasks[i] = newIndex++;
				thereAreNonDanglingTasks = true;
			}
		}

		// Begin of the exportation
		line = new StringBuffer();

		if (thereAreNonDanglingTasks) {

			// writing the start tasks
			line.append(FIELD_SEPARATOR).append("\n");
			for (int i = 0; i < startTasks.size(); i++) {
				line.append(nonDanglingTasks[startTasks.get(i)]).append(
						this.SETS_SEPARATOR);
			}
			line.append("\n");

			// writing the end tasks
			line.append(FIELD_SEPARATOR).append("\n");
			for (int i = 0; i < endTasks.size(); i++) {
				line.append(nonDanglingTasks[endTasks.get(i)]).append(
						this.SETS_SEPARATOR);
			}
			line.append("\n");
			// writing the reverse duplicates mapping
			line.append(FIELD_SEPARATOR).append("\n");
			for (int i = 0; i < reverseDuplicatesMapping.length; i++) {
				le = this.events.getEvent(i);
				line.append(le.getModelElementName() + this.EVENT_SEPARATOR
						+ le.getEventType());
				line.append(SETS_SEPARATOR);
				for (int j = 0; j < reverseDuplicatesMapping[i].size(); j++) {
					if (nonDanglingTasks[reverseDuplicatesMapping[i].get(j)] >= 0) {
						line
								.append(nonDanglingTasks[reverseDuplicatesMapping[i]
										.get(j)]);
						line.append(AND_SEPARATOR);
					}
				}
				line.append("\n");
			}

			line.append("\n");

			// writing the input/output sets
			line.append(FIELD_SEPARATOR).append("\n");
			for (int i = 0; i < inputSets.length; i++) {
				if (nonDanglingTasks[i] >= 0) {
					line.append(nonDanglingTasks[i]);
					line.append(SETS_SEPARATOR);
					line.append(convertToFileFormat(inputSets[i],
							nonDanglingTasks));
					line.append(SETS_SEPARATOR);
					line.append(convertToFileFormat(outputSets[i],
							nonDanglingTasks));
					line.append("\n");
				}
			}
		}
		bw.write(line.toString());
		bw.close();
		// end of exportation
	}

	public boolean setOutputSet(int index, HNSet sets) {
		return setSet(this.outputSets, index, sets);
	}

	public int getNumberOutputSet(int index) {
		return getOutputSet(index).size();
	}

	public int getNumberInputSet(int index) {
		return getInputSet(index).size();
	}

	public HNSet getOutputSet(int index) {
		return getSet(outputSets, index);
	}

	public int compareTo(Object o) {
		HeuristicsNet ind = (HeuristicsNet) o;
		if (this.fitness > ind.getFitness()) {
			return 1;
		}
		if (this.fitness == ind.getFitness()) {
			return 0;
		}
		return -1;
	}

	/**
	 * Deep cloning!
	 */
	protected Object clone() {

		HeuristicsNet copy = null;

		// making a deep copy of the "duplicates mapping"...
		int[] dmCopy = new int[this.duplicatesMapping.length];
		System.arraycopy(this.duplicatesMapping, 0, dmCopy, 0,
				this.duplicatesMapping.length);

		// making a deep copy of the "reverse duplicates mapping"...
		HNSubSet[] rdmCopy = new HNSubSet[this.reverseDuplicatesMapping.length];
		for (int i = 0; i < rdmCopy.length; i++) {
			if (this.reverseDuplicatesMapping[i] != null) {
				rdmCopy[i] = this.reverseDuplicatesMapping[i].deepCopy();
			}
		}

		// creating the copy...
		copy = new HeuristicsNet(events, dmCopy, rdmCopy);

		// writing the input/output sets...
		for (int i = 0; i < size(); i++) {
			copy.setInputSet(i, this.inputSets[i].deepCopy());
			copy.setOutputSet(i, this.outputSets[i].deepCopy());
		}

		// copying the fitness...
		copy.setFitness(this.fitness);

		// copying the start/end tasks...
		copy.setStartTasks(this.startTasks);
		copy.setEndTasks(this.endTasks);

		// making deep copy of the "duplicates actual firing"...
		int[] dacCopy = new int[this.duplicatesActualFiring.length];
		System.arraycopy(this.duplicatesActualFiring, 0, dacCopy, 0,
				this.duplicatesActualFiring.length);

		// copying the "duplicates actual firing"
		copy.setDuplicatesActualFiring(dacCopy);

		// copying the "arc usage"
		copy.setArcUsage(this.arcUsage.copy());

		return copy;
	}

	/**
	 * Deep copy from the net.
	 */
	public HeuristicsNet copyNet() {
		return (HeuristicsNet) this.clone();
	}

	/**
	 * This method compares the input and output sets of two individuals.
	 */
	public boolean equals(Object anObject) {
		HeuristicsNet other = (HeuristicsNet) anObject;
		if (other == null) {
			return false;
		}
		if (size() != other.size()) {
			return false;
		}
		// checking if input and output sets are the same
		for (int i = 0; i < size(); i++) {
			if (!this.getInputSet(i).equals(other.getInputSet(i))
					|| !this.getOutputSet(i).equals(other.getOutputSet(i))) {
				return false;
			}
		}
		return true;
	}

	public int hashCode() {
		int hashCode = 0;
		for (int i = 0; i < size(); i++) {
			hashCode += ((this.getInputSet(i).hashCode() + this.getOutputSet(i)
					.hashCode()) * 31 ^ (size - i + 1));
		}
		return hashCode;
	}

	/**
	 * Retrieves a subset from an INPUT set of "index". This subset contains
	 * "element".
	 * 
	 * @param index
	 *            workflow model element whose INPUT set must the searched.
	 * @param element
	 *            element to find in the INPUT subsets.
	 * @return a TreeSet that contains 'element'. Otherwise, null.
	 */
	public HNSubSet getInputSetWithElement(int index, int element) {
		return getSetWithElement(this.inputSets, index, element);
	}

	/**
	 * Retrieves a subset from an OUTPUT set of "index". This subset contains
	 * "element".
	 * 
	 * @param index
	 *            workflow model element whose OUTPUT set must the searched.
	 * @param element
	 *            element to find in the OUTPUT subsets.
	 * @return a TreeSet that contains 'element'. Otherwise, null.
	 */

	public HNSubSet getOutputSetWithElement(int index, int element) {
		return getSetWithElement(this.outputSets, index, element);
	}

	/*
	 * Retrieves a subset from an INPUT/OUTPUT set of "index". This subset
	 * contains "element". Returns null if there is not such index's TreeSet
	 * with element.
	 */
	private HNSubSet getSetWithElement(HNSet[] indSet, int index, int element) {
		HNSet set = null;
		HNSubSet subset = null;

		set = indSet[index];
		for (int i = 0; i < set.size(); i++) {
			subset = set.get(i);
			if (subset.contains(element)) {
				break;
			}
		}
		return subset;
	}

	/**
	 * Returns the union set of all int in the subset of the input set at
	 * " index".
	 */
	public HNSubSet getAllElementsInputSet(int index) {
		return HNSet.getUnionSet(this.inputSets[index]);
	}

	/**
	 * Returns the union set of all int in the subset of the output set at
	 * " index".
	 */
	public HNSubSet getAllElementsOutputSet(int index) {
		return HNSet.getUnionSet(this.outputSets[index]);
	}

	/**
	 * Retrieves subsets from an INPUT set of "index". These subsets contain
	 * "element".
	 * 
	 * @param index
	 *            workflow model element whose INPUT set must the searched.
	 * @param element
	 *            element to find in the INPUT subsets.
	 * @return a HashSet with the subsets that contain 'element'. Otherwise,
	 *         null.
	 */
	public HNSet getInputSetsWithElement(int index, int element) {
		return getSetsWithElement(this.inputSets, index, element);
	}

	/**
	 * Retrieves subsets from an OUTPUT set of "index". These subsets contain
	 * "element".
	 * 
	 * @param index
	 *            workflow model element whose OUTPUT set must the searched.
	 * @param element
	 *            element to find in the OUTPUT subsets.
	 * @return a HashSet with the subsets that contain 'element'. Otherwise,
	 *         null.
	 */

	public HNSet getOutputSetsWithElement(int index, int element) {
		return getSetsWithElement(this.outputSets, index, element);
	}

	/*
	 * Retrieves subsets from an INPUT/OUTPUT set of "index". These subsets
	 * contain "element". Returns null if there is not such subsets with
	 * element.
	 */
	private HNSet getSetsWithElement(HNSet[] indSet, int index, int element) {
		HNSet set = null;
		HNSubSet subSet = null;
		HNSet filterFromSet = null;

		if (index < indSet.length) {
			filterFromSet = indSet[index];
			set = new HNSet();
			for (int i = 0; i < filterFromSet.size(); i++) {
				subSet = filterFromSet.get(i);
				if (subSet.contains(element)) {
					set.add(subSet);
				}
			}
		}
		return set;
	}

	public static final int[] getElements(HNSet set) {

		int[] multiset = null;
		int size = 0;
		HNSubSet subset = null;

		for (int i = 0; i < set.size(); i++) {
			size += set.get(i).size();
		}

		multiset = new int[size];

		for (int i = 0, iMultiset = 0; i < set.size(); i++) {
			subset = set.get(i);
			for (int j = 0; j < subset.size(); j++) {
				multiset[iMultiset++] = subset.get(j);
			}

		}

		return multiset;
	}

	public void writeToDot(Writer bw) throws IOException {

		// this solution with the flag "showSplitJoinSemantics" is clean...
		// I could not think of something else that would be fast to change.
		// Anyway, I needed this flag to make sure that the current
		// visualization of the heuristics net would be the one to be exported
		// to DOT.
		if (showSplitJoinSemantics) {
			writeToDotWithSplitJoinSemantics(bw);
		} else {
			writeToDotWithoutSplitJoinSemantics(bw);
		}
	}

	public void writeToDotWithoutSplitJoinSemantics(Writer bw)
			throws IOException {
		// correcting individual for visual presentation
		// two individuals with different genotype can have the same phenotype
		// HeuristicsNet phenotype =
		// MethodsOverIndividuals.removeDanglingElementReferences((HeuristicsNet)this.clone());

		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Arial\"; fontsize=\"12\";\n");
		bw
				.write("  node [shape=\"box\",fontname=\"Arial\",fontsize=\"12\"];\n");
		// bw.write("  node [shape=\"rect\",fontname=\"Arial\",fontsize=\"12\",style=filled, fillcolor=red];\n");

		// write nodes
		for (int i = 0; i < size(); i++) {
			if (((inputSets[i].size() > 0) || (outputSets[i].size() > 0))
					|| ((this.getStartTasks().contains(i)) || (this
							.getEndTasks().contains(i)))) {

				bw.write("E"
						+ i
						+ " [label=\""
						+ events.getEvent(duplicatesMapping[i])
								.getModelElementName().replace('"', '\'')
						+ /* " " + i + */"\\n("
						+ events.getEvent(duplicatesMapping[i]).getEventType()
								.replace('"', '\'') + ")\\n"
						+ duplicatesActualFiring[i] + "\"];\n");
			}
		}

		// write edges
		for (int from = 0; from < size(); from++) {
			// Iterator set =
			// phenotype.getAllElementsOutputSet(from).iterator();
			HNSubSet set = getAllElementsOutputSet(from);
			for (int iSet = 0; iSet < set.size(); iSet++) {
				int to = set.get(iSet);
				bw.write("E" + from + " -> E" + to
						+ " [style=\"filled\", label=\"  "
						+ (int) arcUsage.get(from, to) + "\"];\n");
			}
		}
		bw.write("}\n");
	}

	public void writeToDotWithSplitJoinSemantics(Writer bw) throws IOException {
		// correcting individual for visual presentation
		// two individuals with different genotype can have the same phenotype
		// HeuristicsNet phenotype =
		// MethodsOverIndividuals.removeDanglingElementReferences((HeuristicsNet)this.clone());
		bw.write("digraph G {\n");
		bw.write("  size=\"6,10\"; fontname=\"Arial\"; fontsize=\"12\";\n");
		bw
				.write("  node [shape=\"record\",fontname=\"Arial\",fontsize=\"12\"];\n");
		// bw.write("  node [shape=\"rect\",fontname=\"Arial\",fontsize=\"12\",style=filled, fillcolor=red];\n");
		// write nodes
		for (int i = 0; i < size(); i++) {
			if (((inputSets[i].size() > 0) || (outputSets[i].size() > 0))
					|| ((this.getStartTasks().contains(i)) || (this
							.getEndTasks().contains(i)))) {
				bw.write("E" + i + " [label=\"{");
				if (inputSets[i].size() > 0) {
					bw.write("{");
					for (int j = 0; j < inputSets[i].size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw
								.write("<"
										+ toInputDotName(inputSets[i].get(j)
												.toString()) + ">  XOR ");
					}
					bw.write("} | ");
				}
				bw.write(events.getEvent(duplicatesMapping[i])
						.getModelElementName().replace('"', '\'')
						+ " \\n("
						+ events.getEvent(duplicatesMapping[i]).getEventType()
								.replace('"', '\'')
						+ ")\\n "
						+ duplicatesActualFiring[i]);
				if (outputSets[i].size() > 0) {
					bw.write(" | {");
					for (int j = 0; j < outputSets[i].size(); j++) {
						if (j > 0) {
							bw.write(" | and | ");
						}
						bw.write("<"
								+ toOutputDotName(outputSets[i].get(j)
										.toString()) + "> XOR ");
					}
					bw.write("}}");
				}

				bw.write("}\"];\n");
			}
		}
		// write edges
		for (int from = 0; from < size(); from++) {
			// Iterator set =
			// phenotype.getAllElementsOutputSet(from).iterator();
			for (int outSubsetIndex = 0; outSubsetIndex < outputSets[from]
					.size(); outSubsetIndex++) {
				HNSubSet outSubset = outputSets[from].get(outSubsetIndex);
				for (int outSubsetElementIndex = 0; outSubsetElementIndex < outSubset
						.size(); outSubsetElementIndex++) {
					int to = outSubset.get(outSubsetElementIndex);
					HNSet inputSubsetsElementWithFrom = getInputSetsWithElement(
							to, from);
					for (int k = 0; k < inputSubsetsElementWithFrom.size(); k++) {
						bw.write("E"
								+ from
								+ ":"
								+ toOutputDotName(outSubset.toString())
								+ " -> E"
								+ to
								+ ":"
								+ toInputDotName(inputSubsetsElementWithFrom
										.get(k).toString())
								+ " [style=\"filled\", label=\"  "
								+ (int) arcUsage.get(from, to) + "\"];\n");
					}
				}
			}
		}
		bw.write("}\n");
	}

	protected String toInputDotName(String s) {
		return "in" + toDotName(s);
	}

	protected String toOutputDotName(String s) {
		return "out" + toDotName(s);
	}

	private String toDotName(String s) {
		return "p"
				+ s.trim().replace('[', 'a').replace(']', 'b')
						.replace(',', 'c');
	}

	/*
	 * This method disconnects from the tasks that are not used during the
	 * parsing of a log. The main idea is to remove duplicates that are not
	 * used.
	 */
	private void disconnectUnusedTasks() {
		HNSubSet unfiredElements;

		// identifying the tasks that did not fire during the parsing...
		unfiredElements = identifyUnfiredElements();

		// cleaning the in/out sets of the unfired tasks
		for (int iUnfiredElements = 0; iUnfiredElements < unfiredElements
				.size(); iUnfiredElements++) {
			inputSets[unfiredElements.get(iUnfiredElements)] = new HNSet();
			outputSets[unfiredElements.get(iUnfiredElements)] = new HNSet();

		}

		// removing the connections to the unfired elements
		for (int i = 0; i < this.size; i++) {
			for (int iUnfiredElements = 0; iUnfiredElements < unfiredElements
					.size(); iUnfiredElements++) {
				// clean input sets...
				inputSets[i] = HNSet.removeElementFromSubsets(inputSets[i],
						unfiredElements.get(iUnfiredElements));
				// clean output sets...
				outputSets[i] = HNSet.removeElementFromSubsets(outputSets[i],
						unfiredElements.get(iUnfiredElements));
			}
		}

	}

	/*
	 * This method disconnects the arcs that are used fewer times than a given
	 * threshold (inclusive). In other words, only the arcs that are used more
	 * times than 'threshold' are kept.
	 */

	public void disconnectArcsUsedBelowThreshold(double threshold) {

		// disconnecting unused input arcs
		for (int row = 0; row < arcUsage.rows(); row++) {
			for (int column = 0; column < arcUsage.columns(); column++) {
				if (arcUsage.get(row, column) <= threshold) {
					outputSets[row] = HNSet.removeElementFromSubsets(
							outputSets[row], column);
					inputSets[column] = HNSet.removeElementFromSubsets(
							inputSets[column], row);
					arcUsage.set(row, column, 0.0);

				}
			}

		}

	}

	/**
	 * This method builds a phenotype that disconnect the tasks and arcs that
	 * are not used during the parsing of an event log.
	 * <p>
	 * <b> NOTE: This method should be used with care because at the starting of
	 * a parsing, no elements (tasks) have been used.</b>
	 */

	public void disconnectUnusedElements() {

		disconnectUnusedTasks();
		disconnectArcsUsedBelowThreshold(0.0);

	}

	private HNSubSet identifyUnfiredElements() {
		HNSubSet unfiredElements = new HNSubSet();
		for (int i = 0; i < duplicatesActualFiring.length; i++) {
			if (duplicatesActualFiring[i] <= 0) {
				unfiredElements.add(i);
			}
		}
		return unfiredElements;
	}

	public ModelGraphPanel getGrappaVisualization() {
		showSplitJoinSemantics = false;
		ModelGraphPanel p = new DuplicateTasksHeuristicsNetModelGraph(this,
				showSplitJoinSemantics).getGrappaVisualization();
		p.setOriginalObject(this);
		return p;
	}

	public ModelGraphPanel getGrappaVisualizationWithSplitJoinSemantics() {
		showSplitJoinSemantics = true;
		ModelGraphPanel p = new DuplicateTasksHeuristicsNetModelGraph(this,
				showSplitJoinSemantics).getGrappaVisualization();
		p.setOriginalObject(this);
		return p;
	}

}

class DuplicateTasksHeuristicsNetModelGraph extends ModelGraph {
	private HeuristicsNet net;
	private boolean showSplitJoinSemantics = false;

	public DuplicateTasksHeuristicsNetModelGraph(HeuristicsNet net,
			boolean showSplitJoinSemantics) {
		super("Heuristics net");
		this.net = net;
		this.showSplitJoinSemantics = showSplitJoinSemantics;
	}

	public void writeToDot(Writer bw) throws IOException {
		nodeMapping.clear();
		if (showSplitJoinSemantics) {
			net.writeToDotWithSplitJoinSemantics(bw);
		} else {
			net.writeToDotWithoutSplitJoinSemantics(bw);
		}
	}
}

class TreeSetWithStringComparator implements Comparator {

	public TreeSetWithStringComparator() {

	}

	public int compare(Object obj1, Object obj2) {
		TreeSet set1 = (TreeSet) obj1;
		TreeSet set2 = (TreeSet) obj2;

		// checking the set size
		if (set1.size() != set2.size()) {
			return set1.size() - set2.size();
		} else {
			// sets have the same size
			Iterator i1 = set1.iterator();
			Iterator i2 = set2.iterator();
			while (i1.hasNext()) {
				String str1 = (String) i1.next();
				String str2 = (String) i2.next();
				int comparison = str1.compareTo(str2);
				if (comparison != 0) {
					return comparison;
				}

			}

		}

		return 0;

	}

}
