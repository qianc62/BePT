package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import org.processmining.framework.models.hlprocess.gui.att.dist.HLBernoulliDistributionGui;

/**
 * Represents a bernoulli distribution.
 * 
 * @see HLBernoulliDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLBernoulliDistribution extends HLDistribution {

	// distribution attributes
	protected double myProbability;

	/**
	 * Creates a bernoulli distribution based on an probability value
	 * 
	 * @param probability
	 *            double the probability value. Note that this value always has
	 *            to be bigger or equal to 0 and equal or less than 1.
	 */
	public HLBernoulliDistribution(double probability) {
		myProbability = probability;
	}

	/**
	 * Returns the probability value for this distribution
	 * 
	 * @return double the probability value
	 */
	public double getProbability() {
		return myProbability;
	}

	/**
	 * Sets the probability value for this distribution
	 * 
	 * @param prob
	 *            the new probability value
	 */
	public void setProbability(double prob) {
		myProbability = prob;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #getDistributionType()
	 */
	public DistributionEnum getDistributionType() {
		return HLDistribution.DistributionEnum.BERNOULLI_DISTRIBUTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Bernoulli";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLBernoulliDistribution)
				&& (this.getProbability() == ((HLBernoulliDistribution) obj)
						.getProbability());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #hashCode()
	 */
	public int hashCode() {
		// simple recipe for generating hashCode given by
		// Effective Java (Addison-Wesley, 2001)
		int result = 17;
		long l = Double.doubleToLongBits(this.getProbability());
		int c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #writeDistributionToDot(java.lang.String, java.lang.String,
	 * java.lang.String, java.io.Writer)
	 */
	public void writeDistributionToDot(String boxId, String nodeId,
			String addText, Writer bw) throws IOException {
		// write the box itself
		String label = "";
		label = label + addText + "\\n";
		label = label + "Bernoulli Distribution\\n";
		label = label + "probability=" + myProbability + "\\n";
		bw.write(boxId + " [shape=\"ellipse\", label=\"" + label + "\"];\n");
		// write the connection (if needed)
		if (!nodeId.equals("")) {
			bw.write(nodeId + " -> " + boxId + " [dir=none, style=dotted];\n");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #timeMultiplicationValue(double)
	 */
	public void setTimeMultiplicationValue(double value) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		return false;
	}

}
