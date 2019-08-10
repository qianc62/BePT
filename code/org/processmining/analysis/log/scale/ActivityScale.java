/**
 *
 */
package org.processmining.analysis.log.scale;

import org.processmining.framework.log.ProcessInstance;
import org.processmining.framework.log.AuditTrailEntry;
import java.util.Iterator;
import org.processmining.framework.log.LogStateMachine;
import org.processmining.framework.log.LogReader;

/**
 * This class weighs a process instance based on the contained number of audit
 * trail entries.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 * 
 */
public class ActivityScale implements ProcessInstanceScale {

	protected boolean positivelyCorrelated;

	/**
	 * Creates a new scale instance which is negatively correlated, i.e. the
	 * weight will decrease with the length of the weighed process instance.
	 */
	public ActivityScale() {
		this(true);
	}

	/**
	 * Creates a new scale instance
	 * 
	 * @param isPositivelyCorrelated
	 *            Whether the scale is positively correlated to the number of
	 *            events contained. I.e., if this flag is set to
	 *            <code>true</code>, longer instances will yield a higher
	 *            weight. If this flag is set to <code>false</code> the weight
	 *            will decrease with the length of the weighed process instance.
	 */
	public ActivityScale(boolean isPositivelyCorrelated) {
		this.positivelyCorrelated = isPositivelyCorrelated;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.processmining.analysis.log.scale.ProcessInstanceScale#weigh(org.
	 * processmining.framework.log.LogReader, int)
	 */
	public double weigh(ProcessInstance anInstance) {

		double count = 0;
		Iterator it = anInstance.getAuditTrailEntryList().iterator();
		for (int i = 0; i < anInstance.getAuditTrailEntryList().size(); i++) {
			AuditTrailEntry ate = (AuditTrailEntry) it.next();
			if (ate.getElement().startsWith("Drink")
					&& ate.getType().equals(LogStateMachine.COMPLETE)) {
				count += 1.0;
			}

		}
		if (positivelyCorrelated == true) {
			return count;
		} else {
			return (1.0 / count);
		}
	}

	public String getName() {
		return "Number `Drink'-events scale";
	}

	public String getHtmlDescription() {
		return "Returns the number `Drink' ates in the process instance.";
	}

	public void initializeScale(LogReader log) {
		// TODO Auto-generated method stub

	}

	public void updateScale(ProcessInstance instance, LogReader log) {
		// TODO Auto-generated method stub

	}

}
