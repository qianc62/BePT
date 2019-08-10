/**
 * Author Ronald Crooy
 */
package org.processmining.mining.prediction;

import java.util.*;
import org.processmining.framework.log.LogReader;
import org.processmining.mining.Miner;
import org.processmining.framework.ui.*;
import org.processmining.framework.log.rfb.BufferedLogReader;

/**
 * @author Ronald Crooy
 * 
 */
public class PredictionMiner {

	@Miner(name = "Prediction Miner", settings = PredictionMinerSettingsGUIBasedOnLogSummary.class)
	public PredictionMinerResult mine(LogReader log,
			PredictionMinerSettingsBasedOnLogSummary settings) {
		settings.uniqueInstance = settings;
		LogVariableExtracter logextracter = new LogVariableExtracter(log);// first
		// creation
		// of
		// the
		// static
		// logexracter
		Predictor predict = new Predictor();
		CrossValidationResult result = null;
		Vector<String> targetname = new Vector<String>();

		switch (settings.target) {
		case 0:
			targetname.add("Total remaining cycle time");
			break;
		case 1:
			targetname.add("Remaining cycle time until "
					+ settings.targetElement);
			break;
		case 2:
			targetname.add("Occurance of " + settings.targetElement);
			break;
		}

		try {
			if (settings.crossvalidate) {
				result = predict.testCrossValidatedLog();
			} else {
				result = predict.testCompleteLog();
			}
			int size = logextracter.log.getLogSummary()
					.getNumberOfProcessInstances();
			Message.add("cross validation complete");

			// exportablepredictions puts the results that we can add to the log
			// in a vector
			Vector<Vector> exportableresults = result.exportablepredictions();
			Integer[] pids = (Integer[]) exportableresults.get(5).toArray(
					new Integer[exportableresults.get(5).size()]);
			Integer[] evnums = (Integer[]) exportableresults.get(6).toArray(
					new Integer[exportableresults.get(6).size()]);

			BufferedLogReader exportlog = (BufferedLogReader) logextracter
					.getLogReader(intarray(pids));

			if (settings.crossvalidate) {
				// put the variables in the log for later analysis
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(1).toArray(
								new Double[pids.length]),
						"nonparametric estimator: " + targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(2).toArray(
								new Double[pids.length]), "average estimator: "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(0).toArray(
								new Double[pids.length]), "actual "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(4).toArray(
								new Double[pids.length]),
						"standard deviation of average estimator: "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(3).toArray(
								new Double[pids.length]),
						"standard deviation of nonparametric estimator: "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(8).toArray(
								new Double[pids.length]),
						"error of average estimator: " + targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(7).toArray(
								new Double[pids.length]),
						"error of nonparametric estimator: "
								+ targetname.get(0));

				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(9).toArray(
								new Double[pids.length]),
						"relative error of average estimator: "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(10).toArray(
								new Double[pids.length]),
						"relativeerror of nonparametric estimator: "
								+ targetname.get(0));

				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(12).toArray(
								new Double[pids.length]),
						"absolute error of average estimator: "
								+ targetname.get(0));
				logextracter.setLogEventDataAttributes(exportlog, pids, evnums,
						(Double[]) exportableresults.get(11).toArray(
								new Double[pids.length]),
						"absolute error of nonparametric estimator: "
								+ targetname.get(0));
				System.out.println("Exported results to log");
			} else {
				for (int i = 0; i < result.getBandwidthNames().length; i++) {
					logextracter.setLogProcessAttribute(exportlog, result
							.getBandwidthNames()[i], result
							.getBandwidthValues()[i].toString());
				}
			}

			/*
			 * create prediction result with the updated log and all tables
			 */
			PredictionMinerResult miningResult = new PredictionMinerResult(
					exportlog);
			miningResult.createTablePane(result.resultsTable(2), result
					.resultsTableHeader(), "MSE");
			// myresult.createTablePane(result.resultsTable(1),result.resultsTableHeader(),"MAPE");
			miningResult.createTablePane(result.resultsTable(0), result
					.resultsTableHeader(), "MAE");
			miningResult.createTablePane(result.estimationsTable(), result
					.estimationsTableHeader(), "estimations");
			miningResult.createTabbedTabPane(result.bandwidthsTable(), result
					.bandwidthsHeader(), "Bandwidths", "part");
			if (settings.target < 2) {
				miningResult
						.createTablePane(
								logextracter.extractedVariables.prefixedRemainingCycleTimes,
								" Prefixed " + targetname.get(0));
			} else {
				// miningResult.createTablePane(logextracter.getPrefixedTargetOccurances(0,size),targetname,"Prefixed "+targetname.get(0));
			}

			if (settings.useOccurrences) {
				miningResult.createTablePane(
						logextracter.extractedVariables.completeOccurrences,
						logextracter.extractedVariables.occurrenceNames
								.toArray(), "Occurences");
				miningResult.createTablePane(
						logextracter.extractedVariables.prefixedOccurrences,
						logextracter.extractedVariables.occurrenceNames
								.toArray(), "Prefixed occurences");
			}
			if (settings.useDurations) {
				miningResult
						.createTablePane(
								logextracter.extractedVariables.completeDurations,
								logextracter.extractedVariables.durationNames
										.toArray(), "Durations");
				miningResult
						.createTablePane(
								logextracter.extractedVariables.prefixedDurations,
								logextracter.extractedVariables.durationNames
										.toArray(), "Prefixed durations");
			}
			if (settings.useAttributes) {
				miningResult.createTablePane(
						logextracter.extractedVariables.completeAttributes,
						logextracter.extractedVariables.atrributeNames
								.toArray(), "Attributes");
				miningResult.createTablePane(
						logextracter.extractedVariables.prefixedAttributes,
						logextracter.extractedVariables.atrributeNames
								.toArray(), "Prefixed attributes");
			}
			return miningResult;
		} catch (CancelledException e) {

		}
		return null;
	}

	/**
	 * adds the array to the vector
	 */
	private Vector<Double> vectorArrayAdd(double[] a, Vector<Double> b) {
		for (int i = 0; i < a.length; i++) {
			b.add(a[i]);
		}
		return b;
	}

	/**
	 * combines to vectors
	 */
	private Vector<Vector> combineVectors(Vector<Vector> a, Vector<Vector> b) {
		Vector<Vector> result = new Vector<Vector>();
		result.addAll(a);
		result.addAll(b);
		return result;
	}

	private int[] intarray(Integer[] a) {
		int[] result = new int[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i];
		}
		return result;
	}
}
