package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.processmining.framework.ui.MainUI;

/**
 * Represents a poisson distribution.
 * 
 * @see HLPoissonDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLPoissonDistribution extends HLDistribution {

	// distribution attributes
	private double myIntensity;

	/**
	 * Creates a negative exponential distribution based on an intensity value
	 * 
	 * @param intensity
	 *            double the intensity value. Remark that the intensity value
	 *            always needs to be equal or greater than 0.
	 */
	public HLPoissonDistribution(double intensity) {
		myIntensity = intensity;
	}

	/**
	 * Returns the intensity value of this negative exponential distribution
	 * 
	 * @return double the intensity value
	 */
	public double getIntensity() {
		return myIntensity;
	}

	/**
	 * Sets the intensity value of this negative exponential distribution
	 * 
	 * @param value
	 *            the intensity value
	 */
	public void setIntensity(double value) {
		myIntensity = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #getDistributionType()
	 */
	public DistributionEnum getDistributionType() {
		return HLDistribution.DistributionEnum.POISSON_DISTRIBUTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Poisson";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLPoissonDistribution)
				&& (this.getIntensity() == ((HLPoissonDistribution) obj)
						.getIntensity());
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
		long l = Double.doubleToLongBits(this.getIntensity());
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
		label = label + "Poisson Distribution\\n";
		label = label + "intensity=" + myIntensity + "\\n";
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
		myIntensity = myIntensity * (1 / value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		if (Math.round(this.getIntensity()) == 0) {
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
