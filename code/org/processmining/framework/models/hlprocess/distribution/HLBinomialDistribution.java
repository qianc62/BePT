package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import org.processmining.framework.models.hlprocess.gui.att.dist.HLBinomialDistributionGui;

/**
 * Represents a bernoulli distribution.
 * 
 * @see HLBinomialDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLBinomialDistribution extends HLDistribution implements Cloneable {

	// distribution attributes
	private int myNumberExperiments;
	private double myProbability;

	/**
	 * Creates a binomial distribution based on a number of experiments and a
	 * probability value
	 * 
	 * @param numberExperiments
	 *            the number of experiments. Note that this value always has to
	 *            bigger or equal to 1.
	 * @param probability
	 *            the probability value. Note that this value always has to be
	 *            bigger or equal to 0 and equal or less than 1.
	 */
	public HLBinomialDistribution(int numberExperiments, double probability) {
		myNumberExperiments = numberExperiments;
		myProbability = probability;
	}

	/**
	 * Retrieves the number of experiments for this distribution.
	 * 
	 * @return double the number of experiments
	 */
	public int getNumberOfExperiments() {
		return myNumberExperiments;
	}

	/**
	 * Retrieves the probability value of this distribution.
	 * 
	 * @return double the probability value
	 */
	public double getProbability() {
		return myProbability;
	}

	/**
	 * Sets the number of experiments for this distribution.
	 * 
	 * @param value
	 *            the number of experiments
	 */
	public void setNumberOfExperiments(int value) {
		myNumberExperiments = value;
	}

	/**
	 * Sets the probability value of this distribution.
	 * 
	 * @param the
	 *            probability value
	 */
	public void setProbability(double value) {
		myProbability = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #getDistributionType()
	 */
	public DistributionEnum getDistributionType() {
		return HLDistribution.DistributionEnum.BINOMIAL_DISTRIBUTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Binomial";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLBinomialDistribution)
				&& (this.getNumberOfExperiments() == ((HLBinomialDistribution) obj)
						.getNumberOfExperiments())
				&& (this.getProbability() == ((HLBinomialDistribution) obj)
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

		result = 37 * result + this.getNumberOfExperiments();

		int c = 0;
		long l = Double.doubleToLongBits(this.getProbability());
		c = (int) (l ^ (l >>> 32));
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
		label = label + "Binomial Distribution\\n";
		label = label + "number experiments=" + myNumberExperiments + "\\n";
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
