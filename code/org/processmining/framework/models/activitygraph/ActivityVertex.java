package org.processmining.framework.models.activitygraph;

import org.processmining.framework.models.ModelGraphVertex;

/**
 * An activity vertex is a node in an activity graph.
 *
 * @see ActivityGraph
 *
 * @author rmans
 */
//public class ActivityVertex extends ModelGraphVertex implements Cloneable {
//
//	/**
//	 * Basic constructor.
//	 * @param actGraph ActivityGraph the activity graph the activity vertex belongs to
//	 */
//	public ActivityVertex(ActivityGraph actGraph) {
//		super(actGraph);
//	}
//
//	/**
//	 * Retrieves the activity graph to which this activity vertex belongs to
//	 * @return ActivityGraph the activity graph to which this activity vertex belongs to
//	 */
//	public ActivityGraph getActivityGraph() {
//		return (ActivityGraph) getSubgraph();
//	}
//
//	/**
//	 * Make a deep copy of the object.
//	 * Note that this method needs to be extended as soon as there are
//	 * attributes added to the class which are not primitive or immutable.
//	 * <br>
//	 * Note further that the belonging Activity graph is not cloned (so the cloned
//	 * object will point to the same one as this object).
//	 * Only the {@link ActivityGraph#clone ActivityGraph.clone()} method will update the
//	 * reference correspondingly.
//	 * @return the cloned object
//	 */
//	public Object clone() {
//		ActivityVertex o = null;
//		o = (ActivityVertex)super.clone();
//		return o;
//	}
//
//}
