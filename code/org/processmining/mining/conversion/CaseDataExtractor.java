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

package org.processmining.mining.conversion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ModelElement;
import org.processmining.framework.log.ModelElements;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.SwingWorker;
import org.processmining.mining.MiningPlugin;
import org.processmining.mining.MiningResult;

/*
 * @author Eric Verbeek
 * @version 1.0
 */

public class CaseDataExtractor implements MiningPlugin {

	// To save the output to.
	private BufferedWriter theWriter;
	private int nofChars = 0;
	private int nofLines = 0;

	// To read information from.
	private LogReader theLog;

	// Obsolete at the moment.
	private int theKind;

	// To read the settings from.
	private CaseDataExtractorOptions theOptions;

	// To determine a low and high service times among instances of one model
	// element.
	private long theLowServiceTime, theHighServiceTime;

	// To hold the model elements.
	public String[] theModelElements;

	// To hold the originators.
	public String[] theOriginators;

	// To hold the even types.
	public String[] theEventTypes;

	// To hold the data elements present in model elements.
	public String[] theATEDataElements;

	// To hold the data elements present in process instances.
	public String[] thePIDataElements;

	// Some global constants.
	private final static String separator = ",";
	private final static String newline = "\n";
	private final static String quote = "\"";
	private final int timeUnitInMilliseconds = 1000;

	private int theRun;
	private int theColumn;
	private boolean[] isNonNull = null;
	private boolean theExporterIsBusy = false;

	// Constructor. Not a lot to do here at the moment.
	public CaseDataExtractor() {
	}

	// Get the name of this plug-in.
	public String getName() {
		return "Case data extraction plugin";
	}

	// Return a panel for this plug-in.
	/*
	 * Intially, the panel is blank. Only after mining we fill the panel. Reason
	 * is that before mining we lack sufficient information to fill the panel.
	 */
	public JPanel getOptionsPanel(LogSummary summary) {

		theOptions = new CaseDataExtractorOptions(this);
		/** @todo Implement this org.processmining.mining.MiningAlgorithm method */
		return null;
	}

	// Mine the log.
	/*
	 * Extracts information from the mined data to fill the panel. After the
	 * user has selected the appropriate settings and pressed "Save" the data is
	 * written to the file the user selected.
	 */
	public MiningResult mine(LogReader log) {
		theLog = log;
		theOriginators = theLog.getLogSummary().getOriginators();
		theEventTypes = theLog.getLogSummary().getEventTypes();
		theModelElements = theLog.getLogSummary().getModelElements();
		theATEDataElements = GetATEDataElements();
		thePIDataElements = GetPIDataElements();
		theKind = theOptions.getKind();

		// Fill the panel such that the user can (de)select items.
		theOptions.Clear();
		theOptions.Add9("Save results");
		theOptions.Add0(thePIDataElements, "Process data");
		theOptions.Add1(theATEDataElements, "Model element data");
		theOptions.Add2(theOriginators, "Originator");
		theOptions.Add3(theEventTypes, "Event type");

		return new CaseDataResult(theOptions, log);
	}

	// Extract the set of data elements belonging to model element instances
	// from the log.
	private String[] GetATEDataElements() {
		// ds will contain all data elements.
		HashSet ds = new HashSet();
		Iterator iit = theLog.instanceIterator();
		// theLog.reset();
		while (iit.hasNext()) {
			ProcessInstance pi = (ProcessInstance) iit.next();
			Iterator ait = pi.getAuditTrailEntryList().iterator();
			while (ait.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ait.next();
				ds.addAll(ate.getAttributes().keySet());
			}
		}
		// Convert ds to an array of Strings.
		String s[] = new String[ds.size() + 1];
		Iterator i = ds.iterator();
		int c = 1;
		s[0] = "@timestamp";
		while (i.hasNext()) {
			s[c] = (String) i.next();
			c++;
		}
		return s;
	}

