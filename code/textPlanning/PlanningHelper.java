package textPlanning;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.log4j.nt.NTEventLogAppender;
//import org.apache.xpath.axes.ChildIterator;
import org.jbpt.bp.BehaviouralProfile;
import org.jbpt.bp.construct.BPCreatorNet;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.Place;
import org.jbpt.petri.Transition;
import org.jbpt.petri.unfolding.OccurrenceNet;
import org.jbpt.pm.FlowNode;
import org.jbpt.pm.structure.ProcessModel2NetSystem;
import org.jbpt.throwable.TransformationException;

//import com.gnu.hcode.in_c;

import dataModel.p2t.WFnet2Processes;
import dataModel.petri.ProcessCover;
import dataModel.process.Activity;
import dataModel.process.Annotation;
import dataModel.process.EventType;
import dataModel.process.GatewayType;
import dataModel.process.Lane;
import dataModel.process.ProcessModel;
import de.hpi.bpt.graph.Edge;
import de.hpi.bpt.graph.algo.rpst.RPST;
import de.hpi.bpt.graph.algo.rpst.RPSTEdge;
import de.hpi.bpt.graph.algo.rpst.RPSTNode;
import de.hpi.bpt.graph.algo.tctree.TCType;
import de.hpi.bpt.hypergraph.abs.Vertex;
import de.hpi.bpt.process.ControlFlow;
import de.hpi.bpt.process.Event;
import de.hpi.bpt.process.Gateway;
import de.hpi.bpt.process.Node;
import java_cup.runtime.virtual_parse_stack;

public class PlanningHelper {

	/**
	 * Creates an order for the top level of a given RPST Tree.
	 */
	public static ArrayList<RPSTNode<ControlFlow, Node>> sortTreeLevel( RPSTNode<ControlFlow, Node> lnode, Node startElem, RPST<ControlFlow, Node> rpst) {

		if (PlanningHelper.isSplit(lnode, rpst)) { ArrayList<RPSTNode<ControlFlow, Node>> unordered = new ArrayList<RPSTNode<ControlFlow, Node>>();

			if (rpst.getChildren((lnode)).size() != 2) {
				unordered.addAll(rpst.getChildren((lnode)));
				return unordered;
			} else {
				unordered.addAll(rpst.getChildren((lnode)));
				if (getDepth(unordered.get(0), rpst) > getDepth(
						unordered.get(1), rpst)) {
					ArrayList<RPSTNode<ControlFlow, Node>> ordered = new ArrayList<RPSTNode<ControlFlow, Node>>();
					ordered.add(unordered.get(1));
					ordered.add(unordered.get(0));
					return ordered;
				} else {
					ArrayList<RPSTNode<ControlFlow, Node>> ordered = new ArrayList<RPSTNode<ControlFlow, Node>>();
					ordered.addAll(rpst.getChildren((lnode)));
					return ordered;
				}
			}
		}

		Collection<RPSTNode<ControlFlow, Node>> topNodes = rpst.getChildren(lnode);
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = new ArrayList<RPSTNode<ControlFlow, Node>>();

		if (isRigid(lnode)) {
			//By Qc
			for ( RPSTNode<ControlFlow, Node> node_ : topNodes ){
				orderedTopNodes.add( node_ );
			}
			//
			return orderedTopNodes;
		}

		Node currentElem = startElem;
		while (orderedTopNodes.size() < topNodes.size()) {
			for (RPSTNode<ControlFlow, Node> node : topNodes) {
				if (node.getEntry().equals(currentElem)) {
					orderedTopNodes.add(node);
					currentElem = node.getExit();
					break;
				}
			}
		}
		return orderedTopNodes;
	}

