package org.processmining.mining.prediction;

import java.util.*;
import java.io.*;

import org.processmining.framework.log.*;
import org.processmining.framework.log.rfb.BufferedLogReader;
import org.processmining.framework.ui.Message;

/**
 * @author Ronald Crooy
 * 
 */
public class LogVariableExtracter {

	private static LogVariableExtracter uniqueInstance;

	public static synchronized LogVariableExtracter getInstance() {
		return uniqueInstance;
	}

	PredictionMinerSettingsBasedOnLogSummary localSettings;
	BufferedLogReader log;

	CaseSet extractedVariables;

	public LogVariableExtracter(LogReader logreader) {
		localSettings = PredictionMinerSettingsBasedOnLogSummary.getInstance();
		/*
		 * make sure there is a buffered log reader
		 */
		if (!(logreader instanceof BufferedLogReader)) {
			LogReader newlog = null;
			try {
				newlog = BufferedLogReader.createInstance(logreader, logreader
						.processInstancesToKeep());
			} catch (Exception ex2) {
			}
			log = (BufferedLogReader) newlog;
		} else {
			Message.add("BufferedLogReader found");
			log = (BufferedLogReader) logreader;
		}

		/*
		 * if settings.target==1 (remaining time until X) then filter the log to
		 * contain only traces with X, X being settings.targetelement
		 */
		if (localSettings.target == 0) {
			localSettings.targetElement = null;
		}
		if (localSettings.target == 1) {
			filter();
			Message.add("log filterd");
		}

		extractedVariables = new CaseSet(log);
		uniqueInstance = this;

		extract();
		this.extractedVariables.removeAttributeConstants();
		this.extractedVariables.removeDurationConstants();

	}

	/**
	 * map with values for audittrail entries stored under attrib all arrays
	 * must be equal in size
	 * 
	 * @param values
	 * @param atrib
	 */
	public void setLogEventDataAttributes(BufferedLogReader exportlog,
			Integer[] procInstNumbers, Integer[] eventNumbers, Double[] values,
			String attribname) {
		for (int i = 0; i < procInstNumbers.length; i++) {
			try {
				ProcessInstance pi = exportlog.getInstance(procInstNumbers[i]);
				AuditTrailEntry ate = pi.getAuditTrailEntryList().get(
						eventNumbers[i]);
				Double val = values[i];
				ate.setAttribute(attribname, val.toString());
				pi.getAuditTrailEntryList().replace(ate, eventNumbers[i]);

			} catch (IOException ex3) {
				System.out.println("error writing to log");
			}// CATCH
		}// ROF
	}// END

	/**
	 * this function writes the array of 'values' to the process as an attribute
	 * under the 'name'
	 * 
	 * @param values
	 * @param names
	 */
	public void setLogProcessAttribute(BufferedLogReader exportlog,
			String value, String name) {
		try {
			exportlog.getProcess(0).setAttribute(name, value);
		} catch (Exception ex) {
			System.out.println("error writing to log: " + ex.getMessage());
		}
	}

	/**
	 * this function takes an array with possible duplicate integers, the array
	 * is filtered to unique integers and the log is filtered to contain only
	 * these instances.
	 * 
	 * @param filter
	 * @return a buffered log reader
	 */
	public LogReader getLogReader(int[] filter) {
		LogReader newlog = null;
		ArrayList temp = new ArrayList();

		// sort list and enforce unique items
		for (int i = 0; i < filter.length; i++) {
			if (!temp.contains(filter[i])) {
				temp.add(filter[i]);
			}
		}
		int[] result = new int[temp.size()];
		Iterator it = temp.iterator();
		int i = 0;
		while (it.hasNext()) {
			result[i] = ((Integer) it.next()).intValue();
			i++;
		}

		try {
			newlog = BufferedLogReader.createInstance(log, result);

		} catch (Exception ex2) {
			System.out.println("error creating BufferedLogReader");
		}
		return newlog;
	}

