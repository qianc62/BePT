package org.processmining.framework.models.activitygraph;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphVertex;

/**
 * An activity graph is a graph with only activities in it. So, there doesn't
 * exists any connections between the activities.
 * 
 * @author rmans
 */
public class ActivityGraph extends ModelGraph {

	/** the list of activities */
	protected ArrayList<ModelGraphVertex> activities = new ArrayList<ModelGraphVertex>();

	// /** the mapping for an object to its corresponding boxID and which is
	// needed for DOT. */
	// private HashMap mappingBoxesToBoxId = new HashMap();

	// /** the link to the highlevelPN to be able to access the simulation
	// information
	// * that exists for nodes and edges.
	// */
	// private HLActivitySet hlActivitySet = null;

	/**
	 * basic constructor
	 */
	public ActivityGraph() {
		super("Activity Graph");
	}

	/**
	 * adds an activity vertex to the activity graph
	 * 
	 * @param av
	 *            ActivityVertex the activity vertex to be added to the graph
	 * @return ActivityVertex the added activity vertex
	 */
	public ModelGraphVertex addActivityVertex(ModelGraphVertex av) {
		addVertex(av);
		activities.add(av);
		return av;
	}

	/**
	 * Retrieves the activity vertices that are present in this activity graph
	 * 
	 * @return List
	 */
	public List<ModelGraphVertex> getActivityVertices() {
		return activities;
	}

	/**
	 * Removes an activity vertex from the activity graph
	 * 
	 * @param av
	 *            ActivityVertex the activity vertex to be removed from the
	 *            activity graph
	 */
	public void delActivityVertex(ModelGraphVertex av) {
		removeVertex(av);
		activities.remove(av);
	}

	/**
	 * When a activity graph is asked for its visualization, a temporary dot
	 * file is written and afterwards read by grappa to convert it into a java
	 * frame. For the activity graph, only the activities will be displayed in
	 * the picture.
	 * 
	 * @param bw
	 *            Writer
	 * @throws IOException
	 */
	public void writeToDot(Writer bw) throws IOException {
		nodeMapping.clear();
		bw
				.write("digraph G {ranksep=\".3\"; fontsize=\"8\"; remincross=true; margin=\"0.0,0.0\"; ");
		bw.write("fontname=\"Arial\";rankdir=\"TB\";compound=\"true\" \n");
		bw.write("edge [arrowsize=\"0.5\"];\n");
		bw
				.write("node [height=\".2\",width=\"1\",fontname=\"Arial\",fontsize=\"8\"];\n");
		// write the activities to dot
		for (int i = 0; i < activities.size(); i++) {
			ModelGraphVertex av = (ModelGraphVertex) activities.get(i);
			bw
					.write("node"
							+ av.getId()
							+ " [shape=\"box\", style=\"filled\",fillcolor=\"lavenderblush1\",label=\""
							+ av.getIdentifier() + "\",fontsize=6];\n");
			nodeMapping.put(new String("node" + av.getId()), av);
		}
		bw.write("}\n");
	}

	/**
	 * Adds a given activity vertex to this activity graph. <br>
	 * Note that the activity vertex is expected not to be associated to this
	 * activity graph already and this link will be established. If that link
	 * already has been established use {@link addActivityVertex(ActivityVertex
	 * av) addActivityVertex(ActivityVertex av))} instead.
	 * 
	 * @param av
	 *            ActivityVertex the activity vertex to be added
	 * @return ActivityVertex the newly added activity vertex
	 */
	// public ActivityVertex addAndLinkActivityVertex(ActivityVertex av) {
	// vertices.add(av);
	// av.setSubgraph(this);
	// activities.add(av);
	// return av;
	// }
	//
	// /**
	// * Makes a deep copy of the object, i.e., reconstructs the activity graph
	// with
	// * cloned activity vertices. Note that this method needs to be extended as
	// soon as
	// * there are attributes added to the class which are not primitive or
	// immutable.
	// * @return Object the cloned object.
	// */
	// public Object clone() {
	// ActivityGraph o = null;
	// o = (ActivityGraph)super.clone();
	//
	// // reset activities list
	// o.activities = new ArrayList<ActivityVertex>();
	//
	// Iterator activities = this.getActivityVertices().iterator();
	// while (activities.hasNext()) {
	// ActivityVertex av = (ActivityVertex) activities.next();
	// ActivityVertex clonedAv = (ActivityVertex) av.clone();
	// // cloned av must be told that is belongs to the cloned net
	// o.addAndLinkActivityVertex(clonedAv);
	// }
	// //
	// return o;
	// }
	// /**
	// * Returns the visualization of the activity graph for the given
	// perspectives
	// * @param perspectivesToShow Set the perspectives that need to be shown in
	// the activity graph
	// * @return ModelGraphPanel the visualization of the activity graph
	// */
	// public ModelGraphPanel getPanel(Set<HighLevelTypes.Perspective>
	// perspectivesToShow) {
	// this.perspectivesToShow = perspectivesToShow;
	// return this.getGrappaVisualization();
	// }
	// /**
	// * Sets the highlevelactivity set for this activity graph
	// * @param highLevelActivitySet HLActivitySet the hlActivitySet
	// */
	// public void setHLActivitySet(HLActivitySet highLevelActivitySet) {
	// this.hlActivitySet = highLevelActivitySet;
	// }
	//
	// public HighLevelProcess getHighLevelProcess() {
	// return hlActivitySet;
	// }
}