	// Extract the set of data elements belonging to process instances from the
	// log.
	private String[] GetPIDataElements() {
		// ds will contain all data elements.
		HashSet ds = new HashSet();
		Iterator pit = theLog.instanceIterator();
		// theLog.reset();
		while (pit.hasNext()) {
			ProcessInstance pi = (ProcessInstance) pit.next();
			ds.addAll(pi.getAttributes().keySet());
		}
		// Convert ds to an array of Strings.
		String s[] = new String[ds.size()];
		Iterator i = ds.iterator();
		int c = 0;
		while (i.hasNext()) {
			s[c] = (String) i.next();
			c++;
		}
		return s;
	}

	// Print values of data elements belonging to the given process instance.
	private void PrintPIDataElements(ProcessInstance pi) {
		// Get those data elements.
		Map des = pi.getAttributes();
		// Check in order as stored in our array.
		for (int d = 0; d < thePIDataElements.length; d++) {
			// Check whether user is interested in this data element.
			if (theOptions.IsSelected0(d)) {
				// Yes, s/he's interested. Print it.
				if (des.containsKey(thePIDataElements[d])) {
					Print(quote);
					Print(des.get(thePIDataElements[d]).toString());
					Print(quote);
				}
				Print(separator);
			}
			// Otherwise, ignore it.
		}
	}

	// Print values of data elements belonging to the i-th model element
	// instance of the process instance.
	private void PrintATEDataElements(ProcessInstance pi, int i) {
		// Get all model elements.
		ModelElements mes = pi.getModelElements();
		// Check data elements in order as stored in our array.
		for (int d = 0; d < theATEDataElements.length; d++) {
			// Check whether user is interested in this data element.
			if (theOptions.IsSelected1(d)) {
				boolean b = true;
				// Yes, s/he's interested. Continue.
				Iterator mit = mes.iterator();
				// Find and print first entry for the i-th model element.
				while (b && mit.hasNext()) {
					ModelElement me = (ModelElement) mit.next();
					if (me.getName().compareTo(theModelElements[i]) == 0) {
						// Found an entry for the i-th model element.
						// Get its instances.
						Set<AuditTrailEntry> instanceSet = me.getInstances();
						Iterator<AuditTrailEntry> it = instanceSet.iterator();
						// Check all instances for the selected data element.
						while (b && it.hasNext()) {
							AuditTrailEntry ate = it.next();
							// Get data elements for this entry.
							if (d > 0) {
								Map des = ate.getAttributes();
								if (des.containsKey(theATEDataElements[d])) {
									// Found the data element. Print it and stop
									// searching.
									b = false;
									Print(quote);
									Print(des.get(theATEDataElements[d])
											.toString());
									Print(quote);
								}
							} else if (d == 0) {
								b = false;
								Print(quote);
								Print(String.valueOf(getTime(ate)
										/ timeUnitInMilliseconds));
								Print(quote);
							}
						}
					}
				}
				Print(separator);
			}
			// Otherwise, ignore it.
		}
	}

	// Print the name of the given process instance to the output file.
	private void PrintName(ProcessInstance aProcessInstance) {
		Print(quote);
		Print(aProcessInstance.getName());
		Print(quote);
		Print(separator); // End of name
	}

	// Return the timestamp (as long) of the given ate, and -1 if it has non
	// timestamp.
	private long getTime(AuditTrailEntry ate) {
		if (ate.getTimestamp() != null) {
			return ate.getTimestamp().getTime();
		}
		return -1;
	}

	// Determine and print the sojourn time of the given process instance to
	// the output file.
	private void PrintSojournTime(ProcessInstance aProcessInstance) {
		long et = -1, lt = -1, t;
		AuditTrailEntryList ates = aProcessInstance.getAuditTrailEntryList();
		Iterator ait = ates.iterator();
		while (ait.hasNext()) {
			AuditTrailEntry ate = (AuditTrailEntry) ait.next();
			t = getTime(ate);
			if (t != -1) {
				if (et == -1 || et > t) {
					et = t;
				}
				if (lt == -1 || lt < t) {
					lt = t;
				}
			}
		}
		if (et != -1 && lt != -1) {
			Print(quote);
			Print(String.valueOf((lt - et) / timeUnitInMilliseconds));
			Print(quote);
		}
		Print(separator); // End of sojourn time
	}

