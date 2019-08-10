package org.processmining.analysis.performance.basicperformance.model;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;

/**
 * Provides some simple log statistics related to the duration of and the
 * distance between activities.
 * 
 * @author Minseok Song
 */
public class StatisticUnit {

	/*
	 * static facilities for providing unique ascending indices
	 */
	protected DescriptiveStatistics statistics = null;
	protected String name = "";
	protected int numTried;

	/**
	 * constructor protected - use factory methods for instantiation;
	 * 
	 * @param aProcessInstance
	 *            the process instance where this abstract event is contained
	 * @param aStartEvent
	 *            left boundary atomic event
	 * @param anEndEvent
	 *            right boundary atomic event
	 */
	public StatisticUnit(String aName) {
		this.name = aName;
		statistics = DescriptiveStatistics.newInstance();
		numTried = 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String aName) {
		this.name = aName;
	}

	public DescriptiveStatistics getStatistics() {
		return statistics;
	}

	public void addValue(double value) {
		statistics.addValue(value);
	}

	public void tick() {
		numTried++;
	}

	public void removeOutlier(int min, int max) {
		double minValue = statistics.getPercentile(min);
		double maxValue = statistics.getPercentile(max);
		DescriptiveStatistics tempDS = DescriptiveStatistics.newInstance();
		for (int k = 0; k < statistics.getValues().length; k++) {
			double temp = statistics.getValues()[k];
			if (temp >= minValue && temp <= maxValue) {
				tempDS.addValue(temp);
			}
		}
		statistics = null;
		statistics = tempDS;
	}
}
