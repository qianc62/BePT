/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.Element;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng Class for BPMN
 *         activity and task
 */
public class BpmnTask extends BpmnObject {
	protected BpmnTaskType typeTag;

	// properties of subprocess
	protected String loopType;
	protected String timing; // parallel for subprocess, true for task
	protected boolean compensationactivity; // same for task

	protected String transaction;

	/**
	 * Constructor from a document node TODO implement the constructor to
	 * extract properties in document node
	 */
	public BpmnTask(Element element) {
		super(element);
	}

	public BpmnTask(String id) {

		super(id);
	}

	public String getLoopType() {
		return this.loopType;
	}

	/**
	 * @return the compensation
	 */
	public boolean isCompensation() {
		return compensationactivity;
	}

	/**
	 * @param compensation
	 *            the compensation to set
	 */
	public void setCompensation(boolean compensationactivity) {
		this.compensationactivity = compensationactivity;
	}

	/**
	 * @return the timing
	 */
	public String isTiming() {
		return timing;
	}

	/**
	 * @param timing
	 *            the timing to set
	 */
	public void setTiming(String timing) {
		this.timing = timing;
	}

	/**
	 * @return the transaction
	 */
	public String getTransaction() {
		return transaction;
	}

	/**
	 * @param transaction
	 *            the transaction to set
	 */
	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}

	/**
	 * @return the typeTag
	 */
	public BpmnTaskType getTypeTag() {
		return typeTag;
	}

	/**
	 * @param typeTag
	 *            the typeTag to set
	 */
	public void setTypeTag(BpmnTaskType typeTag) {
		this.typeTag = typeTag;
	}

	/**
	 * Set a property value, return true if the property name is in the object,
	 * otherwise false
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	@Override
	public boolean setProperty(String name, String value) {
		boolean found = false;

		// check whether the property name is valid in the sub class
		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_LOOPTYPE)) {
			this.loopType = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_TIMING)) {
			this.timing = value;
			found = true;
		} else if (name
				.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY)) {
			this.compensationactivity = Boolean.parseBoolean(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_TRANSACTION)) {
			this.transaction = value;
			found = true;
		}

		// note the CSSclass is not interpreted, it is not needed because this
		// object is
		// already the event

		// try to find property in super class if not found in sub class
		if (!found) {
			found = super.setProperty(name, value);
		}

		return found;
	}

	@Override
	public String toDotString() {
		return super.toDotString();
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		sb.append("<" + BpmnXmlTags.BPMN_TASK);
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, id);
		sb.append(">");
		// properties
		outputProperties(sb);
		if (typeTag == BpmnTaskType.Activity)
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_CSSCLASS,
					BpmnXmlTags.BPMN_CSSC_ACTIVITY);
		else
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_CSSCLASS,
					BpmnXmlTags.BPMN_CSSC_TASK);
		sb.append("</" + BpmnXmlTags.BPMN_TASK + ">");
		return sb.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		super.outputProperties(sb);

		if (this.loopType != null)
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_LOOPTYPE, this.loopType);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_TIMING, this.timing);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_TRANSACTION, this.transaction);
		if (this.compensationactivity)
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_COMPENSATIONACTIVITY,
					String.valueOf(this.compensationactivity));
	}
}
