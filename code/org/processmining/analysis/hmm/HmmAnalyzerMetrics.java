package org.processmining.analysis.hmm;

import java.util.ArrayList;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.log.LogReader;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

public class HmmAnalyzerMetrics {

	public ArrayList<LogEvent> observed;
	public int[][][] eventTraceFrequencies; // actual frequencies for event
	// pairs for each trace
	public boolean[][] falseNegatives;
	public boolean[][] falsePositives;
	public int[] traceFrequencies;
	public double[] inputHmmProb;
	public double[] adjustedHmmProb;
	public Hmm<ObservationInteger> inputHmm;
	public LogReader inputLog;

	public HmmAnalyzerMetrics() {
	}

	/**
	 * Calculates the most basic fitness based on the number of traces that fit
	 * the input hmm vs. the number of traces that do not fit.
	 * <p>
	 * Calculates 1 - ((no. of non-fitting traces) / (total no. of traces))
	 * 
	 * @return trace fitness in the range of [0,1]
	 */
	public double getTraceFitness() {
		int noNonFitting = 0;
		int totalNoTraces = 0;
		for (int i = 0; i < inputHmmProb.length; i++) {
			if (inputHmmProb[i] == 0.0) {
				noNonFitting = noNonFitting + traceFrequencies[i];
			}
			totalNoTraces = totalNoTraces + traceFrequencies[i];
		}
		return ((double) totalNoTraces - noNonFitting)
				/ ((double) totalNoTraces);
	}

	/**
	 * Calculates the fitness based on the number of 'false negative' (i.e.,
	 * zero probability relations that were adjusted to non-zero probability
	 * relations based on the observed log transitions) relations.
	 * <p>
	 * Because this measure is evalutated on the model level, it ignores
	 * frequencies of occurrences of these relations.
	 * <p>
	 * Calculates 1 - ((no. false negatives) / (no. of negatives in model).
	 * 
	 * @return model based fitness in the range of [0,1]
	 */
	public double getModelLevelFitness() {
		int noFN = 0;
		int totalNo = 0;
		for (int i = 0; i < observed.size(); i++) {
			for (int j = 0; j < observed.size(); j++) {
				if (inputHmm.getAij(i, j) == 0.0) { // Negatives
					if (falseNegatives[i][j] == true) { // False Negatives
						noFN++;
					}
					totalNo++;
				}
			}
		}
		return ((double) totalNo - noFN) / ((double) totalNo);
	}

	/**
	 * Calculates the fitness based on the number of 'false negative' (i.e.,
	 * zero probability relations that were adjusted to non-zero probability
	 * relations based on the observed log transitions) relations.
	 * <p>
	 * Because this measure is evalutated on the log level, it does take
	 * frequencies of occurrences of these relations into account.
	 * <p>
	 * Calculates 1 - ((no. false negatives) / (total number of relations).
	 * 
	 * @return model based fitness in the range of [0,1]
	 */
	public double getLogLevelFitness() {
		int noFN = 0;
		int totalNo = 0;
		for (int i = 0; i < observed.size(); i++) {
			for (int j = 0; j < observed.size(); j++) {
				for (int k = 0; k < inputLog.getLogSummary()
						.getNumberOfUniqueProcessInstances(); k++) {
					if (falseNegatives[i][j] == true) { // False Negatives
						noFN = noFN
								+ (eventTraceFrequencies[k][i][j] * traceFrequencies[k]);
					}
					totalNo = totalNo + eventTraceFrequencies[k][i][j]
							* traceFrequencies[k];
				}
			}
		}
		return ((double) totalNo - noFN) / ((double) totalNo);
	}

	/**
	 * Calculates the most basic fitness based on the number of traces that fit
	 * the input hmm vs. the number of traces that do not fit.
	 * <p>
	 * Calculates 1 - ((no. false negatives) / (total number of relations).
	 */
	public double getAverageTraceFitness() {
		double tracefitnessSum = 0.0;
		double numberoftraces = 0.0;
		for (int k = 0; k < inputLog.getLogSummary()
				.getNumberOfUniqueProcessInstances(); k++) {
			int noFN = 0;
			int totalNo = 0;
			for (int i = 0; i < observed.size(); i++) {
				for (int j = 0; j < observed.size(); j++) {

					if (falseNegatives[i][j] == true) { // False Negatives
						noFN = noFN + eventTraceFrequencies[k][i][j];
					}
					totalNo = totalNo + eventTraceFrequencies[k][i][j];

				}
			}
			double tracefitness = ((double) totalNo - noFN)
					/ ((double) totalNo);
			tracefitnessSum = tracefitnessSum
					+ (tracefitness * traceFrequencies[k]);
			numberoftraces = numberoftraces + traceFrequencies[k];
		}
		return (tracefitnessSum / numberoftraces);
	}

	/**
	 * Adds up the probabilities of all non-duplicate log sequences with respect
	 * to the adjusted hmm.
	 * <p>
	 * Corresponds to a absolute log completeness measurement.
	 * 
	 * @return the total precision measurement in the range of [0,1]
	 */
	public double getAbsoluteLogCompletenessPrecision() {
		double result = 0.0;
		for (int i = 0; i < adjustedHmmProb.length; i++) {
			result = result + adjustedHmmProb[i];
		}
		return result;
	}

	/**
	 * Adds up the probabilities of all non-duplicate log sequences with respect
	 * to the input hmm.
	 * <p>
	 * Corresponds to a total precision measurement.
	 * 
	 * @return the total precision measurement in the range of [0,1]
	 */
	public double getTotalPrecision() {
		double result = 0.0;
		for (int i = 0; i < inputHmmProb.length; i++) {
			result = result + inputHmmProb[i];
		}
		return result;
	}

	/**
	 * Calculates the precision based on the number of 'false positives' (i.e.,
	 * non-zero probability relations that were adjusted to zero probability
	 * relations based on the observed log transitions) relations.
	 * <p>
	 * Because this measure is evalutated on the model level, it ignores
	 * frequencies of occurrences of these relations.
	 * <p>
	 * Calculates 1 - ((no. false positives) / (total number of relations).
	 * 
	 * @return model based precision in the range of [0,1]
	 */
	public double getModelPrecision() {
		int noFP = 0;
		int totalNo = 0;
		for (int i = 0; i < observed.size(); i++) {
			for (int j = 0; j < observed.size(); j++) {
				if (inputHmm.getAij(i, j) > 0.0) { // Positives
					if (falsePositives[i][j] == true) {
						noFP++;
					}
					totalNo++;
				}
			}
		}
		return ((double) totalNo - noFP) / ((double) totalNo);
	}

}