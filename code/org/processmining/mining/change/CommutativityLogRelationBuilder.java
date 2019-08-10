/**
 * Project: ProM
 * File: CommutativityLogRelationBuilder.java
 * Author: Christian W. Guenther (christian at deckfour dot org)
 * Created: Apr 18, 2006, 1:58:44 PM
 *
 * Copyright (c) 2006, Eindhoven Technical University
 * All rights reserved.
 *
 ***********************************************************
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
package org.processmining.mining.change;

import java.io.IOException;
import java.util.ArrayList;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.Progress;
import org.processmining.framework.ui.UISettings;
import org.processmining.framework.util.TestUtils;
import org.processmining.mining.logabstraction.LogAbstraction;
import org.processmining.mining.logabstraction.LogAbstractionImpl;
import org.processmining.mining.logabstraction.LogRelationBuilder;
import org.processmining.mining.logabstraction.LogRelations;
import org.processmining.mining.logabstraction.LogRelationsImpl;

import cern.colt.matrix.DoubleFactory1D;
import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;

/**
 * This class builds log relations based on the notion of commutativity defined
 * between change operations. It is expected that audit trail entries in the log
 * under consideration adhere to the change log format, as defined. In
 * particular, the presence of the following data attributes is expected
 * (Missing attributes will cause null pointer exceptions and failure):
 * <ul>
 * <li>CHANGE.subject</li>
 * <li>CHANGE.preset</li>
 * <li>CHANGE.postset</li>
 * </ul>
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 * 
 */
public class CommutativityLogRelationBuilder implements LogRelationBuilder {

	protected LogReader log = null;
	protected Progress progress = null;

	protected boolean allowConflictingCausality = false;

	/**
	 * Creates a new instance based on a specific log(reader).
	 */
	public CommutativityLogRelationBuilder(LogReader logReader) {
		super();
		log = logReader;
	}

