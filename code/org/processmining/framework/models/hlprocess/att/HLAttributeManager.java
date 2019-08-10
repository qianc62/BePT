package org.processmining.framework.models.hlprocess.att;

import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.HLID;
import org.processmining.framework.models.hlprocess.HLProcess;
import org.processmining.framework.models.hlprocess.HLTypes.AttributeType;
import org.processmining.framework.models.hlprocess.distribution.HLGeneralDistribution;
import org.processmining.framework.models.hlprocess.distribution.HLDistribution.DistributionEnum;
import org.processmining.framework.ui.Message;

public class HLAttributeManager {

	/**
	 * Creates a new attribute of the requested type based on the passed old
	 * attribute parameters, as far as they are applicable for the new type.
	 * 
	 * @param type
	 *            the type of attribute that should be created
	 * @param oldAttribute
	 *            the attribute in its previous type
	 * @return the new attribute of the requested type
	 */
	public static HLAttribute switchAttributeType(AttributeType type,
			HLAttribute hlAttribute) {
		HLID oldID = hlAttribute.getID();
		if (type == AttributeType.Boolean) {
			hlAttribute = new HLBooleanAttribute(hlAttribute.getName(),
					hlAttribute.getHLProcess());
		} else if (type == AttributeType.Numeric) {
			hlAttribute = new HLNumericAttribute(hlAttribute.getName(),
					hlAttribute.getHLProcess());
		} else if (type == AttributeType.Nominal) {
			hlAttribute = new HLNominalAttribute(hlAttribute.getName(),
					hlAttribute.getHLProcess());
		} else {
			Message.add("Requested attribute type could not be found!");
			return null;
		}
		hlAttribute.getHLProcess().replaceAttribute(oldID, hlAttribute);
		return hlAttribute;
	}

	/**
	 * Creates a new attribute of numeric or boolean type, if possible.
	 * 
	 * @param att
	 *            the nominal attribute for which the type is checked
	 * @return an attribute of the detected type (nominal again if could not be
	 *         otherwise recognized)
	 */
	public static HLAttribute autoChangeType(HLNominalAttribute att) {
		// check for boolean first
		if (att.getPossibleValues().getValues().size() == 2 ||
		// if only one of the boolean values was observed so far..
				att.getPossibleValues().getValues().size() == 1) {
			int trueFreq = 0;
			int falseFreq = 0;
			boolean isBoolean = true;
			for (String val : att.getPossibleValues().getValues()) {
				val = val.trim();
				// assumes some sensible combinations
				if (val.equals("yes") || val.equals("true") || val.equals("0")
						|| val.equals("pos") || val.equals("positive")) {
					trueFreq = att.getPossibleValues()
							.getFrequencyPossibleValueNominal(val);
				} else if (val.equals("no") || val.equals("false")
						|| val.equals("1") || val.equals("neg")
						|| val.equals("negative")) {
					falseFreq = att.getPossibleValues()
							.getFrequencyPossibleValueNominal(val);
				} else {
					isBoolean = false;
					continue;
				}
			}
			if (isBoolean == true) {
				HLBooleanAttribute newAtt;
				HLProcess hlProcess = att.getHLProcess();
				newAtt = new HLBooleanAttribute(att.getName(), hlProcess);
				newAtt.getPossibleValues().setTrueFrequency(trueFreq);
				newAtt.getPossibleValues().setFalseFrequency(falseFreq);
				hlProcess.replaceAttribute(att.getID(), newAtt);
				return newAtt;
			}
		}

		// check for numeric
		SummaryStatistics myNumericValues = SummaryStatistics.newInstance();
		for (String val : att.getPossibleValues().getValues()) {
			// if is numeric also add value to statistics
			try {
				long numericValue = Long.parseLong(val);
				int freq = att.getPossibleValues()
						.getFrequencyPossibleValueNominal(val);
				for (int i = 0; i < freq; i++) {
					// add as often as was observed to reflect true value
					myNumericValues.addValue(numericValue);
				}
			} catch (NumberFormatException ex) {
				// is not fully numeric - return original nomal attribute
				return att;
			}
		}
		// get characteristics from numeric attribute from statistics plugin
		double min = myNumericValues.getMin();
		double max = myNumericValues.getMax();
		double mean = myNumericValues.getMean();
		double var = myNumericValues.getVariance();
		// use mean value for constant distribution value
		HLGeneralDistribution dist = new HLGeneralDistribution(mean, mean, var,
				min, max);
		dist.setBestDistributionType(DistributionEnum.UNIFORM_DISTRIBUTION);
		// initial value set to zero
		HLNumericAttribute newAtt;
		HLProcess hlProcess = att.getHLProcess();
		newAtt = new HLNumericAttribute(att.getName(), dist, 0, hlProcess);
		hlProcess.replaceAttribute(att.getID(), newAtt);
		return newAtt;
	}

	/**
	 * Checks whether the given nominal attribute can be converted into a
	 * numeric attribute.
	 * 
	 * @param att
	 *            the numeric attribute which should be checked
	 * @return <code>true</code> if all values can be parsed as double,
	 *         <code>false</code> otherwise
	 */
	public static boolean isNumeric(HLNominalAttribute att) {
		for (String val : att.getPossibleValues().getValues()) {
			// if is numeric also add value to statistics
			try {
				long numericValue = Long.parseLong(val);
			} catch (NumberFormatException ex) {
				// is not fully numeric - return original nomal attribute
				return false;
			}
		}
		return true;
	}

}
