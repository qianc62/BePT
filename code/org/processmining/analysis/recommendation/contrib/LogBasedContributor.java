/**
 *
 */
package org.processmining.analysis.recommendation.contrib;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.logabstraction.LogAbstraction;
import org.processmining.framework.models.recommendation.NotEqualRecommendationException;
import org.processmining.framework.models.recommendation.Recommendation;
import org.processmining.framework.models.recommendation.RecommendationQuery;
import org.processmining.framework.models.recommendation.RecommendationResult;
import org.processmining.framework.ui.Message;

import java.util.SortedSet;
import org.processmining.framework.log.LogEvent;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * This abstract class may be used as a starting point for contributor
 * implementations which cluster a set of process instances. It provides a
 * method for calculating the
 * 
 * @author christian
 * 
 */
public abstract class LogBasedContributor extends RecommendationContributor {

	public LogBasedContributor() {
		super();
	}

	public RecommendationResult generateFilteredRecommendations(
			RecommendationQuery query, String process) {
		ProcessInstance pi = query.toProcessInstance(process);
		// No log is present, since this is the partial trace. Note that we
		// pass "null" as the LogReader, since this instance is not going to be
		// weight
		LogAbstraction incompletePIAbstraction = logAbstractionFactory
				.getAbstraction(null, pi, scale);

		double partialTraceWeight = scale.weigh(pi);
		RecommendationResult result = new RecommendationResult(query.getId(),
				partialTraceWeight);

		SortedSet<LogEvent> enabledEvents = query.getAvailableTasks();
		List<Recommendation> recommendations = new ArrayList();
		SortedSet<LogEvent> abstractionEnabledEvents;
		// for each abstraction, get a recommendation for each of these events,
		// provided that these events are a possible continuation in that
		// abstraction.

		for (LogAbstraction abstraction : logAbstractions) {
			abstractionEnabledEvents = abstraction
					.getPossibleNextLogEvents(incompletePIAbstraction);

			// Now, do not consider this abstraction, if the intersection
			// between
			// enabledEvents and recommendationEnabledEvents is empty

			// SortedSet<LogEvent> intersection = new
			// TreeSet<LogEvent>(abstractionEnabledEvents);
			// intersection.retainAll(enabledEvents);
			// if (intersection.isEmpty()) {
			// continue;
			// }

			// There is at least one element both in the set of enabled events,
			// as well as
			// in the set of abstractionEnabledEvents
			// Message.add("Case "+abstraction);
			for (LogEvent evt : enabledEvents) {
				// The event evt is enabled
				Recommendation rec = abstraction.getRecommendation(evt,
						incompletePIAbstraction, enabledEvents);
				// Message.add("      Evt: "+evt+"  Results in: "+rec.toExtendedString());
				update(recommendations, rec, abstractionEnabledEvents);
			}
		}
		double sumW = 0;
		double sumE = 0;
		double sumE2 = 0;
		for (Recommendation r : recommendations) {
			result.add(r);
			sumW += r.getDoWeight();
			sumE += r.getDoExpectedValue();
			sumE2 += r.getDontExpectedSquaredValue();
			enabledEvents.remove(r.getLogEvent());
			// Message.add(r.toExtendedString());
		}
		// Check for the remaining events for which no recommendation was given
		/*
		 * for (LogEvent e : enabledEvents) { Recommendation r = new
		 * Recommendation(); r.setDontWeight(sumW); r.setDontExpectedValue(sumE
		 * / sumW); r.setDontExpectedSquaredValue(sumE2 / sumW);
		 * r.setTask(e.getModelElementName()); r.setEventType(e.getEventType());
		 * r.setRationale("This behaviour has not been observed in the log.");
		 * result.add(r); }
		 */

		return result;
	}

	private void update(List<Recommendation> recommendations,
			Recommendation toAdd, SortedSet<LogEvent> abstractionEnabledEvents) {
		// for one abstraction and one event, I got the do, do2 and weight.
		// now update all the don'ts that have been recorded so far
		boolean found = false;

		for (int i = 0; i < recommendations.size(); i++) {
			Recommendation rec = recommendations.get(i);
			if (rec.equals(toAdd)) {
				found = true;
				// Recommendation rec recommends to do the evtToDo
				// hence, update the "do" and "dont" values
				rec.addDoToDoRecommendation(toAdd);
				if (!abstractionEnabledEvents.contains(toAdd.getLogEvent())) {
					// It is not possible in the abstraction currently being
					// processed that
					// the logEvent proposed by toAdd is executed, hence it has
					// influence on the don't
					rec.addDontToDontRecommendation(toAdd);
				}
			}
		}
		if (!found) {
			// Now this recommendation has to be added
			// to the set.
			recommendations.add(toAdd);
		}
	}
}
