/**
 *
 */
package org.processmining.analysis.log.scale;

import java.io.IOException;
import java.util.Date;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.LogReader;

/**
 * This scale weighs a process instance based on the overall duration of
 * execution (i.e. the runtime).
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class DurationScale implements ProcessInstanceScale {

	protected boolean positivelyCorrelated;

	/**
	 * Creates a new duration scale which is <b>negatively correlated</b>, i.e.
	 * shorter instances will be weighed higher.
	 */
	public DurationScale() {
		this(true);
	}

	/**
	 * Creates a new duration scale
	 * 
	 * @param isPositivelyCorrelated
	 *            Whether this instance is positively correlated to the instance
	 *            duration, i.e. if set to <code>true</code> longer-running
	 *            instances will yield a higher weight. If set to
	 *            <code>false</code>, shorter instances will be weighed higher.
	 */
	public DurationScale(boolean isPositivelyCorrelated) {
		positivelyCorrelated = isPositivelyCorrelated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.log.scale.ProcessInstanceScale#weigh(org.
	 * processmining.framework.log.LogReader, int)
	 */
	public double weigh(ProcessInstance anInstance) {
		if (anInstance.getAuditTrailEntryList().size() == 0) {
			return 0.0;
		}
		Date start, end;
		try {
			start = anInstance.getAuditTrailEntryList().get(0).getTimestamp();
			end = anInstance.getAuditTrailEntryList().get(
					anInstance.getAuditTrailEntryList().size() - 1)
					.getTimestamp();
		} catch (IOException ie) {
			ie.printStackTrace();
			return 0.0;
		}
		if (start == null || end == null) {
			// no weighing possible (first and last events have no timestamp)
			return 0.0;
		} else if (positivelyCorrelated == true) {
			return (end.getTime() - start.getTime()) / 1000.;
		} else {
			return (1000.0 / (end.getTime() - start.getTime()));
		}
	}

	public String getName() {
		return "Duration scale";
	}

	public String getHtmlDescription() {
		return "Calculates the time spent in this instance in second precision";
	}

	public void initializeScale(LogReader log) {
		// TODO Auto-generated method stub

	}

	public void updateScale(ProcessInstance instance, LogReader log) {
		// TODO Auto-generated method stub

	}

}
