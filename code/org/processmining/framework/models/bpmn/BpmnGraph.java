/**
 *
 */
package org.processmining.framework.models.bpmn;

import java.io.*;
import java.util.*;

import org.processmining.framework.models.*;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public class BpmnGraph extends ModelGraph {
	protected BpmnProcessModel process;
	protected HashMap<String, BpmnGraphVertex> vids = new HashMap<String, BpmnGraphVertex>();
	protected HashMap<String, BpmnObject> pools = new HashMap<String, BpmnObject>();
	protected String parentid;

	public BpmnGraph(String graphName, BpmnProcessModel process) {
		super(graphName);
		this.process = process;
		this.parentid = graphName;
		// construct model use writeToDot
		construct();
	}

	/**
	 * @return the process
	 */
	public BpmnProcessModel getProcess() {
		return process;
	}

	/**
	 * @param process
	 *            the process to set
	 */
	public void setProcess(BpmnProcessModel process) {
		this.process = process;
	}

	public void writeToDot(Writer bw) throws IOException {
		bw.write(toDotString(false));
	}

	public String toDotString() {
		// by default, return the sub process Dot file section
		return toDotString(true);
	}

	/*
	 * compose the cluster ID of the sub graph
	 */
	public String getClusterId() {
		return "cluster_" + this.getIdentifier();
	}

	public String toDotString(boolean isSubProcess) {
		StringBuffer strBuf = new StringBuffer();

		// only the header is different for subprocess or main process
		if (isSubProcess) {
			strBuf.append("subgraph " + this.getClusterId());
			strBuf.append(" {\n");
		} else {
			strBuf.append("digraph G");
			strBuf.append(" {\n");
			// allow edges start or end from/to a subgraph
			strBuf.append("\tcompound = true;\n");
			strBuf.append("fontsize=\"8\"; remincross=true,");
			strBuf.append("fontname=\"Arial\";rankdir=\"TB\";\n");
			strBuf
					.append("edge [arrowsize=\"0.7\",fontname=\"Arial\",fontsize=\"8\"];\n");
			strBuf
					.append("node [height=\".2\",width=\".2\",fontname=\"Arial\",fontsize=\"8\"];\n");
		}
		nodeMapping.clear();
		Iterator it = this.vertices.iterator();
		while (it.hasNext()) {
			BpmnGraphVertex v = (BpmnGraphVertex) it.next();
			// consider parent and layout
			String parentid = v.getBpmnObject().getpid();
			if (!isSubProcess && parentid == null || isSubProcess
					&& parentid == this.parentid)
				strBuf.append(v.toDotString());
			nodeMapping.put(v.getIdentifier(), v);
		}

		it = this.getEdges().iterator();
		while (it.hasNext()) {
			BpmnGraphEdge e = (BpmnGraphEdge) it.next();
			// debug:
			System.out.print(e.toDotString());

			strBuf.append(e.toDotString());
		}

		strBuf.append("}\n");

		// debug:
		System.out.print(strBuf);

		return strBuf.toString();
	}

	/**
	 * Get the vertex in the graph, including the sub graphs
	 * 
	 * @param vertexId
	 * @return
	 */
	public BpmnGraphVertex getVertex(String vertexId) {
		BpmnGraphVertex result = (BpmnGraphVertex) vids.get(vertexId);

		if (result == null) {
			// search vertext in sub process models
			BpmnSubProcess[] subProcesses = this.process.getBpmnSubProcesses();
			for (BpmnSubProcess subProc : subProcesses) {
				result = subProc.getSubGraph().getVertex(vertexId);
				// got it
				if (result != null) {
					break;
				}
			}
		}

		return result;
	}

	public ArrayList<String> getPreds(String nodeId) {
		ArrayList<String> alPred = new ArrayList();

		getPreds(alPred, nodeId);

		return alPred;
	}

	public String getNameAndId(String nodeId) {
		BpmnObject bo = process.getNode(nodeId);
		if (bo == null) {
			boolean isFound = false;
			BpmnSubProcess[] subProcesses = this.process.getBpmnSubProcesses();
			for (BpmnSubProcess subProc : subProcesses) {
				String nameid = subProc.getSubGraph().getNameAndId(nodeId);
				if (nameid != null) {
					return nameid;
				}
			}
			return null;
		} else {
			return bo.getNameAndId();
		}
	}

	public boolean getPreds(ArrayList<String> alPred, String nodeId) {
		alPred.add(process.parentId);
		BpmnObject bo = process.getNode(nodeId);
		if (bo == null) {
			BpmnSubProcess[] subProcesses = this.process.getBpmnSubProcesses();
			for (BpmnSubProcess subProc : subProcesses) {
				boolean isFound = subProc.getSubGraph()
						.getPreds(alPred, nodeId);
				// if found, got it
				if (isFound) {
					return true;
				} else {
					alPred.remove(alPred.size() - 1);
				}
			}
			return false;
		}

		return true;
	}

	/**
	 * constructModel
	 */
	protected void construct() {
		// enumerate all vertexs and edges in mainProcess and add them to this
		// model
		BpmnSubProcess[] subs = process.getBpmnSubProcesses();
		for (BpmnSubProcess bSub : subs) {
			BpmnGraphVertex v = new BpmnGraphVertex(this, bSub);
			v.setAttribute("label", bSub.getName());
			v.setIdentifier(bSub.getId());

			this.addVertex(v);

			// store every node for the edge to use
			vids.put(bSub.getId(), v);
		}

		BpmnObject[] nodes = process.getBpmnNodes();
		for (BpmnObject bObj : nodes) {
			BpmnGraphVertex v = new BpmnGraphVertex(this, bObj);
			v.setAttribute("label", bObj.getName());
			v.setIdentifier(bObj.getId());

			this.addVertex(v);

			// store every node for the edge to use
			vids.put(bObj.getId(), v);
		}

		// loop over the edges
		BpmnEdge[] edges = process.getBpmnEdges();
		for (BpmnEdge bEdge : edges) {
			// get the vertex of the source
			BpmnGraphVertex vs = this.getVertex(bEdge.getFromId());
			// get the vertex of the destination
			BpmnGraphVertex vd = this.getVertex(bEdge.getToId());
			if (vs != null && vd != null) {
				BpmnGraphEdge newEdge = new BpmnGraphEdge(vs, vd, bEdge);
				this.addEdge(newEdge);
			}
		}
		// find all pools and lanes
		Iterator it = this.vertices.iterator();
		while (it.hasNext()) {
			BpmnGraphVertex v = (BpmnGraphVertex) it.next();
			if (v.bpmnObject instanceof BpmnSwimPool
					|| v.bpmnObject instanceof BpmnSwimLane) {
				pools.put(v.bpmnObject.getName(), v.bpmnObject);
			}
		}
		it = this.vertices.iterator();
		while (it.hasNext()) {
			BpmnGraphVertex v = (BpmnGraphVertex) it.next();
			String lane = v.getBpmnObject().getLane();
			if (lane != null && !lane.equals("") && pools.get(lane) != null) {
				BpmnObject parent = pools.get(lane);
				if (parent instanceof BpmnSwimPool) {
					((BpmnSwimPool) parent).addChild(v);
				} else {
					((BpmnSwimLane) parent).addChild(v);
				}
				v.getBpmnObject().setpid(lane);
			}
		}
	}

	/**
	 * toXMLString
	 * 
	 * @return Object
	 */
	public String toXMLString() {
		StringBuffer sb = new StringBuffer();
		BpmnSubProcess[] subs = process.getBpmnSubProcesses();
		for (BpmnSubProcess bSub : subs) {
			sb.append(bSub.toXMLString());
		}
		BpmnObject[] nodes = process.getBpmnNodes();
		for (BpmnObject bObj : nodes) {
			sb.append(bObj.toXMLString());
		}
		BpmnEdge[] edges = process.getBpmnEdges();
		for (BpmnEdge bEdge : edges) {
			sb.append(bEdge.toXMLString());
		}
		return sb.toString();
	}

	/**
	 * writeToBPMN
	 * 
	 * @return String
	 */
	public String writeToBPMN() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
		sb.append("<" + BpmnXmlTags.BPMN_DIAGRAM + ">");
		sb.append(this.toXMLString());
		sb.append("</" + BpmnXmlTags.BPMN_DIAGRAM + ">");
		return sb.toString();
	}

	// For BPMN to YAWL,find the from and to node of a edge, because in bpmn, we
	// allow that
	// an edge can point into an element of subprocess, but this attribute is
	// not allowed in yawl
	// so, we need to choice the common predecessor of the from and edge node of
	// the edge.
	public void constructEdge(String[] arParams) {
		if (arParams.length != 3)
			return;
		ArrayList<String> alPredFrom = getPreds(arParams[1]);
		ArrayList<String> alPredTo = getPreds(arParams[2]);
		// compare the two predecessors to find the last common one
		int i = 1;
		while (alPredFrom.size() > i && alPredTo.size() > i) {
			if (!alPredFrom.get(i).equals(alPredTo.get(i)))
				break;
			i++;
		}
		// set the last common predecessor
		arParams[0] = alPredFrom.get(i - 1);
		if (arParams[0] == null)
			arParams[0] = "root";
		else
			arParams[0] = getNameAndId(arParams[0]);
		// change to new from
		if (alPredFrom.size() != i) {
			arParams[1] = alPredFrom.get(i);
		}
		arParams[1] = getNameAndId(arParams[1]);
		// change to new to
		if (alPredTo.size() != i) {
			arParams[2] = alPredTo.get(i);
		}
		arParams[2] = getNameAndId(arParams[2]);
	}
}
