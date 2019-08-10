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

import java.util.ArrayList;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogEvents;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.filter.DefaultLogFilter;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author not attributable
 * @version 1.0
 */

public class TimeIntervalLogRelationBuilder implements LogRelationBuilder {
	private LogRelations relations;

	private DoubleMatrix2D causalRelationsMatrix;
	private DoubleMatrix2D parallelRelationsMatrix;
	private int nme;

	private LogReader log;
	private DefaultLogFilter filter;

	private LogEvents modelElements;

	public TimeIntervalLogRelationBuilder(LogRelations relations,
			LogReader log, String startEvent, String finalEvent) {
		LogFilter oldFilter = (LogFilter) log.getLogFilter();

		this.log = log;
		this.relations = relations;

		filter = new DefaultLogFilter(DefaultLogFilter.DISCARD);
		filter.setLowLevelFilter(oldFilter);
		filter.filterEventType(startEvent, DefaultLogFilter.INCLUDE);
		filter.filterEventType(finalEvent, DefaultLogFilter.INCLUDE);
		log.reset();

		// Now, get the causal matrix and the parallelmatrix from relations
		DoubleMatrix2D causalRelations = relations.getCausalFollowerMatrix()
				.copy();
		DoubleMatrix2D parallelRelations = relations.getParallelMatrix().copy();
		causalRelationsMatrix = causalRelations.copy();
		parallelRelationsMatrix = parallelRelations.copy();

		modelElements = relations.getLogEvents();
		nme = modelElements.size();

		findParallellism(startEvent, finalEvent, causalRelations,
				parallelRelations);
		insertNewCausalities(startEvent, finalEvent, causalRelations,
				parallelRelations);

		this.relations = new LogRelationsImpl(causalRelationsMatrix,
				parallelRelationsMatrix, relations.getEndInfo(), relations
						.getStartInfo(), relations.getOneLengthLoopsInfo(),
				relations.getLogEvents());
	}

	private boolean findParallellism(String startEvent, String finalEvent,
			DoubleMatrix2D causalRelations, DoubleMatrix2D parallelRelations) {

		ArrayList parallels = new ArrayList();
		while (log.hasNext()) {
			ProcessInstance pi = log.next();
			AuditTrailEntries ates = pi.getAuditTrailEntries();

			ArrayList parallel = new ArrayList();
			int i = 0;
			while (ates.hasNext()) {
				AuditTrailEntry ate = ates.next();
				if (ate.getType().equals(startEvent)) {
					// it is the start of an interval
					// Check if for this ate there is another
					int j = -1;
					AuditTrailEntries ates2 = pi.getAuditTrailEntries();
					boolean b = true;
					while (ates2.hasNext()) {
						AuditTrailEntry ate2 = ates2.next();
						j++;
						if (j <= i) {
							continue;
						}
						if (!ate.getElement().equals(ate2.getElement())) {
							continue;
						}
						b = ate.getType().equals(ate2.getType());
						break;
					}
					// if b=true, then there are two overlapping intervals of
					// the same
					// ModelElement (ate's with the same getElement). The
					// intervals are now
					// undistinguishable, so skip.
					// In other words, if !b then this start event has an end
					// event within
					// the same process instance.
					if (!b) {
						parallel.add(ate);
					}
				} else {
					// it is the end of an interval
					// Check if this end has a start in parallel:
					int j = 0;
					AuditTrailEntry ate2 = null;
					while (j < parallel.size()) {
						if (((AuditTrailEntry) parallel.get(j)).getElement()
								.equals(ate.getElement())) {
							ate2 = (AuditTrailEntry) parallel.get(j);
						}
						j++;
					}
					if (ate2 != null) {
						if (parallel.size() > 1) {
							parallels.add(parallel.clone());
						}
						parallel.remove(ate2);
					}
				}
				i++;
			}
		}

		if (parallels.size() == 0) {
			return false;
		}
		setParallellism(parallels, causalRelations, parallelRelations);
		// TODO, re-attach disconnected parts.

		return true;

	}

	private void setParallellism(ArrayList parallels,
			DoubleMatrix2D causalRelations, DoubleMatrix2D parallelRelations) {

		// Now parallels contains a list of lists. Each individual list contains
		// a number of AuditTrailEntry's.
		// Now, for the original ModelElements, set parallels for all event
		// types of
		// the ate's per list.
		for (int i = 0; i < parallels.size(); i++) {
			ArrayList parallel = (ArrayList) parallels.get(i);
			for (int j = 0; j < parallel.size() - 1; j++) {
				AuditTrailEntry ate_j = (AuditTrailEntry) parallel.get(j);
				for (int k = j + 1; k < parallel.size(); k++) {
					AuditTrailEntry ate_k = (AuditTrailEntry) parallel.get(k);

					// Now set parallels
					for (int l = 0; l < modelElements.size(); l++) {
						for (int m = 0; m < modelElements.size(); m++) {
							if (modelElements.getEvent(l).getModelElementName()
									.equals(ate_j.getElement())
									&& modelElements.getEvent(m)
											.getModelElementName().equals(
													ate_k.getElement())) {

								// Now set a parallel between modelElements[l]
								// and modelElements[m]
								causalRelations.set(l, m, 0);
								causalRelations.set(m, l, 0);
								parallelRelations.set(l, m, 1);
								parallelRelations.set(m, l, 1);
							}
						}
					}
				}
			}
		}

	}

	private void insertNewCausalities(String startEvent, String finalEvent,
			DoubleMatrix2D causalRelations, DoubleMatrix2D parallelRelations) {
		// causalRelations and parallelRelations give the new situation
		// the old situation is still stores in parallelRelationsMatrix and
		// causalRelationsMatrix
		//
		// if (i,j) were not in parallel in the old situations and are in the
		// new situation
		// and i->j in the old situation then :
		// All causal predecessors k of i that in the new situation are not in
		// parallel with j will
		// become causal predecessors of j.
		// All causal successors k of i that in the new situation are not in
		// parallel with j will
		// become causal successors of j.
		DoubleMatrix2D C = causalRelations.copy();
		for (int i = 0; i < nme; i++) {
			for (int j = 0; j < nme; j++) {
				if (!(modelElements.getEvent(i).getEventType()
						.equals(modelElements.getEvent(j).getEventType()))) {
					continue;
				}
				if ((parallelRelationsMatrix.get(i, j) == 0)
						&& (parallelRelations.get(i, j) == 1)
						&& (causalRelationsMatrix.get(i, j) == 1)) {
					for (int k = 0; k < nme; k++) {
						if ((causalRelations.get(k, i) == 1)
								&& (parallelRelations.get(k, j) == 0)
								&& modelElements.getEvent(i).getEventType()
										.equals(startEvent)) {
							C.set(k, j, 1);
						}
						if ((causalRelations.get(j, k) == 1)
								&& (parallelRelations.get(k, i) == 0)
								&& modelElements.getEvent(i).getEventType()
										.equals(finalEvent)) {
							C.set(i, k, 1);
						}
					}
				}
			}
		}
		causalRelationsMatrix = C;
		parallelRelationsMatrix = parallelRelations;
	}

	public LogRelations getLogRelations() {
		return relations;
	}
}
