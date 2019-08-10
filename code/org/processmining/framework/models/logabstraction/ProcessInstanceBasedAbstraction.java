package org.processmining.framework.models.logabstraction;

import java.util.*;

import org.processmining.analysis.log.scale.*;
import org.processmining.framework.log.*;
import org.processmining.framework.models.recommendation.*;
import org.processmining.framework.ui.Message;

/**
 * <p>
 * Title:
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
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public abstract class ProcessInstanceBasedAbstraction extends LogAbstraction {

	private ProcessInstanceScale scale = null;

	public ProcessInstanceBasedAbstraction(ProcessInstanceScale scale,
			LogReader log) {
		super(log);
		this.scale = scale;
	}

	public abstract SortedSet<LogEvent> getPossibleNextLogEvents(
			LogAbstraction partialTrace);

	public abstract double calculateFitness(LogAbstraction partialTrace);

	/**
	 * The set of process instances clustered by this contributor, implemented
	 * as a list of process instances for performance (set semantics, if
	 * required, are supposed to be retained by the specific implementation,
	 * i.e. making sure no two equal process instances are contained in this
	 * list).
	 */
	protected ArrayList<ProcessInstance> instances = new ArrayList<ProcessInstance>(
			1);

	/**
	 * @return the set of process instances clustered by this contributor
	 *         instance.
	 */
	public ArrayList<ProcessInstance> getInstances() {
		return instances;
	}

	private double[] getAllValuesAndWeights(LogAbstraction partialTrace) {
		return getAllValuesAndWeights(calculateFitness(partialTrace));
	}

	private double[] getAllValuesAndWeights(double partialTraceWeight) {
		double weightedSum = 0;
		double weightedSquareSum = 0;
		double weight = partialTraceWeight;
		double instSize = (double) instances.size();
		for (ProcessInstance pi : instances) {
			double d = scale.weigh(pi);
			weightedSum += d;
			weightedSquareSum += d * d;
		}
		return new double[] { weightedSum / instSize,
				weightedSquareSum / instSize, weight };
	}

	/**
	 * Adds a process instance to the set represented by this contributor
	 * instance.
	 * 
	 * @param instance
	 *            The new process instance to be added.
	 */
	public void addInstance(ProcessInstance pi) {
		if (!instances.contains(pi)) {
			instances.add(pi);
		}

	}

	// The implementing class can assume that the provided LogAbstraction
	// partialTrace is
	// of the same class, i.e. this.getClass().equals(partialTrace.getClass()) =
	// true
	//
	// The implementing class can assume that the LogEvent provided is in the
	// set returned by
	// getPossibleNextLogEvents(partialTrace), i.e. in all instances
	// represented by this abstraction, the given LogEvent is a possible
	// next step.
	public final Recommendation getRecommendation(LogEvent logEventToDo,
			LogAbstraction partialTrace, SortedSet<LogEvent> enabledEvents) {
		Recommendation rec = new Recommendation();
		rec.setTask(logEventToDo.getModelElementName());
		rec.setEventType(logEventToDo.getEventType());
		double weight = calculateFitness(partialTrace);

		if (weight > 0) {

			SortedSet<LogEvent> possibleEvents = getPossibleNextLogEvents(partialTrace);

			if (possibleEvents.contains(logEventToDo)
					&& enabledEvents.contains(logEventToDo)) {

				// It is possible to do this event, so give a recommendation
				double[] vals = getAllValuesAndWeights(weight);
				rec.setDoExpectedValue(vals[0]);
				rec.setDoExpectedSquaredValue(vals[1]);
				rec.setDoWeight(vals[2]);
			} else {
				// It is not possible to do this event, so give a "negative"
				// recommendation
				double w = 0;
				double e = 0;
				double e2 = 0;
				for (LogEvent evt : possibleEvents) {
					if (!enabledEvents.contains(evt)) {
						continue;
					}
					// Event evt is not a possible event in this case,
					// but is enabled in the query
					double[] vals = getAllValuesAndWeights(weight);
					double newW = w + vals[2];
					e = (e * w + vals[0] * vals[2]) / newW;
					e2 = (e2 * w + vals[1] * vals[2]) / newW;
					w = newW;
				}
				rec.setDontExpectedValue(e);
				rec.setDontExpectedSquaredValue(e2);
				rec.setDontWeight(w);
			}
		}
		return rec;

	}
}
