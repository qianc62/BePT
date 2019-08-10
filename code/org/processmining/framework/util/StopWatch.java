/**
 *
 */
package org.processmining.framework.util;

import java.util.Date;

/**
 * Provides a simple timer facility, to measure the duration of tasks.
 * 
 * @author Christian W. Guenther (christian at deckfour dot org)
 */
public class StopWatch {

	/**
	 * Constants encoding the duration of time units in millisconds.
	 */
	public static final long SECOND = 1000;
	public static final long MINUTE = 60000;
	public static final long HOUR = 3600000;
	public static final long DAY = 86400000;

	/**
	 * Formats a duration, given in milliseconds, to a human-readable string
	 * (verbose)
	 * 
	 * @param timeInMillis
	 * @return
	 */
	public static String formatDuration(long timeInMillis) {
		boolean matched = false;
		StringBuffer formatted = new StringBuffer();
		if (timeInMillis > DAY) {
			matched = true;
			long days = timeInMillis / DAY;
			formatted.append(days);
			if (days > 1) {
				formatted.append(" days, ");
			} else {
				formatted.append(" day, ");
			}
			timeInMillis %= DAY;
		}
		if (timeInMillis > HOUR || matched == true) {
			matched = true;
			long hours = timeInMillis / HOUR;
			formatted.append(hours);
			if (hours > 1) {
				formatted.append(" hours, ");
			} else {
				formatted.append(" hour, ");
			}
			timeInMillis %= HOUR;
		}
		if (timeInMillis > MINUTE || matched == true) {
			matched = true;
			long minutes = timeInMillis / MINUTE;
			formatted.append(minutes);
			if (minutes > 1) {
				formatted.append(" minutes, ");
			} else {
				formatted.append(" minute, ");
			}
			timeInMillis %= MINUTE;
		}
		if (timeInMillis > SECOND || matched == true) {
			matched = true;
			long seconds = timeInMillis / SECOND;
			formatted.append(seconds);
			if (seconds > 1) {
				formatted.append(" seconds, and ");
			} else {
				formatted.append(" second, and ");
			}
			timeInMillis %= SECOND;
		}
		formatted.append(timeInMillis);
		formatted.append(" milliseconds");
		return formatted.toString();
	}

	/**
	 * Formats a duration, given in milliseconds, to a human-readable string
	 * (verbose)
	 * 
	 * @param timeInMillis
	 * @return
	 */
	public static String formatDurationNoMilliSeconds(long timeInMillis) {
		boolean matched = false;
		StringBuffer formatted = new StringBuffer();
		if (timeInMillis > DAY) {
			matched = true;
			long days = timeInMillis / DAY;
			formatted.append(days);
			if (days > 1) {
				formatted.append(" days, ");
			} else {
				formatted.append(" day, ");
			}
			timeInMillis %= DAY;
		}
		if (timeInMillis > HOUR || matched == true) {
			matched = true;
			long hours = timeInMillis / HOUR;
			formatted.append(hours);
			if (hours > 1) {
				formatted.append(" hours, ");
			} else {
				formatted.append(" hour, ");
			}
			timeInMillis %= HOUR;
		}
		if (timeInMillis > MINUTE || matched == true) {
			matched = true;
			long minutes = timeInMillis / MINUTE;
			formatted.append(minutes);
			if (minutes > 1) {
				formatted.append(" minutes, ");
			} else {
				formatted.append(" minute, ");
			}
			timeInMillis %= MINUTE;
		}
		if (timeInMillis > SECOND || matched == true) {
			matched = true;
			long seconds = timeInMillis / SECOND;
			formatted.append(seconds);
			if (seconds > 1) {
				formatted.append(" seconds.");
			} else {
				formatted.append(" second.");
			}
			timeInMillis %= SECOND;
		}
		return formatted.toString();
	}

	/**
	 * Keeps the start date
	 */
	protected long start = 0;
	/**
	 * Keeps the finish date
	 */
	protected long finish = 0;

	/**
	 * Creates a new timer instance
	 */
	public StopWatch() {
		start = 0;
		finish = 0;
	}

	/**
	 * Starts measuring the duration (sets start time)
	 */
	public synchronized void start() {
		start = System.currentTimeMillis();
	}

	/**
	 * Finishes measuring the duration (sets finish time)
	 */
	public synchronized long stop() {
		finish = System.currentTimeMillis();
		return finish - start;
	}

	/**
	 * Retrieves the start date
	 * 
	 * @return the start date
	 */
	public Date getStartDate() {
		return new Date(start);
	}

	/**
	 * Retrieves the finish date
	 * 
	 * @return the finish date
	 */
	public Date getFinishDate() {
		return new Date(finish);
	}

	/**
	 * Retrieves the duration
	 * 
	 * @return the duration
	 */
	public long getDuration() {
		return finish - start;
	}

	/**
	 * Retrieves the duration as a human-readable string
	 * 
	 * @return the duration as a human-readable string
	 */
	public String formatDuration() {
		return StopWatch.formatDuration(finish - start);
	}
}
