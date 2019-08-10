package org.processmining.analysis.epc.transformEpcToCepc;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import org.processmining.framework.log.LogEvent;
import org.processmining.framework.models.epcpack.ConfigurableEPC;
import org.processmining.framework.models.epcpack.EPCConnector;
import org.processmining.framework.models.epcpack.EPCEdge;
import org.processmining.framework.models.epcpack.EPCEvent;
import org.processmining.framework.models.epcpack.EPCFunction;
import org.processmining.framework.models.epcpack.EPCObject;
import org.processmining.framework.plugin.ProvidedObject;
import org.processmining.framework.plugin.Provider;
import org.processmining.framework.ui.Message;
import org.processmining.framework.ui.UISettings;

import org.processmining.analysis.epc.epcmetrics.ControlFlow;
import org.processmining.analysis.epc.epcmetrics.Coupling;
import org.processmining.analysis.epc.epcmetrics.CrossConnectivity;
import org.processmining.analysis.epc.epcmetrics.Density;
import org.processmining.analysis.epc.epcmetrics.ICalculator;
import org.processmining.analysis.epc.epcmetrics.NumberOfANDs;
import org.processmining.analysis.epc.epcmetrics.NumberOfEvents;
import org.processmining.analysis.epc.epcmetrics.NumberOfFunctions;
import org.processmining.analysis.epc.epcmetrics.NumberOfORs;
import org.processmining.analysis.epc.epcmetrics.NumberOfXORs;
import org.processmining.analysis.epc.similarity.Checker;

/**
 * <p>
 * Title: TransformEpcToCepcResult
 * </p>
 * 
 * <p>
 * Description: combines TWO EPC's into an integrated EPC. Algorithm can be
 * extended in order to create an integrated C-EPC (not done yet, future work?)
 * </p>
 * 
 * @author Marijn Nagelkerke (mnagelkerke)
 * @version 1.2
 */

public class CombineEpcToCepcResult extends JPanel implements Provider {

	private static final long serialVersionUID = 3749047641742727468L;
	private final static int MAINDIVIDERLOCATION = 320;
	private final static double SYNEQUIVSCORE = 0.9;
	private final static boolean VALIDEPC = false;
	private final static int OR = 4;
	private final static int AND = 5;
	private final static int XOR = 6;

	private ConfigurableEPC epc1, epc2; // EPCs
	private ConfigurableEPC epcNew; // new EPC

	private ArrayList<EPCObject> ncfNodesEpc1, ncfNodesEpc2; // non-control flow
	// nodes
	private HashMap<Integer, Integer> mappingEpc1, mappingEpc2; // mapping of
	// ncf node ID's
	// to ncf nodes
	// of new EPC
	// ID's
	private int[][] relationMapping;

	// UI
	private JSplitPane paneMain;
	private JPanel leftPanel, model1Panel, model2Panel, cmodelPanel;
	private JTabbedPane tab;

	private JTable metrics;
	private JScrollPane metricsSp;

	public CombineEpcToCepcResult(ConfigurableEPC inputEpc1,
			ConfigurableEPC inputEpc2) {
		this.epc1 = inputEpc1;
		this.epc2 = inputEpc2;
		this.epcNew = new ConfigurableEPC(VALIDEPC);
		this.epcNew.setShowObjects(false, false, false);

		// set-up UI
		this.setLayout(new BorderLayout());
		this.leftPanel = new JPanel();
		this.model1Panel = new JPanel();
		this.model2Panel = new JPanel();
		this.cmodelPanel = new JPanel();
		this.model1Panel.setLayout(new BorderLayout());
		this.model2Panel.setLayout(new BorderLayout());
		this.cmodelPanel.setLayout(new BorderLayout());

		this.tab = new JTabbedPane(JTabbedPane.TOP);
		this.tab.add("EPC 1", model1Panel);
		this.tab.add("EPC 2", model2Panel);
		this.tab.add("Combined model", cmodelPanel);

		this.paneMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true,
				leftPanel, tab);
		this.paneMain.setDividerLocation(MAINDIVIDERLOCATION);

		// merge models
		mergeModels();

		// get some metrics and add UI
		String[] columnNames = { "Name", "Type", "Value" };
		Object[][] data = this.getMetricValues();

		this.metrics = new JTable(data, columnNames);
		this.metrics.setEnabled(false);
		this.metricsSp = new JScrollPane(metrics);
		this.leftPanel.add(this.metrics, JSplitPane.LEFT);

