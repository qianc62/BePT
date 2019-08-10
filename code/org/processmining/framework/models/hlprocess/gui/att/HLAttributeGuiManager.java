package org.processmining.framework.models.hlprocess.gui.att;

import org.processmining.framework.models.hlprocess.att.HLAttributeValue;
import org.processmining.framework.models.hlprocess.att.HLBooleanDistribution;
import org.processmining.framework.models.hlprocess.att.HLBooleanValue;
import org.processmining.framework.models.hlprocess.att.HLNominalDistribution;
import org.processmining.framework.models.hlprocess.att.HLNominalValue;
import org.processmining.framework.models.hlprocess.att.HLNumericDistribution;
import org.processmining.framework.models.hlprocess.att.HLNumericValue;
import org.processmining.framework.models.hlprocess.gui.HLAttributeGui;
import org.processmining.framework.models.hlprocess.gui.HLAttributeValueGui;
import org.processmining.framework.ui.Message;

public class HLAttributeGuiManager {

	/**
	 * Creates a new attribute value GUI based on the kind of passed value
	 * object. Changes on the GUI will be directly reported back to the
	 * underlying attribute.
	 * 
	 * @param parent
	 *            the attribute gui for which the GUI is requested
	 * @return the GUI of matching value type
	 */
	public static HLAttributeValueGui getAttributeValueGui(
			HLAttributeValue value, HLAttributeGui parent) {
		if (value instanceof HLBooleanDistribution) {
			return new HLBooleanDistributionGui((HLBooleanDistribution) value,
					parent);
		} else if (value instanceof HLNominalDistribution) {
			return new HLNominalDistributionGui((HLNominalDistribution) value,
					parent);
		} else if (value instanceof HLNumericDistribution) {
			return new HLNumericDistributionGui((HLNumericDistribution) value,
					parent);
		} else if (value instanceof HLBooleanValue) {
			return new HLBooleanValueGui((HLBooleanValue) value, parent);
		} else if (value instanceof HLNumericValue) {
			return new HLNumericValueGui((HLNumericValue) value, parent);
		} else if (value instanceof HLNominalValue) {
			return new HLNominalValueGui((HLNominalValue) value, parent);
		} else {
			Message.add("Requested attribute type could not be found!");
			return null;
		}
	}

}