	/**
	 * filters the log to contain only traces with target element X
	 */
	private void filter() {
		Iterator<ProcessInstance> caseIterator;
		HashSet<Integer> instancelist = new HashSet<Integer>();
		LogReader newlog = null;
		int pid = 0;
		for (ProcessInstance procInst : log.getInstances()) {
			for (AuditTrailEntry ate : procInst.getListOfATEs()) {
				if (ate.getElement().equals(localSettings.targetElement)) {
					instancelist.add(pid);
				}
			}
			pid++;
		}
		int[] result = new int[instancelist.size()];
		int i = 0;
		for (Integer val : instancelist
				.toArray(new Integer[instancelist.size()])) {
			result[i] = val;
			i++;
		}
		try {
			newlog = BufferedLogReader.createInstance(log, result);
			log = (BufferedLogReader) newlog;
		} catch (Exception ex2) {
			System.out.println("error creating BufferedLogReader");
		}
	}

	private void extract() {
		for (ProcessInstance pi : log.getInstances()) {
			// reset counters, etc
			extractedVariables.initProcessInstance(pi);

			ArrayList<String> startedElement = new ArrayList<String>();
			ArrayList<Long> startTime = new ArrayList<Long>();
			// store all case attributes
			if (localSettings.useAttributes) {
				for (String attrib : pi.getAttributes().keySet()) {
					String value = pi.getAttributes().get(attrib);
					if (!(value == null)) {
						extractedVariables.storeCaseAttribute(attrib, value);
					}
				}
			}
			for (AuditTrailEntry ate : pi.getListOfATEs()) {
				if (localSettings.useElements.contains(ate.getElement())) {
					String element = ate.getElement();
					if (localSettings.startEvents.contains(ate.getType())) {
						startedElement.add(element);
						startTime.add(ate.getTimestamp().getTime());
					} else if (localSettings.completeEvents.contains(ate
							.getType())) {
						// complete means occurrence

						if (localSettings.target == 2) {
							if (localSettings.targetElement.equals(ate
									.getElement())) {
								extractedVariables
										.storeTargetElementOccurrence();
								extractedVariables.storeOccurrence(element, ate
										.getTimestamp().getTime());
							} else {
								extractedVariables.storeOccurrence(element, ate
										.getTimestamp().getTime());
							}
						} else {
							extractedVariables.storeOccurrence(element, ate
									.getTimestamp().getTime());
						}

						// if a previous start was found
						if (localSettings.useDurations
								&& startedElement.contains(element)) {
							if (localSettings.target == 2) {
								if (localSettings.targetElement.equals(ate
										.getElement())) {
									// take max(0,timediff) of timediff then
									// divide by timesize
									Long tempdur = Math.max(0, ate
											.getTimestamp().getTime()
											- startTime.get(startedElement
													.indexOf(element)));
									extractedVariables.storeDuration(element,
											tempdur);
									/*
									 * remove start for this event, remove only
									 * 1 element, to enable parrallel tasks
									 */
									startTime.remove(startedElement
											.indexOf(element));
									startedElement.remove(element);
								} else {
									// take max(0,timediff) of timediff then
									// divide by timesize
									Long tempdur = Math.max(0, ate
											.getTimestamp().getTime()
											- startTime.get(startedElement
													.indexOf(element)));
									extractedVariables.storeDuration(element,
											tempdur);
									/*
									 * remove start for this event, remove only
									 * 1 element, to enable parrallel tasks
									 */
									startTime.remove(startedElement
											.indexOf(element));
									startedElement.remove(element);
								}
							} else {
								// take max(0,timediff) of timediff then divide
								// by timesize
								Long tempdur = Math.max(0, ate.getTimestamp()
										.getTime()
										- startTime.get(startedElement
												.indexOf(element)));
								extractedVariables.storeDuration(element,
										tempdur);
								/*
								 * remove start for this event, remove only 1
								 * element, to enable parrallel tasks
								 */
								startTime.remove(startedElement
										.indexOf(element));
								startedElement.remove(element);
							}
						}

						if (localSettings.useAttributes) {
							for (String attrib : ate.getDataAttributes()
									.keySet()) {
								String value = ate.getAttributes().get(attrib);
								if (!(value == null)) {
									extractedVariables.storeEventAttribute(
											attrib, ate.getDataAttributes()
													.get(attrib));
								}
							}
							// cases.storeEventAttribute(element+"_originator",
							// ate.getOriginator());
						}

						// complete-events always mean new prefix
						extractedVariables.nextPrefix();
					}
					extractedVariables.nextAuditTrail();
				}
			}
			extractedVariables.nextProcessInstance();
		}
	}

}

