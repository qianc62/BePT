package org.processmining.analysis.traceclustering.preprocessor;

import java.io.IOException;

import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.log.LogReader;

public class TFIDFProfile extends AbstractPreProcessor {

	public TFIDFProfile(LogReader log) throws IndexOutOfBoundsException,
			IOException {
		super("TF.IDF", "Apply TF.IDF to the profiles", log);
	}

	public void buildProfile(AggregateProfile aggregateProfile) {
		for (String key : aggregateProfile.getItemKeys()) {
			int freq = 0;
			for (int c = 0; c < log.numberOfInstances(); c++) {
				if (aggregateProfile.getValue(c, key) > 0.0)
					freq++;
			}

			double idf = Math.log10((double) log.numberOfInstances()
					/ (double) freq);
			for (int c = 0; c < log.numberOfInstances(); c++) {
				incrementValue(c, key, aggregateProfile.getValue(c, key) * idf);
			}
			this.setNormalizationMaximum(1.0);
		}
	}

	public void buildProfile(AggregateProfile aggregateProfile, int dim) {
		this.buildProfile(aggregateProfile);
	}
}
