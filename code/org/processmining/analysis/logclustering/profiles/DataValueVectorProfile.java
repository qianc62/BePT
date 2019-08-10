/**
 * 
 */
package org.processmining.analysis.logclustering.profiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

/**
 * Profile which measures the usage of attribute values, or data, used in all
 * traces.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class DataValueVectorProfile extends VectorProfile {

	protected DataValueVectorProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super("Data value profile", "Profile for attribute values", log
				.getLogSummary().getNumberOfProcessInstances(),
				(String[]) scanDataValues(log).toArray());
	}

	/**
	 * Helper method: scan for all data key/value combinations found in the log
	 * (to create indexed map)
	 */
	protected static Set<String> scanDataValues(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		HashSet<String> values = new HashSet<String>();
		for (ProcessInstance instance : log.getInstances()) {
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			for (int i = 0; i < ateList.size(); i++) {
				Map<String, String> ateAttributes = ateList.get(i)
						.getAttributes();
				for (String key : ateAttributes.keySet()) {
					values.add(convertAttributeToItem(key, ateAttributes
							.get(key)));
				}
			}
		}
		return values;
	}

	/**
	 * Helper method: convert an attribute key/value combination to a unique
	 * identifier string (used for indexing the map)
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	protected static String convertAttributeToItem(String key, String value) {
		return key + " == " + value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.analysis.logclustering.profiles.VectorProfile#buildProfile
	 * (int, org.processmining.framework.log.AuditTrailEntry)
	 */
	@Override
	protected void buildProfile(int traceIndex, AuditTrailEntry ate) {
		for (String key : ate.getAttributes().keySet()) {
			String item = convertAttributeToItem(key, ate.getAttributes().get(
					key));
			super.increaseItemBy(item, traceIndex, 1.0);
		}
	}

}
