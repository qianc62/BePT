package org.processmining.framework.models.hlprocess.distribution;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.processmining.framework.ui.MainUI;
import org.processmining.framework.ui.Message;

/**
 * Represents soke kind of meta distribution to which a lot of distribution
 * related information can be provided, so that based on the provided
 * information to this object separate distributions (like a
 * <code>HLUniformDistribution</code> object or a
 * <code>HLNormalDistribution</code>) can be provided. If no specific value for
 * some distribution related attribute has been set, a default value will be
 * used (e.g. when no value for variance is given, as value <code>0</code> will
 * be used). For setting the best distribution, an instance of
 * HLGeneralDistribution.DistributionEnum has to be chosen.
 * 
 * <p>
 * Note that the validity of provided values is not checked. For example, it is
 * assumed that if a min and a max value is provided, that the min value is less
 * than the max value.
 * 
 * @author arozinat
 * @author rmans
 */
public class HLGeneralDistribution extends HLDistribution {

	// distribution attributes
	private double myConstant;
	private double myMean;
	private double myVariance;
	private double myMin;
	private double myMax;
	private double myIntensity;
	private double myProbability;
	private int myNumberExperiments;
	private int myDegreesFreedom;
	private int myEmergenceEvents;

	private DistributionEnum myBestDistribution = DistributionEnum.CONSTANT_DISTRIBUTION;

	/**
	 * Creates a general distribution based on values for the mean, variance,
	 * min, max and the bestDistribution
	 * 
	 * @param constant
	 *            the constant value of the distribution
	 * @param mean
	 *            double (arithmetic) mean value of the distribution
	 * @param variance
	 *            double the variance of the distribution
	 * @param min
	 *            the minumum value of the distribution
	 * @param max
	 *            the maximum value of the distribution
	 * @param probability
	 *            the probability value. Note that this value always has to be
	 *            bigger or equal to 0 and equal or less than 1.
	 * @param numberExperiments
	 *            the number of experiments. Note that this value always has to
	 *            bigger or equal to 1.
	 * @param degreesFreedom
	 *            double the degrees of freedom value. Not that this value
	 *            always has to be equal or greater to 1.
	 * @param emergenceEvents
	 *            Emergence of <it>emergenceEvents</it> events. Note that the
	 *            number of events always has to be equal or greater than 1.
	 * @param intensity
	 *            the intensity value of the distribution
	 * @param bestDistribution
	 *            the best distribution. Based on the values that are provided
	 */
	public HLGeneralDistribution(double constant, double mean, double variance,
			double min, double max, double intensity, double probability,
			int numberExperiments, int degreesFreedom, int emergenceEvents) {
		if (mean == 0 && intensity != 0)
			mean = 1 / intensity;
		if (mean != 0 && intensity == 0)
			intensity = 1 / mean;
		if (mean == 0 && intensity == 0)
			intensity = Double.MAX_VALUE;
		if (probability == -1.0)
			probability = 0.5;
		if (numberExperiments == -1) {
			if (probability != 0.0 && (int) (mean / probability) >= 1)
				numberExperiments = (int) (mean / probability);
			else
				numberExperiments = 1;
		}
		if (emergenceEvents == -1 && (int) (mean * intensity) >= 1)
			emergenceEvents = (int) (mean * intensity);
		else
			emergenceEvents = 1;

		myConstant = constant;
		myMean = mean;
		myVariance = variance;
		myMin = min;
		myMax = max;
		myIntensity = intensity;
		myProbability = probability;
		myNumberExperiments = numberExperiments;
		myDegreesFreedom = degreesFreedom;
		myEmergenceEvents = emergenceEvents;
	}

	/**
	 * Creates a general distribution with default values.
	 */
	public HLGeneralDistribution() {
		this(0, 0, 0, 0, 0, 1, 1, 1, 1, 1);
	}

	/**
	 * Creates a general distribution with default values for some of the values
	 * in the generaldistribution
	 * 
	 * @param constant
	 *            the constant value of the distribution
	 * @param mean
	 *            double (arithmetic) mean value of the distribution
	 * @param variance
	 *            double the variance of the distribution
	 * @param min
	 *            the minumum value of the distribution
	 * @param max
	 *            the maximum value of the distribution
	 * @param bestDistribution
	 *            the best distribution. Based on the values that are provided
	 */
	public HLGeneralDistribution(double constant, double mean, double variance,
			double min, double max) {
		this(constant, mean, variance, min, max, 0, -1.0, -1, -1, -1);
	}

