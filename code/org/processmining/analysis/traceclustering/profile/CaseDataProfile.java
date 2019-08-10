package org.processmining.analysis.traceclustering.profile;

import java.util.Map;

import org.processmining.framework.log.LogReader;

public class CaseDataProfile extends AbstractProfile {

	public CaseDataProfile(LogReader log) {
		super("Case data", "Compares the attributes of cases", log);
		buildProfile(log);
	}

	protected void buildProfile(LogReader log) {
		for (int c = 0; c < log.numberOfInstances(); c++) {
			Map<String, String> attributes = log.getInstance(c).getAttributes();
			for (String key : attributes.keySet()) {
				// add both key and key/value combination as items
				String item = key + "==" + attributes.get(key);
				incrementValue(c, key, 1.0);
				incrementValue(c, item, 1.0);
			}
		}
		// disable if no more than one attributes present in log
		if (this.getItemKeys().size() <= 1) {
			this.setNormalizationMaximum(0.0);
		}
	}

}
