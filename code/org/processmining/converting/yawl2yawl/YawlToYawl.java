/**
 * <p>Title: YawlToYawl</p>
 *
 * <p>Description: YawlToYawl conversions</p>
 *
 * <p>Copyright: Copyright (c) 2008</p>
 *
 * <p>Company: TU/e</p>
 *
 * @author Eric Verbeek
 * @version 1.0
 */

package org.processmining.converting.yawl2yawl;

import org.processmining.converting.Converter;
import org.processmining.framework.models.yawl.*;
import java.util.HashMap;
import java.util.ArrayList;
import org.processmining.mining.yawlmining.YAWLResult;

public class YawlToYawl {

	/*
	 * Prefixes to be used when a composite taks sis split into a start and end
	 * task.
	 */
	private final static String START_STRING = "START_";
	private final static String END_STRING = "END_";

	/**********************************************************************
	 * Help functions.
	 */

	/*
	 * It seems a bad diea to map object to objects. Better is to map ids to
	 * objects. However, when flattening a model, objects from multiple graphs
	 * have to mapped onto object from a singel graph, and the ids or object
	 * from different graphs are not necessarily unique. For this reason, we
	 * introduced a tempId for the YAWL objcts. When mapping a YAWL object to
	 * some object, a fresh id is generated, which is stored with the YAWL
	 * object. Next, the id is mapped onto the given object.
	 */
	private static Integer id = 1;

	/**
	 * Get the unique Id from a node name. If a node name has from X_Y_Z, then Z
	 * is the unique Id. Note that the Id is only unique within the node's
	 * graph.
	 * 
	 * @param nodeName
	 *            String The node name.
	 * @return Integer The unique Id.
	 */
	private static Integer getId(String nodeName) {
		Integer id = -1;
		int index = nodeName.length();
		while ("0123456789".contains(nodeName.substring(index - 1, index))) {
			index--;
		}
		if (index < nodeName.length()) {
			id = Integer.valueOf(nodeName.substring(index));
		}
		return id;
	}

	/**
	 * Get the engine path to the current decomposition.
	 * 
	 * @param decomposedTask
	 *            ArrayList The context of the current decomposition.
	 * @return String The engine path.
	 */
	private static String getPath(ArrayList<YAWLTask> decomposedTask) {
		String path = "";
		for (int i = 0; i < decomposedTask.size(); i++) {
			YAWLTask task = decomposedTask.get(i);
			String taskName = task.getID();
			path += (path.length() == 0 ? "" : ".") + getId(taskName);
		}
		if (path.length() > 0) {
			path += ":";
		}
		return path;
	}

	/**********************************************************************
	 * Adding implicit conditions.
	 */

