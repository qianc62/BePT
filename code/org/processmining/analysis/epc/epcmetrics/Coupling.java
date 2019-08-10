package org.processmining.analysis.epc.epcmetrics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import org.processmining.framework.models.epcpack.*;
import org.processmining.framework.ui.Message;

import att.grappa.Edge;
import att.grappa.Node;

public class Coupling implements ICalculator {
	private final static String TYPE = "Coupling";
	private final static String NAME = "Weighted Coupling";
	private ConfigurableEPC epc;
	private Vector<Arc> ArcsVector;
	private double result;
	private Vector paths;

	public Coupling(ConfigurableEPC aEpc) {
		epc = aEpc;
	}

	public String Calculate() {
		Message.add("\t<WeightedCoupling>", Message.TEST);
		int numEdges = this.epc.getEdges().size();
		ArcsVector = new Vector<Arc>();
		paths = new Vector();
		for (int i = 0; i < numEdges; i++) {
			EPCEdge edge = (EPCEdge) this.epc.getEdges().get(i);
			Node headNode = edge.getHead();
			Node tailNode = edge.getTail();

			String tailName = tailNode.toString();
			double tailValue = 0;
			if (tailName.startsWith("OR")) {
				int outEdges = tailNode.getOutEdges().size();
				double first = 1 / (double) (power(2, outEdges) - 1);
				double second = ((power(2, outEdges) - 1) - 1)
						/ (double) ((power(2, outEdges) - 1) * outEdges);
				tailValue = first + second;
			} else if (tailName.startsWith("XOR")) {
				tailValue = 1 / (double) tailNode.getOutEdges().size();
			} else
				tailValue = 1;

			String headName = headNode.toString();
			double headValue = 0;
			if (headName.startsWith("OR")) {
				int inEdges = headNode.getInEdges().size();
				double first = 1 / (double) (power(2, inEdges) - 1);
				double second = ((power(2, inEdges) - 1) - 1)
						/ (double) ((power(2, inEdges) - 1) * inEdges);
				headValue = first + second;
			} else if (headName.startsWith("XOR")) {
				headValue = 1 / (double) headNode.getInEdges().size();
			} else
				headValue = 1;
			ArcsVector.add(new Arc(edge.hashCode(), headValue * tailValue));
			Message.add("\t\t<ArcValue name=\"" + edge.toString()
					+ "\" value=\"" + (headValue * tailValue) + "\"/>",
					Message.TEST);
		}
		result = 0;
		int numEvents = this.epc.getEvents().size();
		for (int i = 0; i < numEvents; i++) {
			EPCEvent event = (EPCEvent) this.epc.getEvents().get(i);
			Node node = (Node) event;
			Vector nodeVector = new Vector();
			nodeVector.add(node);
			ValueRecursive(1, nodeVector);
		}
		int numFunctions = this.epc.getFunctions().size();
		for (int i = 0; i < numFunctions; i++) {
			EPCFunction function = (EPCFunction) this.epc.getFunctions().get(i);
			Node node = (Node) function;
			Vector nodeVector = new Vector();
			nodeVector.add(node);
			ValueRecursive(1, nodeVector);
		}
		for (int count = 0; count < paths.size(); count++) {
			Double newdouble = (Double) ((Vector) paths.elementAt(count))
					.lastElement();
			result = result + (double) newdouble;
		}
		double numOfPossibleConnections = this.getNumberOfActivities()
				* (this.getNumberOfActivities() - 1);
		result = result / numOfPossibleConnections;
		Message.add("\t\t<WeightedCouplingValue value=\""
				+ Double.toString(result) + "\"/>", Message.TEST);
		Message.add("\t</WeightedCoupling>", Message.TEST);
		return Double.toString(result);
	}

	private void ValueRecursive(double value, Vector aNodeVector) {
		Node aNode = (Node) aNodeVector.lastElement();
		ArrayList<Edge> array = aNode.getOutEdges();
		int numOutEdges;
		if (array == null) {
			numOutEdges = 0;
		} else
			numOutEdges = aNode.getOutEdges().size();
		for (int i = 0; i < numOutEdges; i++) {
			Vector nodeVector = (Vector) aNodeVector.clone();
			Edge edge = aNode.getOutEdges().get(i);
			double newValue = value * getArcValue(edge.hashCode());
			if (edge.getHead().toString().startsWith("AND")
					|| edge.getHead().toString().startsWith("XOR")
					|| edge.getHead().toString().startsWith("OR")) {
				if (nodeVector.contains(edge.getHead())) {
					return;
				}
				nodeVector.add(edge.getHead());
				ValueRecursive(newValue, nodeVector);
			} else {
				nodeVector.add(edge.getHead());
				nodeVector.add(newValue);
				checkPaths(nodeVector);
			}
		}
	}

	private void checkPaths(Vector aVec) {
		for (int i = 0; i < paths.size(); i++) {
			if (((Vector) paths.elementAt(i)).firstElement().equals(
					aVec.firstElement())) {
				Node node1 = (Node) ((Vector) paths.elementAt(i))
						.elementAt(((Vector) paths.elementAt(i)).size() - 2);
				Node node2 = (Node) aVec.elementAt(aVec.size() - 2);
				if (node1.equals(node2)) {
					if (((Vector) paths.elementAt(i)).size() > aVec.size()) {
						Vector tempVec = (Vector) aVec.clone();
						paths.setElementAt(tempVec, i);
						return;
					} else {
						return;
					}
				}
			}
		}
		Vector tempVec = (Vector) aVec.clone();
		paths.add(tempVec);
	}

	public double getArcValue(int hash) {
		for (int i = 0; i < ArcsVector.size(); i++) {
			if (ArcsVector.elementAt(i).getHash() == hash) {
				return ArcsVector.elementAt(i).getValue();
			}
		}
		return 0;
	}

	public String getName() {
		return this.NAME;
	}

	private float getNumberOfActivities() {
		float result = this.epc.getEvents().size()
				+ this.epc.getFunctions().size();
		return result;
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

class Arc {
	private int hash;
	private double value;

	public Arc(int aHash, double aValue) {
		this.hash = aHash;
		this.value = aValue;
	}

	public int getHash() {
		return hash;
	}

	public void setHash(int hash) {
		this.hash = hash;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public String toString() {
		return "[" + this.getHash() + "," + this.getValue() + "]";
	}
}