	/*
	 * Determine and print the servicetime for a given model element instance.
	 * The servicetime is defined as being the minimal time of exit for the
	 * state STARTED minus the maximal time of entry for this state.
	 * 
	 * Note that this method determines a maximal time of entry (min) and the
	 * minimal time of exit (max) time for every possible state.
	 */
	private void SetServiceTime(ProcessInstance pi, int i,
			Set<AuditTrailEntry> ates) {
		String[] ets = LogStateMachine.EVENT_TYPES;
		String[] sts = LogStateMachine.STATE_TYPES;
		long[] event = new long[ets.length];
		long[] min = new long[sts.length];
		long[] max = new long[sts.length];
		long[] min1 = new long[sts.length];
		long[] max1 = new long[sts.length];
		int e;
		for (e = 0; e < ets.length; e++) {
			event[e] = -1;
		}
		// Collect event times
		Iterator<AuditTrailEntry> atesIt = ates.iterator();
		while (atesIt.hasNext()) {
			AuditTrailEntry ate = atesIt.next();
			long t = getTime(ate);
			String s = ate.getType();
			for (e = 0; e < ets.length; e++) {
				if (s.compareTo(ets[e]) == 0) {
					event[e] = t;
				}
			}
		}
		// Transfer event times to state times.
		// For a high time (upper bound), we can use any event that can happen
		// only
		// before or after we have left this state and the
		for (int s = 0; s < sts.length; s++) {
			min[s] = max[s] = min1[s] = max1[s] = -1;
			for (e = 0; e < ets.length; e++) {
				if (event[e] != -1) {
					if (LogStateMachine.inPreset(s, e, true)) {
						if (min[s] == -1 || min[s] < event[e]) {
							min[s] = event[e];
						}
					}
					if (LogStateMachine.inPostset(s, e, true)) {
						if (max[s] == -1 || max[s] > event[e]) {
							max[s] = event[e];
						}
					}
					if (LogStateMachine.inPreset(s, e, false)) {
						min1[s] = event[e];
					}
					if (LogStateMachine.inPostset(s, e, false)) {
						max1[s] = event[e];
					}
				}
			}
		}
		if (min[LogStateMachine.ORD_STARTED] != -1
				&& max[LogStateMachine.ORD_STARTED] != -1) {
			// Sufficient information to print a high service time
			if (min1[LogStateMachine.ORD_STARTED] != -1
					&& max1[LogStateMachine.ORD_STARTED] != -1) {
				// Sufficient information to print a low service time
				theLowServiceTime = (max1[LogStateMachine.ORD_STARTED] - min1[LogStateMachine.ORD_STARTED])
						/ timeUnitInMilliseconds;
			} else {
				// Insufficient information to print a low service time, but we
				// have sufficicent information
				// to print a high service time. Print 0 as low service time.
				theLowServiceTime = 0;
			}
			theHighServiceTime = (max[LogStateMachine.ORD_STARTED] - min[LogStateMachine.ORD_STARTED])
					/ timeUnitInMilliseconds;
		} else {
			theLowServiceTime = theHighServiceTime = -1;
		}
	}

	// Print the number of instances for the i-th model element in a given
	// process instance.
	private void PrintNumberOfInstances(ProcessInstance pi, int i) {
		int c = 0;
		ModelElements mes = pi.getModelElements();
		Iterator pit = mes.iterator();
		while (pit.hasNext()) {
			ModelElement me = (ModelElement) pit.next();
			if (me.getName().compareTo(theModelElements[i]) == 0) {
				c += me.getInstances().size();
			}
		}
		Print(quote);
		Print(String.valueOf(c));
		Print(quote);
		Print(separator); // End of number of instances.
	}

	// Print a low and a high service time for the i-th model element of the
	// given process instance.
	private void PrintServiceTimeBounds(ProcessInstance pi, int i) {
		long l = -1, h = -1;
		ModelElements mes = pi.getModelElements();
		Iterator pit = mes.iterator();
		while (pit.hasNext()) {
			ModelElement me = (ModelElement) pit.next();
			if (me.getName().compareTo(theModelElements[i]) == 0) {
				Set<AuditTrailEntry> instanceSet = me.getInstances();
				SetServiceTime(pi, i, instanceSet);
				if (theLowServiceTime != -1
						&& (l == -1 || theLowServiceTime < l)) {
					l = theLowServiceTime;
				}
				if (theHighServiceTime != -1
						&& (h != -1 || theHighServiceTime > h)) {
					h = theHighServiceTime;
				}
			}
		}
		if (l != -1) {
			Print(quote);
			Print(String.valueOf(l));
			Print(quote);
		}
		Print(separator); // End of low service time.
		if (h != -1) {
			Print(quote);
			Print(String.valueOf(h));
			Print(quote);
		}
		Print(separator); // End of high service time.
	}

