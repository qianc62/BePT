/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.*;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng connection between
 *         flow objects, including sequence flow and message flow
 */
public class BpmnEdge extends BpmnElement {
	protected BpmnEdgeType type;
	protected String isLink;
	protected String fromId; // object id of the flow source
	protected String toId;

	// optional fields
	protected String defaultFlag; // slash at the end of default flow
	protected String condition; // triangle at the end of condition flow
	protected String message;
	protected String fromGatewayPosition;
	protected String x;
	protected String y;

	// is used to indicate whether this is a true edge in BPMN
	protected boolean isUseful = true;

	public BpmnEdge(String from, String to) {
		this.fromId = from;
		this.toId = to;
	}

	public BpmnEdge(String from, String to, boolean isUseful) {
		this.fromId = from;
		this.toId = to;
		this.isUseful = isUseful;
	}

	public void setUseful(boolean isUseful) {
		this.isUseful = isUseful;
	}

	/**
	 * Constructor from document node
	 */
	public BpmnEdge(Element element) {
		super(element);
	}

	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}

	/**
	 * @param condition
	 *            the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}

	/**
	 * @return the defaultFlag
	 */
	public boolean isDefaultFlag() {
		return defaultFlag != null && defaultFlag.equalsIgnoreCase("true");
	}

	/**
	 * @param defaultFlag
	 *            the defaultFlag to set
	 */
	public void setDefaultFlag(boolean defaultFlag) {
		this.defaultFlag = String.valueOf(defaultFlag);
	}

	/**
	 * @return the fromGatewayPosition
	 */
	public String getFromGatewayPosition() {
		return fromGatewayPosition;
	}

	/**
	 * @param fromGatewayPosition
	 *            the fromGatewayPosition to set
	 */
	public void setFromGatewayPosition(String fromGatewayPosition) {
		this.fromGatewayPosition = fromGatewayPosition;
	}

	/**
	 * @return the fromId
	 */
	public String getFromId() {
		return fromId;
	}

	/**
	 * @param fromId
	 *            the fromId to set
	 */
	public void setFromId(String fromId) {
		this.fromId = fromId;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @param message
	 *            the message to set
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return the toId
	 */
	public String getToId() {
		return toId;
	}

	/**
	 * @param toId
	 *            the toId to set
	 */
	public void setToId(String toId) {
		this.toId = toId;
	}

	/**
	 * @return the typeTag
	 */
	public BpmnEdgeType getType() {
		return type;
	}

	/**
	 * @param typeTag
	 *            the typeTag to set
	 */
	public void setType(BpmnEdgeType type) {
		this.type = type;
	}

	/**
	 * @return the isLink
	 */
	public boolean isLink() {
		return isLink != null && isLink.equalsIgnoreCase("true");
	}

	/**
	 * @param isLink
	 *            the isLink to set
	 */
	public void setLink(boolean isLink) {
		this.isLink = String.valueOf(isLink);
	}

	public boolean setProperty(String name, String value) {
		boolean found = false;

		// check whether the property name is valid in the sub class
		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_DEFAULT)) {
			this.defaultFlag = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_MESSAGE)) {
			this.message = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_CONDITION)) {
			this.condition = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_GATEWAYPOSITION)) {
			this.fromGatewayPosition = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_X)) {
			this.x = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_Y)) {
			this.y = value;
			found = true;
		}
		// note the CSSclass is not interpreted, it is not needed because this
		// object is
		// already the event

		return found;
	}

	@Override
	protected void extractAttributes(Element element) {
		super.extractAttributes(element);

		// local attributes
		String value = element.getAttribute(BpmnXmlTags.BPMN_ATTR_ISLINK);
		// true for "true", otherwise false
		this.isLink = value;

		this.fromId = element.getAttribute(BpmnXmlTags.BPMN_ATTR_FROM);

		this.toId = element.getAttribute(BpmnXmlTags.BPMN_ATTR_TO);
	}

	@Override
	public String toDotString() {
		StringBuffer strBuf = new StringBuffer();

		String srcNodeId = this.getFromId();
		String dstNodeId = this.getToId();
		String label = "";

		if (!isUseful) {
			strBuf
					.append("\t"
							+ srcNodeId
							+ " -> "
							+ dstNodeId
							+ "[arrowtail=\"none\",arrowhead=\"none\",color=lightgrey];"
							+ "\n");
			strBuf.append("\n");
			return strBuf.toString();
		}

		// use message or condition as label if available
		if (this.getType() == BpmnEdgeType.Message) {
			// message flow
			label = getMessage();
			strBuf
					.append("\t"
							+ srcNodeId
							+ " -> "
							+ dstNodeId
							+ "[style=\"dotted\",arrowtail=\"odot\",arrowhead=\"onormal\"");
			if (label != null && !label.equals("")) {
				strBuf.append(",label=\"" + label);
				strBuf.append("\"];\n");
			} else {
				strBuf.append("];\n");
			}
		} else if (this.getType() == BpmnEdgeType.Flow) {
			// flow with message
			if (getMessage() != null && getCondition() == null) {
				label = getMessage();
				strBuf.append("\t" + srcNodeId + " -> " + dstNodeId
						+ "[label=\"" + label + "\"];\n");
			} else if (getCondition() != null) {
				label = getCondition();

				if (isDefaultFlag() == true) {
					// default flow with condition
					strBuf.append("\t" + srcNodeId + " -> " + dstNodeId
							+ "[arrowtail=\"tee\",label=\"" + label + "\"];\n");
				} else {
					// condition flow, the state "getcondition()is empty" is not
					// possible happen
					strBuf.append("\t" + srcNodeId + " -> " + dstNodeId
							+ "[arrowtail=\"diamond\",label=\"" + label
							+ "\"];\n");
				}
			}
			// default flow without condition
			else if (getCondition() == null && isDefaultFlag() == true) {
				strBuf.append("\t" + srcNodeId + " -> " + dstNodeId
						+ "[arrowtail=\"tee\"];\n");
			}
			// flow without message and condition
			else {
				strBuf.append("\t" + srcNodeId + " -> " + dstNodeId
						+ "[label=\"\"];" + "\n");
			}
		}
		strBuf.append("\n");
		return strBuf.toString();
	}

	public boolean isUseful() {
		return this.isUseful;
	}

	/**
	 * toXMLString
	 * 
	 * @return Object
	 */
	public String toXMLString() {
		// do not export useless edge
		if (!isUseful)
			return "";
		StringBuffer sb = new StringBuffer();
		// start tag
		if (type == BpmnEdgeType.Flow)
			sb.append("<" + BpmnXmlTags.BPMN_FLOW);
		else
			sb.append("<" + BpmnXmlTags.BPMN_MESSAGE);
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, this.id);
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ISLINK, this.isLink);
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_FROM, this.fromId);
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_TO, this.toId);
		sb.append(">\n");
		// properties
		outputProperties(sb);
		if (type == BpmnEdgeType.Flow)
			sb.append("</" + BpmnXmlTags.BPMN_FLOW + ">\n");
		else
			sb.append("</" + BpmnXmlTags.BPMN_MESSAGE + ">\n");
		return sb.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_DEFAULT, this.defaultFlag);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_MESSAGE, this.message);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_CONDITION, this.condition);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_GATEWAYPOSITION,
				this.fromGatewayPosition);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_X, this.x);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_Y, this.y);
	}
}
