package org.processmining.mining.partialorderminingTimeUnit;

import java.util.ArrayList;

/**
 * Class representing a time unit, such as "hour" or "millisecond". The
 * following time units can be represented by this class TimeUnit.YEARS,
 * TimeUnit.MONTHS, TimeUnit.WEEKS, TimeUnit.DAYS, TimeUnit.HOURS,
 * TimeUnit.MINUTES, TimeUnit.SECONDS, TimeUnit.MILLISECONDS. Actually, an
 * instance of the class TimeUnit can simply be create by TimeUnit. and followed
 * by the constant (e.g. DAYS)
 * 
 * @author arozinat
 * @author rmans
 */
public enum TimeUnit {
	// YEARS("Years", 12*30*7*24*60*60),
	// MONTHS("Months", 30*7*24*60*60),
	WEEKS("Weeks", 7 * 24 * 60 * 60 * 1000), DAYS("Days", 24 * 60 * 60 * 1000), HOURS(
			"Hours", 60 * 60 * 1000), MINUTES("Minutes", 60 * 1000), SECONDS(
			"Seconds", 1000), MILLISECONDS("Milliseconds", 1);

	/**
	 * Constructor for the enumeration type TimeUnit
	 */
	private TimeUnit(String name, long conversionValue) {
		myName = name;
		myConversionValue = conversionValue;
	}

	/**
	 * Returns a list with all possible timeunit values
	 * 
	 * @return ArrayList a list with all possible time unit values
	 */
	public ArrayList<TimeUnit> getValues() {
		ArrayList<TimeUnit> returnValues = new ArrayList<TimeUnit>();
		// returnValues.add(TimeUnit.YEARS);
		// returnValues.add(TimeUnit.MONTHS);
		returnValues.add(TimeUnit.WEEKS);
		returnValues.add(TimeUnit.DAYS);
		returnValues.add(TimeUnit.HOURS);
		returnValues.add(TimeUnit.MINUTES);
		returnValues.add(TimeUnit.SECONDS);
		returnValues.add(TimeUnit.MILLISECONDS);
		return returnValues;
	}

	public String toString() {
		return myName;
	}

	/**
	 * Returns the value by which a time value in seconds needs to be devided in
	 * order to yield the respective time unit.
	 * 
	 * @return the conversion value for the repsective time unit
	 */
	public long getConversionValue() {
		return myConversionValue;
	}

	private String myName;
	private long myConversionValue;
}
