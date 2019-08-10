package org.processmining.analysis.epc.epcmetrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.ui.Message;

import att.grappa.Edge;
import att.grappa.Node;

/**
 * <p>
 * Title: EPC complexity analysis: Cross-Connectivity
 * </p>
 * 
 * <p>
 * Description: Calculates the Cross-Connectivity value for a process model
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2008
 * </p>
 * 
 * 
 * @author Irene Vanderfeesten, and Daniel Teixeira and Jo,o Sobrinho
 * @version 2.0
 */
public class CrossConnectivity implements ICalculator {

	private final static String TYPE = "Cohesion";
	private final static String NAME = "Cross-Connectivity";
	private ConfigurableEPC epc;
	private double result;
	private Vector paths = new Vector();

	public CrossConnectivity(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		Message.add("\t<CrossConnectivity>", Message.TEST);

		// Make a HashSet of all nodes in the EPC.
		ArrayList functions = this.epc.getFunctions();
		ArrayList events = this.epc.getEvents();
		ArrayList connectors = this.epc.getConnectors();
		HashSet nodes = new HashSet();
		Iterator it1 = functions.iterator();
		while (it1.hasNext()) {
			EPCFunction f = (EPCFunction) it1.next();
			nodes.add(f);
		}
		Iterator it2 = events.iterator();
		while (it2.hasNext()) {
			EPCEvent e = (EPCEvent) it2.next();
			nodes.add(e);
		}
		Iterator it3 = connectors.iterator();
		while (it3.hasNext()) {
			EPCConnector c = (EPCConnector) it3.next();
			nodes.add(c);
		}
		// calculate all paths
		Iterator it = nodes.iterator();
		while (it.hasNext()) {
			Node node = (Node) it.next();
			Vector path = new Vector();
			path.add(node);
			// System.out.println(node.toString());
			calculatePaths(path);

			/*
			 * Iterator itpa = paths.iterator(); while (itpa.hasNext()){ Vector
			 * v = (Vector) itpa.next(); Iterator itv = v.iterator(); while
			 * (itv.hasNext()) { Object o = itv.next(); if (o instanceof
			 * EPCEdge) { EPCEdge edge = (EPCEdge) o; String str =
			 * edge.toString(); System.out.print(str + " "); } else if (o
			 * instanceof Node) { // Node node1 = (Node) o; // String str =
			 * node1.toString(); // System.out.print(str + " "); } }
			 * System.out.println(); }
			 */

		}
		// calculate Cross-Connectivity value
		double conn = 0.0;
		Iterator itn1 = nodes.iterator();
		while (itn1.hasNext()) {
			Node n1 = (Node) itn1.next();
			Iterator itn2 = nodes.iterator();
			while (itn2.hasNext()) {
				Node n2 = (Node) itn2.next();
				// System.out.print(n1.toString()+ "->");
				// System.out.print(n2.toString()+ ": ");
				HashSet connections = new HashSet();
				Iterator itp = paths.iterator();
				while (itp.hasNext()) {
					Vector path = (Vector) itp.next();
					Node start = (Node) path.firstElement();
					Node end = (Node) path.lastElement();
					if (start.equals(n1) && end.equals(n2)) {
						connections.add(path);
					}
				}
				// calculate the maximum value over all paths connecting node
				// 'n1' and 'n2'
				Iterator itc = connections.iterator();
				double conValue = 0.0;
				while (itc.hasNext()) {
					Vector path = (Vector) itc.next();
					double pathValue = calculatePathValue(path);
					if (pathValue > conValue) {
						conValue = pathValue;
					}
				}
				conn = conn + conValue;
				connections.clear();
				// System.out.println(n1 + "->" + n2 + ": " + conValue);
				// Message.add("\t</PathValue \"" + n1 + "->" + n2 + ": " +
				// conValue + "\">", Message.TEST);
			}
		}
		int numNodes = nodes.size();
		result = conn / (numNodes * (numNodes - 1));
		Message.add("\t<CrossConnectivityValue value=\"" + result + "\"/>",
				Message.TEST);
		Message.add("\t</CrossConnectivity>", Message.TEST);
		return Double.toString(result);
	}