	// Print the numbers of times an originator was responsible for an event for
	// the i-th model element in the given process instance.
	private void PrintOriginators(ProcessInstance pi, int i) {
		// c[e][o] will hold the number of times that originator o was
		// responsible for event e.
		int[][] c = new int[theEventTypes.length][theOriginators.length];
		for (int e = 0; e < theEventTypes.length; e++) {
			for (int o = 0; o < theOriginators.length; o++) {
				c[e][o] = 0;
			}
		}
		ModelElements mes = pi.getModelElements();
		Iterator pit = mes.iterator();
		// Fill c.
		while (pit.hasNext()) {
			ModelElement me = (ModelElement) pit.next();
			if (me.getName().compareTo(theModelElements[i]) == 0) {
				// This is thecorrect model element.
				Set<AuditTrailEntry> instanceSet = me.getInstances();
				Iterator<AuditTrailEntry> it = instanceSet.iterator();
				while (it.hasNext()) {
					AuditTrailEntry ate = it.next();
					String se = ate.getType();
					String so = ate.getOriginator();
					if (so != null) {
						for (int e = 0; e < theEventTypes.length; e++) {
							if (theOptions.IsSelected3(e)) {
								// The user is interested in this even type.
								if (se.compareTo(theEventTypes[e]) == 0) {
									// This is the correct event type.
									for (int o = 0; o < theOriginators.length; o++) {
										if (theOptions.IsSelected2(o)
												&& so
														.compareTo(theOriginators[o]) == 0) {
											// This is the correct originator,
											// and the user is interested in
											// him/her.
											c[e][o]++;
											// Terminate this inner loop.
											o = theOriginators.length;
										}
									}
									// Terminate this outer loop.
									e = theEventTypes.length;
								}
							}
						}
					}
				}
			}
		}
		// Print c.
		for (int e = 0; e < theEventTypes.length; e++) {
			if (theOptions.IsSelected3(e)) {
				for (int o = 0; o < theOriginators.length; o++) {
					if (theOptions.IsSelected2(o)) {
						Print(quote);
						Print(String.valueOf(c[e][o]));
						Print(quote);
						Print(separator);
					}
				}
			}
		}
	}

	// Print the i-th model element of the given process instance.
	private void PrintModelElement(ProcessInstance pi, int i) {
		// First, print the number of instances.
		PrintNumberOfInstances(pi, i);
		// Second, print low and high service times.
		PrintServiceTimeBounds(pi, i);
		// Third, print numbers concerning originators and model elements.
		PrintOriginators(pi, i);
		// Last, print relevant data elements.
		PrintATEDataElements(pi, i);
	}

	// Print the given process instance.
	private void PrintProcessInstance(ProcessInstance aProcessInstance) {
		// First, print the name.
		PrintName(aProcessInstance);
		// Second, print sojourn time.
		PrintSojournTime(aProcessInstance);
		// Third, print relevant data elements.
		PrintPIDataElements(aProcessInstance);
		// Last, print every model element.
		for (int i = 0; i < theModelElements.length; i++) {
			PrintModelElement(aProcessInstance, i);
		}
		Print(newline);
	}