	/**
	 * Creates a general distribution with default values for some of the values
	 * in the generaldistribution
	 * 
	 * @param constant
	 *            double the constant value of the distribution
	 * @param intensity
	 *            double the intensity value of the distribution
	 * @param bestDistribution
	 *            DistributionEnum the best distribution. Based on the values
	 *            that are provided.
	 */
	public HLGeneralDistribution(double constant, double intensity,
			DistributionEnum bestDistribution) {
		this(constant, 0.0, 0.0, 0.0, 0.0, intensity, -1.0, -1, -1, -1);
	}

	/**
	 * Creates a general distribution with default values for some of the values
	 * in the generaldistribution
	 * 
	 * @param constant
	 *            double the constant value of the distribution
	 * @param mean
	 *            double the mean value of the distribution
	 * @param min
	 *            double the min value of the distribution
	 * @param max
	 *            double the max value of the distribution
	 * @param variance
	 *            double the variance value of the distribution
	 * @param intensity
	 *            double the intensity value of the distribution
	 * @param bestDistribution
	 *            DistributionEnum the best distribution. Based on the values
	 *            that are provided.
	 */
	public HLGeneralDistribution(double constant, double mean, double variance,
			double min, double max, double intensity,
			DistributionEnum bestDistribution) {
		this(constant, mean, variance, min, max, intensity, -1.0, -1, -1, -1);
	}

	/**
	 * Sets the best distribution.
	 * 
	 * @param bestDistribution
	 *            DistributionEnum the best distribution
	 */
	public void setBestDistributionType(DistributionEnum bestDistribution) {
		myBestDistribution = bestDistribution;
	}

	/**
	 * Returns the number of the best distribution that has been set.
	 * 
	 * @return DistributionEnum the number of the best distribution
	 */
	public DistributionEnum getBestDistributionType() {
		return myBestDistribution;
	}

	/**
	 * Returns the name of the best distribution that has been set.
	 * 
	 * @return String the name of the best distribution.
	 */
	public String getBestDistributionString() {
		return this.getBestDistributionType().distributionName();
	}

	/**
	 * Sets the constant of the general distribution
	 * 
	 * @param constant
	 *            double the constant value
	 */
	public void setConstant(double constant) {
		myConstant = constant;
	}

	/**
	 * Returns the constant of the general distribution
	 * 
	 * @return double the constant value
	 */
	public double getConstant() {
		return myConstant;
	}

	/**
	 * Sets the mean value of this general distribution
	 * 
	 * @param mean
	 *            double the mean value
	 */
	public void setMean(double mean) {
		myMean = mean;
		if (myMean == 0) {
			myIntensity = Double.MAX_VALUE;
			myMean = 1 / myIntensity;
		} else {
			myIntensity = 1 / myMean;
		}
	}

	/**
	 * Returns the mean value of the general distribution.
	 * 
	 * @return double the mean value
	 */
	public double getMean() {
		return myMean;
	}

	/**
	 * Sets the variance of the general distribution.
	 * 
	 * @param variance
	 *            double the variance.
	 */
	public void setVariance(double variance) {
		myVariance = variance;
	}

	/**
	 * Returns the variance of the general distribution.
	 * 
	 * @return double the variance.
	 */
	public double getVariance() {
		return myVariance;
	}

	/**
	 * Sets the max of the general distribution
	 * 
	 * @param max
	 *            double the max value. Note that the max value always has to be
	 *            bigger or equal to the min value (if it has been provided).
	 */
	public void setMax(double max) {
		myMax = max;
	}

	/**
	 * Returns the max value of the general distribution
	 * 
	 * @return double the max value
	 */
	public double getMax() {
		return myMax;
	}

	/**
	 * Sets the min of the general distribution
	 * 
	 * @param min
	 *            double the min value. Note that the min value always has to be
	 *            less or equal than the max value (if it has been provided).
	 */
	public void setMin(double min) {
		myMin = min;
	}

