package org.processmining.analysis.performance.fsmevaluator;

import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.processmining.analysis.performance.fsmanalysis.FSMStatistics;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.DataSection;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.fsm.AcceptFSM;
import org.processmining.framework.models.fsm.FSMState;
import org.processmining.framework.ui.Message;
import org.processmining.mining.fsm.FsmHorizonSettings;
import org.processmining.mining.fsm.FsmMinerPayload;
import org.processmining.mining.fsm.FsmSettings;

public class FSMEvaluationStatistics extends FSMStatistics {

	protected HashSet<String> unKnownStates;
	protected HashMap<String, DescriptiveStatistics> suMSEMap;
	protected HashMap<String, DescriptiveStatistics> suMAEMap;
	protected HashMap<String, DescriptiveStatistics> suMAPEMap;
	protected HashMap<String, DescriptiveStatistics> suMSEHMap;
	protected HashMap<String, DescriptiveStatistics> suMAEHMap;
	protected HashMap<String, DescriptiveStatistics> suMAPEHMap;
	protected DescriptiveStatistics overAllMSEData;
	protected DescriptiveStatistics overAllMAEData;
	protected DescriptiveStatistics overAllMAPEData;

	protected double overAllMSEaggre;
	protected double overAllMAEaggre;
	protected double overAllMAPEaggre;

	protected FSMStatistics fsmStatistics;
	protected long timeunit;
	protected String estimator;

	public FSMEvaluationStatistics() {
		super();
		unKnownStates = new HashSet<String>();
		suMSEMap = new HashMap<String, DescriptiveStatistics>();
		suMAEMap = new HashMap<String, DescriptiveStatistics>();
		suMAPEMap = new HashMap<String, DescriptiveStatistics>();
		overAllMSEData = DescriptiveStatistics.newInstance();
		overAllMAEData = DescriptiveStatistics.newInstance();
		overAllMAPEData = DescriptiveStatistics.newInstance();
		overAllMSEaggre = 0.0;
		overAllMAEaggre = 0.0;
		overAllMAPEaggre = 0.0;

		suMSEHMap = new HashMap<String, DescriptiveStatistics>();
		suMAEHMap = new HashMap<String, DescriptiveStatistics>();
		suMAPEHMap = new HashMap<String, DescriptiveStatistics>();
	}

	public HashMap<String, DescriptiveStatistics> getMSEMap() {
		return suMSEMap;
	}

	public HashMap<String, DescriptiveStatistics> getMAEMap() {
		return suMAEMap;
	}

	public HashMap<String, DescriptiveStatistics> getMAPEMap() {
		return suMAPEMap;
	}

	public HashMap<String, DescriptiveStatistics> getMSEHMap() {
		return suMSEHMap;
	}

	public HashMap<String, DescriptiveStatistics> getMAEHMap() {
		return suMAEHMap;
	}

	public HashMap<String, DescriptiveStatistics> getMAPEHMap() {
		return suMAPEHMap;
	}

