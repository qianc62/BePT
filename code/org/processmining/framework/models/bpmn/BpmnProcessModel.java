/**
 *
 */
package org.processmining.framework.models.bpmn;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng This class represent
 *         one level of process, a process is the main diagram of defined in a
 *         SubProcess node
 */
public class BpmnProcessModel {
	protected HashMap<String, BpmnSubProcess> subProcesses; // all BMPN sub
	// process
	protected HashMap<String, BpmnObject> nodes; // all BPMN nodes except sub
	// process
	protected HashMap<String, BpmnEdge> edges; // all BPMN connections
	protected String parentId;
	protected BpmnEvent start;
	protected BpmnEvent end;

	protected HashMap<String, BpmnGraphVertex> vids = new HashMap<String, BpmnGraphVertex>();

	/**
	 * Constructor from XML document
	 * 
	 */
	public BpmnProcessModel(String parentId, Element element) {
		// local initialization
		subProcesses = new HashMap<String, BpmnSubProcess>();
		nodes = new HashMap<String, BpmnObject>();
		edges = new HashMap<String, BpmnEdge>();
		this.parentId = parentId;
		parseCurrentLevel(element);

	}

	public BpmnProcessModel(String parentId) {
		subProcesses = new HashMap<String, BpmnSubProcess>();
		nodes = new HashMap<String, BpmnObject>();
		edges = new HashMap<String, BpmnEdge>();
		this.parentId = parentId;
	}

	/**
	 * @return the end
	 */
	public BpmnEvent getEnd() {
		return end;
	}

	/**
	 * @param end
	 *            the end to set
	 */
	public void setEnd(BpmnEvent end) {
		this.end = end;
	}

	/**
	 * @return the start
	 */
	public BpmnEvent getStart() {
		return start;
	}

	/**
	 * @param start
	 *            the start to set
	 */
	public void setStart(BpmnEvent start) {
		this.start = start;
	}

	public BpmnSubProcess[] getBpmnSubProcesses() {
		int size = subProcesses.size();
		return subProcesses.values().toArray(new BpmnSubProcess[size]);
	}

	public BpmnObject[] getBpmnNodes() {
		int size = nodes.size();
		return nodes.values().toArray(new BpmnObject[size]);
	}

	public BpmnEdge[] getBpmnEdges() {
		int size = edges.size();
		return edges.values().toArray(new BpmnEdge[size]);
	}

	public BpmnEdge getEdge(String edgeId) {
		return edges.get(edgeId);
	}

	public BpmnObject getNode(String nodeId) {
		if (nodes.get(nodeId) != null)
			return nodes.get(nodeId);
		return subProcesses.get(nodeId);
	}

	public String getNameAndId(String nodeId) {
		BpmnObject node = nodes.get(nodeId);
		if (node == null)
			node = subProcesses.get(nodeId);
		return node.getName() + "\t" + node.getId();
	}

	/**
	 * If this is a sub process, get the Id of node to which the incoming edge
	 * should point
	 * 
	 * @return
	 */
	public String getInEdgeHeadId() {
		String idStr = "";
		if (this.start != null) {
			idStr = this.start.getId();
		} else if (this.nodes.size() > 0) {
			// id of the first node
			idStr = (this.getBpmnNodes()[0]).getId();
		}
		return idStr;
	}

	/**
	 * If this is a sub process, get the Id of node from which the outgoing edge
	 * should point
	 * 
	 * @return
	 */
	public String getOutEdgeTailId() {
		String idStr = "";
		if (this.end != null) {
			idStr = this.end.getId();
		} else if (this.nodes.size() > 0) {
			int lastIdx = this.nodes.size() - 1;
			// id of the last node
			idStr = (this.getBpmnNodes()[lastIdx]).getId();
		}
		return idStr;
	}

	protected void parseCurrentLevel(Element element) {
		// get all child nodes of the parent element
		NodeList childNodes = element.getChildNodes();
		int childrenNum = childNodes.getLength();

		for (int i = 0; i < childrenNum; i++) {
			Node nd = childNodes.item(i);
			if (nd instanceof Element) {
				parseElement((Element) nd);
			}
		}
	}

	public void addNode(BpmnObject node) {
		if (node instanceof BpmnSubProcess) {
			subProcesses.put(node.getId(), (BpmnSubProcess) node);
		} else {
			nodes.put(node.getId(), node);
		}
	}

