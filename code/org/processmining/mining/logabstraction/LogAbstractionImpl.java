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

package org.processmining.mining.logabstraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.util.MethodsForWorkflowLogDataStructures;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author not attributable
 * @version 1.0
 */

public class LogAbstractionImpl implements LogAbstraction {

	private LogEvents modelElements;
	private LogReader log;

	private DoubleMatrix1D start = null;
	private DoubleMatrix1D end = null;
	private boolean usePOInfo = false;

	public LogAbstractionImpl(LogReader log, boolean usePOInfo) {
		this.log = log;
		this.usePOInfo = usePOInfo;
		modelElements = log.getLogSummary().getLogEvents();
	}

	public LogAbstractionImpl(LogReader log) {
		this(log, false);
	}

	/**
	 * This function returns a matrix with succession relations
	 * 
	 * @param distance
	 *            The distance to look at
	 * @return The resulting matrix D[i][j] gives the number of times the
	 *         ModelElement with number i is followed by j at a given distance.
	 *         If the distance is 1 then the direct successors are given
	 * 
	 */
	public DoubleMatrix2D getFollowerInfo(int distance) throws IOException {

		int numSimilarPIs = 0;

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		// Walk through the log to create all ModelElements
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = it.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			// Create a memory for the required distance
			int[] memory = new int[distance];

			int i = 0;
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			if (ates.size() == 0) {
				continue;
			}

			if (usePOInfo
					&& pi.getAttributes()
							.containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// We are dealing with a partial order and we should use that
				// information

				HashMap<String, Integer> ateIDMap = new HashMap(ates.size());
				HashMap<String, Integer> iD2Ate = new HashMap(ates.size());
				Iterator<AuditTrailEntry> ateIt = ates.iterator();
				int index = 0;
				while (ateIt.hasNext()) {
					AuditTrailEntry ate = ateIt.next();
					ateIDMap.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), new Integer(
							modelElements.findLogEventNumber(ate.getElement(),
									ate.getType())));
					iD2Ate.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), new Integer(index));
					index++;
				}

				ateIt = ates.iterator();
				while (ateIt.hasNext()) {
					// Walk trough all audittrailentries
					AuditTrailEntry ate = ateIt.next();
					int ateIndex = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());
					findFollowers(ates, ate, ateIndex, distance, ateIDMap,
							iD2Ate, D, numSimilarPIs);
				}

			} else {
				// We should consider the linear order on events
				Iterator<AuditTrailEntry> ateIt = ates.iterator();
				while (ateIt.hasNext()) {
					// Walk trough all audittrailentries
					AuditTrailEntry ate = ateIt.next();

					if (i < distance) {
						memory[i] = modelElements.findLogEventNumber(ate
								.getElement(), ate.getType());
						i++;
						continue;
					}

					int index = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());

					if (distance == 0) {
						D
								.set(index, index, D.get(index, index)
										+ numSimilarPIs);
					} else {
						// Now, start adding values to the distance matrix
						D.set(memory[0], index, D.get(memory[0], index)
								+ numSimilarPIs);
						for (int j = 0; j < distance - 1; j++) {
							memory[j] = memory[j + 1];
						}
						memory[distance - 1] = index;
					}
				}
			}
		}
		return D;
	}

	private void findFollowers(AuditTrailEntryList ates, AuditTrailEntry ate,
			int baseAteIndex, int distance, HashMap<String, Integer> ateIDMap,
			HashMap<String, Integer> iD2Ate, DoubleMatrix2D D, int numSimilarPIs)
			throws IOException {

		String ateSucs = ate.getAttributes().get(ProcessInstance.ATT_ATE_POST);
		StringTokenizer st = new StringTokenizer(ateSucs, ",", false);
		while (st.hasMoreTokens()) {
			String nextAteId = st.nextToken();
			int nextAteIndex = ateIDMap.get(nextAteId).intValue();
			if (distance == 1) {
				D.set(baseAteIndex, nextAteIndex, D.get(baseAteIndex,
						nextAteIndex)
						+ numSimilarPIs);
			} else {
				findFollowers(ates, ates.get(iD2Ate.get(nextAteId).intValue()),
						baseAteIndex, distance - 1, ateIDMap, iD2Ate, D,
						numSimilarPIs);
			}
		}
	}

	private void findCloseIn(AuditTrailEntryList ates, AuditTrailEntry ate,
			int baseAteIndex, int distance, HashMap<String, Integer> ateIDMap,
			HashMap<String, Integer> iD2Ate, DoubleMatrix2D D,
			int numSimilarPIs, List<AuditTrailEntry> inSide) throws IOException {

		String ateSucs = ate.getAttributes().get(ProcessInstance.ATT_ATE_POST);
		StringTokenizer st = new StringTokenizer(ateSucs, ",", false);
		while (st.hasMoreTokens()) {
			String nextAteId = st.nextToken();
			int nextAteIndex = ateIDMap.get(nextAteId).intValue();
			if (distance == 0) {
				// we have reached the end point of depth-first search
				if (baseAteIndex == nextAteIndex) {
					// the base ATE is the same as this ATE, so all elements of
					// inSide should
					// be added to the distance matrix.
					Iterator<AuditTrailEntry> ateIt = inSide.iterator();
					while (ateIt.hasNext()) {
						AuditTrailEntry ateInside = ateIt.next();
						int insideAteIndex = modelElements.findLogEventNumber(
								ateInside.getElement(), ateInside.getType());
						D.set(baseAteIndex, insideAteIndex, D.get(baseAteIndex,
								insideAteIndex)
								+ numSimilarPIs);
					}
				}
				// remove the last element of the inSide list.
			} else {
				// this ate might be inside
				AuditTrailEntry thisAte = ates.get(iD2Ate.get(nextAteId)
						.intValue());
				inSide.add(thisAte);
				findCloseIn(ates, thisAte, baseAteIndex, distance - 1,
						ateIDMap, iD2Ate, D, numSimilarPIs, inSide);
			}
		}
		if (distance == 0) {
			inSide.remove(inSide.size() - 1);
		}
	}

	public DoubleMatrix1D getStartInfo() throws IOException {

		int numSimilarPIs = 0;
		if (start != null) {
			return start;
		}

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix1D V = DoubleFactory1D.sparse.make(s, 0);

		// Walk through the log to create all ModelElements
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = it.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);

			if (usePOInfo
					&& pi.getAttributes()
							.containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// We are dealing with a partial order and we should use that
				// information
				Iterator<AuditTrailEntry> ateIt = pi.getAuditTrailEntryList()
						.iterator();
				while (ateIt.hasNext()) {
					AuditTrailEntry ate = ateIt.next();
					String atePres = ate.getAttributes().get(
							ProcessInstance.ATT_ATE_PRE).trim();
					if (atePres.equals("")) {
						// This is an initial ATE
						int i = modelElements.findLogEventNumber(ate
								.getElement(), ate.getType());
						V.set(i, V.get(i) + numSimilarPIs);
					}
				}
			} else {
				AuditTrailEntry ate = null;
				ate = pi.getAuditTrailEntryList().get(0);
				if (ate != null) {
					int i = modelElements.findLogEventNumber(ate.getElement(),
							ate.getType());
					V.set(i, V.get(i) + numSimilarPIs);
				}
			}
		}
		return V;
	}

	public DoubleMatrix1D getEndInfo() throws IOException {

		int numSimilarPIs = 0;
		if (end != null) {
			return end;
		}

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix1D V = DoubleFactory1D.sparse.make(s, 0);

		// Walk through the log to create all ModelElements
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = it.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);

			if (usePOInfo
					&& pi.getAttributes()
							.containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// We are dealing with a partial order and we should use that
				// information
				Iterator<AuditTrailEntry> ateIt = pi.getAuditTrailEntryList()
						.iterator();
				while (ateIt.hasNext()) {
					AuditTrailEntry ate = ateIt.next();
					String ateSucs = ate.getAttributes().get(
							ProcessInstance.ATT_ATE_POST).trim();
					if (ateSucs.equals("")) {
						// This is a final ATE
						int i = modelElements.findLogEventNumber(ate
								.getElement(), ate.getType());
						V.set(i, V.get(i) + numSimilarPIs);
					}
				}
			} else {

				AuditTrailEntry ate = null;

				ate = pi.getAuditTrailEntryList().get(
						pi.getAuditTrailEntryList().size() - 1);
				if (ate != null) {
					int i = modelElements.findLogEventNumber(ate.getElement(),
							ate.getType());
					V.set(i, V.get(i) + numSimilarPIs);
				}
			}
		}
		return V;
	}

	public DoubleMatrix2D getCloseInInfo(int distance) throws IOException {
		int numSimilarPIs = 0;

		// Initialise the Distance matrix
		int s = modelElements.size();
		DoubleMatrix2D D = DoubleFactory2D.sparse.make(s, s, 0);

		if (distance < 2) {
			return D;
		}

		// Walk through the log to create all ModelElements
		Iterator<ProcessInstance> it = log.instanceIterator();
		while (it.hasNext()) {
			// Walk through each process instance
			ProcessInstance pi = it.next();
			numSimilarPIs = MethodsForWorkflowLogDataStructures
					.getNumberSimilarProcessInstances(pi);
			// Create a memory for the required distance
			int[] memory = new int[distance];

			int i = 0;
			AuditTrailEntryList ates = pi.getAuditTrailEntryList();
			if (ates.size() == 0) {
				continue;
			}

			if (usePOInfo
					&& pi.getAttributes()
							.containsKey(ProcessInstance.ATT_PI_PO)
					&& pi.getAttributes().get(ProcessInstance.ATT_PI_PO)
							.equals("true")) {
				// We are dealing with a partial order and we should use that
				// information

				HashMap<String, Integer> ateIDMap = new HashMap(ates.size());
				HashMap<String, Integer> iD2Ate = new HashMap(ates.size());
				Iterator<AuditTrailEntry> ateIt = ates.iterator();
				int index = 0;
				while (ateIt.hasNext()) {
					AuditTrailEntry ate = ateIt.next();
					ateIDMap.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), new Integer(
							modelElements.findLogEventNumber(ate.getElement(),
									ate.getType())));
					iD2Ate.put(ate.getAttributes().get(
							ProcessInstance.ATT_ATE_ID), new Integer(index));
					index++;
				}

				ateIt = ates.iterator();
				while (ateIt.hasNext()) {
					// Walk trough all audittrailentries
					AuditTrailEntry ate = ateIt.next();
					int ateIndex = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());
					findCloseIn(ates, ate, ateIndex, distance, ateIDMap,
							iD2Ate, D, numSimilarPIs, new ArrayList(distance));
				}

			} else {
				// We should consider the linear order on events
				Iterator<AuditTrailEntry> ateIt = ates.iterator();
				while (ateIt.hasNext()) {
					// Walk trough all audittrailentries
					AuditTrailEntry ate = ateIt.next();

					if (i < distance) {
						memory[i] = modelElements.findLogEventNumber(ate
								.getElement(), ate.getType());
						i++;
						continue;
					}

					int index = modelElements.findLogEventNumber(ate
							.getElement(), ate.getType());

					// Now, start adding values to the distance matrix
					if (memory[0] == index) {
						// the first is memory equals this one.
						for (int j = 0; j < distance; j++) {
							D.set(index, memory[j], D.get(index, memory[j])
									+ numSimilarPIs);
						}
					}
					for (int j = 0; j < distance - 1; j++) {
						memory[j] = memory[j + 1];
					}
					memory[distance - 1] = index;
				}
			}
		}
		return D;
	}

	public LogEvents getLogEvents() {
		return modelElements;
	}
}