	/**
	 * Returns the min of the general distribution
	 * 
	 * @return double the min value
	 */
	public double getMin() {
		return myMin;
	}

	/**
	 * Sets the intensity of the general distribution
	 * 
	 * @param intensity
	 *            double the intensity value. Note that this value always has to
	 *            be bigger or equal to 1.
	 */
	public void setIntensity(double intensity) {
		myIntensity = intensity;
		if (myIntensity == 0) {
			myMean = Double.MAX_VALUE;
			myIntensity = 1 / myMean;
		} else {
			myMean = 1 / myIntensity;
		}
	}

	/**
	 * Returns the intensity of the general distribution
	 * 
	 * @return double the intensity value.
	 */
	public double getIntensity() {
		return myIntensity;
	}

	/**
	 * Sets the probability of the general distribution.
	 * 
	 * @param probability
	 *            double the probability value. Note that this value always has
	 *            to be bigger or equal to 0 and less than or equal to 1.
	 */
	public void setProbability(double probability) {
		myProbability = probability;
	}

	/**
	 * Returns the probability value of the general distribution.
	 * 
	 * @return double the probability value.
	 */
	public double getProbability() {
		return myProbability;
	}

	/**
	 * Sets the number of experiments for the general distribution
	 * 
	 * @param numberExperiments
	 *            int the number of experiments. Note that this value always has
	 *            to be bigger or equal to 1.
	 */
	public void setNumberExperiments(int numberExperiments) {
		myNumberExperiments = numberExperiments;
	}

	/**
	 * Returns the number of experiments of the general distribution
	 * 
	 * @return int the number of experiments.
	 */
	public int getNumberExperiments() {
		return myNumberExperiments;
	}

	/**
	 * Sets the degrees of freedom for the general distribution
	 * 
	 * @param degreesFreedom
	 *            int the degrees of freedom. Note that this value always has to
	 *            be bigger or equal to 1.
	 */
	public void setDegreesOfFreedom(int degreesFreedom) {
		myDegreesFreedom = degreesFreedom;
	}

	/**
	 * Returns the degrees of freedom of the general distribution
	 * 
	 * @return int the degrees of freedom
	 */
	public int getDegreesOfFreedom() {
		return myDegreesFreedom;
	}

	/**
	 * Sets the emergence of events for the general distribution
	 * 
	 * @param emergenceEvents
	 *            int the emergence of <it>emergenceEvents</it> events. Note
	 *            that this value always has to be bigger or equal to 1.
	 */
	public void setEmergenceOfEvents(int emergenceEvents) {
		myEmergenceEvents = emergenceEvents;
	}

	/**
	 * Returns the number of emergence of events of the general distribution
	 * 
	 * @return int the number of emergence of events.
	 */
	public int getEmergenceofEvents() {
		return myEmergenceEvents;
	}

	/**
	 * Returns a list of available distributions.
	 * <p>
	 * Note that does not include bernoulli, chisquare, poisson, and student
	 * although they are available.
	 * 
	 * @return the available distributions
	 */
	public ArrayList<DistributionEnum> getAvailableDistributions() {
		ArrayList<DistributionEnum> allDist = new ArrayList<DistributionEnum>();
		allDist.add(DistributionEnum.BERNOULLI_DISTRIBUTION);
		allDist.add(DistributionEnum.BINOMIAL_DISTRIBUTION);
		allDist.add(DistributionEnum.CONSTANT_DISTRIBUTION);
		allDist.add(DistributionEnum.DISCRETE_DISTRIBUTION);
		allDist.add(DistributionEnum.ERLANG_DISTRIBUTION);
		allDist.add(DistributionEnum.EXPONENTIAL_DISTRIBUTION);
		allDist.add(DistributionEnum.NORMAL_DISTRIBUTION);
		allDist.add(DistributionEnum.UNIFORM_DISTRIBUTION);
		return allDist;
	}

	/**
	 * Returns a <code>HLUniformDistribution</code> object. If no specific value
	 * has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLUniformDistribution a <code>HLUniformDistribution</code>
	 *         object.
	 */
	public HLUniformDistribution getUniformDistribution() {
		return new HLUniformDistribution(myMin, myMax);
	}