	public DescriptiveStatistics getOverallRMSE() {
		overAllMSEData = DescriptiveStatistics.newInstance();
		for (String str : suMSEMap.keySet()) {
			if (String.valueOf(suMSEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMSEData.addValue(Math.sqrt(suMSEMap.get(str).getMean()));
			Message.add("Error value = " + suMSEMap.get(str).getMean(),
					Message.NORMAL);
		}
		return overAllMSEData;
	}

	public DescriptiveStatistics getOverallMAE() {
		overAllMAEData = DescriptiveStatistics.newInstance();
		for (String str : suMAEMap.keySet()) {
			if (String.valueOf(suMAEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMAEData.addValue(suMAEMap.get(str).getMean());
			Message.add("Error value = " + suMAEMap.get(str).getMean(),
					Message.NORMAL);
		}
		return overAllMAEData;
	}

	public DescriptiveStatistics getOverallMAPE() {
		overAllMAPEData = DescriptiveStatistics.newInstance();
		for (String str : suMAPEMap.keySet()) {
			if (String.valueOf(suMAPEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMAPEData.addValue(suMAPEMap.get(str).getMean());
			Message.add("Error value = " + suMAPEMap.get(str).getMean(),
					Message.NORMAL);
		}
		return overAllMAPEData;
	}

	public double getOverallRMSEAggre() {
		overAllMSEaggre = 0.0;
		int k = 0;
		for (String str : suMSEMap.keySet()) {
			if (String.valueOf(suMSEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMSEaggre += Math.sqrt(suMSEMap.get(str).getMean())
					* suMSEMap.get(str).getN();
			k += suMSEMap.get(str).getN();
		}
		return overAllMSEaggre / k;
	}

	public double getOverallMAEAggre() {
		overAllMAEaggre = 0.0;
		int k = 0;
		for (String str : suMAEMap.keySet()) {
			if (String.valueOf(suMAEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMAEaggre += suMAEMap.get(str).getMean()
					* suMAEMap.get(str).getN();
			k += suMAEMap.get(str).getN();
		}
		return overAllMAEaggre / k;
	}

	public double getOverallMAPEAggre() {
		overAllMAPEaggre = 0.0;
		int k = 0;
		for (String str : suMAPEMap.keySet()) {
			if (String.valueOf(suMAPEMap.get(str).getMean()).equals("NaN"))
				continue;
			overAllMAPEaggre += suMAPEMap.get(str).getMean()
					* suMAPEMap.get(str).getN();
			k += suMAPEMap.get(str).getN();
		}
		return overAllMAPEaggre / k;
	}

	public int getNumber() {
		int k = 0;
		for (String str : suMAPEMap.keySet()) {
			if (String.valueOf(suMAPEMap.get(str).getMean()).equals("NaN"))
				continue;
			k += suMAPEMap.get(str).getN();
		}
		return k;
	}

	public HashSet<String> getUnknownSet() {
		return unKnownStates;
	}

	// added for evaluation!!!
	public void analysis(LogReader log, AcceptFSM fsm,
			FSMStatistics fsmStatistics, String estimator) {
		this.fsmStatistics = fsmStatistics;
		this.estimator = estimator;
		FsmSettings settings = ((FsmMinerPayload) fsm.getStartState()
				.getPayload()).getSettings();
		try {
			// Progress progress = null;
			int progressCtr = 0;
			if (settings.hasGUI()) {
				// First count how many stpes we have to do for the progress
				// bar.
				progressCtr = 0;
				for (ProcessInstance pi : log.getInstances()) {
					progressCtr += pi.getAuditTrailEntryList().size();
				}
				// Create the progress bar.
				// progress = new Progress("Constructing FSM...", 0,
				// progressCtr - 1);
			}
			// And now for the real thing.
			progressCtr = 0;
			// And now for the real thing.
			for (ProcessInstance pi : log.getInstances()) {
				long startTime = getStartTime(pi);
				if (startTime == -1)
					continue;
				long endTime = getEndTime(pi);
				if (endTime == -1)
					continue;

				AuditTrailEntryList atel = pi.getAuditTrailEntryList();
				for (int i = 0; i < atel.size(); i++) {
					AuditTrailEntry ate = atel.get(i);

					/**
					 * An AuditTrailEntry corresponds to a transition in the
					 * FSM. First, construct the payload of the state preceding
					 * the transition.
					 */

					FsmMinerPayload fromPayload = new FsmMinerPayload(settings);
					// Use the horizon settings.
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 1, settings.getHorizonSettings(true,
								mode), fromPayload, mode);
						mineFwd(atel, i, 0, settings.getHorizonSettings(false,
								mode), fromPayload, mode);
					}

					// Use the attribute settings.
					if (settings.getUseAttributes()) {
						for (int j = 0; j < i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										fromPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					/**
					 * Second, in a similar way, create the payload of the state
					 * succeding the transition.
					 */
					FsmMinerPayload toPayload = new FsmMinerPayload(settings);
					for (int mode = 0; mode < FsmMinerPayload.LAST; mode++) {
						mineBwd(atel, i, 0, settings.getHorizonSettings(true,
								mode), toPayload, mode);
						mineFwd(atel, i, 1, settings.getHorizonSettings(false,
								mode), toPayload, mode);
					}

					if (settings.getUseAttributes()) {
						for (int j = 0; j <= i; j++) {
							AuditTrailEntry ate2 = atel.get(j);
							DataSection dataSection = ate2.getDataAttributes();
							for (String attribute : dataSection.keySet()) {
								if (settings.getAttributeSettings()
										.containsKey(attribute)) {
									String cluster = settings
											.getAttributeSettings().get(
													attribute).get(
													dataSection.get(attribute));
									if (cluster != null) {
										toPayload.getAttributePayload().put(
												attribute, cluster);
									}
								}
							}
						}
					}

					if (getMSEMap().get(toPayload.toString()) == null) {
						getUnknownSet().add(toPayload.toString());
						continue;
					}

					// for node
					if (i == 0) {
						if (getMSEMap().get(fromPayload.toString()) != null) {
							if (ate.getTimestamp() != null) {
								double remaingTime = endTime
										- ate.getTimestamp().getTime();
								double remaingTimeMean = getEstimator(fsmStatistics
										.getRemainingMap().get(
												fromPayload.toString()));
								if (estimator.equals("HEURISTIC1")) {
									remaingTimeMean = fsmStatistics
											.getRemainingMap().get("[{}]")
											.getMean() / 2;
								} else if (estimator.equals("HEURISTIC2")) {
									double elapsedTime = ate.getTimestamp()
											.getTime()
											- startTime;
									double heurst = fsmStatistics
											.getRemainingMap().get("[{}]")
											.getMean();
									remaingTimeMean = Math.max(0.0, heurst
											- elapsedTime);
								}
								getMSEMap().get(fromPayload.toString())
										.addValue(
												getMSE(remaingTimeMean,
														remaingTime));
								getMAEMap().get(fromPayload.toString())
										.addValue(
												getMAE(remaingTimeMean,
														remaingTime));
								getMAPEMap().get(fromPayload.toString())
										.addValue(
												getMAPE(remaingTimeMean,
														remaingTime));
							}
						}
					}

					if (getMSEMap().get(toPayload.toString()) != null) {
						if (ate.getTimestamp() != null) {
							double remaingTime = endTime
									- ate.getTimestamp().getTime();
							double remaingTimeMean = getEstimator(fsmStatistics
									.getRemainingMap()
									.get(toPayload.toString()));
							if (estimator.equals("HEURISTIC1")) {
								remaingTimeMean = fsmStatistics
										.getRemainingMap().get("[{}]")
										.getMean() / 2;
							} else if (estimator.equals("HEURISTIC2")) {
								double elapsedTime = ate.getTimestamp()
										.getTime()
										- startTime;
								double heurst = fsmStatistics.getRemainingMap()
										.get("[{}]").getMean();
								remaingTimeMean = Math.max(0.0, heurst
										- elapsedTime);
							}
							getMSEMap().get(toPayload.toString()).addValue(
									getMSE(remaingTimeMean, remaingTime));
							getMAEMap().get(toPayload.toString()).addValue(
									getMAE(remaingTimeMean, remaingTime));
							getMAPEMap().get(toPayload.toString()).addValue(
									getMAPE(remaingTimeMean, remaingTime));
						}
					}
				}
			}
		} catch (Exception e) {
			Message.add(e.toString(), Message.ERROR);
		}
	}

	private double getEstimator(DescriptiveStatistics ds) {
		if (estimator.equals("MEAN"))
			return ds.getMean();
		else if (estimator.equals("MEDIAN"))
			return ds.getPercentile(50);
		else if (estimator.equals("MIN"))
			return ds.getMin();
		else if (estimator.equals("MAX"))
			return ds.getMax();
		return 0.0;
	}

	/**
	 * Collect infromation for the payload of a given state by taking backwards
	 * step.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineBwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		// Skip if these settings should not be used.
		if (settings.getUse()) {
			int k = 0;
			// Initialize number of visible steps to take.
			int horizon = settings.getFilteredHorizon();
			for (int j = i - offset; horizon != 0 && j >= 0; j--) {
				try {
					// Get the audit trail entry.
					AuditTrailEntry ate2 = atel.get(j);
					// Get the info. This depends on mode.
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					// Check whether not filtered out.
					if (settings.getFilter().contains(s)) {
						// Not filtered out, add to payload if number of steps
						// not exceeded.
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + j + offset > i) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addBwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addBwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addBwdBag(mode, s);
								break;
							}
							}
						}
						// Found an unfiltered step: decrease counter.
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	/**
	 * Collect infromation for the payload of a given state by taking forward
	 * steps.
	 * 
	 * @param atel
	 *            AuditTrailEntryList the list of audti trail entries for this
	 *            process instance.
	 * @param i
	 *            int the index of the current audit trail entry in this list.
	 * @param offset
	 *            int offset from i to start taking steps.
	 * @param settings
	 *            FsmHorizonSettings the settings to use.
	 * @param payload
	 *            FsmMinerPayload the payload to store the results in.
	 * @param mode
	 *            int whether to collect model element info, originator info, or
	 *            event type info.
	 */
	private static void mineFwd(AuditTrailEntryList atel, int i, int offset,
			FsmHorizonSettings settings, FsmMinerPayload payload, int mode) {
		/**
		 * See MineBwd for adiditonal comments.
		 */
		if (settings.getUse()) {
			int k = 0;
			int horizon = settings.getFilteredHorizon();
			for (int j = i + offset; horizon != 0 && j < atel.size(); j++) {
				try {
					AuditTrailEntry ate2 = atel.get(j);
					String s;
					switch (mode) {
					case (FsmMinerPayload.MODELELEMENT): {
						s = ate2.getElement();
						break;
					}
					case (FsmMinerPayload.ORIGINATOR): {
						s = ate2.getOriginator();
						break;
					}
					case (FsmMinerPayload.EVENTTYPE): {
						s = ate2.getType();
						break;
					}
					default: {
						s = "";
					}
					}
					if (settings.getFilter().contains(s)) {
						if (settings.getHorizon() < 0
								|| settings.getHorizon() + i + offset > j) {
							switch (settings.getAbstraction()) {
							case (FsmHorizonSettings.SEQ): {
								payload.addFwdSeq(mode, s);
								break;
							}
							case (FsmHorizonSettings.SET): {
								payload.addFwdSet(mode, s);
								break;
							}
							case (FsmHorizonSettings.BAG): {
								payload.addFwdBag(mode, s);
								break;
							}
							}
						}
						horizon--;
					}
				} catch (Exception e) {
					Message.add(e.toString(), Message.ERROR);
				}
			}
		}
	}

	public void buildDS(AcceptFSM graph, long timeunit) {
		this.timeunit = timeunit;
		for (ModelGraphVertex as : graph.getVerticeList()) {
			Message.add(as.getId() + "," + ((FSMState) as).getLabel(),
					Message.DEBUG);
			String label = getActualLabel(((FSMState) as).getLabel());
			getMSEMap().put(label, DescriptiveStatistics.newInstance());
			getMAEMap().put(label, DescriptiveStatistics.newInstance());
			getMAPEMap().put(label, DescriptiveStatistics.newInstance());
		}
	}

	private double getMSE(double a, double b) {
		a = a / timeunit;
		b = b / timeunit;
		return (a - b) * (a - b);
	}

	private double getMAE(double a, double b) {
		a = a / timeunit;
		b = b / timeunit;
		return Math.abs(a - b);
	}

	private double getMAPE(double a, double b) {
		if (b == 0.0)
			return 0.0;
		a = a / timeunit;
		b = b / timeunit;
		return Math.abs(b - a) / b * 100.0;
	}

	private String getActualLabel(String str) {
		return str.substring(0, str.lastIndexOf("]") + 1);
	}

	private long getStartTime(ProcessInstance pi) {
		AuditTrailEntryList atel = pi.getAuditTrailEntryList();
		try {
			for (int i = 0; i < atel.size(); i++) {
				AuditTrailEntry ate = atel.get(i);
				if (ate.getTimestamp() != null)
					return ate.getTimestamp().getTime();
			}
		} catch (Exception ce) {
		}
		return -1;
	}

	private long getEndTime(ProcessInstance pi) {
		AuditTrailEntryList atel = pi.getAuditTrailEntryList();
		try {
			for (int i = 0; i < atel.size(); i++) {
				AuditTrailEntry ate = atel.get(atel.size() - i - 1);
				if (ate.getTimestamp() != null)
					return ate.getTimestamp().getTime();
			}
		} catch (Exception ce) {
		}
		return -1;
	}
}
