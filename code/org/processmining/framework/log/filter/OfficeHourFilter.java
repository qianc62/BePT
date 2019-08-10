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

package org.processmining.framework.log.filter;

import java.io.*;
import java.util.*;

import java.awt.*;

import javax.swing.*;

import org.processmining.framework.log.AuditTrailEntries;
import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.util.GUIPropertyBoolean;
import org.processmining.framework.util.GUIPropertyInteger;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogFilter;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.LogSummary;
import org.processmining.framework.log.ProcessInstance;
import org.w3c.dom.*;

/**
 * This logFilter filters the log based on the originators of audittrailentries.
 * 
 * During construction, a list of originators to keep has to be provided,
 * together with another LogFilter. Then afterwards, using the method
 * doFiltering(), specific originators are filtered.
 * 
 * ProcessInstances are ignored if they turn out to be empty after filtering.
 * 
 * This filter is based on the OfficeHourFilter
 * 
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2006
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author Minseok Song
 * @version 1.0
 */

public class OfficeHourFilter extends LogFilter {

	private long offset = 0;
	private int startTime = 0;
	private int endTime = 0;
	private int timezoneOffset = 0;
	private boolean holiday = false;

	public OfficeHourFilter() {
		super(LogFilter.MODERATE, "Office Hour Filter");
	}

	public OfficeHourFilter(long offset, int start, int end,
			int timezoneOffset, boolean holiday) {// GUIPropertyBoolean[]
		// dateUI) {
		super(LogFilter.MODERATE, "Office Hour Filter");
		this.offset = offset;
		this.startTime = start;
		this.endTime = end;
		this.timezoneOffset = timezoneOffset;
		this.holiday = holiday;
	}