	@Converter(name = "YAWL: Add implicit conditions", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:yawl2yawl:implicit")
	public static YAWLResult addImplicitConditions(YAWLModel model) {
		HashMap map = new HashMap();
		YawlToYawlSettings settings = new YawlToYawlSettings();
		YAWLResult result = new YAWLResult(null, addImplicitConditions(model,
				settings, map));
		return result;
	}

	/**
	 * Make a copy of the given YAWL model that contains all implicit
	 * conditions.
	 * 
	 * @param model
	 *            YAWLModel The YAWL model to copy.
	 * @param settings
	 *            YawlToYawlSettings The settings to use while copying.
	 * @param map
	 *            HashMap The map to fill (every YAWL object will be mapped onto
	 *            its copy in this map).
	 * @return YAWLModel The copy of the YAWL model.
	 */
	public static YAWLModel addImplicitConditions(YAWLModel model,
			YawlToYawlSettings settings, HashMap map) {
		YAWLModel newModel = new YAWLModel(model);
		map.put(model, newModel);
		/*
		 * Convert all decompositions..
		 */
		for (YAWLDecomposition decomposition : model.getDecompositions()) {
			YAWLDecomposition newDecomposition = new YAWLDecomposition(
					decomposition);
			newModel.addDecomposition(newDecomposition.getID(),
					newDecomposition);
			map.put(decomposition, newDecomposition);
			addImplicitConditions(model, decomposition, settings, map);
		}
		return newModel;
	}

	/**
	 * Copy the decomposition, replace implicit conditions by real conditions.
	 * 
	 * @param model
	 *            YAWLModel The YAWL model.
	 * @param decomposition
	 *            YAWLDecomposition The decomposition to copy.
	 * @param settings
	 *            YawlToYawlSettings The settings to use while copying.
	 * @param map
	 *            HashMap Maps every object to its copy.
	 */
	private static void addImplicitConditions(YAWLModel model,
			YAWLDecomposition decomposition, YawlToYawlSettings settings,
			HashMap map) {
		/*
		 * First, get the copy of the decomposition.
		 */
		YAWLDecomposition copyOfDecomposition = (YAWLDecomposition) map
				.get(decomposition);
		/*
		 * Second, copy all nodes. Take not of all Ids, as we will have to
		 * generate an Id for every implicit condition later on.
		 */
		// Integer maxId = 0;
		for (YAWLNode node : decomposition.getNodes()) {
			/*
			 * Update maxId if necessary.
			 */
			// String conditionName = node.getID();
			// Integer id = getId(conditionName);
			// if (id > maxId) {
			// maxId = id;
			// }
			if (node instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) node;
				YAWLCondition copyOfCondition = copyOfDecomposition
						.addCondition(condition);
				map.put(condition, copyOfCondition);
			} else if (node instanceof YAWLTask) {
				YAWLTask task = (YAWLTask) node;
				YAWLTask copyOfTask = copyOfDecomposition.addTask(task);
				map.put(task, copyOfTask);
			}
		}
		/*
		 * Third, copy all edges, but introduce a Condition for a Task-Task
		 * edge.
		 */
		for (Object object : decomposition.getEdges()) {
			YAWLEdge edge = (YAWLEdge) object;
			YAWLNode fromNode = (YAWLNode) map.get((YAWLNode) edge.getSource());
			YAWLNode toNode = (YAWLNode) map.get((YAWLNode) edge.getDest());
			if ((fromNode instanceof YAWLTask) && (toNode instanceof YAWLTask)
					&& edge.isNormal()) {
				/*
				 * Create a new condition.
				 */
				// maxId++; // Get a fresh Id for this new condition.
				String constructedName = "c{" + fromNode.getIdentifier() + "_"
						+ toNode.getIdentifier() + "}";
				YAWLCondition condition = copyOfDecomposition
						.addCondition(constructedName);
				// condition.setIdentifier(constructedName);
				/*
				 * Create an edge from the fromNode to the new condition. Copy
				 * the old edge data.
				 */
				YAWLEdge copyOfEdge = copyOfDecomposition.addEdge(fromNode,
						condition, edge);
				map.put(edge, copyOfEdge);
				/*
				 * Create an edge from the new condition to the toNode. Create
				 * default edge data.
				 */
				YAWLEdge dummyEdge = new YAWLEdge(condition, toNode, false,
						null, "0");
				YAWLEdge extraEdge = copyOfDecomposition.addEdge(condition,
						toNode, dummyEdge);
				decomposition.removeEdge(dummyEdge);
			} else {
				/*
				 * Copy the original edge.
				 */
				YAWLEdge copyOfEdge = copyOfDecomposition.addEdge(fromNode,
						toNode, edge);
				map.put(edge, copyOfEdge);
			}
		}
	}

	/**********************************************************************
	 * Flatten the model.
	 */

	@Converter(name = "YAWL: Flatten model", help = "http://www.win.tue.nl/~hverbeek/doku.php?id=projects:prom:plug-ins:conversion:yawl2yawl:flatten")
	public static YAWLResult flattenModel(YAWLModel model) {
		HashMap map = new HashMap();
		YawlToYawlSettings settings = new YawlToYawlSettings();
		YAWLResult result = new YAWLResult(null, flattenModel(model, settings,
				map));
		return result;
	}

