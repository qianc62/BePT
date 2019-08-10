package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;

/**
 * This abstract class represents some distribution of a set of numerical
 * values. Subclasses may implement concrete distributions, such as a normal or
 * a uniform distribution.
 * 
 * @author arozinat
 * @author rmans
 */
public abstract class HLDistribution implements Cloneable, java.io.Serializable {

	/**
	 * Defines which kinds of distributions are available.
	 */
	public enum DistributionEnum {
		NORMAL_DISTRIBUTION("Normal Distribution"), UNIFORM_DISTRIBUTION(
				"Uniform Distribution"), CONSTANT_DISTRIBUTION(
				"Constant Distribution"), DISCRETE_DISTRIBUTION(
				"Discrete Distribution"), EXPONENTIAL_DISTRIBUTION(
				"Exponential Distribution"), BERNOULLI_DISTRIBUTION(
				"Bernoulli Distribution"), BINOMIAL_DISTRIBUTION(
				"Binomial Distribution"), CHISQUARE_DISTRIBUTION(
				"Chisquare Distribution"), ERLANG_DISTRIBUTION(
				"Erlang Distribution"), POISSON_DISTRIBUTION(
				"Poisson Distribution"), STUDENT_DISTRIBUTION(
				"Student Distribution");

		private final String distributionName;

		private DistributionEnum(String distrName) {
			distributionName = distrName;
		}

		public String distributionName() {
			return distributionName();
		}

		public String toString() {
			return distributionName;
		}
	}

	/**
	 * Indicates the type of this distribution.
	 * 
	 * @return the type of this distribution
	 */
	public abstract DistributionEnum getDistributionType();

	/**
	 * Implementing classes have to override the clone method
	 * 
	 * @return Object
	 */
	public Object clone() {
		HLDistribution o = null;
		try {
			o = (HLDistribution) super.clone();
			return o;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Determines when two distribution objects are the same. Two Distributions
	 * are the same when they are both of the same object type and that the
	 * values for all the parameters of the particular distribution are the
	 * same.
	 * 
	 * @param obj
	 *            Object the object to compare with
	 * @return boolean <code>true</code> when the two distribution objects are
	 *         the same object and that the values for all the parameters of the
	 *         particalur distribution are the same. <code>false</code>
	 *         otherwise
	 */
	public abstract boolean equals(Object obj);

	/**
	 * Retrieves the hashcode for the object. The hashcode value for a
	 * Distribution object is calculated according to the recipe of Josha Bloch
	 * in <i>Effective Java</i> (Addison-Wesley 2001). For this recipe it is
	 * only allowed to consider the fields that correspond with the parameter(s)
	 * of the particular distribution.
	 * 
	 * @return int the hashcode value for the Distribution object.
	 */
	public abstract int hashCode();

	/**
	 * Writes the distribution to dot. The general idea is that the parameters
	 * and other relevant information of the distribution are written in a box
	 * and if needed can be connected to another node in the dot file. In that
	 * case the connection has to be an undirected line.
	 * 
	 * @param boxId
	 *            the identifier of the box (in the DOT file) in which the
	 *            parameters and other relevant information of the distribution
	 *            will be written.
	 * @param nodeId
	 *            the identifier of the node (in the DOT file) to which the box
	 *            that will be created has to be connected. <code>""</code> has
	 *            to be provided if the box that will be created does not need
	 *            to be connected to another node in the DOT file.
	 * @param addText
	 *            additional text that needs to be filled in at the beginning of
	 *            the box
	 * @param bw
	 *            Writer the BufferedWriter used to stream the data to the file
	 * @throws IOException
	 */
	public abstract void writeDistributionToDot(String boxId, String nodeId,
			String addText, Writer bw) throws IOException;

	/**
	 * Defines the factor with which the values in the distribution that refer
	 * to time need to be multiplicated with
	 * 
	 * @param multiplicationValue
	 *            double
	 */
	public abstract void setTimeMultiplicationValue(double multiplicationValue);

	/**
	 * Check whether parameters, which can be related to time, in the
	 * Distribution will get value '0' when these are rounded. When such a
	 * parameter gets value '0' after rounding, a warning needs to be issued.
	 * Furthermore, when a warning is raised the value 'true' needs to be
	 * returned
	 * 
	 * @param info
	 *            Additional information to be displayed when a warning is
	 *            issued.
	 * @return boolean the value true needs to be returned when a warning has
	 *         been issued by the method, otherwise false.
	 */
	public abstract boolean checkValuesOfTimeParameters(String info);
}
