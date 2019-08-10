package org.processmining.mining.prediction;

import java.util.*;

import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.WaitDialog;
import org.processmining.framework.ui.Progress;

/**
 * @author Ronald Crooy
 * 
 */
public class Predictor {
	private PredictionMinerSettingsBasedOnLogSummary settings;
	private LogVariableExtracter log;

	public Predictor() {
		this.log = log.getInstance();
		this.settings = PredictionMinerSettingsBasedOnLogSummary.getInstance();
	}

	public CrossValidationResult testCompleteLog() {
		ArrayList<RConnector> freeRconnections = new ArrayList<RConnector>();
		freeRconnections.addAll(settings.Rconnections.values());
		NonParametricRegression R = new NonParametricRegression(
				freeRconnections.get(0));
		Message.add("Running Regression on " + freeRconnections.get(0).name);
		CrossValidationResult result = new CrossValidationResult(1, 1);
		getAllVariablesIntoR(R);

		R.run();
		Boolean busy = true;
		while (busy) {
			try {
				R.join(100);
			} catch (InterruptedException e) {
			}
			// if thread is done
			if (!R.isAlive()) {
				result.setSingleResult(R.sresult, 0, 0);
				busy = false;
			}
		}
		return result;
	}

	/**
	 * takes the LogVariableExtractor and the settings and creates a list of
	 * work while there is work the available R connections will be given work.
	 * 
	 * @return
	 */
	public CrossValidationResult testCrossValidatedLog()
			throws CancelledException {
		int N = log.log.getLogSummary().getNumberOfProcessInstances();
		int K = settings.cvsize;// k-fold cross validation
		int R = settings.repeatOpt;
		CrossValidationResult result = new CrossValidationResult(K, R);

		Progress pbar = new Progress(K + "-fold cross validation with " + R
				+ " repititions in progress", 0, (R * K) + 1);
		Message
				.add("starting "
						+ K
						+ "-fold cross validation with "
						+ settings.repeatOpt
						+ " repititions each, this might take a while.....Hint: go get some coffee");
		pbar.inc();// to make it appear
		int k = 0;
		ArrayList<Integer> workK = new ArrayList<Integer>();
		ArrayList<Integer> workR = new ArrayList<Integer>();
		while (k < K) {
			// define the dataset for this run
			int r = 0;
			while (r < R) {
				workK.add(k);
				workR.add(r);
				r++;
			}
			k++;
		}
		ArrayList<RConnector> freeRconnections = new ArrayList<RConnector>();
		ArrayList<NonParametricRegression> workers = new ArrayList<NonParametricRegression>();
		Boolean busy = true;
		int busyServers = 0;
		freeRconnections.addAll(settings.Rconnections.values());
		while ((!workK.isEmpty() || busy) && !pbar.isCanceled()) {
			while (!freeRconnections.isEmpty() && !workK.isEmpty()
					&& !pbar.isCanceled()) {

				// initialize a new thread with work
				System.out.println("starting import of variables to ("
						+ workK.get(0) + "," + workR.get(0) + ") at "
						+ freeRconnections.get(0).name);
				Message
						.add("starting work(" + workK.get(0) + ","
								+ workR.get(0) + ") at "
								+ freeRconnections.get(0).name);

				// create a new regression thread and give it work
				NonParametricRegression worker = new NonParametricRegression(
						freeRconnections.get(0));
				this.getCrossValidationVariablesIntoR(worker, K, workK.get(0),
						log.log.numberOfInstances(), workR.get(0));
				System.out.println("imported work(" + workK.get(0) + ","
						+ workR.get(0) + ") at " + freeRconnections.get(0).name
						+ ". starting thread");

				workK.remove(0);
				workR.remove(0);
				freeRconnections.remove(0);
				worker.start();// start the new thread
				workers.add(worker);
				busyServers++;
			}
			int i = 0;
			while (i < workers.size()) {
				if (pbar.isCanceled()) {
					i = workers.size();
				} else {

					NonParametricRegression worker = workers.get(i);
					// wait a very short while for this thread to end
					try {
						worker.join(100);
						// System.out.print(".");
					} catch (InterruptedException e) {
					}
					// if thread is done
					if (!worker.busy) {
						System.out.println("work(" + worker.sresult.k + ","
								+ worker.sresult.r + ") done from "
								+ worker.rconnection.name);
						Message.add("work(" + worker.sresult.k + ","
								+ worker.sresult.r + ") done from "
								+ worker.rconnection.name);
						pbar.inc();

						result.setSingleResult(worker.sresult,
								worker.sresult.k, worker.sresult.r);
						freeRconnections.add(worker.rconnection);
						workers.remove(i);
						worker = null;
						i = 0;// reset loop
						busyServers--;
					}
					i++;
				}
			}
			if (workK.isEmpty() && busyServers < 1) {
				busy = false;
			}
		}
		if (pbar.isCanceled()) {
			throw new CancelledException();
		}
		return result;
	}

