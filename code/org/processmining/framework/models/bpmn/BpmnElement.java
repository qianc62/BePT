/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public abstract class BpmnElement implements BpmnDotOutput, BpmnXMLOutput {
	protected String id;
	protected String pid = null; // parent id

	public BpmnElement() {
	};

	public BpmnElement(String id) {
		this.id = id;
	}

	protected BpmnElement(Element element) {
		extractAttributes(element);

		if (id != null && !id.equals("")) {
			NodeList propertyList = element.getChildNodes();
			// extract properties
			extractProperties(propertyList);
		}
	}

	/**
	 * @return the parent
	 */
	public String getpid() {
		return pid;
	}

	/**
	 * @param pid
	 *            the pid to set
	 */
	public void setpid(String pid) {
		this.pid = pid;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	abstract public boolean setProperty(String name, String value);

	/**
	 * Get the DOT file section of this element
	 * 
	 * @return String of the DOT file section of this element, line break
	 *         included
	 */
	abstract public String toDotString();

	abstract public String toXMLString();

	abstract protected void outputProperties(StringBuffer sb);

	protected void outputProperty(StringBuffer sb, String propName,
			String propValue) {
		if (propValue != null && !propValue.equals("null")) {
			sb.append("<");
			sb.append(BpmnXmlTags.BPMN_PROPERTY);
			sb.append(" name=\"");
			sb.append(propName);
			sb.append("\">");
			sb.append(propValue);
			sb.append("</");
			sb.append(BpmnXmlTags.BPMN_PROPERTY);
			sb.append(">");
		}
	}

	protected void outputAttribute(StringBuffer sb, String attName,
			String attValue) {
		if (attValue != null && !attValue.equals("null")) {
			sb.append(" ");
			sb.append(attName);
			sb.append("=\"");
			sb.append(attValue);
			sb.append("\"");
		}
	}

	protected void extractAttributes(Element element) {
		if (element != null) {
			id = element.getAttribute(BpmnXmlTags.BPMN_ATTR_ID);
		} else {
			System.out.println("Element in Constructor is null");
		}
	}

	// extract properties from the xml node
	protected void extractProperties(NodeList nodeList) {
		if (nodeList != null) {
			int size = nodeList.getLength();
			Element el = null;
			String name = ""; // name of property
			String value = ""; // value of property
			for (int i = 0; i < size; i++) {
				Node nd = nodeList.item(i);
				if (BpmnUtils.isProperty(nd)) {
					// get an element from the node list
					el = (Element) nodeList.item(i);
					name = el.getAttribute(BpmnXmlTags.BPMN_ATTR_NAME);
					value = el.getTextContent();
					setProperty(name, value);
				}
			}
		}
	}
}
