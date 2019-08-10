package org.processmining.analysis.traceclustering.preprocessor;

import org.processmining.analysis.traceclustering.profile.AbstractProfile;
import org.processmining.analysis.traceclustering.profile.AggregateProfile;
import org.processmining.framework.log.LogReader;

public abstract class AbstractPreProcessor extends AbstractProfile {

	public AbstractPreProcessor(String aName, String aDescription,
			LogReader aLog) {
		super(aName, aDescription, aLog);
	}

	public abstract void buildProfile(AggregateProfile aggregateProfile);

	public abstract void buildProfile(AggregateProfile aggregateProfile, int dim);
}