/**
 * @author Ronald Crooy
 * 
 */
class CaseSet {
	/**
	 * this function checks every variable to eleminate constants. Currently
	 * only for attributes since these are the only problem
	 */
	public void removeAttributeConstants() {
		int i = 0;
		while (i < this.atrributeNames.size()) {
			boolean isConstant = true;
			int j = 0;
			while (isConstant && j < this.prefixedAttributes.length - 1) {
				String a = this.prefixedAttributes[j + 1][i];
				String b = this.prefixedAttributes[j][i];
				if (a == null) {
					a = " ";
				}
				if (b == null) {
					b = " ";
				}
				if (a.equals(b)) {
					j++;
				} else {
					isConstant = false;
				}
			}
			if (isConstant) {
				System.out.println("removing attribute "
						+ this.atrributeNames.get(i));
				// System.out.println("size before "+this.atrributeNames.size());
				// copy array without j to new array
				String[][] newPrefixedAttributes = new String[this.prefixedAttributes.length][this.atrributeNames
						.size() - 1];
				for (int x = 0; x < this.prefixedAttributes.length; x++) {
					int y = 0;
					while (y < i) {
						newPrefixedAttributes[x][y] = this.prefixedAttributes[x][y];
						y++;
					}
					y = i + 1;
					while (y < this.atrributeNames.size()) {
						newPrefixedAttributes[x][y - 1] = this.prefixedAttributes[x][y];
						y++;
					}
				}
				// overwrite array
				this.prefixedAttributes = newPrefixedAttributes.clone();

				// copy array without j to new array
				String[][] newCompleteAttributes = new String[this.completeAttributes.length][this.atrributeNames
						.size() - 1];
				for (int x = 0; x < this.completeAttributes.length; x++) {
					int y = 0;
					while (y < i) {
						newCompleteAttributes[x][y] = this.completeAttributes[x][y];
						y++;
					}
					y = i + 1;
					while (y < this.atrributeNames.size()) {
						newCompleteAttributes[x][y - 1] = this.completeAttributes[x][y];
						y++;
					}
				}
				// overwrite array
				this.completeAttributes = newCompleteAttributes.clone();

				// finally remove j from the index
				this.atrributeNames.remove(i);
				// System.out.println("size after "+this.atrributeNames.size());
				i = 0;
			} else {
				i++;
			}
		}

	}

	public void removeDurationConstants() {
		int i = 0;
		int minimumcount = 10;
		while (i < this.durationNames.size()) {
			boolean isConstant = true;
			int j = 0;
			int count = 0;
			while (isConstant && j < this.prefixedDurations.length - 1) {
				double a = this.prefixedDurations[j + 1][i];
				double b = this.prefixedDurations[j][i];

				if (a == b) {

				} else {
					count++;
					// isConstant=false;
				}
				j++;
			}
			if (count < minimumcount) {
				isConstant = true;
			} else {
				isConstant = false;
			}
			if (isConstant) {
				System.out.println("removing duration "
						+ this.durationNames.get(i));
				// System.out.println("size before "+this.atrributeNames.size());
				// copy array without j to new array
				double[][] newPrefixedDurations = new double[this.prefixedDurations.length][this.durationNames
						.size() - 1];
				for (int x = 0; x < this.prefixedDurations.length; x++) {
					int y = 0;
					while (y < i) {
						newPrefixedDurations[x][y] = this.prefixedDurations[x][y];
						y++;
					}
					y = i + 1;
					while (y < this.durationNames.size()) {
						newPrefixedDurations[x][y - 1] = this.prefixedDurations[x][y];
						y++;
					}
				}
				// overwrite array
				this.prefixedDurations = newPrefixedDurations.clone();

				// copy array without j to new array
				double[][] newCompleteDurations = new double[this.completeDurations.length][this.durationNames
						.size() - 1];
				for (int x = 0; x < this.completeDurations.length; x++) {
					int y = 0;
					while (y < i) {
						newCompleteDurations[x][y] = this.completeDurations[x][y];
						y++;
					}
					y = i + 1;
					while (y < this.durationNames.size()) {
						newCompleteDurations[x][y - 1] = this.completeDurations[x][y];
						y++;
					}
				}
				// overwrite array
				this.completeDurations = newCompleteDurations.clone();

				// finally remove j from the index
				this.durationNames.remove(i);
				// System.out.println("size after "+this.atrributeNames.size());
				i = 0;
			} else {
				i++;
			}
		}

	}