	// print the header.
	private void PrintHeader() {
		Print(quote);
		Print("id");
		Print(quote);
		Print(separator); // name
		Print(quote);
		Print("sojournTime.seconds");
		Print(quote);
		Print(separator); // sojourn time
		for (int i = 0; i < thePIDataElements.length; i++) {
			if (theOptions.IsSelected0(i)) {
				Print(quote);
				Print("data.");
				Print(thePIDataElements[i]);
				Print(quote);
				Print(separator);
			}
		}
		for (int i = 0; i < theModelElements.length; i++) {
			Print(quote);
			Print(theModelElements[i]);
			Print(".numberOfInstances");
			Print(quote);
			Print(separator);
			Print(quote);
			Print(theModelElements[i]);
			Print(".lowServiceTime.seconds");
			Print(quote);
			Print(separator);
			Print(quote);
			Print(theModelElements[i]);
			Print(".highServiceTime.seconds");
			Print(quote);
			Print(separator);
			for (int e = 0; e < theEventTypes.length; e++) {
				if (theOptions.IsSelected3(e)) {
					for (int o = 0; o < theOriginators.length; o++) {
						if (theOptions.IsSelected2(o)) {
							Print(quote);
							Print(theModelElements[i]);
							Print(".");
							Print(String.valueOf(theEventTypes[e]));
							Print(".");
							Print(String.valueOf(theOriginators[o]));
							Print(quote);
							Print(separator);
						}
					}
				}
			}
			for (int d = 0; d < theATEDataElements.length; d++) {
				if (theOptions.IsSelected1(d)) {
					Print(quote);
					Print(theModelElements[i]);
					Print(".data.");
					Print(theATEDataElements[d]);
					Print(quote);
					Print(separator);
				}
			}
		}
		if (theRun == 1) {
			isNonNull = new boolean[theColumn + 1];
			for (int i = 0; i < isNonNull.length; i++) {
				isNonNull[i] = false;
			}
		}
		Print(newline);
	}

	// Print a string to the output file. Hardly exciting.
	private void Print(String aString) {
		if (theRun == 2
				&& (isNonNull[theColumn] || aString.compareTo(newline) == 0)) {
			try {
				if (aString.length() == 0) {
					theWriter.write("<unspecified>");
					nofChars += 13;
				} else {
					theWriter.write(aString);
					nofChars += aString.length();
					if (aString.compareTo(newline) == 0) {
						nofLines++;
					}
				}
			} catch (IOException e) {
				Message.add("Exception " + e.getMessage(), Message.ERROR);
			}
		}
		if (aString.compareTo(newline) == 0) {
			theColumn = 0;
		} else if (aString.compareTo(separator) == 0) {
			theColumn++;
		} else if (aString.compareTo(quote) == 0) {
		} else if (theRun == 1 && isNonNull != null && aString.length() > 0
				&& aString.compareTo("0") != 0 && aString.compareTo("=0") != 0) {
			isNonNull[theColumn] = true;
		}
	}

	// print the converted log.
	public void PrintLog(BufferedWriter aWriter, String tag) throws IOException {
		if (theExporterIsBusy) {
			return;
		}
		theExporterIsBusy = true;

		theWriter = aWriter;
		nofLines = 0;
		nofChars = 0;

		SwingWorker w = new SwingWorker() {
			public Object construct() {
				Boolean result;
				Progress p = new Progress("Process instance:", 0, 2 * theLog
						.getLogSummary().getNumberOfProcessInstances());
				int i = 0;
				isNonNull = null;

				for (theRun = 1; (theRun < 3); theRun++) {
					theColumn = 0;
					// Print the header.
					PrintHeader();
					// print every process instance.
					Iterator pit = theLog.instanceIterator();
					while ((pit.hasNext()) && !p.isCanceled()) {
						p.setProgress(i++);
						ProcessInstance pi = (ProcessInstance) pit.next();
						p.setNote(pi.getName());
						PrintProcessInstance(pi);
					}
				}

				result = new Boolean(p.isCanceled());
				p.close();
				return result;
			}

			public void finished() {
				theExporterIsBusy = false;
			}
		};

		w.start();
		try {
			w.join();
		} catch (InterruptedException ex) {
		}
		if (w.get() == null || ((Boolean) w.get()).booleanValue() == true) {
			throw new IOException("export interrupted");
		}

		if (tag != null) {
			Message.add("<" + tag + " nofLines=\"" + nofLines
					+ "\" nofChars=\"" + nofChars + "\"/>", Message.TEST);
		}
	}

	public String getHtmlDescription() {
		return "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:mining:cde";
	}
}
