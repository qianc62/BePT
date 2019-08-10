package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.processmining.framework.ui.MainUI;

/**
 * Represents a uniform distribution. It is assumed that the min value that is
 * provided will be less than the max value that is provided.
 * 
 * @see HLUniformDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLUniformDistribution extends HLDistribution {

	// distribution attributes
	// TODO: check how to ensure that min is always smaller than max
	// (if they can be manipulated in the GUI)
	private double myMin;
	private double myMax;

	/**
	 * Creates a normal distribution based on a minimal and a maximal value.
	 * 
	 * @param min
	 *            the minumum value of the distribution
	 * @param max
	 *            the maximum value of the distribution
	 */
	public HLUniformDistribution(double min, double max) {
		myMin = min;
		myMax = max;
	}

	/**
	 * Returns the minimum value of this uniform distribution.
	 * 
	 * @return the minimum value
	 */
	public double getMin() {
		return myMin;
	}

	/**
	 * Returns the maximum value of this uniform distribution.
	 * 
	 * @return the maximum value
	 */
	public double getMax() {
		return myMax;
	}

	/**
	 * Sets the minimum value of this uniform distribution.
	 * 
	 * @param value
	 *            the minimum value
	 */
	public void setMin(double value) {
		myMin = value;
	}

	/**
	 * Sets the maximum value of this uniform distribution.
	 * 
	 * @param value
	 *            the maximum value
	 */
	public void setMax(double value) {
		myMax = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.Distribution#getDistributionType
	 * ()
	 */
	public DistributionEnum getDistributionType() {
		return DistributionEnum.UNIFORM_DISTRIBUTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Uniform";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLUniformDistribution)
				&& (this.getMin() == ((HLUniformDistribution) obj).getMin())
				&& (this.getMax() == ((HLUniformDistribution) obj).getMax());
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

		long l = Double.doubleToLongBits(this.getMin());
		int c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		long k = Double.doubleToLongBits(this.getMax());
		int d = (int) (l ^ (l >>> 32));
		result = 37 * result + d;

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
		label = label + "Uniform Distribution\\n";
		label = label + "max=" + myMax + "\\n";
		label = label + "min=" + myMin + "\\n";
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
		myMin = myMin * value;
		myMax = myMax * value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		if (Math.round(this.getMax()) == 0 || Math.round(this.getMin()) == 0) {
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
