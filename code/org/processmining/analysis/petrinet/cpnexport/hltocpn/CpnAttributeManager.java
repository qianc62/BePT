package org.processmining.analysis.petrinet.cpnexport.hltocpn;

import org.processmining.analysis.petrinet.cpnexport.CpnFunction;
import org.processmining.analysis.petrinet.cpnexport.CpnUtils;
import org.processmining.framework.models.hlprocess.HLAttribute;
import org.processmining.framework.models.hlprocess.att.HLBooleanAttribute;
import org.processmining.framework.models.hlprocess.att.HLNominalAttribute;
import org.processmining.framework.models.hlprocess.att.HLNumericAttribute;
import org.processmining.framework.ui.Message;

public class CpnAttributeManager {

	/**
	 * Appends string to initialize data attribute to the given string.
	 * 
	 * @param dataAttribute
	 *            the data attribute for which the initial value should be
	 *            written
	 * @param initString
	 *            the initialization string so far
	 * @return the extended initialization string
	 */
	public static String appendInitialValueString(HLAttribute dataAttribute,
			String initString) {
		initString = initString
				+ VariableTranslator.getCpnVarForDataAttribute(dataAttribute)
						.getVarName() + " = ";
		if (dataAttribute.usesInitialValue() == true) {
			initString = initString + getFixedInitialValueString(dataAttribute);
		} else {
			initString = initString
					+ getRandomInitialValueString(dataAttribute);
		}
		return initString + ",\n";
	}

	/**
	 * Appends string to initialize data attribute to given string without line
	 * break.
	 * <p>
	 * To be used in observer function of logging monitors.
	 * 
	 * @param dataAttr
	 *            the data attribute for which the initial value should be
	 *            written
	 * @param valuesFalseDataAttr
	 *            the initialization string so far
	 * @return the extended initialization string
	 */
	public static String getFalseStringLoggingMonitors(HLAttribute dataAttr,
			String valuesFalseDataAttr) {
		valuesFalseDataAttr = valuesFalseDataAttr
				+ VariableTranslator.getCpnVarForDataAttribute(dataAttr)
						.getVarName() + "=";
		if (dataAttr.usesInitialValue() == true) {
			valuesFalseDataAttr = valuesFalseDataAttr
					+ getFixedInitialValueString(dataAttr);
		} else {
			valuesFalseDataAttr = valuesFalseDataAttr
					+ getRandomInitialValueString(dataAttr);
		}
		return valuesFalseDataAttr + ", ";
	}

	/**
	 * Returns only the fixed initial value for the given attribute as a string.
	 * 
	 * @param dataAttribute
	 *            the data attribute for which the initial value is requested
	 * @return the string holding the initial value for the given attribute
	 */
	public static String getFixedInitialValueString(HLAttribute dataAttribute) {
		if (dataAttribute instanceof HLNominalAttribute) {
			// the initial value should be cpn compliant
			return CpnUtils
					.getCpnValidName(((HLNominalAttribute) dataAttribute)
							.getInitialValueNominal());
		} else if (dataAttribute instanceof HLNumericAttribute) {
			return ((HLNumericAttribute) dataAttribute)
					.getInitialValueNumeric()
					+ "";
		} else if (dataAttribute instanceof HLBooleanAttribute) {
			return ((HLBooleanAttribute) dataAttribute)
					.getInitialValueBoolean()
					+ "";
		} else {
			Message
					.add(
							"Attribute type was not found when should be translated to CPN random initial value.",
							Message.ERROR);
			return "";
		}
	}

	/**
	 * Returns the random inital value string for the given attribute.
	 * <p>
	 * This means that instead of a fixed initial value, a random initial value
	 * will be generated in CPN Tools. Needed for, e.g., global attributes.
	 * 
	 * @param dataAttribute
	 *            the data attribute for which the initial value is requested
	 * @return the string holding the random function call that will obtain the
	 *         inital value
	 */
	public static String getRandomInitialValueString(HLAttribute dataAttribute) {
		CpnFunction randFunction = new CpnFunction(dataAttribute);
		return randFunction.getFunctionName();
	}

}
