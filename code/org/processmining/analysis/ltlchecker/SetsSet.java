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

package org.processmining.analysis.ltlchecker;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.processmining.analysis.ltlchecker.parser.Attribute;
import org.processmining.analysis.ltlchecker.parser.LTLParser;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Progress;

public class SetsSet {

	/**
	 * SetsSet is a container of the attributes of type set. There are actually
	 * two sets of sets: one for the process instance attributes and one for the
	 * audit trail entries.
	 * 
	 * @version 0.2
	 * @author HT de Beer
	 */

	// FIELDS
	/**
	 * The set containing the sets of the process instance sets.
	 */
	TreeMap<String, Set<SetsSetItem>> piSets;

	/**
	 * The set containing the sets of the audit trail entries.
	 */
	TreeMap<String, Set<SetsSetItem>> ateSets;

	/**
	 * If only standard sets are used, flag it, to save time :-).
	 */
	boolean standardSetsOnly;

	// CONSTRUCTORS

	public SetsSet(LTLParser parser, LogSummary log) {

		Attribute attr;
		String name;

		// If no sets are created at all, only standard ones are used.
		standardSetsOnly = log.getConceptsAndInstances().isEmpty();

		piSets = new TreeMap<String, Set<SetsSetItem>>();
		ateSets = new TreeMap<String, Set<SetsSetItem>>();

		for (Object o : parser.getAttributes()) {
			attr = (Attribute) o;

			if (attr.getType() == Attribute.SET) {
				// Only creating sets for those attributes which have type set.
				if (attr.getScope() == Attribute.PI) {
					// Scope is the process instance
					name = attr.getAttributeId().substring(3); // cut `pi.'
					piSets.put(name, new HashSet<SetsSetItem>());
					standardSetsOnly = false;
				} else {
					String[] items = new String[0];

					// scope is the audit trail entry
					name = attr.getAttributeId().substring(4); // cut `ate.'

					if (name.equals("WorkflowModelElement")) {
						items = log.getModelElements();
					} else if (name.equals("EventType")) {
						items = log.getEventTypes();
					} else if (name.equals("Originator")) {
						items = log.getOriginators();
					} else { // a data element set attribute
						standardSetsOnly = false;
					}

					Set<SetsSetItem> set = new HashSet<SetsSetItem>(
							items.length);
					if (log.getConceptsAndInstances().isEmpty()) {
						for (String item : items) {
							set.add(new SetsSetItem(item));
						}
					}
					ateSets.put(name, set);
				}
			}
		}
	}

	// METHODS

	/**
	 * Are there only standard sets used, that is, WorkFlowModelElement,
	 * Originator and EventType?
	 * 
	 * @return If only standard sets are used, return true.
	 */
	public boolean standardSetsOnly() {
		return standardSetsOnly;
	}

	/**
	 * Get an set given the name and the scope of the set.
	 * 
	 * @param name
	 *            The name of the set.
	 * @param scope
	 *            The scope of the set.
	 * 
	 * @return The set specified by name and scope.
	 */
	public Set<SetsSetItem> getSet(String name, int scope) {
		String cuttedName;

		if (scope == Attribute.PI) {
			cuttedName = name.substring(3); // cut `pi.'

			if (piSets.containsKey(cuttedName)) {
				return piSets.get(cuttedName);
			}
		} else {
			cuttedName = name.substring(4); // cut `ate.'

			if (ateSets.containsKey(cuttedName)) {
				return ateSets.get(cuttedName);
			}
		}
		return null;
	}

	/**
	 * Fill the sets with information of a logreader.
	 * 
	 * @param log
	 *            A {@see LogReader} object containing all the processinstances.
	 * @param p
	 *            A (@see Progress} to show progress. (can be null)
	 */
	public void fill(final LogReader log, final Progress p) {
		if (standardSetsOnly) {
			return;
		}

		int j = 0;
		Iterator piIt = log.instanceIterator();

		while (piIt.hasNext()) {
			ProcessInstance pi = (ProcessInstance) piIt.next();
			fillPiSets(pi);

			if (p != null) {
				p.setProgress(j);
				j++;
			}

			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			Iterator ateIt = ates.iterator();
			while (ateIt.hasNext()) {
				AuditTrailEntry ate = (AuditTrailEntry) ateIt.next();
				fillAteSets(ate);
			}
		}
	}

	/**
	 * Fill the pi sets with the dataelements of a process instance.
	 * 
	 * @param pi
	 *            The process instance to fill the pi sets with.
	 */
	private void fillPiSets(ProcessInstance pi) {
		for (Map.Entry<String, Set<SetsSetItem>> item : piSets.entrySet()) {
			String name = item.getKey();
			Set<SetsSetItem> items = item.getValue();

			if (pi.getAttributes().containsKey(name)) {
				items.add(new SetsSetItem(name, pi.getDataAttributes()
						.getModelReferences(name)));
			}
		}
	}

	/**
	 * Fill the ate sets with the dataelements of a audit trail entry.
	 * 
	 * @param ate
	 *            The audit trail entry to fill the ate sets with.
	 */
	private void fillAteSets(AuditTrailEntry ate) {
		for (Map.Entry<String, Set<SetsSetItem>> item : ateSets.entrySet()) {
			String name = item.getKey();
			Set<SetsSetItem> items = item.getValue();

			if (name.equals("WorkflowModelElement")) {
				items.add(new SetsSetItem(ate.getElement(), ate
						.getElementModelReferences()));
			} else if (name.equals("EventType")) {
				items.add(new SetsSetItem(ate.getType(), ate
						.getTypeModelReferences()));
			} else if (name.equals("Originator")) {
				items.add(new SetsSetItem(ate.getOriginator(), ate
						.getOriginatorModelReferences()));
			} else if (name.equals("Timestamp")) {
				items.add(new SetsSetItem(ate.getTimestamp().toString()));
			} else if (ate.getAttributes().containsKey(name)) {
				items.add(new SetsSetItem(ate.getAttributes().get(name), ate
						.getDataAttributes().getModelReferences(name)));
			}
		}
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (Map.Entry<String, Set<SetsSetItem>> item : piSets.entrySet()) {
			result.append("PI: " + item.getKey() + "\n");
			for (SetsSetItem i : item.getValue()) {
				result.append("    " + i.getName() + " [");
				for (String uri : i.getModelReferences()) {
					result.append(" @" + uri);
				}
				result.append(" ]\n");
			}
		}
		for (Map.Entry<String, Set<SetsSetItem>> item : ateSets.entrySet()) {
			result.append("ATE: " + item.getKey() + "\n");
			for (SetsSetItem i : item.getValue()) {
				result.append("    " + i.getName() + " [");
				for (String uri : i.getModelReferences()) {
					result.append(" @" + uri);
				}
				result.append(" ]\n");
			}
		}
		return result.toString();
	}

}
