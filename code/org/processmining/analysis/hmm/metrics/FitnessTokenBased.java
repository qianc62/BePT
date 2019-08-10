package org.processmining.analysis.hmm.metrics;

import org.processmining.analysis.benchmark.metric.TokenFitnessMetric;
import org.processmining.analysis.hmm.HmmAnalyzer;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.models.petrinet.PetriNet;
import org.processmining.framework.ui.Progress;

/**
 * Conformance Checker Token fitness metric for HMM experiment.
 * 
 * @author Anne Rozinat (a.rozinat at tue.nl)
 */
public class FitnessTokenBased extends HmmExpMetric {

	public FitnessTokenBased(String aFolder) {
		super(aFolder, "Token Based", MetricType.Fitness, ExpType.Noise);
	}

	public double calculateValue(HmmAnalyzer analyzer, PetriNet pnet,
			LogReader log) {
		TokenFitnessMetric tokenFitnessMetric = new TokenFitnessMetric();
		return tokenFitnessMetric.measure(pnet, log, null, new Progress(""));
	}

}