	private void getAllVariablesIntoR(NonParametricRegression R) {
		int N = log.log.getLogSummary().getNumberOfProcessInstances();
		getCrossValidationVariablesIntoR(R, 1, 1, N, 1);

		// however now some things are empty... and must be filled
		ArrayList<Double> sofar = new ArrayList<Double>();
		for (int piid = 0; piid < N; piid++) {
			for (int prefixid : log.extractedVariables.piids2prefix.get(piid)) {
				// if (log.cases.prefixedRemainingCycleTimes[prefixid]!=0){
				R.sresult.pids
						.add(log.extractedVariables.prefixedPiids[prefixid]);
				R.sresult.evnums
						.add(log.extractedVariables.prefixedAtids[prefixid]);
				sofar.add(log.extractedVariables.prefixedTimePassed[prefixid]);

				// }
			}
		}
		R.importNewVariable(sofar.toArray(new Double[sofar.size()]),
				"timesofar", "", null);
	}

	/**
	 * get varialbes from the log into R
	 * 
	 * @param R
	 * @param K
	 * @param k
	 */
	private void getCrossValidationVariablesIntoR(NonParametricRegression R,
			int K, int k, int N, int r) {
		int Vs = k * (N / K);
		int Ve = (k + 1) * (N / K);
		if (k + 1 >= K) {
			Ve = N;
		}
		;
		ArrayList<Integer> B = null;
		if (settings.bychance) {
			B = generateRandomIndices(settings.chance, Vs, Ve, N);
		} else {
			B = generateRandomIndices(settings.bandSize, Vs, Ve, N);
		}
		// start with duration variables
		if (settings.useDurations) {
			for (int d = 0; d < log.extractedVariables.durationNames.size(); d++) {
				int piid = 0;
				ArrayList<Double> trainValues = new ArrayList<Double>();
				ArrayList<Double> valValues = new ArrayList<Double>();
				ArrayList<Double> bandValues = new ArrayList<Double>();

				while (piid < Vs) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedDurations[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedDurations[prefixid][d]);
						}
					}
					piid++;
				}

