/**
 * 
 */
package org.processmining.framework.models.bpmn;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.yawl.YAWLModel;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng
 * 
 */
public class BpmnGraphVertex extends ModelGraphVertex implements BpmnDotOutput {
	protected BpmnObject bpmnObject;

	public BpmnGraphVertex(ModelGraph g, BpmnObject object) {
		super(g);

		bpmnObject = object;
	}

	/**
	 * @return the object
	 */
	public BpmnObject getBpmnObject() {
		return bpmnObject;
	}

	/**
	 * @param object
	 *            the object to set
	 */
	public void setBpmnObject(BpmnObject object) {
		bpmnObject = object;
	}

	public String toDotString() {
		return (bpmnObject != null) ? bpmnObject.toDotString() : "";
	}

}
