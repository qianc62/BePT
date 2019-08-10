/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.Element;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public class BpmnGateway extends BpmnObject {
	protected BpmnGatewayType type;

	protected String XORType; // event for Event-XOR, otherwise Date-XOR

	/**
	 * Constructor from document node TODO implement the constructor to extract
	 * properties in document node
	 */
	public BpmnGateway(Element element) {
		super(element);
	}

	public BpmnGateway(String id) {
		super(id);
	}

	/**
	 * @return the XORType
	 */
	public String isXORType() {
		return XORType;
	}

	/**
	 * @param XORType
	 *            to set
	 */
	public void setXORType(String XORType) {
		this.XORType = XORType;
	}

	/**
	 * @return the type
	 */
	public BpmnGatewayType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BpmnGatewayType type) {
		this.type = type;
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
		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_GATEWAYTYPE)) {
			this.type = BpmnGatewayType.valueOf(value);
			found = true;
		}

		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_GW_XORTYPE)) {
			this.XORType = value;
			found = true;
		}
		// try to find property in super class if not found in sub class
		if (!found) {
			found = super.setProperty(name, value);
		}

		return found;
	}

	public Class<?> getShapeClass() {
		return att.grappa.bpmn.GatewayShape.class;
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
		// shapefile=\"att.grappa.yawl.SplitJoinCompositeTask\",shape=\"custom\",
		// att.grappa.yawl.SplitJoinCompositeTask will be called for
		// drawing.
		strBuf.append("\t" + getId() + " [label=\"\"," + shapeStr + "];\n");

		return strBuf.toString();
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		sb.append("<" + BpmnXmlTags.BPMN_GATEWAY);
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, id);
		sb.append(">");
		// properties
		outputProperties(sb);
		if (this.XORType == null || this.XORType.equals("null")) {
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_GATEWAYTYPE, String
					.valueOf(this.type));
		} else {
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_GW_XORTYPE, "Event");
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_GATEWAYTYPE, String
					.valueOf(this.type));
		}
		sb.append("</" + BpmnXmlTags.BPMN_GATEWAY + ">");
		return sb.toString();
	}
}