	/**
	 * Returns a <code>HLNormalDistribution</code> object. If no specific value
	 * has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLNormalDistribution a <code>HLNormalDistribution</code> object.
	 */
	public HLNormalDistribution getNormalDistribution() {
		return new HLNormalDistribution(myMean, myVariance);
	}

	/**
	 * Returns a <code>HLConstantDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLConstantDistribution a <code>HLConstantDistribution</code>
	 *         object.
	 */
	public HLConstantDistribution getConstantDistribution() {
		return new HLConstantDistribution(myConstant);
	}

	/**
	 * Returns a <code>HLExponentialDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLExponentialDistribution a
	 *         <code>HLExponentialDistribution</code> object.
	 */
	public HLExponentialDistribution getExponentialDistribution() {
		return new HLExponentialDistribution(myIntensity);
	}

	/**
	 * Returns a <code>HLBernoulliDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLBernoulliDistribution a <code>HLBernoulliDistribution</code>
	 *         object.
	 */
	public HLBernoulliDistribution getBernoulliDistribution() {
		return new HLBernoulliDistribution(myProbability);
	}

	/**
	 * Returns a <code>HLBinomialDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLBinomialDistribution a <code>HLBinomialDistribution</code>
	 *         object.
	 */
	public HLBinomialDistribution getBinomialDistribution() {
		return new HLBinomialDistribution(myNumberExperiments, myProbability);
	}

	/**
	 * Returns a <code>HLChiSquareDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLChiSquareDistribution a <code>HLChiSquareDistribution</code>
	 *         object.
	 */
	public HLChiSquareDistribution getChiSquareDistribution() {
		return new HLChiSquareDistribution(myDegreesFreedom);
	}

	/**
	 * Returns a <code>HLDiscreteDistribution</code> object. If no specific
	 * value has been set for some attribute specifically related to this
	 * distribution, a default value will be used. Note that for this
	 * distribution the min and max value of the general distribution are
	 * rounded to the closest int, because the min and max value that are
	 * supplied to the general distribution are of type double.
	 * 
	 * @return HLDiscreteDistribution a <code>HLDiscreteDistribution</code>
	 *         object.
	 */
	public HLDiscreteDistribution getDiscreteDistribution() {
		return new HLDiscreteDistribution(Math.round((int) myMin), (int) Math
				.round(myMax));
	}

	/**
	 * Returns a <code>HLErlangDistribution</code> object. If no specific value
	 * has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLErlangDistribution a <code>HLErlangDistribution</code> object.
	 */
	public HLErlangDistribution getErlangDistribution() {
		return new HLErlangDistribution(myEmergenceEvents, myIntensity);
	}

	/**
	 * Returns a <code>HLPoissonDistribution</code> object. If no specific value
	 * has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLPoissonDistribution a <code>HLPoissonDistribution</code>
	 *         object.
	 */
	public HLPoissonDistribution getPoissonDistribution() {
		return new HLPoissonDistribution(myIntensity);
	}

	/**
	 * Returns a <code>HLStudentDistribution</code> object. If no specific value
	 * has been set for some attribute specifically related to this
	 * distribution, a default value will be used.
	 * 
	 * @return HLStudentDistribution a <code>HLStudentDistribution</code>
	 *         object.
	 */
	public HLStudentDistribution getStudentDistribution() {
		return new HLStudentDistribution(myDegreesFreedom);
	}

