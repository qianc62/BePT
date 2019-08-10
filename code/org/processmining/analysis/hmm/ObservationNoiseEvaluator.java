package org.processmining.analysis.hmm;

import java.util.Map;

import org.processmining.framework.models.petrinet.PetriNet;

public class ObservationNoiseEvaluator extends HmmNoiseEvaluator {

	public ObservationNoiseEvaluator(Map<String, PetriNet> models,
			HmmExpConfiguration aConf) {
		super(models, aConf);
	}

	protected String getEvaluationFolder() {
		return HmmExpUtils.noiseEvalFolder + "/" + "ObservationNoise";
	}

	protected String getNoisyLogFolder() {
		return HmmExpUtils.noiseLogFolder + "/" + "ObservationNoise";
	}
}