	private void initializePrivateVars() {
		this.ateID = 0;
		this.piID = 0;
		this.prefixID = 0;
	}

	/**
	 * increments counter for audittrails
	 * 
	 * @param ate
	 */
	public void nextAuditTrail() {
		this.ateID++;
	}

	private int ateID;

	/**
	 * stores the timestamps needed for remaining and total cycle time, corrects
	 * prefixes to be empty
	 * 
	 * @param pi
	 */
	public void initProcessInstance(ProcessInstance pi) {

		this.ateID = 0;

		currentPrefixes = new ArrayList<Integer>();

		this.timeOfBegin = pi.getListOfATEs().get(0).getTimestamp().getTime();
		this.timeOfEnd = pi.getListOfATEs().get(pi.getListOfATEs().size() - 1)
				.getTimestamp().getTime();
		Double totalduration = Math.max(0.0,
				(double) (this.timeOfEnd - this.timeOfBegin))
				/ (double) localSettings.timeSize;

		this.completeTotalCycleTimes[this.piID] = totalduration.doubleValue();

		this.prefixedTotalCycleTimes[this.prefixID] = totalduration
				.doubleValue();
		this.prefixedRemainingCycleTimes[this.prefixID] = totalduration
				.doubleValue();
		this.prefixedTargetElement[this.prefixID] = 0;

		// reset the current row becase there is a new proces instance.
		// storeprefix copied the last row into this one.
		this.prefixedDurations[this.prefixID] = new double[this.durationNames
				.size()];
		this.prefixedAttributes[this.prefixID] = new String[this.atrributeNames
				.size()];
		this.prefixedOccurrences[this.prefixID] = new int[this.occurrenceNames
				.size()];
	}

	private Long timeOfBegin;
	private Long timeOfEnd;
	private ArrayList<Integer> currentPrefixes;

	/**
	 * increments counter for process instances and store case attributes
	 * 
	 * @param pi
	 */
	public void nextProcessInstance() {
		// precondtion: process instance is processed

		// average durations
		for (int i = 0; i < this.durationNames.size(); i++) {
			Double sum = this.completeDurations[this.piID][i];
			if (sum > 0) {
				String elementname = this.durationNames.get(i);
				int occurrenceindex = this.occurrenceNames.indexOf(elementname);
				Integer count = this.completeOccurrences[this.piID][occurrenceindex];
				this.completeDurations[this.piID][i] = sum / count;
			}
		}// all durations for the complete row are averaged

		this.piids2prefix.add(this.currentPrefixes);

		this.piID++;
	}

	private int piID;

	public void storeTargetElementOccurrence() {
		this.prefixedTargetElement[this.prefixID]++;
		for (Integer previd : this.currentPrefixes) {
			this.prefixedTargetElement[previd] = this.prefixedTargetElement[this.prefixID];
		}
	}

	/**
	 * store the occurrence, and calculate remaining time, etc
	 * 
	 * @param element
	 */
	public void storeOccurrence(String elementname, Long time) {
		String element = cleanString(elementname);
		int index = this.occurrenceNames.indexOf(element);
		this.completeOccurrences[this.piID][index]++;
		this.prefixedOccurrences[this.prefixID][index]++;

		Double sofar = Math.max(0.0, (double) (time - this.timeOfBegin))
				/ (double) localSettings.timeSize;
		Double remaining = Math.max(0.0, (double) (this.timeOfEnd - time))
				/ (double) localSettings.timeSize;

		this.prefixedRemainingCycleTimes[this.prefixID] = remaining
				.doubleValue();
		this.prefixedTimePassed[this.prefixID] = sofar.doubleValue();
	}