	public double calculatePathValue(Vector v) {
		double result = 0.0;
		double value = 1.0;
		Vector v2 = (Vector) v.clone();
		// System.out.print(v2.firstElement().toString() + " -> " +
		// v2.lastElement().toString() + ": ");
		while (v2.size() > 1) {
			Node n = (Node) v2.firstElement();
			v2.remove(n);
			EPCEdge edge = (EPCEdge) v2.firstElement();
			double weight = getArcWeight(edge);
			// System.out.print(weight + "*");
			v2.remove(edge);
			value = value * weight;
		}
		// System.out.println(" = " + value);
		result = value;
		// System.out.println("Path value =" + result);
		v2.clear();
		return result;
	}

	public void calculatePaths(Vector path) {
		Node node1 = (Node) path.lastElement();
		ArrayList<Edge> outEdges = node1.getOutEdges();
		int numOutEdges;
		if (outEdges == null) {
			numOutEdges = 0;
		} else
			numOutEdges = outEdges.size();
		if (numOutEdges > 0) {
			for (int i = 0; i < numOutEdges; i++) {
				Vector path2 = (Vector) path.clone();
				EPCEdge edge = (EPCEdge) outEdges.get(i);
				Node node3 = edge.getHead(); // the head of the arc points to
				// this node
				// Check whether the node is already in the path!
				boolean nodeExists = false;
				Iterator itp2 = path2.iterator();
				while (itp2.hasNext() && !(nodeExists)) {
					Object o = itp2.next();
					if (o.equals(node3)) {
						nodeExists = true;
					}
				}
				// If so, then stop And only add the edge and node to the path
				if (nodeExists) {
					path2.add(edge);
					path2.add(node3);
					paths.add(path2);
				}

				// If not, then add the edge and node to the path and add the
				// path to the set of paths
				if (!nodeExists) {
					path2.add(edge);
					path2.add(node3);
					paths.add(path2);
					calculatePaths(path2);
				}
			}
		}
	}

	/**
	 * Calculates the weight of the EPC-arc, based on the weight of the source
	 * and destination nodes.
	 * 
	 * @param edge
	 *            EPCEdge
	 * @return double
	 */
	public double getArcWeight(EPCEdge edge) {
		double result;
		Node source = edge.getTail();
		Node dest = edge.getHead();
		double sourceWeight = getNodeWeight(source);
		double destWeight = getNodeWeight(dest);
		result = sourceWeight * destWeight;
		// System.out.println("arc weight "+edge.toString() + ": " +result);
		// Message.add("\t</ArcWeight \"" +edge.toString() + ": " +result +
		// "\">", Message.TEST);
		return result;
	}

	/**
	 * Computes the weight of the given node from the EPC.
	 * 
	 * @param node
	 *            Node
	 * @return double
	 */
	public double getNodeWeight(Node node) {
		double result = 0.0;
		String name = node.toString();
		// Check how many edges are going out of this node
		int outEdges;
		ArrayList outs = node.getOutEdges();
		if (outs == null) {
			outEdges = 0;
		} else {
			outEdges = outs.size();
		}
		// Check how many edges are going into this node
		int inEdges;
		ArrayList ins = node.getInEdges();
		if (ins == null) {
			inEdges = 0;
		} else {
			inEdges = ins.size();
		}
		// Then, determine the degree of the node
		int degree = outEdges + inEdges;
		// And calculate the weight of the node based on the definition in the
		// CAiSE'08 paper
		if (name.startsWith("OR")) {
			double first = 1.0 / (double) (power(2, degree) - 1.0);
			double second = ((power(2, degree) - 1.0) - 1.0)
					/ (double) ((power(2, degree) - 1.0) * degree);
			result = first + second;
		} else if (name.startsWith("XOR")) {
			result = 1.0 / degree;
		} else
			result = 1.0;
		// System.out.println("node weight " + node.toString() + ": " +result);
		return result;
	}

	public String getName() {
		return this.NAME;
	}

	private int power(int base, int expoente) {
		int resultado = 1;
		for (int i = 0; i < expoente; i++) {
			resultado = resultado * base;
		}
		return resultado;
	}

	public String getType() {
		return this.TYPE;
	}

	public String VerifyBasicRequirements() {
		return ".";
	}

}
