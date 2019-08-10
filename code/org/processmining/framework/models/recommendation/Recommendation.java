/**
 *
 */
package org.processmining.framework.models.recommendation;

import java.util.*;

import org.processmining.framework.log.*;
import org.processmining.framework.ui.Message;

/**
 * A simple data structure for storing recommendations, i.e. that a specific
 * task should be executed and the confidence with which this is recommended.
 * Optionally, the recommendation may also contain the resource specification
 * for which it applies.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class Recommendation implements Comparable<Recommendation> {

	private static final String NAN = Double.toString(Double.NaN);
	public String task = "";
	public String eventType = "";
	public String rationale = "";
	protected SortedSet<String> users = new TreeSet<String>();
	protected SortedSet<String> roles = new TreeSet<String>();
	protected SortedSet<String> groups = new TreeSet<String>();

	private double dontExpectedValue = Double.NaN;
	private double dontExpectedSquaredValue = Double.NaN;
	private double doExpectedSquaredValue = Double.NaN;
	private double doExpectedValue = Double.NaN;
	private double doWeight = 0.0;
	private double dontWeight = 0.0;

	/**
	 * Creates a new, pristine recommendation.
	 */
	public Recommendation() {
	}

	/**
	 * Sets the recommended task
	 * 
	 * @param aTask
	 */
	public void setTask(String aTask) {
		task = aTask;
	}

	/**
	 * Sets the recommended event type
	 * 
	 * @param anEventType
	 */
	public void setEventType(String anEventType) {
		eventType = anEventType;
	}

	/**
	 * Sets the rationale for this recommendation
	 * 
	 * @param aRationale
	 *            human-readable justification or explanation string
	 */
	public void setRationale(String aRationale) {
		rationale = aRationale;
	}

	public void setDoExpectedValue(double doExpectedValue) {
		this.doExpectedValue = doExpectedValue;
	}

	public void setDontExpectedValue(double dontExpectedValue) {
		this.dontExpectedValue = dontExpectedValue;
	}

	public void setDoExpectedSquaredValue(double doExpectedSquaredValue) {
		this.doExpectedSquaredValue = doExpectedSquaredValue;
	}

	public void setDontExpectedSquaredValue(double dontExpectedSquaredValue) {
		this.dontExpectedSquaredValue = dontExpectedSquaredValue;
	}

	public void setDoWeight(double doWeight) {
		this.doWeight = doWeight;
	}

	public void setDontWeight(double dontWeight) {
		this.dontWeight = dontWeight;
	}

	/**
	 * Adds a user for which this recommendation holds
	 * 
	 * @param aUser
	 */
	public void addUser(String aUser) {
		if (users == null) {
			users = new TreeSet<String>();
		}
		users.add(aUser);
	}

	/**
	 * Adds a role for which this recommendation holds
	 * 
	 * @param aRole
	 */
	public void addRole(String aRole) {
		if (roles == null) {
			roles = new TreeSet<String>();
		}
		roles.add(aRole);
	}

	/**
	 * Adds a group for which this recommendation holds
	 * 
	 * @param aGroup
	 */
	public void addGroup(String aGroup) {
		if (groups == null) {
			groups = new TreeSet<String>();
		}
		groups.add(aGroup);
	}

	/**
	 * @return the recommended LogEvent
	 */
	public LogEvent getLogEvent() {
		return new LogEvent(task, eventType);
	}

	/**
	 * @return the recommended task
	 */
	public String getTask() {
		return task;
	}

	/**
	 * <b>optional:</b> may return <code>null</code> if not set!
	 * 
	 * @return the recommended event type
	 */
	public String getEventType() {
		return eventType;
	}

	/**
	 * @return confidence value of the recommendation (should be within [0, 1])
	 */
	// public double getConfidence() {
	// return confidence;
	// }
	/**
	 * @return supprt value of the recommendation
	 */
	// public double getWeight() {
	// return weight;
	// }
	/**
	 * <b>optional:</b> may return <code>null</code> if not set!
	 * 
	 * @return a human-readable justification for this recommendation
	 */
	public String getRationale() {
		return rationale;
	}

	/**
	 * <b>optional:</b> may return <code>null</code> if not set!
	 * 
	 * @return the set of users to which this recommendation applies.
	 */
	public SortedSet<String> getUsers() {
		return users;
	}

	/**
	 * <b>optional:</b> may return <code>null</code> if not set!
	 * 
	 * @return the set of roles to which this recommendation applies.
	 */
	public SortedSet<String> getRoles() {
		return roles;
	}

	/**
	 * <b>optional:</b> may return <code>null</code> if not set!
	 * 
	 * @return the set of groups to which this recommendation applies.
	 */
	public SortedSet<String> getGroups() {
		return groups;
	}

	public double getDoExpectedValue() {
		return doExpectedValue;
	}

	public double getDontExpectedValue() {
		return dontExpectedValue;
	}

	public double getDoExpectedSquaredValue() {
		return doExpectedSquaredValue;
	}

	public double getDontExpectedSquaredValue() {
		return dontExpectedSquaredValue;
	}

	public double getDoWeight() {
		return doWeight;
	}

	public double getDontWeight() {
		return dontWeight;
	}

	public double getDoVariance() {
		return getDoExpectedSquaredValue() - getDoExpectedValue()
				* getDoExpectedValue();
	}

	public double getDontVariance() {
		return getDontExpectedSquaredValue() - getDontExpectedValue()
				* getDontExpectedValue();
	}

	public double get5PercentConfidenceIntervalForDo() {
		return 1.960 * Math.sqrt(getDoVariance() / getDoWeight());
	}

	/*
	 * z.1 z.05 z.025 z.01 z.005 z.001 z.0005 1.282 1.645 1.960 2.326 2.576
	 * 3.090 3.291
	 */
	public double get5PercentConfidenceIntervalForDont() {
		return 1.960 * Math.sqrt(getDontVariance() / getDontWeight());
	}

	private static final String LOCALE = "%1$6.2f";
	private static final String FULLLOCALE = LOCALE + " [+/- %2$6.2f]";

	public String getDoString() {
		if (Double.toString(doExpectedValue).equalsIgnoreCase(NAN)) {
			return "No recommendation";
		} else if (Double.toString(get5PercentConfidenceIntervalForDo())
				.equalsIgnoreCase(NAN)) {
			return String.format(LOCALE, getDoExpectedValue());
		} else {
			return String.format(FULLLOCALE, getDoExpectedValue(),
					get5PercentConfidenceIntervalForDo());
		}
	}

	public String getDontString() {
		if (Double.toString(dontExpectedValue).equalsIgnoreCase(NAN)) {
			return "unknown";
		} else if (Double.toString(get5PercentConfidenceIntervalForDont())
				.equalsIgnoreCase(NAN)) {
			return String.format(LOCALE, getDontExpectedValue());
		} else {
			return String.format(FULLLOCALE, getDontExpectedValue(),
					get5PercentConfidenceIntervalForDont());
		}
	}

	public String toString() {
		return getRationale() + " \n" + getTask() + " (" + getEventType() +
		// ") , confidence: " + getConfidence() +", weight: " + getWeight();
				") , do: " + getDoString() + ", dont: " + getDontString();
	}

	public String toExtendedString() {
		return getRationale() + " \n" + getTask() + " (" + getEventType()
				+ ") , do: " + getDoString() + "{" + getDoWeight() + "}"
				+ ", dont: " + getDontString() + "{" + getDontWeight() + "}";
	}

	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof Recommendation)) {
			return false;
		}
		Recommendation rec = (Recommendation) o;
		return eventType.equals(rec.eventType) && groups.equals(rec.groups)
				&& roles.equals(rec.roles) && task.equals(rec.task)
				&& users.equals(rec.users);

	}

	/**
	 * The compareTo method is implemented based on the values of the
	 * recommendation. This means that it does <b>NOT</b> necessarily coincide
	 * with the <code>equals()</code> method's results!
	 */
	public int compareTo(Recommendation rec) {
		double r1 = doExpectedValue - dontExpectedValue;
		double r2 = rec.doExpectedValue - rec.dontExpectedValue;
		// Let us assume that we always maximize our scale.
		if (Double.toString(doExpectedValue).equalsIgnoreCase(NAN)
				&& Double.toString(rec.doExpectedValue).equalsIgnoreCase(NAN)) {
			return Double.compare(rec.dontExpectedValue, dontExpectedValue);
		} else if (Double.toString(doExpectedValue).equalsIgnoreCase(NAN)) {
			return 1;
		} else if (Double.toString(rec.doExpectedValue).equalsIgnoreCase(NAN)) {
			return -1;
		} else if (r1 > r2) {
			// this has a greater ratio than rec
			return -1;
		} else if (r1 < r2) {
			return 1;
		} else {
			// Sort them on the confidence
			return Double.compare(get5PercentConfidenceIntervalForDo(), rec
					.get5PercentConfidenceIntervalForDo());
		}
	}

	public void addToDoRecommendation(double expectedDoValue,
			double expectedDoSquaredValue, double expectedDoWeight) {
		double doW = doWeight + expectedDoWeight;
		if (doW > 0) {
			this.setDoExpectedValue(((doWeight > 0 ? doExpectedValue * doWeight
					: 0) + (expectedDoWeight > 0 ? expectedDoValue
					* expectedDoWeight : 0))
					/ doW);
			this
					.setDoExpectedSquaredValue(((doWeight > 0 ? doExpectedSquaredValue
							* doWeight
							: 0) + (expectedDoWeight > 0 ? expectedDoSquaredValue
							* expectedDoWeight
							: 0))
							/ doW);
			this.setDoWeight(doW);
		}
	}

	public void addToDontRecommendation(double expectedDontValue,
			double expectedDontSquaredValue, double expectedDontWeight) {
		double dontW = this.getDontWeight() + expectedDontWeight;
		if (dontW > 0) {
			this
					.setDontExpectedValue(((dontWeight > 0 ? dontExpectedValue
							* dontWeight : 0) + (expectedDontWeight > 0 ? expectedDontValue
							* expectedDontWeight
							: 0))
							/ dontW);
			this
					.setDontExpectedSquaredValue(((dontWeight > 0 ? dontExpectedSquaredValue
							* dontWeight
							: 0) + (expectedDontWeight > 0 ? expectedDontSquaredValue
							* expectedDontWeight
							: 0))
							/ dontW);
			this.setDontWeight(dontW);
		}
	}

	public void addDoToDoRecommendation(Recommendation toAdd) {
		this.addToDoRecommendation(toAdd.doExpectedValue,
				toAdd.doExpectedSquaredValue, toAdd.doWeight);
	}

	public void addDontToDontRecommendation(Recommendation toAdd) {
		this.addToDontRecommendation(toAdd.dontExpectedValue,
				toAdd.dontExpectedSquaredValue, toAdd.dontWeight);
	}

	public void addDoToDontRecommendation(Recommendation toAdd) {
		this.addToDontRecommendation(toAdd.doExpectedValue,
				toAdd.doExpectedSquaredValue, toAdd.doWeight);
	}

	public void addRecommendation(Recommendation toAdd)
			throws NotEqualRecommendationException {
		if (!this.equals(toAdd)) {
			// Problem
			throw new NotEqualRecommendationException(
					"Can only add recommendations that are the same.");
		}
		addToDoRecommendation(toAdd.doExpectedValue,
				toAdd.doExpectedSquaredValue, toAdd.doWeight);
		addToDontRecommendation(toAdd.dontExpectedValue,
				toAdd.dontExpectedSquaredValue, toAdd.dontWeight);
	}
}
