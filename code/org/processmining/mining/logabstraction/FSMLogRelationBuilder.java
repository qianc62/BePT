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

import org.processmining.framework.log.LogEvents;

import cern.colt.matrix.DoubleMatrix2D;

/**
 * @author not attributable
 * @version 1.0
 */

public class FSMLogRelationBuilder implements LogRelationBuilder {

	private LogRelations relations;

	private DoubleMatrix2D causalRelations;
	private DoubleMatrix2D parallelRelations;
	private LogEvents modelElements;

	public FSMLogRelationBuilder(LogRelations relations) {
		this.relations = relations;

		modelElements = relations.getLogEvents();
		causalRelations = (DoubleMatrix2D) relations.getCausalFollowerMatrix()
				.clone();
		parallelRelations = (DoubleMatrix2D) relations.getParallelMatrix()
				.clone();

		// Now walk trough the log, looking for a start event.
		InferCausality();

		relations = new LogRelationsImpl(causalRelations, parallelRelations,
				relations.getEndInfo(), relations.getStartInfo(), relations
						.getOneLengthLoopsInfo(), relations.getLogEvents());
	}

	private void InferCausality() {
		// Now infer causality based on different profiles
		ArrayList exclude = new ArrayList();

		InferCausality("schedule", "assign", exclude);
		InferCausality("schedule", "manualskip", exclude);
		InferCausality("schedule", "withdraw", exclude);
		InferCausality("assign", "reassign", exclude);
		InferCausality("assign", "start", exclude);
		InferCausality("assign", "manualskip", exclude);
		InferCausality("assign", "withdraw", exclude);
		InferCausality("reassign", "start", exclude);
		InferCausality("start", "suspend", exclude);
		InferCausality("start", "complete", exclude);
		InferCausality("start", "ate_abort", exclude);
		InferCausality("suspend", "resume", exclude);
		InferCausality("suspend", "ate_abort", exclude);
		InferCausality("resume", "suspend", exclude);
		InferCausality("resume", "complete", exclude);
		InferCausality("resume", "ate_abort", exclude);

		exclude.add("assign");
		InferCausality("schedule", "reassign", exclude);
		InferCausality("schedule", "start", exclude);

		exclude.clear();
		exclude.add("resume");
		InferCausality("suspend", "complete", exclude);

		exclude.clear();
		exclude.add("start");
		InferCausality("assign", "suspend", exclude);
		InferCausality("assign", "complete", exclude);
		InferCausality("assign", "ate_abort", exclude);
		InferCausality("reassign", "suspend", exclude);
		InferCausality("reassign", "complete", exclude);
		InferCausality("reassign", "ate_abort", exclude);

		exclude.add("assign");
		InferCausality("schedule", "suspend", exclude);
		InferCausality("schedule", "complete", exclude);
		InferCausality("schedule", "ate_abort", exclude);

		exclude.clear();
		exclude.add("suspend");
		InferCausality("start", "resume", exclude);
		exclude.add("start");
		InferCausality("assign", "resume", exclude);
		InferCausality("reassign", "resume", exclude);
		exclude.add("assign");
		InferCausality("schedule", "resume", exclude);

	}

	private boolean InferCausality(String eventType1, String eventType2,
			ArrayList exclude) {
		boolean result = false;
		for (int i = 0; i < modelElements.size(); i++) {
			if (!modelElements.getEvent(i).getEventType().equals(eventType1)) {
				continue;
			}
			int j = -1;
			boolean b = false;
			while ((!b) && (j < modelElements.size() - 1)) {
				j++;
				if (!modelElements.getEvent(i).getModelElementName().equals(
						modelElements.getEvent(j).getModelElementName())) {
					continue;
				}
				b = (exclude.contains(modelElements.getEvent(j).getEventType()));
			}
			// If b then for this ModelElement, a type given in the exclude list
			// is in the modelElements
			// Thus it should be skipped.
			if (b) {
				continue;
			}
			j = -1;
			while (j < modelElements.size() - 1) {
				j++;
				if (!modelElements.getEvent(i).getModelElementName().equals(
						modelElements.getEvent(j).getModelElementName())) {
					continue;
				}
				if (!modelElements.getEvent(j).getEventType()
						.equals(eventType2)) {
					continue;
				}
				// We found a pair (i,j):
				causalRelations.set(i, j, 1);
				parallelRelations.set(i, j, 0);
				parallelRelations.set(j, i, 0);
				result = true;
			}
		}
		return result;
	}

	public LogRelations getLogRelations() {
		return relations;
	}
}
