/**
 * 
 */
package org.processmining.analysis.logclustering.profiles;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.processmining.framework.log.AuditTrailEntry;
import org.processmining.framework.log.AuditTrailEntryList;
import org.processmining.framework.log.LogReader;
import org.processmining.framework.log.ProcessInstance;

/**
 * Profile which measures the usage of attribute keys, or data types, used in
 * all traces.
 * 
 * @author Christian W. Guenther (christian@deckfour.org)
 */
public class DataTypeVectorProfile extends VectorProfile {

	public DataTypeVectorProfile(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		super("Data type profile", "Profile for attribute keys", log
				.getLogSummary().getNumberOfProcessInstances(),
				(String[]) scanNumberOfDataTypes(log).toArray());
	}

	/**
	 * Helper method; scans a log for the set of observed data types, i.e.
	 * attribute keys.
	 * 
	 * @param log
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws IOException
	 */
	static protected Set<String> scanNumberOfDataTypes(LogReader log)
			throws IndexOutOfBoundsException, IOException {
		HashSet<String> attributeKeys = new HashSet<String>();
		for (ProcessInstance instance : log.getInstances()) {
			AuditTrailEntryList ateList = instance.getAuditTrailEntryList();
			for (int i = 0; i < ateList.size(); i++) {
				attributeKeys.addAll(ateList.get(i).getAttributes().keySet());
			}
		}
		return attributeKeys;
	}

	@Override
	protected void buildProfile(int traceIndex, AuditTrailEntry ate) {
		for (String key : ate.getAttributes().keySet()) {
			super.increaseItemBy(key, traceIndex, 1.0);
		}

	}

}
