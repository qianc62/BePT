package org.processmining.analysis.hmm;

import java.util.Map;

import org.processmining.framework.models.petrinet.PetriNet;

public class TransitionNoiseEvaluator extends HmmNoiseEvaluator {

	public TransitionNoiseEvaluator(Map<String, PetriNet> models,
			HmmExpConfiguration aConf) {
		super(models, aConf);
	}

	protected String getEvaluationFolder() {
		return HmmExpUtils.noiseEvalFolder + "/" + "TransitionNoise";
	}

	protected String getNoisyLogFolder() {
		return HmmExpUtils.noiseLogFolder + "/" + "TransitionNoise";
	}
}