	/**
	 * Returns String representation of node.
	 */
	public static String getNodeRepresentation(Node n) {
		String s = "";
		if (PlanningHelper.isEvent(n)) {
			s = "Event + (" + n.getId() + ")";
		} else if (PlanningHelper.isGateway(n)) {
			Gateway g = (Gateway) n;
			if (g.isAND()) {
				s = "AND (" + n.getId() + ")";
			}
			if (g.isXOR()) {
				if (g.getName().equals("")) {
					s = "XOR (" + n.getId() + ")";
				}
				s = g.getName() + "(XOR," + n.getId() + ")";

			}
			if (g.isOR()) {
				if (g.getName().equals("")) {
					s = "OR (" + n.getId() + ")";
				}
				s = g.getName() + "(OR," + n.getId() + ")";

			}
		} else {
			s = n.toString();
		}
		return s;
	}

	/**
	 * Returns amount of nodes of the next level in the RPST.
	 */
	public static int getSubLevelCount(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		return rpst.getChildren(node).size();
	}

	/**
	 * Returns amount of nodes on the current RPST level.
	 */
	public static int getNodeCount(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (PlanningHelper.isTrivial(node)) {
			return 0;
		} else {
			Collection<RPSTNode<ControlFlow, Node>> children = rpst
					.getChildren(node);
			int sum = 0;
			for (RPSTNode<ControlFlow, Node> child : children) {
				sum = sum + 1 + getNodeCount(child, rpst);
			}
			return sum;
		}
	}

	/**
	 * Compute depth of a given component.
	 */
	public static int getDepth(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		int depth = getDepthHelper(node, rpst);
		if (depth > 1) {
			return depth - 1;
		} else {
			return depth;
		}
	}

	/**
	 * Helper for depth computation.
	 */
	public static int getDepthHelper(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (node.getName().startsWith("T")) {
			return 0;
		}
		ArrayList<Integer> depthValues = new ArrayList<Integer>();
		for (RPSTNode<ControlFlow, Node> n : rpst.getChildren(node)) {
			depthValues.add(getDepthHelper(n, rpst) + 1);
		}
		return Collections.max(depthValues);
	}