		// get graphical representation of the model and add it so we can see it
		this.model1Panel.add(
				new JScrollPane(this.epc1.getGrappaVisualization()),
				BorderLayout.CENTER);
		this.model2Panel.add(
				new JScrollPane(this.epc2.getGrappaVisualization()),
				BorderLayout.CENTER);
		this.cmodelPanel.add(new JScrollPane(this.epcNew
				.getGrappaVisualization()), BorderLayout.CENTER);

		this.add(paneMain, BorderLayout.CENTER);
	}

	/**
	 * start merging the input models
	 */
	private void mergeModels() {
		Message.add("Start: mergeModels()", Message.TEST);
		ArrayList<EPCObject> epcNodes;

		// add dummies to input models
		addDummies(this.epc1);
		addDummies(this.epc2);

		// combine function nodes and event nodes
		this.ncfNodesEpc1 = mergeFunctionsEvents(epc1);
		this.ncfNodesEpc2 = mergeFunctionsEvents(epc2);

		// get all ncf nodes
		match(this.ncfNodesEpc1, this.ncfNodesEpc2, this.epcNew);

		// count cnf nodes, create matrix
		Message.add("Start: create relation mapping", Message.TEST);
		epcNodes = createRelationMatrix();

		// validate connector types and add relations to the new EPC
		Message.add("Start: validate relations and add relations to new EPC",
				Message.TEST);
		reassessRelationMatrix();

		// visualize(); // visualize the combined ncf-matrix in system.out

		addRelations(epcNodes);

		// remove dummy nodes from EPCs
		removeDummies(this.epc1);
		removeDummies(this.epc2);
		removeDummies(this.epcNew);

		// remove possible redundant connector's
		Message.add("Start: remove redundant connector nodes", Message.TEST);
		removeRedundantConnectors();

		// System.out.println("new epc has " + this.epcNew.getFunctions().size()
		// + " functions and " + this.epcNew.getEvents().size()+
		// " events and " + this.epcNew.getEdges().size() + " relations and " +
		// this.epcNew.getConfigurableObjects().size() +
		// " configurable objects");

	}

	/**
	 * create and fill the relation matrix
	 */
	private ArrayList<EPCObject> createRelationMatrix() {
		Message.add("Start: createRelationMatrix()", Message.TEST);

		ArrayList<EPCObject> newEpcNodes = new ArrayList<EPCObject>();
		newEpcNodes.addAll(this.epcNew.getFunctions());
		newEpcNodes.addAll(this.epcNew.getEvents());
		int matrixSize = newEpcNodes.size();

		this.relationMapping = new int[matrixSize + 1][matrixSize + 1];

		addRelations(this.epc1, this.mappingEpc1);
		// this.FirstEPCDone = true;
		addRelations(this.epc2, this.mappingEpc2);

		return newEpcNodes;
	}

	/**
	 * add all relations from the relation matrix to the new EPC
	 * 
	 * @param ncfNodes
	 *            ; list of all functions and events
	 */
	private void addRelations(ArrayList<EPCObject> ncfNodes) {
		Message.add("Start: addRelations() -from matrix to EPC", Message.TEST);
		int matrixSize = this.relationMapping.length;

		for (int col = 0; col < matrixSize; col++) {
			EPCObject fromNode = getEpcObjectById(col, ncfNodes);
			for (int row = 0; row < matrixSize; row++) {
				EPCObject toNode = getEpcObjectById(row, ncfNodes);

				if (this.relationMapping[col][row] == 1) {
					// add relation between 'fromNode' and 'toNode'
					addRelation(fromNode, toNode);
				}
			}
		}
	}

	/**
	 * add the connector chain relations from specified EPC to the relation
	 * matrix
	 * 
	 * @param epc
	 * @param mapping
	 */
	private void addRelations(ConfigurableEPC epc,
			HashMap<Integer, Integer> mapping) {
		Message.add("Start: addRelations() -from EPC to matrix", Message.TEST);

		ArrayList<EPCObject> succNodes;
		ArrayList<EPCObject> ncfNodes = new ArrayList<EPCObject>();
		int connInOutId = this.relationMapping.length - 1;
		ncfNodes.addAll(epc.getFunctions());
		ncfNodes.addAll(epc.getEvents());

		for (Iterator<EPCObject> nI = ncfNodes.iterator(); nI.hasNext();) {
			EPCObject node = (EPCObject) nI.next();
			succNodes = getPostSet(node, true);
			int newFromId = (Integer) mapping.get(node.getId());

			// add conn-out if connector type is split
			int connOut = getConnectorType(node, true, false);
			if (connOut != -1) {
				int currentNode1 = this.relationMapping[newFromId][connInOutId];
				if (currentNode1 != 0)// there is already a connector stored
				{
					// reassessConnectorType ( node, current type, new type,
					// isSplit )
					this.relationMapping[newFromId][connInOutId] = reassessConnectorType(
							newFromId,
							this.relationMapping[newFromId][connInOutId],
							connOut, true);
				} else {
					this.relationMapping[newFromId][connInOutId] = connOut;
				}
			}

			for (Iterator<EPCObject> psI = succNodes.iterator(); psI.hasNext();) {
				EPCObject toNode = psI.next();
				int newToId = (Integer) mapping.get(toNode.getId());

				// add conn-in if connector type is join
				int connIn = getConnectorType(toNode, false, true);
				int currentNode2 = this.relationMapping[connInOutId][newToId];
				if (connIn != -1) {
					// if(currentNode2 != 0 && this.FirstEPCDone == true)
					if (currentNode2 != 0) {
						this.relationMapping[connInOutId][newToId] = reassessConnectorType(
								newToId,
								this.relationMapping[connInOutId][newToId],
								connIn, false);
					} else {
						this.relationMapping[connInOutId][newToId] = connIn;
					}
				}
				this.relationMapping[newFromId][newToId] = 1;
			}
		}
	}

	/**
	 * add a from-to relation to the new EPC
	 * 
	 * @param nodeFrom
	 * @param nodeTo
	 */
	private void addRelation(EPCObject nodeFrom, EPCObject nodeTo) {
		int fromId = nodeFrom.getId();
		int toId = nodeTo.getId();
		int matrixSize = this.relationMapping.length;

		EPCObject from = nodeFrom;
		EPCObject to = nodeTo;

		// check if from-node has a connout
		if (this.relationMapping[fromId][matrixSize - 1] != 0
				&& this.relationMapping[fromId][matrixSize - 1] != -1) {
			// get the fromNode's connector
			from = getConnector(from, false);
		}

		// check if to-node has a connin
		if (this.relationMapping[matrixSize - 1][toId] != 0
				&& this.relationMapping[matrixSize - 1][toId] != -1) {
			// get the toNode's connector
			to = getConnector(to, true);
		}

		this.epcNew.addEdge(from, to);
	}

	/**
	 * get the preceding/succeeding connector of specified node. If node has no
	 * connector, the node itself is returned
	 * 
	 * @param node
	 * @param isInConnector
	 * @return EPC object: connector or node
	 */
	private EPCObject getConnector(EPCObject node, boolean isInConnector) {
		ArrayList<EPCObject> ps = new ArrayList<EPCObject>();
		int connectorType = -1;

		// get postset if isInConnector=true, else get preset
		ps = (isInConnector == false ? getPostSet(node, false) : getPreSet(
				node, false));

		if (ps.size() == 1) {
			EPCObject psObject = ps.get(0);
			if (psObject instanceof EPCConnector) {
				// connector exists, return it
				return (EPCConnector) psObject;
			}
		} else {
			// connector doesn't exist, create new, add edge and return

			if (isInConnector == false) {
				connectorType = this.relationMapping[node.getId()][this.relationMapping.length - 1];
			} else {
				connectorType = this.relationMapping[this.relationMapping.length - 1][node
						.getId()];
			}

			EPCConnector conn = new EPCConnector(connectorType, false,
					this.epcNew);
			this.epcNew.addConnector(conn);

			if (isInConnector == true) {
				this.epcNew.addEdge(conn, node);
			} else {
				this.epcNew.addEdge(node, conn);
			}
			return conn;
		}

		return node;

	}

	/**
	 * checks and returns connector type if given node has a connector
	 * 
	 * @param node
	 * @param succeedingNodes
	 * @return int connector type
	 */
	private int getConnectorType(EPCObject node, boolean succeedingNodes,
			boolean isInConnector) {
		ArrayList<EPCObject> ps = new ArrayList<EPCObject>();
		if (succeedingNodes == true) {
			ps = getPostSet(node, false);
		} else {
			ps = getPreSet(node, false);
		}

		if (ps.size() == 1) {
			EPCObject psNode = ps.get(0);
			if (psNode instanceof EPCConnector) {
				EPCConnector conn = (EPCConnector) psNode;
				if (isInConnector == true && conn.inDegree() > 1) {
					// join type
					return conn.getType();
				} else if (isInConnector == false && conn.outDegree() > 1) {
					// split type
					return conn.getType();
				}
			}
		}
		return -1;
	}

	/**
	 * get the new connector type
	 * 
	 * @param currentConnectorType
	 * @param connectorType
	 * @return
	 */
	private int reassessConnectorType(int nodeId, int currentConnectorType,
			int newConnectorType, boolean isSplit) {
		switch (currentConnectorType) {
		case OR:
			return OR;
		case XOR:
			return (newConnectorType == XOR ? XOR : OR);
		case AND:
			ArrayList<EPCObject> psNode1,
			psNode2,
			nodeList1,
			nodeList2;
			ArrayList<Integer> newNodeIDsList1,
			newNodeIDsList2;
			EPCObject node1,
			node2;
			nodeList1 = new ArrayList<EPCObject>();
			nodeList2 = new ArrayList<EPCObject>();
			psNode1 = new ArrayList<EPCObject>();
			psNode2 = new ArrayList<EPCObject>();

			if (newConnectorType == AND) {
				int node1Id = getOldNodeIdFromMapping(nodeId, this.mappingEpc1);
				int node2Id = getOldNodeIdFromMapping(nodeId, this.mappingEpc2);

				if (node1Id != -1 && node2Id != -1) {
					nodeList1.addAll(this.epc1.getFunctions());
					nodeList1.addAll(this.epc1.getEvents());
					nodeList2.addAll(this.epc2.getFunctions());
					nodeList2.addAll(this.epc2.getEvents());

					node1 = getEpcObjectById(node1Id, nodeList1);
					node2 = getEpcObjectById(node2Id, nodeList2);

					if (isSplit == true) {
						System.out.println("split");
						psNode1 = getPostSet(node1, true);
						psNode2 = getPostSet(node2, true);
					} else {
						System.out.println("join");
						psNode1 = getPreSet(node1, true);
						psNode2 = getPreSet(node2, true);
					}

					// get new node id's from mappings
					newNodeIDsList1 = getNodeIDsFromMapping(psNode1,
							this.mappingEpc1);
					newNodeIDsList2 = getNodeIDsFromMapping(psNode2,
							this.mappingEpc2);

					if (newNodeIDsList1.containsAll(newNodeIDsList2)
							&& newNodeIDsList2.containsAll(newNodeIDsList1)) {
						return AND;
					} else {
						return OR;
					}
				} else {
					return AND;
				}
			} else {
				return OR;
			}
		}
		// fail-safe
		return newConnectorType;
	}

	/**
	 * validate the relations and connectors in the relation matrix. Adds
	 * connectors if a ncf node has more then one incoming or outgoing edge
	 */
	private void reassessRelationMatrix() {
		Message.add("Start: validateRelationMatrix()", Message.TEST);
		int connectorIndex = this.relationMapping.length - 1;
		int maxValueIndex = this.relationMapping.length - 2;
		int numRelations;
		int currentType;

		// validate connout
		for (int row = 0; row <= maxValueIndex; row++) {
			numRelations = 0;
			currentType = this.relationMapping[row][connectorIndex];

			for (int col = 0; col <= maxValueIndex; col++) {
				if (this.relationMapping[row][col] == 1) {
					numRelations++;
				}
			}

			// set relation of not already set
			if (numRelations > 1 && currentType != OR && currentType != AND
					&& currentType != XOR) {
				this.relationMapping[row][connectorIndex] = XOR;
			}
		}

		// validate connin
		for (int col = 0; col <= maxValueIndex; col++) {
			numRelations = 0;
			currentType = this.relationMapping[connectorIndex][col];
			for (int row = 0; row <= maxValueIndex; row++) {
				if (this.relationMapping[row][col] == 1) {
					numRelations++;
				}
			}

			// set relation of not already set
			if (numRelations > 1 && currentType != OR && currentType != AND
					&& currentType != XOR) {
				this.relationMapping[connectorIndex][col] = XOR;
			}
		}
	}

	/**
	 * returns the post-set of a ncf node if connectorChained is false, if true
	 * all connector chained ncf nodes are returned
	 * 
	 * @param node
	 * @param connectorChained
	 * @return
	 */
	private ArrayList<EPCObject> getPostSet(EPCObject node,
			boolean connectorChained) {
		ArrayList<EPCObject> ps = new ArrayList<EPCObject>();
		if (node == null) {
			return ps;
		} else {
			return getPostSet(node, ps, connectorChained);
		}
	}

	/**
	 * returns the post-set ncf nodes of the specified node
	 * 
	 * @param node
	 * @param postset
	 * @param connectorChained
	 * @return
	 */
	private ArrayList<EPCObject> getPostSet(EPCObject node,
			ArrayList<EPCObject> postset, boolean connectorChained) {
		HashSet<EPCObject> successors = node.getSuccessors();

		// travel edges
		for (Iterator<EPCObject> iS = successors.iterator(); iS.hasNext();) {
			Object obj = iS.next();
			if (obj instanceof EPCConnector) {
				// node is a connector
				EPCConnector sucCon = (EPCConnector) obj;
				if (connectorChained == true) {
					getPostSet(sucCon, postset, connectorChained);
				} else {
					postset.add(sucCon);
				}
			} else {
				// node is a function or event
				EPCObject sucNode = (EPCObject) obj;
				postset.add(sucNode);
			}
		}
		return postset;
	}

	/**
	 * getPreSet of node
	 * 
	 * @param the
	 *            node
	 * @param connectorChained
	 *            ; true if all next functions or events need to be returned;
	 *            false for all next nodes
	 * @return arraylist of all preceeding nodes
	 */
	private ArrayList<EPCObject> getPreSet(EPCObject node,
			boolean connectorChained) {
		ArrayList<EPCObject> ps = new ArrayList<EPCObject>();
		if (node == null) {
			return ps;
		} else {
			return getPreSet(node, ps, connectorChained);
		}
	}

	private ArrayList<EPCObject> getPreSet(EPCObject node,
			ArrayList<EPCObject> preset, boolean connectorChained) {
		HashSet<EPCObject> predecessors = node.getPredecessors();

		for (Iterator<EPCObject> iP = predecessors.iterator(); iP.hasNext();) {
			Object obj = iP.next();
			if (obj instanceof EPCConnector) {
				// node is connector
				EPCConnector preCon = (EPCConnector) obj;
				if (connectorChained == true) {
					getPreSet(preCon, preset, connectorChained);
				} else {
					preset.add(preCon);
				}
			} else {
				// node is a function or event
				EPCObject preNode = (EPCObject) obj;
				preset.add(preNode);
			}
		}
		return preset;
	}

	/**
	 * check EPC models for similar ncf nodes, create new ncf nodes, match ncf
	 * nodes from original EPC to new EPC
	 * 
	 * @param ncfNodeList1
	 * @param ncfNodeList2
	 * @param new EPC
	 */
	private void match(ArrayList<EPCObject> ncfNodeList1,
			ArrayList<EPCObject> ncfNodeList2, ConfigurableEPC epc) {
		Message.add("Start: match()", Message.TEST);
		this.mappingEpc1 = new HashMap<Integer, Integer>(ncfNodeList1.size());
		this.mappingEpc2 = new HashMap<Integer, Integer>(ncfNodeList2.size());
		int newNodeId;
		boolean equivalentFound;
		Checker syntacticChecker = new Checker(UISettings
				.getProMDirectoryPath()
				+ "lib"
				+ System.getProperty("file.separator")
				+ "plugins"
				+ System.getProperty("file.separator")
				+ "similarity"
				+ System.getProperty("file.separator"));

		// match ncf nodes
		for (Iterator<EPCObject> list1I = ncfNodeList1.iterator(); list1I
				.hasNext();) {
			EPCObject l1Node = (EPCObject) list1I.next();
			equivalentFound = false;

			Iterator<EPCObject> list2I = ncfNodeList2.iterator();
			while (equivalentFound == false && list2I.hasNext()) {
				EPCObject l2Node = (EPCObject) list2I.next();

				// do some check if two function/event nodes are equivalent
				// if similar/equivalent based on....label||syntactic
				// similarity(%) and not a dummy
				if ((l1Node.toString().startsWith("[_dummy_") == false)
						&& (l2Node.toString().startsWith("[_dummy_") == false)
						&& (syntacticChecker.syntacticEquivalenceScore(l1Node
								.toString().toLowerCase(), l2Node.toString()
								.toLowerCase()) >= SYNEQUIVSCORE)) {
					// equivalent
					newNodeId = newNcfNode(l1Node, false);

					this.mappingEpc1.put(l1Node.getId(), newNodeId);
					this.mappingEpc2.put(l2Node.getId(), newNodeId);

					list2I.remove();
					equivalentFound = true;
				} else if ((l1Node.toString().startsWith("[_dummy_") == true)
						&& (l2Node.toString().startsWith("[_dummy_") == true)
						&& checkIfDummiesMatch(l1Node, l2Node)) // is dummy...
				{
					// else if preset1==preset2 || postset1==postset2
					// new dummy
					newNodeId = newNcfNode(l1Node, false);
					// add mappings to dummy
					this.mappingEpc1.put(l1Node.getId(), newNodeId);
					this.mappingEpc2.put(l2Node.getId(), newNodeId);

					list2I.remove();
					equivalentFound = true;
				}
			}

			// no equivalent found
			if (equivalentFound == false) {
				this.mappingEpc1.put(l1Node.getId(), newNcfNode(l1Node, true));
				list1I.remove();
			} else {
				list1I.remove();
			}
		}

		// add remaining function/event nodes from other EPC
		if (ncfNodeList2.size() > 0) {
			for (Iterator<EPCObject> remList2I = ncfNodeList2.iterator(); remList2I
					.hasNext();) {
				EPCObject remL2Node = (EPCObject) remList2I.next();
				this.mappingEpc2.put(remL2Node.getId(), newNcfNode(remL2Node,
						true));
			}
		}
	}

	/**
	 * creates a new non-control flow node. Node is added to the new EPC
	 * 
	 * @param oldNode
	 * @param isConfigurable
	 * @return
	 */
	private int newNcfNode(EPCObject oldNode, boolean isConfigurable) {
		String ncfNodeLabel = oldNode.toString();
		int newId;
		if (oldNode instanceof EPCFunction) {
			EPCFunction newFunction = new EPCFunction(new LogEvent(
					ncfNodeLabel, ""), isConfigurable, this.epcNew);
			newFunction.setIdentifier(ncfNodeLabel);
			this.epcNew.addFunction(newFunction);

			newId = newFunction.getId();
		} else if (oldNode instanceof EPCEvent) {
			EPCEvent newEvent = new EPCEvent(ncfNodeLabel, this.epcNew);
			newEvent.setIdentifier(ncfNodeLabel);
			this.epcNew.addEvent(newEvent);

			newId = newEvent.getId();
		} else {
			// ncf node is not a function/event!
			newId = -1;
		}
		return newId;
	}

	/**
	 * check if two dummy events match
	 * 
	 * @param dummy1
	 * @param dummy2
	 * @return true if dummies match, else return false
	 */
	private boolean checkIfDummiesMatch(EPCObject dummy1, EPCObject dummy2) {
		ArrayList<EPCObject> presetD1 = getPreSet(dummy1, true);
		ArrayList<EPCObject> presetD2 = getPreSet(dummy2, true);
		ArrayList<EPCObject> postsetD1 = getPostSet(dummy1, true);
		ArrayList<EPCObject> postsetD2 = getPostSet(dummy2, true);

		if (isDummySubsetOf(presetD1, presetD2)
				|| isDummySubsetOf(presetD2, presetD1)) {
			return true;
		} else if (isDummySubsetOf(postsetD1, postsetD2)
				|| isDummySubsetOf(postsetD2, postsetD1)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Checks if set 2 contains all elements of set 1, or if set 2 is a subset
	 * of set 1
	 * 
	 * @param set1
	 * @param set2
	 * @return true if set 2 contains all elements from set 1 or if set 2 is a
	 *         subset of set 1. Else returns false
	 */
	private boolean isDummySubsetOf(ArrayList<EPCObject> set1,
			ArrayList<EPCObject> set2) {
		for (Iterator<EPCObject> s1I = set1.iterator(); s1I.hasNext();) {
			EPCObject s1Node = (EPCObject) s1I.next();
			for (Iterator<EPCObject> s2I = set2.iterator(); s2I.hasNext();) {
				EPCObject s2Node = (EPCObject) s2I.next();
				if (s1Node.toString().equalsIgnoreCase(s2Node.toString())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * add temporary dummy events to the EPC if connector node has a succeeding
	 * connector
	 * 
	 * @param epc
	 * @return Configurable EPC with, potential, temporary dummy events
	 */
	private ConfigurableEPC addDummies(ConfigurableEPC epc) {
		int id = 1;
		ArrayList<EPCConnector> connList = epc.getConnectors();

		for (Iterator<EPCConnector> cI = connList.iterator(); cI.hasNext();) {
			EPCConnector conn = (EPCConnector) cI.next();
			HashSet<EPCObject> succ = conn.getSuccessors();

			for (Iterator<EPCObject> sI = succ.iterator(); sI.hasNext();) {
				EPCObject node = (EPCObject) sI.next();
				if (node instanceof EPCConnector) {
					// connector has a succeeding connector
					addDummy(id, conn, node, epc);
					id = id + 1;
				}
			}
		}
		return epc;
	}

	/**
	 * 
	 * @param epc
	 */
	private void removeDummies(ConfigurableEPC epc) {
		ArrayList<EPCObject> ncfNodes = new ArrayList<EPCObject>();
		ncfNodes.addAll(epc.getEvents());

		for (Iterator<EPCObject> nodeI = ncfNodes.iterator(); nodeI.hasNext();) {
			EPCObject node = (EPCObject) nodeI.next();
			if (node.toString().startsWith("[_dummy_")) {
				// remove dummy event and add new relation
				try {
					ArrayList<EPCObject> preset = getPreSet(node, false);
					ArrayList<EPCObject> postset = getPostSet(node, false);
					EPCObject from = (EPCObject) preset.get(0);
					EPCObject to = (EPCObject) postset.get(0);

					epc.addEdge(from, to);
					epc.delEvent((EPCEvent) node);
				} catch (Exception e) {
					Message.add(
							"Execption when removing dummy nodes from EPC model "
									+ epc.getName(), Message.ERROR);
				}
			}
		}
	}

	/**
	 * add a dummy event and edges between 'from' to 'to' removes prior relation
	 * between 'from' and 'to'
	 * 
	 * @param from
	 * @param to
	 */
	private void addDummy(int id, EPCObject from, EPCObject to,
			ConfigurableEPC epc) {
		EPCEvent dummy = new EPCEvent("[_dummy_" + id + "_]", epc);
		epc.addEvent(dummy);
		epc.addEdge(from, dummy);
		epc.addEdge(dummy, to);
		deleteEdge(from, to, epc);
	}

	/**
	 * delete the edge between two EPC edges
	 * 
	 * @param edgeFrom
	 * @param edgeTo
	 * @param epc
	 */
	private void deleteEdge(EPCObject edgeFrom, EPCObject edgeTo,
			ConfigurableEPC epc) {
		HashSet<EPCEdge> edges = epc.getEdgesBetween(edgeFrom, edgeTo);

		for (Iterator<EPCEdge> eI = edges.iterator(); eI.hasNext();) {
			EPCEdge edge = (EPCEdge) eI.next();
			epc.removeEdge(edge);
		}
	}

	/**
	 * removes all connector nodes that have only one incoming and one outgoing
	 * edge or have one edge
	 */
	private void removeRedundantConnectors() {
		ArrayList<EPCConnector> connectors = this.epcNew.getConnectors();

		for (Iterator<EPCConnector> cI = connectors.iterator(); cI.hasNext();) {
			EPCConnector con = (EPCConnector) cI.next();
			// if # of incoming and outgoing edges is one then remove the
			// connector and make a new edge to replace the connector
			if (con.inDegree() == 1 && con.outDegree() == 1) {
				// get the pre-node and suc-node
				ArrayList<EPCObject> preset = getPreSet(con, false); // holds
				// from
				ArrayList<EPCObject> postset = getPostSet(con, false); // holds
				// to

				EPCObject fromNode = (EPCObject) preset.get(0); // at offset 0 ?
				EPCObject toNode = (EPCObject) postset.get(0);
				this.epcNew.addEdge(fromNode, toNode);

				cI.remove();
				this.epcNew.delConnector(con);
			} else if (con.inDegree() == 0 || con.outDegree() == 0) {
				cI.remove();
				this.epcNew.delConnector(con);
			}
		}
	}

	/**
	 * merge the function and event nodes in one arraylist
	 * 
	 * @param epc
	 * @param nodes
	 * @return arraylist containing all function and event nodes
	 */
	private ArrayList<EPCObject> mergeFunctionsEvents(ConfigurableEPC epc) {
		ArrayList<EPCObject> nodes = new ArrayList<EPCObject>();
		nodes.addAll(epc.getFunctions());
		nodes.addAll(epc.getEvents());

		return nodes;
	}

	/**
	 * get the EPC object by ID from the provided arraylist
	 * 
	 * @param nodeId
	 * @param epcNodes
	 * @return found EPCObject or null
	 */
	private EPCObject getEpcObjectById(int nodeId, ArrayList<EPCObject> epcNodes) {
		ArrayList<EPCObject> ncfNodes = epcNodes;
		for (Iterator<EPCObject> nI = ncfNodes.iterator(); nI.hasNext();) {
			EPCObject node = (EPCObject) nI.next();
			if (node.getId() == nodeId) {
				return node;
			}
		}
		return null;
	}

	/**
	 * get the old/original node ID from the mapping
	 * 
	 * @param newNodeId
	 *            (the 'new' node ID)
	 * @param mapping
	 * @return old node's ID
	 */
	private int getOldNodeIdFromMapping(int newNodeId,
			HashMap<Integer, Integer> mapping) {
		for (Iterator<Integer> mI = mapping.keySet().iterator(); mI.hasNext();) {
			int key = mI.next();
			int value = mapping.get(key);

			if (value == newNodeId) {
				return key;
			}
		}
		return -1;
	}

	/**
	 * get the new node ID's from the mapping
	 * 
	 * @param oldNodes
	 * @param mapping
	 * @return arraylist containing the new node ID's
	 */
	private ArrayList<Integer> getNodeIDsFromMapping(
			ArrayList<EPCObject> oldNodes, HashMap<Integer, Integer> mapping) {
		ArrayList<Integer> newNodeIDs = new ArrayList<Integer>();

		for (Iterator<EPCObject> listI = oldNodes.iterator(); listI.hasNext();) {
			EPCObject node = (EPCObject) listI.next();

			if (mapping.containsKey(node.getId())) {
				newNodeIDs.add(mapping.get(node.getId()));
			}
		}

		return newNodeIDs;
	}

	/**
	 * get some statistics (see EPC Complexity Plug-in)
	 * 
	 * @return
	 */
	private String[][] getMetricValues() {
		ICalculator calc;
		calc = new ControlFlow(this.epcNew);
		String[] out1 = new String[3];
		out1[0] = calc.getName();
		out1[1] = calc.getType();
		out1[2] = calc.Calculate();

		calc = new Density(this.epcNew);
		String[] out2 = new String[3];
		out2[0] = calc.getName();
		out2[1] = calc.getType();
		out2[2] = calc.Calculate();

		calc = new NumberOfFunctions(this.epcNew);
		String[] out4 = new String[3];
		out4[0] = calc.getName();
		out4[1] = calc.getType();
		out4[2] = calc.Calculate();

		calc = new NumberOfEvents(this.epcNew);
		String[] out5 = new String[3];
		out5[0] = calc.getName();
		out5[1] = calc.getType();
		out5[2] = calc.Calculate();

		calc = new NumberOfORs(this.epcNew);
		String[] out6 = new String[3];
		out6[0] = calc.getName();
		out6[1] = calc.getType();
		out6[2] = calc.Calculate();

		calc = new Coupling(this.epcNew);
		String[] out7 = new String[3];
		out7[0] = calc.getName();
		out7[1] = calc.getType();
		out7[2] = calc.Calculate();

		calc = new NumberOfXORs(this.epcNew);
		String[] out8 = new String[3];
		out8[0] = calc.getName();
		out8[1] = calc.getType();
		out8[2] = calc.Calculate();

		calc = new NumberOfANDs(this.epcNew);
		String[] out9 = new String[3];
		out9[0] = calc.getName();
		out9[1] = calc.getType();
		out9[2] = calc.Calculate();

		calc = new CrossConnectivity(this.epcNew);
		String[] out10 = new String[3];
		out10[0] = calc.getName();
		out10[1] = calc.getType();
		out10[2] = calc.Calculate();

		String[] out11 = new String[3];
		out11[0] = "Num of Edges";
		out11[1] = "Size";
		out11[2] = "" + this.epcNew.getEdges().size();

		// String[][] final_output = {out1,out2, out10,
		// out7,out4,out5,
		// out6, out8, out9, out11
		// };
		String[][] final_output = { out4, out5, out6, out8, out9, out11 };
		return final_output;
	}

	/**
	 * used to provide the created objects back to the framework
	 */
	public ProvidedObject[] getProvidedObjects() {
		ArrayList<Object> objects = new ArrayList<Object>();
		if (this.epc1 != null) {
			objects.add(new ProvidedObject("EPC Model 1",
					new Object[] { this.epc1 }));
		}
		if (this.epc2 != null) {
			objects.add(new ProvidedObject("EPC Model 2",
					new Object[] { this.epc2 }));
		}
		if (this.epcNew != null) {
			objects.add(new ProvidedObject("Transformed EPC",
					new Object[] { this.epcNew }));
		}
		ProvidedObject[] res = new ProvidedObject[objects.size()];
		for (int i = 0; i < objects.size(); i++) {
			res[i] = (ProvidedObject) objects.get(i);
		}
		return res;
	}

	/*
	 * visualize the ncf-matrix in System.out
	 */
	private void visualize() {
		System.out.println("--[print relation matrix]--------------");
		for (int row = 0; row < this.relationMapping.length; row++) {
			for (int col = 0; col < this.relationMapping.length; col++) {
				System.out.print("  " + this.relationMapping[row][col]);
			}

			System.out.println("");
		}
		System.out.println("--------------------------");
	}
}