	protected boolean doFiltering(ProcessInstance instance) {
		AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
		AuditTrailEntry current;
		for (int i = 0; i < ateList.size(); i++) {
			try {
				current = ateList.get(i);
				if (holiday)
					current.setTimestamp(new Date(workingTimeHoliday(current)));
				else
					current.setTimestamp(new Date(workingTime(current)));
				ateList.replace(current, i);
			} catch (IOException e) {
				// error?
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	/**
	 * The log is changed, it events are present in the processinstance that are
	 * not in the list given in the constructor.
	 * 
	 * @return boolean true
	 */
	public boolean thisFilterChangesLog() {
		return true;
	}

	protected String getHelpForThisLogFilter() {
		return "Removes all AuditTrailEntries from the log that do not correspond "
				+ "to one of the Originators in the selection.";
	}

	public LogFilterParameterDialog getParameterDialog(LogSummary summary) {
		return new LogFilterParameterDialog(summary, OfficeHourFilter.this) {
			GUIPropertyInteger startValue;
			GUIPropertyInteger targetValue;
			GUIPropertyBoolean holidayUI;
			GUIPropertyInteger timeZone;
			protected LogReader inputLog;

			public LogFilter getNewLogFilter() {
				long offset = summary.getStartTime(
						summary.getProcesses()[0].getName()).getTime()
						+ timeZone.getValue() * 3600000L;
				// offset = (offset+3600000L)/(long)86400000L*(long)86400000L;
				return new OfficeHourFilter(offset, startValue.getValue(),
						targetValue.getValue(), timeZone.getValue(), holidayUI
								.getValue());// date);
			}

			protected JPanel getPanel() {
				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
				startValue = new GUIPropertyInteger("start time", 9, 0, 23);
				targetValue = new GUIPropertyInteger("end time", 17, 0, 23);
				holidayUI = new GUIPropertyBoolean("Considering weekends",
						false);
				GregorianCalendar calendar = new GregorianCalendar();
				int k = (calendar.getTimeZone().getDefault().getRawOffset() + calendar
						.getTimeZone().getDSTSavings()) / 3600000;
				timeZone = new GUIPropertyInteger("Timezone Offset: ", "", k,
						0, 24);
				;
				p.add(startValue.getPropertyPanel());
				p.add(targetValue.getPropertyPanel());
				p.add(timeZone.getPropertyPanel());
				p.add(holidayUI.getPropertyPanel());

				return p;
			}

			protected boolean getAllParametersSet() {
				return true;
			}

		};
	}

	/*
	 * protected long workingTime(AuditTrailEntry ate) { long diff; long day =
	 * ate.getTimestamp().getTime()+timezoneOffset*3600000L; long day_duration =
	 * day/(long)86400000L - offset/(long)86400000L; long hour_offset = day -
	 * day/(long)86400000L*(long)86400000L +3600000L - 3600000L*startTime; long
	 * hour_diff =
	 * Math.min(Math.max(hour_offset,0),3600000L*(endTime-startTime)); diff =
	 * day_duration*3600000L*(endTime-startTime) + hour_diff; if(holiday) diff
	 * -= numHoliday(ate)*3600000L*(endTime-startTime); return diff; }
	 */
	protected long workingTime(AuditTrailEntry ate) {
		long diff = 0;
		long start = offset;
		long end = ate.getTimestamp().getTime() + timezoneOffset * 3600000L;
		long duration = end / (long) 86400000L - start / (long) 86400000L;
		long adjustedEnd = Math.max(end % (long) 86400000L - 3600000L
				* startTime, 0);
		adjustedEnd = Math.min(adjustedEnd, 3600000L * (endTime - startTime));
		long adjustedBegin = Math.max(start % (long) 86400000L - 3600000L
				* startTime, 0);
		adjustedBegin = Math.min(adjustedBegin,
				3600000L * (endTime - startTime));

		if (duration == 0) {
			diff = adjustedEnd - adjustedBegin;
		} else if (duration >= 1) {
			long temp = 3600000L * (endTime - startTime) * (duration - 1);
			diff += Math.max(temp, 0);
			diff += 3600000L * (endTime - startTime) - adjustedBegin
					+ adjustedEnd;
		}
		return diff;
	}

	protected long workingTimeHoliday(AuditTrailEntry ate) {
		long diff = 0;
		long start = offset;
		long end = ate.getTimestamp().getTime() + timezoneOffset * 3600000L;
		long duration = end / (long) 86400000L - start / (long) 86400000L;
		long adjustedEnd = Math.max(end % (long) 86400000L - 3600000L
				* startTime, 0);
		adjustedEnd = Math.min(adjustedEnd, 3600000L * (endTime - startTime));
		long adjustedBegin = Math.max(start % (long) 86400000L - 3600000L
				* startTime, 0);
		adjustedBegin = Math.min(adjustedBegin,
				3600000L * (endTime - startTime));

		if (duration == 0) {
			if (!isWeekend(start))
				diff = adjustedEnd - adjustedBegin;
		} else if (duration >= 1) {
			long temp = start;
			diff = 0;
			if (!isWeekend(start))
				diff += 3600000L * (endTime - startTime) - adjustedBegin;
			for (int i = 1; i < duration; i++) {
				temp += 86400000L;
				if (!isWeekend(temp))
					diff += 3600000L * (endTime - startTime);
			}
			if (!isWeekend(end)) {
				diff += adjustedEnd;
			}
		}
		return diff;
	}

	protected boolean isWeekend(long date) {
		boolean bResult = false;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(new Date(date));
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
			bResult = true;
		return bResult;
	}

	protected int numHoliday(AuditTrailEntry ate) {
		long beginTime = offset;
		long day = ate.getTimestamp().getTime() + timezoneOffset * 3600000L;
		long duration = day / (long) 86400000L - beginTime / (long) 86400000L;

		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(ate.getTimestamp());
		int numHoliday = 0;
		if (duration >= 2) {
			long temp = ate.getTimestamp().getTime();
			for (int i = 1; i < duration - 1; i++) {
				temp += 86400000L;
				calendar.setTime(new Date(temp));
				int date = calendar.get(Calendar.DAY_OF_WEEK);

				if (date == Calendar.SATURDAY || date == Calendar.SUNDAY)
					numHoliday++;
			}
		}
		return numHoliday;
	}

	/**
	 * Write the inside of the <FilterSpecific> tag in the XML export file to
	 * the OutputStream output.
	 * 
	 * @param output
	 *            OutputStream
	 */
	protected void writeSpecificXML(BufferedWriter output) throws IOException {

	}

	/**
	 * Read the inside of the <FilterSpecific> tag in the XML export file from
	 * the InputStream input.
	 * 
	 * @param input
	 *            InputStream
	 */
	protected void readSpecificXML(Node logFilterSpecifcNode)
			throws IOException {

	}
}
