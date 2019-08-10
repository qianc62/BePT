package org.processmining.mining.prediction;

import java.util.*;

import org.processmining.framework.log.LogSummary;

/**
 * @author Ronald Crooy
 * 
 */
public class PredictionMinerSettingsBasedOnLogSummary {
	public static PredictionMinerSettingsBasedOnLogSummary uniqueInstance;

	public static synchronized PredictionMinerSettingsBasedOnLogSummary getInstance() {
		return uniqueInstance;
	}

	public Integer timeSize;
	public HashSet<String> useElements;
	public HashSet<String> startEvents;
	public HashSet<String> completeEvents;
	public Integer cvsize;// cross validation size
	// public Integer trainPerc;//percentage
	public Integer bandSize;// actual size
	public Integer repeatOpt;
	public Boolean bychance;// false implies choose N random cases, true implies
	// choose cases by chance
	public double chance;
	public Integer target;// 0: remaining cyle time, 1:remaining time until X,
	// 2: X?
	public String targetElement;
	public Integer contKernel;// 0: gaussian, 1: epanechnikov,2:uniform
	public Integer catKernel;// 0: aitchisonaitken, 1: liracine
	public Integer ordKernel;// 0:aitchisonaitken, 1:wangvanryzin
	public Boolean useAttributes;
	public Boolean useEventDataAttributes;
	public Boolean useCaseDataAttributes;
	public Boolean useDurations;
	public Boolean useOccurrences;
	public Boolean useCompleteTrainingForBandwidth;
	public Integer casesize;
	public double tol;
	// public RConnector rconnection;
	public Boolean crossvalidate;// default true, if false no crossvalidation
	// will be performed but pure bandwidth
	// selection
	public HashMap<String, RConnector> Rconnections;

	public PredictionMinerSettingsBasedOnLogSummary(LogSummary summary) {
		timeSize = 60 * 1000 * 60;// default to hours
		useElements = new HashSet<String>();
		completeEvents = new HashSet<String>();
		completeEvents.add("complete");
		startEvents = new HashSet<String>();
		startEvents.add("start");
		String[] elements = summary.getModelElements();
		for (int i = 0; i < elements.length; i++) {
			useElements.add(elements[i]);
		}
		target = 0;
		targetElement = elements[0];
		catKernel = 1;
		contKernel = 0;
		ordKernel = 1;
		useAttributes = false;
		useDurations = false;
		useEventDataAttributes = false;
		useCaseDataAttributes = false;
		crossvalidate = true;
		useCompleteTrainingForBandwidth = false;
		useOccurrences = true;
		casesize = summary.getNumberOfProcessInstances();
		/*
		 * cases divided by (the number of cases that would lead to 100 prefixed
		 * cases) this gives us the number K for K-fold cross validation that
		 * would have nice performance
		 */
		cvsize = Math.min(casesize, 10);
		bandSize = Math.max(1, ((Double) Math.floor(200 / (summary
				.getNumberOfAuditTrailEntries() / casesize))).intValue());
		bandSize = Math.min(casesize - casesize / cvsize, bandSize);
		repeatOpt = 1;
		tol = 0.1;
		chance = Math.max(bandSize / casesize, 0.0001);
		bychance = false;
		Rconnections = new HashMap<String, RConnector>();

		// ensure only 1 settings object exists
		// uniqueInstance=this;
	}

	public String getcker() {
		if (contKernel == 0) {
			return new String("gaussian");
		} else if (contKernel == 1) {
			return new String("epanechnikov");
		} else {
			return new String("uniform");
		}
	}

	public String getoker() {
		if (ordKernel == 0) {
			return new String("wangvanryzin");
		} else {
			return new String("liracine");
		}
	}

	public String getuker() {
		if (catKernel == 0) {
			return new String("aitchisonaitken");
		} else {
			return new String("liracine");
		}
	}

}
