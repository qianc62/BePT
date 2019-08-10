//@author JianHong.YE, collaborate with LiJie.WEN and Feng
package org.processmining.framework.models.bpmn;

import org.w3c.dom.*;

public class BpmnSubProcess extends BpmnTask {
	protected boolean adHoc;
	protected boolean expanded;
	protected BpmnProcessModel processModel; // the process model inside the
	// subprocess
	protected BpmnGraph subGraph;

	public BpmnSubProcess(Element element) {
		// parse and give the subprocess's common parameter
		super(element);

		// parse and give the parameter of subgraph in subprocess
		processModel = new BpmnProcessModel(this.id, element);

		// build the subGraph
		subGraph = new BpmnGraph(this.id, processModel);

	}

	public String getNameAndId() {
		String nid = name + id;
		return nid.replaceAll(" ", "_");
	}

	public BpmnSubProcess(String id) {
		super(id);

		// parse and give the parameter of subgraph in subprocess
		processModel = new BpmnProcessModel(this.id);
	}

	public void buildGraph() {
		// build the subGraph
		subGraph = new BpmnGraph(this.id, processModel);
		this.expanded = true;
	}

	/**
	 * @return the adHoc
	 */
	public boolean isAdHoc() {
		return adHoc;
	}

	/**
	 * @param adHoc
	 *            the adHoc to set
	 */
	public void setAdHoc(boolean adHoc) {
		this.adHoc = adHoc;
	}

	/**
	 * @return the expanded
	 */
	public boolean isExpanded() {
		return expanded;
	}

	/**
	 * @param expanded
	 *            the expanded to set
	 */
	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	/**
	 * @return the processModel
	 */
	public BpmnProcessModel getProcessModel() {
		return processModel;
	}

	/**
	 * @param processModel
	 *            the processModel to set
	 */
	public void setProcessModel(BpmnProcessModel processModel) {
		this.processModel = processModel;
		buildGraph();
	}

	/**
	 * @return the subGraph
	 */
	public BpmnGraph getSubGraph() {
		return subGraph;
	}

	/**
	 * @param subGraph
	 *            the subGraph to set
	 */
	public void setSubGraph(BpmnGraph subGraph) {
		this.subGraph = subGraph;
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

		if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_ADHOC)) {
			this.adHoc = Boolean.parseBoolean(value);
			found = true;
		} else if (name.equalsIgnoreCase(BpmnXmlTags.BPMN_PROP_EXPANDED)) {
			this.expanded = Boolean.parseBoolean(value);
			found = true;
		}

		// try to find property in super class if not found in sub class
		if (!found) {
			found = super.setProperty(name, value);
		}

		return found;
	}

	@Override
	public String toDotString() {
		String dotStr = null;
		if (!this.expanded) {
			// unexpanded subprocess is just like a normal node
			dotStr = super.toDotString();
		} else {
			// case of expanded: draw a subgraph
			dotStr = this.subGraph.toDotString(true);
		}

		return dotStr;
	}

	/**
	 * toXMLString
	 * 
	 * @return Object
	 */
	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		// start tag
		sb.append("<" + BpmnXmlTags.BPMN_SUBPROCESS);
		// id attribute
		outputAttribute(sb, BpmnXmlTags.BPMN_ATTR_ID, id);
		sb.append(">");
		// properties
		outputProperties(sb);
		// add all the children below
		BpmnSubProcess[] subs = processModel.getBpmnSubProcesses();
		for (BpmnSubProcess bSub : subs) {
			sb.append(bSub.toXMLString());
		}
		BpmnObject[] nodes = processModel.getBpmnNodes();
		for (BpmnObject bObj : nodes) {
			sb.append(bObj.toXMLString());
		}
		BpmnEdge[] edges = processModel.getBpmnEdges();
		for (BpmnEdge bEdge : edges) {
			sb.append(bEdge.toXMLString());
		}
		// end
		sb.append("</" + BpmnXmlTags.BPMN_SUBPROCESS + ">");
		return sb.toString();
	}

	protected void outputProperties(StringBuffer sb) {
		super.outputProperties(sb);
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_ADHOC, String
				.valueOf(this.adHoc));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_EXPANDED, String
				.valueOf(this.expanded));
		outputProperty(sb, BpmnXmlTags.BPMN_PROP_CSSCLASS,
				BpmnXmlTags.BPMN_CSSC_ACTIVITY);
	}
}