	/**
	 * store the duration
	 * 
	 * @param element
	 * @param time
	 */
	public void storeDuration(String elementname, Long time) {
		String element = cleanString(elementname);
		int index = this.durationNames.indexOf(element);
		this.completeDurations[this.piID][index] += time.doubleValue();
		this.prefixedDurations[this.prefixID][index] += time.doubleValue();
	}

	/**
	 * store the case attribute
	 * 
	 * @param name
	 * @param value
	 */
	public void storeCaseAttribute(String attribname, String value) {
		String name = cleanString(attribname);
		int index = this.atrributeNames.indexOf(name);
		this.completeAttributes[this.piID][index] = value;
		this.prefixedAttributes[this.prefixID][index] = value;
	}

	/**
	 * store the event attribute
	 * 
	 * @param name
	 * @param value
	 */
	public void storeEventAttribute(String attribname, String value) {
		String name = cleanString(attribname);
		int index = this.atrributeNames.indexOf(name);
		this.completeAttributes[this.piID][index] = value;
		this.prefixedAttributes[this.prefixID][index] = value;
	}

	/**
	 * while !newcase copy last row, increment counter. called after the current
	 * prefix was written.
	 */
	public void nextPrefix() {
		/*
		 * precondition: data is written, meaning no data should be written (in
		 * the current loop) after this
		 */
		this.prefixedAtids[this.prefixID] = this.ateID;
		this.prefixedPiids[this.prefixID] = this.piID;
		this.currentPrefixes.add(this.prefixID);

		this.prefixedAttributes[this.prefixID + 1] = this.prefixedAttributes[this.prefixID]
				.clone();
		this.prefixedDurations[this.prefixID + 1] = this.prefixedDurations[this.prefixID]
				.clone();
		this.prefixedOccurrences[this.prefixID + 1] = this.prefixedOccurrences[this.prefixID]
				.clone();
		this.prefixedTotalCycleTimes[this.prefixID + 1] = this.prefixedTotalCycleTimes[this.prefixID];
		this.prefixedTargetElement[this.prefixID + 1] = this.prefixedTargetElement[this.prefixID];

		// now the current prefix is copied, time to calculate average durations
		for (int i = 0; i < this.durationNames.size(); i++) {
			Double sum = this.prefixedDurations[this.prefixID][i];
			if (sum > 0) {
				String elementname = this.durationNames.get(i);
				int occurrenceindex = this.occurrenceNames.indexOf(elementname);
				Integer count = this.prefixedOccurrences[this.prefixID][occurrenceindex];
				this.prefixedDurations[this.prefixID][i] = sum / count;
			}
		}
		/* all durations are averaged over their occurences */

		/*
		 * postcondition: the current prefix is copied to the next, indexes were
		 * stored, previous row done
		 */
		this.prefixID++;
		/* ready for next prefix */
	}

	private int prefixID;

	protected double[][] prefixedDurations;
	protected int[][] prefixedOccurrences;
	protected String[][] prefixedAttributes;

	protected double[][] completeDurations;
	protected int[][] completeOccurrences;
	protected String[][] completeAttributes;

	protected ArrayList<String> durationNames;
	protected ArrayList<String> occurrenceNames;
	protected ArrayList<String> atrributeNames;

	protected int[] prefixedPiids;
	protected int[] prefixedAtids;
	protected ArrayList<ArrayList<Integer>> piids2prefix;

	// protected int[] completePiids;
	// protected int[] completeAtids;

	protected double[] prefixedRemainingCycleTimes;
	protected double[] prefixedTotalCycleTimes;
	protected double[] prefixedTimePassed;
	// protected double[] completeRemainingCycleTimes;
	protected double[] completeTotalCycleTimes;
	protected int[] prefixedTargetElement;

	PredictionMinerSettingsBasedOnLogSummary localSettings;