	/**
	 * Creates a distribution of the requested type based on the values in this
	 * meta disribution object.
	 * <p>
	 * Note that the created distribution object will be independent and changes
	 * in either of both will not affect the other.
	 * 
	 * @param requestedType
	 *            the type of distribution asked for
	 * @return the distribution of the requested type
	 */
	public HLDistribution getDistribution(DistributionEnum requestedType) {
		if (requestedType == DistributionEnum.CONSTANT_DISTRIBUTION) {
			return getConstantDistribution();
		} else if (requestedType == DistributionEnum.NORMAL_DISTRIBUTION) {
			return getNormalDistribution();
		} else if (requestedType == DistributionEnum.UNIFORM_DISTRIBUTION) {
			return getUniformDistribution();
		} else if (requestedType == DistributionEnum.EXPONENTIAL_DISTRIBUTION) {
			return getExponentialDistribution();
		} else if (requestedType == DistributionEnum.POISSON_DISTRIBUTION) {
			return getPoissonDistribution();
		} else if (requestedType == DistributionEnum.STUDENT_DISTRIBUTION) {
			return getStudentDistribution();
		} else if (requestedType == DistributionEnum.BERNOULLI_DISTRIBUTION) {
			return getBernoulliDistribution();
		} else if (requestedType == DistributionEnum.BINOMIAL_DISTRIBUTION) {
			return getBinomialDistribution();
		} else if (requestedType == DistributionEnum.CHISQUARE_DISTRIBUTION) {
			return getChiSquareDistribution();
		} else if (requestedType == DistributionEnum.DISCRETE_DISTRIBUTION) {
			return getDiscreteDistribution();
		} else if (requestedType == DistributionEnum.ERLANG_DISTRIBUTION) {
			return getErlangDistribution();
		} else {
			Message.add("Best distribution could not be found!");
			return null;
		}
	}

	/**
	 * Returns the distribution that is currently chosen to be the best.
	 * 
	 * @return the best distribution
	 */
	public HLDistribution getBestDistribution() {
		return getDistribution(myBestDistribution);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.Distribution#getDistributionType
	 * ()
	 */
	public DistributionEnum getDistributionType() {
		return getBestDistributionType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.processmining.framework.models.hlprocess.distribution.HLDistribution
	 * #equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		return (obj instanceof HLGeneralDistribution)
				&& (this.getConstant() == ((HLGeneralDistribution) obj)
						.getConstant())
				&& (this.getDegreesOfFreedom() == ((HLGeneralDistribution) obj)
						.getDegreesOfFreedom())
				&& (this.getEmergenceofEvents() == ((HLGeneralDistribution) obj)
						.getEmergenceofEvents())
				&& (this.getIntensity() == ((HLGeneralDistribution) obj)
						.getIntensity())
				&& (this.getMax() == ((HLGeneralDistribution) obj).getMax())
				&& (this.getMean() == ((HLGeneralDistribution) obj).getMean())
				&& (this.getMin() == ((HLGeneralDistribution) obj).getMin())
				&& (this.getNumberExperiments() == ((HLGeneralDistribution) obj)
						.getNumberExperiments())
				&& (this.getProbability() == ((HLGeneralDistribution) obj)
						.getProbability())
				&& (this.getVariance() == ((HLGeneralDistribution) obj)
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

		int c = 0;
		long l = 0;

		l = Double.doubleToLongBits(this.getConstant());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		result = 37 * result + this.getDegreesOfFreedom();

		result = 37 * result + this.getEmergenceofEvents();

		l = Double.doubleToLongBits(this.getIntensity());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		l = Double.doubleToLongBits(this.getMax());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		l = Double.doubleToLongBits(this.getMean());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		l = Double.doubleToLongBits(this.getMin());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		result = 37 * result + this.getNumberExperiments();

		l = Double.doubleToLongBits(this.getProbability());
		c = (int) (l ^ (l >>> 32));
		result = 37 * result + c;

		l = Double.doubleToLongBits(this.getVariance());
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
		label = label + "General Distribution\\n";
		label = label + "best distribution=" + myBestDistribution.toString()
				+ "\\n";
		label = label + "constant=" + myConstant + "\\n";
		label = label + "degrees freedom=" + myDegreesFreedom + "\\n";
		label = label + "emergence events=" + myEmergenceEvents + "\\n";
		label = label + "intensity=" + myIntensity + "\\n";
		label = label + "max=" + myMax + "\\n";
		label = label + "min=" + myMin + "\\n";
		label = label + "mean=" + myMean + "\\n";
		label = label + "number experiments=" + myNumberExperiments + "\\n";
		label = label + "probability=" + myProbability + "\\n";
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
		myConstant = myConstant * value;
		myMean = myMean * value;
		myVariance = myVariance * value;
		myMin = myMin * value;
		myMax = myMax * value;
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
		if (Math.round(this.getMean()) == 0 || Math.round(this.getMax()) == 0
				|| Math.round(this.getMin()) == 0
				|| Math.round(this.getVariance()) == 0
				|| Math.round(this.getConstant()) == 0
				|| Math.round(this.getIntensity()) == 0) {
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