	/**
	 * Make a flat copy of the given YAWL model, starting from the root
	 * decomposition.
	 * 
	 * @param model
	 *            YAWLModel The given YAWL model.
	 * @param settings
	 *            YawlToYawlSettings The settings to use while copying.
	 * @param map
	 *            HashMap Maps every YAWL object onto its copy. Composite tasks
	 *            are mapped onto an array list containing a start task and an
	 *            end task.
	 * @return YAWLModel The falt copy of the YAWL model.
	 */
	public static YAWLModel flattenModel(YAWLModel model,
			YawlToYawlSettings settings, HashMap map) {
		YAWLModel newModel = new YAWLModel(model);
		map.put(model, newModel);
		ArrayList<YAWLTask> decomposedTask = new ArrayList<YAWLTask>();
		HashMap<YAWLTask, YAWLCondition> inputMap = new HashMap<YAWLTask, YAWLCondition>();
		HashMap<YAWLTask, YAWLCondition> outputMap = new HashMap<YAWLTask, YAWLCondition>();
		/*
		 * Convert all decompositions..
		 */
		for (YAWLDecomposition decomposition : model.getDecompositions()) {
			if (decomposition.isRoot()) {
				YAWLDecomposition newDecomposition = new YAWLDecomposition(
						decomposition);
				newModel.addDecomposition(newDecomposition.getID(),
						newDecomposition);
				map.put(decomposition, newDecomposition);
				flattenDecomposition(model, decomposition, decomposedTask,
						settings, map, inputMap, outputMap);
			}
		}
		return newModel;
	}

	/**
	 * Flat-copy the decomposition.
	 * 
	 * @param model
	 *            YAWLModel The YAWL model.
	 * @param decomposition
	 *            YAWLDecomposition The decomposition to flat-copy.
	 * @param decomposedTask
	 *            ArrayList The context of the current decomposition.
	 * @param settings
	 *            YawlToYawlSettings The settings to use while flat-copying.
	 * @param map
	 *            HashMap Maps every object to its copy.
	 * @param inputMap
	 *            HashMap Maps every composed task to its input condition.
	 * @param outputMap
	 *            HashMap Likewise, output condition.
	 */
	private static void flattenDecomposition(YAWLModel model,
			YAWLDecomposition decomposition,
			ArrayList<YAWLTask> decomposedTask, YawlToYawlSettings settings,
			HashMap map, HashMap<YAWLTask, YAWLCondition> inputMap,
			HashMap<YAWLTask, YAWLCondition> outputMap) {
		/*
		 * First, get the copy of decomposition.
		 */
		YAWLDecomposition copyOfDecomposition = (YAWLDecomposition) map
				.get(decomposition);
		/*
		 * Second, copy all nodes.
		 */
		for (YAWLNode node : decomposition.getNodes()) {
			if (node instanceof YAWLCondition) {
				YAWLCondition condition = (YAWLCondition) node;
				String oldID = condition.getID();
				/*
				 * Prefix the name of the condition with its engine path.
				 */
				String prefix = getPath(decomposedTask);
				condition.setID(prefix + oldID);
				YAWLCondition copyOfCondition = copyOfDecomposition
						.addCondition(condition);
				condition.setID(oldID);
				map.put(condition, copyOfCondition);
				/*
				 * Update inputMap and outputMap if necessary.
				 */
				if (decomposedTask.size() > 0) {
					/*
					 * Condition is in some task composition.
					 */
					if (copyOfCondition.isInputCondition()) {
						inputMap.put(decomposedTask
								.get(decomposedTask.size() - 1),
								copyOfCondition);
						copyOfCondition.normalize();
					} else if (copyOfCondition.isOutputCondition()) {
						outputMap.put(decomposedTask
								.get(decomposedTask.size() - 1),
								copyOfCondition);
						copyOfCondition.normalize();
					}
				}
			} else if (node instanceof YAWLTask) {
				YAWLTask task = (YAWLTask) node;
				if (model.isComposite(task.getDecomposition())) {
					/*
					 * Split the composite task into a start and end task.
					 */
					ArrayList<YAWLTask> copyOfTask = new ArrayList<YAWLTask>(2);
					String oldID = task.getID();
					task.setID(START_STRING + getPath(decomposedTask) + oldID);
					YAWLTask copyOfTask0 = copyOfDecomposition.addTask(task);
					task.setID(END_STRING + getPath(decomposedTask) + oldID);
					YAWLTask copyOfTask1 = copyOfDecomposition.addTask(task);
					task.setID(oldID);
					copyOfTask.add(0, copyOfTask0);
					copyOfTask.add(1, copyOfTask1);
					map.put(task, copyOfTask);
					/*
					 * Now convert the composite task.
					 */
					YAWLDecomposition taskDecomposition = model
							.getDecomposition(task.getDecomposition());
					map.put(taskDecomposition, copyOfDecomposition);
					decomposedTask.add(task);
					flattenDecomposition(model, taskDecomposition,
							decomposedTask, settings, map, inputMap, outputMap);
					decomposedTask.remove(task);
					/*
					 * Last, add edges to connect the decomposition.
					 */
					YAWLEdge dummyEdge = new YAWLEdge(copyOfTask0, inputMap
							.get(task), false, null, "0");
					YAWLEdge copyOfEdge0 = copyOfDecomposition.addEdge(
							copyOfTask0, inputMap.get(task), dummyEdge);
					YAWLEdge copyOfEdge1 = copyOfDecomposition.addEdge(
							outputMap.get(task), copyOfTask1, dummyEdge);
					decomposition.removeEdge(dummyEdge);
				} else {
					String oldID = task.getID();
					String prefix = getPath(decomposedTask);
					task.setID(prefix + oldID);
					YAWLTask copyOfTask = copyOfDecomposition.addTask(task);
					task.setID(oldID);
					map.put(task, copyOfTask);
				}
			}
		}
		/*
		 * Third, copy all edges.
		 */
		for (Object object : decomposition.getEdges()) {
			YAWLEdge edge = (YAWLEdge) object;
			YAWLNode fromNode;
			YAWLNode toNode;
			boolean isSimple = true;
			if (map.get((YAWLNode) edge.getSource()) instanceof ArrayList) {
				/*
				 * Composite task. Get end task.
				 */
				ArrayList<YAWLTask> startEndTask = (ArrayList<YAWLTask>) map
						.get((YAWLNode) edge.getSource());
				fromNode = startEndTask.get(1);
			} else {
				fromNode = (YAWLNode) map.get((YAWLNode) edge.getSource());
			}
			if (map.get((YAWLNode) edge.getDest()) instanceof ArrayList) {
				/*
				 * Composite task. Get start task.
				 */
				ArrayList<YAWLTask> startEndTask = (ArrayList<YAWLTask>) map
						.get((YAWLNode) edge.getDest());
				toNode = startEndTask.get(0);
				if (!edge.isNormal()) {
					/*
					 * Uh-oh, a reset arc to a composite task.
					 */
					isSimple = false;
					YAWLTask resetTask = (YAWLTask) edge.getDest();
					addResetEdges(model, copyOfDecomposition, fromNode,
							resetTask, edge, map);
				}
			} else {
				toNode = (YAWLNode) map.get((YAWLNode) edge.getDest());
			}
			if (isSimple) {
				YAWLEdge copyOfEdge = copyOfDecomposition.addEdge(fromNode,
						toNode, edge);
				map.put(edge, copyOfEdge);
			}
		}
	}