	protected BpmnElement parseElement(Element element) {
		String tag = element.getTagName();

		// events
		// all elements starting with Start, Intermediate or End
		if (tag.equals(BpmnXmlTags.BPMN_START)
				|| tag.equals(BpmnXmlTags.BPMN_INTERMEDIATE)
				|| tag.equals(BpmnXmlTags.BPMN_END)) {
			BpmnEvent event = new BpmnEvent(element);
			String id = event.getId();
			// set the type by the starting tag name
			event.setTypeTag(BpmnEventType.valueOf(tag));
			// set the parent id
			event.setpid(this.parentId);
			// save the node
			nodes.put(id, event);
			if (tag.equals(BpmnXmlTags.BPMN_START)) {
				this.start = event;
			} else if (tag.equals(BpmnXmlTags.BPMN_END)) {
				this.end = event;
			}
			return event;
		} else if (tag.equals(BpmnXmlTags.BPMN_TASK)) {
			// Task and activities starting with the tag "Task"
			BpmnTask task = new BpmnTask(element);
			String id = task.getId();
			// set the type by the starting tag name
			task.setTypeTag(BpmnTaskType.valueOf(tag));
			// set the parent id
			task.setpid(this.parentId);
			// save the node
			nodes.put(id, task);

			// handle its child of intermediate event type
			NodeList childNodes = element
					.getElementsByTagName(BpmnXmlTags.BPMN_INTERMEDIATE);
			int childrenNum = childNodes.getLength();
			for (int i = 0; i < childrenNum; i++) {
				Node nd = childNodes.item(i);
				if (nd instanceof Element) {
					BpmnEvent event = (BpmnEvent) parseElement((Element) nd);
					event.setLane(task.getLane());
					if (event != null) {
						String edgeId = event.getId() + "_" + id;
						BpmnEdge edge = new BpmnEdge(event.getId(), id);
						edge.setType(BpmnEdgeType.Flow);
						// set the parent id
						edge.setpid(this.parentId);
						edge.setId(edgeId);
						// save the node
						edges.put(edgeId, edge);
					}
				}
			}
			return task;
		} else if (tag.equals(BpmnXmlTags.BPMN_SUBPROCESS)) {
			// sub process
			BpmnSubProcess sub = new BpmnSubProcess(element);
			String id = sub.getId();
			sub.setTypeTag(BpmnTaskType.valueOf(tag));
			// set the parent id
			sub.setpid(this.parentId);
			// save the node
			subProcesses.put(id, sub);
			return sub;
		} else if (tag.equals(BpmnXmlTags.BPMN_GATEWAY)) {
			// gateways
			BpmnGateway gw = new BpmnGateway(element);
			String id = gw.getId();
			// set the parent id
			gw.setpid(this.parentId);
			// save the node
			nodes.put(id, gw);
			return gw;
		} else if (tag.equals(BpmnXmlTags.BPMN_POOL)) {
			// lane and pool
			BpmnSwimPool pool = new BpmnSwimPool(element);
			pool.setType(BpmnSwimType.valueOf(tag));
			String poolid = pool.getId();
			// set the parent id
			pool.setpid(this.parentId);
			// save the node
			nodes.put(poolid, pool);
			return pool;
		} else if (tag.equals(BpmnXmlTags.BPMN_LANE)) {
			// lane and pool
			BpmnSwimLane lane = new BpmnSwimLane(element);
			lane.setType(BpmnSwimType.valueOf(tag));
			String laneid = lane.getId();
			// set the parent id
			lane.setpid(this.parentId);
			// save the node
			nodes.put(laneid, lane);
			return lane;
		} else if (tag.equals(BpmnXmlTags.BPMN_FLOW)
				|| tag.equals(BpmnXmlTags.BPMN_MESSAGE)) {
			// connections
			BpmnEdge edge = new BpmnEdge(element);
			edge.setType(BpmnEdgeType.valueOf(tag));
			String id = edge.getId();
			// set the parent id
			edge.setpid(this.parentId);
			// save the node
			edges.put(id, edge);
			return edge;
		}
		return null;
	}

	public void addEdge(BpmnEdge edge) {
		if (edges.get(edge.getId()) == null)
			edges.put(edge.getId(), edge);
	}

	public void removeEdge(String id) {
		edges.remove(id);
	}
}
