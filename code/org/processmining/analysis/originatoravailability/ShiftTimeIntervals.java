package org.processmining.analysis.originatoravailability;

/***********************************************************
 *      This software is part of the ProM package          *
 *             http://www.processmining.org/               *
 *                                                         *
 *            Copyright (c) 2003-2006 TU/e Eindhoven       *
 *                and is licensed under the                *
 *            Common Public License, Version 1.0           *
 *        by Eindhoven University of Technology            *
 *           Department of Information Systems             *
 *                 http://is.tm.tue.nl                     *
 *                                                         *
 **********************************************************/

import java.util.Calendar;
import java.util.Date;

import org.processmining.framework.util.ComparablePair;

/**
 * Calculates the shift of the day for which a given time belongs. Every shift
 * has a starting date and an ending date.
 * 
 * For instance, if the shift is equal to six hours, than the times of the days
 * will fit in one of the following four time shift intervals: (i) 0:00h-5:59h,
 * (ii) 6:00h-11:59h, (iii) 12:00h-17:59h, and (iv) 18:00h-23:59h.
 * 
 * @author Peter van den Brand, Ana Karla Alves de Medeiros
 * 
 */

public class ShiftTimeIntervals {
	public static final int DEFAULT_HOURS_PER_SHIFT = 6;

	private int hoursPerShift = DEFAULT_HOURS_PER_SHIFT;

	/**
	 * Calculates the time intervals based on the value for the constant
	 * ShiftTimeIntervals.DEFAULT_HOURS_PER_SHIFT
	 */
	public ShiftTimeIntervals() {
		hoursPerShift = DEFAULT_HOURS_PER_SHIFT;
	}

	/**
	 * Build the time intervals based on the value provided for the parameter
	 * hoursPerShift.
	 * 
	 * @param Number
	 *            of ours per shift of a resource.
	 */
	public ShiftTimeIntervals(int hoursPerShift) {
		this.hoursPerShift = hoursPerShift;
	}

	/**
	 * Identifies the shift for which a given time belongs. Every shift has a
	 * starting date and an ending date.
	 * 
	 * @param timestamp
	 *            Given time to calculate the shift for.
	 * @return The shift with the starting and ending dates.
	 */
	public ComparablePair<Date, Date> getShift(Date timestamp) {
		Calendar cal = Calendar.getInstance();

		cal.setTime(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, (int) Math.floor((cal
				.get(Calendar.HOUR_OF_DAY) % 24)
				/ hoursPerShift)
				* hoursPerShift);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date start = cal.getTime();

		cal.add(Calendar.HOUR_OF_DAY, hoursPerShift);
		cal.add(Calendar.SECOND, -1);
		Date end = cal.getTime();

		return new ComparablePair<Date, Date>(start, end);
	}
}
