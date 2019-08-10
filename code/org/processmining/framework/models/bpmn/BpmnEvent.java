/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.Element;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng class for a flow
 *         object event, including elements "Start", "End" and "Intermediate"
 */
public class BpmnEvent extends BpmnObject {
	public BpmnEvent(Element element) {
		super(element);
	}

	public BpmnEvent(String id) {
		super(id);
	}

	protected BpmnEventType typeTag;
	protected BpmnEventTriggerType trigger; // if trigger is None, then this
	// event is a result

	protected String Interrupt;

	/**
	 * Return the XML tag name, needed for exporting
	 * 
	 * @return
	 */
	public String getTag() {
		return this.typeTag.toString();
	}

	/**
	 * Return the XML CSSclass value, needed for exporting
	 * 
	 * @return
	 */
	public String getCssClass() {
		return "Event";
	}

	public String getInterrupt() {
		return Interrupt;
	}

	public void setInterrupt(String interrupt) {
		Interrupt = interrupt;
	}

	public BpmnEventTriggerType getTrigger() {
		return trigger;
	}

	public void setTrigger(BpmnEventTriggerType trigger) {
		this.trigger = trigger;
	}

	public BpmnEventType getTypeTag() {
		return typeTag;
	}

	public void setTypeTag(BpmnEventType typeTag) {
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
		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_TRIGGER)) {
			// debug:
			System.out.println("Trigger: " + value);
			this.trigger = BpmnEventTriggerType.valueOf(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_INTERRUPT)) {
			this.Interrupt = value;
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

	public Class<?> getShapeClass() {
		// trigger and shape of Start, End or Intermediate is defined
		// in the EventShape class
		return att.grappa.bpmn.EventShape.class;
	}

	@Override
	public String toDotString() {

		StringBuffer strBuf = new StringBuffer();

		Class<?> shapeClass = this.getShapeClass();
		// String name = getName();
		// String label = (name != null) ? name : getId();

		String shapeStr = "shapefile=\"" + shapeClass.getName()
				+ "\",shape=\"custom\"";
		// As a result of
		// shapefile=\"att.grappa.bpmn.EventShape\",shape=\"custom\",
		// att.grappa.bpmn.EventShape will be called for
		// drawing.
		strBuf.append("\t" + getId() + " [label=\"\"," + shapeStr + "];\n");

		return strBuf.toString();
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		if (typeTag == BpmnEventType.Start) {
			sb.append("<" + BpmnXmlTags.BPMN_START);
		} else if (typeTag == BpmnEventType.End) {
			sb.append("<" + BpmnXmlTags.BPMN_END);
		} else {
			sb.append("<" + BpmnXmlTags.BPMN_INTERMEDIATE);
		}
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, id);
		sb.append(">");
		// properties
		outputProperties(sb);
		if (typeTag == BpmnEventType.Start) {
			sb.append("</" + BpmnXmlTags.BPMN_START + ">");
		} else if (typeTag == BpmnEventType.End) {
			sb.append("</" + BpmnXmlTags.BPMN_END + ">");
		} else {
			sb.append("</" + BpmnXmlTags.BPMN_INTERMEDIATE + ">");
		}
		return sb.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		super.outputProperties(sb);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_TRIGGER, String
				.valueOf(this.trigger));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_INTERRUPT, this.Interrupt);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_CSSCLASS, "Event");
	}
}