	/**
	 * Add reset edges for all nodes in the composite task.
	 * 
	 * @param model
	 *            YAWLModel The given model.
	 * @param copyOfDecomposition
	 *            YAWLDecomposition The decomposition to add the reset edges to.
	 * @param fromNode
	 *            YAWLNode The source node of the reset edge.
	 * @param resetTask
	 *            YAWLTask The composite task which is the target of the
	 *            original reset edge.
	 * @param edge
	 *            YAWLEdge The original reset edge.
	 * @param map
	 *            HashMap Maps every YAWL object onto its copy.
	 */
	private static void addResetEdges(YAWLModel model,
			YAWLDecomposition copyOfDecomposition, YAWLNode fromNode,
			YAWLTask resetTask, YAWLEdge edge, HashMap map) {
		ArrayList<YAWLTask> compositeTask = (ArrayList<YAWLTask>) map
				.get(resetTask);
		copyOfDecomposition.addEdge(fromNode, compositeTask.get(0), edge);
		copyOfDecomposition.addEdge(fromNode, compositeTask.get(1), edge);
		YAWLDecomposition resetDecomposition = model.getDecomposition(resetTask
				.getDecomposition());
		for (YAWLNode resetNode : resetDecomposition.getNodes()) {
			if (map.get(resetNode) instanceof ArrayList) {
				/*
				 * Found a composite task inside the composite task. Recursion.
				 */
				addResetEdges(model, copyOfDecomposition, fromNode,
						(YAWLTask) resetNode, edge, map);
			} else {
				copyOfDecomposition.addEdge(fromNode, (YAWLNode) map
						.get(resetNode), edge);
			}
		}
	}
}
