package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.processmining.framework.ui.MainUI;

/**
 * Represents a normal distribution.
 * 
 * @see HLNormalDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLNormalDistribution extends HLDistribution {

	// distribution attributes
	private double myMean;
	private double myVariance;

	/**
	 * Creates a normal distribution based on a mean and a variance.
	 * 
	 * @param mean
	 *            the (arithmetic) mean value of the distribution
	 * @param variance
	 *            the variance of the distribution
	 */
	public HLNormalDistribution(double mean, double variance) {
		myMean = mean;
		myVariance = variance;
	}

	/**
	 * Returns the mean value of this normal distribution.
	 * 
	 * @return the mean value
	 */
	public double getMean() {
		return myMean;
	}

	/**
	 * Returns the variance of this normal distribution.
	 * 
	 * @return the variance
	 */
	public double getVariance() {
		return myVariance;
	}

	/**
	 * Sets the mean value of this normal distribution.
	 * 
	 * @param value
	 *            the mean value
	 */
	public void setMean(double value) {
		myMean = value;
	}

	/**
	 * Sets the variance of this normal distribution.
	 * 
	 * @param value
	 *            the variance
	 */
	public void setVariance(double value) {
		myVariance = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.Distribution#getDistributionType
	 * ()
	 */
	public DistributionEnum getDistributionType() {
		return DistributionEnum.NORMAL_DISTRIBUTION;
	}

	/**
	 * The string representation for this distribution.
	 * 
	 * @return String the string representation.
	 */
	public String toString() {
		return "Normal";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLNormalDistribution)
				&& (this.getMean() == ((HLNormalDistribution) obj).getMean())
				&& (this.getVariance() == ((HLNormalDistribution) obj)
						.getVariance());
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

		long l = Double.doubleToLongBits(this.getMean());
		int c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		long k = Double.doubleToLongBits(this.getVariance());
		int d = (int) (l ^ (l >>> 32));
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
		label = label + "General Distribution\\n";
		label = label + "mean=" + myMean + "\\n";
		label = label + "variance=" + myVariance + "\\n";
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
		myMean = myMean * value;
		myVariance = myVariance * value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		if (Math.round(this.getMean()) == 0
				|| Math.round(this.getVariance()) == 0) {
			JOptionPane.showMessageDialog(MainUI.getInstance().getDesktop(),
					"One or more parameters of the Distribution of " + info
							+ "has a value that will be rounded to zero",
					"Value rounded to zero warning",
					JOptionPane.WARNING_MESSAGE);
			return true;
		} else {
			return false;
		}

	}

}
