package org.processmining.analysis.rolehierarchy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.processmining.framework.models.ModelGraph;
import org.processmining.framework.models.ModelGraphCluster;
import org.processmining.framework.models.ModelGraphEdge;
import org.processmining.framework.models.ModelGraphVertex;
import org.processmining.framework.models.ontology.ConceptModel;
import org.processmining.framework.models.ontology.OntologyModel;
import org.processmining.framework.util.Func;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix2D;

public class RoleHierarchy {

	public final static int MIN_NUMBER_LABELS_PER_NODE = 1;
	public final static int MAX_NUMBER_LABELS_PER_NODE = 5;

	public final static NodeLabelFormatter DEFAULT_FORMATTER = new NodeLabelFormatter() {
		public String getLabel(Set<String> nodeNames, boolean useShortLabels) {
			if (useShortLabels) {
				return Func.joinAtMost(MIN_NUMBER_LABELS_PER_NODE, nodeNames,
						"", "_et_al");
			} else {
				return Func.joinAtMost(MAX_NUMBER_LABELS_PER_NODE, nodeNames,
						"\\n", "... and ", " more ...");
			}
		}
	};

	private Map<Set<String>, Set<String>> taskset2originatorset;
	private int nodeCounter;
	private String name;
	private Map<String, TaskSet> originator2taskset;
	private NodeLabelFormatter nodeLabelFormatter;
	private Map<String, Set<String>> visibleTasks;
	private static int counter = 0;

	public RoleHierarchy(String name, Map<String, Set<String>> visibleTasks,
			NodeLabelFormatter nodeLabelFormatter) {
		this.name = name;
		this.visibleTasks = visibleTasks;
		this.nodeLabelFormatter = nodeLabelFormatter;
		this.originator2taskset = new HashMap<String, TaskSet>();
		this.taskset2originatorset = new HashMap<Set<String>, Set<String>>();
	}

	public int getMaximumFrequencyForAnyOriginator() {
		int result = 0;

		for (Map.Entry<String, TaskSet> item : originator2taskset.entrySet()) {
			for (Map.Entry<String, Integer> task : item.getValue().entrySet()) {
				if (visibleTasks.containsKey(item.getKey())
						&& visibleTasks.get(item.getKey()).contains(
								task.getKey())) {
					result = Math.max(result, task.getValue());
				}
			}
		}
		return result;
	}

