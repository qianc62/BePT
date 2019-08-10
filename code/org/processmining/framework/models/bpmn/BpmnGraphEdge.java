/**
 *
 */
package org.processmining.framework.models.bpmn;

import java.io.IOException;
import java.io.Writer;

import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public class BpmnGraphEdge extends ModelGraphEdge implements BpmnDotOutput {
	protected BpmnEdge edge;

	public BpmnGraphEdge(BpmnGraphVertex source, BpmnGraphVertex destination,
			BpmnEdge edge) {
		super(source, destination);

		this.edge = edge;
	}

	/**
	 * @return the edge
	 */
	public BpmnEdge getEdge() {
		return edge;
	}

	/**
	 * @param edge
	 *            the edge to set
	 */
	public void setEdge(BpmnEdge edge) {
		this.edge = edge;
	}

	public String toDotString() {
		StringBuffer strBuf = new StringBuffer();
		StringBuffer edgeStrBuf = new StringBuffer(this.edge.toDotString());

		// check whether the source object is expanded subgraph
		BpmnGraphVertex srcVertex = (BpmnGraphVertex) this.getSource();
		BpmnObject srcObj = srcVertex.getBpmnObject();
		if (BpmnUtils.isExpandedSubGraph(srcObj)) {
			// replace the subprocess id with the id of subprocess end
			String clusterId = ((BpmnSubProcess) srcObj).getSubGraph()
					.getClusterId();
			String oldId = srcObj.getId();
			int startIdx = edgeStrBuf.indexOf(oldId);
			if (startIdx >= 0) {
				int endIdx = startIdx + oldId.length();
				String newId = ((BpmnSubProcess) srcObj).getProcessModel()
						.getOutEdgeTailId();
				if (newId != null && !newId.equals("")) {
					edgeStrBuf.replace(startIdx, endIdx, newId);
				}
			}
			strBuf.append("ltail=");
			strBuf.append(clusterId);
		}

		// check whether the destination object is expanded subgraph
		// get the string of this edge
		BpmnGraphVertex dstVertex = (BpmnGraphVertex) this.getDest();
		BpmnObject dstObj = dstVertex.getBpmnObject();
		if (BpmnUtils.isExpandedSubGraph(dstObj)) {
			// replace the subprocess id with the id of subprocess start
			String clusterId = ((BpmnSubProcess) dstObj).getSubGraph()
					.getClusterId();
			String oldId = dstObj.getId();
			int startIdx = edgeStrBuf.lastIndexOf(oldId);
			if (startIdx >= 0) {
				int endIdx = startIdx + oldId.length();
				String newId = ((BpmnSubProcess) dstObj).getProcessModel()
						.getInEdgeHeadId();
				if (newId != null && !newId.equals("")) {
					edgeStrBuf.replace(startIdx, endIdx, newId);
				}
			}
			if (strBuf.length() > 0) {
				strBuf.append(", ");
			}
			strBuf.append("lhead=");
			strBuf.append(clusterId);
		}

		// case we need to append a section about sub graph
		if (strBuf.length() > 0) {
			// find the position of last ']'
			int lastPos = edgeStrBuf.lastIndexOf("]");
			int len = edgeStrBuf.length();
			if (lastPos > 0) {
				edgeStrBuf.insert(lastPos, "," + strBuf);
			} else {
				strBuf.insert(0, "[");
				strBuf.append("]");
				// insert just before ";"
				edgeStrBuf.insert(len - 2, strBuf);
			}
		}

		return edgeStrBuf.toString();
	}

}
