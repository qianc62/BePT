package org.processmining.analysis.hmm;

import java.util.Map;

import org.processmining.framework.models.petrinet.PetriNet;

public class TransitionNoiseGenerator extends HmmNoiseGenerator {

	/**
	 * Creates a noise generator with the given parameters.
	 * 
	 * @param anHmm
	 *            the hmm to be used as a basis for generating the noisy logs
	 * @param noNoiseLevels
	 *            the number of noise levels (without the 0% level). Should be
	 *            between 1 and 100
	 * @param noTraces
	 *            the number of traces that should be generated for each of the
	 *            noise levels
	 * @param traceLength
	 *            the maximum trace length per generated instance (in the
	 *            presence of loops might be otherwise infinite)
	 * @param eventMapping
	 *            the observation element mapping matching the given hmm
	 */
	public TransitionNoiseGenerator(Map<String, PetriNet> models,
			HmmExpConfiguration aConf) {
		super(models, aConf);
	}

	public String getNoisyLogFolder() {
		return HmmExpUtils.noiseLogFolder + "/" + "TransitionNoise";
	}

	public String getGeneratorHmmFolder() {
		return HmmExpUtils.noiseHmmFolder + "/" + "TransitionNoise";
	}

	/**
	 * Adjusts the Hmm to the current noise level.
	 * 
	 * @param the
	 *            noise interval to which the hmm is adjusted (if there are 3
	 *            noise levels, then there is interval 1, 2, and 3 - which
	 *            corresponds to noise level 33.33%, 66.66%, and 100%)
	 */
	protected void adjustHmm(int noiseInterval) {
		// calculate probabilities to generate correct and wrong symbols on this
		// noise level
		double delta = (1.0 / (double) conf.getNoiseLevels());
		double correct = 1.0 - ((double) noiseInterval * delta);
		double incorrect = (1.0 - correct);
		// final state observation probability is changed nowhere
		for (int i = 0; i < observationMapping.size(); i++) {
			// count the number of positive transitions in input Hmm
			int posOutTrans = 0;
			for (int j = 0; j < observationMapping.size() + 1; j++) {
				if (noiseFreeHmm.getAij(i, j) > 0) {
					posOutTrans++;
				}
			}
			double prob;
			for (int j = 0; j < observationMapping.size() + 1; j++) {
				if (noiseFreeHmm.getAij(i, j) > 0) {
					prob = correct / (double) posOutTrans;
				} else {
					prob = incorrect
							/ ((double) observationMapping.size() - posOutTrans + 1);
				}
				noisyHmm.setAij(i, j, prob);
			}
		}
	}

}
