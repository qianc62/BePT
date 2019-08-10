package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JOptionPane;

import org.processmining.framework.models.hlprocess.gui.att.dist.HLConstantDistributionGui;
import org.processmining.framework.ui.MainUI;

/**
 * Represents a constant distribution.
 * 
 * @see HLConstantDistributionGui
 * 
 * @author arozinat
 * @author rmans
 */
public class HLConstantDistribution extends HLDistribution implements Cloneable {

	// distribution attributes
	protected double myConstant;

	/**
	 * Creates a constant distribution based on a value for the constant.
	 * 
	 * @param constant
	 *            the constant value of the distribution
	 */
	public HLConstantDistribution(double constant) {
		myConstant = constant;
	}

	/**
	 * Returns the constant value of this constant distribution.
	 * 
	 * @return the constant value
	 */
	public double getConstant() {
		return myConstant;
	}

	/**
	 * Sets the constant value of this distribution.
	 * 
	 * @param value
	 *            the constant value
	 */
	public void setConstant(double value) {
		myConstant = value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.Distribution#getDistributionType
	 * ()
	 */
	public DistributionEnum getDistributionType() {
		return DistributionEnum.CONSTANT_DISTRIBUTION;
	}

	/**
	 * The string representation for this constant distribution.
	 * 
	 * @return String the string representation.
	 */
	public String toString() {
		return "Constant";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLConstantDistribution)
				&& (this.getConstant() == ((HLConstantDistribution) obj)
						.getConstant());
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
		long l = Double.doubleToLongBits(this.getConstant());
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
		label = label + "Constant Distribution\\n";
		label = label + "constant=" + myConstant + "\\n";
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
		myConstant = myConstant * value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #checkValuesOfTimeParameters(java.lang.String)
	 */
	public boolean checkValuesOfTimeParameters(String info) {
		if (Math.round(this.getConstant()) == 0) {
			JOptionPane.showMessageDialog(MainUI.getInstance().getDesktop(),
					"One or more parameters of the Distribution of " + info
							+ " has a value that will be rounded to zero",
					"Value rounded to zero warning",
					JOptionPane.WARNING_MESSAGE);
			return true;
		} else {
			return false;
		}
	}

}
