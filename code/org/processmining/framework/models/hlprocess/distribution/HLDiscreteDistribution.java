package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.processmining.framework.models.hlprocess.gui.att.dist.HLDiscreteDistributionGui;
import org.processmining.framework.ui.MainUI;

/**
 * Represents a discrete distribution. It is assumed that the min value that is
 * provided will be less than the max value that is provided.
 * 
 * @see HLDiscreteDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLDiscreteDistribution extends HLDistribution {

	// distribution attributes
	private int myMin;
	private int myMax;

	/**
	 * Creates a normal distribution based on a minimal and a maximal value.
	 * 
	 * @param min
	 *            the minumum value of the distribution
	 * @param max
	 *            the maximum value of the distribution
	 */
	public HLDiscreteDistribution(int min, int max) {
		myMin = min;
		myMax = max;
	}

	/**
	 * Returns the minimum value of this discrete distribution.
	 * 
	 * @return the minimum value of this discrete distribution.
	 */
	public int getMin() {
		return myMin;
	}

	/**
	 * Returns the maximum value of this discrete distribution.
	 * 
	 * @return the maximum value of this discrete distribution.
	 */
	public int getMax() {
		return myMax;
	}

	/**
	 * Sets the minimum value of this discrete distribution.
	 * 
	 * @param value
	 *            the minimum value of this discrete distribution.
	 */
	public void setMin(int value) {
		myMin = value;
	}

	/**
	 * Sest the maximum value of this discrete distribution.
	 * 
	 * @param value
	 *            the maximum value of this discrete distribution.
	 */
	public void setMax(int value) {
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
		return DistributionEnum.DISCRETE_DISTRIBUTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Discrete";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLDiscreteDistribution)
				&& (this.getMin() == ((HLDiscreteDistribution) obj).getMin())
				&& (this.getMax() == ((HLDiscreteDistribution) obj).getMax());
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
		result = 37 * result + this.getMin();
		result = 37 * result + this.getMax();
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
		label = label + "Discrete Distribution\\n";
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
		myMin = (int) Math.round(myMin * value);
		myMax = (int) Math.round(myMax * value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		if (this.getMax() == 0 || this.getMin() == 0) {
			JOptionPane.showMessageDialog(MainUI.getInstance().getDesktop(),
					"One or more parameters of the Distribution of " + info
							+ "has value zero", "0 value warning",
					JOptionPane.WARNING_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

}