	/**
	 * postconditions: names and array sizes are set
	 * 
	 * @param log
	 */
	public CaseSet(BufferedLogReader log) {

		localSettings = PredictionMinerSettingsBasedOnLogSummary.getInstance();

		this.durationNames = new ArrayList<String>();
		this.occurrenceNames = new ArrayList<String>();
		this.atrributeNames = new ArrayList<String>();

		int prefixes = 0;
		HashSet<String> starts = new HashSet<String>();
		HashSet<String> dn = new HashSet<String>();
		HashSet<String> on = new HashSet<String>();
		HashSet<String> an = new HashSet<String>();

		for (ProcessInstance pi : log.getInstances()) {
			if (localSettings.useAttributes) {
				for (String name : pi.getDataAttributes().keySet()) {
					an.add(cleanString(name));
				}
			}
			for (AuditTrailEntry ate : pi.getListOfATEs()) {
				if (localSettings.useElements.contains(ate.getElement())) {
					String name = cleanString(ate.getElement());
					if (localSettings.startEvents.contains(ate.getType())) {
						starts.add(name);
					} else if (localSettings.completeEvents.contains(ate
							.getType())) {
						// every complete event means a prefix
						prefixes++;
						if (localSettings.target == 2) {
							if (localSettings.targetElement.equals(ate
									.getElement())) {
								on.add(name);
							} else {
								on.add(name);
							}
						} else {
							on.add(name);
						}

						if (localSettings.useAttributes) {
							for (String attrib : ate.getDataAttributes()
									.keySet()) {
								an.add(cleanString(attrib));
							}
							// an.add(cleanString(name+"_originator"));
						}

						if (localSettings.useDurations && starts.contains(name)) {
							if (localSettings.target == 2) {
								if (localSettings.targetElement.equals(ate
										.getElement())) {
									dn.add(name);
									starts.remove(name);
								} else {
									dn.add(name);
									starts.remove(name);
								}
							} else {
								dn.add(name);
								starts.remove(name);
							}
						}
					}
				}
			}
		}

		this.durationNames.addAll(dn);
		this.occurrenceNames.addAll(on);
		this.atrributeNames.addAll(an);

		this.prefixedDurations = new double[prefixes + 1][dn.size()];
		this.prefixedOccurrences = new int[prefixes + 1][on.size()];
		this.prefixedAttributes = new String[prefixes + 1][an.size()];
		// init attributes as " "
		for (int i = 0; i < prefixes + 1; i++) {
			for (int j = 0; j < dn.size(); j++) {
				this.prefixedDurations[i][j] = 0;
			}
			for (int j = 0; j < an.size(); j++) {
				this.prefixedAttributes[i][j] = new String(" ");
			}
		}

		this.piids2prefix = new ArrayList<ArrayList<Integer>>(log
				.numberOfInstances());

		this.prefixedPiids = new int[prefixes + 1];
		this.prefixedAtids = new int[prefixes + 1];
		this.prefixedRemainingCycleTimes = new double[prefixes + 1];
		this.prefixedTotalCycleTimes = new double[prefixes + 1];
		this.prefixedTimePassed = new double[prefixes + 1];
		this.prefixedTargetElement = new int[prefixes + 1];

		int csize = log.getLogSummary().getNumberOfProcessInstances();
		this.completeDurations = new double[csize][dn.size()];
		this.completeOccurrences = new int[csize][on.size()];
		this.completeAttributes = new String[csize][an.size()];
		// init attributes as " "
		for (int i = 0; i < csize; i++) {
			for (int j = 0; j < dn.size(); j++) {
				this.completeDurations[i][j] = 0;
			}
			for (int j = 0; j < an.size(); j++) {
				this.completeAttributes[i][j] = new String(" ");
			}
		}

		// this.completePiids=new int[csize];
		// this.completeAtids=new int[csize];
		// this.completeRemainingCycleTimes=new double[csize];
		this.completeTotalCycleTimes = new double[csize];
		initializePrivateVars();
	}

	private String cleanString(String s) {
		return s.trim().replaceAll("[^a-zA-Z0-9_]", "");
	}
}
