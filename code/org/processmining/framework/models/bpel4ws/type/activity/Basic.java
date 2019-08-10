package org.processmining.framework.models.bpel4ws.type.activity;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ModelGraph;

/**
 * @author Kristian Bisgaard Lassen
 * 
 */
public abstract class Basic extends Activity {

	private LogEvent event;
	private ModelGraphVertex vertex;

	/**
	 * @param name
	 */
	public Basic(String name) {
		super(name);
	}

	public LogEvent getLogEvent() {
		return event;
	}

	public void setLogEvent(LogEvent event) {
		this.event = event;
	}

	public ModelGraphVertex getVertex() {
		return vertex;
	}

	public void setVertex(ModelGraphVertex vertex) {
		this.vertex = vertex;
	}

	public void newVertex(ModelGraph graph) {
		vertex = new ModelGraphVertex(graph);
	}
}