	/**
	 * Creates an ontology from this role hierarchy and returns it. This method
	 * will return null if the ontology could not be created.
	 * 
	 * @param useTasks
	 *            True to use tasks as concept names, false to use originators
	 *            as concept names
	 * @return the ontology or null if the ontology could not be created
	 */
	public OntologyModel toOntology() {
		try {
			// FIXME This is a workaround because the ontology reasoner crashes
			// when we create more than one ontology with the same name.
			// So, we include a counter to make it unique.

			OntologyModel ontology = new OntologyModel(toModelGraph(false,
					false, true),
					"http://www.processmining.org/ontologies/RoleHierarchy-"
							+ counter++, "RoleHierarchy");
			for (Set<String> originatorSet : taskset2originatorset.values()) {
				String conceptName = DEFAULT_FORMATTER.getLabel(originatorSet,
						true);
				ConceptModel concept = ontology.findConcept(conceptName);

				for (String originator : originatorSet) {
					concept.addInstance(originator);
				}
			}
			return ontology;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public ModelGraph toModelGraph(boolean annotate) {
		return toModelGraph(annotate, true, false);
	}

	private ModelGraph toModelGraph(boolean annotate,
			boolean showArtificalRoot, boolean useShortLabels) {
		ModelGraph result = new ModelGraph(name);
		Map<Set<String>, ModelGraphVertex> nodes = new HashMap<Set<String>, ModelGraphVertex>();
		NodeLabelFormatter originatorFormatter = DEFAULT_FORMATTER;
		Map<Set<String>, Set<String>> annotations = new HashMap<Set<String>, Set<String>>();

		this.nodeCounter = 0; // used by createNode only

		// create a node for every set of tasks, and annotate that node with the
		// set of originators which perform that set of tasks
		for (Map.Entry<Set<String>, Set<String>> item : taskset2originatorset
				.entrySet()) {
			ModelGraphVertex node;
			Set<String> tasks = item.getKey();
			Set<String> originators = item.getValue();

			node = createNode(result, tasks, originators, "box", "cadetblue1",
					originatorFormatter, false, useShortLabels);
			nodes.put(tasks, node);
			annotations.put(tasks, new HashSet<String>(tasks));
		}

		// create an edge from node A to node B if the task set of A is a subset
		// of the task set of B
		for (Map.Entry<Set<String>, ModelGraphVertex> a : nodes.entrySet()) {
			Set<String> tasksA = a.getKey();

			for (Map.Entry<Set<String>, ModelGraphVertex> b : nodes.entrySet()) {
				Set<String> tasksB = b.getKey();

				if (tasksB.size() > tasksA.size() && tasksB.containsAll(tasksA)) {
					// tasksA < tasksB
					boolean isLargestSubset = true;
					for (Map.Entry<Set<String>, ModelGraphVertex> c : nodes
							.entrySet()) {
						Set<String> tasksC = c.getKey();

						if (tasksB.size() > tasksC.size()
								&& tasksB.containsAll(tasksC)
								&& tasksC.size() > tasksA.size()
								&& tasksC.containsAll(tasksA)) {
							// we've found a node C for which tasksA < tasksC <
							// tasksB, so A is not a largest possible subset of
							// B
							isLargestSubset = false;
							break;
						}
					}
					if (isLargestSubset) {
						ModelGraphEdge edge = result.addEdge(a.getValue(), b
								.getValue());
						edge.setDotAttribute("arrowtail", "normal");
						edge.setDotAttribute("arrowhead", "none");

						annotations.get(tasksB).removeAll(tasksA);
					}
				}
			}
		}

		if (showArtificalRoot) {
			List<ModelGraphVertex> rootNodes = new ArrayList<ModelGraphVertex>();

			for (ModelGraphVertex node : nodes.values()) {
				if (node.inDegree() == 0) {
					rootNodes.add(node);
				}
			}

			if (rootNodes.size() > 1) {
				// create a single root node
				ModelGraphVertex root = result.addVertex(new ModelGraphVertex(
						result));
				root.setIdentifier("__root_node");
				root.setDotAttribute("label", "Anybody");
				root.setDotAttribute("shape", "box");
				root.setDotAttribute("color", "lightgray");
				root.setDotAttribute("style", "filled");

				for (ModelGraphVertex node : rootNodes) {
					ModelGraphEdge edge = result.addEdge(root, node);
					edge.setDotAttribute("arrowtail", "normal");
					edge.setDotAttribute("arrowhead", "none");
				}
			}
		}

		if (annotate) {
			int id = 0;
			for (Set<String> tasks : taskset2originatorset.keySet()) {
				if (!annotations.get(tasks).isEmpty()) {
					ModelGraphVertex annotation;
					ModelGraphVertex node = nodes.get(tasks);

					annotation = createNode(result, tasks, annotations
							.get(tasks), "ellipse", "burlywood1",
							nodeLabelFormatter, false, useShortLabels);

					ModelGraphEdge edge = result.addEdge(node, annotation);
					edge.setDotAttribute("color", "gray");
					edge.setDotAttribute("constraint", "false");

					ModelGraphCluster cluster = new ModelGraphCluster("c"
							+ id++);
					cluster.setDotAttribute("fillcolor", "white");
					cluster.setDotAttribute("color", "white");
					cluster.addVertex(annotation);
					cluster.addVertex(node);
					result.addCluster(cluster);
				}
			}
		}

		return result;
	}

	private ModelGraphVertex createNode(ModelGraph graph, Set<String> tasks,
			Set<String> originators, String shape, String color,
			NodeLabelFormatter formatter, boolean showTasks,
			boolean useShortLabels) {
		ModelGraphVertex node = graph.addVertex(new ModelGraphVertex(graph));
		Set<String> labels;

		if (showTasks) {
			labels = new HashSet<String>();

			for (String originator : originators) {
				for (String task : tasks) {
					if (visibleTasks.containsKey(originator)
							&& visibleTasks.get(originator).contains(task)) {
						labels.add(task);
					}
				}
			}
		} else {
			labels = originators;
		}

		node.setIdentifier("node" + nodeCounter);
		node.setDotAttribute("label", formatter
				.getLabel(labels, useShortLabels));
		node.setDotAttribute("shape", shape);
		node.setDotAttribute("color", color);
		node.setDotAttribute("style", "filled");
		node.object2 = tasks;
		nodeCounter++;

		return node;
	}

	public void add(TaskSet set, String originator) {
		Set<String> tasks = set.getTasks();

		if (!taskset2originatorset.containsKey(tasks)) {
			taskset2originatorset.put(tasks, new TreeSet<String>());
		}
		taskset2originatorset.get(tasks).add(originator);

		originator2taskset.put(originator, set);
	}

	public TableModel getLocalOTMatrix(Set<String> taskSet) {
		return new LocalOTMatrix(originator2taskset, taskset2originatorset
				.get(taskSet), visibleTasks, nodeLabelFormatter);
	}

	public TableModel getFullOTMatrix() {
		return new LocalOTMatrix(originator2taskset, originator2taskset
				.keySet(), visibleTasks, nodeLabelFormatter);
	}
}

class LocalOTMatrix extends AbstractTableModel {

	private static final long serialVersionUID = 257540067695709523L;

	private DoubleMatrix2D table;
	private List<String> originators;
	private List<String> tasks;
	private NodeLabelFormatter nodeLabelFormatter;

	public LocalOTMatrix(Map<String, TaskSet> originator2taskset,
			Set<String> originatorSet, Map<String, Set<String>> visibleTasks,
			NodeLabelFormatter nodeLabelFormatter) {
		Set<String> taskSet = new HashSet<String>();

		for (Map.Entry<String, TaskSet> item : originator2taskset.entrySet()) {
			if (originatorSet.contains(item.getKey())) {
				for (String task : item.getValue().getTasks()) {
					if (visibleTasks.containsKey(item.getKey())
							&& visibleTasks.get(item.getKey()).contains(task)) {
						taskSet.add(task);
					}
				}
			}
		}

		Map<String, Integer> taskMapping = createSortedMap(taskSet);
		Map<String, Integer> origMapping = createSortedMap(originatorSet);

		this.tasks = new ArrayList<String>(taskMapping.keySet());
		this.originators = new ArrayList<String>(origMapping.keySet());
		this.table = DoubleFactory2D.sparse.make(origMapping.size(),
				taskMapping.size(), 0);
		this.nodeLabelFormatter = nodeLabelFormatter;

		for (Map.Entry<String, TaskSet> item : originator2taskset.entrySet()) {
			Integer origIndex = origMapping.get(item.getKey());

			if (origIndex != null) {
				for (Map.Entry<String, Integer> taskWithFreq : item.getValue()
						.entrySet()) {
					Integer taskIndex = taskMapping.get(taskWithFreq.getKey());

					if (taskIndex != null) {
						this.table.set(origIndex, taskIndex, taskWithFreq
								.getValue());
					}
				}
			}
		}
	}

	private Map<String, Integer> createSortedMap(Set<String> items) {
		List<String> sortedItems = new ArrayList<String>(items);
		Map<String, Integer> result = new TreeMap<String, Integer>();
		int index = 0;

		Collections.sort(sortedItems);

		for (String item : sortedItems) {
			result.put(item, index);
			index++;
		}
		return result;
	}

	public int getColumnCount() {
		return table.columns() + 1;
	}

	public int getRowCount() {
		return table.rows();
	}

	public Object getValueAt(int row, int col) {
		return col == 0 ? originators.get(row) : (int) table.get(row, col - 1);
	}

	public String getColumnName(int col) {
		if (col == 0) {
			return "O/T Matrix";
		}

		Set<String> nodeNames = new HashSet<String>();
		nodeNames.add(tasks.get(col - 1));

		return nodeLabelFormatter.getLabel(nodeNames, false);
	}
}
