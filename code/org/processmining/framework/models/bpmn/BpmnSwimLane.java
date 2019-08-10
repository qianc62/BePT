/**
 *
 */
package org.processmining.framework.models.bpmn;

import java.util.HashMap;
import java.util.Iterator;

import org.w3c.dom.Element;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public class BpmnSwimLane extends BpmnObject {
	protected float length;
	protected float size;
	protected int maxLevel;
	protected BpmnSwimType type;
	protected HashMap<String, BpmnGraphVertex> children = new HashMap<String, BpmnGraphVertex>();

	/**
	 * Constructor from document node TODO implement the constructor to extract
	 * properties in document node
	 */
	public BpmnSwimLane(Element element) {
		super(element);
	}

	public BpmnSwimLane(String id) {
		super(id);
	}

	public void addChild(BpmnGraphVertex child) {
		children.put(child.getIdentifier(), child);
	}

	/**
	 * @return the length
	 */
	public float getLength() {
		return length;
	}

	/**
	 * @param length
	 *            the length to set
	 */
	public void setLength(float length) {
		this.length = length;
	}

	/**
	 * @return the maxLevel
	 */
	public int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * @param maxLevel
	 *            the maxLevel to set
	 */
	public void setMaxLevel(int maxLevel) {
		this.maxLevel = maxLevel;
	}

	/**
	 * @return the size
	 */
	public float getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(float size) {
		this.size = size;
	}

	/**
	 * @return the type
	 */
	public BpmnSwimType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(BpmnSwimType type) {
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
		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_LENGTH)) {
			this.length = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_SIZE)) {
			this.size = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_HEIGHT)) {
			this.height = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_MAXLEVEL)) {
			this.maxLevel = Integer.parseInt(value);
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
		// enumerate all children
		StringBuffer strBuf = new StringBuffer();
		strBuf.append("subgraph " + "cluster_" + this.getId());
		strBuf.append(" {\n");
		Iterator it = this.children.values().iterator();
		while (it.hasNext()) {
			BpmnGraphVertex v = (BpmnGraphVertex) it.next();
			strBuf.append(v.toDotString());
		}
		strBuf.append("label=\"" + name + "\";\n");
		strBuf.append("color=black;\n");
		strBuf.append(" }\n");

		return strBuf.toString();
	}

	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		sb.append("<" + BpmnXmlTags.BPMN_LANE);
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, id);
		sb.append(">");
		// properties
		outputProperties(sb);
		sb.append("</" + BpmnXmlTags.BPMN_LANE + ">");
		return sb.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		super.outputProperties(sb);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_SIZE, String
				.valueOf(this.size));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_MAXLEVEL, String
				.valueOf(this.maxLevel));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_LENGTH, String
				.valueOf(this.length));
	}
}
