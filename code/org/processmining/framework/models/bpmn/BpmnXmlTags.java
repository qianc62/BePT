/**
 *
 */
package org.processmining.framework.models.bpmn;

/**
 * @author JianHong.YE, collaborate with LiJie.WEN and Feng XML tags used in
 *         BPMN files
 */
public class BpmnXmlTags {
	// diagram
	public static String BPMN_DIAGRAM = "diagram";

	/**
	 * Tags
	 */
	// event
	public static String BPMN_START = "Start";
	public static String BPMN_INTERMEDIATE = "Intermediate";
	public static String BPMN_END = "End";

	// Task
	public static String BPMN_TASK = "Task";

	// Sub process
	public static String BPMN_SUBPROCESS = "SubProcess";

	// lane and pool
	public static String BPMN_LANE = "Lane";
	public static String BPMN_POOL = "Pool";

	// flow and message
	public static String BPMN_FLOW = "Flow";
	public static String BPMN_MESSAGE = "Message";

	// gateway
	public static String BPMN_GATEWAY = "Gateway";

	/**
	 * Property
	 */
	// event
	public static String BPMN_PROPERTY = "property";
	public static String BPMN_PROP_NAME = "Name";
	public static String BPMN_PROP_X = "x";
	public static String BPMN_PROP_Y = "y";
	public static String BPMN_PROP_SDMX = "sdm:x";
	public static String BPMN_PROP_SDMY = "sdm:y";
	public static String BPMN_PROP_HEIGHT = "height";
	public static String BPMN_PROP_WIDTH = "width";
	public static String BPMN_PROP_CSSCLASS = "CSSclass";
	public static String BPMN_PROP_TRIGGER = "Trigger";
	public static String BPMN_PROP_LANE = "Lane";
	public static String BPMN_PROP_INTERRUPT = "Interrupt";

	// flow (edge)
	public static String BPMN_PROP_DEFAULT = "Default";
	public static String BPMN_PROP_MESSAGE = "Message";
	public static String BPMN_PROP_CONDITION = "Condition";
	public static String BPMN_PROP_GATEWAYPOSITION = "FromGatewayPosition";

	// swim lane
	public static String BPMN_PROP_LENGTH = "Length";
	public static String BPMN_PROP_SIZE = "Size";
	public static String BPMN_PROP_MAXLEVEL = "MaxLevel";

	// gateway
	public static String BPMN_PROP_GATEWAYTYPE = "GatewayType";
	public static String BPMN_PROP_GW_XORTYPE = "XORType";

	// activity
	public static String BPMN_PROP_LOOPTYPE = "LoopType";
	public static String BPMN_PROP_TIMING = "Timing";
	public static String BPMN_PROP_COMPENSATIONACTIVITY = "CompensationActivity";
	public static String BPMN_PROP_ADHOC = "AdHoc";
	public static String BPMN_PROP_EXPANDED = "Expanded";
	public static String BPMN_PROP_TRANSACTION = "Transaction";
	/**
	 * Attributes
	 */
	public static String BPMN_ATTR_NAME = "name";
	public static String BPMN_ATTR_ID = "id";
	public static String BPMN_ATTR_ISLINK = "islink";
	public static String BPMN_ATTR_FROM = "from";
	public static String BPMN_ATTR_TO = "to";

	/**
	 * CSSclass
	 */
	public static String BPMN_CSSC_EVENT = "Event";
	public static String BPMN_CSSC_ACTIVITY = "Activity";
	public static String BPMN_CSSC_TASK = "Task";

	// used by converse bpmn to yawl
	public static String BPMN_NODETYPE = "NodeType";
	public static String BPMN_POOLLANE = "PoolLane";
	public static String BPMN_EVENTTYPE = "EventType";
	public static String BPMN_TASKTYPE = "TaskType";
	public static String BPMN_EDGETYPE = "EdgeType";
	public static String BPMN_SUBTYPE = "SubType";
	public static String BPMN_GWT_AND = "and";
	public static String BPMN_GWT_OR = "or";
	public static String BPMN_GWT_XOR = "xor";
	public static String BPMN_GWT_COMPLEX = "complex";
	public static String BPMN_GWT_NORMAL = "normal";

	public static String BPMN_ORIGINAL = "Original";
	public static String BPMN_MANUAL = "Manual";

	public static String BPMN_POOLS = "Pools";
	public static String BPMN_LANES = "Lanes";

	public static String[] EVENTYPE = { BPMN_NODETYPE, BPMN_POOLLANE,
			BPMN_EVENTTYPE, BPMN_PROP_TRIGGER };
	public static String[] TASKTYPE = { BPMN_TASKTYPE, BPMN_POOLLANE,
			BPMN_SUBTYPE, BPMN_PROP_LOOPTYPE, BPMN_PROP_TIMING,
			BPMN_PROP_COMPENSATIONACTIVITY, BPMN_PROP_ADHOC,
			BPMN_PROP_EXPANDED, BPMN_PROP_TRANSACTION };
	public static String[] EDGETYPE = { BPMN_EDGETYPE, BPMN_PROP_CONDITION,
			BPMN_PROP_DEFAULT, BPMN_PROP_MESSAGE };

}