	/**
	 * Returns type of given bond.
	 */
	public static String getBondType(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isEventSplit(bond, rpst)) {
			return "EVENTBASED";
		}
		if (isANDSplit(bond, rpst)) {
			return "AND";
		}
		if (isXORSplit(bond, rpst)) {
			return "XOR";
		}
		if (isORSplit(bond, rpst)) {
			return "OR";
		}
		if (isSkip(bond, rpst)) {
			return "Skip";
		}
		if (isLoop(bond, rpst)) {
			return "Loop";
		}
		return "";
	}

	/**
	 * Decides whether a given bond is a loop (Arc from exit gateway to entry
	 * gateway).
	 */
	public static boolean isLoop(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		
		System.out.println( isBond(bond) );
		System.out.println( isGateway(bond.getEntry()) );
		System.out.println( ((Gateway) bond.getEntry()).isXOR() );
		
		if (isBond(bond) && isGateway(bond.getEntry())
				&& ((Gateway) bond.getEntry()).isXOR()) {
			for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
				if (isTrivial(node) && node.getEntry().equals(bond.getExit())
						&& node.getExit().equals(bond.getEntry())
						&& isGateway(node.getExit())) {
					return true;
				}
				if (bond.getEntry().equals(node.getExit())
						&& isGateway(node.getExit())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Decides whether a given bond is a skip (Arc from entry gateway to exit
	 * gateway).
	 */
	public static boolean isSkip(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())
				&& ((Gateway) bond.getEntry()).isXOR() && isSplit(bond, rpst)) {
			for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
				if (isTrivial(node) && node.getEntry().equals(bond.getEntry())
						&& node.getExit().equals(bond.getExit())
						&& isGateway(node.getExit())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Decides whether a given bond is a skip (All arcs are outgoing).
	 */
	public static boolean isSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		for (RPSTNode<ControlFlow, Node> node : rpst.getChildren((bond))) {
			if (node.getEntry() != bond.getEntry()) {
				return false;
			}
		}
		return true;
	}

	public static boolean isEventSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ((Gateway) bond.getEntry()).isEventBased()
					&& isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an AND split.
	 */
	public static boolean isANDSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ((Gateway) bond.getEntry()).isAND() && isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given gatewy is an XOR split.
	 */
	public static boolean isIsolatedXORSplit(RPSTNode<ControlFlow, Node> node,
			RPST<ControlFlow, Node> rpst) {
		if (isGateway(node.getEntry())) {
			return (((Gateway) node.getEntry()).isXOR()) && isSplit(node, rpst)
					&& !isSkip(node, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an XOR split.
	 */
	public static boolean isXORSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return (((Gateway) bond.getEntry()).isXOR()) && isSplit(bond, rpst)
					&& !isSkip(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given bond is an OR split.
	 */
	public static boolean isORSplit(RPSTNode<ControlFlow, Node> bond,
			RPST<ControlFlow, Node> rpst) {
		if (isBond(bond) && isGateway(bond.getEntry())) {
			return ( ( ((Gateway) bond.getEntry()).isOR() ) || ( ((Gateway) bond.getEntry()).isCGT() ) ) && isSplit(bond, rpst);
		}
		return false;
	}

	/**
	 * Decides whether a given component is a Bond.
	 */
	public static boolean isBond(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("B");
	}

	/**
	 * Decides whether a given component is a trivial one.
	 */
	public static boolean isTrivial(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("T");
	}

	/**
	 * Decides whether a given component is a Rigid.
	 */
	public static boolean isRigid(RPSTNode<ControlFlow, Node> node) {
		return node.getName().startsWith("R");
	}

	/**
	 * Decides whether a given node is a gateway.
	 */
	public static boolean isGateway(Node node) {
		System.out.println(node.getClass().toString());
		return node.getClass().toString()
				.equals("class de.hpi.bpt.process.Gateway");
	}

	/**
	 * Decides whether considered event is an end event
	 */
	public static boolean isEndEvent(Object o, ProcessModel process) {
		if (o.getClass().toString().equals("class de.hpi.bpt.process.Event") == true) {
			dataModel.process.Event event = process.getEvents().get(
					Integer.valueOf(((Event) o).getId()));
			if (event.getType() == EventType.END_EVENT) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Return true if o is an HPI event
	 */
	public static boolean isEvent(Object o) {
		return o.getClass().toString().equals("class de.hpi.bpt.process.Event");
	}

	/**
	 * Returns true if o is a HPI task
	 */
	public static boolean isTask(Object o) {
		return o.getClass().toString().equals("class de.hpi.bpt.process.Task");
	}

	/**
	 * Chekcs whether bond stays in the same lane.
	 */
	private boolean staysInLane(RPSTNode<ControlFlow, Node> bond, Lane lane,
			ProcessModel process, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(bond, bond.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && PlanningHelper.isTrivial(node)) {
				int id = Integer.valueOf(node.getEntry().getId());
				if (process.getActivites().containsKey(id)) {
					Lane currentLane = process.getActivites().get(id).getLane();
					if (currentLane.getName().equals(lane.getName()) == false) {
						return false;
					}
				}
			} else {
				boolean stays = staysInLane(node, lane, process, rpst);
				if (stays == false) {
					return false;
				}
			}
		}
		return true;
	}

	//R存在问题,改为qc_getRunSequencesFromRPSTFragment测试
	public static ArrayList<ArrayList<String>> getRunSequencesFromRPSTFragment(
			RPSTNode<ControlFlow, Node> node, ProcessModel process) {

		ArrayList<ArrayList<String>> runSequences = new ArrayList<ArrayList<String>>();

		// Transforming RPST subtree to Petri Net
		org.jbpt.pm.ProcessModel pm = new org.jbpt.pm.ProcessModel();
		HashMap<Integer, FlowNode> elements = new HashMap<Integer, FlowNode>();
		HashMap<String, String> orignalMapping = new HashMap<String, String>();

		// Transform Rigid into jbpt process model
		for (Node n : node.getFragment().getVertices()) {
			orignalMapping.put(n.getId(), n.getName());
			if (process.getGateways().containsKey(Integer.valueOf(n.getId()))) {
				int type = process.getGateways()
						.get(Integer.valueOf(n.getId())).getType();

				if (type == GatewayType.AND) {
					org.jbpt.pm.AndGateway gw = new org.jbpt.pm.AndGateway(
							n.getId());
					gw.setId(n.getId());
					elements.put(Integer.valueOf(n.getId()), gw);
				}
				if (type == GatewayType.XOR) {
					org.jbpt.pm.XorGateway gw = new org.jbpt.pm.XorGateway(
							n.getId());
					gw.setId(n.getId());
					elements.put(Integer.valueOf(n.getId()), gw);
				}
			} else {
				org.jbpt.pm.Activity a = new org.jbpt.pm.Activity(n.getId());
				a.setId(n.getId());
				elements.put(Integer.valueOf(n.getId()), a);
			}
		}

		for (de.hpi.bpt.graph.abs.AbstractDirectedEdge arc : node.getFragment()
				.getEdges()) {
			int sID = Integer.valueOf(arc.getSource().getId());
			int tID = Integer.valueOf(arc.getTarget().getId());
			pm.addControlFlow(elements.get(sID), elements.get(tID));
		}

		NetSystem netSystem = null;
		try {
			netSystem = ProcessModel2NetSystem.transform(pm);
		} catch (TransformationException e1) {
			e1.printStackTrace();
		}
		netSystem.loadNaturalMarking();
		
		//By Chen Qian
//		Set<Place> places = netSystem.getPlaces();
//		Set<Transition> transitions = netSystem.getTransitions();
//		Collection<Flow> flows = netSystem.getFlow();
//		System.out.println( "---------------------" );
//		for( Place place : places ){
//			System.out.println( "Place: " + place.getId() );
//		}
//		for( Transition transition : transitions ){
//			System.out.println( "Transition: " + transition.getId() );
//		}
//		for( Flow flow : flows ){
//			System.out.println( "Flow: " + flow.getSource().getId() + " -> " + flow.getTarget().getId() );
//		}
//		System.out.println( "---------------------" );
		
		ProcessCover fps = new ProcessCover(netSystem);

		int c = 0;

		for (dataModel.petri.Process p : fps.getCorrectProcesses()) {
			c++;

			// Create NetSystem from PetriNet
			NetSystem ns = new NetSystem();
			for (Flow flow : p.getCausalNet().getEdges()) {
				ns.addFlow(flow.getSource(), flow.getTarget());
			}
			ns.loadNaturalMarking();

			// Save run (NetSystem) as png
			// dotSource = serializer.serialize(ns);
			// try {
			// IOUtils.invokeDOT("src", c+".png", dotSource);
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			// add single start node if run contains several
			int startNodes = 0;
			for (org.jbpt.petri.Node n : ns.getNodes()) {
				if (ns.getDirectPredecessors(n).size() == 0) {
					startNodes++;
				}
			}
			if (startNodes > 1) {
				org.jbpt.petri.Node newN = new Place();
				ns.addNode(newN);
				for (org.jbpt.petri.Node n : ns.getNodes()) {
					if (ns.getDirectPredecessors(n).size() == 0 && n != newN) {
						ns.addEdge(newN, n);
					}
				}
			}

			// Compute behavioural profile
			BehaviouralProfile<NetSystem, org.jbpt.petri.Node> bp = BPCreatorNet
					.getInstance().deriveRelationSet(ns);

			// Go through all nodes and add relevant elems
			ArrayList<org.jbpt.petri.Node> relevantNodes = new ArrayList<org.jbpt.petri.Node>();
			for (org.jbpt.petri.Node n : ns.getNodes()) {
				if (orignalMapping.containsKey(n.getName())) {
					String oldProcessID = n.getName();

					// Add relevant activities
					for (org.jbpt.pm.Activity a : pm.getActivities()) {
						if (a.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
					// Add relevant gateways
					for (org.jbpt.pm.Gateway g : pm.getGateways()) {
						if (g.getId().equals(oldProcessID)) {
							relevantNodes.add(n);
						}
					}
				}
			}

			// Go through relevant nodes and build up run sequence (assumption:
			// only sequence, no concurrent branches)
			ArrayList<org.jbpt.petri.Node> runSequence = new ArrayList<org.jbpt.petri.Node>();
			for (org.jbpt.petri.Node relNode : relevantNodes) {

				// If list is empty
				if (runSequence.size() == 0) {
					runSequence.add(relNode);
				} else {

					// If element is not already part of the list
					if (!runSequence.contains(relNode)) {

						// Determine correct position of elem
						for (int i = 0; i < runSequence.size(); i++) {
							String rel = bp.getRelationForEntities(
									runSequence.get(i), relNode).toString();
							if (rel.equals("<-")) {
								runSequence.add(i, relNode);
								break;
							}
							if (i == (runSequence.size() - 1)) {
								runSequence.add(relNode);
								break;
							}
						}
					}
				}
			}

			// Convert to a list of IDs
			ArrayList<String> runSequenceIDs = new ArrayList<String>();
			for (int i = 0; i < runSequence.size(); i++) {
				runSequenceIDs.add(runSequence.get(i).getName());
			}
			runSequences.add(runSequenceIDs);
		}

		//Note By Chen Qian
//		WFnet2Processes wf2pi = null;
//		try {
//			wf2pi = new WFnet2Processes(netSystem);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		for (List<org.jbpt.petri.Node> l: wf2pi.getPetriNetPaths()) {
//		
//			// Go through all nodes and add relevant elems
//			ArrayList<org.jbpt.petri.Node> relevantNodes = new ArrayList<org.jbpt.petri.Node>();
//			for (org.jbpt.petri.Node n : l) {
//				if (orignalMapping.containsKey(n.getName())) {
//					String oldProcessID = n.getName();
//
//					// Add relevant activities
//					for (org.jbpt.pm.Activity a : pm.getActivities()) {
//						if (a.getId().equals(oldProcessID)) {
//							relevantNodes.add(n);
//						}
//					}
//					// Add relevant gateways
//					for (org.jbpt.pm.Gateway g : pm.getGateways()) {
//						if (g.getId().equals(oldProcessID)) {
//							relevantNodes.add(n);
//						}
//					}
//				}
//			}
//		}

		//By Chen Qian
//		runSequences.clear();
//		ArrayList<String> arr1 = new ArrayList<String>();
//		arr1.add( "6" );arr1.add( "14" );arr1.add( "11" );arr1.add( "18" );arr1.add( "20" );
//		runSequences.add( arr1 );
//		ArrayList<String> arr2 = new ArrayList<String>();
//		arr2.add( "11" );arr2.add( "9" );arr2.add( "16" );arr2.add( "20" );
//		runSequences.add( arr2 );
		
		return runSequences;
	}

	//qc_getRunSequencesFromRPSTFragment方法
	public static ArrayList<ArrayList<RPSTNode<ControlFlow, Node>>> qc_getRunSequencesFromRPSTFragment(
			RPSTNode<ControlFlow, Node> node, ProcessModel process, RPST<ControlFlow, Node> rpst) throws IOException {
		
		//算法过程:
			//建立带权值的DAG图
			//标记数组
			//通过Floyed求出最长路径
				//标记路径结点
				//把路径加入Gpath中
				//把Gpath加入Gpathes中
			//返回Gpathes
		
		ArrayList<ArrayList<RPSTNode<ControlFlow, Node>>> gpathes = new ArrayList<ArrayList<RPSTNode<ControlFlow, Node>>>();
		
		//建立带权值的DAG图
		//建立定点集和边集
		ArrayList<Integer> vertexs = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> edges = new ArrayList<ArrayList<Integer>>();
		
		Object[] children = rpst.getChildren( node ).toArray();
		for( int i=0 ; i<children.length ; i++ ){
			RPSTNode<ControlFlow, Node> child = ((RPSTNode<ControlFlow, Node>)children[i]);
			
			Integer v1 = new Integer( Integer.parseInt( child.getEntry().getId() ) );
			Integer v2 = new Integer( Integer.parseInt( child.getExit().getId() ) );
			
			ArrayList<Integer> edge = new ArrayList<Integer>();
			edge.add( v1 );
			edge.add( v2 );
			edge.add( getEdgesNum( child , rpst ) );
			edges.add( edge );
			
			if( inArrayList( v1 , vertexs ) == false ){
				vertexs.add( v1 );
			}
			if( inArrayList( v2 , vertexs ) == false ){
				vertexs.add( v2 );
			}
		}
		
		System.out.println( "DAG：" );
		for( Integer vertex : vertexs ){
			System.out.print( vertex + "  " );
		}
		System.out.println();
		for( ArrayList<Integer> edge : edges ){
			System.out.println( edge.get( 0 ) + "->" + edge.get( 1 ) + " : " + edge.get( 2 ) + "条边" );
		}
		System.out.println();
		
		//创建Map
		int vertexNum = vertexs.size();
		int[][] map = new int[vertexNum][vertexNum];
		for( int i=0 ; i<map.length ; i++ ){
			Arrays.fill( map[i] , Integer.MIN_VALUE );
		}
		for( ArrayList<Integer> edge : edges ){
			int u = getSmallerNum( edge.get(0) , vertexs );
			int v = getSmallerNum( edge.get(1) , vertexs );
			map[u][v] = edge.get( 2 );
		}
		
		//通过Floyed求出最长路径
		int restEdgeNum = edges.size();
		while( restEdgeNum > 0 ){
			printMap( map );
			ArrayList<Integer> path = Floyed( vertexs , edges , map );
			System.out.println( path );
			if( path.size() == 0 ){
				break;
			}
			ArrayList<RPSTNode<ControlFlow, Node>> pathNode = createRPSTNode( process , rpst , node , path);
			deletePath( map , path , vertexs );
			restEdgeNum -= path.size() - 1;
			gpathes.add( pathNode );
		}
		
		return gpathes;
	}

	public static Integer getEdgesNum( RPSTNode<ControlFlow, Node> node , RPST<ControlFlow, Node> rpst ){
		
		if( node.getType() == TCType.T ){
			return 1;
		}
		
		int num = 0;
		Object[] children = rpst.getChildren( node ).toArray();
		for( int i=0 ; i<children.length ; i++ ){
			RPSTNode<ControlFlow, Node> child = ((RPSTNode<ControlFlow, Node>)children[i]);
			num += getEdgesNum( child , rpst );
		}
		return num;
	}

	public static void printMap( int[][] map ){
		//打印地图
		System.out.println();
		for( int i=0 ; i<map.length ; i++ ){
			for( int j=0 ; j<map[i].length ; j++ ){
				if( map[i][j] == Integer.MIN_VALUE ){
					System.out.print( "**" + " " );
				}
				else{
					System.out.print( map[i][j] + "  " );
				}
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static ArrayList<Integer> Floyed( ArrayList<Integer> vertexs , ArrayList<ArrayList<Integer>> edges , final int[][] map ){
		
		int[][] map1 = new int[map.length][];
		for( int i=0 ; i<map1.length ; i++ ){
			map1[i] = map[i].clone();
		}
		
		int[][] backPointer = new int[map1.length][map1.length];
		for( int i=0 ; i<backPointer.length ; i++ ){
			Arrays.fill( backPointer[i] , -1 );
		}
		
		for( int k=0 ; k<map1.length ; k++ ){
			for( int u=0 ; u<map1.length ; u++ ){
				for( int v=0 ; v<map1.length ; v++ ){
					if( map1[u][k] < 0 || map1[k][v] < 0 ){//防止整数范围越界
						continue;
					}
					if( map1[u][k] + map1[k][v] > map1[u][v] ){
						map1[u][v] = map1[u][k] + map1[k][v];
						backPointer[u][v] = k;
					}
				}
			}	
		}
		
		int max = Integer.MIN_VALUE , max_u = -1 , max_v = -1;
		for( int i=0 ; i<map1.length ; i++ ){
			for( int j=0 ; j<map1[i].length ; j++ ){
				if( map1[i][j] > max ){
					max = map1[i][j];
					max_u = i;
					max_v = j;
				}
			}
		}
		
		ArrayList<Integer> path = new ArrayList<Integer>();
		boolean[] flag = new boolean[vertexs.size()];
		Arrays.fill( flag , false );
		createPath( backPointer , max_u , max_v , path , flag );
		
		ArrayList<Integer> pathId = new ArrayList<Integer>();
		for( Integer ii : path ){
			pathId.add( vertexs.get( ii ) );
		}
		
		return pathId;
	}
	
	public static void createPath( int[][] bp , int u , int v , ArrayList<Integer> path , boolean[] flag ){

		if( bp[u][v] < 0 ){
			if( flag[u] == false ){
				path.add( u );
				flag[u] = true;
			}
			if( flag[v] == false ){
				path.add( v );
				flag[v] = true;
			}
			return ;
		}
		createPath( bp , u , bp[u][v] , path , flag );
		createPath( bp , bp[u][v] , v , path , flag );
	}
	
	public static void deletePath( int[][] map , ArrayList<Integer> path , ArrayList<Integer> vertexs ){
		Object[] paths = path.toArray();
		for( int i=0 ; i<paths.length - 1 ; i++ ){
			int u = getSmallerNum( path.get( i ) , vertexs );
			int v = getSmallerNum( path.get( i + 1 ) , vertexs );
			map[u][v] = -Integer.MIN_VALUE;
		}
	}
	
	public static ArrayList<RPSTNode<ControlFlow, Node>> createRPSTNode(
			ProcessModel process, RPST<ControlFlow, Node> rpst , RPSTNode<ControlFlow, Node> node , ArrayList<Integer> path ){
		
		ArrayList<RPSTNode<ControlFlow, Node>> pathNode = new ArrayList<RPSTNode<ControlFlow, Node>>(); 
		
		for( int i=0 ; i<path.size()-1 ; i++ ){
			int u = path.get( i );
			int v = path.get( i + 1 );
			Object[] children = rpst.getChildren( node ).toArray();
			for( int j=0 ; j<children.length ; j++ ){
				RPSTNode<ControlFlow, Node> child = ((RPSTNode<ControlFlow, Node>)children[j]);
				if( Integer.parseInt( child.getEntry().getId() ) == u &&
					Integer.parseInt( child.getExit().getId() ) == v ){
					pathNode.add( child );
					break;
				}
			}
			if( i == children.length ){
				System.out.println( "R型Fragment形成RPSTNode路径有误." );
			}
		}
		
		if( path.size() - 1 != pathNode.size() ){
			System.out.println( "R型Fragment形成RPSTNode数量有误." );
		}
		for( int i=0 ; i<pathNode.size() ; i++ ){
			if( !(pathNode.get( i ).getType() == TCType.B ||
				pathNode.get( i ).getType() == TCType.P ||
				pathNode.get( i ).getType() == TCType.T )
					){
				System.out.println( "R型Fragment形成RPSTNode类型有误." );
			}
		}
		
		return pathNode;
	}
	
	public static Integer getSmallerNum( Integer num , ArrayList<Integer> vertexs ){
		for( int i=0 ; i<vertexs.size() ; i++ ){
			if( vertexs.get(i).intValue() == num.intValue() ){
				return i;
			}
		}
		return -1;
	}
	
	public static Integer getBiggerNum( Integer num , ArrayList<Integer> vertexs ){
		if( num >=0 && num < vertexs.size() ){
			return vertexs.get( num );
		}
		return -1;
	}

	public static boolean inArrayList( Integer num , ArrayList<Integer> array ){
		for( Integer n : array ){
			if( n.intValue() == num.intValue() ){
				return true;
			}
		}
		return false;
	} 
	
	/**
	 * Prints Text Structure.
	 */
	public static void printTextStructure(RPSTNode<ControlFlow, Node> root,
			int level, ProcessModel process, RPST<ControlFlow, Node> rpst) {

		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);

			if (PlanningHelper.isBond(node)) {
				printIndent(level);
				if (PlanningHelper.isLoop(node, rpst)) {
					System.out.println("LOOP [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (LOOP) ");
				}
				if (PlanningHelper.isSkip(node, rpst)) {
					System.out.println("SKIP [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (SKIP) ");
				}
				if (PlanningHelper.isXORSplit(node, rpst)) {
					System.out.println("XOR [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (XOR) ");
				}
				if (PlanningHelper.isANDSplit(node, rpst)) {
					System.out.println("AND [");
					printTextStructure(node, level + 1, process, rpst);
					printIndent(level);
					System.out.println("] (AND) ");
				}
			} else {
				if (PlanningHelper.isTask(node.getEntry())) {
					printIndent(level);
					Activity activity = (Activity) process.getActivity(Integer
							.parseInt(node.getEntry().getId()));
					Annotation anno = activity.getAnnotations().get(0);
//					System.out.println(anno.getActions().get(0) + " "
//							+ anno.getBusinessObjects().get(0) + " "
//							+ anno.getAddition());
					if (depth > 0) {
						printTextStructure(node, level, process, rpst);
					}
				} else {
					if (depth > 0) {
						printTextStructure(node, level, process, rpst);
					}
				}
			}
		}
	}

	/**
	 * Prints intend on screen (standard out).
	 */
	private static void printIndent(int level) {
		for (int i = 0; i < level; i++) {
			System.out.print("\t");
		}
	}

	/**
	 * Return next activity.
	 */
	public static RPSTNode<ControlFlow, Node> getNextNode(
			RPSTNode<ControlFlow, Node> root, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && PlanningHelper.isTrivial(node)) {
				return node;
			} else {
				return getNextNode(node, rpst);
			}
		}
		return null;
	}

	/**
	 * Determines activity count in RPST.
	 */
	public static int getActivityCount(RPSTNode<ControlFlow, Node> root,
			RPST<ControlFlow, Node> rpst) {
		int c = 0;
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (depth == 0 && (PlanningHelper.isTask(node.getEntry()))) {
				c++;
				;
			} else {
				c = c + getActivityCount(node, rpst);
			}
		}
		return c;
	}

	/**
	 * Print a given RPST Tree.
	 */
	public static void printTree(RPSTNode<ControlFlow, Node> root, int level,
			RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			for (int i = 0; i < level; i++) {
				System.out.print("\t");
			}

			// Determine type of node for presentation purposes
			String entryString = PlanningHelper.getNodeRepresentation(node
					.getEntry());
			String exitString = PlanningHelper.getNodeRepresentation(node
					.getExit());

			if (PlanningHelper.isBond(node)) {
				System.out.println(node.getName() + " ("
						+ PlanningHelper.getBondType(node, rpst) + "," + depth
						+ ", " + PlanningHelper.getSubLevelCount(node, rpst)
						+ ") [" + entryString + " --> " + exitString + "]");
			} else {
				System.out.println(node.getName() + " (" + depth + ", "
						+ PlanningHelper.getSubLevelCount(node, rpst) + ") ["
						+ entryString + " --> " + exitString + "]");
			}

			if (depth > 0) {
				printTree(node, level + 1, rpst);
			}
		}
	}

	public static boolean containsRigid(RPSTNode<ControlFlow, Node> root,
			int level, RPST<ControlFlow, Node> rpst) {
		ArrayList<RPSTNode<ControlFlow, Node>> orderedTopNodes = PlanningHelper
				.sortTreeLevel(root, root.getEntry(), rpst);
		for (RPSTNode<ControlFlow, Node> node : orderedTopNodes) {
			int depth = PlanningHelper.getDepth(node, rpst);
			if (isRigid(node)) {
				return true;
			}
			if (depth > 0) {
				printTree(node, level + 1, rpst);
			}
		}
		return false;
	}

}