	public void setAllowConflictingCausality(boolean allowed) {
		allowConflictingCausality = allowed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.mining.logabstraction.LogRelationBuilder#getLogRelations
	 * ()
	 */
	public LogRelations getLogRelations() {
		// introduce data structures (overview)
		DoubleMatrix1D start = null;
		DoubleMatrix1D end = null;
		DoubleMatrix2D causal = null;
		DoubleMatrix2D parallel = null;
		DoubleMatrix1D loops = null;
		// derive basic info from logabstraction
		LogAbstraction abstraction = new LogAbstractionImpl(log);
		int s;
		DoubleMatrix2D directSuccession;
		try {
			directSuccession = abstraction.getFollowerInfo(1);
			s = directSuccession.columns();
			start = abstraction.getStartInfo();
			end = abstraction.getEndInfo();
		} catch (IOException ex) {
			Message.add("Error while reading the log: " + ex.getMessage(),
					Message.ERROR);
			return null;
		}
		// initialize remaining structures
		loops = DoubleFactory1D.sparse.make(s, 0); // ignore loops for now
		parallel = generateParallelMatrix();
		// create causality matrix
		causal = generateCausalMatrix(parallel, directSuccession);
		// rebuild start elements
		for (int i = 0; i < s; i++) {
			if (start.get(i) > 0.0) {
				for (int j = 0; j < s; j++) {
					if (causal.get(j, i) != 0.0) {
						// element i has causal predecessor(s): remove
						start.set(i, 0.0);
						break;
					}
				}
			}
		}
		// rebuild final elements
		for (int i = 0; i < s; i++) {
			if (end.get(i) > 0.0) {
				for (int j = 0; j < s; j++) {
					if (causal.get(i, j) != 0.0) {
						// element i has causal successor(s): remove
						end.set(i, 0.0);
						break;
					}
				}
			}
		}
		// output test, if necessary
		if (UISettings.getInstance().getTest() == true) {
			Message.add("<CommutativityLogRelationBuilder>", Message.TEST);
			Message.add("\t<CausalMatrix hash=\"" + TestUtils.hash(causal)
					+ "\">", Message.TEST);
			Message.add("\t<ParallelMatrix hash=\"" + TestUtils.hash(parallel)
					+ "\">", Message.TEST);
			Message.add("\t<EndMatrix hash=\"" + TestUtils.hash(end) + "\">",
					Message.TEST);
			Message.add("\t<StartMatrix hash=\"" + TestUtils.hash(start)
					+ "\">", Message.TEST);
			Message.add("\t<LoopsMatrix hash=\"" + TestUtils.hash(loops)
					+ "\">", Message.TEST);
			Message.add("</CommutativityLogRelationBuilder>", Message.TEST);
		}
		// assemble and return new log relations
		return new LogRelationsImpl(causal, parallel, end, start, loops, log
				.getLogSummary().getLogEvents());
	}

	/**
	 * Generates the parallelity matrix based on change operation ATE
	 * commutativity.
	 * 
	 * @return The generated parallellity matrix.
	 */
	protected DoubleMatrix2D generateParallelMatrix() {
		LogEvents logEvents = log.getLogSummary().getLogEvents();
		int size = logEvents.size();
		DoubleMatrix2D parallel = DoubleFactory2D.sparse.make(size, size, 0);
		// iterate through process instances to derive
		// parallel relations using commutativity.
		for (log.reset(); log.hasNext();) {
			ProcessInstance pi = log.next();
			ArrayList ates = pi.getAuditTrailEntries().toArrayList();
			for (int i = 0; i < (ates.size() - 1); i++) {
				AuditTrailEntry a = (AuditTrailEntry) ates.get(i);
				int indexA = logEvents.findLogEventNumber(a.getElement(), a
						.getType());
				for (int j = i; j < ates.size(); j++) {
					AuditTrailEntry b = (AuditTrailEntry) ates.get(j);
					int indexB = logEvents.findLogEventNumber(b.getElement(), b
							.getType());
					if (isCommutative(a, b)) {
						// only mark as parallel, if no 'veto' marker has
						// been set previously!
						if (parallel.get(indexA, indexB) >= 0.0) {
							// mark as parallel in both directions
							parallel.set(indexA, indexB, parallel.get(indexA,
									indexB) + 1.0);
							parallel.set(indexB, indexA, parallel.get(indexB,
									indexA) + 1.0);
						}
					} else {
						// set 'veto' marker: once a relation has been found to
						// be not commutative, this cannot be overwritten later
						// on!
						parallel.set(indexA, indexB, -1.0);
						parallel.set(indexB, indexA, -1.0);
					}
				}
			}
		}
		// iterate through parallelity matrix and exchange 'veto' markers by
		// zero
		for (int x = 0; x < parallel.columns(); x++) {
			for (int y = 0; y < parallel.rows(); y++) {
				if (parallel.get(x, y) < 0.0) {
					parallel.set(x, y, 0.0);
				}
			}
		}
		return parallel;
	}

	/**
	 * Generates the causal matrix with the option to include conflicting
	 * causalities in the final matrix.
	 * 
	 * @param parallel
	 * @param directSuccession
	 * @return
	 */
	protected DoubleMatrix2D generateCausalMatrix(DoubleMatrix2D parallel,
			DoubleMatrix2D directSuccession) {
		int s = log.getLogSummary().getLogEvents().size();
		DoubleMatrix2D causal = DoubleFactory2D.sparse.make(s, s, 0);
		// build causal relations
		for (int i = 0; i < s; i++) {
			for (int j = 0; j < s; j++) {
				if (i == j) {
					// no causal relations to self
					continue;
				} else if ((directSuccession.get(i, j) > 0)
						&& (parallel.get(i, j) <= 0)) {
					// record causal relation, if no conflicting present, or if
					// conflicting causal relations are allowed.
					if (this.allowConflictingCausality
							|| (directSuccession.get(j, i) <= 0)) {
						causal.set(i, j, causal.get(i, j) + 1);
					} else {
						// clear reverse entry too, to be sure
						causal.set(i, j, 0.0);
						causal.set(j, i, 0.0);
					}
				}
			}
		}
		return causal;
	}

	/**
	 * Checks for two given audit trail entries, whether their represented
	 * change operations are considered commutative. Commutativity between two
	 * change operations A and B is assumed, when:
	 * <ul>
	 * <li>The subjects of A and B are not equal, and</li>
	 * <li>the subject of A is not contained in the preset of B, and</li>
	 * <li>the subject of A is not contained in the postset of B.</li>
	 * </ul>
	 * This method will return whether all three conditions hold on the
	 * specified audit trail entries.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	protected boolean isCommutative(AuditTrailEntry a, AuditTrailEntry b) {
		String subjectA = ((String) a.getData().get("CHANGE.subject")).trim();
		String subjectB = ((String) b.getData().get("CHANGE.subject")).trim();
		if (subjectA.equals(subjectB)) {
			return false;
		} else {
			ArrayList paramB = extractParameterList(b);
			return ((paramB.contains(subjectA)) == false);
		}
	}

	/**
	 * This method returns the parameter list of a change operation. This
	 * parameter list is defined to contain all objects in the pre- and postset
	 * of a change operation. (What is actually done is, exploding two
	 * comma-separated list strings and insert the extracted elements into an
	 * ArrayList, which is returned.)
	 * 
	 * @param ate
	 * @return
	 */
	protected ArrayList extractParameterList(AuditTrailEntry ate) {
		ArrayList paramList = new ArrayList();
		String preset[] = ((String) ate.getData().get("CHANGE.preset"))
				.split(",");
		for (int i = 0; i < preset.length; i++) {
			paramList.add(preset[i].trim());
		}
		String postset[] = ((String) ate.getData().get("CHANGE.postset"))
				.split(",");
		for (int j = 0; j < postset.length; j++) {
			paramList.add(postset[j].trim());
		}
		return paramList;
	}

}
