/**
 *
 */
package org.processmining.framework.models.bpmn;

import org.w3c.dom.*;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng Base class of BPMN
 *         flow objects and swinlanes
 */
abstract public class BpmnObject extends BpmnElement {
	protected String name;

	protected float height;
	protected float width;
	protected float x;
	protected float y;
	// pic size
	protected float sdmX;
	protected float sdmY;
	protected String lane; // lane name

	protected BpmnObject(Element element) {
		super(element);
	}

	public BpmnObject(String id) {
		super(id);
	}

	/**
	 * @return the height
	 */
	public float getHeight() {
		return height;
	}

	/**
	 * @param height
	 *            the height to set
	 */
	public void setHeight(float height) {
		this.height = height;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the sdmX
	 */
	public float getSdmX() {
		return sdmX;
	}

	/**
	 * @param sdmX
	 *            the sdmX to set
	 */
	public void setSdmX(float sdmX) {
		this.sdmX = sdmX;
	}

	/**
	 * @return the sdmY
	 */
	public float getSdmY() {
		return sdmY;
	}

	/**
	 * @param sdmY
	 *            the sdmY to set
	 */
	public void setSdmY(float sdmY) {
		this.sdmY = sdmY;
	}

	/**
	 * @return the width
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @param width
	 *            the width to set
	 */
	public void setWidth(float width) {
		this.width = width;
	}

	/**
	 * @return the x
	 */
	public float getX() {
		return x;
	}

	/**
	 * @param x
	 *            the x to set
	 */
	public void setX(float x) {
		this.x = x;
	}

	/**
	 * @return the y
	 */
	public float getY() {
		return y;
	}

	/**
	 * @param y
	 *            the y to set
	 */
	public void setY(float y) {
		this.y = y;
	}

	/**
	 * @return the lane
	 */
	public String getLane() {
		return lane;
	}

	/**
	 * @param lane
	 *            the lane to set
	 */
	public void setLane(String lane) {
		this.lane = lane;
	}

	/**
	 * Set a property value, return true if the property name is in the object,
	 * otherwise false
	 * 
	 * @param name
	 * @param value
	 * @return
	 */
	public boolean setProperty(String name, String value) {
		boolean found = false;

		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_NAME)) {
			this.name = value;
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_X)) {
			this.x = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_Y)) {
			this.y = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_SDMX)) {
			this.sdmX = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_SDMY)) {
			this.sdmY = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_HEIGHT)) {
			this.height = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_WIDTH)) {
			this.width = Float.parseFloat(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_LANE)) {
			this.lane = value;
			found = true;
		}

		return found;
	}

	public Class<?> getShapeClass() {
		return null;
	}

	public String toDotString() {
		StringBuffer strBuf = new StringBuffer();

		Class<?> shapeClass = this.getShapeClass();
		String name = getName();
		String label = (name != null) ? name : getId();
		if (shapeClass != null) {
			String shapeStr = "shapefile=\"" + shapeClass.getName()
					+ "\",shape=\"custom\"";
			// As a result of shapefile=\"att.grappa.bpmn.***",shape=\"custom\",
			// att.grappa.bpmn.*** will be called for drawing.
			strBuf.append("\t" + getId() + " [label=\"" + label + "\","
					+ shapeStr + "];\n");
		} else {
			strBuf.append("\t" + getId() + " [shape=\"box\",label=\"" + label
					+ "\"];\n");

		}
		return strBuf.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_NAME, name);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_X, String.valueOf(x));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_Y, String.valueOf(y));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_SDMX, String.valueOf(sdmX));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_SDMY, String.valueOf(sdmY));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_WIDTH, String.valueOf(width));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_HEIGHT, String.valueOf(height));
		if (lane != null && !lane.equals("")) {
			outputProperty(sb, BpmnXmlTags.BPMN_PROP_LANE, lane);
		}
	}

	public String getNameAndId() {
		// TODO Auto-generated method stub
		return name == null ? ("\t" + id) : (name + "\t" + id);
	}
}