				while (Vs <= piid && piid < Ve) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
							valValues
									.add(log.extractedVariables.prefixedDurations[prefixid][d]);
						}
					}
					piid++;
				}
				while (Ve <= piid && piid < N) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedDurations[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedDurations[prefixid][d]);
						}
					}
					piid++;
				}
				R.importNewVariable(trainValues.toArray(new Double[trainValues
						.size()]), log.extractedVariables.durationNames.get(d),
						"_duration_train", R.trainVarNames);
				R.importNewVariable(bandValues.toArray(new Double[bandValues
						.size()]), log.extractedVariables.durationNames.get(d),
						"_duration_band", R.bandVarNames);
				R.importNewVariable(valValues.toArray(new Double[valValues
						.size()]), log.extractedVariables.durationNames.get(d),
						"_duration_val", R.valVarNames);
			}
		}
		if (settings.useOccurrences) {
			for (int d = 0; d < log.extractedVariables.occurrenceNames.size(); d++) {

				int piid = 0;
				ArrayList<Integer> trainValues = new ArrayList<Integer>();
				ArrayList<Integer> valValues = new ArrayList<Integer>();
				ArrayList<Integer> bandValues = new ArrayList<Integer>();

				while (piid < Vs) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedOccurrences[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedOccurrences[prefixid][d]);
						}
					}
					piid++;
				}

				while (Vs <= piid && piid < Ve) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
							valValues
									.add(log.extractedVariables.prefixedOccurrences[prefixid][d]);
						}
					}
					piid++;
				}
				while (Ve <= piid && piid < N) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedOccurrences[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedOccurrences[prefixid][d]);
						}
					}
					piid++;
				}
				R.importNewVariable(trainValues.toArray(new Integer[trainValues
						.size()]), log.extractedVariables.occurrenceNames
						.get(d), "_occurrence_train", R.trainVarNames);
				R.importNewVariable(bandValues.toArray(new Integer[bandValues
						.size()]), log.extractedVariables.occurrenceNames
						.get(d), "_occurrence_band", R.bandVarNames);
				R.importNewVariable(valValues.toArray(new Integer[valValues
						.size()]), log.extractedVariables.occurrenceNames
						.get(d), "_occurrence_val", R.valVarNames);
			}
		}

		if (settings.useAttributes) {
			for (int d = 0; d < log.extractedVariables.atrributeNames.size(); d++) {
				int piid = 0;
				ArrayList<String> trainValues = new ArrayList<String>();
				ArrayList<String> valValues = new ArrayList<String>();
				ArrayList<String> bandValues = new ArrayList<String>();

				while (piid < Vs) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedAttributes[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedAttributes[prefixid][d]);
						}
					}
					piid++;
				}

				while (Vs <= piid && piid < Ve) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
							valValues
									.add(log.extractedVariables.prefixedAttributes[prefixid][d]);
						}
					}
					piid++;
				}
				while (Ve <= piid && piid < N) {
					for (int prefixid : log.extractedVariables.piids2prefix
							.get(piid)) {
						trainValues
								.add(log.extractedVariables.prefixedAttributes[prefixid][d]);
						if (B.contains(piid)) {
							bandValues
									.add(log.extractedVariables.prefixedAttributes[prefixid][d]);
						}
					}
					piid++;
				}
				R.importNewVariable(trainValues.toArray(new String[trainValues
						.size()]),
						log.extractedVariables.atrributeNames.get(d),
						"_attribute_train", R.trainVarNames);
				R.importNewVariable(bandValues.toArray(new String[bandValues
						.size()]),
						log.extractedVariables.atrributeNames.get(d),
						"_attribute_band", R.bandVarNames);
				R.importNewVariable(valValues.toArray(new String[valValues
						.size()]),
						log.extractedVariables.atrributeNames.get(d),
						"_attribute_val", R.valVarNames);
			}
		}

		/* finally import Y */
		if (settings.target < 2) {
			int piid = 0;
			ArrayList<Double> trainValues = new ArrayList<Double>();
			ArrayList<Double> valValues = new ArrayList<Double>();
			ArrayList<Double> bandValues = new ArrayList<Double>();

			ArrayList<Double> trainSoFars = new ArrayList<Double>();
			ArrayList<Double> valSoFars = new ArrayList<Double>();
			ArrayList<Double> bandSoFars = new ArrayList<Double>();

			while (piid < Vs) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					trainValues
							.add(log.extractedVariables.prefixedRemainingCycleTimes[prefixid]);
					trainSoFars
							.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					if (B.contains(piid)) {
						bandValues
								.add(log.extractedVariables.prefixedRemainingCycleTimes[prefixid]);
					}
					if (B.contains(piid)) {
						bandSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			while (Vs <= piid && piid < Ve) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
						valValues
								.add(log.extractedVariables.prefixedRemainingCycleTimes[prefixid]);
						valSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			while (Ve <= piid && piid < N) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					trainValues
							.add(log.extractedVariables.prefixedRemainingCycleTimes[prefixid]);
					trainSoFars
							.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					if (B.contains(piid)) {
						bandValues
								.add(log.extractedVariables.prefixedRemainingCycleTimes[prefixid]);
					}
					if (B.contains(piid)) {
						bandSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			R.importNewVariable(trainValues.toArray(new Double[trainValues
					.size()]), "yTrain", "", null);
			R.importNewVariable(bandValues
					.toArray(new Double[bandValues.size()]), "yBand", "", null);
			R.importNewVariable(
					valValues.toArray(new Double[valValues.size()]), "yVal",
					"", null);

			// if (settings.useDurations){
			// R.importNewVariable(trainSoFars.toArray(new
			// Double[trainSoFars.size()]), "sofarTrain","", R.trainVarNames);
			// R.importNewVariable(valSoFars.toArray(new
			// Double[valSoFars.size()]), "sofarVal","", R.valVarNames);
			// R.importNewVariable(bandSoFars.toArray(new
			// Double[bandSoFars.size()]), "sofarBand","", R.bandVarNames);
			// }
		} else {
			int piid = 0;
			ArrayList<Integer> trainValues = new ArrayList<Integer>();
			ArrayList<Integer> valValues = new ArrayList<Integer>();
			ArrayList<Integer> bandValues = new ArrayList<Integer>();

			ArrayList<Double> trainSoFars = new ArrayList<Double>();
			ArrayList<Double> valSoFars = new ArrayList<Double>();
			ArrayList<Double> bandSoFars = new ArrayList<Double>();

			while (piid < Vs) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					trainValues
							.add(log.extractedVariables.prefixedTargetElement[prefixid]);
					trainSoFars
							.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					if (B.contains(piid)) {
						bandValues
								.add(log.extractedVariables.prefixedTargetElement[prefixid]);
					}
					if (B.contains(piid)) {
						bandSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			while (Vs <= piid && piid < Ve) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
						valValues
								.add(log.extractedVariables.prefixedTargetElement[prefixid]);
						valSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			while (Ve <= piid && piid < N) {
				for (int prefixid : log.extractedVariables.piids2prefix
						.get(piid)) {
					trainValues
							.add(log.extractedVariables.prefixedTargetElement[prefixid]);
					trainSoFars
							.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					if (B.contains(piid)) {
						bandValues
								.add(log.extractedVariables.prefixedTargetElement[prefixid]);
					}
					if (B.contains(piid)) {
						bandSoFars
								.add(log.extractedVariables.prefixedTimePassed[prefixid]);
					}
				}
				piid++;
			}
			R.importNewVariable(trainValues.toArray(new Integer[trainValues
					.size()]), "yTrain", "", null);
			R.importNewVariable(bandValues.toArray(new Integer[bandValues
					.size()]), "yBand", "", null);
			R.importNewVariable(valValues
					.toArray(new Integer[valValues.size()]), "yVal", "", null);
		}

		/* copy pids and atids, etc into the result */
		double[] totaldurations = new double[N - (Ve - Vs)];
		int j = 0;
		for (int piid = 0; piid < N; piid++) {
			if (!(Vs <= piid && piid < Ve)) {
				totaldurations[j] = log.extractedVariables.completeTotalCycleTimes[piid];
				j++;
			}
		}
		R.importNewVariable(totaldurations, "durations", "", null);
		ArrayList<Double> sofar = new ArrayList<Double>();
		for (int piid = Vs; piid < Ve; piid++) {
			for (int prefixid : log.extractedVariables.piids2prefix.get(piid)) {
				if (log.extractedVariables.prefixedRemainingCycleTimes[prefixid] != 0) {
					R.sresult.pids
							.add(log.extractedVariables.prefixedPiids[prefixid]);
					R.sresult.evnums
							.add(log.extractedVariables.prefixedAtids[prefixid]);
					sofar
							.add(log.extractedVariables.prefixedTimePassed[prefixid]);
				}
			}
		}
		R.importNewVariable(sofar.toArray(new Double[sofar.size()]),
				"timesofar", "", null);
		R.sresult.k = k;
		R.sresult.r = r;

	}

	/**
	 * generates 'number' random indices from 0 to N=casesize but none in the
	 * range [start..end]
	 * 
	 * @param number
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<Integer> generateRandomIndices(int number, int start,
			int end, int N) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < number; i++) {
			Integer index = ((Long) Math.round(Math.random() * (N - 1)))
					.intValue();
			while (indices.contains(index) || (start < index && index < end)) {
				index = ((Long) Math.round(Math.random() * (N - 1))).intValue();
			}
			indices.add(index);
		}
		return indices;
	}

	/**
	 * generates random indices from 0 to N=casesize but none in the range
	 * [start..end], in which the indices are randomly chosen with by 'chance'
	 * 
	 * @param chance
	 * @param start
	 * @param end
	 * @return
	 */
	public ArrayList<Integer> generateRandomIndices(double chance, int start,
			int end, int N) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		while (indices.size() < 1) {// ensure at least 1 item
			for (int i = 0; i < start; i++) {
				double guess = Math.random();
				if (guess <= chance) {
					indices.add(i);
				}
			}
			for (int i = end + 1; i < N; i++) {
				double guess = Math.random();
				if (guess <= chance) {
					indices.add(i);
				}
			}
		}
		return indices;
	}

	private String cleanString(String s) {
		return s.trim().replaceAll("[^a-zA-Z0-9_]", "");
	}
}

class CancelledException extends Exception {
	/**
	 * @author Ronald Crooy
	 * 
	 */
}