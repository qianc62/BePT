package org.processmining.mining.prediction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

/**
 * @author Ronald Crooy this class is used to predict remaining cycle time for a
 *         single case, based on a log with bandwidth variables.
 */
public class PredictionRecommendationAdapter {

	/**
	 * log must contain correct bandwidth variables, partial case and log must
	 * match activities.
	 * 
	 * @param log
	 * @param partialcase
	 * @return
	 */
	public double predictTotalRemainingcycleTime(LogReader log,
			ProcessInstance partialcase) {
		// make default settings for single predictions
		PredictionMinerSettingsBasedOnLogSummary settings = new PredictionMinerSettingsBasedOnLogSummary(
				log.getLogSummary());

		// extract variables
		LogVariableExtracter logextracter = new LogVariableExtracter(log);// first
		// creation
		// of
		// the
		// static
		// logexracter
		CaseSet extlog = logextracter.extractedVariables;

		// check log for bandwidths & write the corresponding variables to R
		RConnector rconnection = new RConnector();
		rconnection.testLocalR();
		NonParametricRegression R = new NonParametricRegression(rconnection);
		for (String bandVar : log.getProcess(0).getAttributes().keySet()) {
			for (int i = 0; i < extlog.durationNames.size(); i++) {
				if (bandVar.equals(extlog.durationNames.get(i) + "_duration")) {
					double[] values = new double[extlog.prefixedDurations.length];
					for (int j = 0; j < values.length; j++) {
						values[j] = extlog.prefixedDurations[j][i];
					}
					R.importNewVariable(values, extlog.durationNames.get(i),
							"_duration", R.trainVarNames);
				}
			}
			for (int i = 0; i < extlog.atrributeNames.size(); i++) {
				if (bandVar.equals(extlog.atrributeNames.get(i) + "_attribute")) {
					String[] values = new String[extlog.prefixedAttributes.length];
					for (int j = 0; j < values.length; j++) {
						values[j] = extlog.prefixedAttributes[j][i];
					}
					R.importNewVariable(values, extlog.atrributeNames.get(i),
							"_attribute", R.trainVarNames);
				}
			}
			for (int i = 0; i < extlog.occurrenceNames.size(); i++) {
				if (bandVar.equals(extlog.occurrenceNames.get(i)
						+ "_occurrence")) {
					int[] values = new int[extlog.prefixedOccurrences.length];
					for (int j = 0; j < values.length; j++) {
						values[j] = extlog.prefixedOccurrences[j][i];
					}
					R.importNewVariable(values, extlog.occurrenceNames.get(i),
							"_occurrence", R.trainVarNames);
				}
			}
		}
		R.importNewVariable(extlog.prefixedTotalCycleTimes, "yTrain", "", null);
		R.setDataFrames(R.trainVarNames, "xTrain");

		// convert partialcase to measurement
		HashMap<String, Double> durations = new HashMap<String, Double>();
		HashMap<String, Integer> occurrences = new HashMap<String, Integer>();
		HashMap<String, String> attributes = new HashMap<String, String>();

		for (String key : partialcase.getAttributes().keySet()) {
			attributes.put(cleanString(key), partialcase.getAttributes().get(
					key));
		}
		ArrayList<Long> started = new ArrayList<Long>();
		ArrayList<String> startednames = new ArrayList<String>();
		for (AuditTrailEntry ate : partialcase.getListOfATEs()) {
			String elname = cleanString(ate.getElement());

			if (settings.startEvents.contains(ate.getType())) {
				startednames.add(elname);
				started.add(ate.getTimestamp().getTime());
			}

			if (settings.completeEvents.contains(ate.getType())) {
				// complete found => check duration
				if (startednames.contains(elname)) {
					int index = startednames.indexOf(elname);
					double duration = ((Long) Math.max(0, ate.getTimestamp()
							.getTime()
							- started.get(index))).doubleValue();
					if (durations.containsKey(elname)) {
						durations.put(elname, duration + durations.get(elname));
					} else {
						durations.put(elname, duration);
					}
					started.remove(index);
					startednames.remove(index);
				}

				// complete found => store occurrence
				int count;
				if (occurrences.containsKey(elname)) {
					count = occurrences.get(elname) + 1;
				} else {
					count = 1;
				}
				occurrences.put(elname, count);
			}
		}

		// import correct variables of partialcase to R (R.trainVars)
		for (String bandVar : log.getProcess(0).getAttributes().keySet()) {
			boolean wasfound = false;
			for (String duration : durations.keySet()) {
				if (bandVar.equals(duration + "_duration")) {
					R.importNewVariable(durations.get(duration), duration,
							"_duration", R.valVarNames);
					wasfound = true;
				}
			}
			for (String attribute : attributes.keySet()) {
				if (bandVar.equals(attribute + "_attribute")) {
					R.importNewVariable(durations.get(attribute), attribute,
							"_attribute", R.valVarNames);
					wasfound = true;
				}
			}
			for (String occurrence : occurrences.keySet()) {
				if (bandVar.equals(occurrence + "_occurrence")) {
					R.importNewVariable(durations.get(occurrence), occurrence,
							"_occurrence", R.valVarNames);
					wasfound = true;
				}
			}
			if (!wasfound) {
				if (bandVar.endsWith("_duration")) {
					R.importNewVariable(0.0, bandVar, "", R.valVarNames);
				} else if (bandVar.endsWith("_attribute")) {
					R.importNewVariable(" ", bandVar, "", R.valVarNames);
				} else if (bandVar.endsWith("_occurrence")) {
					R.importNewVariable(0, bandVar, "", R.valVarNames);
				}
			}
		}
		R.setDataFrames(R.valVarNames, "xVal");
		double[] bw = new double[log.getProcess(0).getAttributes().size()];
		int i = 0;
		for (String value : log.getProcess(0).getAttributes().values()) {
			try {
				bw[i] = new Double(value).doubleValue();
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
			i++;
		}
		return R.regressOnce(bw);
	}

	private String cleanString(String s) {
		return s.trim().replaceAll("[^a-zA-Z0-9_]", "");
	}
}